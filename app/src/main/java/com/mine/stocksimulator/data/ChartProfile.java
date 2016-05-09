package com.mine.stocksimulator.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;


public class ChartProfile implements Parcelable{

    private static final String TAG = ChartProfile.class.getSimpleName();
    private ArrayList<String> mChartDates;
    private ArrayList<Double> mChartValues;

    private String mChartDate;
    private double mChartValue = 0.0;


    public ChartProfile(){
        mChartDates = new ArrayList<String>();
        mChartValues = new ArrayList<Double>();

    }

    public String getChartDate() {
        return mChartDate;
    }

    public void setChartDate(String chartDate) {
        mChartDate = chartDate;
    }

    public double getChartValue() {
        return mChartValue;
    }

    public void setChartValue(double chartValue) {
        mChartValue = chartValue;
    }

    public ArrayList<String> getChartDates() {
        return mChartDates;
    }

    public void setChartDates(ArrayList<String> chartDates) {
        mChartDates = chartDates;
    }

    public ArrayList<Double> getChartValues() {
        return mChartValues;
    }

    public void setChartValues(ArrayList<Double> chartValues) {
        mChartValues = chartValues;
    }

    public int getSizeDates(){
        return mChartDates.size();
    }

    public int getSizeValues(){
        return mChartValues.size();
    }

    public void addToDates(){
        if (mChartDate != null) {
            mChartDates.add(mChartDate);
        }
        else{
            mChartDates.add("");
        }
    }
    public void addToValues(){
        if (mChartValue != 0.0) {
            mChartValues.add(mChartValue);
        }
        else{
            mChartValues.add(0.0);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(mChartDates);
        dest.writeList(mChartValues);

    }
    private ChartProfile(Parcel source){
        this();
        source.readStringList(mChartDates);
        source.readList(mChartValues, null);

    }

    public static final Creator<ChartProfile> CREATOR = new Creator<ChartProfile>() {
        @Override
        public ChartProfile createFromParcel(Parcel source) {
            return new ChartProfile(source);
        }

        @Override
        public ChartProfile[] newArray(int size) {
            return new ChartProfile[size];
        }
    };


}
