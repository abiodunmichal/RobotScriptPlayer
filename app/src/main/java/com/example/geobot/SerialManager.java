package com.example.geobot;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.*;
import android.util.Log;
import com.hoho.android.usbserial.driver.*;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.List;

public class SerialManager {

    private UsbManager usbManager;
    private UsbSerialPort serialPort;
    private static final String ACTION_USB_PERMISSION = "com.example.geobot.USB_PERMISSION";

    public SerialManager(Context context) {
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    }

    public boolean connect(Context context) {
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
        if (availableDrivers.isEmpty()) {
            Log.e("SerialManager", "No USB device found");
            return false;
        }

        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = usbManager.openDevice(driver.getDevice());

        if (connection == null) {
            PendingIntent permissionIntent = PendingIntent.getBroadcast(
                    context, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
            usbManager.requestPermission(driver.getDevice(), permissionIntent);
            Log.e("SerialManager", "USB permission not granted yet");
            return false;
        }

        serialPort = driver.getPorts().get(0); // Most devices have just one port
        try {
            serialPort.open(connection);
            serialPort.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            Log.d("SerialManager", "Serial port opened");
            return true;
        } catch (IOException e) {
            Log.e("SerialManager", "Error opening serial port: " + e.getMessage());
            return false;
        }
    }

    public void send(String data) {
        if (serialPort != null) {
            try {
                serialPort.write(data.getBytes(), 1000);
            } catch (IOException e) {
                Log.e("SerialManager", "Send failed: " + e.getMessage());
            }
        }
    }

    public void close() {
        try {
            if (serialPort != null) {
                serialPort.close();
                Log.d("SerialManager", "Serial port closed");
            }
        } catch (IOException e) {
            Log.e("SerialManager", "Close failed: " + e.getMessage());
        }
    }
    }
