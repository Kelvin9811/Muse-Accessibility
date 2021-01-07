package com.yzhao.musecode;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.choosemuse.libmuse.Muse;

public class  HeadbandInstructions extends AppCompatActivity implements View.OnClickListener {

    @Override
    public void onClick(View view) {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.headband_instructions);
        initUI();
    }

    public void initUI(){
        Button start_connection = (Button) findViewById(R.id.btn_continue_headband_instructions);
        start_connection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startStreamActivity();
            }
        });
    }

    public void startStreamActivity() {
        Intent intent = new Intent(this, EEGPlot.class);
        startActivity(intent);
    }
}