package com.mine.stocksimulator.ui;

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
import android.widget.Toast;

import com.google.gson.Gson;
import com.mine.stocksimulator.R;
import com.mine.stocksimulator.adapter.PositionAdapter;
import com.mine.stocksimulator.data.AccountSummary;
import com.mine.stocksimulator.data.Position;
import com.mine.stocksimulator.database.PositionDataSource;
import com.mine.stocksimulator.database.PositionSQLiteHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
    private TextView mEmptyTextView;
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

        mPortfolioValue = (TextView) findViewById(R.id.portfolioValue);
        mAvailableCash = (TextView) findViewById(R.id.availableCash);
        mPercentReturn = (TextView) findViewById(R.id.percentReturn);
        mListView = (ListView) findViewById(android.R.id.list);
        mEmptyTextView = (TextView) findViewById(android.R.id.empty);
        mTradeButton = (Button) findViewById(R.id.tradeButton);

        // mAccountSummary is set here
        setSummary();

        // adapter is set here and mPositions initialized
        setPositions();

        if (getIntent()!= null && getIntent().getExtras() != null) {
            Intent intent = getIntent();
            double remainingCash = intent.getDoubleExtra(TradeActivity.ACCOUNT_REMAINING_CASH, -1);

            if (remainingCash == -1){
                Log.i(TAG+" remainingBefore", remainingCash+"");
                remainingCash = intent.getDoubleExtra(SettingsActivity.INITIAL_BALANCE, 0);
                Log.i(TAG+" remainingAfter", remainingCash+"");
            }

            Log.i(TAG+" remainingCash", remainingCash+"");

            mAccountSummary.setAvailableCash(remainingCash);
            intent.removeExtra(TradeActivity.ACCOUNT_REMAINING_CASH);
            intent.removeExtra(SettingsActivity.INITIAL_BALANCE);
        }

        // here we update the adapter
        wrapperForRefreshPositions();
        updatePositions();

        // update account summary
        updateAccountSummary();

        // set the account details views
        populateAccountTextViews();


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String ticker = mPositions.get(position).getCompanyTicker();
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

    private void setSummary() {

        mSharedPreferencesSummary = getSharedPreferences(PREFS_ACCOUNT_SUMMARY_FILE, Context.MODE_PRIVATE);
        mEditorSummary = mSharedPreferencesSummary.edit();
        String jsonSummary = mSharedPreferencesSummary.getString(ACCOUNT_SUMMARY, "");

        if (jsonSummary.equals("")){
            mAccountSummary = new AccountSummary();
            mAccountSummary.setAvailableCash(1000000);
            mAccountSummary.setPercentReturn(0);
            mCachePortfolioValue = 0;
            mAccountSummary.setPortfolioValue(mCachePortfolioValue);
        }

        else{
            PositionDataSource dataSource = new PositionDataSource(this);
            mAccountSummary = new Gson().fromJson(jsonSummary, AccountSummary.class);
            double totalCost = dataSource.getTotal(PositionSQLiteHelper.COLUMN_TOTAL_COST);
            double totalMkt = dataSource.getTotal(PositionSQLiteHelper.COLUMN_TOTAL_MKT);
            if (totalMkt != 0 && totalCost != 0) {
                mAccountSummary.setPortfolioValue(totalMkt);
                mAccountSummary.setPercentReturn(calculateReturn(totalCost, totalMkt));
            }



        }
    }

    private void setPositions() {
        PositionDataSource dataSource = new PositionDataSource(this);
        mPositions = dataSource.retrieve();
        mAdapter = new PositionAdapter(this, mPositions);
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(mEmptyTextView);
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
            mCachePortfolioValue = datasource.getTotal(PositionSQLiteHelper.COLUMN_TOTAL_MKT);
            double totalCost = datasource.getTotal(PositionSQLiteHelper.COLUMN_TOTAL_COST);
            double percentReturn = calculateReturn(totalCost, mCachePortfolioValue);
            mAccountSummary.setPercentReturn(percentReturn);
        }

    }

    private void wrapperForRefreshPositions() {
        if (mPositions.size() > 0) {
            for (int i = 0; i < mPositions.size(); i ++) {
                refreshPositions(mPositions.get(i).getCompanyTicker(),
                        mPositions.get(i));
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }




    public void populateAccountTextViews(){
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

    private void refreshPositions(String companyName, final Position position) {
        String completeUrl;
        String baseUrl = "http://dev.markitondemand.com/MODApis/Api/v2/Quote/json?symbol=%1$s";
        completeUrl = String.format(baseUrl, companyName);

        Log.i(TAG, completeUrl);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request
                .Builder()
                .url(completeUrl)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonData = response.body().string();
                    Log.v(TAG, jsonData);

                    //TODO check network availability

                    if (response.isSuccessful()) {
                        updatePosition(jsonData, position);

                    } else {
                        alertUserAboutError();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Exception caught: ", e);
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException caught: ", e);
                    Toast.makeText(PortfolioActivity.this, "oops!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }



    private void updatePosition(String jsonData, Position position) throws JSONException {
        JSONObject wholeQuote = new JSONObject(jsonData);

        double newPrice = wholeQuote.getDouble("LastPrice");
        double oldPrice = position.getCost();

        Log.i(TAG + " newPrice", newPrice+"");
        Log.i(TAG + " oldPrice", oldPrice+"");

        double percentReturn = TradeActivity.round(((newPrice - oldPrice) / oldPrice),3);
        Log.i(TAG + " percent return", percentReturn+"");
        position.setPrice(wholeQuote.getDouble("LastPrice"));
        position.setPercentReturn(percentReturn);

    }

    private void alertUserAboutError() {
        //Toast.makeText(this, "response is not successful", Toast.LENGTH_LONG).show();
        Log.i(TAG, "response is not successful");

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
