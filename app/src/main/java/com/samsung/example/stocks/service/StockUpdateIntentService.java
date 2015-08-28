package com.samsung.example.stocks.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;

import com.samsung.example.stocks.model.Stock;
import com.samsung.example.stocks.util.FakeStockQuote;

public class StockUpdateIntentService extends IntentService {
    private static final String ACTION_UPDATE = "com.samsung.example.stocks.action.UPDATE";
    private static final String ACTION_GET = "com.samsung.example.stocks.action.UPDATE";

    private static final String EXTRA_SYMBOL = "com.samsung.example.stocks.extra.SYMBOL";
    private static final String EXTRA_STOCK = "com.samsung.example.stocks.extra.STOCK";
    private static final String EXTRA_PRICE = "com.samsung.example.stocks.extra.PRICE";

    public static void startActionUpdate(Context context, Stock stock) {
        Intent intent = new Intent(context, StockUpdateIntentService.class);
        intent.setAction(ACTION_UPDATE);
        intent.putExtra(EXTRA_SYMBOL, stock.getSymbol());
        intent.putExtra(EXTRA_PRICE, stock.getPrice());
        context.startService(intent);
    }

    public static void startActionGet(Context context, String symbol) {
        Intent intent = new Intent(context, StockUpdateIntentService.class);
        intent.setAction(ACTION_GET);
        intent.putExtra(EXTRA_SYMBOL, symbol);
        context.startService(intent);
    }

    public StockUpdateIntentService() {
        super("StockUpdateIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE.equals(action)) {
                final String symbol = intent.getStringExtra(EXTRA_SYMBOL);
                final Double lastPrice = intent.getDoubleExtra(EXTRA_PRICE, 0.0);
                handleActionUpdate(symbol, lastPrice);
            } else if (ACTION_GET.equals(action)) {
                final String symbol = intent.getStringExtra(EXTRA_SYMBOL);
                handleActionGet(symbol);
            }
        }
    }

    private void handleActionGet(String symbol) {
        FakeStockQuote fakeStockQuote = new FakeStockQuote();
        Double price = fakeStockQuote.price();
        notifyPrice(symbol, price);
    }

    private void handleActionUpdate(String symbol, Double lastPrice) {
        FakeStockQuote fakeStockQuote = new FakeStockQuote();
        Double newPrice = fakeStockQuote.newPrice(lastPrice);
        notifyPrice(symbol, newPrice);
    }

    private void notifyPrice(String symbol, Double newPrice) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        Stock updated = Stock.create(symbol, newPrice);
        Intent intent = new Intent(Stock.ACTION_UPDATED);
        intent.putExtra(EXTRA_STOCK, updated);
        localBroadcastManager.sendBroadcast(intent);
    }
}
