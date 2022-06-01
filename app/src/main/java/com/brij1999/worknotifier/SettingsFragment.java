package com.brij1999.worknotifier;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
//        Logger logger = Logger.getInstance(view.getContext());
//        Button logBtn = view.findViewById(R.id.logBtn);
//        Button clsBtn = view.findViewById(R.id.clsBtn);
//        TextView logView = view.findViewById(R.id.logView);
//        logBtn.setOnClickListener((v) -> {
//            logger.log("SettingsFragment","onCreateView","<break/>");
//            logView.setText(logger.get());
//        });
//        clsBtn.setOnClickListener((v) -> {
//            logger.clear();
//            logView.setText("");
//        });
        return view;
    }
}