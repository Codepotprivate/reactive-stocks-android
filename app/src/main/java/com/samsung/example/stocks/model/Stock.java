package com.samsung.example.stocks.model;

import java.io.Serializable;

public class Stock implements Serializable {

    protected static final long serialVersionUID = 1L;

    public static final String ACTION_UPDATED = "com.samsung.example.stocks.action.UPDATED";

    private final String symbol;
    private double price;

    private Stock(String symbol, double price) {
        this.symbol = symbol;
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Stock)) {
            return false;
        }

        Stock stock = (Stock) o;
        return this.symbol.equals(stock.symbol);
    }

    public static Stock create(String symbol, double price) {
        return new Stock(symbol, price);
    }

    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean hasChanged(Stock that) {
        return this.symbol.equals(that.symbol) && this.price == that.price;
    }
}
