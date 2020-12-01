package com.yzhao.musecode.components.connector;

import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseConnectionListener;
import com.choosemuse.libmuse.MuseConnectionPacket;
import com.yzhao.musecode.MainActivity;

import java.lang.ref.WeakReference;

class ConnectionListener extends MuseConnectionListener {
    final WeakReference<MainActivity> activityRef;

    ConnectionListener(final WeakReference<MainActivity> activityRef) {
        this.activityRef = activityRef;
    }

    @Override
    public void receiveMuseConnectionPacket(final MuseConnectionPacket p, final Muse muse) {
        //activityRef.get().receiveMuseConnectionPacket(p, muse);
    }
}