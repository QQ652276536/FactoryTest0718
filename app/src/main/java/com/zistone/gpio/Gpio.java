package com.zistone.gpio;

/**
 * 通过JNI控制GPIO
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class Gpio {

    private static Gpio mGpioSet = new Gpio();

    private Gpio() {
    }

    public static Gpio getInstance() {
        return mGpioSet;
    }

    public native void set_gpio(int state, int pin_num);

    static {
        System.loadLibrary("gpio");
    }

}
