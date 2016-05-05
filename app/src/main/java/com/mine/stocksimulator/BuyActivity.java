package com.mine.stocksimulator;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BuyActivity extends AppCompatActivity {

    public static final String TAG = BuyActivity.class.getSimpleName();
    public static final String QUOTE_DETAILS = "QUOTE_DETAILS";
    private Button mButton;
    private EditText mSearchEditText;
    private Quote mQuote;
    private TableLayout mFactsContainer;
    private TextView mCompanyValue;
    private TextView mPriceValue;
    private TextView mSymbolValue;
    private TextView mFailMessage;
    private LinearLayout mBuySellContainer;
    private Button mBuyButton;
    private boolean isValidSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);

        Log.i(TAG, "entered onCreate");

        mButton = (Button) findViewById(R.id.searchButton);
        mSearchEditText = (EditText) findViewById(R.id.searchEditText);
        mFactsContainer = (TableLayout) findViewById(R.id.factsContainer);
        mFailMessage = (TextView) findViewById(R.id.failMessage);
        mCompanyValue = (TextView) findViewById(R.id.companyValue);
        mPriceValue = (TextView) findViewById(R.id.priceValue);
        mSymbolValue = (TextView) findViewById(R.id.symbolValue);
        mBuySellContainer = (LinearLayout) findViewById(R.id.buySellContainer);
        mBuyButton = (Button) findViewById(R.id.buyButton);


        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i(TAG, "from onCreate " + mSearchEditText.getText().toString());

                if (mSearchEditText.getText().toString().length() == 0) {
                    Log.i(TAG, "entered 0");
                    Toast.makeText(BuyActivity.this, "Please enter some text", Toast.LENGTH_LONG).show();
                }
                else {

                    String companyName = mSearchEditText.getText().toString();
                    Log.i(TAG, companyName);
                    Toast.makeText(BuyActivity.this, companyName, Toast.LENGTH_LONG).show();
                    getQuotes(companyName);
                }
            }
        });

        mBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BuyActivity.this, PopupActivity.class);
                intent.putExtra(QUOTE_DETAILS, mQuote);
                startActivity(intent);
            }
        });

    }


    private void getQuotes(String companyName) {

        String quoteUrl = "http://finance.yahoo.com/webservice/v1/symbols/%1$s/quote?format=json";
        quoteUrl = String.format(quoteUrl, companyName);
        Log.i(TAG, quoteUrl);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request
                .Builder()
                .url(quoteUrl)
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
                                toggleFacts(isValidSearch);
                            }
                        });
                    } else {
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

    private void updateDisplay() {
        mCompanyValue.setText(mQuote.getName());
        mPriceValue.setText(mQuote.getPrice() + "");
        mSymbolValue.setText(mQuote.getSymbol());

    }

    private void toggleFacts(final boolean isValidSearch) {

        if (isValidSearch){
            mFactsContainer.setVisibility(View.VISIBLE);
            mFailMessage.setVisibility(View.INVISIBLE);
            mBuySellContainer.setVisibility(View.VISIBLE);
        }
        else{
            mFactsContainer.setVisibility(View.INVISIBLE);
            mFailMessage.setVisibility(View.VISIBLE);
            mBuySellContainer.setVisibility(View.INVISIBLE);
        }

    }

    private void alertUserAboutError() {
        Toast.makeText(this, "response is not successful", Toast.LENGTH_LONG).show();
    }

    private boolean getQuote(String jsonData) throws JSONException {

        boolean isValidSearch = true;

        JSONObject wholeQuote = new JSONObject(jsonData);
        JSONObject list = wholeQuote.getJSONObject("list");
        JSONArray resources = list.getJSONArray("resources");

        if (resources.length() == 0){
            isValidSearch = false;
            return isValidSearch;
        }

        JSONObject empty = resources.getJSONObject(0);
        JSONObject resource = empty.getJSONObject("resource");
        JSONObject fields = resource.getJSONObject("fields");

        Log.i(TAG, fields.getString("name"));
        Log.i(TAG, fields.getString("price"));
        Log.i(TAG, fields.getString("symbol"));

        mQuote = new Quote();

        mQuote.setName(fields.getString("name"));
        double price = round(Double.parseDouble(fields.getString("price")), 3);
        mQuote.setPrice(price);
        Log.i(TAG, mQuote.getPrice() + "");

        mQuote.setSymbol(fields.getString("symbol"));
        Log.i(TAG, mQuote.getSymbol());
        Log.i(TAG, Boolean.toString(isValidSearch));

        return isValidSearch;

    }

    public static double round(double value, int places) {
        //if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
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
