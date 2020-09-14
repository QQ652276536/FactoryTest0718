package com.zistone.factorytest0718;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.face.util.ImageUtils;
import com.zistone.factorytest0718.view.MyRemoteControlButton;

/**
 * 用来测试一些东西的，没有任何实际功能...
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
    private MyRemoteControlButton _myRemoteControlButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_test_test);
        SetBaseContentView(R.layout.activity_test_test);

        _txt = findViewById(R.id.txt_test_test);
        _txt.setText(stringFromJNI());
        _myRemoteControlButton = findViewById(R.id.mrcb_test_test);
        MyRemoteControlButton.RoundMenu roundMenu = new MyRemoteControlButton.RoundMenu();
        roundMenu.bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.com_test_bottom);
        roundMenu.onClickListener = v -> Toast.makeText(TestTestActivity.this, "下", Toast.LENGTH_SHORT).show();
        _myRemoteControlButton.AddRoundMenu(roundMenu);

        roundMenu = new MyRemoteControlButton.RoundMenu();
        roundMenu.bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.camera_ok);
        roundMenu.onClickListener = v -> Toast.makeText(TestTestActivity.this, "左", Toast.LENGTH_SHORT).show();
        _myRemoteControlButton.AddRoundMenu(roundMenu);

        roundMenu = new MyRemoteControlButton.RoundMenu();
        roundMenu.bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.com_test_top);
        roundMenu.onClickListener = v -> Toast.makeText(TestTestActivity.this, "上", Toast.LENGTH_SHORT).show();
        _myRemoteControlButton.AddRoundMenu(roundMenu);

        roundMenu = new MyRemoteControlButton.RoundMenu();
        roundMenu.bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.com_test_clear);
        roundMenu.onClickListener = v -> Toast.makeText(TestTestActivity.this, "右", Toast.LENGTH_SHORT).show();
        _myRemoteControlButton.AddRoundMenu(roundMenu);

        Bitmap centerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.camera_before);
        _myRemoteControlButton.SetCenterButton(centerBitmap, v -> Toast.makeText(TestTestActivity.this, "点击了中心圆圈", Toast.LENGTH_SHORT).show());
    }

    public native String stringFromJNI();

}
