package rx.android.stocks.util;

public interface StockQuote {
    public Double newPrice(Double lastPrice);

    public Double price();
}
