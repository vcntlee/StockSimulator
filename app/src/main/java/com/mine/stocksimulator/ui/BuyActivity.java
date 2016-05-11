package com.mine.stocksimulator.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mine.stocksimulator.R;
import com.mine.stocksimulator.data.ChartProfile;
import com.mine.stocksimulator.data.Quote;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BuyActivity extends AppCompatActivity {

    public static final String TAG = BuyActivity.class.getSimpleName();
    public static final String QUOTE_DETAILS = "QUOTE_DETAILS";
    public static final String CHART_DETAILS = "CHART_DETAILS";
    private EditText mSearchEditText;
    private TableLayout mFactsContainer;

    private Quote mQuote;
    private TextView mCompanyValue;
    private TextView mPriceValue;
    private TextView mSymbolValue;
    private TextView mAbsoluteChange;
    private TextView mPercentChange;
    private TextView mMarketCap;
    private TextView mVolume;
    private TextView mChangeYtd;
    private TextView mChangePercentYtd;
    private TextView mHigh;
    private TextView mLow;
    private TextView mOpen;

    private TextView mFailMessage;
    private LinearLayout mBuySellContainer;

    private Button mSearchButton;
    private Button mBuyButton;
    private Button mGetChartButton;
    private Button mCancelButton;

    private ChartProfile mChartProfile;

    private boolean isValidSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);

        Log.i(TAG, "entered onCreate");

        mSearchButton = (Button) findViewById(R.id.searchButton);
        mSearchEditText = (EditText) findViewById(R.id.searchEditText);
        mFactsContainer = (TableLayout) findViewById(R.id.factsContainer);
        mFailMessage = (TextView) findViewById(R.id.failMessage);

        mCompanyValue = (TextView) findViewById(R.id.companyValue);
        mPriceValue = (TextView) findViewById(R.id.priceValue);
        mSymbolValue = (TextView) findViewById(R.id.symbolValue);
        mAbsoluteChange = (TextView) findViewById(R.id.changeValue);
        mPercentChange = (TextView) findViewById(R.id.changePercentValue);
        mMarketCap = (TextView) findViewById(R.id.marketCapValue);
        mVolume = (TextView) findViewById(R.id.volumeValue);
        mChangeYtd = (TextView) findViewById(R.id.changeYtdValue);
        mChangePercentYtd = (TextView) findViewById(R.id.changePercentYtdValue);
        mHigh = (TextView) findViewById(R.id.highValue);
        mLow = (TextView) findViewById(R.id.lowValue);
        mOpen = (TextView) findViewById(R.id.openValue);

        mBuySellContainer = (LinearLayout) findViewById(R.id.buySellContainer);
        mBuyButton = (Button) findViewById(R.id.buyButton);
        mGetChartButton = (Button) findViewById(R.id.getChartButton);
        mCancelButton = (Button) findViewById(R.id.cancelButton);

        Intent intent = getIntent();
        String companyName = intent.getStringExtra(SearchActivity.QUERY_TICKER);

        getRequest(companyName, "quote");
        getRequest(companyName, "chart");


        mBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BuyActivity.this, PopupActivity.class);
                intent.putExtra(QUOTE_DETAILS, mQuote);
                startActivity(intent);
            }
        });

        mGetChartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(BuyActivity.this, ChartActivity.class);
                intent.putExtra(CHART_DETAILS, mChartProfile);
                startActivity(intent);

            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BuyActivity.this, PortfolioActivity.class);
                startActivity(intent);
            }
        });



    }


    private void getRequest(String companyName, final String requestType) {

        String completeUrl;
        if (requestType.equals("quote")) {
            String baseUrl = "http://dev.markitondemand.com/MODApis/Api/v2/Quote/json?symbol=%1$s";
            completeUrl = String.format(baseUrl, companyName);
        }
        else{
            String baseChartUrl = "http://dev.markitondemand.com/MODApis/Api/v2/InteractiveChart/json?parameters=";
            String jsonParams = "{\"Normalized\":false,\"StartDate\":\"2016-04-25T00:00:00-00\", \"EndDate\":\"2016-05-05T00:00:00-00\", \"DataPeriod\":\"Day\", \"Elements\":[{\"Symbol\":\"%1$s\", \"Type\":\"price\", \"Params\":[\"c\"]}]}";
            jsonParams = String.format(jsonParams, companyName);
            completeUrl = baseChartUrl + jsonParams;
        }
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

                    if (response.isSuccessful()) {

                        if (requestType.equals("quote")) {
                            isValidSearch = getQuote(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (isValidSearch) {
                                        updateDisplay();
                                    }
                                    toggleFacts(isValidSearch);
                                }
                            });
                        }

                        else{
                            getChartInfo(jsonData);

                        }

                    } else {
                        alertUserAboutError();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Exception caught: ", e);
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException caught: ", e);
                    Toast.makeText(BuyActivity.this, "oops!", Toast.LENGTH_LONG).show();
                }catch (ParseException e){
                    Log.e(TAG, "Unable to parse", e);
                }
            }
        });


    }



    private void getChartInfo(String jsonData) throws JSONException, ParseException{

        JSONObject wholeChartData = new JSONObject(jsonData);
        JSONArray dates = wholeChartData.getJSONArray("Dates");

        Log.i(TAG, dates.toString());

        mChartProfile = new ChartProfile();
        for (int i = 0; i < dates.length(); i++){
            String formattedDate = getFormattedDate(dates.getString(i));
            mChartProfile.setChartDate(formattedDate);
            mChartProfile.addToDates();
        }

        JSONArray elements = wholeChartData.getJSONArray("Elements");
        JSONObject empty = elements.getJSONObject(0);
        JSONObject dataSeries = empty.getJSONObject("DataSeries");
        JSONObject close = dataSeries.getJSONObject("close");
        JSONArray values = close.getJSONArray("values");

        for (int i = 0; i < values.length(); i++){
            mChartProfile.setChartValue(values.getDouble(i));
            mChartProfile.addToValues();
        }
    }

    private void updateDisplay() {

        mCompanyValue.setText(mQuote.getName());
        mSymbolValue.setText(mQuote.getSymbol());
        mPriceValue.setText(mQuote.getPrice() + "");
        mAbsoluteChange.setText(mQuote.getAbsoluteChange() + "");
        mPercentChange.setText(mQuote.getPercentChange() + "");
        mMarketCap.setText(mQuote.getMarketCap() + "");
        mVolume.setText(mQuote.getVolume()+"");
        mChangeYtd.setText(mQuote.getChangeYtd()+"");
        mChangePercentYtd.setText(mQuote.getChangePercentYtd()+"");
        mHigh.setText(mQuote.getHigh()+"");
        mLow.setText(mQuote.getLow()+"");
        mOpen.setText(mQuote.getOpen()+"");

    }

    private void toggleFacts(final boolean isValidSearch) {

        if (isValidSearch){
            mFactsContainer.setVisibility(View.VISIBLE);
            mGetChartButton.setVisibility(View.VISIBLE);
            mFailMessage.setVisibility(View.INVISIBLE);
            mBuySellContainer.setVisibility(View.VISIBLE);

        }
        else{
            mFactsContainer.setVisibility(View.INVISIBLE);
            mGetChartButton.setVisibility(View.INVISIBLE);
            mFailMessage.setVisibility(View.VISIBLE);
            mBuySellContainer.setVisibility(View.INVISIBLE);
        }

    }

    private void alertUserAboutError() {
        //Toast.makeText(this, "response is not successful", Toast.LENGTH_LONG).show();
        Log.i(TAG, "response is not successful");
    }

    private boolean getQuote(String jsonData) throws JSONException {

        JSONObject wholeQuote = new JSONObject(jsonData);

        if (wholeQuote.has("Message")){
            return false;
        }

        mQuote = new Quote();
        mQuote.setName(wholeQuote.getString("Name"));
        mQuote.setSymbol(wholeQuote.getString("Symbol"));
        mQuote.setPrice(wholeQuote.getDouble("LastPrice"));
        mQuote.setAbsoluteChange(round(wholeQuote.getDouble("Change"), 2));
        mQuote.setPercentChange(wholeQuote.getDouble("ChangePercent"));
        mQuote.setMarketCap(wholeQuote.getLong("MarketCap"));
        mQuote.setVolume(wholeQuote.getLong("Volume"));
        mQuote.setChangeYtd(wholeQuote.getDouble("ChangeYTD"));
        mQuote.setChangePercentYtd(wholeQuote.getDouble("ChangePercentYTD"));
        mQuote.setHigh(wholeQuote.getDouble("High"));
        mQuote.setLow(wholeQuote.getDouble("Low"));
        mQuote.setOpen(wholeQuote.getDouble("Open"));

        Log.i(TAG, mQuote.getName());
        Log.i(TAG, mQuote.getSymbol());
        Log.i(TAG, mQuote.getPrice()+"");
        Log.i(TAG, mQuote.getAbsoluteChange()+"");
        Log.i(TAG, mQuote.getPercentChange()+"");
        Log.i(TAG, mQuote.getMarketCap()+"");
        Log.i(TAG, mQuote.getVolume()+"");
        Log.i(TAG, mQuote.getChangeYtd()+"");
        Log.i(TAG, mQuote.getChangePercentYtd()+"");
        Log.i(TAG, mQuote.getHigh()+"");
        Log.i(TAG, mQuote.getLow()+"");
        Log.i(TAG, mQuote.getOpen()+"");

        return true;

    }

    public static double round(double value, int places) {
        //if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public String getFormattedDate(String unformattedDate) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        java.util.Date date = formatter.parse(unformattedDate);
        long timeMili = date.getTime();
        SimpleDateFormat newFormatter = new SimpleDateFormat("yy-MM-dd");
        return newFormatter.format(timeMili);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "entered on resume state");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "calling onDestroy");
    }
}
