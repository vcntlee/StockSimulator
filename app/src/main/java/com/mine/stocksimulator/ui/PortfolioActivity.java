package com.mine.stocksimulator.ui;
import android.support.design.widget.NavigationView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.mine.stocksimulator.data.OpenPosition;
import com.mine.stocksimulator.data.OpenPositionsContainer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PortfolioActivity extends AppCompatActivity implements
    NavigationView.OnNavigationItemSelectedListener{

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
     */


    public static final String TAG = PortfolioActivity.class.getSimpleName();
    private static final String PREFS_FILE = "com.mine.stocksimulator.prefs_file" ;
    private static final String POSITIONS_ARRAY = "POSITIONS_ARRAY";

    private DrawerLayout mDrawer;
    private ListView mListView;
    private TextView mEmptyTextView;
    private Button mBuyButton;
    private Button mShortButton;
    private OpenPosition mPosition = new OpenPosition();
    private OpenPositionsContainer mPositions = new OpenPositionsContainer();
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;


    private String[] mOptionsMenu;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToogle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Portfolio");
        }

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.left_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        Log.i(TAG, "entered onCreate");

        mListView = (ListView) findViewById(android.R.id.list);
        mEmptyTextView = (TextView) findViewById(android.R.id.empty);

        mBuyButton = (Button) findViewById(R.id.buyButton);
        mShortButton = (Button) findViewById(R.id.shortButton);

        mSharedPreferences = getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();

        Gson gson = new Gson();
        String json = mSharedPreferences.getString(POSITIONS_ARRAY, "");
        if (json != "") {
            mPositions = gson.fromJson(json, OpenPositionsContainer.class);
        }
        if (mPositions.getSize() > 0) {
            int size = mPositions.getSize();
            for (int i = 0; i < size; i ++) {
                refreshPositions(mPositions.getOpenPositions().get(i).getCompanyTicker(),
                        mPositions.getOpenPositions().get(i));
                Log.i(TAG + " price ", mPositions.getOpenPositions().get(i).getCost()+"");
            }
        }

        //Log.i(TAG, mPositions.getSize()+"");
        if (getIntent()!= null && getIntent().getExtras() != null) {
            Intent intent = getIntent();
            mPosition = intent.getParcelableExtra(PopupActivity.POSITION_DETAILS);
            mPositions.addItem(mPosition);
            Log.i(TAG, "length of array " + mPositions.getSize()+"");

        }

        OpenPositionAdapter adapter = new OpenPositionAdapter(this, mPositions.getOpenPositions());

        mListView.setAdapter(adapter);
        mListView.setEmptyView(mEmptyTextView);

        mBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PortfolioActivity.this, BuyActivity.class);
                startActivity(intent);
            }
        });

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
        position.setPrice(wholeQuote.getDouble("LastPrice"));

    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "entered onPause");

        if (getIntent()!= null && getIntent().getExtras() != null) {
            getIntent().removeExtra(PopupActivity.POSITION_DETAILS);
        }

        Gson gson = new Gson();
        String json = gson.toJson(mPositions); // myObject - instance of MyObject
        mEditor.putString(POSITIONS_ARRAY, json);
        mEditor.commit();

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
