package com.yzhao.musecode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

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
import com.choosemuse.libmuse.MuseDataPacket;
import com.yzhao.musecode.components.csv.EEGFileWriter;
import com.yzhao.musecode.components.graphs.DynamicSeries;
import com.yzhao.musecode.components.signal.CircularBuffer;
import com.yzhao.musecode.components.signal.Filter;
import com.choosemuse.libmuse.MuseDataListener;
import com.choosemuse.libmuse.MuseDataPacketType;


public class EEGPlot extends Activity implements View.OnClickListener {


    public String offlineData = "";
    public DataListener dataListener;
    MainActivity appState;
    private LineAndPointFormatter lineFormatter;
    private int notchFrequency = 60;
    private static final int PLOT_LENGTH = 256 * 4;
    public CircularBuffer eegBuffer = new CircularBuffer(220, 4);
    private static final String PLOT_TITLE = "Raw_EEG";
    private int PLOT_LOW_BOUND = 600;
    private int PLOT_HIGH_BOUND = 1000;
    public DynamicSeries dataSeries;
    public XYPlot filterPlot;
    public int samplingRate = 256;
    public Filter activeFilter;
    public double[][] filtState;
    public int channelOfInterest = 1;
    public boolean isRecording = true;


    EEGFileWriter csv = new EEGFileWriter(this, "Titulo de ejemplo");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.egg_graph);

        csv.initFile(PLOT_TITLE);

        startDataListener();

        //setFilterType("BANDPASS");
        initUI();
    }

    @Override
    public void onClick(View view) {
//        if (view.getId() == R.id.btn_init_file) {
//            initFile();
//        }
    }

    public void initUI() {
        dataSeries = new DynamicSeries(PLOT_TITLE);
        filterPlot = new XYPlot(this, PLOT_TITLE);
        initView(this);
        //Button init_file = (Button) findViewById(R.id.btn_init_file);
        // init_file.setOnClickListener(this);
    }

    public void initFile() {
        csv.writeFile(PLOT_TITLE);
    }

    public void initView(Context context) {


        FrameLayout frameLayout = findViewById(R.id.frame_layout_xyplot);

        filterPlot = new XYPlot(context, PLOT_TITLE);

        // Create dataSeries that will be drawn on plot (Y will be obtained from dataSource, x will be implicitly generated):
        dataSeries = new DynamicSeries(PLOT_TITLE);

        // Set X and Y domain
        filterPlot.setRangeBoundaries(PLOT_LOW_BOUND, PLOT_HIGH_BOUND, BoundaryMode.FIXED);
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
        filterPlot.getGraph().getBackgroundPaint().setColor(Color.rgb(114, 194, 241));

        // Remove gridlines
        filterPlot.getGraph().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
        filterPlot.getGraph().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlot.getGraph().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);
        filterPlot.getGraph().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlot.getGraph().getRangeOriginLinePaint().setColor(Color.TRANSPARENT);

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
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
    }

    public void setFilterType(String filterType) {
//        dataSeries.clear();

        switch (filterType) {

            case "LOWPASS":
                PLOT_LOW_BOUND = 600;
                PLOT_HIGH_BOUND = 1000;
                filterPlot.setRangeBoundaries(PLOT_LOW_BOUND, PLOT_HIGH_BOUND, BoundaryMode.FIXED);
                activeFilter = new Filter(samplingRate, "lowpass", 5, 35, 0);
                filtState = new double[4][activeFilter.getNB()];
                break;

            case "BANDPASS":
                PLOT_LOW_BOUND = -200;
                PLOT_HIGH_BOUND = 200;
                //filterPlot.setRangeBoundaries(PLOT_LOW_BOUND, PLOT_HIGH_BOUND, BoundaryMode.FIXED);
                activeFilter = new Filter(samplingRate, "bandpass", 5, 2, 35);
                filtState = new double[4][activeFilter.getNB()];
                break;

            case "HIGHPASS":
                PLOT_LOW_BOUND = -200;
                PLOT_HIGH_BOUND = 200;
                filterPlot.setRangeBoundaries(PLOT_LOW_BOUND, PLOT_HIGH_BOUND, BoundaryMode.FIXED);
                activeFilter = new Filter(samplingRate, "highpass", 2, 1, 0);
                filtState = new double[4][activeFilter.getNB()];
                break;
        }
    }

    public void startDataListener() {

        if (dataListener == null) {
            dataListener = new DataListener();
        }

        appState.connectedMuse.registerDataListener(dataListener, MuseDataPacketType.EEG);
    }

    private final class DataListener extends MuseDataListener {
        public double[] newData;

        // Filter variables
        public boolean filterOn = false;
        public Filter bandstopFilter;
        public double[][] bandstopFiltState;
        private int frameCounter = 0;

        // if connected Muse is a 2016 BLE version, init a bandstop filter to remove 60hz noise
        DataListener() {
            if (appState.connectedMuse.isLowEnergy()) {
                filterOn = true;
                bandstopFilter = new Filter(256, "bandstop", 5, notchFrequency - 5, notchFrequency + 5);
                bandstopFiltState = new double[4][bandstopFilter.getNB()];
            }
            newData = new double[4];
        }

        // Updates eegBuffer with new data from all 4 channels. Bandstop filter for 2016 Muse
        @Override
        public void receiveMuseDataPacket(final MuseDataPacket p, final Muse muse) {
            getEegChannelValues(newData, p);

            bandstopFiltState = bandstopFilter.transform(newData, bandstopFiltState);
            newData = bandstopFilter.extractFilteredSamples(bandstopFiltState);

            filtState = activeFilter.transform(newData, filtState);
            eegBuffer.update(activeFilter.extractFilteredSamples(filtState));

            frameCounter++;
            System.out.println(frameCounter);
            if (frameCounter % 15 == 0) {
                updatePlot();
            }
            if (isRecording) {
                csv.addDataToFile(activeFilter.extractFilteredSamples(filtState));
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
        dataSeries.addAll(eegBuffer.extractSingleChannelTransposedAsDouble(numEEGPoints, channelOfInterest - 1));

        // resets the 'points-since-dataSource-read' value
        eegBuffer.resetPts();

        filterPlot.redraw();
    }

}

/*public static final int BACKGROUND_COLOUR = Color.rgb(114, 194, 241);
    public static final int LINE_COLOUR = Color.rgb(255, 255, 255);
    public static XYPlot eegPlot;
    public static final int PLOT_LENGTH = 256 * 4;
    private static final String PLOT_TITLE = "Raw_EEG";
    public DynamicSeries dataSeries;
    private LineAndPointFormatter lineFormatter;
    //public  DataListener dataListener;
    //public OfflineDataListener offlineDataListener;
    public CircularBuffer eegBuffer = new CircularBuffer(220, 4);
    private int numEEGPoints;
    private Thread dataThread;
    private boolean isPlaying = true;

    // Bridged props
    // Default channelOfInterest = 1 (left ear)
    public int channelOfInterest = 1;
    public String offlineData = "";
    public boolean isRecording;

    // grab reference to global Muse
    MainActivity appState;
    private int notchFrequency = 60;*/

/*
        // initialize our XYPlot reference:
        eegPlot = (XYPlot) findViewById(R.id.plot);
        eegPlot = new XYPlot(this, "Raw EEG Plot");
        dataSeries = new DynamicSeries("dataSeries");

        // Set X and Y domain
        eegPlot.setRangeBoundaries(600, 1000, BoundaryMode.FIXED);
        eegPlot.setDomainBoundaries(0, PLOT_LENGTH, BoundaryMode.FIXED);

        // This is critical for being able to set the color of the plot
        //PixelUtils.init(getContext());

        // Create line formatter with set color
        lineFormatter = new FastLineAndPointRenderer.Formatter(LINE_COLOUR, null,  null);

        // Set line thickness
        lineFormatter.getLinePaint().setStrokeWidth(3);

        // add series to plot
        eegPlot.addSeries(dataSeries, lineFormatter);

        // Format plot layout
        //Remove margins, padding and border
        eegPlot.setPlotMargins(0, 0, 0, 0);
        eegPlot.setPlotPadding(0, 0, 0, 0);
        eegPlot.getBorderPaint().setColor(Color.WHITE);

        // Make plot background blue (including removing grid lines)
        XYGraphWidget graph = eegPlot.getGraph();
        graph.getBackgroundPaint().setColor(BACKGROUND_COLOUR);
        graph.getGridBackgroundPaint().setColor(Color.TRANSPARENT);
        graph.getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        graph.getDomainOriginLinePaint().setColor(Color.TRANSPARENT);
        graph.getRangeGridLinePaint().setColor(Color.TRANSPARENT);
        graph.getRangeOriginLinePaint().setColor(Color.TRANSPARENT);

        // Remove axis labels and values
        // Domain = X; Range = Y
        eegPlot.setDomainLabel(null);
        eegPlot.setRangeLabel(null);
        graph.getRangeGridLinePaint().setColor(Color.TRANSPARENT);
        graph.getRangeOriginLinePaint().setColor(Color.TRANSPARENT);
        graph.getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        graph.getDomainOriginLinePaint().setColor(Color.TRANSPARENT);

        // Remove extraneous elements
        eegPlot.getLayoutManager().remove(eegPlot.getLegend());

        // Set size of plot
        SizeMetric height = new SizeMetric(1, SizeMode.FILL);
        SizeMetric width = new SizeMetric(1, SizeMode.FILL);
        graph.setSize(new Size(height, width));

        // Set position of plot (should be tweaked in order to center chart position)
        graph.position(0, HorizontalPositioning.ABSOLUTE_FROM_LEFT.ABSOLUTE_FROM_LEFT,
                0, VerticalPositioning.ABSOLUTE_FROM_TOP);

        // Add plot to EEGGraph
        //this.addView(eegPlot, new FrameLayout.LayoutParams(
          //      FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        System.out.println("final de el metodo init");*/
