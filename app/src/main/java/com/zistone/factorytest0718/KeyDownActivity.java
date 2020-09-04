package com.zistone.factorytest0718;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.zistone.factorytest0718.util.MyFileUtil;
import com.zistone.factorytest0718.util.MyProgressDialogUtil;

/**
 * 按键测试，目前只支持誉兴通的WD220型号的设备
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class KeyDownActivity extends BaseActivity {

    private static final String TAG = "KeyDownActivity";
    private static final String F1KEY_TEST = "/sdcard/zsttest.txt";

    //型号WD220B所对应的控件
    private TextView _txt1Wd220b, _txt2Wd220b, _txt3Wd220b, _txt4Wd220b, _txt5Wd220b, _txt6Wd220b, _txt7Wd220b, _txt8Wd220b;
    private TextView _txt;
    private ImageView _iv;
    private boolean[] _keyPasss = new boolean[]{false, false, false, false, false, false, false, false};

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

    /**
     * 因为这个版本的系统无法在onKeyDown里监听和拦截Bank键，所以在这里处理
     *
     * @param event
     * @return
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            _txt6Wd220b.setBackgroundColor(SPRING_GREEN);
            _keyPasss[5] = true;
            if (_keyPasss[0] && _keyPasss[1] && _keyPasss[2] && _keyPasss[3] && _keyPasss[4] && _keyPasss[5] && _keyPasss[6] && _keyPasss[7]) {
                _btnPass.setEnabled(true);
                Pass();
            }
            Log.i(TAG, "屏蔽Bank键已执行...");
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
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
            _keyPasss[3] = true;
        } else if (keyCode == KeyEvent.KEYCODE_F1 && event.getAction() == KeyEvent.ACTION_DOWN) {
            _txt4Wd220b.setBackgroundColor(SPRING_GREEN);
            _keyPasss[2] = true;
        } else if (keyCode == KeyEvent.KEYCODE_UNKNOWN && event.getAction() == KeyEvent.ACTION_DOWN) {
            _txt5Wd220b.setBackgroundColor(SPRING_GREEN);
            _keyPasss[4] = true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            _txt6Wd220b.setBackgroundColor(SPRING_GREEN);
            _keyPasss[5] = true;
        } else if (keyCode == KeyEvent.KEYCODE_HOME && event.getAction() == KeyEvent.ACTION_DOWN) {
            _txt7Wd220b.setBackgroundColor(SPRING_GREEN);
            _keyPasss[6] = true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_DOWN) {
            _txt8Wd220b.setBackgroundColor(SPRING_GREEN);
            _keyPasss[7] = true;
        }
        if (_keyPasss[0] && _keyPasss[1] && _keyPasss[2] && _keyPasss[3] && _keyPasss[4] && _keyPasss[5] && _keyPasss[6] && _keyPasss[7]) {
            _btnPass.setEnabled(true);
            Pass();
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyFileUtil.DeleteFile(F1KEY_TEST);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        //F1键在FrameWork层做了“一键拨号”的功能导致按键测试不能正常运行，现在是通过判断是否有/sdcard/zstest.txt而触发功能，测
        //试程序运行时新建文件以达到屏蔽效果，退出时删除文件以解除屏蔽
        MyFileUtil.MakeFile(F1KEY_TEST);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_keydown);
        SetBaseContentView(R.layout.activity_keydown);
        _txt = findViewById(R.id.txt_keydown);
        _iv = findViewById(R.id.iv_keydown);
        _txt1Wd220b = findViewById(R.id.txt1_wd220b_keydown);
        _txt2Wd220b = findViewById(R.id.txt2_wd220b_keydown);
        _txt3Wd220b = findViewById(R.id.txt3_wd220b_keydown);
        _txt4Wd220b = findViewById(R.id.txt4_wd220b_keydown);
        _txt5Wd220b = findViewById(R.id.txt5_wd220b_keydown);
        _txt6Wd220b = findViewById(R.id.txt6_wd220b_keydown);
        _txt7Wd220b = findViewById(R.id.txt7_wd220b_keydown);
        _txt8Wd220b = findViewById(R.id.txt8_wd220b_keydown);
        _btnPass.setEnabled(false);
    }

}
