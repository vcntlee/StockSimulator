package com.mine.stocksimulator.data;


// this class is to separate the 12 fields that Profile has and make it fit into a listview
public class StockProfileFieldMember {
    private String mLeftValue;
    private String mRightValue;

    public String getLeftValue() {
        return mLeftValue;
    }

    public void setLeftValue(String leftValue) {
        mLeftValue = leftValue;
    }

    public String getRightValue() {
        return mRightValue;
    }

    public void setRightValue(String rightValue) {
        mRightValue = rightValue;
    }
}
