package rx.android.stocks.service;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

import rx.Observable;
import rx.android.stocks.model.Stock;
import rx.android.stocks.util.FakeStockQuote;
import rx.schedulers.Schedulers;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

public class StocksService extends Service {
    private final HashMap<String, Stock> stocks = new LinkedHashMap<>();

    public StocksService() {
    }

    private synchronized Stock getLastStockQuote(String symbol) {
        return stocks.get(symbol);
    }

    private synchronized Stock putStock(Stock stock) {
        return stocks.put(stock.getSymbol(), stock);
    }

    public class Binder extends android.os.Binder {
        public StocksService getService() {
            return StocksService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new Binder();
    }

    private Observable<Stock> getStockObservable(Stock stock) {
        //TODO: Create observable using the OnSubscribe
        return Observable.empty();
    }

    public void getStock(String symbol, GetStockRequester callback) {
        Stock stock = getLastStockQuote(symbol);
        FakeStockQuote fakeStockQuote = new FakeStockQuote();
        if (null == stock) {
            stock = Stock.create(symbol, fakeStockQuote.price());
        }
        AsyncTask<GetStockRequest, Void, GetStockResult> getStockDataTask = new GetStockDataTask();
        GetStockRequest request = new GetStockRequest(stock, callback);
        getStockDataTask.execute(request);
    }

    public void watchStock(String symbol) {
        Stock stock = getLastStockQuote(symbol);
        if (null == stock) {
            FakeStockQuote fakeStockQuote = new FakeStockQuote();
            stock = Stock.create(symbol, fakeStockQuote.price());
        }
        stocks.put(stock.getSymbol(), stock);
    }

    public Observable<Stock> getStocks() {

        // TODO: create observable returning
        return Observable.interval(0, 500, TimeUnit.MILLISECONDS)
                .flatMap(aLong -> Observable.from(stocks.values()))
                .flatMap(stock -> getStockObservable(stock)).subscribeOn(Schedulers.computation());
    }


    private class GetStockDataTask extends AsyncTask<GetStockRequest, Void, GetStockResult> {

        @Override
        protected GetStockResult doInBackground(GetStockRequest... params) {
            GetStockRequest request = params[0];
            if (request == null) {
                return null;
            }
            FakeStockQuote fakeStockQuote = new FakeStockQuote();
            Stock stock = request.stock;
            stock.setPrice(fakeStockQuote.newPrice(stock.getPrice()));
            return new GetStockResult(stock, request.callback);
        }

        @Override
        protected void onPostExecute(GetStockResult stockResult) {
            if (stockResult == null) return;
            if (null != stockResult) putStock(stockResult.stock);
            stockResult.callback.onStockReady(stockResult.stock);
        }
    }

    private static class GetStockRequest {
        final Stock stock;
        final GetStockRequester callback;

        public GetStockRequest(Stock stock, GetStockRequester callback) {
            this.stock = stock;
            this.callback = callback;
        }
    }

    private static class GetStockResult {
        final Stock stock;
        final GetStockRequester callback;

        public GetStockResult(Stock stock, GetStockRequester callback) {
            this.stock = stock;
            this.callback = callback;
        }
    }

    public interface GetStockRequester {
        void onStockReady(Stock stock);
    }
}
