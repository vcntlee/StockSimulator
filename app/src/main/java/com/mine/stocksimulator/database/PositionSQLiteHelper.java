package com.mine.stocksimulator.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class PositionSQLiteHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "positions.db";
    public static final int DB_VERSION = 1;

    public static final String POSITIONS_TABLE = "POSITIONS";
    public static final String COLUMN_TICKER = "TICKER";
    public static final String COLUMN_PRICE = "PRICE";
    public static final String COLUMN_TOTAL_MKT = "TOTAL_MKT";
    public static final String COLUMN_COST = "COST";
    public static final String COLUMN_TOTAL_COST = "TOTAL_COST";
    public static final String COLUMN_SHARES = "SHARES";
    public static final String COLUMN_TYPE = "TYPE";
    public static final String COLUMN_RETURN = "RETURN_P";
    public static String CREATE_POSITIONS_TABLE = "CREATE TABLE " + POSITIONS_TABLE +
            "(" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_TICKER + " TEXT," +
            COLUMN_PRICE + " REAL," +
            COLUMN_TOTAL_MKT + " REAL," +
            COLUMN_COST + " REAL," +
            COLUMN_TOTAL_COST + " REAL," +
            COLUMN_SHARES + " INTEGER," +
            COLUMN_TYPE + " TEXT," +
            COLUMN_RETURN + " REAL)";

    public PositionSQLiteHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_POSITIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
