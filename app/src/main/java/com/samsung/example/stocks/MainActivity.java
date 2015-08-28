package com.samsung.example.stocks;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.samsung.example.stocks.model.Stock;
import com.samsung.example.stocks.service.StocksService;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements StocksService.GetStockRequester {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final long REFRESH_RATE = 30*1000;

    private StocksAdapter adapter;
    private StocksService service = null;

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

        Intent intent = new Intent(this, StocksService.class);
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                StocksService.Binder binder = (StocksService.Binder) service;
                MainActivity.this.service = binder.getService();
                adapter = prepareAdapter();
                listView.setAdapter(adapter);
                handler.postDelayed(refreshStocks, REFRESH_RATE);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.e(TAG, "Failed to bind service");
                finish();
            }
        };
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(refreshStocks);
    }

    private StocksAdapter prepareAdapter() {
        if (null == service) return null;

        Stock[] symbols = {
                Stock.create("AAPL", 109.69),
                Stock.create("SSNLF", 969.40)
            };
        List<Stock> stockList = Arrays.asList(symbols);
        Log.d(TAG, "Preparing adapter with Stock list = [" + stockList + "]");
        StocksAdapter adapter = new StocksAdapter(this, stockList);

        return adapter;
    }

    @Override
    public void onStockReady(Stock stock) {
        adapter.update(stock);
    }
}
