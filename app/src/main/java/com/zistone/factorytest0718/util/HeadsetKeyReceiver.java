package com.zistone.factorytest0718.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

/**
 * 有线耳机的按键广播
 */
public class HeadsetKeyReceiver extends BroadcastReceiver {

    private static final String TAG = "HeadsetKeyReceiver";
    private static Listener _listener;

    public interface Listener {
        void OnKeyDown(int keyCode);
    }

    public static void SetListener(Listener listener) {
        _listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        int keyCode = keyEvent.getKeyCode();
        //播放下一首KeyEvent.KEYCODE_MEDIA_NEXT
        //播放上一首KeyEvent.KEYCODE_MEDIA_PREVIOUS
        //中间按钮，暂停/播放KeyEvent.KEYCODE_HEADSETHOOK
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            Log.i(TAG, "耳机按键码：" + keyCode);
            _listener.OnKeyDown(keyCode);
        }
    }

}
