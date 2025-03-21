package com.cod3x.ViP3Raddon;


import static com.cod3x.ViP3Raddon.MainActivity.contains;
import static com.cod3x.ViP3Raddon.MainActivity.customCommand;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;


import java.util.ArrayList;

public class ModulesAdapter extends RecyclerView.Adapter<ModulesAdapter.ViewHolder> {
    public ArrayList<String> pathList;
    public ArrayList<String> driverList;
    public Context context;
    public Activity activity;
    public Prefs prefs;

    public int id = 0;

    public ModulesAdapter(Context context2, Activity mActivity, ArrayList<String> path, ArrayList<String> driver) {
        context = context2;
        pathList = path;
        activity = mActivity;
        driverList = driver;
        prefs = new Prefs(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.module_item, parent, false);
        return new ViewHolder(v);
    }

    public void updateDriverList(ArrayList<String> driverList) {
        activity.runOnUiThread(() -> {
            TextView list = activity.findViewById(R.id.driverList);
            if (list != null) { // Prevent NullPointerException
                list.setText("");
                for (String s : driverList) {
                    list.append(s + "\n");
                }
            } else {
                Log.e("ModulesAdapter", "driverList TextView is NULL!");
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder adapter, @SuppressLint("RecyclerView") final int position) {
        adapter.name.setText(pathList.get(position).replace(".ko", ""));
        adapter.switchMaterial.setChecked(contains(driverList, pathList.get(position).replace(".ko", "")));

        adapter.switchMaterial.setOnClickListener(v -> {
            boolean checked = adapter.switchMaterial.isChecked();
            adapter.switchMaterial.setEnabled(false);

            // Determine correct path based on 'id'
            String modulePath = (id == 0)
                    ? "/system/lib/modules/" + pathList.get(position)
                    : "/vendor_dlkm/lib/modules/" + pathList.get(position);

            if (checked) {
                new Thread(() -> {
                    customCommand("insmod " + modulePath);
                    ArrayList<String> driverList = customCommand("ls /sys/bus/usb/drivers");

                    if (contains(driverList, pathList.get(position).replace(".ko", ""))) {
                        toaster("Module loaded successfully");
                        prefs.addModule(pathList.get(position));
                    } else {
                        toaster("Module failed to load");
                        activity.runOnUiThread(() -> adapter.switchMaterial.setChecked(false));
                    }

                    updateDriverList(driverList);

                }).start();
            } else {
                new Thread(() -> {
                    customCommand("rmmod " + modulePath);
                    ArrayList<String> driverList = customCommand("ls /sys/bus/usb/drivers");

                    if (!contains(driverList, pathList.get(position).replace(".ko", ""))) {
                        toaster("Module unloaded successfully");
                        prefs.removeModule(pathList.get(position));
                    } else {
                        toaster("Module failed to unload");
                        activity.runOnUiThread(() -> adapter.switchMaterial.setChecked(true));
                    }

                    updateDriverList(driverList);

                }).start();
            }
        });
    }

    @Override
    public int getItemCount() {

        return pathList.size();
    }

    public void toaster(String msg) {
        activity.runOnUiThread(() -> {
            Toast toast = Toast.makeText(context,
                    msg, Toast.LENGTH_SHORT);
            toast.show();
        });

    }



    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public SwitchMaterial switchMaterial;

        public ViewHolder(View v) {
            super(v);
            switchMaterial = v.findViewById(R.id.module_switch);
            name = v.findViewById(R.id.module_name);

        }

    }


}
