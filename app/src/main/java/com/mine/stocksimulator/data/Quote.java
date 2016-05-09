package com.mine.stocksimulator.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Quote implements Parcelable{

    private String mName;
    private String mSymbol;
    private double mPrice;
    private double mAbsoluteChange;
    private double mPercentChange;
    private long mMarketCap;
    private long mVolume;
    private double mChangeYtd;
    private double mChangePercentYtd;
    private double mHigh;
    private double mLow;
    private double mOpen;

    public Quote(){}

    public double getAbsoluteChange() {
        return mAbsoluteChange;
    }

    public void setAbsoluteChange(double absoluteChange) {
        mAbsoluteChange = absoluteChange;
    }

    public double getChangePercentYtd() {
        return mChangePercentYtd;
    }

    public void setChangePercentYtd(double changePercentYtd) {
        mChangePercentYtd = changePercentYtd;
    }

    public double getChangeYtd() {
        return mChangeYtd;
    }

    public void setChangeYtd(double changeYtd) {
        mChangeYtd = changeYtd;
    }

    public double getHigh() {
        return mHigh;
    }

    public void setHigh(double high) {
        mHigh = high;
    }

    public double getLow() {
        return mLow;
    }

    public void setLow(double low) {
        mLow = low;
    }

    public long getMarketCap() {
        return mMarketCap;
    }

    public void setMarketCap(long marketCap) {
        mMarketCap = marketCap;
    }

    public double getOpen() {
        return mOpen;
    }

    public void setOpen(double open) {
        mOpen = open;
    }

    public double getPercentChange() {
        return mPercentChange;
    }

    public void setPercentChange(double percentChange) {
        mPercentChange = percentChange;
    }

    public long getVolume() {
        return mVolume;
    }

    public void setVolume(long volume) {
        mVolume = volume;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public double getPrice() {
        return mPrice;
    }

    public void setPrice(double price) {
        mPrice = price;
    }


    public String getSymbol() {
        return mSymbol;
    }

    public void setSymbol(String symbol) {
        mSymbol = symbol;
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mSymbol);
        dest.writeDouble(mPrice);
        dest.writeDouble(mAbsoluteChange);
        dest.writeDouble(mPercentChange);
        dest.writeLong(mMarketCap);
        dest.writeLong(mVolume);
        dest.writeDouble(mChangeYtd);
        dest.writeDouble(mChangePercentYtd);
        dest.writeDouble(mHigh);
        dest.writeDouble(mLow);
        dest.writeDouble(mOpen);

    }

    private Quote(Parcel in){
        mName = in.readString();
        mSymbol = in.readString();
        mPrice = in.readDouble();
        mAbsoluteChange = in.readDouble();
        mPercentChange = in.readDouble();
        mMarketCap = in.readLong();
        mVolume = in.readLong();
        mChangeYtd = in.readDouble();
        mChangePercentYtd = in.readDouble();
        mHigh = in.readDouble();
        mLow = in.readDouble();
        mOpen = in.readDouble();

    }

    public static final Creator<Quote> CREATOR = new Creator<Quote>() {
        @Override
        public Quote createFromParcel(Parcel source) {
            return new Quote(source);
        }

        @Override
        public Quote[] newArray(int size) {
            return new Quote[size];
        }
    };
}
