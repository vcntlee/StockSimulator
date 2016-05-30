package com.mine.stocksimulator.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.mine.stocksimulator.data.Position;
import com.mine.stocksimulator.ui.TradeActivity;

import java.util.ArrayList;

public class PositionDataSource {

    private Context mContext;
    private PositionSQLiteHelper mSQLHelper;

    public PositionDataSource(Context context){
        mContext = context;
        mSQLHelper = new PositionSQLiteHelper(context);
    }

    private SQLiteDatabase open(){
        return mSQLHelper.getWritableDatabase();
    }
    private void close(SQLiteDatabase database){
        database.close();
    }

    public void create(Position position){
        SQLiteDatabase database = open();
        database.beginTransaction();

        ContentValues value = new ContentValues();
        value.put(PositionSQLiteHelper.COLUMN_TICKER, position.getCompanyTicker());
        value.put(PositionSQLiteHelper.COLUMN_PRICE, TradeActivity.round(position.getPrice(), 2));
        value.put(PositionSQLiteHelper.COLUMN_COST, TradeActivity.round(position.getPrice(), 2)); // this starts as the same price
        value.put(PositionSQLiteHelper.COLUMN_SHARES, position.getShares());
        value.put(PositionSQLiteHelper.COLUMN_TYPE, position.getType());
        value.put(PositionSQLiteHelper.COLUMN_RETURN, TradeActivity.round(position.getPercentReturn(),2));

        value.put(PositionSQLiteHelper.COLUMN_TOTAL_MKT, TradeActivity.round(position.getTotalMkt(),2));
        value.put(PositionSQLiteHelper.COLUMN_TOTAL_COST, TradeActivity.round(position.getTotalCost(),2));

        database.insert(PositionSQLiteHelper.POSITIONS_TABLE, null, value);

        database.setTransactionSuccessful();
        database.endTransaction();
        close(database);
    }


    public ArrayList<Position> retrieve(){
        SQLiteDatabase database = open();

        //String[] columns = {BaseColumns._ID, OpenPositionSQLiteHelper.COLUMN_TICKER,
        //        OpenPositionSQLiteHelper.COLUMN_PRICE};

        Cursor cursor = database.query(PositionSQLiteHelper.POSITIONS_TABLE,
                null, null, null, null, null, null);
        ArrayList<Position> positions = new ArrayList<>();
        if (cursor.moveToFirst()){
            do{
                Position position =
                        new Position(getIntFromColName(cursor, BaseColumns._ID),
                                getStrFromColName(cursor, PositionSQLiteHelper.COLUMN_TICKER),
                                getDoubleFromColName(cursor, PositionSQLiteHelper.COLUMN_PRICE),
                                getDoubleFromColName(cursor, PositionSQLiteHelper.COLUMN_COST),
                                getIntFromColName(cursor, PositionSQLiteHelper.COLUMN_SHARES),
                                getStrFromColName(cursor, PositionSQLiteHelper.COLUMN_TYPE),
                                getDoubleFromColName(cursor, PositionSQLiteHelper.COLUMN_RETURN),
                                getDoubleFromColName(cursor, PositionSQLiteHelper.COLUMN_TOTAL_MKT),
                                getDoubleFromColName(cursor, PositionSQLiteHelper.COLUMN_TOTAL_COST));
                positions.add(position);
            }while(cursor.moveToNext());
        }


        cursor.close();
        close(database);
        return positions;
    }

    public Position retrieveOne(String ticker){
        SQLiteDatabase database = open();
        Cursor cursor = database.rawQuery("SELECT * FROM " + PositionSQLiteHelper.POSITIONS_TABLE +
                " WHERE TICKER='" + ticker + "'", null);
        if (cursor.moveToFirst()){

            Position position = new Position(getIntFromColName(cursor, BaseColumns._ID),
                                        getStrFromColName(cursor, PositionSQLiteHelper.COLUMN_TICKER),
                                        getDoubleFromColName(cursor, PositionSQLiteHelper.COLUMN_PRICE),
                                        getDoubleFromColName(cursor, PositionSQLiteHelper.COLUMN_COST),
                                        getIntFromColName(cursor, PositionSQLiteHelper.COLUMN_SHARES),
                                        getStrFromColName(cursor, PositionSQLiteHelper.COLUMN_TYPE),
                                        getDoubleFromColName(cursor, PositionSQLiteHelper.COLUMN_RETURN),
                                        getDoubleFromColName(cursor, PositionSQLiteHelper.COLUMN_TOTAL_MKT),
                                        getDoubleFromColName(cursor, PositionSQLiteHelper.COLUMN_TOTAL_COST));

            cursor.close();
            close(database);
            return position;
        }
        else{
            cursor.close();
            close(database);
            //return new Position(-1, "", 0, 0, 0, "", 0);
            return null;
        }


    }

    public void update(Position position, double price, double cost, int shares,
                       double percentReturn, double totalMkt, double totalCost){
        SQLiteDatabase database = open();
        database.beginTransaction();

        ContentValues value = new ContentValues();
        if (price != -1) {
            value.put(PositionSQLiteHelper.COLUMN_PRICE, TradeActivity.round(price,2));
        }
        if (cost != -1) {
            value.put(PositionSQLiteHelper.COLUMN_COST, TradeActivity.round(cost,2)); // TODO need to fix??
        }
        if (shares != -1) {
            value.put(PositionSQLiteHelper.COLUMN_SHARES, shares);
        }
        if (percentReturn != -1) {
            value.put(PositionSQLiteHelper.COLUMN_RETURN, TradeActivity.round(percentReturn,2));
        }
        if (totalMkt != -1){
            value.put(PositionSQLiteHelper.COLUMN_TOTAL_MKT, TradeActivity.round(totalMkt,2));
        }
        if (totalCost != -1){
            value.put(PositionSQLiteHelper.COLUMN_TOTAL_COST, TradeActivity.round(totalCost,2));
        }
        database.update(PositionSQLiteHelper.POSITIONS_TABLE,
                value, String.format("%s=%d", BaseColumns._ID, position.getId()), null);

        database.setTransactionSuccessful();
        database.endTransaction();
        close(database);
    }

    public void delete (int positionId){
        SQLiteDatabase database = open();
        database.beginTransaction();

        database.delete(PositionSQLiteHelper.POSITIONS_TABLE,
                String.format("%s=%d", BaseColumns._ID, positionId), null);

        database.setTransactionSuccessful();
        database.endTransaction();
        close(database);
    }


    private int getIntFromColName(Cursor cursor, String columnName){
        int colIndex = cursor.getColumnIndex(columnName);
        return cursor.getInt(colIndex);
    }
    private String getStrFromColName(Cursor cursor, String colName){
        int colIndex = cursor.getColumnIndex(colName);
        return cursor.getString(colIndex);

    }
    private double getDoubleFromColName(Cursor cursor, String colName){
        int colIndex = cursor.getColumnIndex(colName);
        return cursor.getDouble(colIndex);
    }

    public double getTotal(String column){
        SQLiteDatabase database = open();

        Cursor cursor = database.rawQuery(String.format("SELECT SUM(%s) FROM POSITIONS", column), null);

        double total = 0;
        if (cursor.moveToFirst()){
            total = cursor.getDouble(0);
        }
        else{
            total = 0;
        }

        cursor.close();
        close(database);
        return total;
    }

}
