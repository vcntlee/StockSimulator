package com.mine.stocksimulator.data;

import android.os.Parcel;
import android.os.Parcelable;

public class AccountSummary implements Parcelable {

    private double mAvailableCash;
    private double mPortfolioValue;
    private double mPercentReturn;

    public AccountSummary(){}

    public double getAvailableCash() {
        return mAvailableCash;
    }

    public void setAvailableCash(double availableCash) {
        mAvailableCash = availableCash;
    }

    public double getPercentReturn() {
        return mPercentReturn;
    }

    public void setPercentReturn(double percentReturn) {
        mPercentReturn = percentReturn * 100;
    }

    public double getPortfolioValue() {
        return mPortfolioValue;
    }

    public void setPortfolioValue(double portfolioValue) {
        mPortfolioValue = portfolioValue;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(mAvailableCash);
        dest.writeDouble(mPortfolioValue);
        dest.writeDouble(mPercentReturn);
    }

    private AccountSummary(Parcel in){
        mAvailableCash = in.readDouble();
        mPortfolioValue = in.readDouble();
        mPercentReturn = in.readDouble();

    }

    public final static Creator<AccountSummary> CREATOR = new Creator<AccountSummary>() {
        @Override
        public AccountSummary createFromParcel(Parcel source) {
            return new AccountSummary(source);
        }

        @Override
        public AccountSummary[] newArray(int size) {
            return new AccountSummary[size];
        }
    };

}
