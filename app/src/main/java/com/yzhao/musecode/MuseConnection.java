package com.yzhao.musecode;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
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
import com.choosemuse.libmuse.ConnectionState;
import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseArtifactPacket;
import com.choosemuse.libmuse.MuseConnectionListener;
import com.choosemuse.libmuse.MuseConnectionPacket;
import com.choosemuse.libmuse.MuseDataListener;
import com.choosemuse.libmuse.MuseDataPacket;
import com.choosemuse.libmuse.MuseDataPacketType;
import com.choosemuse.libmuse.MuseListener;
import com.choosemuse.libmuse.MuseManagerAndroid;
import com.choosemuse.libmuse.MuseVersion;
import com.yzhao.musecode.components.connector.ConnectorModule;
import com.yzhao.musecode.components.graphs.EEGGraph;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.choosemuse.libmuse.MuseDataPacketType.ACCELEROMETER;

public class MuseConnection extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "MuseCode";
    private MuseManagerAndroid manager;
    private Muse muse;
    private MuseConnection.ConnectionListener connectionListener;
    private MuseDataListener dataListener;
    private final double[] accelBuffer = new double[3];
    private boolean accelStale;

    private boolean blink = false;
    private boolean lastBlink = false;
    private boolean jawClench = false;
    private boolean lastJawClench = false;

    private final Handler handler = new Handler();

    private int EMGcounter = 0;
    private boolean EMGflag = false;
    private ArrayAdapter<String> spinnerAdapter;
    private boolean dataTransmission = true;
    private final float LEFT_THRESHOLD = -0.25f;
    private final float FRONT_THRESHOLD = 0.35f;
    private AccelerometerData nodQ = new AccelerometerData(FRONT_THRESHOLD);
    private AccelerometerData backspaceQ = new AccelerometerData(LEFT_THRESHOLD);
    private StringBuilder translation = new StringBuilder();
    private SignalQueue sigQ = new SignalQueue();


    private TextView blinkView, jawView;

    private int blinkCount = 0;
    private int jawCount = 0;
    private int nodCount = 0;
    private int tiltCount = 0;

    private MorseDictionary dict = new MorseDictionary();
    private TextView morseTextView;
    private ArrayList<String> morseSequences;

    EMGData jawData = new EMGData();
    EMGData blinkData = new EMGData();

    private int packetCounter = 0;

    private TextView translateTextView;

    private ConnectorModule connectorModule = new ConnectorModule();

    private final Runnable tickUi = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(tickUi, 1000 / 60);
        }
    };
    MainApplication appState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startConnection();
        initUI();
    }

    public void startConnection() {
        manager = MuseManagerAndroid.getInstance();
        manager.setContext(this);
        WeakReference<MuseConnection> weakActivity = new WeakReference<>(this);
        connectionListener = new MuseConnection.ConnectionListener(weakActivity);
        dataListener = new MuseConnection.DataListener(weakActivity);
        manager.setMuseListener(new MuseConnection.MuseL(weakActivity));
        ensurePermissions();
        handler.post(tickUi);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_connect) {
            manager.stopListening();

            List<Muse> availableMuses = manager.getMuses();
            Spinner musesSpinner = (Spinner) findViewById(R.id.muses_conected_spinner);

            if (availableMuses.size() < 1 || musesSpinner.getAdapter().getCount() < 1) {
                Log.w(TAG, "There is nothing to connect to");
            } else {
                muse = availableMuses.get(musesSpinner.getSelectedItemPosition());
                muse.unregisterAllListeners();
                muse.registerConnectionListener(connectionListener);
                muse.registerDataListener(dataListener, MuseDataPacketType.ARTIFACTS);
                muse.registerDataListener(dataListener, MuseDataPacketType.ACCELEROMETER);
                muse.runAsynchronously();
            }
        } else if (view.getId() == R.id.btn_refresh) {
            refreshMuseList();
        } else if (view.getId() == R.id.btn_disconnect) {
            disconnectDevice();
        } else if (view.getId() == R.id.btn_continue) {
            Intent intent = new Intent(this, EEGPlot.class);
            startActivity(intent);
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

    public void receiveMuseDataPacket(final MuseDataPacket p, final Muse muse) {

        final long n = p.valuesSize();
        if (p.packetType() == ACCELEROMETER) {
            assert (accelBuffer.length >= n);
            getAccelValues(p);
            accelStale = true;
        }
    }

    public void receiveMuseArtifactPacket(final MuseArtifactPacket p, final Muse muse) {

        // System.out.println(p.toString());
        /*if (packetCounter == 0) { // packetCounter is reset after 5 ignored packets
            blink = p.getBlink();
            if (blink)
                System.out.println("True");
            else
                System.out.println("False");
            jawClench = p.getJawClench();
            jawData.add(jawClench);
            blinkData.add(blink);

        } else {
            packetCounter = (packetCounter + 1) % 5; // wait for 5 packets before scanning again
        }*/
    }


    private void getAccelValues(MuseDataPacket p) {
        System.out.println((float) (p.getAccelerometerValue(Accelerometer.X)));
        nodQ.add((float) (p.getAccelerometerValue(Accelerometer.X)));
        backspaceQ.add((float) (p.getAccelerometerValue(Accelerometer.Y)));
    }

    public void receiveMuseConnectionPacket(final MuseConnectionPacket p, final Muse muse) {

        final ConnectionState current = p.getCurrentConnectionState();

        final String status = p.getPreviousConnectionState() + " -> " + current;
        // Update the UI with the change in connection state.
        handler.post(new Runnable() {
            @Override
            public void run() {

                final TextView statusText = (TextView) findViewById(R.id.txt_connection_state);
                statusText.setText(status);
                if (current == ConnectionState.CONNECTED) {
                    appState.connectedMuse = muse;
                }
            }
        });

        if (current == ConnectionState.DISCONNECTED) {
            // We have disconnected from the headband, so set our cached copy to null.
            this.muse = null;
            if (appState.connectedMuse != null) {
                appState.connectedMuse.disconnect(true);
                appState.connectedMuse.unregisterAllListeners();
            }
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

            activityRef.get().receiveMuseArtifactPacket(p, muse);
        }
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


}
