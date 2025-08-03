package com.example.dynamicserial;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView debugText;
    private Button openFileButton;
    private UsbSerialPort serialPort;
    private final String TAG = "DynamicSerial";
    private final int REQUEST_PERMISSION = 1;

    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(uri);
                            if (inputStream != null) {
                                String content = FileUtils.readInputStream(inputStream);
                                appendLog("Loaded file:\n" + content);
                                sendSerial(content);
                            }
                        } catch (Exception e) {
                            appendLog("Error reading file: " + e.getMessage());
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        debugText = findViewById(R.id.debugText);
        openFileButton = findViewById(R.id.openFileButton);

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE
        }, REQUEST_PERMISSION);

        openFileButton.setOnClickListener(v -> openFilePicker());

        connectToSerialDevice();
    }

    private void connectToSerialDevice() {
        UsbManager usbManager = (UsbManager) getSystemService(USB_SERVICE);
        List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
        if (drivers.isEmpty()) {
            appendLog("No USB serial devices found.");
            return;
        }

        UsbSerialDriver driver = drivers.get(0);
        UsbDevice device = driver.getDevice();
        if (!usbManager.hasPermission(device)) {
            appendLog("No USB permission.");
            return;
        }

        serialPort = driver.getPorts().get(0);
        try {
            serialPort.open(usbManager.openDevice(device));
            serialPort.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            appendLog("Serial connected.");
        } catch (Exception e) {
            appendLog("Error connecting serial: " + e.getMessage());
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse("/storage/emulated/0/Download/"));
        filePickerLauncher.launch(intent);
    }

    private void sendSerial(String message) {
        if (serialPort == null) {
            appendLog("Serial port not connected.");
            return;
        }
        try {
            serialPort.write(message.getBytes(StandardCharsets.UTF_8), 1000);
            appendLog("Sent to Arduino:\n" + message);
        } catch (Exception e) {
            appendLog("Error sending serial: " + e.getMessage());
        }
    }

    private void appendLog(String msg) {
        debugText.append("\n" + msg);
        Log.d(TAG, msg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (serialPort != null) serialPort.close();
        } catch (Exception e) {
            appendLog("Error closing serial: " + e.getMessage());
        }
    }
                      }
