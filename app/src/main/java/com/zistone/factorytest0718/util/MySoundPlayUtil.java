package com.zistone.factorytest0718.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;

public final class MySoundPlayUtil {

    private static Context _context;
    private static SoundPool _soundPool;
    private static MySoundPlayUtil _My_soundPlayUtils;

    /**
     * （禁止外部实例化）
     */
    private MySoundPlayUtil() {
    }

    public static void Init() {
        if (Build.VERSION.SDK_INT > 21) {
            SoundPool.Builder builder = new SoundPool.Builder();
            //传入音频数量
            builder.setMaxStreams(5);
            //AudioAttributes是一个封装音频各种属性的方法
            AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
            //设置音频流的合适的属性
            attrBuilder.setLegacyStreamType(AudioManager.STREAM_SYSTEM);
            //加载一个AudioAttributes
            builder.setAudioAttributes(attrBuilder.build());
            _soundPool = builder.build();
        } else {
            _soundPool = new SoundPool(5, AudioManager.STREAM_SYSTEM, 0);
        }
    }

    /**
     * 播放系统通知的声音
     */
    public static void SystemSoundPlay(Context context) {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (null == uri)
            return;
        Ringtone ringtone = RingtoneManager.getRingtone(context, uri);
        ringtone.play();
    }

}

