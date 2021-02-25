package com.yzhao.musecode;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.choosemuse.libmuse.Muse;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static Muse connectedMuse;
    public final String TAG = "MuseCode";

    @Override
    public void onClick(View view) {
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.introduction);
        initUI();
    }

    public void initUI() {
        Button start_record = (Button) findViewById(R.id.btn_start_conection);
        start_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecordActivity();
            }
        });

        Button start_control = (Button) findViewById(R.id.btn_start_device_control);
        start_control.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startControlActivity();
            }
        });
    }

    public void startRecordActivity() {
        Intent intent = new Intent(this, MuseConnection.class);
        intent.putExtra("FLOW", "RECORD");
        startActivity(intent);
    }

    public void startControlActivity() {
        Intent intent = new Intent(this, MuseConnection.class);
        intent.putExtra("FLOW", "CONTROL");
        startActivity(intent);
    }

}