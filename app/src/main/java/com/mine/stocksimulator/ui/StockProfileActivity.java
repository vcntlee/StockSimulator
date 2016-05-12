package com.mine.stocksimulator.ui;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.mine.stocksimulator.R;
import com.mine.stocksimulator.adapter.StockProfileAdapter;
import com.mine.stocksimulator.data.ChartProfile;
import com.mine.stocksimulator.data.StockProfile;
import com.mine.stocksimulator.data.StockProfileFieldMember;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StockProfileActivity extends AppCompatActivity {


    public static final String TAG = StockProfileActivity.class.getSimpleName();
    public static final String QUOTE_DETAILS = "QUOTE_DETAILS";
    public static final String CHART_DETAILS = "CHART_DETAILS";

    /* this is for the chart */
    private WebView mChartWebView;
    private ChartProfile mChartProfile;
    private RadioGroup mRadioGroup;

    /* this is for the listview */
    private static final int STOCK_PROFILE_SIZE = 12;
    private final String[] LEFT_VALUES_ARRAY = {"Name", "Symbol", "Price", "Change",
        "% Change", "Market Cap", "Volume", "Change YTD", "Change % YTD", "High",
        "Low", "Open"};
    private String[] RIGHT_VALUES_ARRAY = new String[STOCK_PROFILE_SIZE];
    private ListView mListView;
    private StockProfile mStockProfile;
    private StockProfileFieldMember[] mStockProfileFieldMembers;


    private LinearLayout mBuySellContainer;
    private Button mBuyButton;
    private Button mGetChartButton;
    private Button mCancelButton;

    private boolean isValidSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stockprofile);

        mChartWebView = (WebView) findViewById(R.id.chartWebView);
        mListView = (ListView) findViewById(R.id.stockProfileListView);
        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        Log.i(TAG, "entered onCreate");

//        mBuySellContainer = (LinearLayout) findViewById(R.id.buySellContainer);
//        mBuyButton = (Button) findViewById(R.id.buyButton);
//        mGetChartButton = (Button) findViewById(R.id.getChartButton);
//        mCancelButton = (Button) findViewById(R.id.cancelButton);

        Intent intent = getIntent();
        final String companyName = intent.getStringExtra(SearchActivity.QUERY_TICKER);

        Log.i(TAG + " company", companyName);
        getRequest(companyName);
        getChartRequest(companyName, "Week");

//        mBuyButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(BuyActivity.this, PopupActivity.class);
//                intent.putExtra(QUOTE_DETAILS, mStockProfile);
//                startActivity(intent);
//            }
//        });
//
//        mGetChartButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Intent intent = new Intent(BuyActivity.this, ChartActivity.class);
//                intent.putExtra(CHART_DETAILS, mChartProfile);
//                startActivity(intent);
//
//            }
//        });
//
//        mCancelButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(BuyActivity.this, PortfolioActivity.class);
//                startActivity(intent);
//            }
//        });

        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.i(TAG, "Hello world");
                if (checkedId == R.id.weekRadioButton){
                    getRequest(companyName);
                    getChartRequest(companyName, "Week");

                }
                else if(checkedId == R.id.monthRadioButton){
                    getRequest(companyName);
                    getChartRequest(companyName, "Month");

                }
                else if(checkedId == R.id.yearRadioButton){
                    getRequest(companyName);
                    getChartRequest(companyName, "Year");

                }
            }
        });

    }

    private void getChartRequest(String companyName, final String period) {

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

        jsonParams = String.format(jsonParams, todaysDate, days, dataPeriod, companyName);
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

                        getChartInfo(jsonData, period);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                generateChart();
                            }
                        });

                    } else {
                        alertUserAboutError();
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


    private void getRequest(String companyName) {

        String baseUrl = "http://dev.markitondemand.com/MODApis/Api/v2/Quote/json?symbol=%1$s";
        String completeUrl = String.format(baseUrl, companyName);

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

                        isValidSearch = getQuote(jsonData);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isValidSearch) {
                                    updateDisplay();
                                }
                                else{
                                    Toast.makeText(StockProfileActivity.this, "OOPS! Your search didn't go " +
                                            "through, please try again.", Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                    } else {
                        alertUserAboutError();
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

    private void generateChart() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        String jsFunction = createFunction(mChartProfile.getSizeDates());

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
                + "    <div id=\"chart_div\" style=\"width: 400px; height: 200px;\"></div>"
                + "    <img style=\"padding: 0; margin: 0 0 0 330px; display: block;\"/>"
                + "  </body>" + "</html>";

        WebSettings webSettings = mChartWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mChartWebView.requestFocusFromTouch();
        mChartWebView.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "utf-8", null );

        createFunction(mChartProfile.getSizeDates());
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
                        +   "var options={ hAxis: { title: 'Date'}, vAxis: {title: 'Price'}};"
                        +   "var chart = new google.visualization.LineChart(document.getElementById('chart_div'));"
                        +   "chart.draw(data, options);"
                        +   "}";

        Log.i(TAG+" xvals", xValues);
        Log.i(TAG+" yvals", yValues);
        return jsFunction;

    }

    private void getChartInfo(String jsonData, String period) throws JSONException, ParseException{

        JSONObject wholeChartData = new JSONObject(jsonData);
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
    }

    private void updateDisplay() {
        Log.i(TAG + " length", mStockProfileFieldMembers.length+"");
        StockProfileAdapter adapter = new StockProfileAdapter(this, mStockProfileFieldMembers);
        Log.i(TAG + " length", mStockProfileFieldMembers.length+"");

        mListView.setAdapter(adapter);


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
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        java.util.Date date = formatter.parse(unformattedDate);
        long timeMili = date.getTime();
        SimpleDateFormat newFormatter = new SimpleDateFormat("yy-MM-dd");
        Log.i(TAG+" printme", newFormatter.format(timeMili));
        return newFormatter.format(timeMili);

//        TimeZone timezone = TimeZone.getTimeZone("America/New_York");
//
//        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
//        Date date = formatter.parse(unformattedDate);
//        Log.i(TAG + " date", date.toString());
//        Calendar calendar = Calendar.getInstance(timezone, Locale.ENGLISH);
//        calendar.setTime(date);
//
//
//        if (period.equals("Week")) {
//
//            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
//            SimpleDateFormat newFormatter = new SimpleDateFormat("EE", Locale.ENGLISH);
//            String dayOfWeekString = newFormatter.format(dayOfWeek);
//            Log.i(TAG+" day of week", dayOfWeekString);
//            return dayOfWeekString;
//        }
//        else if (period.equals("Month")){
//
//            int week = calendar.get(Calendar.WEEK_OF_MONTH);
//            return week+"";
//        }
//        else{
//
//            int monthOfYear = calendar.get(Calendar.MONTH);
//            SimpleDateFormat newFormatter = new SimpleDateFormat("MMM", Locale.ENGLISH);
//            return newFormatter.format(monthOfYear);
//        }

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
