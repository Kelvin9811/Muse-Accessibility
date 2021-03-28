package com.yzhao.musecode;

import android.content.Intent;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.choosemuse.libmuse.Muse;
import com.yzhao.musecode.components.csv.ConfigurationsFileManager;
import com.yzhao.musecode.components.csv.DataBaseFileWriter;
import com.yzhao.musecode.components.csv.EEGFileReader;
import com.yzhao.musecode.components.csv.EEGFileWriter;
import com.yzhao.musecode.components.mqtt.Subscriber;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static Muse connectedMuse;
    public final String TAG = "MuseCode";

    public static int channelOfInterest = 3;
    public static int detectionSensibility = 65;
    public static int probabilitySensibility = 65;
    public static int kNearestNeighbors = 15;
    public static int maxSignalFrequency = 950;
    public static int minSignalFrequency = 750;

    @Override
    public void onClick(View view) {
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.introduction);
        initUI();
        startDataBase();

    }

    public void initUI() {
        Button start_record = (Button) findViewById(R.id.btn_start_conection);
        start_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecordActivity();
            }
        });

        Button start_control = (Button) findViewById(R.id.btn_start_device_control);
        start_control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startControlActivity();
            }
        });

        Button start_test = (Button) findViewById(R.id.btn_user_configurations);
        start_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userConfigurationsActivity();
            }
        });
    }

    public void startRecordActivity() {
        Intent intent = new Intent(this, MuseConnection.class);
        intent.putExtra("FLOW", "RECORD");
        startActivity(intent);
    }

    public void startControlActivity() {
        Intent intent = new Intent(this, MuseConnection.class);
        intent.putExtra("FLOW", "CONTROL");
        startActivity(intent);
    }

    public void userConfigurationsActivity() {
        Intent intent = new Intent(this, UserConfigurations.class);
        startActivity(intent);
    }


    void startDataBase() {

        boolean databaseReady = false;
        boolean configurationsReady = false;
        String dbShortBlink = "ShortBlinkDB";
        String dbLongBlink = "LongBlinkDB";
        String dbNoneBlink = "NoneBlinkDB";
        String configurations = "Configurations";

        final File fileShortBlinkDB = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), dbShortBlink + ".json");
        final File fileLongBlinkDB = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), dbLongBlink + ".json");
        final File fileNoneBlinkDB = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), dbNoneBlink + ".json");
        final File configurationsDB = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), configurations + ".json");

        try {
            new FileReader(fileShortBlinkDB);
            new java.io.FileReader(fileLongBlinkDB);
            new java.io.FileReader(fileNoneBlinkDB);
            new java.io.FileReader(configurationsDB);
        } catch (FileNotFoundException e) {
            databaseReady = true;
        }

        if (databaseReady) {
            EEGFileWriter shorBlinkFile = new EEGFileWriter(this, "Captura de datos");
            EEGFileWriter longBlinkFile = new EEGFileWriter(this, "Captura de datos");
            EEGFileWriter noneBlinkFile = new EEGFileWriter(this, "Captura de datos");
            EEGFileWriter configurationsFile = new EEGFileWriter(this, "Captura de datos");

            shorBlinkFile.initFile();
            longBlinkFile.initFile();
            noneBlinkFile.initFile();
            configurationsFile.initFile();

            try {

                InputStream inputStream = getResources().getAssets().open(dbShortBlink + ".json");
                DataBaseFileWriter fileReader = new DataBaseFileWriter(inputStream);
                fileReader.writeShortBlinkDataBase(shorBlinkFile);
            } catch (IOException e) {
                Log.w("EEGGraph", "File not found error");
            }

            try {

                InputStream inputStream = getResources().getAssets().open(dbLongBlink + ".json");
                DataBaseFileWriter fileReader = new DataBaseFileWriter(inputStream);
                fileReader.writeLongBlinkDataBase(longBlinkFile);
            } catch (IOException e) {
                Log.w("EEGGraph", "File not found error");
            }

            try {

                InputStream inputStream = getResources().getAssets().open(dbNoneBlink + ".json");
                DataBaseFileWriter fileReader = new DataBaseFileWriter(inputStream);
                fileReader.writeNoneBlinkDataBase(noneBlinkFile);
            } catch (IOException e) {
                Log.w("EEGGraph", "File not found error");
            }

            try {

                InputStream inputStream = getResources().getAssets().open(configurations + ".json");
                ConfigurationsFileManager fileReader = new ConfigurationsFileManager(inputStream);
                fileReader.writeStartConfiguration(configurationsFile);

                int[] readList = fileReader.readList;
                channelOfInterest = readList[0];
                detectionSensibility = readList[1];
                probabilitySensibility = readList[2];
                kNearestNeighbors = readList[3];
                maxSignalFrequency = readList[4];
                minSignalFrequency = readList[5];

            } catch (IOException e) {
                Log.w("EEGGraph", "File not found error");
            }
        }

        try {
            final File file = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), configurations + ".json");
            FileReader filePathReader = new java.io.FileReader(file);
            EEGFileReader fileReader = new EEGFileReader(filePathReader);

            int[] startConfigurations = fileReader.readConfigurations();

            channelOfInterest = startConfigurations[0];
            detectionSensibility = startConfigurations[1];
            probabilitySensibility = startConfigurations[2];
            kNearestNeighbors = startConfigurations[3];
            maxSignalFrequency = startConfigurations[4];
            minSignalFrequency = startConfigurations[5];

        } catch (IOException e) {
            Log.w("EEGGraph", "File not found error");
        }

    }


}

