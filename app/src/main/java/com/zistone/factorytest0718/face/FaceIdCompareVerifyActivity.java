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
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceSimilar;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.enums.CompareModel;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.zistone.factorytest0718.BaseActivity;
import com.zistone.factorytest0718.R;
import com.zistone.factorytest0718.face.constants.LivenessType;
import com.zistone.factorytest0718.face.constants.RecognizeColor;
import com.zistone.factorytest0718.face.constants.RequestFeatureStatus;
import com.zistone.factorytest0718.face.constants.RequestLivenessStatus;
import com.zistone.factorytest0718.face.constants.CompareResult;
import com.zistone.factorytest0718.face.util.FaceServer;
import com.zistone.factorytest0718.face.model.DrawInfo;
import com.zistone.factorytest0718.face.model.FacePreviewInfo;
import com.zistone.factorytest0718.face.util.CameraHelper;
import com.zistone.factorytest0718.face.util.CameraListener;
import com.zistone.factorytest0718.face.util.ConfigUtil;
import com.zistone.factorytest0718.face.util.DrawHelper;
import com.zistone.factorytest0718.face.util.FaceHelper;
import com.zistone.factorytest0718.face.util.FaceListener;
import com.zistone.factorytest0718.face.widget.FaceRectView;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FaceIdCompareVerifyActivity extends BaseActivity implements ViewTreeObserver.OnGlobalLayoutListener {

    private static final String TAG = "FaceIdCompareVerifyActivity";
    private static final int MAX_DETECT_NUM = 10;
    //活体检等待的时间
    private static final int WAIT_LIVENESS_INTERVAL = 100;
    //失败重试间隔时间（ms）
    private static final long FAIL_RETRY_INTERVAL = 1000;
    //识别阈值
    private static final float SIMILAR_THRESHOLD = 0.8F;

    private CameraHelper _cameraHelper;
    private FaceHelper _faceHelper;
    private DrawHelper _drawHelper;
    private Camera.Size _previewSize;
    //优先打开的摄像头，本界面主要用于单目RGB摄像头设备，因此默认打开前置
    private Integer rgbCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
    //人脸检测引擎，用于预览帧人脸追踪
    private FaceEngine _faceDetectionEngine;
    private int _faceDetectionInitCode = -1;
    //用于特征提取的引擎
    private FaceEngine _faceFeatureEngine;
    private int _faceFeatureInitCode = -1;
    //检测引擎，用于预览帧人脸活体检测
    private FaceEngine _faceLivnessEngine;
    private int _faceLivnessInitCode = -1;
    //是否开启活体检测
    private boolean _livnessDetectionFlag = true;
    //用于记录人脸识别相关状态
    private ConcurrentHashMap<Integer, Integer> _featureStateMap = new ConcurrentHashMap<>();
    //用于记录特征提取出错重试次数
    private ConcurrentHashMap<Integer, Integer> _extractErrorRetryMap = new ConcurrentHashMap<>();
    //用于记录活体值
    private ConcurrentHashMap<Integer, Integer> _livnessMap = new ConcurrentHashMap<>();
    //用于记录活体检测出错重试次数
    private ConcurrentHashMap<Integer, Integer> _livnessErrorRetryMap = new ConcurrentHashMap<>();
    //特征提取延时
    private CompositeDisposable _featureDelayedDisposable = new CompositeDisposable();
    //人脸检测延时
    private CompositeDisposable _faceTaskDisposable = new CompositeDisposable();
    //相机预览显示的控件，可为SurfaceView或TextureView
    private View _previewView;
    //绘制人脸框的控件
    private FaceRectView _faceRectView;
    //活体检测开关
    private Switch _switch;

    public void Return(View view) {
        if (_cameraHelper != null) {
            _cameraHelper.release();
            _cameraHelper = null;
        }
        UnInitEngine();
        finish();
    }

    /**
     * 初始化引擎
     */
    private void InitEngine() {
        _faceDetectionEngine = new FaceEngine();
        _faceDetectionInitCode = _faceDetectionEngine.init(this, DetectMode.ASF_DETECT_MODE_VIDEO, ConfigUtil.GetDetectionAngle(this), 16, MAX_DETECT_NUM, FaceEngine.ASF_FACE_DETECT);
        if (_faceDetectionInitCode != ErrorInfo.MOK) {
            String error = "人脸引擎初始化失败，错误代码：" + _faceDetectionInitCode;
            Log.e(TAG, error);
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
        _faceFeatureEngine = new FaceEngine();
        _faceFeatureInitCode = _faceFeatureEngine.init(this, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY, 16, MAX_DETECT_NUM, FaceEngine.ASF_FACE_RECOGNITION);
        if (_faceFeatureInitCode != ErrorInfo.MOK) {
            String error = "特征引擎初始化失败，错误代码：" + _faceFeatureInitCode;
            Log.i(TAG, error);
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
        _faceLivnessEngine = new FaceEngine();
        _faceLivnessInitCode = _faceLivnessEngine.init(this, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY, 16, MAX_DETECT_NUM, FaceEngine.ASF_LIVENESS);
        if (_faceLivnessInitCode != ErrorInfo.MOK) {
            String error = "活体检测引擎初始化失败，错误代码：" + _faceLivnessInitCode;
            Log.i(TAG, error);
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 销毁引擎，_faceHelper中可能会有特征提取耗时操作仍在执行，加锁防止crash
     */
    private void UnInitEngine() {
        if (_faceDetectionInitCode == ErrorInfo.MOK && _faceDetectionEngine != null) {
            synchronized (_faceDetectionEngine) {
                int ftUnInitCode = _faceDetectionEngine.unInit();
                Log.i(TAG, "销毁引擎：" + ftUnInitCode);
            }
        }
        if (_faceFeatureInitCode == ErrorInfo.MOK && _faceFeatureEngine != null) {
            synchronized (_faceFeatureEngine) {
                int frUnInitCode = _faceFeatureEngine.unInit();
                Log.i(TAG, "销毁引擎：" + frUnInitCode);
            }
        }
        if (_faceLivnessInitCode == ErrorInfo.MOK && _faceLivnessEngine != null) {
            synchronized (_faceLivnessEngine) {
                int flUnInitCode = _faceLivnessEngine.unInit();
                Log.i(TAG, "销毁引擎：" + flUnInitCode);
            }
        }
    }

    /**
     * 初始化相机
     */
    private void InitCamera() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        /**
         * 人脸监听
         */
        final FaceListener faceListener = new FaceListener() {

            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "预览失败：" + e.getMessage());
            }

            @Override
            public void onFaceFeatureInfoGet(@Nullable final FaceFeature faceFeature, final Integer requestId, final Integer errorCode) {
                //从预览上提取到特征
                if (faceFeature != null) {
                    Integer liveness = _livnessMap.get(requestId);
                    //不做活体检测，直接搜索
                    if (!_livnessDetectionFlag) {
                        SearchFace(faceFeature, requestId);
                    }
                    //活体检测通过，搜索特征
                    else if (liveness != null && liveness == LivenessInfo.ALIVE) {
                        SearchFace(faceFeature, requestId);
                    }
                    //活体检测未出结果，或者非活体，延迟执行该函数
                    else {
                        if (_featureStateMap.containsKey(requestId)) {
                            Observable.timer(WAIT_LIVENESS_INTERVAL, TimeUnit.MILLISECONDS).subscribe(new Observer<Long>() {
                                Disposable disposable;

                                @Override
                                public void onSubscribe(Disposable d) {
                                    disposable = d;
                                    _featureDelayedDisposable.add(disposable);
                                }

                                @Override
                                public void onNext(Long aLong) {
                                    onFaceFeatureInfoGet(faceFeature, requestId, errorCode);
                                }

                                @Override
                                public void onError(Throwable e) {
                                }

                                @Override
                                public void onComplete() {
                                    _featureDelayedDisposable.remove(disposable);
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onFaceLivenessInfoGet(@Nullable LivenessInfo livenessInfo, final Integer requestId, Integer errorCode) {
                if (livenessInfo != null) {
                    int liveness = livenessInfo.getLiveness();
                    _livnessMap.put(requestId, liveness);
                    //非活体，重试
                    if (liveness == LivenessInfo.NOT_ALIVE) {
                        _faceHelper.SetName(requestId, "非活体");
                        //延迟进行活体检测，将该人脸的活体状态置为UNKNOWN，帧回调处理时会重新进行活体检测
                        RetryLivenessDetectionDelayed(requestId);
                    }
                }
            }
        };
        /**
         * 相机监听
         */
        CameraListener cameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                Camera.Size lastPreviewSize = _previewSize;
                _previewSize = camera.getParameters().getPreviewSize();
                _drawHelper = new DrawHelper(_previewSize.width, _previewSize.height, _previewView.getWidth(), _previewView.getHeight(), displayOrientation, cameraId, isMirror, false, false);
                Log.i(TAG, "相机打开：" + _drawHelper.toString());
                // 切换相机的时候可能会导致预览尺寸发生变化
                if (_faceHelper == null || lastPreviewSize == null || lastPreviewSize.width != _previewSize.width || lastPreviewSize.height != _previewSize.height) {
                    Integer trackedFaceCount = null;
                    // 记录切换时的人脸序号
                    if (_faceHelper != null) {
                        trackedFaceCount = _faceHelper.GetTrackedFaceCount();
                        _faceHelper.Release();
                    }
                    _faceHelper = new FaceHelper.Builder().SetFaceDetectionEngine(_faceDetectionEngine).SetFaceFeatureEngine(_faceFeatureEngine).SetFaceLivnessEngine(_faceLivnessEngine).SetFaceFeatureQueueSize(MAX_DETECT_NUM).SetFaceLivnessQueueSize(MAX_DETECT_NUM).SetPreviewSize(_previewSize).SetFaceListener(faceListener).SetTrackedFaceCount(trackedFaceCount == null ? ConfigUtil.GetTrackedFaceCount(FaceIdCompareVerifyActivity.this.getApplicationContext()) : trackedFaceCount).Build();
                }
            }


            @Override
            public void onPreview(final byte[] nv21, Camera camera) {
                if (_faceRectView != null) {
                    _faceRectView.ClearFaceInfo();
                }
                List<FacePreviewInfo> facePreviewInfoList = _faceHelper.OnPreviewFrame(nv21);
                if (facePreviewInfoList != null && _faceRectView != null && _drawHelper != null) {
                    DrawPreviewInfo(facePreviewInfoList);
                }
                //清除离开的人脸
                ClearLeaveFace(facePreviewInfoList);
                if (facePreviewInfoList != null && facePreviewInfoList.size() > 0 && _previewSize != null) {
                    for (int i = 0; i < facePreviewInfoList.size(); i++) {
                        Integer status = _featureStateMap.get(facePreviewInfoList.get(i).getTrackId());
                        //在活体检测开启，在人脸识别状态不为成功或人脸活体状态不为处理中（ANALYZING）且不为处理完成（ALIVE、NOT_ALIVE）时重新进行活体检测
                        if (_livnessDetectionFlag && (status == null || status != RequestFeatureStatus.SUCCEED)) {
                            Integer liveness = _livnessMap.get(facePreviewInfoList.get(i).getTrackId());
                            if (liveness == null || (liveness != LivenessInfo.ALIVE && liveness != LivenessInfo.NOT_ALIVE && liveness != RequestLivenessStatus.ANALYZING)) {
                                _livnessMap.put(facePreviewInfoList.get(i).getTrackId(), RequestLivenessStatus.ANALYZING);
                                _faceHelper.RequestFaceLiveness(nv21, facePreviewInfoList.get(i).getFaceInfo(), _previewSize.width, _previewSize.height, FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId(), LivenessType.RGB);
                            }
                        }
                        //对于每个人脸，若状态为空或者为失败，则请求特征提取（可根据需要添加其他判断以限制特征提取次数），
                        //特征提取回传的特征结果在{@link FaceListener#onFaceFeatureInfoGet(FaceFeature, Integer, Integer)}中回传
                        if (status == null || status == RequestFeatureStatus.TO_RETRY) {
                            _featureStateMap.put(facePreviewInfoList.get(i).getTrackId(), RequestFeatureStatus.SEARCHING);
                            _faceHelper.RequestFaceFeature(nv21, facePreviewInfoList.get(i).getFaceInfo(), _previewSize.width, _previewSize.height, FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId());
                        }
                    }
                }
            }

            @Override
            public void onCameraClosed() {
                Log.i(TAG, "相机关闭");
            }

            @Override
            public void onCameraError(Exception e) {
                Log.i(TAG, "调用相机发生错误：" + e.getMessage());
            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
                if (_drawHelper != null) {
                    _drawHelper.setCameraDisplayOrientation(displayOrientation);
                }
                Log.i(TAG, "相机配置改变：" + cameraID + "  " + displayOrientation);
            }
        };
        _cameraHelper = new CameraHelper.Builder().previewSize(new Point(_previewView.getMeasuredWidth(), _previewView.getMeasuredHeight())).rotation(getWindowManager().getDefaultDisplay().getRotation()).specificCameraId(rgbCameraID != null ? rgbCameraID : Camera.CameraInfo.CAMERA_FACING_FRONT).isMirror(false).previewOn(_previewView).cameraListener(cameraListener).build();
        _cameraHelper.init();
        _cameraHelper.start();
    }

    /**
     * 绘制预览内容
     *
     * @param facePreviewInfoList
     */
    private void DrawPreviewInfo(List<FacePreviewInfo> facePreviewInfoList) {
        List<DrawInfo> drawInfoList = new ArrayList<>();
        for (int i = 0; i < facePreviewInfoList.size(); i++) {
            Integer liveness = _livnessMap.get(facePreviewInfoList.get(i).getTrackId());
            Integer recognizeStatus = _featureStateMap.get(facePreviewInfoList.get(i).getTrackId());
            int color = RecognizeColor.COLOR_FAILED;
            //根据识别结果和活体结果设置颜色
            if (recognizeStatus != null) {
                if (recognizeStatus == RequestFeatureStatus.FAILED) {
                    color = RecognizeColor.COLOR_FAILED;
                    drawInfoList.add(new DrawInfo(_drawHelper.AdjustRect(facePreviewInfoList.get(i).getFaceInfo().getRect()), -1, 0, -1, color, "未通过"));
                }
                if (recognizeStatus == RequestFeatureStatus.SUCCEED) {
                    color = RecognizeColor.COLOR_SUCCESS;
                    drawInfoList.add(new DrawInfo(_drawHelper.AdjustRect(facePreviewInfoList.get(i).getFaceInfo().getRect()), -1, 0, -1, color, "通过"));
                }
            }
            if (liveness != null && liveness == LivenessInfo.NOT_ALIVE) {
                color = RecognizeColor.COLOR_FAILED;
                drawInfoList.add(new DrawInfo(_drawHelper.AdjustRect(facePreviewInfoList.get(i).getFaceInfo().getRect()), -1, 0, -1, color, "未通过"));
            }
        }
        _drawHelper.Draw(_faceRectView, drawInfoList);
    }

    /**
     * 删除已经离开的人脸
     *
     * @param facePreviewInfoList
     */
    private void ClearLeaveFace(List<FacePreviewInfo> facePreviewInfoList) {
        if (facePreviewInfoList == null || facePreviewInfoList.size() == 0) {
            _featureStateMap.clear();
            _livnessMap.clear();
            _livnessErrorRetryMap.clear();
            _extractErrorRetryMap.clear();
            if (_featureDelayedDisposable != null) {
                _featureDelayedDisposable.clear();
            }
            return;
        }
        Enumeration<Integer> keys = _featureStateMap.keys();
        while (keys.hasMoreElements()) {
            int key = keys.nextElement();
            boolean contained = false;
            for (FacePreviewInfo facePreviewInfo : facePreviewInfoList) {
                if (facePreviewInfo.getTrackId() == key) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                _featureStateMap.remove(key);
                _livnessMap.remove(key);
                _livnessErrorRetryMap.remove(key);
                _extractErrorRetryMap.remove(key);
            }
        }
    }

    /**
     * 从特征库搜索，如果能搜索到则说明人证一致
     *
     * @param frFace
     * @param requestId
     */
    private void SearchFace(final FaceFeature frFace, final Integer requestId) {
        Observable.create(new ObservableOnSubscribe<CompareResult>() {
            @Override
            public void subscribe(ObservableEmitter<CompareResult> emitter) {
                Log.i(TAG, "人脸库中的人脸数：" + FaceServer.getInstance().GetFaceNumber(FaceIdCompareVerifyActivity.this));
                CompareResult compareResult = FaceServer.getInstance().GetTopOfFaceLib(frFace);
                emitter.onNext(compareResult);
            }
        }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<CompareResult>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(CompareResult compareResult) {
                if (compareResult == null || compareResult.getUserName() == null) {
                    _featureStateMap.put(requestId, RequestFeatureStatus.FAILED);
                    return;
                }
                Log.i(TAG, "与人脸库中的相似度：" + compareResult.getSimilar());
                if (compareResult.getSimilar() > SIMILAR_THRESHOLD) {
                    //添加显示人员时，保存其trackId
                    compareResult.setTrackId(requestId);
                    _featureStateMap.put(requestId, RequestFeatureStatus.SUCCEED);
                    _faceHelper.SetName(requestId, "通过");
                } else {
                    _featureStateMap.put(requestId, RequestFeatureStatus.FAILED);
                    _faceHelper.SetName(requestId, "未通过");
                    RetryRecognizeDelayed(requestId);
                }
            }

            @Override
            public void onError(Throwable e) {
                _faceHelper.SetName(requestId, "未通过");
                RetryRecognizeDelayed(requestId);
            }

            @Override
            public void onComplete() {
            }
        });
    }

    /**
     * 切换相机。注意：若切换相机发现检测不到人脸，则极有可能是检测角度导致的，需要销毁引擎重新创建或者在设置界面修改配置的检测角度
     *
     * @param view
     */
    public void SwitchCamera(View view) {
        if (_cameraHelper != null) {
            boolean success = _cameraHelper.switchCamera();
            if (!success) {
                Toast.makeText(this, "切换相机失败", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "相机已切换，若无法检测到人脸，需要在首页修改视频模式人脸检测角度", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 延迟重新进行活体检测
     *
     * @param requestId 人脸ID
     */
    private void RetryLivenessDetectionDelayed(final Integer requestId) {
        Observable.timer(FAIL_RETRY_INTERVAL, TimeUnit.MILLISECONDS).subscribe(new Observer<Long>() {
            Disposable disposable;

            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
                _faceTaskDisposable.add(disposable);
            }

            @Override
            public void onNext(Long aLong) {
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                //将该人脸状态置为UNKNOWN，帧回调处理时会重新进行活体检测
                if (_livnessDetectionFlag) {
                    _faceHelper.SetName(requestId, Integer.toString(requestId));
                }
                _livnessMap.put(requestId, LivenessInfo.UNKNOWN);
                _faceTaskDisposable.remove(disposable);
            }
        });
    }

    /**
     * 延迟重新进行人脸识别
     *
     * @param requestId 人脸ID
     */
    private void RetryRecognizeDelayed(final Integer requestId) {
        _featureStateMap.put(requestId, RequestFeatureStatus.FAILED);
        Observable.timer(FAIL_RETRY_INTERVAL, TimeUnit.MILLISECONDS).subscribe(new Observer<Long>() {
            Disposable disposable;

            @Override
            public void onSubscribe(Disposable d) {
                disposable = d;
                _faceTaskDisposable.add(disposable);
            }

            @Override
            public void onNext(Long aLong) {
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                //将该特征提取状态置为FAILED，帧回调处理时会重新进行活体检测
                _faceHelper.SetName(requestId, Integer.toString(requestId));
                _featureStateMap.put(requestId, RequestFeatureStatus.TO_RETRY);
                _faceTaskDisposable.remove(disposable);
            }
        });
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

    @Override
    protected void onDestroy() {
        if (_cameraHelper != null) {
            _cameraHelper.release();
            _cameraHelper = null;
        }
        UnInitEngine();
        if (_featureDelayedDisposable != null) {
            _featureDelayedDisposable.clear();
        }
        if (_faceTaskDisposable != null) {
            _faceTaskDisposable.clear();
        }
        if (_faceHelper != null) {
            ConfigUtil.SetTrackedFaceCount(this, _faceHelper.GetTrackedFaceCount());
            _faceHelper.Release();
            _faceHelper = null;
        }
        //这个Activity结束时不销毁人脸库的操作，在前面选取证件照的Activity里销毁，避免前面注册失败
        //        FaceServer.getInstance().UnInit();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_id_compare_verify);
        //保持亮屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            getWindow().setAttributes(attributes);
        }
        //本地人脸库初始化
        FaceServer.getInstance().Init(this);
        _previewView = findViewById(R.id.single_camera_texture_preview);
        _previewView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        _faceRectView = findViewById(R.id.single_camera_face_rect_view);
        _switch = findViewById(R.id.single_camera_switch_liveness_detect);
        _switch.setChecked(_livnessDetectionFlag);
        _switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                _livnessDetectionFlag = isChecked;
            }
        });
    }

}
