package com.mine.stocksimulator.data;

import java.util.ArrayList;

public class SearchResultsContainer {

    private ArrayList<SearchResult> mSearchResults;

    public SearchResultsContainer(){
        mSearchResults = new ArrayList<>();
    }


    public ArrayList<SearchResult> getSearchResults() {
        return mSearchResults;
    }

    public void setSearchResults(ArrayList<SearchResult> searchResults) {
        mSearchResults = searchResults;
    }


    public void addItem(SearchResult result){
        mSearchResults.add(result);
    }

    public int getSize(){
        return mSearchResults.size();
    }
}
