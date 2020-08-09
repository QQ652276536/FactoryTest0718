package com.zistone.factorytest0718;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zistone.factorytest0718.face.ChooseDetectDegreeDialog;

public class FaceAttributeMenuActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "FaceAttributeMenuActivity";

    private TextView _txt;
    private Button _btnSetting, _btnActive, _btnFaceAttributeForImage, _btnFaceAttributeForVideo;
    private ChooseDetectDegreeDialog _chooseDetectDegreeDialog;

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
