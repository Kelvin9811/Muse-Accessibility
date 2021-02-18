package com.yzhao.musecode;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
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
import com.yzhao.musecode.components.csv.EEGFileWriter;
import com.yzhao.musecode.components.graphs.DynamicSeries;
import com.yzhao.musecode.components.signal.CircularBuffer;
import com.yzhao.musecode.components.signal.Filter;
import com.choosemuse.libmuse.MuseDataListener;
import com.choosemuse.libmuse.MuseDataPacketType;


public class EEGPlot extends Activity implements View.OnClickListener {

    public DataListener dataListener;
    MainActivity appState;
    private LineAndPointFormatter lineFormatter;
    private int notchFrequency = 14;
    private static final int PLOT_LENGTH = 256 * 4;
    public CircularBuffer eegBuffer = new CircularBuffer(220, 4);
    private static final String PLOT_TITLE = "Raw_EEG";
    private int PLOT_LOW_BOUND = 600;
    private int PLOT_HIGH_BOUND = 1100;
    public DynamicSeries dataSeries;
    public XYPlot filterPlot;
    public int samplingRate = 256;
    public Filter activeFilter;
    public double[][] filtState;
    public int channelOfInterest = 3;
    MainActivity TAG;
    private int frameCounter = 0;
    private int numberOfRecordings = 0;
    private double[][] extractedArray = new double[1020][4];

    Button btn_start_capture;
    Button btn_save_record;
    Button btn_delete_record;

    EEGFileWriter csv = new EEGFileWriter(this, "Captura de datos");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.egg_graph);
        csv.initFile();
        //startDataListener();
        setNotchFrequency(notchFrequency);
        setFilterType();
        initUI();

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_save_record) {
            saveRecord();
        } else if (view.getId() == R.id.btn_capture) {
            startCapture();
        } else if (view.getId() == R.id.btn_delete_record) {
            deleteRecord();
        }
    }

    void startCapture() {
        startDataListener();
    }

    void deleteRecord() {
        btnsState("waiting_start");
        dataSeries.clear();
        updatePlot();
    }

    void saveRecord() {
        btnsState("waiting_start");
        numberOfRecordings++;
        makeToast(numberOfRecordings);

        for (int i = 0; i < 1020; i++) {
            csv.addDataToFile(extractedArray[i]);
        }

        if (numberOfRecordings == 15)
            csv.writeFile(PLOT_TITLE);
        dataSeries.clear();
        updatePlot();

    }




    /*
    *
    * if(processedSignal(i) < 830 && posibliBlink ==0)
        disp('Existe un probable parpadeo')
        posibliBlink = 1;
        posibliBlinkPosition = i;
     end

     if(posibliBlink == 1 && (posibliBlinkPosition+510) == i)
        disp('Se evalua el parpadeo despues de 2 segundos de una probabilidad de parpadeo')
        posibliBlink = 0;
        sampleToEvaluate = getSampleRange(processedSignal,i);
        knnFunction(sampleToEvaluate)
     end

 plot(getSampleRange(processedSignal,i));drawnow
 *
    * */


    public void makeToast(int numberOfRecordings) {
        CharSequence toastText = "La grabación número " + numberOfRecordings + " fue guardada con éxito.";
        Toast toast = Toast.makeText(this, toastText, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void initUI() {

        dataSeries = new DynamicSeries(PLOT_TITLE);
        filterPlot = new XYPlot(this, PLOT_TITLE);
        initView(this);
        /*btn_start_capture = findViewById(R.id.btn_capture);
        btn_start_capture.setOnClickListener(this);
        btn_save_record = findViewById(R.id.btn_save_record);
        btn_save_record.setOnClickListener(this);
        btn_delete_record = findViewById(R.id.btn_delete_record);
        btn_delete_record.setOnClickListener(this);
        btnsState("waiting_start");*/

        btn_start_capture = (Button) findViewById(R.id.btn_capture);
        btn_start_capture.setBackground(getResources().getDrawable(R.drawable.enable_button));
        btn_start_capture.setOnClickListener(this);

        btn_save_record = (Button) findViewById(R.id.btn_save_record);
        btn_save_record.setBackground(getResources().getDrawable(R.drawable.disable_button));
        btn_save_record.setEnabled(false);
        btn_save_record.setOnClickListener(this);

        btn_delete_record = (Button) findViewById(R.id.btn_delete_record);
        btn_delete_record.setBackground(getResources().getDrawable(R.drawable.disable_button));
        btn_save_record.setEnabled(false);
        btn_delete_record.setOnClickListener(this);

    }

    public void setNotchFrequency(int notchFrequency) {
        this.notchFrequency = notchFrequency;
        if (dataListener != null) {
            dataListener.updateFilter(notchFrequency);
        }
    }

    public void initView(Context context) {

        FrameLayout frameLayout = findViewById(R.id.frame_layout_xyplot);

        filterPlot = new XYPlot(context, PLOT_TITLE);

        // Create dataSeries that will be drawn on plot (Y will be obtained from dataSource, x will be implicitly generated):
        dataSeries = new DynamicSeries(PLOT_TITLE);

        // Set X and Y domain
        filterPlot.setRangeBoundaries(PLOT_LOW_BOUND,PLOT_HIGH_BOUND, BoundaryMode.FIXED);
        filterPlot.setDomainBoundaries(0, PLOT_LENGTH, BoundaryMode.FIXED);

        // Create line formatter with set color
        lineFormatter = new FastLineAndPointRenderer.Formatter(Color.WHITE, null, null);

        // Set line thickness
        lineFormatter.getLinePaint().setStrokeWidth(3);

        // Add line to plot
        filterPlot.addSeries(dataSeries, lineFormatter);

        // Format plot layout
        //Remove margins, padding and border
        filterPlot.setPlotMargins(0, 0, 0, 0);
        filterPlot.setPlotPadding(0, 0, 0, 0);
        filterPlot.getBorderPaint().setColor(Color.WHITE);

        // Set plot background color
        filterPlot.getGraph().getBackgroundPaint().setColor(Color.rgb(104, 176, 171));

        // Remove gridlines
        filterPlot.getGraph().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
        filterPlot.getGraph().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlot.getGraph().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);
        filterPlot.getGraph().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlot.getGraph().getRangeOriginLinePaint().setColor(Color.TRANSPARENT);

        //filterPlot.getGraph().setLineLabelEdges(XYGraphWidget.Edge.LEFT, XYGraphWidget.Edge.BOTTOM);

        // Remove axis labels and values
        // Domain = X; Range = Y
        filterPlot.setDomainLabel(null);
        filterPlot.setRangeLabel(null);
        filterPlot.getGraph().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlot.getGraph().getRangeOriginLinePaint().setColor(Color.TRANSPARENT);
        filterPlot.getGraph().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlot.getGraph().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);

        // Remove extraneous elements
        filterPlot.getLayoutManager().remove(filterPlot.getLegend());

        // Set size of plot
        SizeMetric height = new SizeMetric(1, SizeMode.FILL);
        SizeMetric width = new SizeMetric(1, SizeMode.FILL);
        filterPlot.getGraph().setSize(new Size(height, width));

        // Set position of plot (should be tweaked in order to center chart position)
        filterPlot.getGraph().position(0, HorizontalPositioning.ABSOLUTE_FROM_LEFT.ABSOLUTE_FROM_LEFT,
                0, VerticalPositioning.ABSOLUTE_FROM_TOP);

        // Add plot to FilterGraph
        frameLayout.addView(filterPlot, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
    }

    public void btnsState(String state) {

        switch (state) {
            case "recording":
                btn_delete_record.setEnabled(false);
                btn_delete_record.setBackground(getResources().getDrawable(R.drawable.disable_button));
                btn_save_record.setEnabled(false);
                btn_save_record.setBackground(getResources().getDrawable(R.drawable.disable_button));
                btn_start_capture.setEnabled(false);
                btn_start_capture.setBackground(getResources().getDrawable(R.drawable.disable_button));
                break;
            case "record_saved":
                btn_delete_record.setEnabled(true);
                btn_delete_record.setBackground(getResources().getDrawable(R.drawable.delete_button));
                btn_save_record.setEnabled(true);
                btn_save_record.setBackground(getResources().getDrawable(R.drawable.enable_button));
                btn_start_capture.setEnabled(false);
                btn_start_capture.setBackground(getResources().getDrawable(R.drawable.disable_button));
                break;
            case "waiting_start":
                btn_delete_record.setEnabled(false);
                btn_delete_record.setBackground(getResources().getDrawable(R.drawable.disable_button));
                btn_save_record.setEnabled(false);
                btn_save_record.setBackground(getResources().getDrawable(R.drawable.disable_button));
                btn_start_capture.setEnabled(true);
                btn_start_capture.setBackground(getResources().getDrawable(R.drawable.enable_button));
                break;
        }

    }

    public void setFilterType() {
        activeFilter = new Filter(samplingRate, "bandstop", 5, 1, 6);
        filtState = new double[4][activeFilter.getNB()];
    }

    public void startDataListener() {
        btnsState("recording");
        if (dataListener == null) {
            dataListener = new DataListener();
        }
        appState.connectedMuse.registerDataListener(dataListener, MuseDataPacketType.EEG);
    }

    public void stopDataListener() {
        btnsState("record_saved");
        appState.connectedMuse.unregisterAllListeners();
    }

    private final class DataListener extends MuseDataListener {

        public double[] newData;
        public Filter bandstopFilter;
        private int frameCounter = 0;

        DataListener() {
            newData = new double[4];
        }

        // Updates eegBuffer with new data from all 4 channels. Bandstop filter for 2016 Muse
        @Override
        public void receiveMuseDataPacket(final MuseDataPacket p, final Muse muse) {
            getEegChannelValues(newData, p);



            filtState = activeFilter.transform(newData, filtState);
            eegBuffer.update(activeFilter.extractFilteredSamples(filtState));

            extractedArray[frameCounter] = activeFilter.extractFilteredSamples(filtState);
            //extractedArray[frameCounter] = newData;
            //System.out.println(activeFilter.extractFilteredSamples(filtState)[channelOfInterest]);
            //csv.addDataToFile(newData);
            frameCounter++;
            if (frameCounter % 15 == 0) {
                updatePlot();
            }

            //Detiene la grabacion de datos a los 4 segundos
            if (frameCounter == 1020) {
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

        // For adding all data points (Full sampling)
        dataSeries.addAll(eegBuffer.extractSingleChannelTransposedAsDouble(numEEGPoints, channelOfInterest));

        // resets the 'points-since-dataSource-read' value
        eegBuffer.resetPts();

        filterPlot.redraw();
    }

}
