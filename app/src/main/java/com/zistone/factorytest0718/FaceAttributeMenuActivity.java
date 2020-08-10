package com.zistone.factorytest0718;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.enums.RuntimeABI;
import com.zistone.factorytest0718.face.ChooseDetectDegreeDialog;
import com.zistone.factorytest0718.face.Constants;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FaceAttributeMenuActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "FaceAttributeMenuActivity";

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    //在线激活所需的权限
    private static final String[] NEEDED_PERMISSIONS = new String[]{Manifest.permission.READ_PHONE_STATE};
    //所需的动态库文件
    private static final String[] LIBRARIES = new String[]{
            //人脸相关
            "libarcsoft_face_engine.so", "libarcsoft_face.so",
            //图像库相关
            "libarcsoft_image_util.so",};

    private TextView _txt;
    private Button _btnSetting, _btnActive, _btnFaceAttributeForImage, _btnFaceAttributeForVideo;
    private ChooseDetectDegreeDialog _chooseDetectDegreeDialog;
    private boolean _libraryExists = true;

    /**
     * 激活
     */
    public void ActiveEngine() {
        //所需要的SO库不存在
        if (!_libraryExists) {
            Toast.makeText(this, "未找到库文件，请检查是否有将.so文件放至工程的 app\\src\\main\\jniLibs 目录下", Toast.LENGTH_SHORT).show();
            return;
        }
        //未授予权限
        if (!CheckPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
            return;
        }
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) {
                RuntimeABI runtimeABI = FaceEngine.getRuntimeABI();
                Log.i(TAG, "subscribe: getRuntimeABI() " + runtimeABI);

                long start = System.currentTimeMillis();
                int activeCode = FaceEngine.activeOnline(FaceAttributeMenuActivity.this, Constants.APP_ID, Constants.SDK_KEY);
                Log.i(TAG, "subscribe cost: " + (System.currentTimeMillis() - start));
                emitter.onNext(activeCode);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Integer activeCode) {
                if (activeCode == ErrorInfo.MOK) {
                    Toast.makeText(FaceAttributeMenuActivity.this, "激活成功", Toast.LENGTH_SHORT).show();
                } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                    Toast.makeText(FaceAttributeMenuActivity.this, "引擎已激活", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FaceAttributeMenuActivity.this, "激活失败，错误码为：" + activeCode, Toast.LENGTH_SHORT).show();
                }
                ActiveFileInfo activeFileInfo = new ActiveFileInfo();
                int res = FaceEngine.getActiveFileInfo(FaceAttributeMenuActivity.this, activeFileInfo);
                if (res == ErrorInfo.MOK) {
                    Log.i(TAG, activeFileInfo.toString());
                }
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(FaceAttributeMenuActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() {
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_setting_face_attribute_menu:
                if (_chooseDetectDegreeDialog == null) {
                    _chooseDetectDegreeDialog = new ChooseDetectDegreeDialog();
                }
                if (_chooseDetectDegreeDialog.isAdded()) {
                    _chooseDetectDegreeDialog.dismiss();
                }
                _chooseDetectDegreeDialog.show(getSupportFragmentManager(), ChooseDetectDegreeDialog.class.getSimpleName());
                break;
            case R.id.btn_active_face_attribute_menu:
                ActiveEngine();
                break;
            case R.id.btn_face_attribute_image_menu:
                break;
            case R.id.btn_face_attribute_video_menu:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_bankcard);
        SetBaseContentView(R.layout.activity_face_attribute_menu);
        _txt = findViewById(R.id.txt_bankcard);
        _btnSetting = findViewById(R.id.btn_setting_face_attribute_menu);
        _btnSetting.setOnClickListener(this::onClick);
        _btnActive = findViewById(R.id.btn_active_face_attribute_menu);
        _btnActive.setOnClickListener(this::onClick);
        _btnFaceAttributeForImage = findViewById(R.id.btn_face_attribute_image_menu);
        _btnFaceAttributeForImage.setOnClickListener(this::onClick);
        _btnFaceAttributeForVideo = findViewById(R.id.btn_face_attribute_video_menu);
        _btnFaceAttributeForVideo.setOnClickListener(this::onClick);
        _btnPass.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putExtra(ARG_PARAM1, PASS);
            setResult(RESULT_OK, intent);
            finish();
        });
        _btnFail.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putExtra(ARG_PARAM1, FAIL);
            setResult(RESULT_OK, intent);
            finish();
        });
    }

}
