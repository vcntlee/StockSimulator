package com.mine.stocksimulator.data;

import java.util.HashMap;
import java.util.Map;

public class OpenPositionsMap {

    Map<String, String> mOpenPositionsMap;

    public OpenPositionsMap(){
        mOpenPositionsMap = new HashMap<>();
    }

    public Map<String, String> getOpenPositionsMap() {
        return mOpenPositionsMap;
    }

    public void setOpenPositionsMap(Map<String, String> openPositionsMap) {
        mOpenPositionsMap = openPositionsMap;
    }

    // read a key

    public boolean doesKeyExist(String ticker){
        String value = mOpenPositionsMap.get(ticker);
        if (value != null){
            return true;
        }
        else{
            return false;
        }
    }

    public String getKey(String ticker){
        if (doesKeyExist(ticker)){
            String value = mOpenPositionsMap.get(ticker);
            return value;
        }
        else{
            return "";
        }
    }

    // set a key
    public void setKey(String ticker, String type){
        mOpenPositionsMap.put(ticker, type);
    }


    // erase a key

    public void removeKey(String ticker){
        mOpenPositionsMap.remove(ticker);
    }
}
