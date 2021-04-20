package com.yzhao.musecode;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.choosemuse.libmuse.Muse;
import com.yzhao.musecode.components.csv.ConfigurationsFileManager;
import com.yzhao.musecode.components.csv.DataBaseFileWriter;
import com.yzhao.musecode.components.csv.EEGFileWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

public class UserConfigurations extends AppCompatActivity implements View.OnClickListener {

    public static Muse connectedMuse;
    public final String TAG = "MuseCode";

    TextView txt_current_sensibility;
    TextView txt_current_umbral;
    TextView txt_current_knn;

    MainActivity configurations;
    EEGFileWriter configurationsFile = new EEGFileWriter(this, "Captura de datos");
    public int kNearestNeighbors = 10;
    public int detectionSensibility = 0;
    public int probabilitySensibility = 0;

    @Override
    public void onClick(View view) {
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_configurations);
        configurationsFile.initFile();
        kNearestNeighbors = configurations.kNearestNeighbors;
        probabilitySensibility = configurations.probabilitySensibility;
        detectionSensibility = configurations.detectionSensibility;
        initUI();
    }

    public void initUI() {
        Button chose_channel = (Button) findViewById(R.id.btn_chose_channel);
        chose_channel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startChoseChanelActivity();
            }
        });

        Button start_test = (Button) findViewById(R.id.btn_start_test_activity);
        start_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTestActivity();
            }
        });

        Button record_data_set = (Button) findViewById(R.id.btn_start_data_set_recording);
        record_data_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDataSetActivity();
            }
        });


        Button btn_save_tree_configurations = (Button) findViewById(R.id.btn_save_tree_configurations);
        btn_save_tree_configurations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveConfigurations();
            }
        });


        Button btn_add_neighbor = (Button) findViewById(R.id.btn_add_neighbor);
        btn_add_neighbor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNeighbor();
            }
        });


        Button btn_subtract_neighbor = (Button) findViewById(R.id.btn_subtract_neighbor);
        btn_subtract_neighbor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subtractNeighbor();
            }
        });


        SeekBar seekBar_sensibility = findViewById(R.id.seekBar_sensibility);
        seekBar_sensibility.setProgress(configurations.detectionSensibility);
        seekBar_sensibility.setOnSeekBarChangeListener(seekBarSensibilityChangeListener);

        txt_current_sensibility = findViewById(R.id.txt_current_sensibility);
        txt_current_sensibility.setText("Sensibilidad al detectar parpadeos: " + configurations.detectionSensibility + "%");

        SeekBar seekBar_umbral = findViewById(R.id.seekBar_umbral);
        seekBar_umbral.setProgress(configurations.probabilitySensibility);
        seekBar_umbral.setOnSeekBarChangeListener(seekBarUmbralChangeListener);

        txt_current_umbral = findViewById(R.id.txt_current_umbral);
        txt_current_umbral.setText("Sensibilidad al clasificar un parpadeo: " + configurations.probabilitySensibility + "%");

        txt_current_knn = findViewById(R.id.txt_current_knn);
        txt_current_knn.setText("" + configurations.kNearestNeighbors);

    }

    SeekBar.OnSeekBarChangeListener seekBarSensibilityChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int new_sensibility_detection, boolean fromUser) {
            txt_current_sensibility.setText("Sensibilidad al detectar parpadeos: " + new_sensibility_detection + "%");
            detectionSensibility = new_sensibility_detection;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }

    };

    SeekBar.OnSeekBarChangeListener seekBarUmbralChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int new_current_umbral, boolean fromUser) {
            txt_current_umbral.setText("Sensibilidad al clasificar un parpadeo: " + new_current_umbral + "%");
            probabilitySensibility = new_current_umbral;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }

    };

    public void startChoseChanelActivity() {
        Intent intent = new Intent(this, MuseConnection.class);
        intent.putExtra("FLOW", "CHANNEL");
        startActivity(intent);
    }

    public void startTestActivity() {
        Intent intent = new Intent(this, MuseConnection.class);
        intent.putExtra("FLOW", "TEST");
        startActivity(intent);
    }

    public void startDataSetActivity() {
        Intent intent = new Intent(this, MuseConnection.class);
        intent.putExtra("FLOW", "DATASET");
        startActivity(intent);
    }


    public void addNeighbor() {
        if (kNearestNeighbors < 15)
            kNearestNeighbors = kNearestNeighbors + 1;
        TextView channelOfInterest = findViewById(R.id.txt_current_knn);
        channelOfInterest.setText("" + (kNearestNeighbors));
    }

    public void subtractNeighbor() {
        if (kNearestNeighbors > 0)
            kNearestNeighbors = kNearestNeighbors - 1;
        TextView channelOfInterest = findViewById(R.id.txt_current_knn);
        channelOfInterest.setText("" + (kNearestNeighbors));

    }

    public void saveConfigurations() {

        configurations.kNearestNeighbors = kNearestNeighbors;
        configurations.probabilitySensibility = probabilitySensibility;
        configurations.detectionSensibility = detectionSensibility;


        configurationsFile.addLineToFile("" + configurations.channelOfInterest);
        configurationsFile.addLineToFile("" + configurations.detectionSensibility);
        configurationsFile.addLineToFile("" + configurations.probabilitySensibility);
        configurationsFile.addLineToFile("" + configurations.kNearestNeighbors);

        configurationsFile.writeConfigurationsFile();


        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);

    }


}