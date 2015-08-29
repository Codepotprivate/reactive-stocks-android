package rx.android.stocks.util;

import java.util.Random;

public class FakeStockQuote implements StockQuote {
    private final Random random = new Random();

    @Override
    public Double newPrice(Double lastPrice) {
        return lastPrice * (0.95 + (0.1 * random.nextDouble()));
    }

    @Override
    public Double price() {
        return random.nextDouble() * random.nextInt(100);
    }
}
