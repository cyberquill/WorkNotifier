package com.brij1999.worknotifier;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    AppManager appManager;
    Logger logger;
    AlertDialog enableNotificationListenerAlertDialog;
    AlertDialog enableBatteryOptimizationAlertDialog;

    BottomAppBar btmAppBar;
    HomeFragment homeFragment = new HomeFragment();
    SettingsFragment settingsFragment = new SettingsFragment();

    private TinyDB tinydb;
    public static String PACKAGE_NAME;
    public static final String NOTIFICATION_CHANNEL_ID = "WorkNotifierNotification";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appManager = AppManager.getInstance(getApplicationContext());
        logger = Logger.getInstance(getApplicationContext());
        tinydb = new TinyDB(getApplicationContext());
        PACKAGE_NAME = getApplicationContext().getPackageName();

        // Fragments Setup
        getSupportFragmentManager().beginTransaction().replace(R.id.frameContainer, homeFragment).commit();
        btmAppBar = findViewById(R.id.bottomAppBar);
        btmAppBar.setNavigationOnClickListener((item) -> getSupportFragmentManager().beginTransaction().replace(R.id.frameContainer, homeFragment).commit());
        btmAppBar.setOnMenuItemClickListener((item) -> {
            switch (item.getItemId()) {
                case R.id.home:
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameContainer, homeFragment).commit();
                    return true;
                case R.id.settings:
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameContainer, settingsFragment).commit();
                    return true;
            }
            return false;
        });

        IntentFilter intentFilter = new IntentFilter("com.brij1999.worknotifier.RESPAWN");
        WorkNotifierBroadcastReceiver wnReciever = new WorkNotifierBroadcastReceiver();
        registerReceiver(wnReciever, intentFilter);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        if (intent.getType() != null && Intent.ACTION_SEND.equals(intent.getAction())) {
            if(intent.getType().contains("text")) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                addURL(sharedText);
            }
        }

        // Request Exemption from Battery Optimization
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (!pm.isIgnoringBatteryOptimizations(PACKAGE_NAME)) {
            enableBatteryOptimizationAlertDialog = buildBatteryOptimizationAlertDialog();
            enableBatteryOptimizationAlertDialog.show();
        }

        // If the user did not turn the notification listener service on we prompt him to do so
        if(!isNotificationServiceEnabled()){
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        }

        FloatingActionButton serviceBtn = findViewById(R.id.serviceBtn);
        if(!tinydb.getBoolean(WorkNotifierListenerService.WORKNOTIFIER_LISTENER_ACTIVE)) {
            serviceBtn.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.colorOn)));
        } else {
            serviceBtn.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.colorOff)));
        }
        serviceBtn.setOnClickListener((v) -> {
            if(!tinydb.getBoolean(WorkNotifierListenerService.WORKNOTIFIER_LISTENER_ACTIVE)) {
                //start
                tinydb.putBoolean(WorkNotifierListenerService.WORKNOTIFIER_LISTENER_ACTIVE, true);
                serviceBtn.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.colorOff)));
                Intent startIntent = new Intent(MainActivity.this, WorkNotifierListenerService.class);
                startIntent.setAction(WorkNotifierListenerService.FOREGROUND_START_ACTION);
                startForegroundService(startIntent);
                logger.log("MainActivity", "onCreate-onClick", "Service Started");
            } else {
                //stop
                tinydb.putBoolean(WorkNotifierListenerService.WORKNOTIFIER_LISTENER_ACTIVE, false);
                serviceBtn.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.colorOn)));
                Intent stopIntent = new Intent(MainActivity.this, WorkNotifierListenerService.class);
                stopIntent.setAction(WorkNotifierListenerService.FOREGROUND_STOP_ACTION);
                startService(stopIntent);
                logger.log("MainActivity", "onCreate-onClick", "Service Stopped");
            }
        });

        // Create Notification channel to show notifications
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "WorkNotifier Notification", NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription("All Work Profile Notifications Captured");
        mNotificationManager.createNotificationChannel(channel);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (enableNotificationListenerAlertDialog != null) {
            enableNotificationListenerAlertDialog.dismiss();
            enableNotificationListenerAlertDialog = null;
        }
    }

    public void addURL(String input) {
        try {
            URL url = new URL(input);
            Uri uri = Uri.parse(url.toString());
            String pkgName = uri.getQueryParameter("id");

            if(!url.getHost().equals("play.google.com")) {
                Toast.makeText(MainActivity.this, "Not a play-store URL", Toast.LENGTH_SHORT).show();
            } else if(appManager.containsPkg(pkgName)) {
                Toast.makeText(MainActivity.this, "App is already being monitored", Toast.LENGTH_SHORT).show();
            } else {
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                AppInfo app = new AppInfo();
                app.setAppPkg(pkgName);

                executorService.execute(() -> {
                    URL iconURL;
                    String appName;
                    try {
                        Document doc = Jsoup.connect("https://play.google.com/store/apps/details?id="+pkgName).get();
                        URL image = new URL(doc.select("img[alt=\"Cover art\"]").first().attr("src").replaceAll("=s180-rw", "=s360-rw"));
                        appName = doc.select("[itemprop=\"name\"] span").first().text();
                        iconURL = image;
                    } catch (NullPointerException | IOException e) {
                        iconURL = null;
                        appName = pkgName;
                        e.printStackTrace();
                    }
                    app.setAppName(appName);
                    app.setAppIcon(iconURL);
                    appManager.addApp(app);
                    logger.log("MainActivity", "addURL", "App Added: "+app);

                    runOnUiThread(() -> {
                        homeFragment.adapter.notifyItemInserted(appManager.size()-1);
                        Toast.makeText(MainActivity.this, app.getAppName()+" is now being monitored", Toast.LENGTH_SHORT).show();
                    });
                });
            }
        } catch (MalformedURLException e) {
            Toast.makeText(MainActivity.this, "Not a valid URL", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }


    /**
     * Is Notification Service Enabled.
     * Verifies if the notification listener service is enabled.
     * Got it from: https://github.com/kpbird/NotificationListenerService-Example/blob/master/NLSExample/src/main/java/com/kpbird/nlsexample/NLService.java
     * @return True if enabled, false otherwise.
     */
    private boolean isNotificationServiceEnabled(){
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name : names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Build Notification Listener Alert Dialog.
     * Builds the alert dialog that pops up if the user has not turned
     * the Notification Listener Service on yet.
     * @return An alert dialog which leads to the notification enabling screen
     */
    private AlertDialog buildNotificationServiceAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                .setTitle(R.string.notification_listener_dialogue_title)
                .setMessage(R.string.notification_listener_dialogue_explanation)
                .setCancelable(false)
                .setPositiveButton(R.string.yes,
                    (dialog, id) -> startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS)))
                .setNegativeButton(R.string.no,
                    (dialog, id) -> finishAndRemoveTask());
        return(alertDialogBuilder.create());
    }

    /**
     * Build Notification Listener Alert Dialog.
     * Builds the alert dialog that pops up if the user has not turned
     * the Notification Listener Service on yet.
     * @return An alert dialog which leads to the notification enabling screen
     */
    private AlertDialog buildBatteryOptimizationAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                .setTitle(R.string.battery_optimization_dialogue_title)
                .setMessage(R.string.battery_optimization_dialogue_explanation)
                .setPositiveButton(R.string.yes,
                    (dialog, id) -> startActivity(new Intent().setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)))
                .setNegativeButton(R.string.no,
                    (dialog, id) -> {});
        return(alertDialogBuilder.create());
    }
}