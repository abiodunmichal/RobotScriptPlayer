package com.example.geobot;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ScrollView;
import android.database.Cursor;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST_CODE = 1;

    private TextView debugText;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        debugText = findViewById(R.id.debugText);
        scrollView = findViewById(R.id.scrollView);

        Button pickFileButton = findViewById(R.id.pickFileButton);
        pickFileButton.setOnClickListener(v -> openFilePicker());
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        String[] mimeTypes = {"text/plain", "application/octet-stream"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                readLuaFile(uri);
            }
        }
    }

    private void readLuaFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder fileContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append("\n");
            }
            reader.close();

            appendToLog("File content loaded:");
            appendToLog(fileContent.toString());

            // Placeholder for Lua interpreter execution
            runLuaScript(fileContent.toString());

        } catch (Exception e) {
            appendToLog("Error reading file: " + e.getMessage());
        }
    }

    private void runLuaScript(String script) {
        // This is where you’ll later add Lua execution logic
        appendToLog("Pretending to run script... (Lua engine to be added)");
    }

    private void appendToLog(String text) {
        runOnUiThread(() -> {
            debugText.append(text + "\n");
            scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
        });
    }
    }
