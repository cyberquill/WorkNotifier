package com.brij1999.worknotifier;

import androidx.annotation.NonNull;

import java.net.URL;

public class AppInfo {
    private String appName;
    private String appPkg;
    private URL appIcon;

    public AppInfo() {}

    public AppInfo(String appName, String appPkg, URL appIcon) {
        super();
        this.appName = appName;
        this.appPkg = appPkg;
        this.appIcon = appIcon;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString()+"\nName:\t\t"+appName+"\nPackage:\t"+appPkg+"\nApp Icon:\t"+appIcon.toString()+"\n";
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppPkg() {
        return appPkg;
    }

    public void setAppPkg(String appPkg) {
        this.appPkg = appPkg;
    }

    public URL getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(URL appIcon) {
        this.appIcon = appIcon;
    }
}
