package com.yzhao.musecode.components.csv;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads EEG data from CSV files
 */

public class DataBaseFileWriter {

    // ---------------------------------------------------------------------------
    // Variables

    //FileReader inputStream;
    InputStream inputStream;
    private Context context;
    List<double[]> readList;


    // public DataBaseFileWriter(FileReader inputStream) {
    public DataBaseFileWriter(InputStream inputStream) {
        this.inputStream = inputStream;
        this.context = context;
    }

    public List read() {
        List<double[]> resultList = new ArrayList();
        //BufferedReader reader = new BufferedReader(inputStream);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String csvLine;
            while ((csvLine = reader.readLine()) != null) {
                String[] stringLine = csvLine.split(",");
                double[] line = new double[stringLine.length];
                for (int i = 0; i < line.length; i++) {
                    line[i] = Double.parseDouble(stringLine[i]);
                }
                resultList.add(line);
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

    public void writeShortBlinkDataBase(EEGFileWriter file) {
        readList = read();
        int len = readList.size();
        double[] readArray ;
        for (int i = 0; i < len; i++) {
            readArray = new double[]{readList.get(i)[1], readList.get(i)[2], readList.get(i)[3], readList.get(i)[4]};
            file.addDataToFile(readArray);
        }
        file.writeShortBlinkFile();
    }

    public void writeLongBlinkDataBase(EEGFileWriter file) {
        readList = read();
        int len = readList.size();
        double[] readArray ;
        for (int i = 0; i < len; i++) {
            readArray = new double[]{readList.get(i)[1], readList.get(i)[2], readList.get(i)[3], readList.get(i)[4]};
            file.addDataToFile(readArray);
        }
        file.writeLongBlinkFile();
    }

    public void writeNoneBlinkDataBase(EEGFileWriter file) {
        readList = read();
        int len = readList.size();
        double[] readArray ;
        for (int i = 0; i < len; i++) {
            readArray = new double[]{readList.get(i)[1], readList.get(i)[2], readList.get(i)[3], readList.get(i)[4]};
            file.addDataToFile(readArray);
        }
        file.writeNoneBlinkFile();
    }
}
