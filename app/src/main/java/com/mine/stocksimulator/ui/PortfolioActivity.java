package com.mine.stocksimulator.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mine.stocksimulator.R;
import com.mine.stocksimulator.adapter.PositionAdapter;
import com.mine.stocksimulator.background.UpdateReceiver;
import com.mine.stocksimulator.data.AccountSummary;
import com.mine.stocksimulator.data.Position;
import com.mine.stocksimulator.database.PositionDataSource;
import com.mine.stocksimulator.database.PositionSQLiteHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class PortfolioActivity extends AppCompatActivity implements
    NavigationView.OnNavigationItemSelectedListener{

    // EmptyTextView doesn't work here

    public static final String WHERE_IS_HOME = "WHERE_IS_HOME";

    public static final int PORTFOLIO_OPTION = 0;
    public static final int WATCHLIST_OPTION = 1;
    public static final int SETTINGS_OPTION = 2;
    public static final int INSTRUCTIONS_OPTION = 3;


    private static final String TAG = PortfolioActivity.class.getSimpleName();

    public static final String PREFS_ACCOUNT_SUMMARY_FILE = "com.mine.stocksimulator.acct_summary";
    public static final String ACCOUNT_SUMMARY = "ACCOUNT_SUMMARY";


    //private TextView mPortfolioValue;
    private TextView mProfitLossValue;
    private TextView mAvailableCash;
    private TextView mPercentReturn;
    private DrawerLayout mDrawer;
    private ListView mListView;
    private Button mTradeButton;
    private PositionAdapter mAdapter;


    private ArrayList<Position> mPositions;

    private SharedPreferences mSharedPreferencesSummary;
    private SharedPreferences.Editor mEditorSummary;


    /* This is for the account summary*/
    private AccountSummary mAccountSummary;
    //private double mCachePortfolioValue;


    private View mHeaderView;
    private View mFooterView;

    private LinearLayout mHeaderContainer;
    private TextView mEmpty;

    private double mRemainingCash;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /*TODO List:
        9. take care of onRotate
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
        navigationView.getMenu().getItem(PORTFOLIO_OPTION).setChecked(true);


        mHeaderView = getLayoutInflater().inflate(R.layout.header_portfolio, null);
        mFooterView = getLayoutInflater().inflate(R.layout.footer_watchlist, null);
        mListView = (ListView) findViewById(android.R.id.list);
        mEmpty = (TextView) findViewById(R.id.emptyMessage);

        mListView.addHeaderView(mHeaderView, null, false);
        mListView.addFooterView(mFooterView, null, false);

        //mPortfolioValue = (TextView) mHeaderView.findViewById(R.id.profitLossValue);
        mProfitLossValue = (TextView) mHeaderView.findViewById(R.id.profitLossValue);
        mAvailableCash = (TextView) mHeaderView.findViewById(R.id.availableCash);
        mPercentReturn = (TextView) mHeaderView.findViewById(R.id.percentReturn);
        mHeaderContainer = (LinearLayout) mHeaderView.findViewById(R.id.headerContainer);

        mTradeButton = (Button) findViewById(R.id.tradeButton);

        // mAccountSummary is set here
        setSummary();

        // adapter is set here and mPositions initialized
        setPositions();


        if (getIntent()!= null && getIntent().getExtras() != null) {

            Intent intent = getIntent();
            double remainingCash = intent.getDoubleExtra(TradeActivity.ACCOUNT_REMAINING_CASH, -1);
            double initBal = intent.getDoubleExtra(SettingsActivity.INITIAL_BALANCE, -1);


            // this is to get intent from settings
            if (remainingCash == -1 && initBal != -1){
                mRemainingCash = initBal;
                mAccountSummary.setAvailableCash(mRemainingCash);
                mAccountSummary.setPercentReturn(0);
                //mAccountSummary.setPortfolioValue(0);
                mAccountSummary.setProfitLossValue(0);
            }

            // this is to get intent from trade
            else if (remainingCash != -1 && initBal == -1){
                mRemainingCash = TradeActivity.round(remainingCash,2);
                mAccountSummary.setAvailableCash(mRemainingCash);
            }

            saveSummary();
            intent.removeExtra(TradeActivity.ACCOUNT_REMAINING_CASH);
            intent.removeExtra(SettingsActivity.INITIAL_BALANCE);
        }

        populateAccountTextViews();
        scheduleAlarm();


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (isNetworkAvailable()) {
                    String ticker = mPositions.get(position - 1).getCompanyTicker();
                    Intent intent = new Intent(PortfolioActivity.this, StockProfileActivity.class);
                    intent.putExtra(SearchActivity.QUERY_TICKER, ticker);
                    intent.putExtra(PortfolioActivity.WHERE_IS_HOME, true);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(PortfolioActivity.this, "No Internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });



        mTradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PortfolioActivity.this, SearchActivity.class);
                intent.putExtra(PortfolioActivity.WHERE_IS_HOME, true);
                startActivity(intent);
            }
        });


    }



    private void scheduleAlarm() {

        if (isWithinDayRange()){
            Intent intent = new Intent(getApplicationContext(), UpdateReceiver.class);
            final PendingIntent pendingIntent = PendingIntent.getBroadcast(this, UpdateReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            long firstMillis = System.currentTimeMillis();
            AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);

        }
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
            //mCachePortfolioValue = 0;
            //mAccountSummary.setPortfolioValue(mCachePortfolioValue);
            mAccountSummary.setProfitLossValue(0);
        }

        else{
            PositionDataSource dataSource = new PositionDataSource(this);
            mAccountSummary = new Gson().fromJson(jsonSummary, AccountSummary.class);

            Log.i(TAG+" availableCash", mAccountSummary.getAvailableCash()+"");
            Log.i(TAG+" profitloss", mAccountSummary.getProfitLossValue()+"");
            Log.i(TAG+" percentReturn", mAccountSummary.getPercentReturn()+"");


            mRemainingCash = mAccountSummary.getAvailableCash();

            double totalCost = dataSource.getTotal(PositionSQLiteHelper.COLUMN_TOTAL_COST);
            double totalMkt = dataSource.getTotal(PositionSQLiteHelper.COLUMN_TOTAL_MKT);
            if (totalMkt != 0 && totalCost != 0) {
                Log.i(TAG+" totalMkt" , totalMkt+"");
                Log.i(TAG+" totalCost" , totalCost+"");

                //mCachePortfolioValue = TradeActivity.round(totalMkt,2);
                //mAccountSummary.setPortfolioValue(mCachePortfolioValue);
                mAccountSummary.setProfitLossValue(TradeActivity.round(totalMkt-totalCost,2));
                double percentReturn = (totalMkt - totalCost) / totalCost * 100;
                mAccountSummary.setPercentReturn(TradeActivity.round(percentReturn,2));
                Log.i(TAG + " return", mAccountSummary.getPercentReturn()+"");

            }

        }
    }

    private void setPositions() {
        final PositionDataSource dataSource = new PositionDataSource(this);
        mPositions = dataSource.retrieve();

        if (mPositions.size() == 0){
            mEmpty.setVisibility(View.VISIBLE);
            mHeaderContainer.setVisibility(View.INVISIBLE);
            mTradeButton.setVisibility(View.VISIBLE);

        }
        else {
            mListView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }
        mAdapter = new PositionAdapter(this, mPositions);
        mListView.setAdapter(mAdapter);

        for (int i = 0 ; i < mPositions.size(); i++){
            Log.i(TAG+" mPosMkt", mPositions.get(i).getTotalMkt()+"");
        }

    }


    public void populateAccountTextViews(){

        //mPortfolioValue.setText("$ " + mCachePortfolioValue);
        mProfitLossValue.setText("$ " + mAccountSummary.getProfitLossValue());

        mAvailableCash.setText("$ " + mAccountSummary.getAvailableCash());
        mPercentReturn.setText(mAccountSummary.getPercentReturn() + " %");

        if (mAccountSummary.getProfitLossValue() >= 0){
            mProfitLossValue.setTextColor(Color.parseColor("#4dd14d"));
        }else{
            mProfitLossValue.setTextColor(Color.parseColor("#f1575a"));
        }

        if (mAccountSummary.getPercentReturn() >= 0){
            mPercentReturn.setTextColor(Color.parseColor("#4dd14d"));
        }else{
            mPercentReturn.setTextColor(Color.parseColor("#f1575a"));
        }

    }

    private void saveSummary(){
        String jsonSummary = new Gson().toJson(mAccountSummary);
        mEditorSummary.putString(ACCOUNT_SUMMARY, jsonSummary);
        mEditorSummary.apply();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (getIntent()!= null && getIntent().getExtras() != null) {

            getIntent().removeExtra(TradeActivity.ACCOUNT_REMAINING_CASH);
            getIntent().removeExtra(SettingsActivity.INITIAL_BALANCE);
        }

        saveSummary();

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
            intent.putExtra(WHERE_IS_HOME, true);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.nav_portfolio){

        }
        else if (id == R.id.nav_watchlist){
            Intent intent = new Intent(this, WatchlistActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_settings){
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_instructions){
            Intent intent = new Intent(this, InstructionsActivity.class);
            startActivity(intent);
        }


        mDrawer.closeDrawer(GravityCompat.START);

        return false;
    }

    public static boolean isWithinDayRange(){
        TimeZone newYorkTimeZone = TimeZone.getTimeZone("America/New_York");
        Date date = Calendar.getInstance(newYorkTimeZone).getTime();
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE");
        String today = formatter.format(date);

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        if (today.equals("Monday") || today.equals("Tuesday") || today.equals("Wednesday")
                || today.equals("Thursday") || today.equals("Friday")) {
            if (hour >= 9 && hour < 16) {
                return true;
            } else {
                return false;
            }
        }
        else{
            return false;
        }
    }

    private boolean isNetworkAvailable() {

        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }

        return isAvailable;
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
