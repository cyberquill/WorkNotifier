package com.brij1999.worknotifier;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
    public static final String WORKNOTIFIER_HIDE_MONITOR_NTF = "WORKNOTIFIER_HIDE_MONITOR_NTF";
    public static final int SERVICE_NOTIFICATION_ID = 101;

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
        if(tinydb.getBoolean(WORKNOTIFIER_HIDE_MONITOR_NTF))    return;

        boolean ntfDetected = false;
        StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();
        for (StatusBarNotification nt : notifications) {
            if (nt.getId() == SERVICE_NOTIFICATION_ID) {
                ntfDetected = true;
                break;
            }
        }
        if(!ntfDetected && tinydb.getBoolean(WORKNOTIFIER_LISTENER_ACTIVE)) {
            Intent cIntent = new Intent(getApplicationContext(), MainActivity.class);
            cIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent cPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, cIntent, PendingIntent.FLAG_IMMUTABLE);

            Notification.Builder notification = new Notification.Builder(getApplicationContext(), MainActivity.NOTIFICATION_CHANNEL_ID)
                    .setContentTitle("WorkNotifier Enabled")
                    .setContentText("Forwarding notifications of monitored apps to watch")
                    .setSmallIcon(getApplicationInfo().icon)
                    .setContentIntent(cPendingIntent)
                    .setOngoing(true);

            mNotificationManager.notify(SERVICE_NOTIFICATION_ID, notification.build());
        }
    }

    @Override
    public void onListenerDisconnected() {
        logger.log("WorkNotifierListenerService", "onListenerDisconnected", "Listener Disconnected");
        if(tinydb.getBoolean(WORKNOTIFIER_HIDE_MONITOR_NTF))    return;

        boolean ntfDetected = false;
        StatusBarNotification[] notifications = mNotificationManager.getActiveNotifications();
        for (StatusBarNotification nt : notifications) {
            if (nt.getId() == SERVICE_NOTIFICATION_ID) {
                ntfDetected = true;
                break;
            }
        }
        if(ntfDetected && !tinydb.getBoolean(WORKNOTIFIER_LISTENER_ACTIVE)) {
            mNotificationManager.cancel(SERVICE_NOTIFICATION_ID);
        }

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
        if(!sbn.isClearable()) return;

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
