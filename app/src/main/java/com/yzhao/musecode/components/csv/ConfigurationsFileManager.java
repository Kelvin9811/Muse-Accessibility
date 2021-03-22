package com.yzhao.musecode.components.csv;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reads EEG data from CSV files
 */

public class ConfigurationsFileManager {

    // ---------------------------------------------------------------------------
    // Variables
    //Linea 1: Canal
    //Linea 2: Sensibilidad de detección
    //Linea 3: Sensibilidad de probabilidad de parpadeo
    //Linea 4: Número de vecinos cercanos
    // ---------------------------------------------------------------------------

    //FileReader inputStream;
    InputStream inputStream;
    private Context context;
    public int[] readList;


    // public DataBaseFileWriter(FileReader inputStream) {
    public ConfigurationsFileManager(InputStream inputStream) {
        this.inputStream = inputStream;
        this.context = context;
    }

    public int[] read() {
        int[] resultList = new int[4];
        //BufferedReader reader = new BufferedReader(inputStream);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String csvLine;
            int i = 0;
            while ((csvLine = reader.readLine()) != null) {
                resultList[i] = (Integer.parseInt(csvLine));
                i++;
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: " + ex);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException("Error while closing input stream: " + e);
            }
        }

        return resultList;
    }

    public void writeStartConfiguration(EEGFileWriter file) {
        readList = read();
        for (int i = 0; i < 4; i++) {
            System.out.println("------------" + readList[i]);
            file.addLineToFile("" + readList[i]);
        }
        file.writeConfigurationsFile();
    }




}
