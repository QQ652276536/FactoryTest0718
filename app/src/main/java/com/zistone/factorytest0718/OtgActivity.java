package com.zistone.factorytest0718;

import android.os.Bundle;
import android.util.Log;

/**
 * OTG测试
 *
 * @author LiWei
 * @date 2020/8/28 9:37
 * @email 652276536@qq.com
 */
public class OtgActivity extends BaseActivity {

    private static final String TAG = "OtgActivity";

    private void SwitchUSBMode(int value) {
        try {
            switch (value) {
                case 0:
                    Runtime.getRuntime().exec("gpio-test 66 0");
                    Runtime.getRuntime().exec("gpio-test 137 0");
                    Log.i(TAG, "设备已切换至USB模式");
                    break;
                case 1:
                    Runtime.getRuntime().exec("gpio-test 66 1");
                    Runtime.getRuntime().exec("gpio-test 137 1");
                    Log.i(TAG, "设备已切换至HUB模式");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_otg);
        SetBaseContentView(R.layout.activity_otg);
        SwitchUSBMode(1);
    }

}
