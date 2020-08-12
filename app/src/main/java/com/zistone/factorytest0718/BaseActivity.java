package com.zistone.factorytest0718;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

public class BaseActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "BaseActivity";

    public static final String ARG_PARAM1 = "param1";
    public static final String ARG_PARAM2 = "param2";
    public static final String PASS = "PASS";
    public static final String FAIL = "FAIL";

    public Button _btnPass, _btnFail;
    public int _screenHeight, _screenWidth;
    //基类布局
    public LinearLayout _baseLinearLayout;

    /**
     * 权限检查
     *
     * @param neededPermissions 需要的权限
     * @return 全部被允许
     */
    public boolean CheckPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this, neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    /**
     * 将继承自BaseActivity的layout加进来
     *
     * @param layoutId
     */
    public void SetBaseContentView(int layoutId) {
        _baseLinearLayout = findViewById(R.id.ll_base);
        //根据屏幕尺寸设置控件大小，不然基类的控件可能看不到
        ViewGroup.LayoutParams layoutParams = _baseLinearLayout.getLayoutParams();
        layoutParams.height = _screenHeight - 300;
        //将继承自该Activity的布局加进来
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(layoutId, null);
        _baseLinearLayout.addView(view);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_pass_base:
                Log.i(TAG, "父类的点击Pass事件触发");
                break;
            case R.id.btn_fail_base:
                Log.i(TAG, "父类的点击Fail事件触发");
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去年系统的TitleBar
        //        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_base);
        _btnPass = findViewById(R.id.btn_pass_base);
        _btnPass.setOnClickListener(this::onClick);
        _btnFail = findViewById(R.id.btn_fail_base);
        _btnFail.setOnClickListener(this::onClick);
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(point);
        _screenHeight = point.y;
        _screenWidth = point.x;
    }
}