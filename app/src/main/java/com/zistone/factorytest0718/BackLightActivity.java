package com.zistone.factorytest0718;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.zistone.factorytest0718.util.MyProgressDialogUtil;

public class BackLightActivity extends BaseActivity implements View.OnTouchListener {

    private LinearLayout _ll;
    private int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_back_light);
        _ll = findViewById(R.id.ll_backlight);
        _ll.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        i++;
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.screenBrightness = i * 80 / 255.0f;
        window.setAttributes(layoutParams);
        if (i == 3) {
            MyProgressDialogUtil.ShowConfirm(this, "通过", "失败", "提示", "屏幕亮度是否变化", false, new MyProgressDialogUtil.ConfirmListener() {
                @Override
                public void OnConfirm() {
                    Pass();
                }

                @Override
                public void OnCancel() {
                    Fail();
                }
            });
        }
        return false;
    }
}
