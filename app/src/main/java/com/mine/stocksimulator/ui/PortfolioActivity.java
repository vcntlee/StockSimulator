package com.mine.stocksimulator.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

    public static final int PORTFOLIO_OPTION = 0;
    public static final int WATCHLIST_OPTION = 1;
    public static final int SETTINGS_OPTION = 2;

    private static final String TAG = PortfolioActivity.class.getSimpleName();

    public static final String PREFS_ACCOUNT_SUMMARY_FILE = "com.mine.stocksimulator.acct_summary";
    public static final String ACCOUNT_SUMMARY = "ACCOUNT_SUMMARY";


    private TextView mPortfolioValue;
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
    private double mCachePortfolioValue;

    private String[] mOptionsMenu;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToogle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private View mHeaderView;
    private View mFooterView;

    private LinearLayout mHeaderContainer;
    private TextView mEmpty;

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
        navigationView.getMenu().getItem(PORTFOLIO_OPTION).setChecked(true);


        mHeaderView = getLayoutInflater().inflate(R.layout.header_portfolio, null);
        mFooterView = getLayoutInflater().inflate(R.layout.footer_watchlist, null);
        mListView = (ListView) findViewById(android.R.id.list);
        mEmpty = (TextView) findViewById(R.id.emptyMessage);

        mListView.addHeaderView(mHeaderView, null, false);
        mListView.addFooterView(mFooterView, null, false);

        mPortfolioValue = (TextView) mHeaderView.findViewById(R.id.portfolioValue);
        mAvailableCash = (TextView) mHeaderView.findViewById(R.id.availableCash);
        mPercentReturn = (TextView) mHeaderView.findViewById(R.id.percentReturn);
        mHeaderContainer = (LinearLayout) mHeaderView.findViewById(R.id.headerContainer);

        mTradeButton = (Button) findViewById(R.id.tradeButton);

        // mAccountSummary is set here
        setSummary();

        // adapter is set here and mPositions initialized
        setPositions();


        if (getIntent()!= null && getIntent().getExtras() != null) {

            Log.i(TAG, " entering here");
            Log.i(TAG + " extraTrade", getIntent().getDoubleExtra(TradeActivity.ACCOUNT_REMAINING_CASH, -1)+"");
            Log.i(TAG + " extraSetting", getIntent().getDoubleExtra(SettingsActivity.INITIAL_BALANCE, -1)+"");

            Intent intent = getIntent();
            double remainingCash = intent.getDoubleExtra(TradeActivity.ACCOUNT_REMAINING_CASH, -1);
            double initBal = intent.getDoubleExtra(SettingsActivity.INITIAL_BALANCE, -1);


            // this is to get intent from settings
            if (remainingCash == -1 && initBal != -1){
                mRemainingCash = initBal;
                mAccountSummary.setAvailableCash(mRemainingCash);
                mAccountSummary.setPercentReturn(0);
                mAccountSummary.setPortfolioValue(0);
            }

            // this is to get intent from trade
            else if (remainingCash != -1 && initBal == -1){
                mRemainingCash = TradeActivity.round(remainingCash,2);
                mAccountSummary.setAvailableCash(mRemainingCash);
            }

            Log.i(TAG+" cash", mAccountSummary.getAvailableCash() +"");

            saveSummary();
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

                if (isNetworkAvailable()) {
                    String ticker = mPositions.get(position - 1).getCompanyTicker();
                    Intent intent = new Intent(PortfolioActivity.this, StockProfileActivity.class);
                    intent.putExtra(SearchActivity.QUERY_TICKER, ticker);
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
            mCachePortfolioValue = 0;
            mAccountSummary.setPortfolioValue(mCachePortfolioValue);
        }

        else{
            PositionDataSource dataSource = new PositionDataSource(this);
            mAccountSummary = new Gson().fromJson(jsonSummary, AccountSummary.class);

            Log.i(TAG+" summary", mAccountSummary.getAvailableCash()+"");
            Log.i(TAG+" summary", mAccountSummary.getPortfolioValue()+"");
            Log.i(TAG+" summary", mAccountSummary.getPercentReturn()+"");
            Log.i(TAG+" summary", mAccountSummary.getAvailableCash()+"");


            mRemainingCash = mAccountSummary.getAvailableCash();

            double totalCost = dataSource.getTotal(PositionSQLiteHelper.COLUMN_TOTAL_COST);
            double totalMkt = dataSource.getTotal(PositionSQLiteHelper.COLUMN_TOTAL_MKT);
            if (totalMkt != 0 && totalCost != 0) {
                Log.i(TAG+" totalMkt" , totalMkt+"");
                Log.i(TAG+" totalCost" , totalCost+"");


                mCachePortfolioValue = totalMkt;
                mAccountSummary.setPortfolioValue(mCachePortfolioValue);
                mAccountSummary.setPercentReturn(calculateReturn(totalCost, totalMkt));
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

        mAdapter = new PositionAdapter(this, mPositions);
        mListView.setAdapter(mAdapter);
    }

    private double calculateReturn(double a, double b){
        return TradeActivity.round((b - a)/ a, 5);
    }

//    private void updatePositions(){
//        PositionDataSource dataSource = new PositionDataSource(this);
//        for (int i = 0; i < mPositions.size(); i++) {
//            double percentReturn = calculateReturn(mPositions.get(i).getCost(), mPositions.get(i).getPrice());
//            double totalMkt = mPositions.get(i).getPrice() * mPositions.get(i).getShares();
//            dataSource.update(mPositions.get(i), mPositions.get(i).getPrice(), -1, -1, percentReturn, totalMkt, -1);
//        }
//        mAdapter.notifyDataSetChanged();
//    }
//
//    private void updateAccountSummary(){
//        if (mPositions.size() > 0) {
//            PositionDataSource datasource = new PositionDataSource(this);
//            mCachePortfolioValue = TradeActivity.round(datasource.getTotal(PositionSQLiteHelper.COLUMN_TOTAL_MKT),2);
//            double totalCost = datasource.getTotal(PositionSQLiteHelper.COLUMN_TOTAL_COST);
//            double percentReturn = calculateReturn(totalCost, mCachePortfolioValue);
//            mAccountSummary.setPercentReturn(percentReturn);
//        }
//
//    }


    public void populateAccountTextViews(){

        mPortfolioValue.setText("$ " + mCachePortfolioValue);

        mAvailableCash.setText("$ " + mAccountSummary.getAvailableCash());
        mPercentReturn.setText(mAccountSummary.getPercentReturn() + " %");
    }

    private void saveSummary(){
        String jsonSummary = new Gson().toJson(mAccountSummary);
        Log.i(TAG + " jsonSummary", jsonSummary);
        mEditorSummary.putString(ACCOUNT_SUMMARY, jsonSummary);
        mEditorSummary.apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "entered onPause");

        if (getIntent()!= null && getIntent().getExtras() != null) {
            Log.i(TAG, "entered onPause's remove extras");
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
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.nav_portfolio){
            Log.i(TAG, "portfolio icon pressed");

        }
        else if (id == R.id.nav_watchlist){
            Log.i(TAG, "watchlist icon pressed");
            Intent intent = new Intent(this, WatchlistActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_settings){
            Log.i(TAG, "settings icon pressed");
            Intent intent = new Intent(this, SettingsActivity.class);
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
            if (hour >= 9 && hour <= 16) {
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
