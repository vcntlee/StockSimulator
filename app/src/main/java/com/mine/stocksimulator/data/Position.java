package com.mine.stocksimulator.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Position implements Parcelable {

    private int mId;
    private String mCompanyTicker;
    private double mPrice;
    private double mCost;
    private int mShares;
    private String mType;
    private double mPercentReturn;

    private double mTotalMkt;
    private double mTotalCost;

    public Position(){}
    public Position(int id, String ticker, double price, double cost, int shares,
                    String type, double percentReturn, double totalMkt, double totalCost){
        mId = id;
        mCompanyTicker = ticker;
        mPrice = price;
        mCost = cost;
        mShares = shares;
        mType = type;
        mPercentReturn = percentReturn;

        mTotalMkt = totalMkt;
        mTotalCost = totalCost;


    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

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

    public double getPercentReturn() {
        return mPercentReturn;
    }

    public void setPercentReturn(double percentReturn) {
        mPercentReturn = percentReturn;
    }

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

    public double getTotalMkt() {
        return mTotalMkt;
    }

    public void setTotalMkt(double totalMkt) {
        mTotalMkt = totalMkt;
    }

    public double getTotalCost() {
        return mTotalCost;
    }

    public void setTotalCost(double totalCost) {
        mTotalCost = totalCost;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mCompanyTicker);
        dest.writeDouble(mPrice);
        dest.writeDouble(mCost);
        dest.writeInt(mShares);
        dest.writeString(mType);
        dest.writeDouble(mPercentReturn);
    }

    private Position(Parcel in){
        mId = in.readInt();
        mCompanyTicker = in.readString();
        mPrice = in.readDouble();
        mCost = in.readDouble();
        mShares = in.readInt();
        mType = in.readString();
        mPercentReturn = in.readDouble();

    }

    public final static Creator<Position> CREATOR = new Creator<Position>() {
        @Override
        public Position createFromParcel(Parcel source) {
            return new Position(source) ;
        }

        @Override
        public Position[] newArray(int size) {
            return new Position[size];
        }
    };
}
