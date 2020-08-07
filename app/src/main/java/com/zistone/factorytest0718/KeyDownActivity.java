package com.zistone.factorytest0718;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zistone.factorytest0718.util.MyActivityManager;

public class KeyDownActivity extends BaseActivity {

    private static final String TAG = "KeyDownActivity";

    private TextView _txt;
    private ImageView _iv;

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
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_keydown);
        SetBaseContentView(R.layout.activity_keydown);
        _txt = findViewById(R.id.txt_keydown);
        _iv = findViewById(R.id.iv_keydown);
        _btnPass.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putExtra(ARG_PARAM1, PASS);
            setResult(RESULT_OK, intent);
            finish();
        });
        _btnFail.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putExtra(ARG_PARAM1, FAIL);
            setResult(RESULT_OK, intent);
            finish();
        });
    }
}
