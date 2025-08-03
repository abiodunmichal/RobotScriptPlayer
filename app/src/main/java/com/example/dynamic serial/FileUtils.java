package com.example.dynamicserial;

import android.content.Context;
import android.net.Uri;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtils {
    public static String readTextFromUri(Context context, Uri uri) {
        StringBuilder builder = new StringBuilder();
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            reader.close();
        } catch (Exception e) {
            builder.append("Error reading file: ").append(e.getMessage());
        }
        return builder.toString();
    }
          }
