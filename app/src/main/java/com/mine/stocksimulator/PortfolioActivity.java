package com.mine.stocksimulator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class PortfolioActivity extends AppCompatActivity {

    public static final String TAG = PortfolioActivity.class.getSimpleName();
    private static final String POSITION_DETAILS_ARRAYLIST = "POSITION_DETAILS_ARRAYLIST";
    private Button mBuyButton;
    private Button mShortButton;
    private OpenPosition mPosition;
    private ArrayList<OpenPosition> mPositions = new ArrayList<>();

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "entered onSaveInstanceState");


        if (mPosition!=null){

            mPositions.add(mPosition);
            Log.i(TAG, mPositions.get(0).getCost() + "");
        }

        outState.putInt(POSITION_DETAILS_ARRAYLIST, 90);

    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        int somenumber = savedInstanceState.getInt(POSITION_DETAILS_ARRAYLIST);
        mBuyButton.setText(somenumber + "");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);

        Log.i(TAG, "entered onCreate");

        if (getIntent()!= null && getIntent().getExtras() != null) {
            Intent intent = getIntent();
            mPosition = intent.getParcelableExtra(PopupActivity.POSITION_DETAILS);
            Log.i(TAG, mPosition.getCompanyTicker());
        }


        mBuyButton = (Button) findViewById(R.id.buyButton);
        mShortButton = (Button) findViewById(R.id.shortButton);

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
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "entered onStop");



    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "entered onDestroy");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "entered onStart");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "entered onResume");

    }
}
