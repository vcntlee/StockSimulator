package com.mine.stocksimulator.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ListView;
import android.widget.TextView;

import com.mine.stocksimulator.R;
import com.mine.stocksimulator.adapter.WatchlistAdapter;
import com.mine.stocksimulator.background.UpdateAlarm;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchlist);

        mListView = (ListView) findViewById(android.R.id.list);
        mEmpty = (TextView) findViewById(android.R.id.empty);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

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

        mWatchlists = setWatchlist();
        mAdapter = new WatchlistAdapter(this, mWatchlists);
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(mEmpty);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String ticker = mWatchlists.get(position).getTicker();
                Intent intent = new Intent(WatchlistActivity.this, StockProfileActivity.class);
                intent.putExtra(SearchActivity.QUERY_TICKER, ticker);
                startActivity(intent);
            }
        });

        scheduleAlarm();

    }

    private void scheduleAlarm() {
        Intent intent = new Intent(getApplicationContext(), UpdateAlarm.class);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(this, UpdateAlarm.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long firstMillis = System.currentTimeMillis();
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
    }

    private ArrayList<Watchlist> setWatchlist(){
        ArrayList<Watchlist> watchlists;
        WatchlistDataSource dataSource = new WatchlistDataSource(this);
        watchlists = dataSource.retrieve();

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


        mDrawer.closeDrawer(GravityCompat.START);

        return false;
    }
}
