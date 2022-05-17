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
    Logger logger;
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
        logger = Logger.getInstance(getApplicationContext());
        tinydb = new TinyDB(getApplicationContext());
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        logger.log("WorkNotifierListenerService", "onCreate", "Hello");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        logger.log("WorkNotifierListenerService", "onStartCommand", "Inside");

        if (intent.getAction().equals(FOREGROUND_START_ACTION)) {
            logger.log("WorkNotifierListenerService", "onStartCommand", "Received Start Foreground Intent");

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
            logger.log("WorkNotifierListenerService", "onStartCommand", "Created Foreground Notification");
            ComponentName componentName = new ComponentName(getApplicationContext(), WorkNotifierListenerService.class);
            requestRebind(componentName);
        }
        else if (intent.getAction().equals(FOREGROUND_STOP_ACTION)) {
            logger.log("WorkNotifierListenerService", "onStartCommand", "Received Stop Foreground Intent");

            requestUnbind();
            stopForeground(STOP_FOREGROUND_REMOVE);
            stopSelfResult(startId);
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        logger.log("WorkNotifierListenerService", "onListenerConnected", "Listener Connected");
    }

    @Override
    public void onListenerDisconnected() {
        logger.log("WorkNotifierListenerService", "onListenerDisconnected", "Listener Disconnected");
        stopForeground(STOP_FOREGROUND_REMOVE);
        Intent intent = new Intent("com.brij1999.worknotifier.RESPAWN");
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendBroadcast(intent);
        logger.log("WorkNotifierListenerService", "onListenerDisconnected", "Sent UNDEAD broadcast");
        super.onListenerDisconnected();
    }

    @Override
    public void onDestroy() {
        logger.log("WorkNotifierListenerService", "onDestroy", "Service Killed");
        super.onDestroy();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        logger.log("WorkNotifierListenerService", "onNotificationPosted", "Invoked ("+packageName+")");
        if(!tinydb.getBoolean(WORKNOTIFIER_LISTENER_ACTIVE)) return;

        Notification ntf = sbn.getNotification();
        if(packageName.equals(MainActivity.PACKAGE_NAME))  return;
        if(!appManager.containsPkg(packageName))  return;

        Notification.Builder reBuilder = Notification.Builder.recoverBuilder(getApplicationContext(), ntf).setChannelId(MainActivity.NOTIFICATION_CHANNEL_ID).setGroup(packageName);
        mNotificationManager.notify(getModId(sbn), reBuilder.build());
        cancelNotification(sbn.getKey());
        logger.log("WorkNotifierListenerService", "onNotificationPosted", "Created Notification for "+packageName);
    }

    public int getModId(StatusBarNotification sbn) {
        return sbn.getPackageName().hashCode()+sbn.getId();
    }
}
