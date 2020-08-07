package com.zistone.factorytest0718;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.zistone.factorytest0718.view.MyDrawView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WatermarkCameraActivity extends BaseActivity implements SurfaceHolder.Callback, SensorEventListener,
        OnGetGeoCoderResultListener, View.OnClickListener {

    private static final String TAG = "WatermarkCameraActivity";
    private static final int STATUS_NONE = 0;
    private static final int STATUS_STATIC = 1;
    private static final int STATUS_MOVE = 2;
    private static final int DELAY_DURATION = 500;
    private static final double MOVE_IS = 1.4;
    private static final int FOCUS_RECT_SIZE = 200;

    private Camera _camera;
    private SurfaceView _surfaceView;
    private MyDrawView _myDrawView;
    private SurfaceHolder _surfaceHolder;
    private ImageButton _btnCamera, _btnOk, _btnBeforeAfter, _btnReset;
    private Bitmap _bitmap = null;
    private String _picUrl;
    private TextView _txtLat, _txtAddress;
    private TextClock _txtClock1, _txtClock2;
    //0代表前置摄像头，1代表后置摄像头
    private int _cameraDir = 1;
    private boolean _isFirstLocation = true;
    private double _lastX = 0.0, _currentLat = 0.0, _currentLot = 0.0;
    private int _currentDirection = 0;
    //位置信息，用于在地图上显示，这里只需要获取经纬度信息用于显示在相机上所以没用到
    private MyLocationData _myLocationData;
    private float _currentAccracy;
    private MyLocationListener _myLocationListener = new MyLocationListener();
    //定位相关
    private LocationClient _locationClient;
    //地理编码模块，可去掉地图单独使用
    private GeoCoder _geoCoder;
    //传感器
    private SensorManager _sensorManager;
    private Sensor _sensor;
    private boolean _canFocus = false, _canFocusIn = false, _isFocusing = false;
    private int _state = STATUS_NONE;
    private float _x, _y, _z;
    private Calendar _calendar;
    //对焦的间隔时间
    private long _lastStaticStamp = 0;
    private CameraFocusListener _cameraFocusListener;
    //自动对焦的回调
    private Camera.AutoFocusCallback _autoFocusCallback;
    private Paint _paint;
    //回调拍照声音的
    private Camera.ShutterCallback _shutterCallback = () -> {
    };
    //相机拍照后对照片的回调
    private Camera.PictureCallback _pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            _bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Matrix matrix = new Matrix();
            //前后置摄像头拍的照片不一定都是正的，这里不同的机型情况不一样
            if (_cameraDir == 1) {
                matrix.postRotate(270);
            } else {
                matrix.postRotate(90);
            }
            Bitmap _tempBitmap = Bitmap.createBitmap(_bitmap, 0, 0, _bitmap.getWidth(), _bitmap.getHeight(), matrix, true);
            String time = _txtClock2.getText().toString() + "   " + _txtClock1.getText().toString();
            String lat = _txtLat.getText().toString();
            String address = _txtAddress.getText().toString();
            //添加水印
            _bitmap = AddWatermark(_tempBitmap, time, lat, address);
        }
    };

    public interface CameraFocusListener {
        void onFocus();
    }

    /**
     * 定位SDK
     */
    private class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            //销毁后不再处理新接收的位置
            if (bdLocation == null) {
                return;
            }
            _currentLat = bdLocation.getLatitude();
            _currentLot = bdLocation.getLongitude();
            _currentAccracy = bdLocation.getRadius();
            //此处设置开发者获取到的方向信息，顺时针0-360
            _myLocationData = new MyLocationData.Builder().accuracy(bdLocation.getRadius()).direction(_currentDirection).latitude(bdLocation.getLatitude()).longitude(bdLocation.getLongitude()).build();
            if (_isFirstLocation) {
                _isFirstLocation = false;
                LatLng latLng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(latLng).zoom(18.0f);
            }
            LatLng latLng = new LatLng(_currentLat, _currentLot);
            _txtLat.setText("北纬" + _currentLat + "  东经" + _currentLot);
            //坐标反查
            _geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng).newVersion(1).radius(500));
        }
    }

    /**
     * 手动对焦
     *
     * @param event
     */
    private void FocusOnTouch(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        int surfaceWidth = _surfaceView.getWidth();
        int surfaceHeight = _surfaceView.getHeight();
        Log.i(TAG, "x的值：" + x + "，y的值：" + y + "，surfaceWidth的值：" + surfaceWidth + "，surfaceHeight的值：" + surfaceHeight);
        int left = x - 100, top = y - 100, right = x + 100, bottom = y + 100;
        //绘制矩形框，不能绘到SurfaceView的外面
        if (left < 0) {
            left = 0;
            right = FOCUS_RECT_SIZE;
        }
        if (right >= surfaceWidth) {
            left = surfaceWidth - FOCUS_RECT_SIZE;
            right = surfaceWidth;
        }
        if (top < 0) {
            top = 0;
            bottom = FOCUS_RECT_SIZE;
        }
        if (bottom >= surfaceHeight) {
            top = surfaceHeight - FOCUS_RECT_SIZE;
            bottom = surfaceHeight;
        }
        _myDrawView.ClearDraw();
        Rect focusRect = new Rect(left, top, right, bottom);
        Log.i(TAG, "focusRect=" + focusRect.left + "，" + focusRect.top + "，" + focusRect.right + "，" + focusRect.bottom);
        _myDrawView.DrawTouchFocusRect(focusRect, _paint);
        if (_camera != null) {
            //如果超出了(-1000,1000)到(1000, 1000)的范围，则会导致相机崩溃
            Rect meterRect = new Rect(-1000, -1000, 1000, 1000);
            Camera.Area area = new Camera.Area(meterRect, 1000);
            //获取当前相机的参数配置对象
            Camera.Parameters parameters = _camera.getParameters();
            List<Camera.Area> focusAreaList = new ArrayList<>();
            List<Camera.Area> meteringAreaList = new ArrayList<>();
            //获取支持对焦、测光区域的个数
            int focusArea = parameters.getMaxNumFocusAreas();
            int meterArea = parameters.getMaxNumMeteringAreas();
            Log.i(TAG, "支持对焦区域的个数：" + parameters.getMaxNumFocusAreas() + "，支持测光区域的个数：" + meterArea);
            if (meterArea > 0) {
                focusAreaList.add(area);
                meteringAreaList.add(area);
            }
            //设置对焦模式、对焦区域、测光区域
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            parameters.setFocusAreas(focusAreaList);
            parameters.setMeteringAreas(meteringAreaList);
            try {
                //对焦前先取消上一次的对焦，不管上一次对焦有没有完成
                _camera.cancelAutoFocus();
                //一定要记得把相应参数设置给相机
                _camera.setParameters(parameters);
                //开启对焦
                _camera.autoFocus(_autoFocusCallback);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.toString());
            }
        }
    }

    /**
     * 照片上添加水印
     *
     * @param bitmap
     * @param time
     * @param lat
     * @param address
     * @return
     */
    private Bitmap AddWatermark(Bitmap bitmap, String time, String lat, String address) {
        Bitmap.Config bitmapConfig = bitmap.getConfig();
        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        //获取原始图片的宽高
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        //获取屏幕的宽高
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float multiplied = bitmapWidth / displayMetrics.widthPixels;
        //新的图片，也就是加了水印后的图片
        Bitmap newBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, bitmapConfig);
        Canvas canvas = new Canvas(newBitmap);
        //向位图中开始画入MBitmap原始图片
        canvas.drawBitmap(bitmap, 0, 0, null);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTextSize(SpToPx(this, 25) * multiplied);
        Rect bounds = new Rect();
        //绘制时间
        paint.getTextBounds(time, 0, time.length(), bounds);
        float textW = paint.measureText(time);
        float x = (bitmapWidth / 2) - (textW / 2);
        x = 5;
        float textH = -paint.ascent() + paint.descent();
        canvas.drawText(time, x, 5 + textH, paint);
        //绘制坐标
        paint.setTextSize(SpToPx(this, 20) * multiplied);
        paint.getTextBounds(lat, 0, lat.length(), bounds);
        textW = paint.measureText(lat);
        x = (bitmapWidth / 2) - (textW / 2);
        x = 5;
        canvas.drawText(lat, x, 5 + textH * 2, paint);
        //绘制地理位置
        paint.getTextBounds(address, 0, address.length(), bounds);
        textW = paint.measureText(address);
        x = (bitmapWidth / 2) - (textW / 2);
        x = 5;
        canvas.drawText(address, x, 5 + textH * 3, paint);
        canvas.save();
        return newBitmap;
    }

    /**
     * 检测手机是否存在内置SD卡
     */
    private void CheckSoftStage() {
        //判断是否存在SD卡
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //获取SD卡的根目录
            String rootPath = Environment.getExternalStorageDirectory().getPath();
            File file = new File(rootPath);
            if (!file.exists()) {
                file.mkdir();
            }
        } else {
            new AlertDialog.Builder(this).setMessage("检测到手机没有存储卡！请插入手机存储卡再开启本应用。").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).show();
        }
    }

    /**
     * 打开摄像头
     *
     * @return
     */
    public Camera GetCamera() {
        Camera camera = null;
        try {
            //默认打开的是后置摄像头，如果机器没有后置摄像头则打开前置摄像头
            camera = Camera.open();
            if (camera == null)
                camera = Camera.open(0);
        } catch (Exception e) {
            try {
                camera = Camera.open(0);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return camera;
    }

    /**
     * 开始预览相机内容
     *
     * @param camera
     * @param holder
     */
    private void SetStartPreview(Camera camera, SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            //系统预览角度的调整
            camera.setDisplayOrientation(270);
            //打开摄像头
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放相机资源
     */
    private void ReleaseCamera() {
        if (_camera != null) {
            _camera.setPreviewCallback(null);
            _camera.stopPreview();
            _camera.release();
            _camera = null;
        }
    }

    /**
     * 将SP转为PX，保证文字大小不变
     *
     * @param context
     * @param spValue
     * @return
     */
    public int SpToPx(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //拍照
            case R.id.btnCamera_camera: {
                _btnOk.setVisibility(View.VISIBLE);
                _btnReset.setVisibility(View.VISIBLE);
                _btnBeforeAfter.setVisibility(View.GONE);
                _btnCamera.setVisibility(View.GONE);
                Camera.Parameters parameters = _camera.getParameters();
                parameters.setPictureFormat(ImageFormat.JPEG);
                parameters.setPictureSize(800, 400);
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                _camera.autoFocus((success, camera) -> {
                    if (success) {
                        _camera.takePicture(_shutterCallback, null, _pictureCallback);
                    }
                });
            }
            break;
            //切换前后摄像头
            case R.id.btnBeforeAfter_camera: {
                int cameraCount = 0;
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数
                if (cameraCount == 1) {
                    Toast.makeText(WatermarkCameraActivity.this, "该设备只有一个摄像头", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (int i = 0; i < cameraCount; i++) {
                    //得到每个摄像头的信息
                    Camera.getCameraInfo(i, cameraInfo);
                    //后置摄像头变为前置摄像头
                    if (_cameraDir == 1) {
                        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            try {
                                //停掉原来摄像头的预览
                                _camera.stopPreview();
                                _camera.release();
                                _camera = null;
                                _camera = Camera.open(i);
                                //通过surfaceview显示取景画面
                                _camera.setPreviewDisplay(_surfaceHolder);
                                SetStartPreview(_camera, _surfaceHolder);
                                _cameraDir = 0;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                    //前置摄像头变为后置摄像头
                    else {
                        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                            try {
                                //停掉原来摄像头的预览
                                _camera.stopPreview();
                                _camera.release();
                                _camera = null;
                                _camera = Camera.open(i);
                                //通过surfaceview显示取景画面
                                _camera.setPreviewDisplay(_surfaceHolder);
                                SetStartPreview(_camera, _surfaceHolder);
                                _cameraDir = 1;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            }
            break;
            //保存
            case R.id.btnOk_camera: {
                FileOutputStream outStream;
                String filePath = Environment.getExternalStorageDirectory().getPath() + File.separator;
                String fileName = filePath + File.separator + System.currentTimeMillis() + ".jpg";
                try {
                    File file = new File(fileName);
                    if (!file.exists())
                        file.getParentFile().mkdirs();
                    outStream = new FileOutputStream(fileName);
                    _bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                    if (outStream != null) {
                        try {
                            outStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    _picUrl = file.getAbsolutePath();
                    //通知图库更新
                    WatermarkCameraActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + fileName)));
                    Toast.makeText(WatermarkCameraActivity.this, "文件已保存至:" + fileName, Toast.LENGTH_LONG).show();
                    Log.i(TAG, "_picUrl：" + _picUrl);
                    //保存完以后回到拍照界面
                    _camera.startPreview();
                    if (_bitmap != null)
                        _bitmap.recycle();
                    _btnBeforeAfter.setVisibility(View.VISIBLE);
                    _btnCamera.setVisibility(View.VISIBLE);
                    _btnReset.setVisibility(View.GONE);
                    _btnOk.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
            //重新拍照
            case R.id.btnRest_camera: {
                _btnOk.setVisibility(View.GONE);
                _btnReset.setVisibility(View.GONE);
                _btnBeforeAfter.setVisibility(View.VISIBLE);
                _btnCamera.setVisibility(View.VISIBLE);
                _camera.startPreview();
                if (_bitmap != null)
                    _bitmap.recycle();
            }
            break;
        }
    }

    /**
     * 画面显示时
     *
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (null != _camera)
            SetStartPreview(_camera, _surfaceHolder);
    }

    /**
     * 屏幕发生变化时
     *
     * @param holder
     * @param format
     * @param width
     * @param height
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (null != _camera) {
            _camera.stopPreview();
            SetStartPreview(_camera, _surfaceHolder);
        }
    }

    /**
     * 画面销毁时
     *
     * @param holder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        ReleaseCamera();
    }

    /**
     * 传感数据
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == null)
            return;
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        //定位的时候的方向获取
        if (Math.abs(x - _lastX) > 1.0) {
            _currentDirection = (int) x;
            //此处设置开发者获取到的方向信息，顺时针0-360
            _myLocationData = new MyLocationData.Builder().accuracy(_currentAccracy).direction(_currentDirection).latitude(_currentLat).longitude(_currentLot).build();
        }
        _lastX = x;
        //聚集实现
        if (_isFocusing) {
            _state = STATUS_NONE;
            _canFocusIn = false;
            _x = 0;
            _y = 0;
            _z = 0;
            return;
        }
        //加速度传感器
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            _calendar = Calendar.getInstance();
            long stamp = _calendar.getTimeInMillis();
            int second = _calendar.get(Calendar.SECOND);
            if (_state != STATUS_NONE) {
                int px = (int) Math.abs(_x - x);
                int py = (int) Math.abs(_y - y);
                int pz = (int) Math.abs(_z - z);
                double value = Math.sqrt(px * px + py * py + pz * pz);
                if (value > MOVE_IS) {
                    _state = STATUS_MOVE;
                } else {
                    if (_state == STATUS_MOVE) {
                        _lastStaticStamp = stamp;
                        _canFocusIn = true;
                    }
                    if (_canFocusIn) {
                        if (stamp - _lastStaticStamp > DELAY_DURATION) {
                            //移动后静止一段时间，可以发生对焦行为
                            if (!_isFocusing) {
                                _canFocusIn = false;
                                if (_cameraFocusListener != null) {
                                    _cameraFocusListener.onFocus();
                                }
                            }
                        }
                    }
                    _state = STATUS_STATIC;
                }
            } else {
                _lastStaticStamp = stamp;
                _state = STATUS_STATIC;
            }
            _x = x;
            _y = y;
            _z = z;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * 坐标查询结果
     *
     * @param geoCodeResult
     */
    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
        if (geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Log.e(TAG, "坐标查询未能找到结果：" + geoCodeResult + "，" + geoCodeResult.error);
            Toast.makeText(this, "坐标查询未能找到结果", Toast.LENGTH_LONG).show();
            return;
        }
    }

    /**
     * 坐标反查结果
     *
     * @param reverseGeoCodeResult
     */
    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Log.e(TAG, "坐标反查未能找到结果：" + reverseGeoCodeResult + "，" + reverseGeoCodeResult.error);
            Toast.makeText(this, "坐标反查未能找到结果", Toast.LENGTH_LONG).show();
            return;
        }
        _txtAddress.setText(reverseGeoCodeResult.getAddress());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        _canFocus = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _sensorManager.unregisterListener(this, _sensor);
        //销毁定位
        _locationClient.stop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            Intent intent = new Intent();
            intent.putExtra(ARG_PARAM1, FAIL);
            setResult(RESULT_OK, intent);
            finish();
        }
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watermark_camera);
        _surfaceView = findViewById(R.id.sfv_camera);
        _surfaceView.setOnTouchListener((v, event) -> {
            FocusOnTouch(event);
            return false;
        });
        _myDrawView = findViewById(R.id.myDrawView);
        _btnCamera = findViewById(R.id.btnCamera_camera);
        _btnOk = findViewById(R.id.btnOk_camera);
        _btnBeforeAfter = findViewById(R.id.btnBeforeAfter_camera);
        _btnReset = findViewById(R.id.btnRest_camera);
        _surfaceHolder = _surfaceView.getHolder();
        _surfaceHolder.addCallback(this);
        //判断内存卡是否存在的
        CheckSoftStage();
        //切换摄像头
        _btnBeforeAfter.setOnClickListener(this);
        _btnCamera.setOnClickListener(this);
        _btnReset.setOnClickListener(this);
        //保存
        _btnOk.setOnClickListener(this);
        _txtLat = findViewById(R.id.txt_lat);
        _txtAddress = findViewById(R.id.txt_address);
        _txtClock1 = findViewById(R.id.txtClock1_camera);
        _txtClock2 = findViewById(R.id.txtClock2_camera);
        //初始化地理编码模块
        _geoCoder = GeoCoder.newInstance();
        _geoCoder.setOnGetGeoCodeResultListener(this);
        //获取传感器服务
        _sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        _sensor = _sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        _sensorManager.registerListener(this, _sensor, _sensorManager.SENSOR_DELAY_NORMAL);
        //定位初始化
        _locationClient = new LocationClient(this);
        _locationClient.registerLocationListener(_myLocationListener);
        LocationClientOption option = new LocationClientOption();
        //设置定位模式为高精度定位模式，这种模式下会同时使用GPS和网络
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //打开gps
        option.setOpenGps(true);
        //设置坐标类型
        option.setCoorType("bd09ll");
        //发起定位请求的间隔时间
        option.setScanSpan(10 * 1000);
        _locationClient.setLocOption(option);
        _locationClient.start();
        //预览摄像头
        _camera = GetCamera();
        if (_surfaceHolder != null && _camera != null) {
            SetStartPreview(_camera, _surfaceHolder);
        } else {
            new AlertDialog.Builder(this).setMessage("没有到检测摄像头！").setPositiveButton("确定", (dialog, which) -> finish()).show();
        }
        _state = STATUS_NONE;
        _canFocusIn = false;
        _x = 0;
        _y = 0;
        _z = 0;
        _canFocus = true;
        _cameraFocusListener = () -> {
            if (_camera != null) {
                DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
                int screenWidth = _surfaceView.getWidth();
                int screenHeight = _surfaceView.getHeight();
                Log.i(TAG, "screenWidth的值为" + screenWidth + "，screenHeight的值为" + screenHeight);
                if (_canFocus && !_isFocusing) {
                    _isFocusing = true;
                    Rect focusRect = new Rect(screenWidth / 2 - 100, screenHeight / 2 - 100, screenWidth / 2 + 100, screenHeight / 2 + 100);
                    _myDrawView.ClearDraw();
                    _myDrawView.DrawTouchFocusRect(focusRect, _paint);
                    //如果超出了(-1000,1000)到(1000, 1000)的范围，则会导致相机崩溃
                    Rect meterRect = new Rect(-1000, -1000, 1000, 1000);
                    Camera.Area area = new Camera.Area(meterRect, 1000);
                    //获取当前相机的参数配置对象
                    Camera.Parameters parameters = _camera.getParameters();
                    List<Camera.Area> focusAreaList = new ArrayList<>();
                    List<Camera.Area> meteringAreaList = new ArrayList<>();
                    //获取支持对焦、测光区域的个数
                    int meterArea = parameters.getMaxNumMeteringAreas();
                    Log.i(TAG, "支持对焦区域的个数：" + parameters.getMaxNumFocusAreas() + "，支持测光区域的个数：" + meterArea);
                    if (meterArea > 0) {
                        focusAreaList.add(area);
                        meteringAreaList.add(area);
                    }
                    //设置对焦模式、对焦区域、测光区域
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    parameters.setFocusAreas(focusAreaList);
                    parameters.setMeteringAreas(meteringAreaList);
                    try {
                        //对焦前先取消上一次的对焦，不管上一次对焦有没有完成
                        _camera.cancelAutoFocus();
                        //一定要记得把相应参数设置给相机
                        _camera.setParameters(parameters);
                        //开启对焦
                        _camera.autoFocus(_autoFocusCallback);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, e.toString());
                    }
                }
            }
        };
        _autoFocusCallback = (success, camera) -> {
            if (success) {
                Log.i(TAG, "对焦成功");
                //                _myDrawView.ClearDraw();
                //一秒之后才能再次对焦
                new Handler().postDelayed(() -> _isFocusing = false, 1000);
            } else {
                Log.i(TAG, "对焦失败");
            }
        };
        _paint = new Paint();
        _paint.setAntiAlias(true);
        _paint.setColor(Color.GREEN);
        _paint.setStyle(Paint.Style.STROKE);
    }

}