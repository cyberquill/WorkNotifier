package com.brij1999.worknotifier;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class WorkNotifierListenerService extends NotificationListenerService {

    AppManager appManager;
    Logger logger;
    NotificationManager mNotificationManager;
    private TinyDB tinydb;

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
