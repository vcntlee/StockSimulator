package com.mine.stocksimulator.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class OpenPositionsList implements Parcelable{

    private ArrayList<OpenPosition> mOpenPositions;

    public OpenPositionsList(){
        mOpenPositions = new ArrayList<>();
    }

    public void addItem(OpenPosition position){
        mOpenPositions.add(position);
    }

    public void removeItem(OpenPosition position){
        mOpenPositions.remove(position);
    }

    public ArrayList<OpenPosition> getOpenPositions() {
        return mOpenPositions;
    }

    public void setOpenPositions(ArrayList<OpenPosition> openPositions) {
        mOpenPositions = openPositions;
    }

    public int getSize(){
        return mOpenPositions.size();
    }




    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mOpenPositions);
    }

    private OpenPositionsList(Parcel in){
        this();
        in.readTypedList(mOpenPositions, OpenPosition.CREATOR);
    }

    public final static Creator<OpenPositionsList> CREATOR = new Creator<OpenPositionsList>() {
        @Override
        public OpenPositionsList createFromParcel(Parcel source) {
            return new OpenPositionsList(source) ;
        }

        @Override
        public OpenPositionsList[] newArray(int size) {
            return new OpenPositionsList[size];
        }
    };
}
