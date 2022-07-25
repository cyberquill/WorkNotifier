package com.brij1999.worknotifier;

import static com.brij1999.worknotifier.WorkNotifierListenerService.SERVICE_NOTIFICATION_ID;
import static com.brij1999.worknotifier.WorkNotifierListenerService.WORKNOTIFIER_HIDE_MONITOR_NTF;
import static com.brij1999.worknotifier.WorkNotifierListenerService.WORKNOTIFIER_LISTENER_ACTIVE;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.brij1999.worknotifier.BuildConfig;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsFragment extends Fragment {

    private TinyDB tinydb;
    NotificationManager mNotificationManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        tinydb = new TinyDB(view.getContext());
        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;

        TextView appVersion = view.findViewById(R.id.appVersion);
        appVersion.setText(versionName+"  <"+versionCode+"/>");
        SwitchMaterial activeNtfSwitch = view.findViewById(R.id.activeNtfSwitch);
        activeNtfSwitch.setChecked(tinydb.getBoolean(WORKNOTIFIER_HIDE_MONITOR_NTF));
        activeNtfSwitch.setOnClickListener((v) -> {
            boolean val = !tinydb.getBoolean(WORKNOTIFIER_HIDE_MONITOR_NTF);
            tinydb.putBoolean(WORKNOTIFIER_HIDE_MONITOR_NTF, val);
            activeNtfSwitch.setChecked(val);

            if(tinydb.getBoolean(WORKNOTIFIER_HIDE_MONITOR_NTF)) {
                mNotificationManager.cancel(SERVICE_NOTIFICATION_ID);
            }
        });
        return view;
    }
}