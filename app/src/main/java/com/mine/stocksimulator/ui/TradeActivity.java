package com.mine.stocksimulator.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mine.stocksimulator.R;
import com.mine.stocksimulator.data.StockProfile;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TradeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private Spinner mActionSpinner;
    private String mAction;
    private EditText mEditText;
    private TextView mPricePerShare;
    private TextView mTotalCostTextView;
    private int mNumShares;
    private double mTotalCost;


    private StockProfile mStockProfile;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade);

        // get intent from Profile
        Intent intent = getIntent();
        mStockProfile = intent.getParcelableExtra(StockProfileActivity.QUOTE_DETAILS);

        mActionSpinner = (Spinner) findViewById(R.id.actionSpinner);
        mEditText = (EditText) findViewById(R.id.numberOfSharesInput);
        mPricePerShare = (TextView) findViewById(R.id.pricePerShare);
        mTotalCostTextView = (TextView) findViewById(R.id.totalCost);

        ArrayAdapter adapter =
                ArrayAdapter.createFromResource(this,
                        R.array.tradeAction,
                        android.R.layout.simple_spinner_dropdown_item);

        mActionSpinner.setAdapter(adapter);
        mActionSpinner.setOnItemSelectedListener(this);

        // populate price and total coast

        mPricePerShare.setText("$ "+mStockProfile.getPrice());


        // populate shares

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
                if (s.length() > 0) {
                    mNumShares = Integer.parseInt(s.toString());
                    mTotalCost = round((mNumShares * mStockProfile.getPrice()), 3);
                    mTotalCostTextView.setText("$ " + mTotalCost);
                }
            }
        });

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
        Toast.makeText(this, "you selected " + mAction, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
