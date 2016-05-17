package com.mine.stocksimulator.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mine.stocksimulator.R;
import com.mine.stocksimulator.data.Position;
import com.mine.stocksimulator.data.StockProfile;


public class PopupActivity extends AppCompatActivity {

    public final static String POSITION_DETAILS = "POSITION_DETAILS";
    private TextView mSymbolValuePopup;
    private TextView mPriceValuePopup;
    private TextView mTotalValuePopup;
    private EditText mNumSharesInput;
    private Button mBuyButton;
    private Button mCancelButton;

    private Position mPosition;

    private int mNumShares;
    private double mTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_popup);

        mSymbolValuePopup = (TextView) findViewById(R.id.symbolValuePopup);
        mPriceValuePopup = (TextView) findViewById(R.id.priceValuePopup);
        mTotalValuePopup = (TextView) findViewById(R.id.totalValuePopup);
        mNumSharesInput = (EditText) findViewById(R.id.numSharesInput);
        mBuyButton = (Button) findViewById(R.id.buyButtonPopup);
        mCancelButton = (Button) findViewById(R.id.cancelButtonPopup);

        DisplayMetrics dm = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout(width, (int) (height * 0.6));

        Intent intent = getIntent();

        final StockProfile stockProfile = intent.getParcelableExtra(BuyActivity.QUOTE_DETAILS);
        mSymbolValuePopup.setText(stockProfile.getSymbol());
        String priceString = stockProfile.getPrice()+"";
        mPriceValuePopup.setText(priceString);

        mNumSharesInput.addTextChangedListener(new TextWatcher() {
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
                    mTotal = BuyActivity.round((mNumShares * stockProfile.getPrice()), 3);
                }

                mTotalValuePopup.setText(mTotal + "");
            }
        });


        mBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNumSharesInput.getText().toString().length() == 0) {
                    Toast.makeText(PopupActivity.this, "Please enter number of shares", Toast.LENGTH_LONG).show();
                } else {
                    mPosition = new Position();
                    mPosition.setPrice(stockProfile.getPrice());
                    mPosition.setCost(stockProfile.getPrice());
                    mPosition.setCompanyTicker(stockProfile.getSymbol());
                    mPosition.setShares(mNumShares);
                    Intent intent = new Intent(PopupActivity.this, PortfolioActivity.class);
                    intent.putExtra(POSITION_DETAILS, mPosition);
                    startActivity(intent);
                }
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PopupActivity.this, BuyActivity.class);
                startActivity(intent);
            }
        });





    }
}
