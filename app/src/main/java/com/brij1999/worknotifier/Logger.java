package com.brij1999.worknotifier;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Logger {
    private static Logger sInstance;
    private File logFile;
    private FileOutputStream fos;

    // private constructor to limit new instance creation
    private Logger(Context ctx) {
        try {
            logFile = new File(ctx.getFilesDir(), "work_notifier.log");
            fos = new FileOutputStream(logFile, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Logger getInstance(Context ctx) {
        if (sInstance == null) {
            sInstance = new Logger(ctx);
        }
        return sInstance;
    }

    public void log(String cls, String fn, String msg) {
        try {
            if (!BuildConfig.DEBUG) return;
            String log;
            if(msg.equals("<break/>")) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < 20; i++) {
                    stringBuilder.append("----------");
                }
                log = stringBuilder +"\n";
            } else {
                log = getTime()+"    "+cls+".|."+fn+" :    "+msg+"\n";
            }
            fos.write(log.getBytes(StandardCharsets.UTF_8));
            fos.flush();
            Log.e("WorkNotifier", log);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String get() {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            FileInputStream fis = new FileInputStream(logFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line).append('\n');
                line = reader.readLine();
            }
            reader.close();
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("TEST", "-->"+e);
            return e.toString();
        }
    }

    public void clear() {
        try {
            fos.close();
            fos = new FileOutputStream(logFile, false);
            fos.write("\n".getBytes(StandardCharsets.UTF_8));
            fos.close();
            fos = new FileOutputStream(logFile, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getTime() {
        TimeZone tz = TimeZone.getTimeZone("GMT+05:30");
        Date currentTime = Calendar.getInstance(tz).getTime();
        return currentTime.toString();
    }

    @Override
    protected void finalize() throws Throwable {
        fos.close();
        super.finalize();
    }
}
