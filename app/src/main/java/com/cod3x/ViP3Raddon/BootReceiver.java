package com.cod3x.ViP3Raddon;

import static com.cod3x.ViP3Raddon.MainActivity.contains;
import static com.cod3x.ViP3Raddon.MainActivity.customCommand;


import static com.cod3x.ViP3Raddon.SecondaryActivity.contains_vd;
import static com.cod3x.ViP3Raddon.SecondaryActivity.customCommand_vd;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("BootReceiver", "onReceive: Boot completed");

        Prefs prefs = new Prefs(context);
        if (prefs.getBoolean("boot")) {
            int loadedSystem = 0;
            int loadedVendor = 0;
            int failed = 0;

            for (String module : prefs.getModules()) {
                boolean isLoaded = false;

                // Check if the module is in /system/lib/modules/
                ArrayList<String> driverList = customCommand("ls /system/lib/modules/");
                if (contains(driverList, module)) {
                    customCommand("insmod /system/lib/modules/" + module);
                    driverList = customCommand("ls /sys/bus/usb/drivers");
                    if (contains(driverList, module.replace(".ko", ""))) {
                        loadedSystem++;
                        isLoaded = true;
                    }
                }

                // Check if the module is in /vendor_dlkm/lib/modules/
                ArrayList<String> driverList_vd = customCommand_vd("ls /vendor_dlkm/lib/modules/");
                if (contains_vd(driverList_vd, module)) {
                    customCommand_vd("insmod /vendor_dlkm/lib/modules/" + module);
                    driverList_vd = customCommand_vd("ls /sys/bus/usb/drivers");
                    if (contains_vd(driverList_vd, module.replace(".ko", ""))) {
                        loadedVendor++;
                        isLoaded = true;
                    }
                }

                // Count as failed if the module could not be loaded from either path
                if (!isLoaded) {
                    failed++;
                }
            }

            // Create notification content
            String notificationContent = "Loaded " + (loadedSystem + loadedVendor) + " modules successfully\n"
                    + loadedSystem + " from /system/lib/modules/\n"
                    + loadedVendor + " from /vendor_dlkm/lib/modules/\n"
                    + failed + " modules failed to load.";

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "NE")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Modules Boot Status")
                    .setContentText("Loaded " + (loadedSystem + loadedVendor) + " modules, " + failed + " failed")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationContent))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            createNotificationChannel(context);
            notificationManager.notify(12, builder.build());
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "ViP3R Module Manager";
            String description = "Driver status notification";
            int importance = NotificationManager.IMPORTANCE_HIGH; // Use HIGH importance
            NotificationChannel channel = new NotificationChannel("NE", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}

