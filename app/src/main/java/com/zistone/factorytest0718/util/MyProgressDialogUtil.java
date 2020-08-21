package com.zistone.factorytest0718.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.zistone.factorytest0718.R;

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

    /**
     * 倒计时提示框
     *
     * @param context    上下文
     * @param btnStr     按钮文字
     * @param timerCount 倒计时时长，单位：毫秒
     * @param title      标题
     * @param content    提示内容
     * @param touchOut   点击外部消失
     * @param listener   点击事件
     */
    public static void ShowCountDownTimerWarning(Context context, String btnStr, int timerCount, String title, String content, boolean touchOut, WarningListener listener) {
        //确保创建Dialog的Activity没有finish才显示
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            if (null == _alertDialog) {
                _alertDialog = new AlertDialog.Builder(context).create();
                _alertDialog.setTitle(title);
                _alertDialog.setMessage(content);
                _alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, btnStr, (dialog, which) -> {
                    if (null != listener)
                        listener.OnIKnow();
                });
                _alertDialog.setCancelable(touchOut);
                _alertDialog.show();
                Button btn = _alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                CountDownTimer countDownTimer = new CountDownTimer(timerCount, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        btn.setText(btnStr + "(" + millisUntilFinished / 1000 + ")");
                    }

                    @Override
                    public void onFinish() {
                        if (null != _alertDialog) {
                            _alertDialog.dismiss();
                            _alertDialog = null;
                        }
                        if (null != listener)
                            listener.OnIKnow();
                    }
                };
                countDownTimer.start();
            }
        } else {
            _alertDialog.show();
        }
    }

    /**
     * 等待提示框
     *
     * @param context      上下文
     * @param touchOutSide 点击外部消失
     * @param listener     点击事件
     * @param str          提示内容
     */
    public static void ShowProgressDialog(Context context, boolean touchOutSide, ProgressDialogListener listener, String str) {
        //确保创建Dialog的Activity没有finish才显示
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            if (null == _alertDialog) {
                _alertDialog = new AlertDialog.Builder(context, R.style.CustomProgressDialog).create();
                View loadView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null);
                _alertDialog.setView(loadView, 0, 0, 0, 0);
                _alertDialog.setCanceledOnTouchOutside(touchOutSide);
                TextView textView = loadView.findViewById(R.id.txt_dialog);
                textView.setText(str);
                _alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (null != listener)
                            listener.OnDismiss();
                    }
                });
                _alertDialog.show();
            } else {
                _alertDialog.show();
            }
        }
    }

    public static void DismissAlertDialog() {
        if (null != _alertDialog) {
            _alertDialog.dismiss();
            _alertDialog = null;
        }
    }

    /**
     * 确认提示框
     *
     * @param context  上下文
     * @param btnStr1  确认按钮文字
     * @param btnStr2  取消按钮文字
     * @param title    标题
     * @param content  内容
     * @param touchOut 点击外部消失
     * @param listener 点击事件
     */
    public static void ShowConfirm(Context context, String btnStr1, String btnStr2, String title, String content, boolean touchOut, ConfirmListener listener) {
        //确保创建Dialog的Activity没有finish才显示
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            builder.setMessage(content);
            builder.setNegativeButton(btnStr1, (dialog, which) -> {
                if (null != listener)
                    listener.OnConfirm();
            });
            builder.setPositiveButton(btnStr2, (dialog, which) -> {
                if (null != listener)
                    listener.OnCancel();
            });
            builder.setCancelable(touchOut);
            builder.show();
        }
    }

    /**
     * 提示框
     *
     * @param context  上下文
     * @param btnStr   按钮文字
     * @param title    标题
     * @param content  内容
     * @param touchOut 点击外部消失
     * @param listener 点击事件
     */
    public static void ShowWarning(Context context, String btnStr, String title, String content, boolean touchOut, WarningListener listener) {
        //确保创建Dialog的Activity没有finish才显示
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            builder.setMessage(content);
            builder.setPositiveButton(btnStr, (dialog, which) -> {
                if (null != listener)
                    listener.OnIKnow();
            });
            builder.setCancelable(touchOut);
            builder.show();
        }
    }

}
