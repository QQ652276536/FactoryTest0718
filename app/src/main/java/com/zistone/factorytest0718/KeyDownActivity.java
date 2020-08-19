package com.zistone.factorytest0718;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zistone.factorytest0718.util.MyProgressDialogUtil;

public class KeyDownActivity extends BaseActivity {

    private static final String TAG = "KeyDownActivity";

    //型号WD220B所对应的控件
    private TextView _txt1Wd220b, _txt2Wd220b, _txt3Wd220b, _txt4Wd220b, _txt5Wd220b;
    private LinearLayout _llWd220b;
    private TextView _txt;
    private ImageView _iv;
    private boolean[] _keyPasss = new boolean[]{false, false, false, false, false};

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        _txt.setTextColor(Color.GRAY);
        _txt.setBackground(getDrawable(R.color.white));
        _iv.setVisibility(View.INVISIBLE);
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG, "键码值:" + keyCode);
        _txt.setText(keyCode + "");
        _txt.setTextColor(Color.WHITE);
        _txt.setBackground(getDrawable(R.color.springGreen));
        _iv.setVisibility(View.VISIBLE);
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            this.finish();
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && event.getAction() == KeyEvent.ACTION_DOWN) {
            _txt1Wd220b.setBackgroundColor(SPRING_GREEN);
            _keyPasss[0] = true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN && event.getAction() == KeyEvent.ACTION_DOWN) {
            _txt2Wd220b.setBackgroundColor(SPRING_GREEN);
            _keyPasss[1] = true;
        } else if (keyCode == KeyEvent.KEYCODE_F2 && event.getAction() == KeyEvent.ACTION_DOWN) {
            _txt3Wd220b.setBackgroundColor(SPRING_GREEN);
            _keyPasss[2] = true;
        } else if (keyCode == KeyEvent.KEYCODE_UNKNOWN && event.getAction() == KeyEvent.ACTION_DOWN) {
            _txt5Wd220b.setBackgroundColor(SPRING_GREEN);
            _keyPasss[4] = true;
        }
        if (_keyPasss[0] && _keyPasss[1] && _keyPasss[2] && _keyPasss[3] && _keyPasss[4]) {
            _btnPass.setEnabled(true);
            MyProgressDialogUtil.ShowCountDownTimerWarning(this, "知道了", 3 * 1000, "提示", "按键测试已通过！", false, () -> {
                MyProgressDialogUtil.DismissAlertDialog();
                Pass();
            });
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_keydown);
        SetBaseContentView(R.layout.activity_keydown);
        _txt = findViewById(R.id.txt_keydown);
        _iv = findViewById(R.id.iv_keydown);
        //型号WD220B对应的控件
        _llWd220b = findViewById(R.id.ll_wd220b_keydown);
        _txt1Wd220b = findViewById(R.id.txt1_wd220b_keydown);
        _txt2Wd220b = findViewById(R.id.txt2_wd220b_keydown);
        _txt3Wd220b = findViewById(R.id.txt3_wd220b_keydown);
        _txt4Wd220b = findViewById(R.id.txt4_wd220b_keydown);
        _txt5Wd220b = findViewById(R.id.txt5_wd220b_keydown);
        _btnPass.setEnabled(false);
    }

}
