package com.example.geobot;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends Activity {

    private static final int PICK_FILE_REQUEST = 1;

    private TextView debugText;
    private SerialManager serialManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        debugText = findViewById(R.id.debugText);
        Button pickButton = findViewById(R.id.pickFileButton);

        serialManager = new SerialManager(this, this::appendToLog);
        serialManager.start();

        pickButton.setOnClickListener(v -> openFilePicker());
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                runLuaScript(uri);
            }
        }
    }

    private void runLuaScript(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder script = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                script.append(line).append("\n");
            }
            reader.close();

            appendToLog("Running script...");
            LuaRunner.runScript(script.toString(), serialManager, this::appendToLog);

        } catch (Exception e) {
            appendToLog("Error reading file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void appendToLog(String message) {
        runOnUiThread(() -> {
            debugText.append(message + "\n");
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        serialManager.stop();
    }
}
