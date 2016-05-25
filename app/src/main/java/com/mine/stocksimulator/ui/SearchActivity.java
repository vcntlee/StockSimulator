package com.mine.stocksimulator.ui;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mine.stocksimulator.R;
import com.mine.stocksimulator.adapter.SearchResultAdapter;
import com.mine.stocksimulator.data.SearchResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class SearchActivity extends AppCompatActivity {

    public static final String TAG = SearchActivity.class.getSimpleName();
    public static final String QUERY_TICKER = "QUERY_TICKER";
    private ListView mListView;
    private EditText mSearchEditText;
    private Button mSearchButton;
    private boolean mIsValidSearch;
    private ArrayList<SearchResult> mSearchResults = new ArrayList<>();
    private TextView mFailedSearch;
    private boolean mIsFromPortfolio;
    private TextView mErrorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Intent intent = getIntent();
        mIsFromPortfolio = intent.getBooleanExtra(PortfolioActivity.WHERE_IS_HOME, true);

        Toolbar toolbar = (Toolbar) findViewById(R.id.searchActivityAppBar);
        mListView = (ListView) findViewById(android.R.id.list);
        mSearchEditText = (EditText) findViewById(R.id.searchEditText);
        mSearchButton = (Button) findViewById(R.id.searchButton);
        mFailedSearch = (TextView) findViewById(R.id.failedSearch);
        mErrorMessage = (TextView) findViewById(R.id.emptyMessage);

        if (toolbar != null){
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Search");
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.showSoftInput(mSearchEditText, InputMethodManager.SHOW_FORCED);

        mSearchEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mErrorMessage.setVisibility(View.INVISIBLE);
                mSearchEditText.setCursorVisible(true);
            }
        });
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mErrorMessage.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mErrorMessage.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                mErrorMessage.setVisibility(View.INVISIBLE);
            }
        });


        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String search = mSearchEditText.getText().toString();
                mSearchEditText.setText("");
                // this hides keyboard

                manager.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);

                resetAdapter();

                if (search.length() == 0) {
                    Toast.makeText(SearchActivity.this, "Please enter some text", Toast.LENGTH_LONG).show();
                } else {
                    Log.i(TAG, search);
                    getSearchRequest(search);
                }
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (isNetworkAvailable()) {
                    String ticker = mSearchResults.get(position).getTicker();
                    Intent intent = new Intent(SearchActivity.this, StockProfileActivity.class);
                    intent.putExtra(QUERY_TICKER, ticker);


                    if (mIsFromPortfolio)
                        intent.putExtra(PortfolioActivity.WHERE_IS_HOME, true);
                    else
                        intent.putExtra(PortfolioActivity.WHERE_IS_HOME, false);


                    startActivity(intent);
                }

                else{
                    Toast.makeText(SearchActivity.this, "No Internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void getSearchRequest(String searchName) {
        //TODO need to check network availability
        String baseUrl = "http://dev.markitondemand.com/MODApis/Api/v2/Lookup/json?input=";
        String completeUrl = baseUrl + searchName;

        Log.i(TAG + " completeURL ", completeUrl);

        if (isNetworkAvailable()) {
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
                        if (response.isSuccessful()) {
                            mIsValidSearch = getSearchResults(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mIsValidSearch) {
                                        updateDisplay();
                                    } else {
                                        mFailedSearch.setVisibility(View.VISIBLE);
                                        mFailedSearch.setText("Search produced 0 results");
                                    }
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    displayErrorMessage("Your search failed, please try again");
                                }
                            });
                            alertUserAboutError();
                        }

                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSONException caught: ", e);
                    }
                }

            });
        }
        else{
            displayErrorMessage("No Internet connection");
        }
    }



    private boolean getSearchResults(String jsonData) throws JSONException{
        JSONArray searchArray = new JSONArray(jsonData);
        if (searchArray.length() == 0){
            return false;
        }

        for (int i = 0; i < searchArray.length(); i++){
            JSONObject searchObject = searchArray.getJSONObject(i);
            SearchResult searchResult = new SearchResult();
            searchResult.setTicker(searchObject.getString("Symbol"));
            searchResult.setCompanyName(searchObject.getString("Name"));
            mSearchResults.add(searchResult);
        }


        return true;
    }

    private void updateDisplay() {
        mFailedSearch.setVisibility(View.INVISIBLE);
        SearchResultAdapter adapter = new SearchResultAdapter(this, mSearchResults);
        mListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void resetAdapter(){
        mSearchResults.clear();
        mSearchResults = new ArrayList<>();
        SearchResultAdapter adapter = new SearchResultAdapter(this, mSearchResults);
        mListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    private void alertUserAboutError() {
        Log.i(TAG, "something went wrong");
    }

    private void displayErrorMessage(String s) {

        mErrorMessage.setText(s);
        mErrorMessage.setVisibility(View.VISIBLE);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            if (mIsFromPortfolio) {
                Intent intent = new Intent(this, PortfolioActivity.class);
                NavUtils.navigateUpTo(this, intent);
            }
            else{
                Intent intent = new Intent(this, WatchlistActivity.class);
                NavUtils.navigateUpTo(this, intent);
            }
        }
        return super.onOptionsItemSelected(item);
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
//
//
}
