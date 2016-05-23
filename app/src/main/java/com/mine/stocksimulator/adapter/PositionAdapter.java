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
            holder.percent = (TextView) convertView.findViewById(R.id.percentPortfolioItem);
            //holder.triangle = (ImageView) convertView.findViewById(R.id.triangleImage);

            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        Position position = mPositions.get(index);

        holder.ticker.setText(position.getCompanyTicker());
        //holder.ticker.setTypeface(Typer.set(mContext).getFont(Font.ROBOTO_REGULAR));
        holder.numShares.setText(position.getType() + " "+ position.getShares());

        holder.price.setText("$ " + position.getPrice());
        holder.cost.setText("$ " + position.getCost());
        holder.percent.setText(position.getPercentReturn() + " %");

        Log.i(TAG + " Price", position.getPrice() + "");
        Log.i(TAG + " Cost", position.getCost() + "");


//        if (position.getPercentReturn() >= 0){
//            holder.triangle.setImageResource(R.drawable.triangle_up);
//        }
//        else{
//            holder.triangle.setImageResource(R.drawable.triangle_down);
//
//        }

        //holder.profit.setText(position.getPercentReturn() + " %");


        return convertView;

    }


    private static class ViewHolder{
        TextView ticker;
        TextView numShares;
        TextView price;
        TextView cost;
        //ImageView triangle;
        TextView percent;
    }

}
