package com.hhmusic.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.hhmusic.activity.PlayingActivity;

public class LaunchNowPlayingReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {


        Intent activityIntent = new Intent(context.getApplicationContext(), PlayingActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.getApplicationContext().startActivity(activityIntent);
        Intent intent1 = new Intent();
        intent1.setComponent(new ComponentName("com.hhmusic", "com.hhmusic.activity.PlayingActivity.class"));
        context.sendBroadcast(intent1);


    }

}
