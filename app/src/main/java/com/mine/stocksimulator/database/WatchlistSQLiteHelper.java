package com.mine.stocksimulator.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class WatchlistSQLiteHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "watchlist.db";
    public static final int DB_VERSION = 1;

    public static final String WATCHLIST_TABLE = "WATCHLIST";
    public static final String COLUMN_TICKER = "TICKER";
    public static final String COLUMN_PRICE = "PRICE";
    public static final String COLUMN_CHANGE = "CHANGE";
    public static final String COLUMN_CHANGE_YTD = "CHANGE_YTD";

    public static final String CREATE_WATCHLIST_TABLE = "CREATE TABLE " + WATCHLIST_TABLE +
            "(" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_TICKER + " TEXT," +
            COLUMN_PRICE + " REAL," +
            COLUMN_CHANGE + " REAL," +
            COLUMN_CHANGE_YTD + " REAL)";

    public WatchlistSQLiteHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_WATCHLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
