package com.mine.stocksimulator.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mine.stocksimulator.R;
import com.mine.stocksimulator.data.Position;

import java.util.ArrayList;


public class PositionAdapter extends BaseAdapter {

    private static final String TAG = PositionAdapter.class.getSimpleName();
    private Context mContext;
    private ArrayList<Position> mPositions;

    public PositionAdapter(Context context, ArrayList<Position> positions){
        mContext = context;
        mPositions = positions;
    }

    @Override
    public int getCount() {
        return mPositions.size();
    }

    @Override
    public Object getItem(int position) {
        return mPositions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int index, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.open_position_list_item, null);
            holder = new ViewHolder();
            holder.ticker = (TextView) convertView.findViewById(R.id.tickerLabelPortfolioItem);
            holder.numShares = (TextView) convertView.findViewById(R.id.numSharesLabelPortfolioItem);
            holder.price = (TextView) convertView.findViewById(R.id.priceLabelPortfolioItem);
            holder.cost = (TextView) convertView.findViewById(R.id.costLabelPortfolioItem);
            holder.profit = (TextView) convertView.findViewById(R.id.profitLabelPortfolioItem);
            //holder.percent = (TextView) convertView.findViewById(R.id.percentProfitItemLabel);

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        Position position = mPositions.get(index);

        holder.ticker.setText(position.getCompanyTicker());
        holder.numShares.setText(position.getType() + " "+ position.getShares());
        //holder.price.setText(100+"");
        holder.price.setText("$ " + position.getPrice());
        holder.cost.setText("$ " + position.getCost());

        Log.i(TAG + " Price", position.getPrice() + "");
        Log.i(TAG + " Cost", position.getCost() + "");

        holder.profit.setText(position.getPercentReturn() + " %");


        return convertView;

    }


    private static class ViewHolder{
        TextView ticker;
        TextView numShares;
        TextView price;
        TextView cost;
        TextView profit;
        //TextView percent;
    }

}
