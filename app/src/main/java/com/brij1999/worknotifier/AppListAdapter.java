package com.brij1999.worknotifier;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageButton;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.AppListViewHolder> {
    Context ctx;
    AppManager appManager;
    ExecutorService executorService = Executors.newCachedThreadPool();

    public AppListAdapter(Context context) {
        // Setting up SharedPreferences to communicate the list of apps to be monitored
        ctx = context;
        appManager = AppManager.getInstance(ctx);
    }

    @NonNull
    @Override
    public AppListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflator = LayoutInflater.from(parent.getContext());
        View view = inflator.inflate(R.layout.applist_item, parent, false);
        return new AppListViewHolder(view).linkAdapter(this);
    }

    @Override
    public void onBindViewHolder(@NonNull AppListViewHolder holder, int position) {
        AppInfo app = appManager.getApp(position);
        holder.appName.setText(app.getAppName());
        holder.appPkg.setText(app.getAppPkg());
        executorService.execute(() -> {
            try {
                Bitmap image = BitmapFactory.decodeStream(app.getAppIcon().openConnection().getInputStream());
                new Handler(Looper.getMainLooper()).post(() -> holder.appIcon.setImageBitmap(image));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return appManager.getApps().size();
    }

    public class AppListViewHolder extends RecyclerView.ViewHolder{
        private final TextView appName, appPkg;
        private final ImageView appIcon;
        private final ImageButton appRemoveBtn;
        private AppListAdapter adapter;

        public AppListViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.appIcon);
            appName = itemView.findViewById(R.id.appName);
            appPkg = itemView.findViewById(R.id.appPkg);
            appRemoveBtn = itemView.findViewById(R.id.appRemoveBtn);
            appRemoveBtn.setOnClickListener(view -> {
                AppInfo app = appManager.removeApp(getAdapterPosition());
                adapter.notifyItemRemoved(getAdapterPosition());
                Toast.makeText(adapter.ctx, app.getAppName()+" removed from monitoring", Toast.LENGTH_SHORT).show();
            });
        }

        public AppListViewHolder linkAdapter(AppListAdapter adapter) {
            this.adapter = adapter;
            return this;
        }
    }

}
