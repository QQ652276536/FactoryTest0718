package com.zistone.factorytest0718;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.zistone.factorytest0718.util.MyFileUtil;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

public class SoundActivity extends BaseActivity implements View.OnClickListener, View.OnTouchListener {

    private static final String TAG = "SoundActivity";
    //录音文件所在目录
    private static final String FILE_DIR = "/sdcard/Factory0718/SoundTest/";
    //录音文件名
    private static final String FILENAME_FORMAT = "yyyyMMdd_HHmmss";
    //系统默认来电铃声
    private static final Uri URI_NOTIFICATION = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

    private ImageButton _imgBtnPlay, _imgBtnRecord;
    private TextView _txtCountDown, _txtRecordPath;
    private boolean _isPlaying = false, _isRecording = false, _isPlayingRecord = false;
    private Ringtone _ringtone;
    private MediaRecorder _mediaRecorder;
    private MediaPlayer _mediaPlayer;
    //录音文件的文件名、录音文件的路径
    private String _fileName = "", _filePath = "";
    private CountDownTimer _countDownTimer = new CountDownTimer(10 * 1000, 1 * 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            _txtCountDown.setText("录音倒计时：" + millisUntilFinished / 1000);
        }

        @Override
        public void onFinish() {
            _txtCountDown.setVisibility(View.INVISIBLE);
        }
    };

    /**
     * 扬声器播放
     */
    private void StartPlaySound() {
        _imgBtnRecord.setEnabled(false);
        _txtRecordPath.setEnabled(false);
        _imgBtnPlay.setBackground(getDrawable(R.drawable.sound_play_stop));
        _imgBtnRecord.setBackground(getDrawable(R.drawable.sound_record_start3));
        _txtRecordPath.setTextColor(Color.GRAY);
        _txtRecordPath.setBackground(getDrawable(R.drawable.sound_record_txt_border3));
        _ringtone.play();
    }

    /**
     * 扬声器停止播放
     */
    private void StopPlaySound() {
        _imgBtnRecord.setEnabled(true);
        _txtRecordPath.setEnabled(true);
        _imgBtnPlay.setBackground(getDrawable(R.drawable.sound_play_start1));
        _imgBtnRecord.setBackground(getDrawable(R.drawable.sound_record_start1));
        _txtRecordPath.setTextColor(Color.parseColor("#3CB371"));
        _txtRecordPath.setBackground(getDrawable(R.drawable.sound_record_txt_border1));
        _ringtone.stop();
    }

    /**
     * 播放录音
     */
    private void PlayRecord() {
        _imgBtnPlay.setEnabled(false);
        _imgBtnRecord.setEnabled(false);
        _imgBtnPlay.setBackground(getDrawable(R.drawable.sound_play_start3));
        _imgBtnRecord.setBackground(getDrawable(R.drawable.sound_record_start3));
        _txtRecordPath.setTextColor(Color.RED);
        _txtRecordPath.setBackground(getDrawable(R.drawable.sound_record_txt_border2));
        try {
            if (!"".equals(_filePath)) {
                _mediaPlayer.reset();
                _mediaPlayer.setDataSource(_filePath);
                _mediaPlayer.prepare();
                _mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 录音播放/录制完毕
     */
    private void PlayOrRecordOver() {
        _imgBtnPlay.setEnabled(true);
        _imgBtnRecord.setEnabled(true);
        _imgBtnPlay.setBackground(getDrawable(R.drawable.sound_play_start1));
        _imgBtnRecord.setBackground(getDrawable(R.drawable.sound_record_start1));
        _txtRecordPath.setTextColor(Color.parseColor("#3CB371"));
        _txtRecordPath.setBackground(getDrawable(R.drawable.sound_record_txt_border1));
    }

    /**
     * 停止录音
     */
    private void StopRecord() {
        //只有正在录音的时候才能停止，否则会有异常
        if (_isRecording) {
            //停止录音
            _mediaRecorder.stop();
            _mediaRecorder.reset();
        }
        //取消并隐藏录音倒计时
        _countDownTimer.cancel();
        _txtCountDown.setVisibility(View.INVISIBLE);
        //显示录音文件路径
        _txtRecordPath.setVisibility(View.VISIBLE);
        _txtRecordPath.setText("点击可开始/停止播放录音\n" + _fileName);
        _imgBtnPlay.setEnabled(true);
        _imgBtnPlay.setBackground(getDrawable(R.drawable.sound_play_start1));
        _imgBtnRecord.setBackground(getDrawable(R.drawable.sound_record_start1));
    }

    /**
     * 开始录音
     */
    private void StartRecord() {
        //显示录音倒计时
        _countDownTimer.start();
        _txtCountDown.setVisibility(View.VISIBLE);
        //隐藏录音文件路径
        _txtRecordPath.setVisibility(View.INVISIBLE);
        _txtRecordPath.setText("点击可开始/停止播放录音\n" + _fileName);
        _imgBtnPlay.setEnabled(false);
        _imgBtnPlay.setBackground(getDrawable(R.drawable.sound_play_start3));
        _imgBtnRecord.setBackground(getDrawable(R.drawable.sound_record_stop));
        try {
            //设置麦克风
            _mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            //设置输出文件的格式
            _mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            //设置音频文件的编码
            _mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            //设置最长录制时间，单位：毫秒
            _mediaRecorder.setMaxDuration(10 * 1000);
            //设置文件大小，单位：字节
            _mediaRecorder.setMaxFileSize(100 * 1024);
            _fileName = DateFormat.format(FILENAME_FORMAT, Calendar.getInstance(Locale.CHINA)) + ".m4a";
            //按路径建立文件，已有相同路径的文件则不建立
            MyFileUtil.MakeFile(FILE_DIR + _fileName);
            _filePath = FILE_DIR + _fileName;
            //准备
            _mediaRecorder.setOutputFile(_filePath);
            _mediaRecorder.prepare();
            //开始录音
            _mediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
            _ringtone.stop();
            if (_isRecording) {
                _mediaRecorder.stop();
                _mediaRecorder.reset();
            }
            _mediaRecorder.release();
            _mediaRecorder = null;
            if (!"".equals(_filePath)) {
                File file = new File(_filePath);
                if (file.exists())
                    file.delete();
            }
            _filePath = "";
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.btn_play_sound:
                if (_isPlaying) {
                    return false;
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    _imgBtnPlay.setBackground(getDrawable(R.drawable.sound_play_start2));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    _imgBtnPlay.setBackground(getDrawable(R.drawable.sound_play_start1));
                }
                break;
            case R.id.btn_record_sound:
                if (_isRecording) {
                    return false;
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    _imgBtnRecord.setBackground(getDrawable(R.drawable.sound_record_start2));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    _imgBtnRecord.setBackground(getDrawable(R.drawable.sound_record_start1));
                }
                break;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //扬声器
            case R.id.btn_play_sound:
                //开始扬声器以后：可以停止扬声器，但是禁止录音和播放录音
                if (!_isPlaying) {
                    _isPlaying = true;
                    StartPlaySound();
                }
                //停止扬声器以后：可以再次开启扬声器、可以录音、可以播放录音
                else {
                    _isPlaying = false;
                    StopPlaySound();
                }
                break;
            //录音机
            case R.id.btn_record_sound:
                //开始录音以后：可以停止录音，但是禁止扬声器和录音
                if (!_isRecording) {
                    _isRecording = true;
                    StartRecord();
                }
                //停止录音以后：可以再次录音、可以开启扬声器、可以播放录音
                else {
                    StopRecord();
                    //停止录音需要判断当前标识位，所以将标识位的改变放在停止录音以后
                    _isRecording = false;
                }
                break;
            //播放录音
            case R.id.txt_record_path_sound:
                //开始播放录音以后：可以停止播放录音，但是禁止录音和扬声器
                if (!_isPlayingRecord) {
                    _isPlayingRecord = true;
                    PlayRecord();
                }
                //停止播放录音以后：可以播放扬声器、可以录音、可以播放录音
                else {
                    _isPlayingRecord = false;
                    //记个笔记：MediaPlayer在pause之后可以直接start，但是在stop以后需要perpare才能start
                    _mediaPlayer.stop();
                    PlayOrRecordOver();
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_sound);
        SetBaseContentView(R.layout.activity_sound);
        _ringtone = RingtoneManager.getRingtone(this, URI_NOTIFICATION);
        _mediaRecorder = new MediaRecorder();
        _mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                switch (what) {
                    //已到达设置的最长录制时间或者已到达设置的文件大小
                    case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
                    case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
                        Log.i(TAG, "音频录制完毕");
                        StopRecord();
                        _isRecording = false;
                        //录音完毕
                        PlayOrRecordOver();
                        break;
                }
            }
        });
        _mediaPlayer = new MediaPlayer();
        _mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.i(TAG, "音频播放完毕");
                //结束播放的时候：禁止播放、禁止录音、禁止播放录音
                _isPlaying = false;
                _isPlayingRecord = false;
                //录音播放完毕
                PlayOrRecordOver();
            }
        });
        _imgBtnPlay = findViewById(R.id.btn_play_sound);
        _imgBtnPlay.setOnClickListener(this::onClick);
        _imgBtnPlay.setOnTouchListener(this::onTouch);
        _imgBtnRecord = findViewById(R.id.btn_record_sound);
        _imgBtnRecord.setOnClickListener(this::onClick);
        _imgBtnRecord.setOnTouchListener(this::onTouch);
        _txtCountDown = findViewById(R.id.txt_record_countdown_sound);
        _txtCountDown.setVisibility(View.INVISIBLE);
        _txtRecordPath = findViewById(R.id.txt_record_path_sound);
        _txtRecordPath.setOnClickListener(this::onClick);
        _txtRecordPath.setVisibility(View.INVISIBLE);
    }

}
