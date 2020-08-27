package com.zistone.factorytest0718;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * 用来测试一些东西的，没有任何实际功能
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class TestTestActivity extends BaseActivity {

    //加载本地库
    static {
        System.loadLibrary("native-lib");
    }

    private static final String TAG = "TestTestActivity";

    private TextView _txt;

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
                Log.i(TAG, "继承Fail按钮后的点击事件触发");
            }
        });
        _txt = findViewById(R.id.txt_test_test);
        _txt.setText(stringFromJNI());
    }

    public native String stringFromJNI();

}
