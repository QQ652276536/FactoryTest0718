package com.zistone.factorytest0718.util;

import android.app.Activity;
import android.content.Context;

import androidx.appcompat.app.AlertDialog;

public final class MyProgressDialogUtil {

    private static AlertDialog _alertDialog;

    public interface ProgressDialogListener {
        void OnDismiss();
    }

    public interface ConfirmListener {
        void OnConfirm();

        void OnCancel();
    }

    public interface WarningListener {
        void OnIKnow();
    }

    /**
     * （禁止外部实例化）
     */
    private MyProgressDialogUtil() {
    }

    public static void ShowConfirm(Context context, String title, String content, ConfirmListener listener) {
        //确保创建Dialog的Activity没有finish才显示
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            builder.setMessage(content);
            builder.setNegativeButton("好的", (dialog, which) -> {
                if (null != listener)
                    listener.OnConfirm();
            });
            builder.setPositiveButton("不了", (dialog, which) -> {
                if (null != listener)
                    listener.OnCancel();
            });
            builder.show();
        }
    }

    public static void ShowWarning(Context context, String title, String content, WarningListener listener) {
        //确保创建Dialog的Activity没有finish才显示
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            builder.setMessage(content);
            builder.setPositiveButton("知道了", (dialog, which) -> {
                if (null != listener)
                    listener.OnIKnow();
            });
            builder.show();
        }
    }

    public static void Dismiss() {
        if (_alertDialog != null) {
            _alertDialog.dismiss();
            _alertDialog = null;
        }
    }

}
