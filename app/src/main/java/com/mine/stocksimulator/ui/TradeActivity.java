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
import android.widget.LinearLayout;
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
    public static final String ACCOUNT_REMAINING_CASH = "ACCOUNT_REMAINING_CASH";
    public static final String POSITIONS_ARRAY = "POSITIONS_ARRAY";


    private Spinner mActionSpinner;
    private String mAction;
    private EditText mEditText;
    private LinearLayout mAvailableSharesContainer;
    private TextView mAvailableShares;
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
    private OpenPosition mCachedPosition;
    private OpenPositionsMap mPositionsMap;
    private OpenPositionsList mPositions;

    private double mRemainingCash;

    private StockProfile mStockProfile;

    /* TODO need to include number of shares to close out when selling or buying --> DONE
    TODO need to account for profit per position
    TODO need to figure out how to change textviews when another option is selected
    TODO for PortfolioAcitivity, need to include Positions Value, Remaining Cash, Total Value, and percentage
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade);

        // get intent from Profile
        Intent intent = getIntent();
        if (intent == null){
            Log.i(TAG, "intent is null");
        }
        mStockProfile = intent.getParcelableExtra(StockProfileActivity.QUOTE_DETAILS);
        if (mStockProfile == null){
            Log.i(TAG, "mStockProfile is null");
        }
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
        mAvailableSharesContainer = (LinearLayout) findViewById(R.id.invisibleShares);
        mAvailableShares = (TextView) findViewById(R.id.availableShares);
        mPricePerShareTextView = (TextView) findViewById(R.id.pricePerShare);
        mTotalTransactionTextView = (TextView) findViewById(R.id.totalTransaction);
        mRemainingCashTextView = (TextView) findViewById(R.id.remainingCash);
        mCurrentCashTextView = (TextView) findViewById(R.id.currentCash);
        mTradeButton = (Button) findViewById(R.id.okButton);
        mCancelButton = (Button) findViewById(R.id.cancelButton);

        // setSpinnerAdapter also determines if position is in map
        final boolean isPositionInMap = setSpinnerAdapter();
        if (isPositionInMap){
            mCachedPosition = iterateAllPositions();
            Log.i(TAG+ " shares", mCachedPosition.getShares()+"");

        }

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

                            mRemainingCash = mCurrentCash - mTotalTransaction;
                            mCachedPosition.setCompanyTicker(mStockProfile.getSymbol());
                            mCachedPosition.setShares(mNumShares + mCachedPosition.getShares());
                            mCachedPosition.setPrice(mStockProfile.getPrice()); //TODO need to fix here

                            double pastWeight = mCachedPosition.getShares()/(mCachedPosition.getShares()+mNumShares);
                            double pastWeightedPrice = mCachedPosition.getCost() * pastWeight;
                            double nowWeight = mNumShares / (mCachedPosition.getShares() + mNumShares);
                            double nowWeightedPrice = mStockProfile.getPrice() * nowWeight;
                            double avgWeightedPrice = pastWeightedPrice + nowWeightedPrice;

                            mCachedPosition.setCost(avgWeightedPrice);
                            mCachedPosition.setType(mAction);

                        }
                        else if (mAction.equals("Sell")){
                            isNumSharesInputValid = setCachedPosition("Sell");
                        }
                        else if (mAction.equals("Buy")){
                            isNumSharesInputValid = setCachedPosition("Buy");
                        }

                    }
                    else{
                        mRemainingCash = mCurrentCash - mTotalTransaction;
                        OpenPosition position = new OpenPosition();
                        position.setCompanyTicker(mStockProfile.getSymbol());
                        position.setShares(mNumShares);
                        position.setPrice(mStockProfile.getPrice());
                        position.setCost(mStockProfile.getPrice());
                        position.setType(mAction);

                        mPositions.addItem(position);

                    }

                    if (!isNumSharesInputValid){
                        return;
                    }

                    Intent intent = new Intent(TradeActivity.this, PortfolioActivity.class);
                    intent.putExtra(POSITIONS_ARRAY, mPositions);
                    intent.putExtra(ACCOUNT_REMAINING_CASH, mRemainingCash);
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
        //SharedPreferences.Editor editor = sharedPreferences.edit();
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

    private boolean setCachedPosition(String type){

        if(mNumShares > mCachedPosition.getShares()){
            Toast.makeText(TradeActivity.this,
                    "cannot trade more than what you have", Toast.LENGTH_LONG).show();
            return false;
        }
        if (type.equals("Sell")) {
            mRemainingCash = mCurrentCash + mTotalTransaction;
        }
        else{
            mRemainingCash = mCurrentCash + 2 * (mCachedPosition.getCost() * mCachedPosition.getShares()) - mTotalTransaction;
        }
        if (mNumShares < mCachedPosition.getShares()) {
            mCachedPosition.setCompanyTicker(mStockProfile.getSymbol());
            mCachedPosition.setShares(mCachedPosition.getShares() - mNumShares);
            mCachedPosition.setPrice(mStockProfile.getPrice());
            mCachedPosition.setCost(mCachedPosition.getCost());
            if (type.equals("Sell")){
                mCachedPosition.setType("Long");
            }
            else{
                mCachedPosition.setType("Short");
            }
        }
        else{
            mPositions.removeItem(mCachedPosition);
        }
        return true;
    }

    private double calculateAvailableCashWhenCovering(){

        double total =  mCurrentCash + 2 * (mCachedPosition.getCost() * mCachedPosition.getShares()) - mTotalTransaction;
        Log.i(TAG+" total", total+"");
        return total;
    }



    public static double round(double value, int places) {
        //if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public OpenPosition iterateAllPositions(){
        OpenPosition position = new OpenPosition();
        for (int i = 0; i < mPositions.getSize(); i++) {
            position = mPositions.getOpenPositions().get(i);
            if (mStockProfile.getSymbol().equals(position.getCompanyTicker())) {
                return position;
            }
        }
        // should never reach here
        return position;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        TextView actionText = (TextView) view;
        mAction = actionText.getText().toString();
        Log.i(TAG, mAction);

        if (mAction.equals("Sell") || mAction.equals("Buy")){
            mAvailableSharesContainer.setVisibility(View.VISIBLE);
            mAvailableShares.setText(mCachedPosition.getShares() + "");
        }
        else{
            mAvailableSharesContainer.setVisibility(View.INVISIBLE);
        }

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
