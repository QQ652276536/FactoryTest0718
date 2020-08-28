package com.zistone.factorytest0718;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.zistone.factorytest0718.util.MyProgressDialogUtil;

/**
 * OTG测试
 *
 * @author LiWei
 * @date 2020/8/28 9:37
 * @email 652276536@qq.com
 */
public class OtgActivity extends BaseActivity {

    private static final String TAG = "OtgActivity";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "requestCode=" + requestCode + "，resultCode=" + resultCode);
        switch (requestCode) {
            case 101:
                MyProgressDialogUtil.ShowConfirm(this, "通过", "失败", "提示", "OTG测试是否通过？", false, new MyProgressDialogUtil.ConfirmListener() {
                    @Override
                    public void OnConfirm() {
                        Pass();
                    }

                    @Override
                    public void OnCancel() {
                        Fail();
                    }
                });
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //关闭OTG
        try {
            Runtime.getRuntime().exec("gpio-test 1 0");
            Log.i(TAG, "关闭OTG");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_otg);
        SetBaseContentView(R.layout.activity_otg);
        //开启OTG
        try {
            //            Runtime.getRuntime().exec("gpio-test 1 1");
            Log.i(TAG, "开启OTG");
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, 101);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
