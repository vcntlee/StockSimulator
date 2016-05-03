package com.mine.stocksimulator;

import java.util.ArrayList;

public class OpenPositionsContainer {

    private ArrayList<OpenPosition> mOpenPositions;

    public OpenPositionsContainer(){
        mOpenPositions = new ArrayList<>();
    }

    public void addItem(OpenPosition position){
        mOpenPositions.add(position);
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





}
