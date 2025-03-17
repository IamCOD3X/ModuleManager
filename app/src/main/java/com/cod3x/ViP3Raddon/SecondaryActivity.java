package com.cod3x.ViP3Raddon;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class SecondaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secondary);

        RecyclerView recyclerView = findViewById(R.id.list_vd);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        TextView loading_vd = findViewById(R.id.loading_vd);
        TextView driverListText_vd = findViewById(R.id.driverList_vd);
        CheckBox checkBox_vd = findViewById(R.id.boot_vd);
        checkBox_vd.setChecked(new Prefs(this).getBoolean("boot_vd"));
        checkBox_vd.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Prefs prefs = new Prefs(SecondaryActivity.this);
            prefs.putBoolean("boot_vd", isChecked);
        });

        new Thread(() -> {
            ArrayList<String> driverList_vd = customCommand("ls /sys/bus/usb/drivers");
            ArrayList<String> pathList_vd = customCommand("ls /vendor_dlkm/lib/modules/");
            ModulesAdapter adapter = new ModulesAdapter(SecondaryActivity.this, SecondaryActivity.this, pathList_vd, driverList_vd);
            adapter.id = 1; // Set ID to 1 for vendor_dlkm modules
            runOnUiThread(() -> {
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(SecondaryActivity.this));
                loading_vd.setVisibility(View.INVISIBLE);
                driverListText_vd.setText("");
                for (String s : driverList_vd) {
                    driverListText_vd.append(s + "\n");
                }
            });
        }).start();

        Button openMainActivityButton = findViewById(R.id.openMainActivityButton);
        openMainActivityButton.setOnClickListener(v -> {
            Intent intent = new Intent(SecondaryActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    public static ArrayList<String> customCommand(String command) {
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
                result.add(line);
            }
            br.close();
            BufferedReader br2 = new BufferedReader(new InputStreamReader(stderr));
            String lineError;
            while ((lineError = br2.readLine()) != null) {
                Log.e("ERROR", lineError);
                result.add(lineError);
            }
            br2.close();
        } catch (IOException ignored) {}
        process.destroy();
        return result;
    }

    public static Process generateSuProcess() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            e.printStackTrace();
            try {
                process = Runtime.getRuntime().exec("echo no root");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return process;
    }

    public static boolean contains(ArrayList<String> list, String item) {
        for (String s : list) {
            if (s.contains(item)) {
                return true;
            }
        }
        return false;
    }
}