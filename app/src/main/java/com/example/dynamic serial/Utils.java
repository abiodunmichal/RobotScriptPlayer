package com.example.dynamicserial;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Utils {

    public static void saveToDownloads(Context context, String fileName, String content) {
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }

            File file = new File(downloadsDir, fileName);
            FileWriter writer = new FileWriter(file, true);
            writer.append(content).append("\n");
            writer.flush();
            writer.close();

            Toast.makeText(context, "Saved to Downloads/" + fileName, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
