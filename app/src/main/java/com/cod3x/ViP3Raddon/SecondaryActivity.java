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

        RecyclerView recyclerView_vd = findViewById(R.id.list_vd);
        recyclerView_vd.setLayoutManager(new LinearLayoutManager(this));
        TextView loading_vd = findViewById(R.id.loading_vd);
        TextView driverListText_vd = findViewById(R.id.driverList_vd);
        CheckBox checkBox_vd = findViewById(R.id.boot_vd);
        checkBox_vd.setChecked(new Prefs(this).getBoolean("boot_vd"));
        checkBox_vd.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Prefs prefs = new Prefs(SecondaryActivity.this);
            prefs.putBoolean("boot_vd", isChecked);
        });

        new Thread(() -> {
            ArrayList<String> pathList_vd = customCommand_vd("ls /sys/bus/usb/drivers");
            ArrayList<String> driverList_vd = customCommand_vd("ls /vendor_dlkm/lib/modules/");
            ModulesAdapter adapter = new ModulesAdapter(SecondaryActivity.this, SecondaryActivity.this, driverList_vd, pathList_vd);
            adapter.id = 1; // Set ID to 1 for vendor_dlkm modules
            runOnUiThread(() -> {
                recyclerView_vd.setAdapter(adapter);
                recyclerView_vd.setLayoutManager(new LinearLayoutManager(SecondaryActivity.this));
                loading_vd.setVisibility(View.INVISIBLE);
                driverListText_vd.setText("");
                for (String s : pathList_vd) {
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

    public static ArrayList<String> customCommand_vd(String command) {
        ArrayList<String> result_vd = new ArrayList<>();
        Process process = generateSuProcess();
        try {
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();
            stdin.write((command + '\n').getBytes());
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();
            BufferedReader br3 = new BufferedReader(new InputStreamReader(stdout));
            String line;
            while ((line = br3.readLine()) != null) {
                Log.d("OUTPUT", line);
                result_vd.add(line);
            }
            br3.close();
            BufferedReader br4 = new BufferedReader(new InputStreamReader(stderr));
            String lineError;
            while ((lineError = br4.readLine()) != null) {
                Log.e("ERROR", lineError);
                result_vd.add(lineError);
            }
            br4.close();
        } catch (IOException ignored) {}
        process.destroy();
        return result_vd;
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

    public static boolean contains_vd(ArrayList<String> list, String item){
        for (String s : list){if (s.contains(item)){return true;}}
        return false;
    }
}