package com.zistone.factorytest0718.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 本地存储该项目需要的一些参数
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public final class MySharedPreferences {

    private static final String COMTEST_PORTNAMEANDBAUDRATE = "SerialPortNameAndBaudrate";
    private static final String MAIN_PASS_FAIL = "MainPassFail";

    /**
     * （禁止外部实例化）
     */
    private MySharedPreferences() {
    }

    public static SharedPreferences MyShare(Context context) {
        return context.getSharedPreferences("FACTORYTEST0718_MYSHAREDPREFERENCES", Context.MODE_PRIVATE);
    }

    public static void Clear(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(MAIN_PASS_FAIL, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    /**
     * 读取每个功能的测试结果
     *
     * @param context
     * @return
     */
    public static Map<Integer, Boolean> GetMainPassFail(Context context) {
        Map<Integer, Boolean> map = new HashMap<>();
        try {
            String str = MyShare(context).getString(MAIN_PASS_FAIL, "");
            JSONArray jsonArray = new JSONArray(str);
            if (jsonArray.length() != 1)
                return map;
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            JSONArray nameArray = jsonObject.names();
            for (int i = 0; i < nameArray.length(); i++) {
                String name = nameArray.getString(i);
                boolean value = jsonObject.getBoolean(name);
                map.put(Integer.valueOf(name), value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static boolean SetMainPassFail(Context context, Map<Integer, Boolean> map) {
        JSONArray jsonArray = new JSONArray();
        Iterator<Map.Entry<Integer, Boolean>> iterator = map.entrySet().iterator();
        JSONObject jsonObject = new JSONObject();
        while (iterator.hasNext()) {
            try {
                Map.Entry<Integer, Boolean> entry = iterator.next();
                jsonObject.put(String.valueOf(entry.getKey()), entry.getValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        jsonArray.put(jsonObject);
        SharedPreferences.Editor editor = MyShare(context).edit();
        editor.putString(MAIN_PASS_FAIL, jsonArray.toString());
        return editor.commit();
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

    public static boolean SetSerialPortNameAndBaudrate(Context context, String input) {
        SharedPreferences.Editor editor = MyShare(context).edit();
        editor.putString(COMTEST_PORTNAMEANDBAUDRATE, input);
        return editor.commit();
    }

}
