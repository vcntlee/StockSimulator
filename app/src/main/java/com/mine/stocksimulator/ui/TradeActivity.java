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
import com.mine.stocksimulator.data.OpenPositionsList;
import com.mine.stocksimulator.data.OpenPositionsMap;
import com.mine.stocksimulator.data.StockProfile;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TradeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private static final String TAG = TradeActivity.class.getSimpleName();
    public static final String POSITION_DETAILS = "POSITION_DETAILS";
    public static final String ACCOUNT_DETAILS = "ACCOUNT_DETAILS";
    public static final String POSITIONS_ARRAY = "POSITIONS_ARRAY";

    private Spinner mActionSpinner;
    private String mAction;
    private EditText mEditText;
    private TextView mPricePerShareTextView;
    private TextView mTotalTransactionTextView;
    private TextView mRemainingCashTextView;
    private TextView mCurrentCashTextView;
    private Button mTradeButton;
    private Button mCancelButton;
    private int mNumShares = 0;
    private double mTotalTransaction;
    private double mCurrentCash;
    private AccountSummary mAccountSummary;
    private OpenPosition mOpenPosition;
    private OpenPositionsMap mPositionsMap;
    private OpenPositionsList mPositions;

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

        // get shared prefs of all positions
        SharedPreferences sharedPreferencesPositions = getSharedPreferences(PortfolioActivity.PREFS_POSITIONS_FILE, Context.MODE_PRIVATE);
        String jsonPositions = sharedPreferencesPositions.getString(PortfolioActivity.POSITIONS_ARRAY,"");
        Log.i(TAG + " jsonPositions", jsonPositions);
        if (!jsonPositions.equals("")){
            mPositions = new Gson().fromJson(jsonPositions, OpenPositionsList.class);
        }
        else{
            mPositions = new OpenPositionsList();
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
        mTotalTransactionTextView = (TextView) findViewById(R.id.totalTransaction);
        mRemainingCashTextView = (TextView) findViewById(R.id.remainingCash);
        mCurrentCashTextView = (TextView) findViewById(R.id.currentCash);
        mTradeButton = (Button) findViewById(R.id.okButton);
        mCancelButton = (Button) findViewById(R.id.cancelButton);

        final boolean isPositionInMap = setSpinnerAdapter();

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


                try {
                    mNumShares = Integer.parseInt(s.toString());
                    mTotalTransaction = round((mNumShares * mStockProfile.getPrice()), 3);
                    mTotalTransactionTextView.setText("$ " + mTotalTransaction);

                    if (mAction.equals("Long") || mAction.equals("Short")) {
                        mRemainingCashTextView.setText("$ " + (mCurrentCash - mTotalTransaction));
                    }
                    else if(mAction.equals("Sell")){
                        mRemainingCashTextView.setText("$ " + (mCurrentCash + mTotalTransaction));
                    }

                    else if (mAction.equals("Buy")){
                        double remainingCash = calculateAvailableCashWhenCovering();
                        mRemainingCashTextView.setText("$ " + remainingCash);
                    }

                } catch (NumberFormatException nfe) {
                    Toast.makeText(TradeActivity.this,
                            "please input valid numbers, + and - not allowed", Toast.LENGTH_LONG).show();
                }

            }
        });

        mEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
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
                            "very tough to trade zero or less shares", Toast.LENGTH_LONG).show();
                }
                else{
                    boolean isNumSharesInputValid = true;
                    if (isPositionInMap) {
                        if (mAction.equals("Long") || mAction.equals("Short")) {
                            for (int i = 0; i < mPositions.getSize(); i++){
                                OpenPosition position = mPositions.getOpenPositions().get(i);
                                if (mStockProfile.getSymbol().equals(position.getCompanyTicker())){
                                    mAccountSummary.setAvailableCash(mCurrentCash - mTotalTransaction);
                                    position.setCompanyTicker(mStockProfile.getSymbol());
                                    position.setShares(mNumShares + position.getShares());
                                    position.setPrice(mStockProfile.getPrice()); //TODO need to fix here

                                    double pastWeight = position.getShares()/(position.getShares()+mNumShares);
                                    double pastWeightedPrice = position.getCost() * pastWeight;
                                    double nowWeight = mNumShares / (position.getShares() + mNumShares);
                                    double nowWeightedPrice = mStockProfile.getPrice() * nowWeight;
                                    double avgWeightedPrice = pastWeightedPrice + nowWeightedPrice;

                                    position.setCost(avgWeightedPrice);
                                    position.setType(mAction);
                                    mPositions.addItem(position);
                                    break;
                                }
                            }
                        }
                        else if (mAction.equals("Sell")){
                            isNumSharesInputValid = traverseMap("Sell");
                        }
                        else if (mAction.equals("Buy")){
                            isNumSharesInputValid = traverseMap("Buy");
                        }

                    }
                    else{
                        mAccountSummary.setAvailableCash(mCurrentCash - mTotalTransaction);
                        OpenPosition position = new OpenPosition();
                        position.setCompanyTicker(mStockProfile.getSymbol());
                        position.setShares(mNumShares);
                        position.setPrice(mStockProfile.getPrice());
                        position.setCost(mStockProfile.getPrice());
                        position.setType(mAction);

                        mPositions.addItem(position);

                    }

                    if (isNumSharesInputValid == false){
                        return;
                    }

                    Intent intent = new Intent(TradeActivity.this, PortfolioActivity.class);
                    intent.putExtra(POSITIONS_ARRAY, mPositions);
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
    private boolean setSpinnerAdapter() {
        boolean isPositionInMap;
        SharedPreferences sharedPreferences = getSharedPreferences(PortfolioActivity.PREFS_POSITIONS_MAP_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = sharedPreferences.getString(PortfolioActivity.POSITIONS_MAP, "");

        if (!json.equals("")){
            mPositionsMap = new Gson().fromJson(json, OpenPositionsMap.class);
        }
        else{
            mPositionsMap = new OpenPositionsMap();
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item, android.R.id.text1);
        mActionSpinner.setAdapter(spinnerAdapter);

        if (mPositionsMap.doesKeyExist(mStockProfile.getSymbol())){

            String mapType = mPositionsMap.getKey(mStockProfile.getSymbol());
            Log.i(TAG + " mapType", mapType);
            if (mapType.equals("Long")){
                spinnerAdapter.add("Long");
                spinnerAdapter.add("Sell");
            }
            else if (mapType.equals("Short")){
                spinnerAdapter.add("Short");
                spinnerAdapter.add("Buy");
            }
            isPositionInMap = true;
            spinnerAdapter.notifyDataSetChanged();
        }
        else{
            //only valid options are long and short
            spinnerAdapter.add("Long");
            spinnerAdapter.add("Short");
            isPositionInMap = false;
            spinnerAdapter.notifyDataSetChanged();
        }

        mActionSpinner.setOnItemSelectedListener(this);

        return isPositionInMap;
    }

    private boolean traverseMap(String type){
        for (int i = 0; i < mPositions.getSize(); i++) {
            OpenPosition position = mPositions.getOpenPositions().get(i);
            if (mStockProfile.getSymbol().equals(position.getCompanyTicker())){
                if(mNumShares > position.getShares()){
                    Toast.makeText(TradeActivity.this,
                            "cannot trade more than what you have", Toast.LENGTH_LONG).show();
                    return false;
                }
                if (type.equals("Sell")) {
                    mAccountSummary.setAvailableCash(mCurrentCash + mTotalTransaction);
                }
                else{
                    mAccountSummary.setAvailableCash(mCurrentCash + 2 * (position.getCost() * position.getShares()) - mTotalTransaction);
                }
                if (mNumShares < position.getShares()) {
                    position.setCompanyTicker(mStockProfile.getSymbol());
                    position.setShares(position.getShares() - mNumShares);
                    position.setPrice(mStockProfile.getPrice());
                    position.setCost(position.getCost());
                    if (type.equals("Sell")){
                        position.setType("Long");
                    }
                    else{
                        position.setType("Short");
                    }

                    //mPositions.addItem(position);
                }
                else{
                    mPositions.removeItem(position);
                }
                break;
            }
        }
        return true;
    }

    private double calculateAvailableCashWhenCovering(){
        double total = 0;
        for (int i = 0; i < mPositions.getSize(); i++) {
            OpenPosition position = mPositions.getOpenPositions().get(i);
            if (mStockProfile.getSymbol().equals(position.getCompanyTicker())) {
                total = mCurrentCash + 2 * (position.getCost() * position.getShares()) - mTotalTransaction;

            }
            break;
        }
        return total;
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
        Toast.makeText(TradeActivity.this, "selected "+mAction, Toast.LENGTH_SHORT).show();

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

    @Override
    protected void onPause() {
        super.onPause();

        Log.i(TAG, "entered on pause");
    }
}
