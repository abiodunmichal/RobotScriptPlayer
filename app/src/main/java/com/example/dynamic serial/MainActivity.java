package com.example.dynamicserial;

import android.app.Activity;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MainActivity extends Activity {

    private static final int PICK_TXT_FILE = 1;
    private static final String TAG = "MainActivity";

    private UsbSerialPort serialPort;
    private TextView debugText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        debugText = findViewById(R.id.debugText);

        openSerialPort();
        pickTextFile();
    }

    private void openSerialPort() {
        UsbManager usbManager = (UsbManager) getSystemService(USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);

        if (availableDrivers.isEmpty()) {
            appendLog("No USB serial device found");
            return;
        }

        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDevice device = driver.getDevice();

        if (!usbManager.hasPermission(device)) {
            appendLog("No permission for USB device");
            return;
        }

        UsbDeviceConnection connection = usbManager.openDevice(device);
        if (connection == null) {
            appendLog("Failed to open USB device connection");
            return;
        }

        serialPort = driver.getPorts().get(0);
        try {
            serialPort.open(connection);
            serialPort.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            appendLog("Serial port opened");
        } catch (Exception e) {
            appendLog("Error opening serial port: " + e.getMessage());
        }
    }

    private void pickTextFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/plain");
        startActivityForResult(intent, PICK_TXT_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_TXT_FILE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                String fileContent = FileUtils.readInputStream(inputStream);
                appendLog("File loaded:\n" + fileContent);

                sendToSerial(fileContent);
            } catch (Exception e) {
                appendLog("Error reading file: " + e.getMessage());
            }
        }
    }

    private void sendToSerial(String data) {
        if (serialPort == null) {
            appendLog("Serial port not available");
            return;
        }

        try {
            serialPort.write(data.getBytes(StandardCharsets.UTF_8), 1000);
            appendLog("Sent to serial:\n" + data);
        } catch (Exception e) {
            appendLog("Failed to send data: " + e.getMessage());
        }
    }

    private void appendLog(String text) {
        runOnUiThread(() -> debugText.append(text + "\n"));
        Log.d(TAG, text);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serialPort != null) {
            try {
                serialPort.close();
                appendLog("Serial port closed");
            } catch (Exception ignored) {}
        }
    }
  }
