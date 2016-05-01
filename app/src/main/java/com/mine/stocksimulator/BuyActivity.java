package com.mine.stocksimulator;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BuyActivity extends AppCompatActivity {

    public static final String TAG = BuyActivity.class.getSimpleName();
    private Button mButton;
    private EditText mSearchEditText;
    private Quote mQuote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);

        mButton = (Button) findViewById(R.id.searchButton);
        mSearchEditText = (EditText) findViewById(R.id.searchEditText);




        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String companyName = mSearchEditText.getText().toString();
                Log.i(TAG, companyName);
                Toast.makeText(BuyActivity.this, companyName, Toast.LENGTH_LONG).show();
                getQuotes(companyName);
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
                        getQuote(jsonData);
                    } else {
                        //alertUserAboutError();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Exception caught: ", e);
                } catch (JSONException e){
                    Log.e(TAG, "Exception caught: ", e);
                }
            }
        });


    }

    private void getQuote(String jsonData) throws JSONException {
        JSONObject wholeQuote = new JSONObject(jsonData);
        JSONObject list = wholeQuote.getJSONObject("list");
        JSONArray resources = list.getJSONArray("resources");
        JSONObject empty = resources.getJSONObject(0);
        JSONObject resource = empty.getJSONObject("resource");
        JSONObject fields = resource.getJSONObject("fields");

        Log.i(TAG, fields.getString("name"));
        Log.i(TAG, fields.getString("price"));
        Log.i(TAG, fields.getString("symbol"));

        mQuote.setName(fields.getString("name"));
        double price = (double) Math.round(Double.parseDouble(fields.getString("price"))) / 100;
        mQuote.setPrice(price);
        mQuote.setSymbol(fields.getString("symbol"));

    }
}
