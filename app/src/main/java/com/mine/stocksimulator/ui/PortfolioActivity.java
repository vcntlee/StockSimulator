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
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mine.stocksimulator.R;
import com.mine.stocksimulator.adapter.OpenPositionAdapter;
import com.mine.stocksimulator.data.AccountSummary;
import com.mine.stocksimulator.data.OpenPosition;
import com.mine.stocksimulator.data.OpenPositionsList;
import com.mine.stocksimulator.data.OpenPositionsMap;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PortfolioActivity extends AppCompatActivity implements
    NavigationView.OnNavigationItemSelectedListener{


    private static final String TAG = PortfolioActivity.class.getSimpleName();
    public static final String PREFS_POSITIONS_FILE = "com.mine.stocksimulator.positions";
    public static final String POSITIONS_ARRAY = "POSITIONS_ARRAY";
    public static final String PREFS_ACCOUNT_SUMMARY_FILE = "com.mine.stocksimulator.acct_summary";
    public static final String ACCOUNT_SUMMARY = "ACCOUNT_SUMMARY";
    public static final String PREFS_POSITIONS_MAP_FILE = "com.mine.stocksimulator.positions_map";
    public static final String POSITIONS_MAP = "POSITIONS_MAP";

    private TextView mPortfolioValue;
    private TextView mAvailableCash;
    private TextView mPercentReturn;
    private DrawerLayout mDrawer;
    private ListView mListView;
    private TextView mEmptyTextView;
    private Button mBuyButton;
    private Button mShortButton;
    private OpenPosition mPosition = new OpenPosition();
    private OpenPositionsList mPositions = new OpenPositionsList();
    private OpenPositionsMap mOpenPositionsMap = new OpenPositionsMap();

    private SharedPreferences mSharedPreferencesPositions;
    private SharedPreferences.Editor mEditorPositions;
    private SharedPreferences mSharedPreferencesSummary;
    private SharedPreferences.Editor mEditorSummary;
    private SharedPreferences mSharedPreferencesPositionsMap;
    private SharedPreferences.Editor mEditorPositionsMap;

    /* This is for the account summary*/
    private AccountSummary mAccountSummary;

    private String[] mOptionsMenu;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToogle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        /*TODO List:
        1. make sure that repeated buys don't get duplicated in listView
        2. make a nav bar (search goes to the buy activity page) --> DONE
        3. calculate the profit --> DONE
        4. error check input
        5. create an adapter with search results?
        6. portfolio summary on different activity
        7. watchlist
        8. buy and short | sell to cover and buy to cover
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
        mBuyButton = (Button) findViewById(R.id.tradeButton);
        mShortButton = (Button) findViewById(R.id.shortButton);

        /* Shared prefs for positions and summary*/
        setPositionsAndSummary();
        callRefreshPositions();

        if (getIntent()!= null && getIntent().getExtras() != null) {
            Intent intent = getIntent();
            mPositions = intent.getParcelableExtra(TradeActivity.POSITIONS_ARRAY);
            Log.i(TAG +" positions size", mPositions.getSize()+"");
            // mPositions.addItem(mPosition);
            mAccountSummary = intent.getParcelableExtra(TradeActivity.ACCOUNT_DETAILS);
        }

        // set the account details views
        populateAccountTextViews();

        // set adapter
        OpenPositionAdapter adapter = new OpenPositionAdapter(this, mPositions.getOpenPositions());
        mListView.setAdapter(adapter);
        mListView.setEmptyView(mEmptyTextView);

        // setting AccountSummary portfolio value and return;
        double portfolioValue = calculatePortfolioValue();
        if (portfolioValue == 0){
            portfolioValue = 1000000;
        }
        double percentReturn = calculatePercentReturn(portfolioValue);

        mAccountSummary.setPortfolioValue(portfolioValue);
        mAccountSummary.setPercentReturn(percentReturn);

        mBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PortfolioActivity.this, BuyActivity.class);
                startActivity(intent);
            }
        });

    }

    private void callRefreshPositions() {
        if (mPositions.getSize() > 0) {
            int size = mPositions.getSize();
            for (int i = 0; i < size; i ++) {
                refreshPositions(mPositions.getOpenPositions().get(i).getCompanyTicker(),
                        mPositions.getOpenPositions().get(i));
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.i(TAG + " price ", mPositions.getOpenPositions().get(i).getCost()+"");
            }
        }
    }


    private double calculatePortfolioValue() {
        if (mPositions.getSize() > 0){
            double totalValue = 0;
            for (int i = 0; i < mPositions.getSize(); i++){
                OpenPosition member = mPositions.getOpenPositions().get(i);
                totalValue += member.getPrice() * member.getShares();
            }
            return totalValue;
        }
        return 0;
    }

    private double calculatePercentReturn(double portfolioValue){
        if (mPositions.getSize() > 0){
            double totalCost = 0;
            for (int i = 0; i < mPositions.getSize(); i++){
                OpenPosition member = mPositions.getOpenPositions().get(i);
                totalCost += member.getCost() * member.getShares();
            }
            return (portfolioValue - totalCost) / totalCost;
        }
        return 0;
    }

    private void setPositionsAndSummary() {
        mSharedPreferencesPositions = getSharedPreferences(PREFS_POSITIONS_FILE, Context.MODE_PRIVATE);
        mEditorPositions = mSharedPreferencesPositions.edit();
        String jsonPosition = mSharedPreferencesPositions.getString(POSITIONS_ARRAY, "");

        if (!jsonPosition.equals("")) {
            Log.i(TAG ,"there is a positionsArray");
            mPositions = new Gson().fromJson(jsonPosition, OpenPositionsList.class);
        }

        mSharedPreferencesSummary = getSharedPreferences(PREFS_ACCOUNT_SUMMARY_FILE, Context.MODE_PRIVATE);
        mEditorSummary = mSharedPreferencesSummary.edit();
        String jsonSummary = mSharedPreferencesSummary.getString(ACCOUNT_SUMMARY, "");

        if (jsonSummary.equals("")){
            mAccountSummary = new AccountSummary();
            mAccountSummary.setAvailableCash(1000000);
            mAccountSummary.setPercentReturn(0);
            mAccountSummary.setPortfolioValue(1000000);
        }

        else{
            mAccountSummary = new Gson().fromJson(jsonSummary, AccountSummary.class);
        }
    }

    public void populateAccountTextViews(){
        mPortfolioValue.setText("$ " + mAccountSummary.getPortfolioValue());
        mAvailableCash.setText("$ " + mAccountSummary.getAvailableCash());
        mPercentReturn.setText(mAccountSummary.getPercentReturn()+" %");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "entered onPause");

        if (getIntent()!= null && getIntent().getExtras() != null) {
            getIntent().removeExtra(TradeActivity.POSITIONS_ARRAY);
            getIntent().removeExtra(TradeActivity.ACCOUNT_DETAILS);
        }

        String jsonPositions = new Gson().toJson(mPositions); // myObject - instance of MyObject
        Log.i(TAG+" jsonPositions", jsonPositions);
        mEditorPositions.putString(POSITIONS_ARRAY, jsonPositions);
        mEditorPositions.apply();

        String jsonSummary = new Gson().toJson(mAccountSummary);
        Log.i(TAG + " jsonSummary", jsonSummary);
        mEditorSummary.putString(ACCOUNT_SUMMARY, jsonSummary);
        mEditorSummary.apply();

        Log.i(TAG, mPositions.getSize() + "");

        mSharedPreferencesPositionsMap = getSharedPreferences(PREFS_POSITIONS_MAP_FILE, Context.MODE_PRIVATE);
        mEditorPositionsMap = mSharedPreferencesPositionsMap.edit();
        for (int i = 0; i < mPositions.getSize(); i++) {
            OpenPosition position = mPositions.getOpenPositions().get(i);
            Log.i(TAG+" mPositions", position.getCompanyTicker());
            mOpenPositionsMap.setKey(position.getCompanyTicker(), position.getType());
        }

        for (Map.Entry<String, String> entry : mOpenPositionsMap.getOpenPositionsMap().entrySet()) {
            Log.i(TAG+" Map Key", entry.getKey());
            Log.i(TAG+" Map Value", entry.getValue());
        }

        String jsonPositionsMap = new Gson().toJson(mOpenPositionsMap);
        Log.i(TAG+" jsonMap", jsonPositionsMap);
        mEditorPositionsMap.putString(POSITIONS_MAP, jsonPositionsMap);
        mEditorPositionsMap.apply();





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

    private void refreshPositions(String companyName, final OpenPosition position) {
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



    private void updatePosition(String jsonData, OpenPosition position) throws JSONException {
        JSONObject wholeQuote = new JSONObject(jsonData);

        //FIXME this is a problem, is this actually updating the original member of mPositions?
        position.setPrice(wholeQuote.getDouble("LastPrice"));

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
