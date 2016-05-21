package com.mine.stocksimulator.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.mine.stocksimulator.data.Watchlist;

import java.util.ArrayList;

public class WatchlistDataSource {

    private Context mContext;
    private WatchlistSQLiteHelper mSQLHelper;

    public WatchlistDataSource(Context context){
        mContext = context;
        mSQLHelper = new WatchlistSQLiteHelper(context);
    }

    private SQLiteDatabase open(){
        return mSQLHelper.getWritableDatabase();
    }

    private void close(SQLiteDatabase database){
        database.close();
    }

    public void create(Watchlist watchlist){

        SQLiteDatabase database = open();
        database.beginTransaction();

        ContentValues value = new ContentValues();
        value.put(WatchlistSQLiteHelper.COLUMN_TICKER, watchlist.getTicker());
        value.put(WatchlistSQLiteHelper.COLUMN_PRICE, watchlist.getPrice());
        value.put(WatchlistSQLiteHelper.COLUMN_CHANGE, watchlist.getChange());
        value.put(WatchlistSQLiteHelper.COLUMN_CHANGE_YTD, watchlist.getChangeYtd());

        database.insert(WatchlistSQLiteHelper.WATCHLIST_TABLE, null, value);

        database.setTransactionSuccessful();
        database.endTransaction();
        close(database);

    }

    public ArrayList<Watchlist> retrieve(){

        SQLiteDatabase database = open();
        Cursor cursor = database.query(WatchlistSQLiteHelper.WATCHLIST_TABLE, null, null, null, null, null, null, null);

        ArrayList<Watchlist> watchlists = new ArrayList<>();

        if (cursor.moveToFirst()){
            do{
                Watchlist watchlist = new Watchlist(getIntFromColName(cursor, BaseColumns._ID),
                        getStrFromColName(cursor, WatchlistSQLiteHelper.COLUMN_TICKER),
                        getDoubleFromColName(cursor, WatchlistSQLiteHelper.COLUMN_PRICE),
                        getDoubleFromColName(cursor, WatchlistSQLiteHelper.COLUMN_CHANGE),
                        getDoubleFromColName(cursor, WatchlistSQLiteHelper.COLUMN_CHANGE_YTD));
                watchlists.add(watchlist);
            }while(cursor.moveToNext());
        }
        cursor.close();
        close(database);
        return watchlists;
    }

    public boolean retrieveOne(String ticker){
        SQLiteDatabase database = open();
        Cursor cursor = database.rawQuery("SELECT " + WatchlistSQLiteHelper.COLUMN_TICKER + " FROM " + WatchlistSQLiteHelper.WATCHLIST_TABLE + " WHERE TICKER='" + ticker + "'", null);
        if (cursor.moveToFirst()){
            return true;
        }
        else{
            return false;
        }
    }

    public void update(Watchlist watchlist){

        SQLiteDatabase database = open();
        database.beginTransaction();

        ContentValues value = new ContentValues();

        value.put(WatchlistSQLiteHelper.COLUMN_PRICE, watchlist.getPrice());
        value.put(WatchlistSQLiteHelper.COLUMN_CHANGE, watchlist.getChange());
        value.put(WatchlistSQLiteHelper.COLUMN_CHANGE_YTD, watchlist.getChangeYtd());

        database.update(WatchlistSQLiteHelper.WATCHLIST_TABLE, value, String.format("%s=%d", BaseColumns._ID, watchlist.getId()), null);

        database.setTransactionSuccessful();
        database.endTransaction();
        close(database);

    }

    public void delete(String ticker) {

        SQLiteDatabase database = open();
        database.beginTransaction();

        database.delete(WatchlistSQLiteHelper.WATCHLIST_TABLE, String.format("TICKER='%s'", ticker), null);

        database.setTransactionSuccessful();
        database.endTransaction();
        close(database);

    }



    private int getIntFromColName(Cursor cursor, String colName){
        int colIndex = cursor.getColumnIndex(colName);
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

}
