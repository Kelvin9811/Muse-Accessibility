package com.yzhao.musecode.components.connector;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;

import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseConnectionListener;
import com.choosemuse.libmuse.MuseConnectionPacket;
import com.choosemuse.libmuse.MuseManagerAndroid;
import com.yzhao.musecode.MainActivity;
import com.yzhao.musecode.MuseConnection;

import java.lang.ref.WeakReference;


public class ConnectorModule{


    private boolean isBluetoothEnabled;
    private MuseManagerAndroid manager;
    MainActivity appState;

    public boolean checkBluetoothEnabled() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        isBluetoothEnabled = bluetoothAdapter.isEnabled();
        return isBluetoothEnabled;
    }

    static class ConnectionListener extends MuseConnectionListener {
        final WeakReference<MuseConnection> activityRef;
        ConnectionListener(final WeakReference<MuseConnection> activityRef) {
            this.activityRef = activityRef;
        }
        @Override
        public void receiveMuseConnectionPacket(final MuseConnectionPacket p, final Muse muse) {
            activityRef.get().receiveMuseConnectionPacket(p, muse);
        }
    }



}