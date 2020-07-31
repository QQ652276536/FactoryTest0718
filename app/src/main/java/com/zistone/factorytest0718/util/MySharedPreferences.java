package com.zistone.factorytest0718.util;

import android.content.Context;
import android.content.SharedPreferences;

public final class MySharedPreferences {

    private static final String COMTEST_PORTNAMEANDBAUDRATE = "SerialPortNameAndBaudrate";

    /**
     * （禁止外部实例化）
     */
    private MySharedPreferences() {
    }

    public static SharedPreferences MyShare(Context context) {
        return context.getSharedPreferences("FACTORYTEST0718_MYSHAREDPREFERENCES", Context.MODE_PRIVATE);
    }

    /**
     * 读取串口测试功能里的串口名称和波特率
     *
     * @param context
     * @return
     */
    public static String GetSerialPortNameAndBaudrate(Context context) {
        return MyShare(context).getString(COMTEST_PORTNAMEANDBAUDRATE, "/dev/ttyHSL3,115200,3F07000000017A9D");
    }

    /**
     * 保存串口测试功能里的串口名称和波特率
     *
     * @param context
     * @param input
     * @return
     */
    public static boolean SetSerialPortNameAndBaudrate(Context context, String input) {
        SharedPreferences.Editor editor = MyShare(context).edit();
        editor.putString(COMTEST_PORTNAMEANDBAUDRATE, input);
        return editor.commit();
    }

}
