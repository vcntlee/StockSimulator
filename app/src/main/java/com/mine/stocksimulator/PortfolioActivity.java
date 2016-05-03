package com.mine.stocksimulator;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.gson.Gson;

public class PortfolioActivity extends ListActivity {

    public static final String TAG = PortfolioActivity.class.getSimpleName();
    private static final String PREFS_FILE = "com.mine.stocksimulator.prefs_file" ;
    private static final String POSITIONS_ARRAY = "POSITIONS_ARRAY";
    private Button mBuyButton;
    private Button mShortButton;
    private OpenPosition mPosition = new OpenPosition();
    private OpenPositionsContainer mPositions = new OpenPositionsContainer();
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;


//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        Log.i(TAG, "entered onSaveInstanceState");
//
//
//        if (mPosition!=null){
//
//            mPositions.add(mPosition);
//            Log.i(TAG, mPositions.get(0).getCost() + "");
//        }
//
//    }
//
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);
        Log.i(TAG, "entered onCreate");


        mBuyButton = (Button) findViewById(R.id.buyButton);
        mShortButton = (Button) findViewById(R.id.shortButton);

        mSharedPreferences = getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();

        Gson gson = new Gson();
        Log.i(TAG, "entered onCreate 1");
        String json = mSharedPreferences.getString(POSITIONS_ARRAY, "");
        Log.i(TAG, "entered onCreate 2");
        if (json != "") {
            mPositions = gson.fromJson(json, OpenPositionsContainer.class);
            Log.i(TAG, "entered if");
        }
        Log.i(TAG, "entered onCreate 3");


        //Log.i(TAG, mPositions.getSize()+"");
        if (getIntent()!= null && getIntent().getExtras() != null) {
            Intent intent = getIntent();
            mPosition = intent.getParcelableExtra(PopupActivity.POSITION_DETAILS);
            //Log.i(TAG, mPosition.getCompanyTicker());
            mPositions.addItem(mPosition);

        }

        Log.i(TAG, "entered onCreate 3a");
        OpenPositionAdapter adapter = new OpenPositionAdapter(this, mPositions.getOpenPositions());
        Log.i(TAG, "entered onCreate 4");
        setListAdapter(adapter);
        Log.i(TAG, "entered onCreate 5");

        mBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PortfolioActivity.this, BuyActivity.class);
                startActivity(intent);
            }
        });

    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "entered onPause");

        Gson gson = new Gson();
        String json = gson.toJson(mPositions); // myObject - instance of MyObject
        mEditor.putString(POSITIONS_ARRAY, json);
        mEditor.commit();

    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        Log.i(TAG, "entered onStop");
//
//
//
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        Log.i(TAG, "entered onDestroy");
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        Log.i(TAG, "entered onStart");
//
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        Log.i(TAG, "entered onResume");
//
//    }
}
