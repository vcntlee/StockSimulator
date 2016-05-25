package com.mine.stocksimulator.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mine.stocksimulator.R;
import com.mine.stocksimulator.adapter.StockProfileAdapter;
import com.mine.stocksimulator.data.ChartProfile;
import com.mine.stocksimulator.data.StockProfile;
import com.mine.stocksimulator.data.StockProfileFieldMember;
import com.mine.stocksimulator.data.Watchlist;
import com.mine.stocksimulator.database.WatchlistDataSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StockProfileActivity extends AppCompatActivity {


    public static final String TAG = StockProfileActivity.class.getSimpleName();
    public static final String QUOTE_DETAILS = "QUOTE_DETAILS";

    /* this is for the chart */
    private WebView mChartWebView;
    private ChartProfile mChartProfile;
    private RadioGroup mRadioGroup;
    private ProgressBar mProgressBar;

    /* this is for the listview */
    private static final int STOCK_PROFILE_SIZE = 12;
    private final String[] LEFT_VALUES_ARRAY = {"Name", "Symbol", "Price", "Change",
        "% Change", "Market Cap", "Volume", "Change YTD", "Change % YTD", "High",
        "Low", "Open"};
    private String[] RIGHT_VALUES_ARRAY = new String[STOCK_PROFILE_SIZE];
    private ListView mListView;
    private StockProfile mStockProfile;
    private StockProfileFieldMember[] mStockProfileFieldMembers;


    //private LinearLayout mTradeContainer;
    private Button mTradeButton;
    private Button mCancelButton;

    private String mCompanyName;

    private boolean isValidSearch;
    private boolean isValidChartSearch;
    private TextView mDisplayErrorMessage;
    private String mPeriodChecked;
    private boolean mInWatchlist;
    private Menu mMenu;
    private MenuItem mStarItem;
    private MenuItem mRefreshItem;


    private View mHeader;
    private View mFooter;
    private boolean mIsFromPortfolio;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stockprofile);

        Intent intent = getIntent();
        mCompanyName = intent.getStringExtra(SearchActivity.QUERY_TICKER);
        mIsFromPortfolio = intent.getBooleanExtra(PortfolioActivity.WHERE_IS_HOME, true);

        mInWatchlist = new WatchlistDataSource(this).retrieveOne(mCompanyName);

        mHeader = getLayoutInflater().inflate(R.layout.header_stockprofile, null);
        mFooter = getLayoutInflater().inflate(R.layout.footer_stockprofile, null);
        mChartWebView = (WebView) mHeader.findViewById(R.id.chartWebView);
        mRadioGroup = (RadioGroup) mHeader.findViewById(R.id.radioGroup);
        mDisplayErrorMessage = (TextView) findViewById(R.id.displayErrorMessage);
        mProgressBar = (ProgressBar) mHeader.findViewById(R.id.progressBar);


        mListView = (ListView) findViewById(R.id.stockProfileListView);
        mListView.addHeaderView(mHeader, null, false);


        //mTradeContainer = (LinearLayout) findViewById(R.id.tradeContainer);

        mTradeButton = (Button) mFooter.findViewById(R.id.tradeButton);
        mCancelButton = (Button) mFooter.findViewById(R.id.cancelButton);

        mListView.addFooterView(mFooter, null, false);




        Toolbar toolbar = (Toolbar) findViewById(R.id.stockProfileActivityAppBar);
        if(toolbar!=null){
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(mCompanyName);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        mPeriodChecked = "Week";
        getRequest();
        getChartRequest("Week");



        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.weekRadioButton) {
                    mPeriodChecked = "Week";
                    //getRequest();
                    getChartRequest("Week");

                } else if (checkedId == R.id.monthRadioButton) {
                    mPeriodChecked = "Month";
                    //getRequest();
                    getChartRequest("Month");

                } else if (checkedId == R.id.yearRadioButton) {
                    mPeriodChecked = "Year";
                    //getRequest();
                    getChartRequest("Year");

                }
            }
        });

        mTradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StockProfileActivity.this, TradeActivity.class);
                intent.putExtra(QUOTE_DETAILS, mStockProfile);



                startActivity(intent);
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;

                if (mIsFromPortfolio) {
                    intent = new Intent(StockProfileActivity.this, PortfolioActivity.class);
                }
                else{
                    intent = new Intent(StockProfileActivity.this, WatchlistActivity.class);

                }
                startActivity(intent);
            }
        });


    }

    private class WebClient extends WebViewClient{
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mProgressBar.setVisibility(View.GONE);


        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            mProgressBar.setVisibility(View.VISIBLE);

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mChartWebView.canGoBack()){
            mChartWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void getChartRequest(final String period) {

        int days = 0;
        String dataPeriod = "";
        if (period.equals("Week")) {
            days = 7;
            dataPeriod = "Day";
        }
        else if (period.equals("Month")) {
            days = 30;
            dataPeriod = "Week";
        }
        else if (period.equals("Year")) {
            days = 365;
            dataPeriod = "Month";
        }

        String baseChartUrl = "http://dev.markitondemand.com/MODApis/Api/v2/InteractiveChart/json?parameters=";
        String jsonParams = "{\"Normalized\":false," +
                "\"StartDate\":\"%s\"," +
                "\"NumberOfDays\":%d," +
                "\"DataPeriod\":\"%s\"," +
                "\"Elements\":[{\"Symbol\":\"%s\", \"Type\":\"price\", \"Params\":[\"c\"]}]}";

        Log.i(TAG+" jsonparams", jsonParams);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date today = new Date();
        String todaysDate = formatter.format(today);

        jsonParams = String.format(jsonParams, todaysDate, days, dataPeriod, mCompanyName);
        String completeUrl = baseChartUrl + jsonParams;

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

                        if (isValidSearch) {
                            isValidChartSearch = getChartInfo(jsonData, period);
                        }
                        else{
                            return;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isValidChartSearch) {
                                    generateChart();
                                }
                                else{
                                    updateUiWithErrorState("Stock info unavailable");

                                }
                            }
                        });

                    } else {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(TAG, "getChartRequest is unresponsive");
                                Toast.makeText(StockProfileActivity.this, "Period selected is"
                                    + " unresponsive, try refreshing.", Toast.LENGTH_LONG).show();
                            }
                        });


                    }
                } catch (IOException e) {
                    Log.e(TAG, "Exception caught: ", e);
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException caught: ", e);
                    Toast.makeText(StockProfileActivity.this, "oops!", Toast.LENGTH_LONG).show();
                }catch (ParseException e){
                    Log.e(TAG, "Unable to parse", e);
                }
            }
        });


    }




    private void getRequest() {

        String baseUrl = "http://dev.markitondemand.com/MODApis/Api/v2/Quote/json?symbol=%1$s";
        String completeUrl = String.format(baseUrl, mCompanyName);

        Log.i(TAG, completeUrl);

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
                        Log.v(TAG, jsonData);

                        if (response.isSuccessful()) {

                            isValidSearch = getQuote(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (isValidSearch) {
                                        updateDisplay();
                                    }
                                    else {
                                        updateUiWithErrorState("Stock info unavailable");
                                    }
                                }
                            });

                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateUiWithErrorState("Failed to get a response");
                                }
                            });
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSONException caught: ", e);
                        Toast.makeText(StockProfileActivity.this, "oops!", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        else{
            updateUiWithErrorState("No Internet connection");
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

    private void generateChart() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = mChartWebView.getWidth();
        int height = mChartWebView.getHeight();
        Log.i(TAG+" width", width+"");
        Log.i(TAG+" height", height+"");


        String jsFunction = createFunction(mChartProfile.getSizeDates());
        String widthAndHeight = "<div id=\"chart_div\" style=\"width: %dpx; height: %dpx;\"></div>";
        widthAndHeight = String.format(widthAndHeight, width, height);

        String content = "<html>"
                + "  <head>"
                + "    <script type=\"text/javascript\" src=\"jsapi.js\"></script>"
                + "    <script type=\"text/javascript\">"
                + "      google.load(\"visualization\", \"1\", {packages:[\"corechart\", \"line\"]});"
                + "      google.setOnLoadCallback(drawChart);"
                +       jsFunction
                + "    </script>"
                + "  </head>"
                + "  <body>"
                //+       widthAndHeight
                + "<div id=\"chart_div\" width:\"100%\"></div>"
                + "    <img style=\"padding: 0; margin: 0 0 0 0px; display: block;\"/>"
                + "  </body>" + "</html>";

        mChartWebView.setWebViewClient(new WebClient());

        //WebSettings webSettings = mChartWebView.getSettings();
        mChartWebView.getSettings().setJavaScriptEnabled(true);

        //webSettings.setUseWideViewPort(true);
        //webSettings.setLoadWithOverviewMode(true);

        mChartWebView.requestFocusFromTouch();
        mChartWebView.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "utf-8", null);


    }

    public String createFunction(int size) {
        String xValues = "var xVals = [";
        for (int i = 1; i < size; i++){
            xValues += "'%" + i +""+"$s',";
        }
        xValues += "'%" + size +""+"$s'];";
        xValues = String.format(xValues, mChartProfile.getChartDates().toArray());

        String yValues = "var yVals = [";
        for (int i = 1; i < size; i++){
            yValues += "%" + i +""+"$f,";
        }
        yValues += "%" + size +"" + "$.2f];";
        yValues = String.format(yValues, mChartProfile.getChartValues().toArray());

        String jsFunction =
                "function drawChart(){"
                        +   "var data = new google.visualization.DataTable();"
                        +   "data.addColumn('string', 'Date');"
                        +   "data.addColumn('number', 'Price');"
                        +   xValues + yValues
                        +   "var dataset = [];"
                        +   "for (var i = 0; i < xVals.length; i++){"
                        +   "   dataset.push([xVals[i], yVals[i]]);"
                        +   "}"
                        +   "data.addRows(dataset);"
                        +   "var options={vAxis: {title: 'Price', titleTextStyle:{color:'#9b9b9b'}}, hAxis:{title: 'Date', titleTextStyle:{color:'#9b9b9b'}}, legend:{position:'none'}, series: {0: {color:'#f1b927'}}};"
                        +   "var chart = new google.visualization.LineChart(document.getElementById('chart_div'));"
                        +   "chart.draw(data, options);"
                        +   "}";

        Log.i(TAG+" xvals", xValues);
        Log.i(TAG+" yvals", yValues);
        return jsFunction;

    }

    private boolean getChartInfo(String jsonData, String period) throws JSONException, ParseException{

        JSONObject wholeChartData = new JSONObject(jsonData);



        if (wholeChartData.isNull("Dates")){
            Log.i(TAG, "Dates element is null");
            return false;
        }

        JSONArray dates = wholeChartData.getJSONArray("Dates");



        Log.i(TAG+" getChartInfo", dates.toString());

        mChartProfile = new ChartProfile();
        for (int i = 0; i < dates.length(); i++){
            String formattedDate = getFormattedDate(dates.getString(i), period);
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

        return true;
    }

    private void updateDisplay() {
        Log.i(TAG + " length", mStockProfileFieldMembers.length+"");
        StockProfileAdapter adapter = new StockProfileAdapter(this, mStockProfileFieldMembers);
        Log.i(TAG + " length", mStockProfileFieldMembers.length+"");

        mListView.setAdapter(adapter);


    }


    private void updateUiWithErrorState(String s) {

        mStarItem.setVisible(false);
        mRefreshItem.setVisible(false);

        mChartWebView.setVisibility(View.INVISIBLE);
        mListView.setVisibility(View.INVISIBLE);
        mDisplayErrorMessage.setText(s);
        mDisplayErrorMessage.setVisibility(View.VISIBLE);

    }

    private boolean getQuote(String jsonData) throws JSONException {

        JSONObject wholeQuote = new JSONObject(jsonData);

        if (!wholeQuote.getString("Status").equals("SUCCESS")){
            Log.i(TAG, "status != SUCCESS");
            return false;
        }

        if (wholeQuote.has("Message")){
            return false;
        }

        mStockProfile = new StockProfile();
        mStockProfile.setName(wholeQuote.getString("Name"));
        mStockProfile.setSymbol(wholeQuote.getString("Symbol"));
        mStockProfile.setPrice(wholeQuote.getDouble("LastPrice"));
        mStockProfile.setAbsoluteChange(round(wholeQuote.getDouble("Change"), 2));
        mStockProfile.setPercentChange(wholeQuote.getDouble("ChangePercent"));
        mStockProfile.setMarketCap(wholeQuote.getLong("MarketCap"));
        mStockProfile.setVolume(wholeQuote.getLong("Volume"));
        mStockProfile.setChangeYtd(wholeQuote.getDouble("ChangeYTD"));
        mStockProfile.setChangePercentYtd(wholeQuote.getDouble("ChangePercentYTD"));
        mStockProfile.setHigh(wholeQuote.getDouble("High"));
        mStockProfile.setLow(wholeQuote.getDouble("Low"));
        mStockProfile.setOpen(wholeQuote.getDouble("Open"));

        String name = mStockProfile.getName();
        RIGHT_VALUES_ARRAY[0] = name;
        String symbol = mStockProfile.getSymbol();
        RIGHT_VALUES_ARRAY[1] = symbol;
        String price = round(mStockProfile.getPrice(),2) + "";
        RIGHT_VALUES_ARRAY[2] = price;
        String absChange = round(mStockProfile.getAbsoluteChange(),2) + "";
        RIGHT_VALUES_ARRAY[3] = absChange;
        String percentChange = round(mStockProfile.getPercentChange(),2) + "";
        RIGHT_VALUES_ARRAY[4] = percentChange;
        String marketCap = mStockProfile.getMarketCap() + "";
        RIGHT_VALUES_ARRAY[5] = marketCap;
        String volume = mStockProfile.getVolume() + "";
        RIGHT_VALUES_ARRAY[6] = volume;
        String changeYtd = round(mStockProfile.getChangeYtd(),2) + "";
        RIGHT_VALUES_ARRAY[7] = changeYtd;
        String changePercentYtd = round(mStockProfile.getChangePercentYtd(),2) + "";
        RIGHT_VALUES_ARRAY[8] = changePercentYtd;
        String high = round(mStockProfile.getHigh(), 2) + "";
        RIGHT_VALUES_ARRAY[9] = high;
        String low = round(mStockProfile.getLow(), 2) + "";
        RIGHT_VALUES_ARRAY[10] = low;
        String open = round(mStockProfile.getOpen(), 2) + "";
        RIGHT_VALUES_ARRAY[11] = open;

        mStockProfileFieldMembers = new StockProfileFieldMember[STOCK_PROFILE_SIZE];

        for (int i = 0; i < STOCK_PROFILE_SIZE; i++){
            StockProfileFieldMember fieldMember = new StockProfileFieldMember();
            fieldMember.setLeftValue(LEFT_VALUES_ARRAY[i]);
            fieldMember.setRightValue(RIGHT_VALUES_ARRAY[i]);
            mStockProfileFieldMembers[i] = fieldMember;
        }

        return true;

    }

    public static double round(double value, int places) {
        //if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public String getFormattedDate(String unformattedDate, String period) throws ParseException {
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//
//        java.util.Date date = formatter.parse(unformattedDate);
//        long timeMili = date.getTime();
//        SimpleDateFormat newFormatter = new SimpleDateFormat("yy-MM-dd");
//        Log.i(TAG+" printme", newFormatter.format(timeMili));
//        return newFormatter.format(timeMili);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
        Date date = formatter.parse(unformattedDate);
        Log.i(TAG + " date", date.toString());



        if (period.equals("Week")) {
            SimpleDateFormat newFormatter = new SimpleDateFormat("EE", Locale.ENGLISH);
            return newFormatter.format(date);
        }
        else if (period.equals("Month")){
            SimpleDateFormat newFormatter = new SimpleDateFormat("dd", Locale.ENGLISH);
            return newFormatter.format(date);
        }
        else{

            SimpleDateFormat newFormatter = new SimpleDateFormat("MMM", Locale.ENGLISH);
            return newFormatter.format(date);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_stockprofile, menu);

        mStarItem = menu.findItem(R.id.starOption);
        mRefreshItem = menu.findItem(R.id.refreshOption);


        if (mInWatchlist){
            mStarItem.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.star_fill, null));
        }
        mMenu = menu;
        return true;
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
        else if(id == R.id.refreshOption){
            Log.i(TAG+" periodchecked", mPeriodChecked);
            getRequest();
            getChartRequest(mPeriodChecked);

        }

        else if (id == R.id.starOption){

            WatchlistDataSource dataSource = new WatchlistDataSource(this);
            item = mMenu.findItem(R.id.starOption);
            if (mInWatchlist){
                // change the star to empty
                item.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.star_empty, null));
                // delete from watchlist
                dataSource.delete(mStockProfile.getSymbol());
                mInWatchlist = false;
            }
            else{
                // change star to full

                item.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.star_fill, null));
                // create item in watchlist
                Watchlist watchlist = new Watchlist(mStockProfile.getSymbol(), mStockProfile.getPrice(),
                        TradeActivity.round(mStockProfile.getPercentChange(), 2),
                        TradeActivity.round(mStockProfile.getChangePercentYtd(), 2));
                dataSource.create(watchlist);
                mInWatchlist = true;
            }
        }

        return super.onOptionsItemSelected(item);
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
