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
import com.mine.stocksimulator.data.Position;
import com.mine.stocksimulator.data.StockProfile;
import com.mine.stocksimulator.database.PositionDataSource;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TradeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private static final String TAG = TradeActivity.class.getSimpleName();
    public static final String ACCOUNT_REMAINING_CASH = "ACCOUNT_REMAINING_CASH";

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
    private Position mPosition;

    private boolean mCreateNeeded;
    private double mRemainingCash;
    private StockProfile mStockProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade);

        // get intent from Profile
        Intent intent = getIntent();

        if (intent != null) {
            mStockProfile = intent.getParcelableExtra(StockProfileActivity.QUOTE_DETAILS);
        }
        else{
            Toast.makeText(TradeActivity.this, "seems to be a connection issue", Toast.LENGTH_LONG).show();
        }


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
        mAvailableSharesContainer = (LinearLayout) findViewById(R.id.invisibleShares);
        mAvailableShares = (TextView) findViewById(R.id.availableShares);
        mPricePerShareTextView = (TextView) findViewById(R.id.pricePerShare);
        mTotalTransactionTextView = (TextView) findViewById(R.id.totalTransaction);
        mRemainingCashTextView = (TextView) findViewById(R.id.remainingCash);
        mCurrentCashTextView = (TextView) findViewById(R.id.currentCash);
        mTradeButton = (Button) findViewById(R.id.okButton);
        mCancelButton = (Button) findViewById(R.id.cancelButton);

        // Position is set here
        PositionDataSource dataSource = new PositionDataSource(this);
        mPosition = dataSource.retrieveOne(mStockProfile.getSymbol());

        // Spinner is set here
        setSpinnerAdapter();


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
                    mTotalTransaction = round((mNumShares * mStockProfile.getPrice()), 2);
                    mTotalTransactionTextView.setText("$ " + mTotalTransaction);

                    if (mAction.equals("Long") || mAction.equals("Short")) {
                        mRemainingCashTextView.setText("$ " + round(mCurrentCash - mTotalTransaction,2));
                    }
                    else if(mAction.equals("Sell")){
                        mRemainingCashTextView.setText("$ " + round(mCurrentCash + mTotalTransaction,2));
                    }

                    else if (mAction.equals("Buy")){
                        double remainingCash = calculateAvailableCashWhenCovering();
                        mRemainingCashTextView.setText("$ " + remainingCash);
                    }

                } catch (NumberFormatException nfe) {
                    Toast.makeText(TradeActivity.this,
                            "please input valid numbers, + and - not allowed", Toast.LENGTH_SHORT).show();
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


        mTradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mNumShares <= 0){
                    Toast.makeText(TradeActivity.this,
                            "very tough to trade zero or less shares", Toast.LENGTH_LONG).show();
                }
                else{

                    if (mPosition == null){
                        mRemainingCash = mCurrentCash - mTotalTransaction;
                        if (mRemainingCash > 0) {
                            mPosition = new Position();
                            mPosition.setCompanyTicker(mStockProfile.getSymbol());
                            mPosition.setPrice(mStockProfile.getPrice());
                            mPosition.setCost(mStockProfile.getPrice());
                            mPosition.setShares(mNumShares);
                            mPosition.setType(mAction);
                            mPosition.setPercentReturn(0);
                            mPosition.setTotalCost(mTotalTransaction);
                            mPosition.setTotalMkt();

                        }
                        else{
                            Toast.makeText(TradeActivity.this,
                                    "not enough buying power", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    else{
                        if (mAction.equals("Long") || mAction.equals("Short")) {
                            mRemainingCash = mCurrentCash - mTotalTransaction;
                            if (mRemainingCash > 0) {
                                mPosition.setShares(mNumShares + mPosition.getShares());
                                mPosition.setPrice(mStockProfile.getPrice());
                                mPosition.setWeightedCost(mNumShares, mStockProfile.getPrice());
                                // setTotalCost = totalcost += newCost
                                mPosition.setTotalCost(mTotalTransaction);
                                mPosition.setTotalMkt();

                                Log.i(TAG+" weighted", mPosition.getCost()+"");
                            }
                            else{
                                Toast.makeText(TradeActivity.this,
                                        "not enough buying power", Toast.LENGTH_LONG).show();
                                return;
                            }

                        }
                        else if (mAction.equals("Sell") || mAction.equals("Buy")){
                            if(mNumShares > mPosition.getShares()){
                                Toast.makeText(TradeActivity.this,
                                        "cannot trade more than what you have", Toast.LENGTH_LONG).show();
                                return;
                            }
                            if(mAction.equals("Sell"))
                                mRemainingCash = mCurrentCash + mTotalTransaction;
                            else
                                mRemainingCash = mCurrentCash + 2 * (mPosition.getCost() * mPosition.getShares()) - mTotalTransaction;
                            if (mNumShares < mPosition.getShares()) {
                                mPosition.setShares(mPosition.getShares() - mNumShares);
                                mPosition.setPrice(mStockProfile.getPrice());
                                mPosition.setTotalCost((-1) * mTotalTransaction);
                                mPosition.setTotalMkt();

                            }
                            else{
                                PositionDataSource dataSource = new PositionDataSource(TradeActivity.this);
                                dataSource.delete(mPosition.getId());
                            }
                        }


                    }


                    Log.i(TAG+" positionCost", mPosition.getCost()+"");
                    savePositionToDatabase();

                    Intent intent = new Intent(TradeActivity.this, PortfolioActivity.class);
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

    private void savePositionToDatabase(){
        Log.i(TAG+" mPosition", mPosition.getPrice()+"");
        Log.i(TAG+" mPosition", mPosition.getType());
        Log.i(TAG+" mPosition", mPosition.getShares()+"");
        Log.i(TAG+" mPosition", mPosition.getTotalMkt()+"");
        PositionDataSource dataSource = new PositionDataSource(this);
        // if position in db:
        if (!mCreateNeeded) {
            dataSource.update(mPosition,
                    mPosition.getPrice(),
                    mPosition.getCost(),
                    mPosition.getShares(),
                    -1,
                    mPosition.getTotalMkt(),
                    mPosition.getTotalCost());
        }
        else {
            dataSource.create(mPosition);
        }
    }

    // check to see if there are duplicates and if actions match in the portfolio
    private void setSpinnerAdapter() {

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item, android.R.id.text1);
        mActionSpinner.setAdapter(spinnerAdapter);


        if (mPosition != null){
            mCreateNeeded = false;

            if (mPosition.getType().equals("Long")){
                spinnerAdapter.add("Long");
                spinnerAdapter.add("Sell");
            }
            else if (mPosition.getType().equals("Short")){
                spinnerAdapter.add("Short");
                spinnerAdapter.add("Buy");
            }
            spinnerAdapter.notifyDataSetChanged();
        }
        else{
            mCreateNeeded = true;
            //only valid options are long and short
            spinnerAdapter.add("Long");
            spinnerAdapter.add("Short");
            spinnerAdapter.notifyDataSetChanged();
        }

        mActionSpinner.setOnItemSelectedListener(this);
    }



    private double calculateAvailableCashWhenCovering(){

        double total =  mCurrentCash + 2 * (mPosition.getCost() * mPosition.getShares()) - mTotalTransaction;
        Log.i(TAG+" total", total+"");
        return round(total,2);
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
        Log.i(TAG, mAction);

        if (mAction.equals("Sell") || mAction.equals("Buy")){
            mAvailableSharesContainer.setVisibility(View.VISIBLE);
            mAvailableShares.setText(mPosition.getShares() + "");
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
