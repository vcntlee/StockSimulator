package com.mine.stocksimulator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class PortfolioActivity extends AppCompatActivity {

    private Button mBuyButton;
    private Button mShortButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);

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
