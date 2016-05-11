package com.mine.stocksimulator.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mine.stocksimulator.R;
import com.mine.stocksimulator.data.SearchResult;

import java.util.ArrayList;

public class SearchResultAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<SearchResult> mSearchResults;

    public SearchResultAdapter(Context context, ArrayList<SearchResult> searchResults){
        mContext = context;
        mSearchResults = searchResults;
    }


    @Override
    public int getCount() {
        return mSearchResults.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.search_result_list_item, null);
            holder = new ViewHolder();
            holder.ticker = (TextView) convertView.findViewById(R.id.searchResultTickerLabel);
            holder.companyName = (TextView) convertView.findViewById(R.id.searchResultCompanyLabel);
            convertView.setTag(holder);
        }

        else{
            holder = (ViewHolder) convertView.getTag();
        }

        SearchResult searchResult = mSearchResults.get(position);
        holder.ticker.setText(searchResult.getTicker());
        holder.companyName.setText(searchResult.getCompanyName());


        return convertView;
    }

    private static class ViewHolder{
        TextView ticker;
        TextView companyName;
    }
}
