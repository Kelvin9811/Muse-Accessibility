package com.yzhao.musecode.components.graphs;

import android.graphics.Canvas;

import com.androidplot.Plot;
import com.androidplot.PlotListener;
import com.androidplot.xy.XYSeries;
import com.yzhao.musecode.components.csv.EEGFileWriter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;


// AndroidPlot class that stores dataSource to be plotted. getX() and getY() are called by XYPlot to to draw graph
// This implementation only stores Y values, with X values implicitily determined by the index of the dataSource in the LinkedList
public class DynamicSeries implements XYSeries, PlotListener {

    // -------------------------------------------------------------
    // Variables

    private String title;
    private volatile LinkedList<Number> yVals = new LinkedList<Number>();
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    private static final String PLOT_TITLE = "Raw_EEG";

    // -------------------------------------------------------------
    // Constructor

    public DynamicSeries(String title) {
        this.title = title;
    }

    // --------------------------------------------------------------
    // Data Series Methods

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int size() {
        return yVals != null ? yVals.size() : 0;
    }

    @Override
    public Number getX(int index) {
        return index;
    }

    @Override
    public Number getY(int index) {
        return yVals.get(index);
    }


    public void addLast(Number y) {
        yVals.addLast(y);
    }

    public void addAll(Double[] y) {
        yVals.addAll(Arrays.asList(y));
    }

    public void clear(Double[] y) {
        yVals.clear();
    }

    public void removeFirst() {
        yVals.removeFirst();
    }

    public void remove(int nbsamples) {
        for (int i = 0; i < nbsamples; i++) {
            yVals.removeFirst();
        }
    }

    public void clear() {
        yVals.clear();
    }

    public void writeFile(EEGFileWriter csv) {
        System.out.println(yVals.size());
        for (int i = 0; i < yVals.size(); i++) {
            csv.addLineToFile("" + yVals.get(i));
        }
    }

    public int getAverage() {
        int dataSeriesSize = yVals.size();
        int sum = 0;
        for (int i = dataSeriesSize - 15; i < dataSeriesSize; i++) {
            sum = sum + yVals.get(i).intValue();
        }
        return sum / 15;
    }

    public float[] getPSample() {
        final float[] pSample = new float[yVals.size()];

        for (int i = 0; i < yVals.size(); i++) {
            pSample[i] = Float.parseFloat(String.valueOf(yVals.get(i)));
        }
        return pSample;
    }

    public List<Number> getAll() {
        return yVals.subList(0, yVals.size());
    }


    // ------------------------------------------------------------
    // PlotListener Methods

    @Override
    public void onBeforeDraw(Plot source, Canvas canvas) {
        lock.readLock().lock();
    }

    @Override
    public void onAfterDraw(Plot source, Canvas canvas) {
        lock.readLock().unlock();
    }

}