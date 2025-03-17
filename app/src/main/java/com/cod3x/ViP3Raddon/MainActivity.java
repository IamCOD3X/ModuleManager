package com.cod3x.ViP3Raddon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve kernel version
        String kernelVersion = System.getProperty("os.version");

        // Update the TextView with the kernel version

        TextView deviceInfoTextView;
        deviceInfoTextView = findViewById(R.id.deviceInfoTextView);

        // Retrieve device information
        String manufacturer = android.os.Build.MANUFACTURER;
        String model = android.os.Build.MODEL;
        String AndroidVersion = android.os.Build.VERSION.RELEASE;
        switch (AndroidVersion){
            case "14":
                AndroidVersion = AndroidVersion + " -> Upside Down Cake";
                break;
            case "13":
                AndroidVersion = AndroidVersion + " -> Tiramisu";
                break;
            case "12.1":
                AndroidVersion = AndroidVersion + " -> Snow Cone";
            case "12":
                AndroidVersion = AndroidVersion + " -> Snow Cone";
                break;
            case "11":
                AndroidVersion = AndroidVersion + " -> Red Velvet Cake";
                break;
            case "10":
                AndroidVersion = AndroidVersion + " -> Quince Tart";
                break;
            case "9":
                AndroidVersion = AndroidVersion + " -> Pie";
                break;
            case "8":
                AndroidVersion = AndroidVersion + " -> Oreo";
            case "8.1":
                AndroidVersion = AndroidVersion + " -> Oreo";
                break;
            case "7":
                AndroidVersion = AndroidVersion + " -> Nougat";
                break;
            case "7.1":
                AndroidVersion = AndroidVersion + " -> Nougat";
                break;
            case "7.1.1":
                AndroidVersion = AndroidVersion + " -> Nougat";
                break;
            case "7.1.2":
                AndroidVersion = AndroidVersion + " -> Nougat";
                break;
            case "6":
                AndroidVersion = AndroidVersion + " -> Marshmallow";
                break;
        }
        String device = android.os.Build.DEVICE;
        String board = android.os.Build.BOARD;
        String hardware = android.os.Build.HARDWARE;
        String bootloader = android.os.Build.BOOTLOADER;
        String codename = android.os.Build.PRODUCT;
        String kernelVersionInfo;

        // Check if kernelVersion contains "ViP3R" and set the appropriate value for kernelVersionInfo
        if (kernelVersion.contains("-ViP3R")) {
            kernelVersionInfo = "ViP3R OFFICIAL";
        }
        else if (kernelVersion.contains("-ViPER")) {
            kernelVersionInfo = "ViP3R OFFICIAL";
        } else {
            kernelVersionInfo = "NON-ViP3R";
        }

        // Create a formatted string with device information
        String deviceInfo = "Manufacturer: " + manufacturer + "\n"
                + "Model: " + model + "\n"
                + "Android Version: " + AndroidVersion + "\n"
                + "Device: " + device + "\n"
                + "Board: " + board + "\n"
                + "Hardware: " + hardware + "\n"
                + "Bootloader: " + bootloader + "\n"
                + "Codename: " + codename + "\n"
                + "Kernel Build: " + kernelVersionInfo + "\n"
                + "Kernel Version: " + kernelVersion;

        // Update the TextView with the device information
        deviceInfoTextView.setText(deviceInfo);

        RecyclerView recyclerView = findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        TextView loading = findViewById(R.id.loading);
        TextView driverListText = findViewById(R.id.driverList);
        CheckBox checkBox = findViewById(R.id.boot);
        checkBox.setChecked(new Prefs(this).getBoolean("boot"));
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Prefs prefs = new Prefs(MainActivity.this);
            prefs.putBoolean("boot", isChecked);
        });
        new Thread(() -> {
            ArrayList<String> pathList = customCommand("ls /sys/bus/usb/drivers");
            ArrayList<String> driverList = customCommand("ls /system/lib/modules/");
            ModulesAdapter adapter = new ModulesAdapter(MainActivity.this,MainActivity.this, pathList, driverList);
            runOnUiThread(() -> {
                recyclerView.setAdapter(adapter);
                loading.setVisibility(View.INVISIBLE);
                driverListText.setText("");
                for (String s : pathList){
                    driverListText.append(s + "\n");
                }
            });
        }).start();

        Button openSecondaryActivityButton = findViewById(R.id.openSecondaryActivityButton);
        openSecondaryActivityButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SecondaryActivity.class);
            startActivity(intent);
        });

    }

    public static ArrayList<String> customCommand(String command){
        ArrayList<String> result = new ArrayList<>();
        Process process = generateSuProcess();
        try {
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write((command + '\n').getBytes());
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            String line;
            while ((line = br.readLine()) != null) {
                Log.d("OUTPUT", line);
                result.add(line);}
            br.close();
            BufferedReader br2 = new BufferedReader(new InputStreamReader(stderr));
            String lineError;
            while ((lineError = br2.readLine()) != null) {
                Log.e("ERROR", lineError);
                result.add(lineError);}
            br2.close();
        } catch (IOException ignored) {}
        process.destroy();
        return result;
    }
    public static Process generateSuProcess(){
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            e.printStackTrace();
            try { process = Runtime.getRuntime().exec("echo no root");} catch (IOException ex) {ex.printStackTrace();}
        }
        return  process;
    }
    public static boolean contains(ArrayList<String> list, String item){
        for (String s : list){if (s.contains(item)){return true;}}
        return false;
    }
}