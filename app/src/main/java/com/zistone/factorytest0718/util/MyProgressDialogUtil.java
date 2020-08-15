package com.zistone.factorytest0718.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.zistone.factorytest0718.R;

public final class MyProgressDialogUtil {

    private static AlertDialog _progressDialog;

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

    public static void ShowProgressDialog(Context context, boolean touchOutSide, ProgressDialogListener listener, String str) {
        //确保创建Dialog的Activity没有finish才显示
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            if (null == _progressDialog) {
                _progressDialog = new AlertDialog.Builder(context, R.style.CustomProgressDialog).create();
                View loadView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null);
                _progressDialog.setView(loadView, 0, 0, 0, 0);
                _progressDialog.setCanceledOnTouchOutside(touchOutSide);
                TextView textView = loadView.findViewById(R.id.txt_dialog);
                textView.setText(str);
                _progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (null != listener)
                            listener.OnDismiss();
                    }
                });
                _progressDialog.show();
            } else {
                _progressDialog.show();
            }
        }
    }

    public static void DismissProgressDialog() {
        if (null != _progressDialog) {
            _progressDialog.dismiss();
            _progressDialog = null;
        }
    }

    public static void ShowConfirm(Context context, String title, String content, boolean touchOut, ConfirmListener listener) {
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
            builder.setCancelable(touchOut);
            builder.show();
        }
    }

    public static void ShowWarning(Context context, String title, String content, boolean touchOut, WarningListener listener) {
        //确保创建Dialog的Activity没有finish才显示
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            builder.setMessage(content);
            builder.setPositiveButton("知道了", (dialog, which) -> {
                if (null != listener)
                    listener.OnIKnow();
            });
            builder.setCancelable(touchOut);
            builder.show();
        }
    }

}
