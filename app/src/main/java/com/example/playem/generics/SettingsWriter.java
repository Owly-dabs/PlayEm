package com.example.playem.generics;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;

public class SettingsWriter {
    public static Runnable WriteToFile(Context context, String filename, String item) {
        return new Runnable() {
            @Override
            public void run() {
                Log.w("FILEW",String.format("Attempting write to file %s",filename));
                try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
                    Log.w("FILEW",String.format("Attempting write item to file: %s",item));
                    fos.write(item.getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public static Callable<String> ReadFromFile(Context context,String filename) {
        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                FileInputStream fis = context.openFileInput(filename);
                byte[] buffer = new byte[10];
                StringBuilder sb = new StringBuilder();
                while (fis.read(buffer) != -1) {
                    sb.append(new String(buffer));
                    buffer = new byte[10];
                }
                fis.close();
                return sb.toString();
            }
        };
    }
}
