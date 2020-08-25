package com.zistone.factorytest0718;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Vibrator;

public class ShakeActivity extends BaseActivity {

    private static final String TAG = "ShakeActivity";

    private Vibrator _vibrator;
    private long[] _patter = {0, 500, 500, 0};

    @Override
    protected void onResume() {
        super.onResume();
        _vibrator.vibrate(_patter, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        _vibrator.cancel();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_shake);
        SetBaseContentView(R.layout.activity_shake);
        _vibrator = (Vibrator) this.getSystemService(this.VIBRATOR_SERVICE);
    }
}