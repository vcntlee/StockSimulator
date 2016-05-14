package com.mine.stocksimulator.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mine.stocksimulator.R;
import com.mine.stocksimulator.data.AccountSummary;
import com.mine.stocksimulator.data.OpenPosition;
import com.mine.stocksimulator.data.OpenPositionsMap;
import com.mine.stocksimulator.data.StockProfile;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TradeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private static final String TAG = TradeActivity.class.getSimpleName();
    public static final String POSITION_DETAILS = "POSITION_DETAILS";
    public static final String ACCOUNT_DETAILS = "ACCOUNT_DETAILS";
    private Spinner mActionSpinner;
    private String mAction;
    private EditText mEditText;
    private TextView mPricePerShareTextView;
    private TextView mTotalCostTextView;
    private TextView mRemainingCashTextView;
    private TextView mCurrentCashTextView;
    private Button mTradeButton;
    private Button mCancelButton;
    private int mNumShares;
    private double mTotalCost;
    private double mCurrentCash;
    private AccountSummary mAccountSummary;
    private OpenPosition mOpenPosition;
    private OpenPositionsMap mPositionsMap;

    private StockProfile mStockProfile;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade);

        // get intent from Profile
        Intent intent = getIntent();
        mStockProfile = intent.getParcelableExtra(StockProfileActivity.QUOTE_DETAILS);
        Log.i(TAG+" mStockProfile", mStockProfile.getSymbol());

        // get shared prefs of account summary
        SharedPreferences sharedPreferences = getSharedPreferences(PortfolioActivity.PREFS_ACCOUNT_SUMMARY_FILE
                , Context.MODE_PRIVATE);
        String jsonSummary = sharedPreferences.getString(PortfolioActivity.ACCOUNT_SUMMARY, "");

        if (!jsonSummary.equals("")){
            mAccountSummary = new Gson().fromJson(jsonSummary, AccountSummary.class);
        }
        else{
            Log.e(TAG, "AccountSummary is not set");
        }


        Toolbar toolbar = (Toolbar) findViewById(R.id.tradeActionBar);
        if(toolbar != null){
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(mStockProfile.getSymbol());
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mActionSpinner = (Spinner) findViewById(R.id.actionSpinner);
        mEditText = (EditText) findViewById(R.id.numberOfSharesInput);
        mPricePerShareTextView = (TextView) findViewById(R.id.pricePerShare);
        mTotalCostTextView = (TextView) findViewById(R.id.totalCost);
        mRemainingCashTextView = (TextView) findViewById(R.id.remainingCash);
        mCurrentCashTextView = (TextView) findViewById(R.id.currentCash);
        mTradeButton = (Button) findViewById(R.id.okButton);
        mCancelButton = (Button) findViewById(R.id.cancelButton);

        ArrayAdapter adapter =
                ArrayAdapter.createFromResource(this,
                        R.array.tradeAction,
                        android.R.layout.simple_spinner_dropdown_item);

        mActionSpinner.setAdapter(adapter);
        mActionSpinner.setOnItemSelectedListener(this);

        // populate price and remaining balance

        mCurrentCash = mAccountSummary.getAvailableCash();
        mPricePerShareTextView.setText("$ " + mStockProfile.getPrice());
        mCurrentCashTextView.setText("$ " + mCurrentCash);


        mEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditText.setCursorVisible(true);
            }
        });

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                //TODO need to account for non-numeric



                // populating total cost
                if (s.length() > 0) {
                    try {
                        mNumShares = Integer.parseInt(s.toString());
                        mTotalCost = round((mNumShares * mStockProfile.getPrice()), 3);
                        mTotalCostTextView.setText("$ " + mTotalCost);
                        mRemainingCashTextView.setText("$ " + (mCurrentCash - mTotalCost));
                    }catch (NumberFormatException nfe){
                        Toast.makeText(TradeActivity.this,
                                "please input valid numbers, + and - not allowed", Toast.LENGTH_LONG).show();
                    }

                }
            }
        });

        mEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    manager.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);

                    return true;
                }
                return false;
            }
        });

        // ticker, price, cost, shares, return

        mTradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO need to figure out whether it's a long, short, buy, sell

                if (mNumShares <= 0){
                    Toast.makeText(TradeActivity.this,
                            "very tough to buy <= 0 shares", Toast.LENGTH_LONG).show();
                }
                else{

                    mAccountSummary.setAvailableCash(mCurrentCash - mTotalCost);

                    mOpenPosition = new OpenPosition();
                    mOpenPosition.setCompanyTicker(mStockProfile.getSymbol());
                    mOpenPosition.setShares(mNumShares);
                    mOpenPosition.setPrice(mStockProfile.getPrice());
                    mOpenPosition.setCost(mStockProfile.getPrice());

                    if (!isActionValid()){
                        Toast.makeText(TradeActivity.this,
                                "Action chosen incompatible with portfolio transaction",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
//                    if (mAction.equals("Long") || mAction.equals("Short")) {
//                        mOpenPosition.setType(mAction);
//                    }

                    Intent intent = new Intent(TradeActivity.this, PortfolioActivity.class);
                    intent.putExtra(POSITION_DETAILS, mOpenPosition);
                    intent.putExtra(ACCOUNT_DETAILS, mAccountSummary);
                    startActivity(intent);
                }
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TradeActivity.this, PortfolioActivity.class);
                startActivity(intent);
            }
        });

    }

    // check to see if there are duplicates and if actions match in the portfolio
    private boolean isActionValid() {
        boolean isValid = true;
        SharedPreferences sharedPreferences = getSharedPreferences(PortfolioActivity.PREFS_POSITIONS_MAP_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = sharedPreferences.getString(PortfolioActivity.POSITIONS_MAP, "");

        if (!json.equals("")){
            mPositionsMap = new Gson().fromJson(json, OpenPositionsMap.class);
        }
        else{
            mPositionsMap = new OpenPositionsMap();
        }

        if (mPositionsMap.doesKeyExist(mOpenPosition.getCompanyTicker())){
            /* type = long, action = long --> ok
                type = long, action = short --> no
                type = long, action = buy --> no
                type = long, action = sell --> ok */

            /** FIXME need to subtract from position
             *  for all the trues, need to update textviews accordingly
             *  for the double longs or double shorts, need to do a weighted average
             *  */


            String mapType = mPositionsMap.getKey(mOpenPosition.getCompanyTicker());
            if (mapType.equals("Long") && mAction.equals("Long")){
                isValid = true;
            }
            else if (mapType.equals("Long") && mAction.equals("Short")){
                isValid = false;
            }
            else if (mapType.equals("Long") && mAction.equals("Buy")){
                isValid = false;
            }
            else if (mapType.equals("Long") && mAction.equals("Sell")){
                isValid = true;

            }

            /* type = short, action = long --> no
                type = short, action = short --> yes
                type = short, action = buy --> yes
                type = short, action = sell --> no */

            else if (mapType.equals("Short") && mAction.equals("Long")){
                isValid = false;

            }
            else if (mapType.equals("Short") && mAction.equals("Short")){
                isValid = true;
            }
            else if (mapType.equals("Short") && mAction.equals("Buy")){
                isValid = true;
            }
            else if (mapType.equals("Short") && mAction.equals("Sell")){
                isValid = false;
            }
        }
        else{
            //only valid options are long and short
            if (mAction.equals("Long")){
                mOpenPosition.setType(mAction);
                isValid = true;
            }
            else if (mAction.equals("Short")){
                mOpenPosition.setType(mAction);
                isValid = true;
            }
            else{
                isValid = false;
            }

        }


        return isValid;
    }

    public static double round(double value, int places) {
        //if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        TextView actionText = (TextView) view;
        mAction = actionText.getText().toString();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            Intent intent = new Intent(this, PortfolioActivity.class);
            NavUtils.navigateUpTo(this, intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
