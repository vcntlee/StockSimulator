package com.mine.stocksimulator.data;

public class Watchlist {

    private int mId;
    private String mTicker;
    private double mPrice;
    private double mChange;
    private double mChangeYtd;

    public Watchlist(String ticker, double price, double change, double changeYtd){
        mTicker = ticker;
        mPrice = price;
        mChange = change;
        mChangeYtd = changeYtd;
    }
    public Watchlist(int id, String ticker, double price, double change, double changeYtd){
        mId = id;
        mTicker = ticker;
        mPrice = price;
        mChange = change;
        mChangeYtd = changeYtd;
    }

    public double getChange() {
        return mChange;
    }

    public void setChange(double change) {
        mChange = change;
    }

    public double getChangeYtd() {
        return mChangeYtd;
    }

    public void setChangeYtd(double changeYtd) {
        mChangeYtd = changeYtd;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public double getPrice() {
        return mPrice;
    }

    public void setPrice(double price) {
        mPrice = price;
    }

    public String getTicker() {
        return mTicker;
    }

    public void setTicker(String ticker) {
        mTicker = ticker;
    }
}
