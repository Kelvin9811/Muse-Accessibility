package com.yzhao.musecode;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.choosemuse.libmuse.Accelerometer;
import com.choosemuse.libmuse.Battery;
import com.choosemuse.libmuse.ConnectionState;
import com.choosemuse.libmuse.Eeg;
import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseArtifactPacket;
import com.choosemuse.libmuse.MuseConnectionListener;
import com.choosemuse.libmuse.MuseConnectionPacket;
import com.choosemuse.libmuse.MuseDataListener;
import com.choosemuse.libmuse.MuseDataPacket;
import com.choosemuse.libmuse.MuseDataPacketType;
import com.choosemuse.libmuse.MuseListener;
import com.choosemuse.libmuse.MuseManagerAndroid;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.choosemuse.libmuse.MuseDataPacketType.ACCELEROMETER;

public class MuseConnection extends AppCompatActivity implements View.OnClickListener {


    private MuseManagerAndroid manager;
    private Muse muse;
    private MuseConnection.ConnectionListener connectionListener;
    private MuseDataListener dataListener;
    private final double[] accelBuffer = new double[3];
    private final Handler handler = new Handler();
    private ArrayAdapter<String> spinnerAdapter;
    private final float LEFT_THRESHOLD = -0.25f;
    private final float FRONT_THRESHOLD = 0.35f;
    private AccelerometerData nodQ = new AccelerometerData(FRONT_THRESHOLD);
    private AccelerometerData backspaceQ = new AccelerometerData(LEFT_THRESHOLD);
    private WeakReference<MuseConnection> weakActivity = new WeakReference<>(this);


    private boolean blink = false;
    private boolean jawClench = false;
    private boolean lastJawClench = false;


    private int EMGcounter = 0;
    private boolean EMGflag = false;
    private boolean dataTransmission = true;
    private StringBuilder translation = new StringBuilder();
    private SignalQueue sigQ = new SignalQueue();


    private TextView blinkView, jawView;

    private int blinkCount = 0;
    private int jawCount = 0;
    private int nodCount = 0;
    private int tiltCount = 0;

    private MorseDictionary dict = new MorseDictionary();
    private TextView morseTextView;

    EMGData jawData = new EMGData();
    EMGData blinkData = new EMGData();

    private int packetCounter = 0;


    MainActivity appState;
    MainActivity TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ensurePermissions();
        startConnection();
        initUI();
    }

    public void startConnection() {
        manager = MuseManagerAndroid.getInstance();
        manager.setContext(this);
        connectionListener = new MuseConnection.ConnectionListener(weakActivity);
        dataListener = new MuseConnection.DataListener(weakActivity);
        manager.setMuseListener(new MuseConnection.MuseL(weakActivity));
        manager.startListening();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_connect) {
            connectToSelectedDevice();
        } else if (view.getId() == R.id.btn_refresh) {
            refreshMuseList();
        } else if (view.getId() == R.id.btn_disconnect) {
            disconnectDevice();
        } else if (view.getId() == R.id.btn_continue) {
            Intent intent = new Intent(this, EEGPlot.class);
            startActivity(intent);
        }
    }

    private void connectToSelectedDevice() {
        manager.stopListening();
        List<Muse> availableMuses = manager.getMuses();
        Spinner musesSpinner = findViewById(R.id.muses_conected_spinner);
        if (availableMuses.size() < 1 || musesSpinner.getAdapter().getCount() < 1) {
            Log.w(String.valueOf(TAG), "No hay dispositivos para conectar");
        } else {
            muse = availableMuses.get(musesSpinner.getSelectedItemPosition());
            muse.unregisterAllListeners();
            muse.registerConnectionListener(connectionListener);
            //muse.registerDataListener(dataListener, MuseDataPacketType.ARTIFACTS);
           // muse.registerDataListener(dataListener, MuseDataPacketType.ACCELEROMETER);
            muse.runAsynchronously();
        }
    }

    public void museListChanged() {
        final List<Muse> list = manager.getMuses();
        System.out.println(list.size());
        spinnerAdapter.clear();
        for (Muse m : list) {
            spinnerAdapter.add(m.getName() + " - " + m.getMacAddress());
        }
    }

    public void refreshMuseList() {
        manager.stopListening();
        manager.startListening();
    }

    public void disconnectDevice() {
        if (appState.connectedMuse != null) {
            muse.disconnect(false);
            appState.connectedMuse.disconnect(false);
            appState.connectedMuse.unregisterAllListeners();
        }
    }


    private void getAccelValues(MuseDataPacket p) {
        double[] newData = new double[0];

        newData[0] = p.getEegChannelValue(Eeg.EEG1);


        System.out.println(newData[0]);
        nodQ.add((float) (p.getAccelerometerValue(Accelerometer.X)));
        backspaceQ.add((float) (p.getAccelerometerValue(Accelerometer.Y)));
    }

    private void initUI() {
        setContentView(R.layout.muse_conection);
        Button refreshButton = (Button) findViewById(R.id.btn_refresh);
        refreshButton.setOnClickListener(this);
        Button connectButton = (Button) findViewById(R.id.btn_connect);
        connectButton.setOnClickListener(this);
        Button disconnectButton = (Button) findViewById(R.id.btn_disconnect);
        disconnectButton.setOnClickListener(this);
        Button continueButton = (Button) findViewById(R.id.btn_continue);
        continueButton.setOnClickListener(this);
        spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        Spinner musesSpinner = (Spinner) findViewById(R.id.muses_conected_spinner);
        musesSpinner.setAdapter(spinnerAdapter);
    }

    private void ensurePermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            DialogInterface.OnClickListener buttonListener =
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            ActivityCompat.requestPermissions(MuseConnection.this,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    0);
                        }
                    };

            AlertDialog introDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.permission_dialog_title)
                    .setMessage(R.string.permission_dialog_description)
                    .setPositiveButton(R.string.permission_dialog_understand, buttonListener)
                    .create();
            introDialog.show();
        }
    }

    public void receiveMuseConnectionPacket(final MuseConnectionPacket p, final Muse muse) {

        final ConnectionState current = p.getCurrentConnectionState();
        final String status = current.name();

        handler.post(new Runnable() {
            @Override
            public void run() {
                final TextView statusText = findViewById(R.id.txt_connection_state);
                statusText.setText(status);
                if (current == ConnectionState.CONNECTED) {
                    appState.connectedMuse = muse;
                }
            }
        });

        if (current == ConnectionState.DISCONNECTED) {
            this.muse = null;
            if (appState.connectedMuse != null) {
                appState.connectedMuse.disconnect(true);
                appState.connectedMuse.unregisterAllListeners();
            }
        }
    }

    public void receiveMuseDataPacket(final MuseDataPacket p, final Muse muse) {
        //double[] newData = new double[0];
        //p.getBatteryValue(Battery )
        //newData[1] = p.getEegChannelValue(Eeg.EEG2);
        //newData[2] = p.getEegChannelValue(Eeg.EEG3);
        //newData[3] = p.getEegChannelValue(Eeg.EEG4);

        nodQ.add((float) (p.getAccelerometerValue(Accelerometer.X)));
        backspaceQ.add((float) (p.getAccelerometerValue(Accelerometer.Y)));

    }

    public void receiveMuseArtifactPacket(final MuseArtifactPacket p, final Muse muse) {

        // System.out.println(p.toString());
        if (packetCounter == 0) { // packetCounter is reset after 5 ignored packets
            blink = p.getBlink();
            if (!blink)
                System.out.println("True");

//            else
//                System.out.println("False");
            jawClench = p.getJawClench();
            jawData.add(jawClench);
            blinkData.add(blink);

        } else {

            packetCounter = (packetCounter + 1) % 5; // wait for 5 packets before scanning again
            System.out.println("packetCounter--"+packetCounter);
        }
    }


    static class MuseL extends MuseListener {
        final WeakReference<MuseConnection> activityRef;

        MuseL(final WeakReference<MuseConnection> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        public void museListChanged() {
            activityRef.get().museListChanged();
        }
    }

    static class DataListener extends MuseDataListener {
        final WeakReference<MuseConnection> activityRef;

        DataListener(final WeakReference<MuseConnection> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        public void receiveMuseDataPacket(final MuseDataPacket p, final Muse muse) {
            activityRef.get().receiveMuseDataPacket(p, muse);
        }

        @Override
        public void receiveMuseArtifactPacket(final MuseArtifactPacket p, final Muse muse) {
            //activityRef.get().receiveMuseArtifactPacket(p, muse);
        }
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


/*
com.yzhao.musecode E/MUSE: CHECK fail at libmuse/src/interfaces/implementation/muse_data_packet.h:63 (in virtual double interaxon::MuseDataPacketImpl::get_eeg_channel_value(interaxon::bridge::Eeg)): correctType
com.yzhao.musecode A/libc: Fatal signal 6 (SIGABRT), code -6 in tid 5339 (.yzhao.musecode)

* */