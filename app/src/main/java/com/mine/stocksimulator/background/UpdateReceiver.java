package com.mine.stocksimulator.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UpdateReceiver extends BroadcastReceiver {

    public static final int REQUEST_CODE = 12345;
    public static final String ACTION = "com.mine.stocksimulator.alarm";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, UpdateService.class);
        context.startService(i);
    }
}
