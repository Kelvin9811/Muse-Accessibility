package com.yzhao.musecode.components.csv;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Writes EEG data (either raw/filtered EEG or computed FFT) into a csv. Presents a toast when
 * recording is started and starts sharing intent for sending data to email when recording is
 * completed
 */

public class EEGFileWriter {

    // ---------------------------------------------------------------------------
    // Variables

    private Context context;
    StringBuilder builder;
    int fileNum = 1;
    public FileWriter fileWriter;
    private static boolean isRecording;

    // ---------------------------------------------------------------------------
    // Constructor

    public EEGFileWriter(Context context, String title) {
        this.context = context;
        isRecording = false;
    }

    // ---------------------------------------------------------------------------
    // Internal methods

    public void initFile() {
        builder = new StringBuilder();
        isRecording = true;
    }

    public void addDataToFile(double[] data) {
        // Append timestamp
        Long tsLong = System.currentTimeMillis();
        builder.append(tsLong.toString() +",");
        for (int j = 0; j < data.length; j++) {
            builder.append(""+(data[j]));
            if (j < data.length - 1) {
                builder.append(",");
            }
        }
        builder.append("\n");

    }

    public void addLineToFile(String line){
        builder.append(line);
        builder.append("\n");
    }



    public void writeFile(String title) {
        try {
            final File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

            final File file = new File(dir, title + fileNum + ".json");
            fileWriter = new java.io.FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(builder.toString());
            bufferedWriter.close();
            sendData(file);
            fileNum ++;
            isRecording = false;
        } catch (IOException e) {}
    }

    public void sendData(File dataCSV) {

        //FileProvider fileProvider = new FileProvider();
        //Intent sendIntent = new Intent();
        //sendIntent.setAction(Intent.ACTION_SEND);
        //sendIntent.setType("application/csv");
        //sendIntent.putExtra(Intent.EXTRA_STREAM, fileProvider.getUriForFile(this.context, "com.eeg_project.fileprovider", dataCSV));
        //context.startActivity(Intent.createChooser(sendIntent, "Export data to..."));
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void writeToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.csv", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public void addLineToFile(String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.csv", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

}
