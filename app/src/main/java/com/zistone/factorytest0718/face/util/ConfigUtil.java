package com.zistone.factorytest0718.face.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.StringRes;

import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.zistone.factorytest0718.R;
import com.zistone.factorytest0718.face.constants.Constants;

public class ConfigUtil {

    private static final String APP_NAME = "Factorytest0718";
    //人脸数
    private static final String TRACKED_FACE_COUNT = "TRACKED_FACE_COUNT";
    //检测角度
    private static final String DETECTION_ANGLE = "DETECTION_ANGLE";
    //比对阈值
    private static final String COMPARE_THRESHOLD = "COMPARE_THRESHOLD";

    public static boolean SetTrackedFaceCount(Context context, int trackedFaceCount) {
        if (context == null) {
            return false;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.edit().putInt(TRACKED_FACE_COUNT, trackedFaceCount).commit();
    }

    public static int GetTrackedFaceCount(Context context) {
        if (context == null) {
            return 0;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(TRACKED_FACE_COUNT, 0);
    }

    /**
     * 摄像头检测方向
     *
     * @param context
     * @param ftOrient
     * @return
     */
    public static boolean SetDetectionAngle(Context context, DetectFaceOrientPriority ftOrient) {
        if (context == null) {
            return false;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.edit().putString(DETECTION_ANGLE, ftOrient.name()).commit();
    }

    public static DetectFaceOrientPriority GetDetectionAngle(Context context) {
        if (context == null) {
            return DetectFaceOrientPriority.ASF_OP_270_ONLY;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        return DetectFaceOrientPriority.valueOf(sharedPreferences.getString(DETECTION_ANGLE, DetectFaceOrientPriority.ASF_OP_270_ONLY.name()));
    }

    /**
     * 比对阈值
     *
     * @param context
     * @param value
     * @return
     */
    public static boolean SetThreshold(Context context, float value) {
        if (context == null) {
            return false;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.edit().putFloat(COMPARE_THRESHOLD, value).commit();
    }

    public static float GetThreshold(Context context) {
        if (context == null) {
            return 0.82f;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getFloat(COMPARE_THRESHOLD, 0.82f);
    }

}
