package com.mine.stocksimulator.data;

public class SearchResult {

    private String mTicker;
    private String mCompanyName;


    public SearchResult(){}

    public String getTicker() {
        return mTicker;
    }

    public void setTicker(String ticker) {
        mTicker = ticker;
    }

    public String getCompanyName() {
        return mCompanyName;
    }

    public void setCompanyName(String companyName) {
        mCompanyName = companyName;
    }
}
