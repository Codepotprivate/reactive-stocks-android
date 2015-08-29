package rx.android.stocks;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.stocks.model.Stock;
import rx.android.stocks.sentiments.SentimentsService;
import rx.android.stocks.service.StocksService;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements StocksService.GetStockRequester {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final long REFRESH_RATE = 30*1000;

    private static final String[] initialStocks = { "SSNLF", "AAPL", "GOOG" };

    private StocksAdapter adapter;
    private StocksService service = null;
    private ServiceConnection stockServiceConnection;
    private ServiceConnection sentimentServiceConnection;

    private final Handler handler = new Handler();

    private final Runnable refreshStocks =  new Runnable() {
        @Override
        public void run() {
            List<Stock> stocks = adapter.getStocks();
            for(Stock stock : stocks) {
                service.getStock(stock.getSymbol(), MainActivity.this);
            }
            handler.postDelayed(refreshStocks, REFRESH_RATE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ListView listView = (ListView) findViewById(R.id.list_view);
        View headerView = View.inflate(this, R.layout.add_stock_row, null);

        Button addButton = (Button) headerView.findViewById(R.id.button_add);
        final EditText editText = (EditText) headerView.findViewById(R.id.edit_stock_symbol);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().length() > 3) {
                    if (null != service) {
                        service.getStock(editText.getText().toString(), MainActivity.this);
                    }
                }
            }
        });

        listView.addHeaderView(headerView);


        // TODO: Implement service which
        Intent intent = new Intent(this, StocksService.class);
        stockServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                StocksService.Binder binder = (StocksService.Binder) service;
                MainActivity.this.service = binder.getService();
                adapter = new StocksAdapter(MainActivity.this);
                initStocks();
                listView.setAdapter(adapter);
                handler.postDelayed(refreshStocks, REFRESH_RATE);
                MainActivity.this.service.getStocks()
                        .sample(5, TimeUnit.SECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(MainActivity.this::onStockReady);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.e(TAG, "Failed to bind service");
                finish();
            }
        };
        bindService(intent, stockServiceConnection, BIND_AUTO_CREATE);
        intent = new Intent(this, SentimentsService.class);
        sentimentServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(TAG, "SentimentService connected");
                SentimentsService.Binder binder = (SentimentsService.Binder) service;
                SentimentsService sentimentsService = binder.getService();

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        bindService(intent, sentimentServiceConnection, BIND_AUTO_CREATE);
    }

    private void initStocks() {
        if (null == service) return;

        for(String symbol : initialStocks) {
            service.watchStock(symbol);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(refreshStocks);
        Observable.just("test").subscribe(string -> Toast.makeText(this, string, Toast.LENGTH_LONG).show(   ));
    }

    @Override
    public void onStockReady(Stock stock) {
        adapter.update(stock);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(stockServiceConnection);
        stockServiceConnection = null;
        unbindService(sentimentServiceConnection);
        sentimentServiceConnection = null;
    }
}
