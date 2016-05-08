package com.mine.stocksimulator;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class OpenPositionAdapter extends BaseAdapter {

    private static final String TAG = OpenPositionAdapter.class.getSimpleName();
    private Context mContext;
    private ArrayList<OpenPosition> mOpenPositions;

    public OpenPositionAdapter(Context context, ArrayList<OpenPosition> openPositions){
        mContext = context;
        mOpenPositions = openPositions;
    }

    @Override
    public int getCount() {
        return mOpenPositions.size();
    }

    @Override
    public Object getItem(int position) {
        return mOpenPositions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

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

        OpenPosition openPosition = mOpenPositions.get(position);

        holder.ticker.setText(openPosition.getCompanyTicker());
        holder.numShares.setText(openPosition.getShares()+"");
        //holder.price.setText(100+"");
        holder.price.setText(openPosition.getPrice()+"");
        holder.cost.setText(openPosition.getPrice() + "");

        Log.i(TAG + " Price", openPosition.getPrice() + "");
        Log.i(TAG + " Cost", openPosition.getCost() + "");

        holder.profit.setText((openPosition.getPrice() - openPosition.getCost()) + "");


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
