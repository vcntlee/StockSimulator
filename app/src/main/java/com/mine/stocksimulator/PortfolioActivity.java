package com.mine.stocksimulator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class PortfolioActivity extends AppCompatActivity {

    public static final String TAG = PortfolioActivity.class.getSimpleName();
    private Button mBuyButton;
    private Button mShortButton;
    private OpenPosition mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);

        if (savedInstanceState != null){

            Log.i(TAG, "savedInstance state not null");

            Intent intent = getIntent();

            mPosition = intent.getParcelableExtra(PopupActivity.POSITION_DETAILS);

            Log.i(TAG, mPosition.getCost() + "");
            Log.i(TAG, mPosition.getCompanyTicker());
            Log.i(TAG, mPosition.getPrice()+"");
            Log.i(TAG, mPosition.getShares()+"");

        }

        else {
            Log.i(TAG, "entered onCreate");

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
    }


}
