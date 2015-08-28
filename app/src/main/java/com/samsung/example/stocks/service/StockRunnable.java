package com.samsung.example.stocks.service;

import android.os.Handler;

import java.util.Random;

public class StockRunnable extends Thread {
    private double lastPrice;
    private Handler handler;

    public StockRunnable(Handler handler, double lastPrice) {
        this.handler = handler;
        Random random = new Random();
        this.lastPrice = lastPrice;
    }

    @Override
    public void run() {

    }


}
