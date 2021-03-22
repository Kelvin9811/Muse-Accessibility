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
import com.yzhao.musecode.components.signal.Filter;
import com.choosemuse.libmuse.MuseDataListener;
import com.choosemuse.libmuse.MuseDataPacketType;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;


public class ChosseChannel extends Activity implements View.OnClickListener {

    public DataListener dataListener;
    MainActivity appState;

    MainActivity configurations;


    private LineAndPointFormatter lineFormatterChannelOne;
    private LineAndPointFormatter lineFormatterChannelTwo;
    private LineAndPointFormatter lineFormatterChannelTre;
    private LineAndPointFormatter lineFormatterChannelFour;

    private int notchFrequency = 14;
    private static final int PLOT_LENGTH = 255 * 3;
    public CircularBuffer eegBuffer = new CircularBuffer(220, 4);


    private static final String PLOT_TITLE = "Raw_EEG";
    private int PLOT_LOW_BOUND = 600;
    private int PLOT_HIGH_BOUND = 1100;
    public DynamicSeries dataSeriesChannelOne;
    public DynamicSeries dataSeriesChannelTwo;
    public DynamicSeries dataSeriesChannelTree;
    public DynamicSeries dataSeriesChannelFour;

    public XYPlot filterPlotChannelOne;
    public XYPlot filterPlotChannelTwo;
    public XYPlot filterPlotChannelTree;
    public XYPlot filterPlotChannelFour;

    public int samplingRate = 256;
    public Filter activeFilter;
    public double[][] filtState;

    public int channelOfInterest = 3;

    MainActivity TAG;

    MainActivity mainChannelOfInterest;

    private String typeofRecord = "largos";
    private double[][] extractedArray = new double[1020][4];

    private String[] extractedArrayString = new String[1020];

    private int secondsOfRecording = 510;

    EEGFileWriter configurationsFile = new EEGFileWriter(this, "Captura de datos");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel_configurations);
        configurationsFile.initFile();
        channelOfInterest = configurations.channelOfInterest;
        setNotchFrequency(notchFrequency);
        setFilterType();
        initUI();
        startDataListener();

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_save_channel_selected) {
            saveChannelOfInterest();
        }
        if (view.getId() == R.id.subtract_channel) {
            subtractChannel();
        }
        if (view.getId() == R.id.add_channel) {
            addChannel();
        }
    }

    public void initUI() {

        dataSeriesChannelOne = new DynamicSeries(PLOT_TITLE);
        dataSeriesChannelTwo = new DynamicSeries(PLOT_TITLE);
        dataSeriesChannelTree = new DynamicSeries(PLOT_TITLE);
        dataSeriesChannelFour = new DynamicSeries(PLOT_TITLE);

        filterPlotChannelOne = new XYPlot(this, PLOT_TITLE);
        filterPlotChannelTwo = new XYPlot(this, PLOT_TITLE);
        filterPlotChannelTree = new XYPlot(this, PLOT_TITLE);
        filterPlotChannelFour = new XYPlot(this, PLOT_TITLE);

        initViewChannel1(this);
        initViewChannel2(this);
        initViewChannel3(this);
        initViewChannel4(this);

        Button saveButton = (Button) findViewById(R.id.btn_save_channel_selected);
        saveButton.setOnClickListener(this);

        Button addChannelButton = (Button) findViewById(R.id.add_channel);
        addChannelButton.setOnClickListener(this);

        Button substractChannelButton = (Button) findViewById(R.id.subtract_channel);
        substractChannelButton.setOnClickListener(this);

        TextView channelOfInterest = findViewById(R.id.channel_of_interest);
        channelOfInterest.setText("" + (this.channelOfInterest+1));

    }

    public void initViewChannel1(Context context) {

        FrameLayout frameLayout = findViewById(R.id.frame_layout_xyplot_channel_1);

        filterPlotChannelOne = new XYPlot(context, PLOT_TITLE);

        // Create dataSeries that will be drawn on plot (Y will be obtained from dataSource, x will be implicitly generated):
        dataSeriesChannelOne = new DynamicSeries(PLOT_TITLE);

        // Set X and Y domain
        filterPlotChannelOne.setRangeBoundaries(PLOT_LOW_BOUND, PLOT_HIGH_BOUND, BoundaryMode.FIXED);
        filterPlotChannelOne.setDomainBoundaries(0, PLOT_LENGTH, BoundaryMode.FIXED);

        // Create line formatter with set color
        lineFormatterChannelOne = new FastLineAndPointRenderer.Formatter(Color.WHITE, null, null);

        // Set line thickness
        lineFormatterChannelOne.getLinePaint().setStrokeWidth(3);

        // Add line to plot
        filterPlotChannelOne.addSeries(dataSeriesChannelOne, lineFormatterChannelOne);

        // Format plot layout
        //Remove margins, padding and border
        filterPlotChannelOne.setPlotMargins(0, 0, 0, 0);
        filterPlotChannelOne.setPlotPadding(0, 0, 0, 0);
        filterPlotChannelOne.getBorderPaint().setColor(Color.WHITE);

        // Set plot background color
        filterPlotChannelOne.getGraph().getBackgroundPaint().setColor(Color.rgb(104, 176, 171));

        // Remove gridlines
        filterPlotChannelOne.getGraph().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
        filterPlotChannelOne.getGraph().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelOne.getGraph().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelOne.getGraph().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelOne.getGraph().getRangeOriginLinePaint().setColor(Color.TRANSPARENT);

        //filterPlot.getGraph().setLineLabelEdges(XYGraphWidget.Edge.LEFT, XYGraphWidget.Edge.BOTTOM);

        // Remove axis labels and values
        // Domain = X; Range = Y
        filterPlotChannelOne.setDomainLabel(null);
        filterPlotChannelOne.setRangeLabel(null);
        filterPlotChannelOne.getGraph().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelOne.getGraph().getRangeOriginLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelOne.getGraph().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelOne.getGraph().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);

        // Remove extraneous elements
        filterPlotChannelOne.getLayoutManager().remove(filterPlotChannelOne.getLegend());

        // Set size of plot
        SizeMetric height = new SizeMetric(1, SizeMode.FILL);
        SizeMetric width = new SizeMetric(1, SizeMode.FILL);
        filterPlotChannelOne.getGraph().setSize(new Size(height, width));

        // Set position of plot (should be tweaked in order to center chart position)
        filterPlotChannelOne.getGraph().position(0, HorizontalPositioning.ABSOLUTE_FROM_LEFT.ABSOLUTE_FROM_LEFT,
                0, VerticalPositioning.ABSOLUTE_FROM_TOP);

        // Add plot to FilterGraph
        frameLayout.addView(filterPlotChannelOne);
    }

    public void initViewChannel2(Context context) {

        FrameLayout frameLayout = findViewById(R.id.frame_layout_xyplot_channel_2);

        filterPlotChannelTwo = new XYPlot(context, PLOT_TITLE);

        // Create dataSeries that will be drawn on plot (Y will be obtained from dataSource, x will be implicitly generated):
        dataSeriesChannelTree = new DynamicSeries(PLOT_TITLE);

        // Set X and Y domain
        filterPlotChannelTwo.setRangeBoundaries(PLOT_LOW_BOUND, PLOT_HIGH_BOUND, BoundaryMode.FIXED);
        filterPlotChannelTwo.setDomainBoundaries(0, PLOT_LENGTH, BoundaryMode.FIXED);

        // Create line formatter with set color
        lineFormatterChannelTwo = new FastLineAndPointRenderer.Formatter(Color.WHITE, null, null);

        // Set line thickness
        lineFormatterChannelTwo.getLinePaint().setStrokeWidth(3);

        // Add line to plot
        filterPlotChannelTwo.addSeries(dataSeriesChannelTree, lineFormatterChannelTwo);

        // Format plot layout
        //Remove margins, padding and border
        filterPlotChannelTwo.setPlotMargins(0, 0, 0, 0);
        filterPlotChannelTwo.setPlotPadding(0, 0, 0, 0);
        filterPlotChannelTwo.getBorderPaint().setColor(Color.WHITE);

        // Set plot background color
        filterPlotChannelTwo.getGraph().getBackgroundPaint().setColor(Color.rgb(104, 176, 171));

        // Remove gridlines
        filterPlotChannelTwo.getGraph().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTwo.getGraph().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTwo.getGraph().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTwo.getGraph().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTwo.getGraph().getRangeOriginLinePaint().setColor(Color.TRANSPARENT);

        //filterPlot.getGraph().setLineLabelEdges(XYGraphWidget.Edge.LEFT, XYGraphWidget.Edge.BOTTOM);

        // Remove axis labels and values
        // Domain = X; Range = Y
        filterPlotChannelTwo.setDomainLabel(null);
        filterPlotChannelTwo.setRangeLabel(null);
        filterPlotChannelTwo.getGraph().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTwo.getGraph().getRangeOriginLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTwo.getGraph().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTwo.getGraph().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);

        // Remove extraneous elements
        filterPlotChannelTwo.getLayoutManager().remove(filterPlotChannelTwo.getLegend());

        // Set size of plot
        SizeMetric height = new SizeMetric(1, SizeMode.FILL);
        SizeMetric width = new SizeMetric(1, SizeMode.FILL);
        filterPlotChannelTwo.getGraph().setSize(new Size(height, width));

        // Set position of plot (should be tweaked in order to center chart position)
        filterPlotChannelTwo.getGraph().position(0, HorizontalPositioning.ABSOLUTE_FROM_LEFT.ABSOLUTE_FROM_LEFT,
                0, VerticalPositioning.ABSOLUTE_FROM_TOP);

        // Add plot to FilterGraph
        frameLayout.addView(filterPlotChannelTwo);
    }

    public void initViewChannel3(Context context) {

        FrameLayout frameLayout = findViewById(R.id.frame_layout_xyplot_channel_3);

        filterPlotChannelTree = new XYPlot(context, PLOT_TITLE);

        // Create dataSeries that will be drawn on plot (Y will be obtained from dataSource, x will be implicitly generated):
        dataSeriesChannelTree = new DynamicSeries(PLOT_TITLE);

        // Set X and Y domain
        filterPlotChannelTree.setRangeBoundaries(PLOT_LOW_BOUND, PLOT_HIGH_BOUND, BoundaryMode.FIXED);
        filterPlotChannelTree.setDomainBoundaries(0, PLOT_LENGTH, BoundaryMode.FIXED);

        // Create line formatter with set color
        lineFormatterChannelTre = new FastLineAndPointRenderer.Formatter(Color.WHITE, null, null);

        // Set line thickness
        lineFormatterChannelTre.getLinePaint().setStrokeWidth(3);

        // Add line to plot
        filterPlotChannelTree.addSeries(dataSeriesChannelTree, lineFormatterChannelTre);

        // Format plot layout
        //Remove margins, padding and border
        filterPlotChannelTree.setPlotMargins(0, 0, 0, 0);
        filterPlotChannelTree.setPlotPadding(0, 0, 0, 0);
        filterPlotChannelTree.getBorderPaint().setColor(Color.WHITE);

        // Set plot background color
        filterPlotChannelTree.getGraph().getBackgroundPaint().setColor(Color.rgb(104, 176, 171));

        // Remove gridlines
        filterPlotChannelTree.getGraph().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTree.getGraph().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTree.getGraph().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTree.getGraph().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTree.getGraph().getRangeOriginLinePaint().setColor(Color.TRANSPARENT);

        //filterPlot.getGraph().setLineLabelEdges(XYGraphWidget.Edge.LEFT, XYGraphWidget.Edge.BOTTOM);

        // Remove axis labels and values
        // Domain = X; Range = Y
        filterPlotChannelTree.setDomainLabel(null);
        filterPlotChannelTree.setRangeLabel(null);
        filterPlotChannelTree.getGraph().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTree.getGraph().getRangeOriginLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTree.getGraph().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelTree.getGraph().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);

        // Remove extraneous elements
        filterPlotChannelTree.getLayoutManager().remove(filterPlotChannelTree.getLegend());

        // Set size of plot
        SizeMetric height = new SizeMetric(1, SizeMode.FILL);
        SizeMetric width = new SizeMetric(1, SizeMode.FILL);
        filterPlotChannelTree.getGraph().setSize(new Size(height, width));

        // Set position of plot (should be tweaked in order to center chart position)
        filterPlotChannelTree.getGraph().position(0, HorizontalPositioning.ABSOLUTE_FROM_LEFT.ABSOLUTE_FROM_LEFT,
                0, VerticalPositioning.ABSOLUTE_FROM_TOP);

        // Add plot to FilterGraph
        frameLayout.addView(filterPlotChannelTree);
    }

    public void initViewChannel4(Context context) {

        FrameLayout frameLayout = findViewById(R.id.frame_layout_xyplot_channel_4);

        filterPlotChannelFour = new XYPlot(context, PLOT_TITLE);

        // Create dataSeries that will be drawn on plot (Y will be obtained from dataSource, x will be implicitly generated):
        dataSeriesChannelFour = new DynamicSeries(PLOT_TITLE);

        // Set X and Y domain
        filterPlotChannelFour.setRangeBoundaries(PLOT_LOW_BOUND, PLOT_HIGH_BOUND, BoundaryMode.FIXED);
        filterPlotChannelFour.setDomainBoundaries(0, PLOT_LENGTH, BoundaryMode.FIXED);

        // Create line formatter with set color
        lineFormatterChannelFour = new FastLineAndPointRenderer.Formatter(Color.WHITE, null, null);

        // Set line thickness
        lineFormatterChannelFour.getLinePaint().setStrokeWidth(3);

        // Add line to plot
        filterPlotChannelFour.addSeries(dataSeriesChannelFour, lineFormatterChannelFour);

        // Format plot layout
        //Remove margins, padding and border
        filterPlotChannelFour.setPlotMargins(0, 0, 0, 0);
        filterPlotChannelFour.setPlotPadding(0, 0, 0, 0);
        filterPlotChannelFour.getBorderPaint().setColor(Color.WHITE);

        // Set plot background color
        filterPlotChannelFour.getGraph().getBackgroundPaint().setColor(Color.rgb(104, 176, 171));

        // Remove gridlines
        filterPlotChannelFour.getGraph().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
        filterPlotChannelFour.getGraph().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelFour.getGraph().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelFour.getGraph().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelFour.getGraph().getRangeOriginLinePaint().setColor(Color.TRANSPARENT);

        //filterPlot.getGraph().setLineLabelEdges(XYGraphWidget.Edge.LEFT, XYGraphWidget.Edge.BOTTOM);

        // Remove axis labels and values
        // Domain = X; Range = Y
        filterPlotChannelFour.setDomainLabel(null);
        filterPlotChannelFour.setRangeLabel(null);
        filterPlotChannelFour.getGraph().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelFour.getGraph().getRangeOriginLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelFour.getGraph().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        filterPlotChannelFour.getGraph().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);

        // Remove extraneous elements
        filterPlotChannelFour.getLayoutManager().remove(filterPlotChannelFour.getLegend());

        // Set size of plot
        SizeMetric height = new SizeMetric(1, SizeMode.FILL);
        SizeMetric width = new SizeMetric(1, SizeMode.FILL);
        filterPlotChannelFour.getGraph().setSize(new Size(height, width));

        // Set position of plot (should be tweaked in order to center chart position)
        filterPlotChannelFour.getGraph().position(0, HorizontalPositioning.ABSOLUTE_FROM_LEFT.ABSOLUTE_FROM_LEFT,
                0, VerticalPositioning.ABSOLUTE_FROM_TOP);

        // Add plot to FilterGraph
        frameLayout.addView(filterPlotChannelFour);
    }


    @SuppressLint("SetTextI18n")
    public void addChannel() {
        if (channelOfInterest < 3)
            channelOfInterest = channelOfInterest + 1;
        int channelToShow = channelOfInterest + 1;
        TextView channelOfInterest = findViewById(R.id.channel_of_interest);
        channelOfInterest.setText("" + (channelToShow));
        System.out.println("canal " + channelToShow);
    }

    @SuppressLint("SetTextI18n")
    public void subtractChannel() {
        if (channelOfInterest > 0)
            channelOfInterest = channelOfInterest - 1;
        int channelToShow = channelOfInterest + 1;
        TextView channelOfInterest = findViewById(R.id.channel_of_interest);
        channelOfInterest.setText("" + (channelToShow));
        System.out.println("canal " + channelToShow);

    }

    public void saveChannelOfInterest() {


        configurations.channelOfInterest = channelOfInterest;

        configurationsFile.addLineToFile("" + configurations.channelOfInterest);
        configurationsFile.addLineToFile("" + configurations.detectionSensibility);
        configurationsFile.addLineToFile("" + configurations.probabilitySensibility);
        configurationsFile.addLineToFile("" + configurations.kNearestNeighbors);

        configurationsFile.writeConfigurationsFile();

        appState.connectedMuse.disconnect(false);
        appState.connectedMuse.unregisterAllListeners();
        appState.connectedMuse = null;
        Intent i = new Intent(this, UserConfigurations.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);

    }

    public void setFilterType() {
        activeFilter = new Filter(samplingRate, "bandstop", 5, 1, 6);
        filtState = new double[4][activeFilter.getNB()];
    }

    public void setNotchFrequency(int notchFrequency) {
        this.notchFrequency = notchFrequency;
        if (dataListener != null) {
            dataListener.updateFilter(notchFrequency);
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
        }

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
        if (dataSeriesChannelOne.size() >= PLOT_LENGTH ||
                dataSeriesChannelTwo.size() >= PLOT_LENGTH ||
                dataSeriesChannelTree.size() >= PLOT_LENGTH ||
                dataSeriesChannelFour.size() >= PLOT_LENGTH) {
            dataSeriesChannelOne.remove(numEEGPoints);
            dataSeriesChannelTwo.remove(numEEGPoints);
            dataSeriesChannelTree.remove(numEEGPoints);
            dataSeriesChannelFour.remove(numEEGPoints);
        }

        dataSeriesChannelOne.addAll(eegBuffer.extractSingleChannelTransposedAsDouble(numEEGPoints, 0));
        dataSeriesChannelTwo.addAll(eegBuffer.extractSingleChannelTransposedAsDouble(numEEGPoints, 1));
        dataSeriesChannelTree.addAll(eegBuffer.extractSingleChannelTransposedAsDouble(numEEGPoints, 2));
        dataSeriesChannelFour.addAll(eegBuffer.extractSingleChannelTransposedAsDouble(numEEGPoints, 3));

        eegBuffer.resetPts();
        filterPlotChannelOne.redraw();
        filterPlotChannelTwo.redraw();
        filterPlotChannelTree.redraw();
        filterPlotChannelFour.redraw();

    }


}
