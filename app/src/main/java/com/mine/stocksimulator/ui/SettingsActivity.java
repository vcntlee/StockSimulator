package com.mine.stocksimulator.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mine.stocksimulator.R;
import com.mine.stocksimulator.database.PositionSQLiteHelper;

public class SettingsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    public final static String INITIAL_BALANCE = "INITIAL_BALANCE";
    private static final String TAG = SettingsActivity.class.getSimpleName();
    private DrawerLayout mDrawer;
    private Button mResetButton;
    //private Button mCancelButton;
    private SeekBar mSetBalanceSeekBar;
    private TextView mStartingBalanceTextView;
    private double mStartingBalance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mResetButton = (Button) findViewById(R.id.resetButton);
        //mCancelButton = (Button) findViewById(R.id.cancelButton);
        mSetBalanceSeekBar = (SeekBar) findViewById(R.id.setBalanceSeekBar);
        mStartingBalanceTextView = (TextView) findViewById(R.id.startingBalance);

        if (toolbar!=null){
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Settings");
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.left_drawer);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(PortfolioActivity.SETTINGS_OPTION).setChecked(true);


        mStartingBalance = Double.parseDouble(mStartingBalanceTextView.getText().toString());

        mSetBalanceSeekBar.setProgress(25000);
        mSetBalanceSeekBar.incrementProgressBy(5000);
        mSetBalanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = progress / 5000;
                progress = progress * 5000;
                mStartingBalanceTextView.setText("$ " + (progress + 25000));
                mStartingBalance = progress + 25000;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        mResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i(TAG, mStartingBalance+"");
                SettingsActivity.this.deleteDatabase(PositionSQLiteHelper.DB_NAME);
                SettingsActivity.this.deleteFile(PortfolioActivity.PREFS_ACCOUNT_SUMMARY_FILE);

                Intent intent = new Intent(SettingsActivity.this, PortfolioActivity.class);
                intent.putExtra(INITIAL_BALANCE, mStartingBalance);
                startActivity(intent);

            }
        });

//        mCancelButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(SettingsActivity.this, PortfolioActivity.class);
//                startActivity(intent);
//            }
//        });

    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_portfolio){
            Intent intent = new Intent(this, PortfolioActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_watchlist){
            Intent intent = new Intent(this, WatchlistActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_settings){

        }

        mDrawer.closeDrawer(GravityCompat.START);
        return false;
    }
}
