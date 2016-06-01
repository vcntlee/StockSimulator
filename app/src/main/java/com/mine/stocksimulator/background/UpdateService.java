package com.mine.stocksimulator.background;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.mine.stocksimulator.data.Position;
import com.mine.stocksimulator.data.Watchlist;
import com.mine.stocksimulator.database.PositionDataSource;
import com.mine.stocksimulator.database.WatchlistDataSource;
import com.mine.stocksimulator.ui.PortfolioActivity;
import com.mine.stocksimulator.ui.TradeActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateService extends IntentService {


    private static final String TAG = UpdateService.class.getSimpleName();

    private ArrayList<Position> mPositions;
    private PositionDataSource mPositionDataSource;
    private WatchlistDataSource mWatchlistDataSource;
    private ArrayList<Watchlist> mWatchlists;

    public UpdateService(){
        super("UpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (!PortfolioActivity.isWithinDayRange()){
            cancelAlarm();
        }

        Log.i(TAG, "Service is running");

        if (isNetworkAvailable()) {
            mPositionDataSource = new PositionDataSource(this);
            mPositions = mPositionDataSource.retrieve();


            refreshAllPositions();

            mWatchlistDataSource = new WatchlistDataSource(this);
            mWatchlists = mWatchlistDataSource.retrieve();
            refreshAllWatchlists();
        }
        else{
            Log.i(TAG, "No Internet connection");
        }
    }

    private void cancelAlarm() {
        Intent intent = new Intent(getApplicationContext(), UpdateReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, UpdateReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }

    private void refreshAllWatchlists() {
        if (mWatchlists.size() > 0){
            for (int i = 0; i < mWatchlists.size(); i++){
                Watchlist watchlist = mWatchlists.get(i);
                refreshWatchlist(watchlist);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mWatchlistDataSource.update(watchlist);
            }
        }
    }

    private void refreshWatchlist(final Watchlist watchlist) {
        String completeUrl;
        String baseUrl = "http://dev.markitondemand.com/MODApis/Api/v2/Quote/json?symbol=%1$s";
        completeUrl = String.format(baseUrl, watchlist.getTicker());

        Log.i(TAG + " Watchlist", completeUrl);

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

                    //TODO check network availability

                    if (response.isSuccessful()) {
                        updateWatchlist(jsonData, watchlist);

                    } else {
                        alertUserAboutError();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Exception caught: ", e);
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException caught: ", e);
                    Toast.makeText(UpdateService.this, "oops!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateWatchlist(String jsonData, Watchlist watchlist) throws JSONException{
        JSONObject wholeQuote = new JSONObject(jsonData);

        double newPrice = wholeQuote.getDouble("LastPrice");
        double changeReturn = wholeQuote.getDouble("ChangePercent");
        double changeReturnYtd = wholeQuote.getDouble("ChangePercentYTD");

        changeReturn = TradeActivity.round(changeReturn, 2);
        changeReturnYtd = TradeActivity.round(changeReturnYtd, 2);
        watchlist.setPrice(newPrice);
        watchlist.setChange(changeReturn);
        watchlist.setChangeYtd(changeReturnYtd);
    }


    private void refreshAllPositions() {
        if (mPositions.size() > 0) {
            for (int i = 0; i < mPositions.size(); i ++) {
                Position position = mPositions.get(i);
                refreshPositions(position);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mPositionDataSource.update(position, position.getPrice(), -1, -1, position.getPercentReturn(), position.getTotalMkt(), -1);
            }
        }
    }

    private void refreshPositions(final Position position) {
        String completeUrl;
        String baseUrl = "http://dev.markitondemand.com/MODApis/Api/v2/Quote/json?symbol=%1$s";
        completeUrl = String.format(baseUrl, position.getCompanyTicker());

        Log.i(TAG + " Positions", completeUrl);

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

                    //TODO check network availability

                    if (response.isSuccessful()) {
                        updatePosition(jsonData, position);

                    } else {
                        alertUserAboutError();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Exception caught: ", e);
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException caught: ", e);
                    Toast.makeText(UpdateService.this, "oops!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }



    private void updatePosition(String jsonData, Position position) throws JSONException {
        JSONObject wholeQuote = new JSONObject(jsonData);

        position.setPrice(wholeQuote.getDouble("LastPrice"));
        position.setTotalMkt();
        double percentReturn = (position.getTotalMkt() - position.getTotalCost()) / position.getTotalCost();
        position.setPercentReturn(percentReturn);

    }

    private void alertUserAboutError() {
        //Toast.makeText(this, "response is not successful", Toast.LENGTH_LONG).show();
        Log.i(TAG, "response is not successful");

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
}
