package com.mine.stocksimulator.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mine.stocksimulator.R;
import com.mine.stocksimulator.adapter.WatchlistAdapter;
import com.mine.stocksimulator.background.UpdateReceiver;
import com.mine.stocksimulator.data.Watchlist;
import com.mine.stocksimulator.database.WatchlistDataSource;

import java.util.ArrayList;

public class WatchlistActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = WatchlistActivity.class.getSimpleName();
    private ListView mListView;
    private TextView mEmpty;
    private DrawerLayout mDrawer;
    private ArrayList<Watchlist> mWatchlists;
    private WatchlistAdapter mAdapter;
    private View mHeaderView;
    private View mFooterView;
    private LinearLayout mHeaderContainer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchlist);

        mListView = (ListView) findViewById(android.R.id.list);
        mEmpty = (TextView) findViewById(R.id.emptyMessage);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mHeaderView = getLayoutInflater().inflate(R.layout.header_watchlist, null);
        mFooterView = getLayoutInflater().inflate(R.layout.footer_watchlist, null);
        mHeaderContainer = (LinearLayout) mHeaderView.findViewById(R.id.headerContainer);


        Toolbar toolbar = (Toolbar) findViewById(R.id.watchlist_toolbar);
        if (toolbar != null){
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Watchlist");
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.left_drawer);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(PortfolioActivity.WATCHLIST_OPTION).setChecked(true);


        mWatchlists = setWatchlist();
        mAdapter = new WatchlistAdapter(this, mWatchlists);

        mListView.addHeaderView(mHeaderView, null, false);
        mListView.addFooterView(mFooterView, null, false);

        mListView.setAdapter(mAdapter);



        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (isNetworkAvailable()) {
                    String ticker = mWatchlists.get(position - 1).getTicker();
                    Intent intent = new Intent(WatchlistActivity.this, StockProfileActivity.class);
                    intent.putExtra(SearchActivity.QUERY_TICKER, ticker);
                    intent.putExtra(PortfolioActivity.WHERE_IS_HOME, false);
                    startActivity(intent);
                }
                else{
                    Toast.makeText(WatchlistActivity.this, "No Internet connection", Toast.LENGTH_SHORT).show();

                }
            }
        });

        scheduleAlarm();

    }

    private void scheduleAlarm() {

        if (PortfolioActivity.isWithinDayRange()) {
            Intent intent = new Intent(getApplicationContext(), UpdateReceiver.class);
            final PendingIntent pendingIntent = PendingIntent.getBroadcast(this, UpdateReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            long firstMillis = System.currentTimeMillis();
            AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
        }
    }

    private ArrayList<Watchlist> setWatchlist(){
        ArrayList<Watchlist> watchlists;
        WatchlistDataSource dataSource = new WatchlistDataSource(this);
        watchlists = dataSource.retrieve();

        if (watchlists.size() == 0){
            mEmpty.setVisibility(View.VISIBLE);
            mHeaderContainer.setVisibility(View.INVISIBLE);
        }

        return watchlists;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_watchlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.addOption){
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra(PortfolioActivity.WHERE_IS_HOME, false);
            startActivity(intent);
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.nav_portfolio){
            Intent intent = new Intent(this, PortfolioActivity.class);
            startActivity(intent);
        }

        else if (id == R.id.nav_watchlist){

        }

        else if (id == R.id.nav_settings){
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_instructions){
            Intent intent = new Intent(this, InstructionsActivity.class);
            startActivity(intent);
        }


        mDrawer.closeDrawer(GravityCompat.START);

        return false;
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
