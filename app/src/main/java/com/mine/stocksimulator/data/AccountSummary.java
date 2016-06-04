package com.mine.stocksimulator.data;

import com.mine.stocksimulator.ui.TradeActivity;

public class AccountSummary{

    private double mAvailableCash;
    //private double mPortfolioValue;
    private double mProfitLossValue;
    private double mPercentReturn;

    public AccountSummary(){}

    public double getAvailableCash() {
        return mAvailableCash;
    }

    public void setAvailableCash(double availableCash) {
        mAvailableCash = TradeActivity.round(availableCash,2);
    }

    public double getPercentReturn() {
        return mPercentReturn;
    }

    public void setPercentReturn(double percentReturn) {
        mPercentReturn = percentReturn;
    }

//    public double getPortfolioValue() {
//        return mPortfolioValue;
//    }
//
//    public void setPortfolioValue(double portfolioValue) {
//        mPortfolioValue = TradeActivity.round(portfolioValue,2);
//    }

    public double getProfitLossValue() {
        return mProfitLossValue;
    }

    public void setProfitLossValue(double profitLossValue) {
        mProfitLossValue = profitLossValue;
    }

//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeDouble(mAvailableCash);
//        dest.writeDouble(mPortfolioValue);
//        dest.writeDouble(mPercentReturn);
//    }
//
//    private AccountSummary(Parcel in){
//        mAvailableCash = in.readDouble();
//        mPortfolioValue = in.readDouble();
//        mPercentReturn = in.readDouble();
//
//    }
//
//    public final static Creator<AccountSummary> CREATOR = new Creator<AccountSummary>() {
//        @Override
//        public AccountSummary createFromParcel(Parcel source) {
//            return new AccountSummary(source);
//        }
//
//        @Override
//        public AccountSummary[] newArray(int size) {
//            return new AccountSummary[size];
//        }
//    };

}
