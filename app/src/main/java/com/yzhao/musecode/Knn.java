package com.yzhao.musecode;

import com.yzhao.musecode.components.graphs.DynamicSeries;

import java.util.List;

public class Knn {

    float[] originalSignalShortBlink;
    float[] originalSignalLongBlink;
    float[] originalSignalNoneBlink;
    int chanelOfInteres = 2;
    int sampleLength = 510;
    double umbral = 0.7;

    private List data;
    private DTW dtw = new DTW();

    public Knn(float[] originalSignalShortBlink, float[] originalSignalLongBlink, float[] originalSignalNoneBlink, double umbral) {

        this.originalSignalShortBlink = originalSignalShortBlink;
        this.originalSignalLongBlink = originalSignalLongBlink;
        this.originalSignalNoneBlink = originalSignalNoneBlink;
        this.umbral = umbral * 0.01;

    }

    public String evaluateBlink(DynamicSeries dataSeries) {

        double[][] distancesBlink = new double[150][150];
        float[] pSample = dataSeries.getPSample();
        System.out.println("-------------------------");
        System.out.println(originalSignalShortBlink.length);

        getSampleRange(originalSignalShortBlink, 0);
        System.out.println("-------------------------");
        for (int i = 0; i < 15; i++) {
            System.out.println("-------------------------");
            System.out.println("FOR");
            System.out.println("-------------------------");

            distancesBlink[1][i] = dtw.compute(pSample, getSampleRange(originalSignalShortBlink, i)).getDistance();
            distancesBlink[2][i] = 0;

            distancesBlink[1][i + 15] = dtw.compute(pSample, getSampleRange(originalSignalLongBlink, i)).getDistance();
            distancesBlink[2][i + 15] = 1;

            distancesBlink[1][i + 30] = dtw.compute(pSample, getSampleRange(originalSignalNoneBlink, i)).getDistance();
            distancesBlink[2][i + 30] = 2;
        }


        double temp = 0;
        for (int j = 0; j < 43; j++) {
            for (int i = 0; i < 43; i++) {
                if (distancesBlink[1][i] > distancesBlink[1][i + 1]) {
                    temp = distancesBlink[1][i];
                    distancesBlink[1][i] = distancesBlink[1][i + 1];
                    distancesBlink[1][i + 1] = temp;
                    distancesBlink[1][i + 1] = temp;
                    temp = distancesBlink[2][i];
                    distancesBlink[2][i] = distancesBlink[2][i + 1];
                    distancesBlink[2][i + 1] = temp;
                    distancesBlink[2][i + 1] = temp;
                }
            }
        }
        int k = 10;

        double numberOfShortBlinks = 0;
        double numberOfLongBlinks = 0;
        double numberOfNoneBlinks = 0;

        for (int i = 0; i < k; i++) {
            if (distancesBlink[2][i] == 0)
                numberOfShortBlinks = numberOfShortBlinks + 1 * 0.1;
            if (distancesBlink[2][i] == 1)
                numberOfLongBlinks = numberOfLongBlinks + 1 * 0.1;
            if (distancesBlink[2][i] == 2)
                numberOfNoneBlinks = numberOfNoneBlinks + 1 * 0.1;
        }
        System.out.println("Numero de parpadeos cortos: " + numberOfShortBlinks);
        System.out.println("Numero de parpadeos largos: " + numberOfLongBlinks);
        System.out.println("Numero de parpadeos ninguno: " + numberOfNoneBlinks);
        System.out.println("Numero umbral: " + umbral);

        if (numberOfNoneBlinks > umbral)
            return "";
        else if (numberOfLongBlinks >umbral)
            return "-";
        else if (numberOfShortBlinks > umbral)
            return ".";

        return "";


    }

    public float[] getPSample(double[][] doubleList, int chanelOfInteres) {

        float[] pSample = new float[15300];
        for (int i = 0; i < 15300; i++) {
            pSample[i] = (float) doubleList[i][chanelOfInteres];
        }
        return pSample;
    }

    public float[] getSampleRange(float[] signal, int sampleNumber) {

        float[] sample = new float[sampleLength];

        int sampleStart = (sampleNumber * sampleLength) - sampleLength;
        if (sampleStart < 0)
            sampleStart = 0;

        for (int i = 0; i < sampleLength; i++) {
            sample[i] = signal[i + sampleStart];
        }

        return sample;
    }
}
