package com.yzhao.musecode;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.choosemuse.libmuse.Eeg;
import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseArtifactPacket;
import com.choosemuse.libmuse.MuseDataPacket;
import com.yzhao.musecode.components.csv.EEGFileReader;
import com.yzhao.musecode.components.csv.EEGFileWriter;
import com.yzhao.musecode.components.graphs.DynamicSeries;
import com.yzhao.musecode.components.signal.CircularBuffer;
import com.yzhao.musecode.components.signal.CircularBufferProcessed;
import com.yzhao.musecode.components.signal.Filter;
import com.choosemuse.libmuse.MuseDataListener;
import com.choosemuse.libmuse.MuseDataPacketType;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;


public class BlinkTest extends Activity implements View.OnClickListener {

    public DataListener dataListener;

    MainActivity appState;
    MainActivity configurations;

    private LineAndPointFormatter lineFormatter;
    private int notchFrequency = 14;
    private static final int PLOT_LENGTH = 255 * 3;
    public CircularBufferProcessed eegBuffer;
    private static final String PLOT_TITLE = "Raw_EEG";

    public DynamicSeries dataSeries;
    public XYPlot filterPlot;
    public int samplingRate = 256;
    public Filter activeFilter;
    public double[][] filtState;
    public int channelOfInterest = 3;
    public int maxSignalFrequency = 950;
    public int minSignalFrequency = 750;

    MainActivity TAG;
    private int frameCounter = 0;
    private int numberOfRecordings = 0;

    private int numberOfShortRecordings = 0;
    private int numberOfLongRecordings = 0;

    private String typeofRecord = "largos";


    private int secondsOfRecording = 510;

    Button btn_capture_test;
    Button btn_back_test;


    float[] originalSignalShortBlink;
    float[] originalSignalLongBlink;
    float[] originalSignalNoneBlink;
    private Knn knn;
    private int current_umbral = 75;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blink_test);
        startConfigurations();
        readDataBase();

        channelOfInterest = configurations.channelOfInterest;
        maxSignalFrequency = configurations.maxSignalFrequency;
        minSignalFrequency = configurations.minSignalFrequency;

        eegBuffer = new CircularBufferProcessed(220, 4,maxSignalFrequency,minSignalFrequency);

        System.out.println(configurations.channelOfInterest);

        knn = new Knn(originalSignalShortBlink, originalSignalLongBlink, originalSignalNoneBlink, configurations.probabilitySensibility);
        initUI();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_back_test) {
            finishTesting();
        } else if (view.getId() == R.id.btn_capture_test) {
            startCapture();
        }
    }

    void startCapture() {
        deleteRecord();
        startDataListener();
    }

    void deleteRecord() {
        dataSeries.clear();
        updatePlot();
    }


    public void startConfigurations() {
        setNotchFrequency(notchFrequency);
        setFilterType();
    }

    public void setNotchFrequency(int notchFrequency) {
        this.notchFrequency = notchFrequency;
        if (dataListener != null) {
            dataListener.updateFilter(notchFrequency);
        }
    }

    public void setFilterType() {
        activeFilter = new Filter(samplingRate, "bandstop", 5, 1, 6);
        filtState = new double[4][activeFilter.getNB()];
    }

    void finishTesting() {
        appState.connectedMuse.disconnect(false);
        appState.connectedMuse.unregisterAllListeners();
        appState.connectedMuse = null;
        Intent i = new Intent(this, UserConfigurations.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }


    @SuppressLint({"SetTextI18n", "WrongViewCast"})
    public void initUI() {

        dataSeries = new DynamicSeries(PLOT_TITLE);
        filterPlot = new XYPlot(this, PLOT_TITLE);
        initView(this);

        btn_capture_test = (Button) findViewById(R.id.btn_capture_test);
        btn_capture_test.setBackground(getResources().getDrawable(R.drawable.enable_button));
        btn_capture_test.setOnClickListener(this);

        btn_back_test = (Button) findViewById(R.id.btn_back_test);
        btn_back_test.setOnClickListener(this);

        btnsState("waiting_start");

    }


    public void initView(Context context) {

        FrameLayout frameLayout = findViewById(R.id.frame_layout_xyplot_test);

        filterPlot = new XYPlot(context, PLOT_TITLE);

        dataSeries = new DynamicSeries(PLOT_TITLE);

        filterPlot.setRangeBoundaries(-3, 3, BoundaryMode.FIXED);
        filterPlot.setDomainBoundaries(0, PLOT_LENGTH, BoundaryMode.FIXED);

        lineFormatter = new FastLineAndPointRenderer.Formatter(Color.WHITE, null, null);

        lineFormatter.getLinePaint().setStrokeWidth(3);

        filterPlot.addSeries(dataSeries, lineFormatter);

        filterPlot.setPlotMargins(0, 0, 0, 0);
        filterPlot.setPlotPadding(0, 0, 0, 0);
        filterPlot.getBorderPaint().setColor(Color.WHITE);

        filterPlot.getGraph().getBackgroundPaint().setColor(Color.rgb(104, 176, 171));

        filterPlot.getGraph().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
        filterPlot.getGraph().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlot.getGraph().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);
        filterPlot.getGraph().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlot.getGraph().getRangeOriginLinePaint().setColor(Color.TRANSPARENT);

        filterPlot.setDomainLabel(null);
        filterPlot.setRangeLabel(null);
        filterPlot.getGraph().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlot.getGraph().getRangeOriginLinePaint().setColor(Color.TRANSPARENT);
        filterPlot.getGraph().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlot.getGraph().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);

        filterPlot.getLayoutManager().remove(filterPlot.getLegend());

        SizeMetric height = new SizeMetric(1, SizeMode.FILL);
        SizeMetric width = new SizeMetric(1, SizeMode.FILL);
        filterPlot.getGraph().setSize(new Size(height, width));

        filterPlot.getGraph().position(0, HorizontalPositioning.ABSOLUTE_FROM_LEFT.ABSOLUTE_FROM_LEFT,
                0, VerticalPositioning.ABSOLUTE_FROM_TOP);

        frameLayout.addView(filterPlot, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
    }

    public void btnsState(String state) {

        switch (state) {
            case "recording":
                btn_capture_test.setEnabled(false);
                btn_capture_test.setBackground(getResources().getDrawable(R.drawable.disable_button));
                btn_capture_test.setVisibility(LinearLayout.GONE);
                break;
            case "waiting_start":

                btn_capture_test.setEnabled(true);
                btn_capture_test.setBackground(getResources().getDrawable(R.drawable.enable_button));
                btn_capture_test.setVisibility(LinearLayout.VISIBLE);

                break;
        }

    }

    @SuppressLint("SetTextI18n")
    public void addCommand(String command) {
        if (command.equals(".")) {
            TextView statusText = findViewById(R.id.text_view_blink_result);
            statusText.setText("Parpadeo corto");
        }
        if (command.equals("-")) {
            TextView statusText = findViewById(R.id.text_view_blink_result);
            statusText.setText("Parpadeo largo");
        }
        if (command.equals("")) {
            TextView statusText = findViewById(R.id.text_view_blink_result);
            statusText.setText("No hubo parpadeo");
        }
    }


    public void startDataListener() {
        btnsState("recording");
        if (dataListener == null) {
            dataListener = new DataListener();
        }
        appState.connectedMuse.registerDataListener(dataListener, MuseDataPacketType.EEG);
    }

    public void stopDataListener() {
        btnsState("waiting_start");
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
                updatePlot();
            }

            if (frameCounter == secondsOfRecording) {
                String blink = knn.evaluateBlink(dataSeries);
                addCommand(blink);
                frameCounter = 0;
                stopDataListener();
            }
        }

        // Updates newData array based on incoming EEG channel values
        private void getEegChannelValues(double[] newData, MuseDataPacket p) {
            newData[0] = p.getEegChannelValue(Eeg.EEG1);
            newData[1] = p.getEegChannelValue(Eeg.EEG2);
            newData[2] = p.getEegChannelValue(Eeg.EEG3);
            newData[3] = p.getEegChannelValue(Eeg.EEG4);
        }

        @Override
        public void receiveMuseArtifactPacket(final MuseArtifactPacket p, final Muse muse) {
            // Does nothing for now
        }

        public void updateFilter(int notchFrequency) {
            if (bandstopFilter != null) {
                bandstopFilter.updateFilter(notchFrequency - 5, notchFrequency + 5);
            }
        }

    }

    public void updatePlot() {
        int numEEGPoints = eegBuffer.getPts();
        if (dataSeries.size() >= PLOT_LENGTH) {
            dataSeries.remove(numEEGPoints);
        }
        dataSeries.addAll(eegBuffer.extractSingleChannelTransposedAsDouble(numEEGPoints, channelOfInterest));
        eegBuffer.resetPts();
        filterPlot.redraw();
    }

    void readDataBase() {

        String dbShortBlink = "ShortBlinkDB";
        String dbLongBlink = "LongBlinkDB";
        String dbNoneBlink = "NoneBlinkDB";

        try {
            final File file = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), dbShortBlink + ".json");
            FileReader filePathReader = new java.io.FileReader(file);
            //InputStream inputStream = getResources().getAssets().open(dbShortBlink + ".json");
            EEGFileReader fileReader = new EEGFileReader(filePathReader);
            //EEGFileReader fileReader = new EEGFileReader(inputStream);
            originalSignalShortBlink = fileReader.readToVector(channelOfInterest+1);
            System.out.println("Lectura del primer archivo");

        } catch (IOException e) {
            Log.w("EEGGraph", "File not found error");
        }

        try {
            final File file = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), dbLongBlink + ".json");
            FileReader filePathReader = new java.io.FileReader(file);
            //InputStream inputStream = getResources().getAssets().open(dbLongBlink + ".json");
            EEGFileReader fileReader = new EEGFileReader(filePathReader);
            //EEGFileReader fileReader = new EEGFileReader(inputStream);

            originalSignalLongBlink = fileReader.readToVector(channelOfInterest+1);
            System.out.println("Lectura del segundo archivo");
        } catch (IOException e) {
            Log.w("EEGGraph", "File not found error");
        }

        try {
            final File file = new File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), dbNoneBlink + ".json");
            FileReader filePathReader = new java.io.FileReader(file);
            //InputStream inputStream = getResources().getAssets().open(dbNoneBlink + ".json");
            EEGFileReader fileReader = new EEGFileReader(filePathReader);
            //EEGFileReader fileReader = new EEGFileReader(inputStream);
            originalSignalNoneBlink = fileReader.readNoneBlink();
            System.out.println("Lectura del tercer archivo");

        } catch (IOException e) {
            Log.w("EEGGraph", "File not found error");
        }
    }
}
