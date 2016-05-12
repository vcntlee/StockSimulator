package com.mine.stocksimulator.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mine.stocksimulator.R;
import com.mine.stocksimulator.data.StockProfileFieldMember;


public class StockProfileAdapter extends BaseAdapter{

    private Context mContext;
    private StockProfileFieldMember[] mFieldMembers;

    public StockProfileAdapter(Context context, StockProfileFieldMember[] fieldMembers){
        mContext = context;
        mFieldMembers = fieldMembers;
    }

    @Override
    public int getCount() {
        return mFieldMembers.length;
    }

    @Override
    public Object getItem(int position) {
        return mFieldMembers[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.stockprofile_list_item, null);
            holder = new ViewHolder();
            holder.leftValue = (TextView) convertView.findViewById(R.id.leftLabel);
            holder.rightValue = (TextView) convertView.findViewById(R.id.rightLabel);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        StockProfileFieldMember fieldMember = mFieldMembers[position];
        holder.leftValue.setText(fieldMember.getLeftValue());
        holder.rightValue.setText(fieldMember.getRightValue());

        return convertView;
    }

    private static class ViewHolder{
        TextView leftValue;
        TextView rightValue;
    }
}
