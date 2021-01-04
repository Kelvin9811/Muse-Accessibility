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

import com.choosemuse.libmuse.ConnectionState;
import com.choosemuse.libmuse.Muse;
import com.choosemuse.libmuse.MuseArtifactPacket;
import com.choosemuse.libmuse.MuseConnectionListener;
import com.choosemuse.libmuse.MuseConnectionPacket;
import com.choosemuse.libmuse.MuseDataListener;
import com.choosemuse.libmuse.MuseDataPacket;
import com.choosemuse.libmuse.MuseListener;
import com.choosemuse.libmuse.MuseManagerAndroid;

import java.lang.ref.WeakReference;
import java.util.List;

public class MuseConnection extends AppCompatActivity implements View.OnClickListener {

    private MuseManagerAndroid manager;
    private Muse muse;
    private MuseConnection.ConnectionListener connectionListener;
    private final Handler handler = new Handler();
    private ArrayAdapter<String> spinnerAdapter;
    private WeakReference<MuseConnection> weakActivity = new WeakReference<>(this);
    private Button connectButton;
    private Button continueButton;
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
        manager.setMuseListener(new MuseConnection.MuseL(weakActivity));
        manager.startListening();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_connect) {
            connectDisconnect();
        } else if (view.getId() == R.id.btn_refresh) {
            refreshMuseList();
        } else if (view.getId() == R.id.btn_continue) {
            Intent intent = new Intent(this, EEGPlot.class);
            startActivity(intent);
        }
    }

    private void connectDisconnect() {
        if (appState.connectedMuse != null) {
            muse.disconnect(false);
            appState.connectedMuse.disconnect(false);
            appState.connectedMuse.unregisterAllListeners();
            appState.connectedMuse = null;
            connectButton.setText("Conectar");
            continueButton.setEnabled(false);
            continueButton.setBackground(getResources().getDrawable(R.drawable.disable_button));
            final TextView statusText = findViewById(R.id.txt_connection_state);
            statusText.setText("Desconectado");
        } else {
            manager.stopListening();
            List<Muse> availableMuses = manager.getMuses();
            Spinner musesSpinner = findViewById(R.id.muses_conected_spinner);
            if (availableMuses.size() < 1 || musesSpinner.getAdapter().getCount() < 1) {
                Log.w(String.valueOf(TAG), "No hay dispositivos para conectar");
            } else {
                muse = availableMuses.get(musesSpinner.getSelectedItemPosition());
                muse.unregisterAllListeners();
                muse.registerConnectionListener(connectionListener);
                muse.runAsynchronously();
            }
        }
    }

    public void museListChanged() {
        final List<Muse> list = manager.getMuses();
        spinnerAdapter.clear();
        for (Muse m : list) {
            spinnerAdapter.add(m.getName() + " - " + m.getMacAddress());
        }
    }

    public void refreshMuseList() {
        manager.stopListening();
        manager.startListening();
    }

    private void initUI() {
        setContentView(R.layout.muse_conection);
        Button refreshButton = (Button) findViewById(R.id.btn_refresh);
        refreshButton.setOnClickListener(this);
        connectButton = (Button) findViewById(R.id.btn_connect);
        connectButton.setOnClickListener(this);
        continueButton = (Button) findViewById(R.id.btn_continue);
        continueButton.setOnClickListener(this);
        continueButton.setEnabled(false);
        continueButton.setBackground(getResources().getDrawable(R.drawable.disable_button));

        spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        Spinner musesSpinner = (Spinner) findViewById(R.id.muses_conected_spinner);
        musesSpinner.setAdapter(spinnerAdapter);
        spinnerAdapter.add("Lista de dispositivos disponibles");
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
                switch (status) {
                    case "UNKNOWN":
                        statusText.setText("Desconocido");
                        break;
                    case "CONNECTED":
                        statusText.setText("Conectado");
                        break;
                    case "CONNECTING":
                        statusText.setText("Conectando");
                        break;
                    case "DISCONNECTED":
                        statusText.setText("Desconectado");
                        break;
                    case "NEEDS_UPDATE":
                        statusText.setText("Requiere actualización");
                        break;
                    default:
                        statusText.setText("Desconocido");
                }
                if (current == ConnectionState.CONNECTED) {

                    appState.connectedMuse = muse;
                    appState.connectedMuse = muse;
                    connectButton.setText("Desconectar");
                    continueButton.setEnabled(true);
                    continueButton.setBackground(getResources().getDrawable(R.drawable.enable_button));


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
}
