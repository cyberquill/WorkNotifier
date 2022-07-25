package com.brij1999.worknotifier;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HomeFragment extends Fragment {

    public AppListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Setting up appList recyclerview
        RecyclerView appList = view.findViewById(R.id.appList);
        appList.setLayoutManager(new LinearLayoutManager(view.getContext()));
        adapter = new AppListAdapter(view.getContext());
        appList.setAdapter(adapter);

        // Setting up URL accepting mechanism
        EditText inputURL = view.findViewById(R.id.appURL);
        inputURL.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String url_input = inputURL.getText().toString();
                ((MainActivity) requireActivity()).addURL(url_input);
                inputURL.getText().clear();
                handled = true;
            }
            return handled;
        });
        ImageButton submitBtn = view.findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(v -> {
            String url_input = inputURL.getText().toString();
            ((MainActivity) requireActivity()).addURL(url_input);
            inputURL.getText().clear();
        });

        // Setting up support-me button
        Button supportBtn = view.findViewById(R.id.supportBtn);
        supportBtn.setOnClickListener(v -> {
            Uri uri = Uri.parse("https://www.buymeacoffee.com/brij");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });

        return view;
    }
}