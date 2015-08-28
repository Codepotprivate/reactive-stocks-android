package com.samsung.example.stocks;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.samsung.example.stocks.model.Stock;

import java.util.LinkedList;
import java.util.List;

public class StocksAdapter extends ArrayAdapter<Stock> {
    private static final String TAG = StocksAdapter.class.getSimpleName();

    private final LinkedList<Stock> stocks = new LinkedList<>();
    private final Activity context;

    public StocksAdapter(Activity context, List<Stock> stocks) {
        super(context, R.layout.stock_item);
        this.stocks.addAll(stocks);
        this.context = context;
    }

    public StocksAdapter(Activity context) {
        this(context, new LinkedList<Stock>());
    }

    static class ViewHolder {
        public TextView symbolView;
        public TextView priceView;
    }

    @Override
    public int getCount() {
        return stocks.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "getView for " + position + " ...");
        Log.d(TAG, "... with convertView = [" + convertView + "] ...");
        View itemView = convertView;
        if (null == itemView) {
            Log.d(TAG, "Creating new view ...");
            LayoutInflater inflater = context.getLayoutInflater();
            itemView = inflater.inflate(R.layout.stock_item, null);
            ViewHolder holder = new ViewHolder();
            holder.symbolView = (TextView) itemView.findViewById(R.id.stock_symbol);
            Log.d(TAG, "SymbolView = " + holder.symbolView);
            holder.priceView = (TextView) itemView.findViewById(R.id.stock_price);
            Log.d(TAG, "priceView = " + holder.priceView);
            itemView.setTag(holder);
        }

        ViewHolder holder = (ViewHolder) itemView.getTag();
        Stock stock = stocks.get(position);
        holder.symbolView.setText(stock.getSymbol());
        holder.priceView.setText(String.valueOf(stock.getPrice()));
        return itemView;
    }

    public void update(Stock stock) {
        Log.d(TAG, "Update stock ...");
        int index = stocks.indexOf(stock);
        if (index != -1) {
            stocks.set(index, stock);
        } else {
            stocks.add(stock);
        }
        notifyDataSetChanged();
    }

    public List<Stock> getStocks() {
        return stocks;
    }
}
