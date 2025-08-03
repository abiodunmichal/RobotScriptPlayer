package com.example.dynamicserial;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private static final String ACTION_USB_PERMISSION = "com.example.dynamicserial.USB_PERMISSION";
    private TextView debugText;
    private StringBuilder logBuilder = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        debugText = findViewById(R.id.debugText);

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, 1);

        appendLog("App started. Scanning USB devices...");
        findSerialDevice();

        // Save log when tapped
        debugText.setOnClickListener(view -> {
            FileUtils.saveLogToFile(MainActivity.this, logBuilder.toString());
            appendLog("Log saved to Downloads.");
        });
    }

    private void findSerialDevice() {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();

        if (deviceList.isEmpty()) {
            appendLog("No USB devices found.");
            return;
        }

        for (UsbDevice device : deviceList.values()) {
            appendLog("USB device detected: " + device.getDeviceName());

            PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0,
                    new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);

            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            registerReceiver(usbReceiver, filter);

            usbManager.requestPermission(device, permissionIntent);
            break;
        }
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (ACTION_USB_PERMISSION.equals(intent.getAction())) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            appendLog("Permission granted for device: " + device.getDeviceName());
                            // TODO: Initialize serial connection here
                        }
                    } else {
                        appendLog("Permission denied for device.");
                    }
                }
            }
        }
    };

    private void appendLog(String text) {
        logBuilder.append(text).append("\n");
        debugText.setText(logBuilder.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(usbReceiver);
        } catch (Exception ignored) {}
    }
                            }
