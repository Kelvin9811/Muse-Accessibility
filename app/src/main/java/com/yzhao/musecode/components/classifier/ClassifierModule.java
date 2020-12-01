package com.yzhao.musecode.components.classifier;

import android.os.Handler;
import android.os.HandlerThread;

import com.choosemuse.libmuse.Eeg;
import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseArtifactPacket;
import com.choosemuse.libmuse.MuseDataListener;
import com.choosemuse.libmuse.MuseDataPacket;
import com.choosemuse.libmuse.MuseDataPacketType;
import com.yzhao.musecode.MainActivity;


import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Bridged native module for interacting with the headband for the classifier (and noise detection)
 *
 */

public class ClassifierModule  implements BufferListener {


    @Override
    public void getEpoch(double[][] buffer) {

    }





}
