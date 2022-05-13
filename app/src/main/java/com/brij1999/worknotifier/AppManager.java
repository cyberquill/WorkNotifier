package com.brij1999.worknotifier;

import android.content.Context;

import java.util.ArrayList;

public class AppManager {
    private static AppManager sInstance;
    private final TinyDB tinydb;
    private final String MONITORED_APPS_KEY;
    private final ArrayList<AppInfo> monitoredApps = new ArrayList<>();

    // private constructor to limit new instance creation
    private AppManager(Context ctx) {
        tinydb = new TinyDB(ctx);
        MONITORED_APPS_KEY = ctx.getPackageName()+"MONITORED_APPS";

        ArrayList<Object> monitoredAppObjects = tinydb.getListObject(MONITORED_APPS_KEY, AppInfo.class);
        for(Object obj : monitoredAppObjects){
            monitoredApps.add((AppInfo) obj);
        }
    }

    public static AppManager getInstance(Context ctx) {
        if (sInstance == null) {
            sInstance = new AppManager(ctx);
        }
        return sInstance;
    }

    public ArrayList<AppInfo> getApps() {
        return monitoredApps;
    }

    public int size() {
        return monitoredApps.size();
    }

    public AppInfo getApp(int index) {
        return monitoredApps.get(index);
    }

    public AppInfo getApp(String pkg) {
        for(AppInfo app : monitoredApps) {
            if(app.getAppPkg().equals(pkg)) {
                return app;
            }
        }
        return null;
    }

    public boolean containsPkg(String pkg) {
        for(AppInfo app : monitoredApps) {
            if(app.getAppPkg().equals(pkg)) {
                return true;
            }
        }
        return false;
    }

    public void addApp(AppInfo app) {
        monitoredApps.add(app);
        ArrayList<Object> monitoredAppObjects = new ArrayList<>(monitoredApps);
        tinydb.putListObject(MONITORED_APPS_KEY, monitoredAppObjects);
    }

    public AppInfo removeApp(int index) {
        AppInfo app = monitoredApps.remove(index);
        ArrayList<Object> monitoredAppObjects = new ArrayList<>(monitoredApps);
        tinydb.putListObject(MONITORED_APPS_KEY, monitoredAppObjects);
        return app;
    }
}
