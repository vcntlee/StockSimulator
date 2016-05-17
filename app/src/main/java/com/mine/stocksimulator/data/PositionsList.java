package com.mine.stocksimulator.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class PositionsList implements Parcelable{

    private ArrayList<Position> mPositions;

    public PositionsList(){
        mPositions = new ArrayList<>();
    }

    public void addItem(Position position){
        mPositions.add(position);
    }

    public void removeItem(Position position){
        mPositions.remove(position);
    }

    public ArrayList<Position> getPositions() {
        return mPositions;
    }

    public void setPositions(ArrayList<Position> positions) {
        mPositions = positions;
    }

    public int getSize(){
        return mPositions.size();
    }




    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mPositions);
    }

    private PositionsList(Parcel in){
        this();
        in.readTypedList(mPositions, Position.CREATOR);
    }

    public final static Creator<PositionsList> CREATOR = new Creator<PositionsList>() {
        @Override
        public PositionsList createFromParcel(Parcel source) {
            return new PositionsList(source) ;
        }

        @Override
        public PositionsList[] newArray(int size) {
            return new PositionsList[size];
        }
    };
}
