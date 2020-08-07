package com.zistone.factorytest0718;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class TestTestActivity extends BaseActivity {
    private static final String TAG = "TestTestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_test_test);
        SetBaseContentView(R.layout.activity_test_test);
        _btnPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "继承Pass按钮后的点击事件触发");
            }
        });
        _btnFail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"继承Fail按钮后的点击事件触发");
            }
        });
    }
}
