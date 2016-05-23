package com.mine.stocksimulator.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mine.stocksimulator.R;
import com.mine.stocksimulator.data.Watchlist;

import java.util.ArrayList;

public class WatchlistAdapter extends BaseAdapter {

    private static final String TAG = WatchlistAdapter.class.getSimpleName();
    private Context mContext;
    private ArrayList<Watchlist> mWatchlists;

    public WatchlistAdapter(Context context, ArrayList<Watchlist> watchlists){
        mContext = context;
        mWatchlists = watchlists;
    }

    @Override
    public int getCount() {
        return mWatchlists.size();
    }

    @Override
    public Object getItem(int position) {
        return mWatchlists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int index, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.watchlist_item,null);
            holder = new ViewHolder();
            holder.ticker = (TextView) convertView.findViewById(R.id.tickerLabel);
            holder.triangle = (ImageView) convertView.findViewById(R.id.triangleImage);
            holder.price = (TextView) convertView.findViewById(R.id.priceLabel);
            holder.change = (TextView) convertView.findViewById(R.id.changeLabel);
            //holder.changeYtd = (TextView) convertView.findViewById(R.id.changeYtdLabel);

            convertView.setTag(holder);
        }

        else{
            holder = (ViewHolder) convertView.getTag();
        }

        //TODO move the if else into the Watchlist POJO

        Watchlist watchlist = mWatchlists.get(index);
        holder.ticker.setText(watchlist.getTicker());
        holder.price.setText("$ " + watchlist.getPrice());
        holder.change.setText(watchlist.getChange() + " %");
        if (watchlist.getChange() > 0) {
            holder.triangle.setImageResource(R.drawable.triangle_up);
            holder.change.setTextColor(Color.parseColor("#4dd14d"));
        }
        else if (watchlist.getChange() < 0){
            holder.triangle.setImageResource(R.drawable.triangle_down);
            holder.change.setTextColor(Color.parseColor("#f1575a"));
        }
        else{
            holder.triangle.setVisibility(View.INVISIBLE);
        }

        //holder.changeYtd.setText(watchlist.getChangeYtd()+ " %");

        return convertView;
    }

    private static class ViewHolder{

        TextView ticker;
        ImageView triangle;
        TextView price;
        TextView change;
        //TextView changeYtd;

    }
}
