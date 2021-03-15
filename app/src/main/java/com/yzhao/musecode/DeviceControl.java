package com.yzhao.musecode;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.ui.HorizontalPositioning;
import com.androidplot.ui.Size;
import com.androidplot.ui.SizeMetric;
import com.androidplot.ui.SizeMode;
import com.androidplot.ui.VerticalPositioning;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.FastLineAndPointRenderer;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.choosemuse.libmuse.Eeg;
import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseArtifactPacket;
import com.choosemuse.libmuse.MuseDataListener;
import com.choosemuse.libmuse.MuseDataPacket;
import com.choosemuse.libmuse.MuseDataPacketType;
import com.yzhao.musecode.components.csv.EEGFileReader;
import com.yzhao.musecode.components.csv.EEGFileWriter;
import com.yzhao.musecode.components.graphs.DynamicSeries;
import com.yzhao.musecode.components.mqtt.Subscriber;
import com.yzhao.musecode.components.signal.CircularBuffer;
import com.yzhao.musecode.components.signal.Filter;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;


public class DeviceControl extends Activity implements View.OnClickListener {

    public DataListener dataListener;
    MainActivity appState;
    private int notchFrequency = 14;
    private static final int PLOT_LENGTH = 255 * 3;
    public CircularBuffer eegBuffer = new CircularBuffer(220, 4);
    private static final String PLOT_TITLE = "Raw_EEG";
    public DynamicSeries dataSeries;

    public int samplingRate = 256;
    public Filter activeFilter;
    public double[][] filtState;
    public int channelOfInterest = 3;
    private boolean posibliBlink = false;
    private int posibliBlinkPosition = 0;
    private int sensibility_detection = 65;
    private int current_umbral = 75;
    private boolean systemActivated = false;

    TextView txt_current_sensibility;
    TextView txt_current_umbral;
    LinearLayout layout_up;
    LinearLayout layout_down;
    LinearLayout layout_check;

    float[] originalSignalShortBlink;
    float[] originalSignalLongBlink;
    float[] originalSignalNoneBlink;
    private Knn knn;

    Button tv_on_of;
    Button channel_up;
    Button channel_down;

    //TV - CHNUP - CHNDW
    private static int BTN_STATE = 0;
    private static String CURRENT_COMMAND = "";

    Subscriber mqttSuscriber;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_control);
        startConfigurations();
        readDataBase();
        knn = new Knn(originalSignalShortBlink, originalSignalLongBlink, originalSignalNoneBlink, current_umbral);
        initUI();
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.tv_on_of) {
            changeButton("TV");
            try {
                excecuteCommand("..");
            } catch (MqttException e) {
                e.printStackTrace();
            }

        } else if (view.getId() == R.id.channel_up) {
            changeButton("CHNUP");
            try {
                excecuteCommand("..");
            } catch (MqttException e) {
                e.printStackTrace();
            }

        } else if (view.getId() == R.id.channel_down) {
            changeButton("CHNDW");
            try {
                excecuteCommand("..");
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

    }

    public void initUI() {

        layout_up = (LinearLayout) this.findViewById(R.id.layout_up);
        layout_up.setVisibility(LinearLayout.GONE);

        layout_down = (LinearLayout) this.findViewById(R.id.layout_down);
        layout_down.setVisibility(LinearLayout.GONE);

        layout_check = (LinearLayout) this.findViewById(R.id.layout_check);
        layout_check.setVisibility(LinearLayout.GONE);

        tv_on_of = (Button) findViewById(R.id.tv_on_of);
        tv_on_of.setOnClickListener(this);

        channel_up = (Button) findViewById(R.id.channel_up);
        channel_up.setOnClickListener(this);

        channel_down = (Button) findViewById(R.id.channel_down);
        channel_down.setOnClickListener(this);

//        SeekBar seekBar_sensibility = findViewById(R.id.seekBar_sensibility);
//        seekBar_sensibility.setOnSeekBarChangeListener(seekBarSensibilityChangeListener);
//
//        txt_current_sensibility = findViewById(R.id.txt_current_sensibility);
//        txt_current_sensibility.setText("" + sensibility_detection);
//
//        SeekBar seekBar_umbral = findViewById(R.id.seekBar_umbral);
//        seekBar_umbral.setOnSeekBarChangeListener(seekBarUmbralChangeListener);
//
//        txt_current_umbral = findViewById(R.id.txt_current_umbral);
//        txt_current_umbral.setText("" + current_umbral);
    }

    SeekBar.OnSeekBarChangeListener seekBarSensibilityChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int sensibility_detection, boolean fromUser) {
            txt_current_sensibility.setText("" + sensibility_detection);
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
        public void onProgressChanged(SeekBar seekBar, int current_umbral, boolean fromUser) {
            txt_current_umbral.setText("" + current_umbral);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }

    };

    public void addCommand(String command) throws MqttException {
        CURRENT_COMMAND = CURRENT_COMMAND + "" + command;
        TextView statusText = findViewById(R.id.current_command);
        statusText.setText(CURRENT_COMMAND);

        if (CURRENT_COMMAND.length() == 2) {
            excecuteCommand(CURRENT_COMMAND);
            CURRENT_COMMAND = "";
        }
    }


    void startMqttConnection() throws MqttException, URISyntaxException {
        mqttSuscriber = new Subscriber("mqtt://qnfkgujq:2S14ysy_WIFT@driver.cloudmqtt.com:18841");
    }


    public void excecuteCommand(String command) throws MqttException {
        if (command.equals("--"))
            disableEnableSistem();

        if (systemActivated) {
            switch (command) {
                case "-.":

                    BTN_STATE = BTN_STATE + 1;
                    if (BTN_STATE > 2)
                        BTN_STATE = 0;

                    if (BTN_STATE == 0)
                        changeButton("TV");
                    else if (BTN_STATE == 1)
                        changeButton("CHNUP");
                    else if (BTN_STATE == 2)
                        changeButton("CHNDW");
                    break;

                case ".-":
                    BTN_STATE = BTN_STATE - 1;
                    if (BTN_STATE < 0)
                        BTN_STATE = 2;

                    if (BTN_STATE == 0)
                        changeButton("TV");
                    else if (BTN_STATE == 1)
                        changeButton("CHNUP");
                    else if (BTN_STATE == 2)
                        changeButton("CHNDW");
                    break;
                case "..":

                    if (BTN_STATE == 0) {
                        mqttSuscriber.sendMessage("tv_on_off");
                    } else if (BTN_STATE == 1) {
                        mqttSuscriber.sendMessage("chn_dwn");
                    } else if (BTN_STATE == 2) {
                        mqttSuscriber.sendMessage("chn_up");
                    }
                    break;
                default:
                    break;
            }
        }

    }

    public void disableEnableSistem() {
        systemActivated = !systemActivated;
        System.out.println("systemActivated: " + systemActivated);

        if (systemActivated) {
            layout_up.setVisibility(LinearLayout.VISIBLE);
            layout_down.setVisibility(LinearLayout.VISIBLE);
            layout_check.setVisibility(LinearLayout.VISIBLE);
        } else {
            layout_up.setVisibility(LinearLayout.GONE);
            layout_down.setVisibility(LinearLayout.GONE);
            layout_check.setVisibility(LinearLayout.GONE);
        }
    }

    public void changeButton(String activeButton) {

        switch (activeButton) {
            case "TV":
                BTN_STATE = 0;
                tv_on_of.setBackground(getResources().getDrawable(R.drawable.primary_color_circle));
                tv_on_of.setTextColor(getApplication().getResources().getColor(R.color.primary_white));
                channel_up.setBackground(getResources().getDrawable(R.drawable.white_color_circle));
                channel_up.setTextColor(getApplication().getResources().getColor(R.color.primary_color1));
                channel_down.setBackground(getResources().getDrawable(R.drawable.white_color_circle));
                channel_down.setTextColor(getApplication().getResources().getColor(R.color.primary_color1));
                break;
            case "CHNUP":
                BTN_STATE = 1;
                tv_on_of.setBackground(getResources().getDrawable(R.drawable.white_color_circle));
                tv_on_of.setTextColor(getApplication().getResources().getColor(R.color.primary_color1));
                channel_up.setBackground(getResources().getDrawable(R.drawable.primary_color_circle));
                channel_up.setTextColor(getApplication().getResources().getColor(R.color.primary_white));
                channel_down.setBackground(getResources().getDrawable(R.drawable.white_color_circle));
                channel_down.setTextColor(getApplication().getResources().getColor(R.color.primary_color1));
                break;
            case "CHNDW":
                BTN_STATE = 2;
                tv_on_of.setBackground(getResources().getDrawable(R.drawable.white_color_circle));
                tv_on_of.setTextColor(getApplication().getResources().getColor(R.color.primary_color1));
                channel_up.setBackground(getResources().getDrawable(R.drawable.white_color_circle));
                channel_up.setTextColor(getApplication().getResources().getColor(R.color.primary_color1));
                channel_down.setBackground(getResources().getDrawable(R.drawable.primary_color_circle));
                channel_down.setTextColor(getApplication().getResources().getColor(R.color.primary_white));
                break;
            default:
                break;
        }

    }

    public void startConfigurations() {
        try {
            startMqttConnection();
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        dataSeries = new DynamicSeries(PLOT_TITLE);

        if (dataListener == null) {
            dataListener = new DataListener();
        }
        appState.connectedMuse.registerDataListener(dataListener, MuseDataPacketType.EEG);
        if (dataListener != null) {
            dataListener.updateFilter(notchFrequency);
        }
        activeFilter = new Filter(samplingRate, "bandstop", 5, 1, 6);
        filtState = new double[4][activeFilter.getNB()];
    }


    public void stopDataListener() {
        appState.connectedMuse.unregisterAllListeners();
    }

    private final class DataListener extends MuseDataListener {

        public double[] newData;
        public Filter bandstopFilter;
        private int frameCounter = 0;

        DataListener() {
            newData = new double[4];
        }

        @Override
        public void receiveMuseDataPacket(final MuseDataPacket p, final Muse muse) {
            getEegChannelValues(newData, p);
            filtState = activeFilter.transform(newData, filtState);
            eegBuffer.update(activeFilter.extractFilteredSamples(filtState));
            frameCounter++;
            if (frameCounter % 15 == 0) {
                if (eegBuffer.extract(1)[0][channelOfInterest] < (740 + sensibility_detection) && !posibliBlink) {
                    posibliBlink = true;
                    posibliBlinkPosition = frameCounter;
                }
                if (posibliBlink && (posibliBlinkPosition + 390) == frameCounter) {
                    posibliBlink = false;

                    String blink = knn.evaluateBlink(dataSeries);
                    if (blink.length() > 0) {
                        try {
                            addCommand(blink);
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }
                }
                updateDataSeries();
            }
        }

        private void getEegChannelValues(double[] newData, MuseDataPacket p) {
            newData[0] = p.getEegChannelValue(Eeg.EEG1);
            newData[1] = p.getEegChannelValue(Eeg.EEG2);
            newData[2] = p.getEegChannelValue(Eeg.EEG3);
            newData[3] = p.getEegChannelValue(Eeg.EEG4);

        }

        @Override
        public void receiveMuseArtifactPacket(final MuseArtifactPacket p, final Muse muse) {
        }

        public void updateFilter(int notchFrequency) {
            if (bandstopFilter != null) {
                bandstopFilter.updateFilter(notchFrequency - 5, notchFrequency + 5);
            }
        }
    }

    public void updateDataSeries() {
        int numEEGPoints = eegBuffer.getPts();
        if (dataSeries.size() >= PLOT_LENGTH) {
            dataSeries.remove(numEEGPoints);
        }
        dataSeries.addAll(eegBuffer.extractSingleChannelTransposedAsDouble(numEEGPoints, channelOfInterest));
        eegBuffer.resetPts();
    }

    void readDataBase() {

        String dbShortBlink = "ShortBlinkDB";
        String dbLongBlink = "LongBlinkDB";
        String dbNoneBlink = "NoneBlinkDB";

        try {
            final File file = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), dbShortBlink + ".json");
            FileReader filePathReader = new java.io.FileReader(file);
            InputStream inputStream = getResources().getAssets().open(dbShortBlink + ".json");
            EEGFileReader fileReader = new EEGFileReader(filePathReader);
            //EEGFileReader fileReader = new EEGFileReader(inputStream);
            originalSignalShortBlink = fileReader.readToVector();
            System.out.println("Lectura del primer archivo");

        } catch (IOException e) {
            Log.w("EEGGraph", "File not found error");
        }

        try {
            final File file = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), dbLongBlink + ".json");
            FileReader filePathReader = new java.io.FileReader(file);
            InputStream inputStream = getResources().getAssets().open(dbLongBlink + ".json");
            EEGFileReader fileReader = new EEGFileReader(filePathReader);
            //EEGFileReader fileReader = new EEGFileReader(inputStream);

            originalSignalLongBlink = fileReader.readToVector();
            System.out.println("Lectura del segundo archivo");
        } catch (IOException e) {
            Log.w("EEGGraph", "File not found error");
        }

        try {
            final File file = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), dbNoneBlink + ".json");
            FileReader filePathReader = new java.io.FileReader(file);
            InputStream inputStream = getResources().getAssets().open(dbNoneBlink + ".json");
            EEGFileReader fileReader = new EEGFileReader(filePathReader);
            //EEGFileReader fileReader = new EEGFileReader(inputStream);
            originalSignalNoneBlink = fileReader.readToVector();
            System.out.println("Lectura del tercer archivo");

        } catch (IOException e) {
            Log.w("EEGGraph", "File not found error");
        }
    }

}
