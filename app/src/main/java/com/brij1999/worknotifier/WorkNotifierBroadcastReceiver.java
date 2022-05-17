package com.brij1999.worknotifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WorkNotifierBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Logger logger = Logger.getInstance(context.getApplicationContext());
        logger.log("WorkNotifierBroadcastReceiver", "onReceive", "Invoked");

        Log.e("BROADCAST", "I'm alive!");

        if (intent.getAction().equals("com.brij1999.worknotifier.RESPAWN")) {
            logger.log("WorkNotifierBroadcastReceiver", "onReceive", "Action Identified as UNDEAD");
            if(new TinyDB(context).getBoolean(WorkNotifierListenerService.WORKNOTIFIER_LISTENER_ACTIVE)) {
                Intent startIntent = new Intent(context, WorkNotifierListenerService.class);
                startIntent.setAction(WorkNotifierListenerService.FOREGROUND_START_ACTION);
                context.startForegroundService(startIntent);
                logger.log("WorkNotifierBroadcastReceiver", "onReceive", "Re-launched service");
            }
        }
    }
}
