package com.mine.stocksimulator.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mine.stocksimulator.R;
import com.mine.stocksimulator.adapter.PositionAdapter;
import com.mine.stocksimulator.background.UpdateAlarm;
import com.mine.stocksimulator.data.AccountSummary;
import com.mine.stocksimulator.data.Position;
import com.mine.stocksimulator.database.PositionDataSource;
import com.mine.stocksimulator.database.PositionSQLiteHelper;

import java.util.ArrayList;

public class PortfolioActivity extends AppCompatActivity implements
    NavigationView.OnNavigationItemSelectedListener{


    private static final String TAG = PortfolioActivity.class.getSimpleName();

    public static final String PREFS_ACCOUNT_SUMMARY_FILE = "com.mine.stocksimulator.acct_summary";
    public static final String ACCOUNT_SUMMARY = "ACCOUNT_SUMMARY";


    private TextView mPortfolioValue;
    private TextView mAvailableCash;
    private TextView mPercentReturn;
    private DrawerLayout mDrawer;
    private ListView mListView;
    //private TextView mEmptyTextView;
    private Button mTradeButton;
    private PositionAdapter mAdapter;


    private ArrayList<Position> mPositions;

    private SharedPreferences mSharedPreferencesSummary;
    private SharedPreferences.Editor mEditorSummary;


    /* This is for the account summary*/
    private AccountSummary mAccountSummary;
    private double mCachePortfolioValue;

    private String[] mOptionsMenu;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToogle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private View mHeaderView;

    private double mRemainingCash;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /*TODO List:
        1. make sure that repeated buys don't get duplicated in listView --> DONE
        2. make a nav bar (search goes to the buy activity page) --> DONE
        3. calculate the profit --> DONE
        4. error check input --> DONE
        5. create an adapter with search results --> DONE
        6. portfolio summary on different activity --> NO NEED
        7. watchlist
        8. buy and short | sell to cover and buy to cover --> DONE
        9. take care of onRotate
        10. init beginning balance
     */

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);

        // for the actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Portfolio");
        }

        // for the drawer
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.left_drawer);
        navigationView.setNavigationItemSelectedListener(this);


        mHeaderView = getLayoutInflater().inflate(R.layout.header_portfolio, null);
        mListView = (ListView) findViewById(android.R.id.list);

        mListView.addHeaderView(mHeaderView, null, false);




        mPortfolioValue = (TextView) mHeaderView.findViewById(R.id.portfolioValue);
        mAvailableCash = (TextView) mHeaderView.findViewById(R.id.availableCash);
        mPercentReturn = (TextView) mHeaderView.findViewById(R.id.percentReturn);

        //mEmptyTextView = (TextView) findViewById(android.R.id.empty);
        mTradeButton = (Button) findViewById(R.id.tradeButton);

        // mAccountSummary is set here
        setSummary();

        // adapter is set here and mPositions initialized
        setPositions();


        if (getIntent()!= null && getIntent().getExtras() != null) {
            Intent intent = getIntent();
            mRemainingCash = intent.getDoubleExtra(TradeActivity.ACCOUNT_REMAINING_CASH, -1);

            if (mRemainingCash == -1){
                mRemainingCash = intent.getDoubleExtra(SettingsActivity.INITIAL_BALANCE, 0);
            }

            Log.i(TAG + " remainingCash", mRemainingCash + "");

            mAccountSummary.setAvailableCash(TradeActivity.round(mRemainingCash, 2));
            intent.removeExtra(TradeActivity.ACCOUNT_REMAINING_CASH);
            intent.removeExtra(SettingsActivity.INITIAL_BALANCE);
        }



        // here we update the adapter
                //wrapperForRefreshPositions();
                //updatePositions();
        // update account summary
        //updateAccountSummary();
        // set the account details views
        populateAccountTextViews();

        scheduleAlarm();


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String ticker = mPositions.get(position-1).getCompanyTicker();
                Intent intent = new Intent(PortfolioActivity.this, StockProfileActivity.class);
                intent.putExtra(SearchActivity.QUERY_TICKER, ticker);
                startActivity(intent);
            }
        });



        mTradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PortfolioActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });


    }

    private void scheduleAlarm() {
        Intent intent = new Intent(getApplicationContext(), UpdateAlarm.class);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(this, UpdateAlarm.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long firstMillis = System.currentTimeMillis();
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
    }

    private void setSummary() {

        mSharedPreferencesSummary = getSharedPreferences(PREFS_ACCOUNT_SUMMARY_FILE, Context.MODE_PRIVATE);
        mEditorSummary = mSharedPreferencesSummary.edit();
        String jsonSummary = mSharedPreferencesSummary.getString(ACCOUNT_SUMMARY, "");

        if (jsonSummary.equals("")){
            mRemainingCash = 1000000;
            mAccountSummary = new AccountSummary();
            mAccountSummary.setAvailableCash(mRemainingCash);
            mAccountSummary.setPercentReturn(0);
            mCachePortfolioValue = 0;
            mAccountSummary.setPortfolioValue(mCachePortfolioValue);
        }

        else{
            PositionDataSource dataSource = new PositionDataSource(this);
            mAccountSummary = new Gson().fromJson(jsonSummary, AccountSummary.class);

            mRemainingCash = mAccountSummary.getAvailableCash();
            double totalCost = dataSource.getTotal(PositionSQLiteHelper.COLUMN_TOTAL_COST);
            double totalMkt = dataSource.getTotal(PositionSQLiteHelper.COLUMN_TOTAL_MKT);
            if (totalMkt != 0 && totalCost != 0) {
                Log.i(TAG+" totalMkt" , totalMkt+"");
                Log.i(TAG+" totalCost" , totalCost+"");
                mCachePortfolioValue = TradeActivity.round(totalMkt,2);
                mAccountSummary.setPortfolioValue(mCachePortfolioValue);
                mAccountSummary.setPercentReturn(calculateReturn(totalCost, totalMkt));
            }



        }
    }

    private void setPositions() {

        final PositionDataSource dataSource = new PositionDataSource(this);

        mPositions = dataSource.retrieve();

        mAdapter = new PositionAdapter(this, mPositions);

        mListView.setAdapter(mAdapter);

    }

    private double calculateReturn(double a, double b){
        return TradeActivity.round((b - a)/ a, 2);
    }

    private void updatePositions(){
        PositionDataSource dataSource = new PositionDataSource(this);
        for (int i = 0; i < mPositions.size(); i++) {
            double percentReturn = calculateReturn(mPositions.get(i).getCost(), mPositions.get(i).getPrice());
            double totalMkt = mPositions.get(i).getPrice() * mPositions.get(i).getShares();
            dataSource.update(mPositions.get(i), mPositions.get(i).getPrice(), -1, -1, percentReturn, totalMkt, -1);
        }
        mAdapter.notifyDataSetChanged();
    }

    private void updateAccountSummary(){
        if (mPositions.size() > 0) {
            PositionDataSource datasource = new PositionDataSource(this);
            mCachePortfolioValue = TradeActivity.round(datasource.getTotal(PositionSQLiteHelper.COLUMN_TOTAL_MKT),2);
            double totalCost = datasource.getTotal(PositionSQLiteHelper.COLUMN_TOTAL_COST);
            double percentReturn = calculateReturn(totalCost, mCachePortfolioValue);
            mAccountSummary.setPercentReturn(percentReturn);
        }

    }


    public void populateAccountTextViews(){
        Log.i(TAG + " totalCached3", mCachePortfolioValue + "");

        mPortfolioValue.setText("$ " + mCachePortfolioValue);

        mAvailableCash.setText("$ " + mAccountSummary.getAvailableCash());
        mPercentReturn.setText(mAccountSummary.getPercentReturn()+" %");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "entered onPause");

        if (getIntent()!= null && getIntent().getExtras() != null) {
            getIntent().removeExtra(TradeActivity.ACCOUNT_REMAINING_CASH);
        }

        String jsonSummary = new Gson().toJson(mAccountSummary);
        Log.i(TAG + " jsonSummary", jsonSummary);
        mEditorSummary.putString(ACCOUNT_SUMMARY, jsonSummary);
        mEditorSummary.apply();

    }


    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)){
            mDrawer.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_portfolio, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.searchOption){
            Intent intent = new Intent(PortfolioActivity.this, SearchActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.nav_portfolio){

            //TODO
            Log.i(TAG, "portfolio icon pressed");

        }
        else if (id == R.id.nav_watchlist){
            //TODO
            Log.i(TAG, "watchlist icon pressed");
            Intent intent = new Intent(this, WatchlistActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_settings){
            //TODO
            Log.i(TAG, "settings icon pressed");
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }


        mDrawer.closeDrawer(GravityCompat.START);

        return false;
    }


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
