package com.zistone.factorytest0718.face;

import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.Face3DAngle;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.enums.DetectMode;
import com.zistone.factorytest0718.R;
import com.zistone.factorytest0718.face.model.DrawInfo;
import com.zistone.factorytest0718.face.util.ConfigUtil;
import com.zistone.factorytest0718.face.util.DrawHelper;
import com.zistone.factorytest0718.face.util.CameraHelper;
import com.zistone.factorytest0718.face.util.CameraListener;
import com.zistone.factorytest0718.face.constants.RecognizeColor;
import com.zistone.factorytest0718.face.widget.FaceRectView;

import java.util.ArrayList;
import java.util.List;

/**
 * 人脸属性检测（视频）
 */
public class FaceAttributeDetectionVideoActivity extends AppCompatActivity implements
        ViewTreeObserver.OnGlobalLayoutListener, View.OnClickListener {

    private static final String TAG = "FaceAttributeDetectionVideoActivity";

    private CameraHelper _cameraHelper;
    private DrawHelper _drawHelper;
    private Camera.Size _size;
    private Integer _cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private FaceEngine _faceEngine;
    private int _afCode = -1;
    private int processMask = FaceEngine.ASF_AGE | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_GENDER | FaceEngine.ASF_LIVENESS;
    //相机预览显示的控件，可为SurfaceView或TextureView
    private View _previewView;
    private FaceRectView _faceRectView;
    private Button _btnReturn, _btnSwitch;

    private void InitEngine() {
        _faceEngine = new FaceEngine();
        _afCode = _faceEngine.init(this, DetectMode.ASF_DETECT_MODE_VIDEO, ConfigUtil.GetDetectionAngle(this), 16, 20, FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_AGE | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_GENDER | FaceEngine.ASF_LIVENESS);
        Log.i(TAG, "引擎初始化：" + _afCode);
        Toast.makeText(this, "引擎初始化成功", Toast.LENGTH_SHORT).show();
    }

    private void UnInitEngine() {
        if (_afCode == 0) {
            _afCode = _faceEngine.unInit();
            Log.i(TAG, "销毁引擎：" + _afCode);
        }
    }

    private void InitCamera() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        CameraListener cameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                Log.i(TAG, "摄像头打开：" + cameraId + "  " + displayOrientation + " " + isMirror);
                _size = camera.getParameters().getPreviewSize();
                _drawHelper = new DrawHelper(_size.width, _size.height, _previewView.getWidth(), _previewView.getHeight(), displayOrientation, cameraId, isMirror, false, false);
            }

            @Override
            public void onPreview(byte[] nv21, Camera camera) {
                if (_faceRectView != null) {
                    _faceRectView.ClearFaceInfo();
                }
                List<FaceInfo> faceInfoList = new ArrayList<>();
                //                long start = System.currentTimeMillis();
                int code = _faceEngine.detectFaces(nv21, _size.width, _size.height, FaceEngine.CP_PAF_NV21, faceInfoList);
                if (code == ErrorInfo.MOK && faceInfoList.size() > 0) {
                    code = _faceEngine.process(nv21, _size.width, _size.height, FaceEngine.CP_PAF_NV21, faceInfoList, processMask);
                    if (code != ErrorInfo.MOK) {
                        return;
                    }
                } else {
                    return;
                }
                List<AgeInfo> ageInfoList = new ArrayList<>();
                List<GenderInfo> genderInfoList = new ArrayList<>();
                List<Face3DAngle> face3DAngleList = new ArrayList<>();
                List<LivenessInfo> faceLivenessInfoList = new ArrayList<>();
                int ageCode = _faceEngine.getAge(ageInfoList);
                int genderCode = _faceEngine.getGender(genderInfoList);
                int face3DAngleCode = _faceEngine.getFace3DAngle(face3DAngleList);
                int livenessCode = _faceEngine.getLiveness(faceLivenessInfoList);
                //有其中一个的错误码不为ErrorInfo.MOK，return
                if ((ageCode | genderCode | face3DAngleCode | livenessCode) != ErrorInfo.MOK) {
                    return;
                }
                if (_faceRectView != null && _drawHelper != null) {
                    List<DrawInfo> drawInfoList = new ArrayList<>();
                    for (int i = 0; i < faceInfoList.size(); i++) {
                        drawInfoList.add(new DrawInfo(_drawHelper.AdjustRect(faceInfoList.get(i).getRect()), genderInfoList.get(i).getGender(), ageInfoList.get(i).getAge(), faceLivenessInfoList.get(i).getLiveness(), RecognizeColor.COLOR_UNKNOWN, null));
                    }
                    _drawHelper.Draw(_faceRectView, drawInfoList);
                }
            }

            @Override
            public void onCameraClosed() {
                Log.i(TAG, "相机关闭");
            }

            @Override
            public void onCameraError(Exception e) {
                Log.e(TAG, "相机错误");
            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
                if (_drawHelper != null) {
                    _drawHelper.setCameraDisplayOrientation(displayOrientation);
                }
                Log.i(TAG, "相机配置改变，相机ID：" + cameraID + "显示方向：" + displayOrientation);
            }
        };
        _cameraHelper = new CameraHelper.Builder().previewViewSize(new Point(_previewView.getMeasuredWidth(), _previewView.getMeasuredHeight())).rotation(getWindowManager().getDefaultDisplay().getRotation()).specificCameraId(_cameraId != null ? _cameraId : Camera.CameraInfo.CAMERA_FACING_FRONT).isMirror(false).previewOn(_previewView).cameraListener(cameraListener).build();
        _cameraHelper.init();
        _cameraHelper.start();
    }

    /**
     * 在{@link #_previewView}第一次布局完成后，去除该监听，并且进行引擎和相机的初始化
     */
    @Override
    public void onGlobalLayout() {
        _previewView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        InitEngine();
        InitCamera();
    }

    /**
     * 切换相机，注意：若切换相机发现检测不到人脸，则极有可能是检测角度导致的，需要销毁引擎重新创建或者在设置界面修改配置的检测角度
     */
    public void SwitchCamera() {
        if (_cameraHelper != null) {
            boolean success = _cameraHelper.switchCamera();
            if (!success) {
                Toast.makeText(this, "切换相机失败", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "相机已切换，若无法检测到人脸，需要在首页修改视频模式人脸检测角度", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (_cameraHelper != null) {
            _cameraHelper.release();
            _cameraHelper = null;
        }
        UnInitEngine();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_return_attribute_detection_video:
                if (_cameraHelper != null) {
                    _cameraHelper.release();
                    _cameraHelper = null;
                }
                UnInitEngine();
                finish();
                break;
            case R.id.btn_switch_attribute_detection_video:
                SwitchCamera();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_attribute_detection_video);
        //保持亮屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            getWindow().setAttributes(attributes);
        }
        //Activity启动后就锁定为启动时的方向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        _previewView = findViewById(R.id.texture_preview_attribute_detection_video);
        _faceRectView = findViewById(R.id.rect_view_attribute_detection_video);
        _btnReturn = findViewById(R.id.btn_return_attribute_detection_video);
        _btnReturn.setOnClickListener(this::onClick);
        _btnSwitch = findViewById(R.id.btn_switch_attribute_detection_video);
        _btnSwitch.setOnClickListener(this::onClick);
        //在布局结束后才做初始化操作
        _previewView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        InitEngine();
        InitCamera();
    }

}
