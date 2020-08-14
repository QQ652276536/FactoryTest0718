package com.zistone.factorytest0718;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.enums.RuntimeABI;
import com.zistone.factorytest0718.face.ChooseDetectDegreeDialog;
import com.zistone.factorytest0718.face.FaceIdCompareMenuActivity;
import com.zistone.factorytest0718.face.FaceIdCompareVerifyActivity;
import com.zistone.factorytest0718.face.constants.Constants;
import com.zistone.factorytest0718.face.FaceAttributeDetectionVideoActivity;
import com.zistone.factorytest0718.face.FaceAttributeDetectionImageActivity;
import com.zistone.factorytest0718.face.FaceCompareImageActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    private Button _btnSetting, _btnActive, _btnFaceAttributeForImage, _btnFaceAttributeForVideo, _btnFaceCompareImage, _btnFaceCompareVideo, _btnFaceIdCompare;
    private ChooseDetectDegreeDialog _chooseDetectDegreeDialog;

    /**
     * 激活
     */
    public void ActiveEngine() {
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
                    _txt.setText("激活成功");
                } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                    _txt.setText("引擎已激活");
                } else {
                    _txt.setText("激活失败，错误码为：" + activeCode);
                }
                ActiveFileInfo activeFileInfo = new ActiveFileInfo();
                int res = FaceEngine.getActiveFileInfo(FaceAttributeMenuActivity.this, activeFileInfo);
                if (res == ErrorInfo.MOK) {
                    Log.i(TAG, activeFileInfo.toString());
                }
            }

            @Override
            public void onError(Throwable e) {
                _txt.setText(e.getMessage());
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
            //设置检测角度
            case R.id.btn_setting_face_attribute_menu:
                if (_chooseDetectDegreeDialog == null) {
                    _chooseDetectDegreeDialog = new ChooseDetectDegreeDialog();
                }
                if (_chooseDetectDegreeDialog.isAdded()) {
                    _chooseDetectDegreeDialog.dismiss();
                }
                _chooseDetectDegreeDialog.show(getSupportFragmentManager(), ChooseDetectDegreeDialog.class.getSimpleName());
                break;
            //激活引擎
            case R.id.btn_active_face_attribute_menu:
                ActiveEngine();
                break;
            //显示图片中所有人脸的信息，并且逐一比对相似度
            case R.id.btn_face_attribute_image_menu:
                startActivity(new Intent(this, FaceAttributeDetectionImageActivity.class));
                break;
            //显示视频中所有人脸信息
            case R.id.btn_face_attribute_video_menu:
                startActivity(new Intent(this, FaceAttributeDetectionVideoActivity.class));
                break;
                //人脸比对1：N（图片VS图片）
            case R.id.btn_face_compare_img_menu:
                startActivity(new Intent(this, FaceCompareImageActivity.class));
                break;
                //人脸比对1：N（视频VS人脸库，RGB活体）
            case R.id.btn_face_compare_video_menu:
                break;
                //人证核验
            case R.id.btn_faceid_compare_menu:
                startActivity(new Intent(this, FaceIdCompareMenuActivity.class));
                break;
        }
    }

    /**
     * 检查能否找到动态链接库
     *
     * @param
     * @return
     */
    private boolean CheckSoFile(String[] libraries) {
        File dir = new File(getApplicationInfo().nativeLibraryDir);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return false;
        }
        List<String> libraryNameList = new ArrayList<>();
        for (File file : files) {
            libraryNameList.add(file.getName());
        }
        boolean exists = true;
        for (String library : libraries) {
            exists &= libraryNameList.contains(library);
        }
        return exists;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_bankcard);
        SetBaseContentView(R.layout.activity_face_attribute_menu);
        ApplicationInfo applicationInfo = getApplicationInfo();
        Log.i(TAG, "onCreate: " + applicationInfo.nativeLibraryDir);
        _txt = findViewById(R.id.txt_face_attribute_menu);
        _btnSetting = findViewById(R.id.btn_setting_face_attribute_menu);
        _btnSetting.setOnClickListener(this::onClick);
        _btnActive = findViewById(R.id.btn_active_face_attribute_menu);
        _btnActive.setOnClickListener(this::onClick);
        _btnFaceAttributeForImage = findViewById(R.id.btn_face_attribute_image_menu);
        _btnFaceAttributeForImage.setOnClickListener(this::onClick);
        _btnFaceAttributeForVideo = findViewById(R.id.btn_face_attribute_video_menu);
        _btnFaceAttributeForVideo.setOnClickListener(this::onClick);
        _btnFaceCompareImage = findViewById(R.id.btn_face_compare_img_menu);
        _btnFaceCompareImage.setOnClickListener(this::onClick);
        _btnFaceCompareVideo = findViewById(R.id.btn_face_compare_video_menu);
        _btnFaceCompareVideo.setOnClickListener(this::onClick);
        _btnFaceIdCompare = findViewById(R.id.btn_faceid_compare_menu);
        _btnFaceIdCompare.setOnClickListener(this::onClick);
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
        if (!CheckSoFile(LIBRARIES)) {
            _txt.setText("未找到库文件，请检查是否有将.so文件放至工程的app\\src\\main\\jniLibs目录下");
            _txt.setTextColor(Color.RED);
            _btnActive.setEnabled(false);
            _btnSetting.setEnabled(false);
            _btnFaceAttributeForImage.setEnabled(false);
            _btnFaceAttributeForVideo.setEnabled(false);
        }
    }

}
