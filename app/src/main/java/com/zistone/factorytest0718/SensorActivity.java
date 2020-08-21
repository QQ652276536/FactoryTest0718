package com.zistone.factorytest0718;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class SensorActivity extends BaseActivity {

    private static final String TAG = "SensorActivity";

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_sensor);
        SetBaseContentView(R.layout.activity_sensor);
    }
}
