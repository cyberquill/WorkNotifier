package com.brij1999.worknotifier;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class WorkNotifierListenerService extends NotificationListenerService {

    AppManager appManager;
    NotificationManager mNotificationManager;
    private TinyDB tinydb;

    public static final int FOREGROUND_NOTIFICATION_ID = 101;
    public static final String FOREGROUND_START_ACTION = "START_FOREGROUND_ACTION";
    public static final String FOREGROUND_STOP_ACTION = "STOP_FOREGROUND_ACTION";
    public static final String WORKNOTIFIER_LISTENER_ACTIVE = "WORKNOTIFIER_LISTENER_ACTIVE";

    @Override
    public void onCreate() {
        super.onCreate();
        appManager = AppManager.getInstance(getApplicationContext());
        tinydb = new TinyDB(getApplicationContext());
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (BuildConfig.DEBUG)    Log.e("WorkNotifierListenerService", "Hello");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (BuildConfig.DEBUG)    Log.i("onStartCommand", "Inside");
        if (intent.getAction().equals(FOREGROUND_START_ACTION)) {
            if (BuildConfig.DEBUG)    Log.i("onStartCommand", "Received Start Foreground Intent ");

            Intent cIntent = new Intent(getApplicationContext(), MainActivity.class);
            cIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent cPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, cIntent, PendingIntent.FLAG_IMMUTABLE);

            Intent stopSelf = new Intent(this, WorkNotifierListenerService.class).setAction(FOREGROUND_STOP_ACTION);
            PendingIntent pStopSelf = PendingIntent.getService(this, 0, stopSelf, PendingIntent.FLAG_IMMUTABLE);
            Notification.Action stopAction = new Notification.Action.Builder(Icon.createWithResource(getApplicationContext(), getApplicationInfo().icon),"Stop",pStopSelf).build();

            Notification.Builder notification = new Notification.Builder(getApplicationContext(), MainActivity.NOTIFICATION_CHANNEL_ID)
                    .setContentTitle("WorkNotifier Enabled")
                    .setContentText("Forwarding notifications of monitored apps to watch")
                    .setSmallIcon(getApplicationInfo().icon)
                    .setContentIntent(cPendingIntent)
                    .addAction(stopAction);

            startForeground(FOREGROUND_NOTIFICATION_ID, notification.build());
            if (BuildConfig.DEBUG)    Log.e("onStartCommand", "Created Foreground Notification");
            ComponentName componentName = new ComponentName(getApplicationContext(), WorkNotifierListenerService.class);
            requestRebind(componentName);
            tinydb.putBoolean(WORKNOTIFIER_LISTENER_ACTIVE, true);
        }
        else if (intent.getAction().equals(FOREGROUND_STOP_ACTION)) {
            if (BuildConfig.DEBUG)    Log.i("onStartCommand", "Received Stop Foreground Intent");

            tinydb.putBoolean(WORKNOTIFIER_LISTENER_ACTIVE, false);
            requestUnbind();
            stopForeground(true);
            stopSelfResult(startId);
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        if (BuildConfig.DEBUG)    Log.e("onListenerConnected", "Listener Connected");
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        if (BuildConfig.DEBUG)    Log.e("onListenerDisconnected", "Listener Disconnected");
    }

    @Override
    public void onDestroy() {
        if (BuildConfig.DEBUG)    Log.e("onDestroy", "Service Killed");
        tinydb.putBoolean(WORKNOTIFIER_LISTENER_ACTIVE, false);
        super.onDestroy();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (BuildConfig.DEBUG)    Log.i("onNotificationPosted", "Inside");
        if(!tinydb.getBoolean(WORKNOTIFIER_LISTENER_ACTIVE)) return;
        String packageName = sbn.getPackageName();
        Notification ntf = sbn.getNotification();
        if(packageName.equals(MainActivity.PACKAGE_NAME))  return;
        if(!appManager.containsPkg(packageName))  return;

        Notification.Builder reBuilder = Notification.Builder.recoverBuilder(getApplicationContext(), ntf).setChannelId(MainActivity.NOTIFICATION_CHANNEL_ID).setGroup(packageName);
        mNotificationManager.notify(getModId(sbn), reBuilder.build());
        cancelNotification(sbn.getKey());
    }

    public int getModId(StatusBarNotification sbn) {
        return sbn.getPackageName().hashCode()+sbn.getId();
    }
}
