package com.zistone.factorytest0718;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.zistone.gpio.Gpio;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class FlashLightActivity extends BaseActivity {

    private static final String FLASHLIGHT_NODE = "sys/class/leds/led\\:torch_0/brightness";

    private Gpio _gpio = Gpio.getInstance();
    private boolean _threadFlag = false;

    @Override
    protected void onResume() {
        super.onResume();
        //闪光灯
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(FLASHLIGHT_NODE);
            fileOutputStream.write(new byte[]{120});
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //信号灯
        new Thread(() -> {
            while (!_threadFlag) {
                try {
                    _gpio.set_gpio(1, 102);
                    Thread.sleep(500);
                    _gpio.set_gpio(1, 98);
                    Thread.sleep(500);
                    _gpio.set_gpio(1, 90);
                    Thread.sleep(500);
                    _gpio.set_gpio(1, 96);
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //关闭闪光灯
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(FLASHLIGHT_NODE);
            fileOutputStream.write(new byte[]{0});
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //关闭信号灯
        _threadFlag = true;
        _gpio.set_gpio(0, 102);
        _gpio.set_gpio(0, 98);
        _gpio.set_gpio(0, 90);
        _gpio.set_gpio(0, 96);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_flash_light);
        SetBaseContentView(R.layout.activity_flash_light);
    }
}
