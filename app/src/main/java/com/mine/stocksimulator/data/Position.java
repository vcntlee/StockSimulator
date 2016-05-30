package com.mine.stocksimulator.data;

public class Position  {

    private static final String TAG = Position.class.getSimpleName();
    private int mId;
    private String mCompanyTicker;
    private double mPrice;
    private double mCost;
    private int mShares;
    private String mType;
    private double mPercentReturn;

    private double mTotalMkt;
    private double mTotalCost;

    public Position(){
        mShares = 0;
        mTotalCost = 0;
    }
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
        mPercentReturn = percentReturn * 100;
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

    public void setShares(int newShares) {
        mShares = newShares;
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

    public void setTotalMkt() {
        if (mType.equals("Long")) {
            mTotalMkt = mShares * mPrice;
        }else{
            mTotalMkt = mTotalCost - (mShares * mPrice) + mTotalCost;
        }

    }

    public double getTotalCost() {
        return mTotalCost;
    }

    public void setTotalCost(double newCost) {
        mTotalCost += newCost;
    }

    public void setWeightedCost(int newNumShares, double newPrice){
        double pastWeight = (double) mShares / (mShares + newNumShares);
        double pastWeightPrice = mCost * pastWeight;
        double newWeight = (double) newNumShares / (mShares + newNumShares);
        double newWeightPrice = newPrice * newWeight;
        double avgCost = pastWeightPrice + newWeightPrice;

        mCost = avgCost;

    }

//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeInt(mId);
//        dest.writeString(mCompanyTicker);
//        dest.writeDouble(mPrice);
//        dest.writeDouble(mCost);
//        dest.writeInt(mShares);
//        dest.writeString(mType);
//        dest.writeDouble(mPercentReturn);
//    }
//
//    private Position(Parcel in){
//        mId = in.readInt();
//        mCompanyTicker = in.readString();
//        mPrice = in.readDouble();
//        mCost = in.readDouble();
//        mShares = in.readInt();
//        mType = in.readString();
//        mPercentReturn = in.readDouble();
//
//    }
//
//    public final static Creator<Position> CREATOR = new Creator<Position>() {
//        @Override
//        public Position createFromParcel(Parcel source) {
//            return new Position(source) ;
//        }
//
//        @Override
//        public Position[] newArray(int size) {
//            return new Position[size];
//        }
//    };
}
