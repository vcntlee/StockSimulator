package com.mine.stocksimulator.data;

import android.os.Parcel;
import android.os.Parcelable;

public class OpenPosition implements Parcelable {

    private String mCompanyTicker;
    private double mPrice;
    private double mCost;
    //private double mProfit;
    private int mShares;
    //private double mPnL;
    private String mType;

    public OpenPosition(){}

    public String getCompanyTicker() {
        return mCompanyTicker;
    }

    public void setCompanyTicker(String companyTicker) {
        mCompanyTicker = companyTicker;
    }

    public double getCost() {
        return mCost;
    }

    public void setCost(double cost) {
        mCost = cost;
    }

//    public double getPnL() {
//        return mPnL;
//    }
//
//    public void setPnL(double pnL) {
//        mPnL = pnL;
//    }

    public double getPrice() {
        return mPrice;
    }

    public void setPrice(double price) {
        mPrice = price;
    }

    public int getShares() {
        return mShares;
    }

    public void setShares(int shares) {
        mShares = shares;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mCompanyTicker);
        dest.writeDouble(mPrice);
        dest.writeDouble(mCost);
        dest.writeInt(mShares);
        dest.writeString(mType);
    }

    private OpenPosition(Parcel in){
        mCompanyTicker = in.readString();
        mPrice = in.readDouble();
        mCost = in.readDouble();
        mShares = in.readInt();
        mType = in.readString();
    }

    public final static Creator<OpenPosition> CREATOR = new Creator<OpenPosition>() {
        @Override
        public OpenPosition createFromParcel(Parcel source) {
            return new OpenPosition(source) ;
        }

        @Override
        public OpenPosition[] newArray(int size) {
            return new OpenPosition[size];
        }
    };
}
