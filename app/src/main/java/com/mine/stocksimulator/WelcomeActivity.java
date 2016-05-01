package com.mine.stocksimulator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class WelcomeActivity extends AppCompatActivity {

    private Button mMyPortfolioButton;
    private Button mWatchlistButton;
    private Button mSearchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvity_welcome);

        mMyPortfolioButton = (Button) findViewById(R.id.myPortfolioButton);
        mWatchlistButton = (Button) findViewById(R.id.watchlistButton);
        mSearchButton = (Button) findViewById(R.id.searchButton);

        mMyPortfolioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, PortfolioActivity.class);
                startActivity(intent);
            }
        });


    }
}
