package com.zistone.factorytest0718.face.util;

import android.hardware.Camera;
import android.util.Log;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.LivenessInfo;
import com.zistone.factorytest0718.face.constants.LivenessType;
import com.zistone.factorytest0718.face.model.FacePreviewInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 人脸操作辅助类
 */
public class FaceHelper {

    public static final class Builder {
        private FaceEngine _ftEngine;
        private FaceEngine _frEngine;
        private FaceEngine _flEngine;
        private Camera.Size _previewSize;
        private FaceListener _faceListener;
        private int _frQueueSize;
        private int _flQueueSize;
        private int _trackedFaceCount;

        public FaceHelper build() {
            return new FaceHelper(this);
        }
        public Builder ftEngine(FaceEngine val) {
            _ftEngine = val;
            return this;
        }

        public Builder frEngine(FaceEngine val) {
            _frEngine = val;
            return this;
        }

        public Builder flEngine(FaceEngine val) {
            _flEngine = val;
            return this;
        }


        public Builder previewSize(Camera.Size val) {
            _previewSize = val;
            return this;
        }


        public Builder faceListener(FaceListener val) {
            _faceListener = val;
            return this;
        }

        public Builder frQueueSize(int val) {
            _frQueueSize = val;
            return this;
        }

        public Builder flQueueSize(int val) {
            _flQueueSize = val;
            return this;
        }

        public Builder trackedFaceCount(int val) {
            _trackedFaceCount = val;
            return this;
        }
    }

    /**
     * 人脸特征提取线程
     */
    public class FaceRecognizeRunnable implements Runnable {

        private FaceInfo _faceInfo;
        private int _width;
        private int _height;
        private int _format;
        private Integer _trackId;
        private byte[] _nv21Data;

        private FaceRecognizeRunnable(byte[] nv21Data, FaceInfo faceInfo, int width, int height, int format, Integer trackId) {
            if (nv21Data == null) {
                return;
            }
            this._nv21Data = nv21Data;
            this._faceInfo = new FaceInfo(faceInfo);
            this._width = width;
            this._height = height;
            this._format = format;
            this._trackId = trackId;
        }

        @Override
        public void run() {
            if (_faceListener != null && _nv21Data != null) {
                if (_frEngine != null) {
                    FaceFeature faceFeature = new FaceFeature();
                    long frStartTime = System.currentTimeMillis();
                    int frCode;
                    synchronized (_frEngine) {
                        frCode = _frEngine.extractFaceFeature(_nv21Data, _width, _height, _format, _faceInfo, faceFeature);
                    }
                    if (frCode == ErrorInfo.MOK) {
                        Log.i(TAG, "人脸特征提取耗时：" + (System.currentTimeMillis() - frStartTime) + "ms");
                        _faceListener.onFaceFeatureInfoGet(faceFeature, _trackId, frCode);
                    } else {
                        _faceListener.onFaceFeatureInfoGet(null, _trackId, frCode);
                        _faceListener.onFail(new Exception("人脸特征提取失败，错误代码：" + frCode));
                    }
                } else {
                    _faceListener.onFaceFeatureInfoGet(null, _trackId, ERROR_FR_ENGINE_IS_NULL);
                    _faceListener.onFail(new Exception("人脸特征提取失败，引擎为空！"));
                }
            }
            _nv21Data = null;
        }
    }

    /**
     * 活体检测的线程
     */
    public class FaceLivenessDetectRunnable implements Runnable {
        private FaceInfo _faceInfo;
        private int _width;
        private int _height;
        private int _format;
        private Integer _trackId;
        private byte[] _nv21Data;
        private LivenessType _livenessType;

        private FaceLivenessDetectRunnable(byte[] nv21Data, FaceInfo faceInfo, int width, int height, int format, Integer trackId, LivenessType livenessType) {
            if (nv21Data == null) {
                return;
            }
            this._nv21Data = nv21Data;
            this._faceInfo = new FaceInfo(faceInfo);
            this._width = width;
            this._height = height;
            this._format = format;
            this._trackId = trackId;
            this._livenessType = livenessType;
        }

        @Override
        public void run() {
            if (_faceListener != null && _nv21Data != null) {
                if (_flEngine != null) {
                    List<LivenessInfo> livenessInfoList = new ArrayList<>();
                    int flCode;
                    synchronized (_flEngine) {
                        if (_livenessType == LivenessType.RGB) {
                            flCode = _flEngine.process(_nv21Data, _width, _height, _format, Arrays.asList(_faceInfo), FaceEngine.ASF_LIVENESS);
                        } else {
                            flCode = _flEngine.processIr(_nv21Data, _width, _height, _format, Arrays.asList(_faceInfo), FaceEngine.ASF_IR_LIVENESS);
                        }
                    }
                    if (flCode == ErrorInfo.MOK) {
                        if (_livenessType == LivenessType.RGB) {
                            flCode = _flEngine.getLiveness(livenessInfoList);
                        } else {
                            flCode = _flEngine.getIrLiveness(livenessInfoList);
                        }
                    }

                    if (flCode == ErrorInfo.MOK && livenessInfoList.size() > 0) {
                        _faceListener.onFaceLivenessInfoGet(livenessInfoList.get(0), _trackId, flCode);
                    } else {
                        _faceListener.onFaceLivenessInfoGet(null, _trackId, flCode);
                        _faceListener.onFail(new Exception("活体检测失败，错误代码：" + flCode));
                    }
                } else {
                    _faceListener.onFaceLivenessInfoGet(null, _trackId, ERROR_FL_ENGINE_IS_NULL);
                    _faceListener.onFail(new Exception("活体检测失败，引擎为空！"));
                }
            }
            _nv21Data = null;
        }
    }

    private static final String TAG = "FaceHelper";
    //线程池正在处理任务
    private static final int ERROR_BUSY = -1;
    //特征提取引擎为空
    private static final int ERROR_FR_ENGINE_IS_NULL = -2;
    //活体检测引擎为空
    private static final int ERROR_FL_ENGINE_IS_NULL = -3;

    //人脸追踪引擎
    private FaceEngine _ftEngine;
    //特征提取引擎
    private FaceEngine _frEngine;
    //活体检测引擎
    private FaceEngine _flEngine;
    private Camera.Size _size;
    private List<FaceInfo> _faceInfoList = new ArrayList<>();
    //特征提取线程池
    private ExecutorService _frExecutor;
    //活体检测线程池
    private ExecutorService _flExecutor;
    //特征提取线程队列
    private LinkedBlockingQueue<Runnable> _frThreadQueue;
    //活体检测线程队列
    private LinkedBlockingQueue<Runnable> _flThreadQueue;
    private FaceListener _faceListener;
    //上次应用退出时，记录的该App检测过的人脸数了
    private int _trackedFaceCount = 0;
    //本次打开引擎后的最大faceId
    private int _currentMaxFaceId = 0;
    private List<Integer> _currentTrackIdList = new ArrayList<>();
    private List<FacePreviewInfo> _facePreviewInfoList = new ArrayList<>();
    //用于存储人脸对应的姓名，KEY为trackId，VALUE为姓名
    private ConcurrentHashMap<Integer, String> _nameMap = new ConcurrentHashMap<>();

    private FaceHelper(Builder builder) {
        _ftEngine = builder._ftEngine;
        _faceListener = builder._faceListener;
        _trackedFaceCount = builder._trackedFaceCount;
        _size = builder._previewSize;
        _frEngine = builder._frEngine;
        _flEngine = builder._flEngine;
        //特征提取线程队列大小
        int frQueueSize = 5;
        if (builder._frQueueSize > 0) {
            frQueueSize = builder._frQueueSize;
        } else {
            Log.e(TAG, "线程数必须大于0，现在使用默认值：" + frQueueSize);
        }
        _frThreadQueue = new LinkedBlockingQueue<>(frQueueSize);
        _frExecutor = new ThreadPoolExecutor(1, frQueueSize, 0, TimeUnit.MILLISECONDS, _frThreadQueue);
        //活体检测线程队列大小
        int flQueueSize = 5;
        if (builder._flQueueSize > 0) {
            flQueueSize = builder._flQueueSize;
        } else {
            Log.e(TAG, "线程数必须大于0，现在使用默认值：" + flQueueSize);
        }
        _flThreadQueue = new LinkedBlockingQueue<Runnable>(flQueueSize);
        _flExecutor = new ThreadPoolExecutor(1, flQueueSize, 0, TimeUnit.MILLISECONDS, _flThreadQueue);
        if (_size == null) {
            throw new RuntimeException("必须指定预览图像大小！");
        }
    }

    /**
     * 请求获取人脸特征数据
     *
     * @param nv21     图像数据
     * @param faceInfo 人脸信息
     * @param width    图像宽度
     * @param height   图像高度
     * @param format   图像格式
     * @param trackId  请求人脸特征的唯一请求码，一般使用trackId
     */
    public void RequestFaceFeature(byte[] nv21, FaceInfo faceInfo, int width, int height, int format, Integer trackId) {
        if (_faceListener != null) {
            if (_frEngine != null && _frThreadQueue.remainingCapacity() > 0) {
                _frExecutor.execute(new FaceRecognizeRunnable(nv21, faceInfo, width, height, format, trackId));
            } else {
                _faceListener.onFaceFeatureInfoGet(null, trackId, ERROR_BUSY);
            }
        }
    }

    /**
     * 请求获取活体检测结果，需要传入活体的参数，以下参数同
     *
     * @param nv21         NV21格式的图像数据
     * @param faceInfo     人脸信息
     * @param width        图像宽度
     * @param height       图像高度
     * @param format       图像格式
     * @param trackId      请求人脸特征的唯一请求码，一般使用trackId
     * @param livenessType 活体检测类型
     */
    public void RequestFaceLiveness(byte[] nv21, FaceInfo faceInfo, int width, int height, int format, Integer trackId, LivenessType livenessType) {
        if (_faceListener != null) {
            if (_flEngine != null && _flThreadQueue.remainingCapacity() > 0) {
                _flExecutor.execute(new FaceLivenessDetectRunnable(nv21, faceInfo, width, height, format, trackId, livenessType));
            } else {
                _faceListener.onFaceLivenessInfoGet(null, trackId, ERROR_BUSY);
            }
        }
    }

    /**
     * 释放对象
     */
    public void Release() {
        if (!_frExecutor.isShutdown()) {
            _frExecutor.shutdownNow();
            _frThreadQueue.clear();
        }
        if (!_flExecutor.isShutdown()) {
            _flExecutor.shutdownNow();
            _flThreadQueue.clear();
        }
        if (_faceInfoList != null) {
            _faceInfoList.clear();
        }
        if (_frThreadQueue != null) {
            _frThreadQueue.clear();
            _frThreadQueue = null;
        }
        if (_flThreadQueue != null) {
            _flThreadQueue.clear();
            _flThreadQueue = null;
        }
        if (_nameMap != null) {
            _nameMap.clear();
        }
        _nameMap = null;
        _faceListener = null;
        _faceInfoList = null;
    }

    /**
     * 处理帧数据
     *
     * @param nv21 相机预览回传的NV21数据
     * @return 实时人脸处理结果，封装添加了一个trackId，trackId的获取依赖于faceId，用于记录人脸序号并保存
     */
    public List<FacePreviewInfo> OnPreviewFrame(byte[] nv21) {
        if (_faceListener != null) {
            if (_ftEngine != null) {
                _faceInfoList.clear();
                int code = _ftEngine.detectFaces(nv21, _size.width, _size.height, FaceEngine.CP_PAF_NV21, _faceInfoList);
                if (code != ErrorInfo.MOK) {
                    _faceListener.onFail(new Exception("处理帧数据失败，错误代码：" + code));
                } else {
                }
                //若需要多人脸搜索，删除此行代码
                TrackUtil.KeepMaxFace(_faceInfoList);
                //刷新
                RefreshTrackId(_faceInfoList);
            }
            _facePreviewInfoList.clear();
            for (int i = 0; i < _faceInfoList.size(); i++) {
                _facePreviewInfoList.add(new FacePreviewInfo(_faceInfoList.get(i), _currentTrackIdList.get(i)));
            }
            return _facePreviewInfoList;
        } else {
            _facePreviewInfoList.clear();
            return _facePreviewInfoList;
        }
    }

    /**
     * 刷新trackId
     *
     * @param ftFaceList 传入的人脸列表
     */
    private void RefreshTrackId(List<FaceInfo> ftFaceList) {
        _currentTrackIdList.clear();
        for (FaceInfo faceInfo : ftFaceList) {
            _currentTrackIdList.add(faceInfo.getFaceId() + _trackedFaceCount);
        }
        if (ftFaceList.size() > 0) {
            _currentMaxFaceId = ftFaceList.get(ftFaceList.size() - 1).getFaceId();
        }
        //刷新nameMap
        ClearLeftName(_currentTrackIdList);
    }

    /**
     * 获取当前的最大trackID,可用于退出时保存
     *
     * @return 当前trackId
     */
    public int GetTrackedFaceCount() {
        // 引擎的人脸下标从0开始，因此需要+1
        return _trackedFaceCount + _currentMaxFaceId + 1;
    }

    /**
     * 新增搜索成功的人脸
     *
     * @param trackId 指定的trackId
     * @param name    trackId对应的人脸
     */
    public void SetName(int trackId, String name) {
        if (_nameMap != null) {
            _nameMap.put(trackId, name);
        }
    }

    public String GetName(int trackId) {
        return _nameMap == null ? null : _nameMap.get(trackId);
    }

    /**
     * 清除map中已经离开的人脸
     *
     * @param trackIdList 最新的trackIdList
     */
    private void ClearLeftName(List<Integer> trackIdList) {
        Enumeration<Integer> keys = _nameMap.keys();
        while (keys.hasMoreElements()) {
            int value = keys.nextElement();
            if (!trackIdList.contains(value)) {
                _nameMap.remove(value);
            }
        }
    }

}
