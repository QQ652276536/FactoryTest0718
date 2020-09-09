package com.zistone.factorytest0718.face;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.Face3DAngle;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.bumptech.glide.Glide;
import com.zistone.factorytest0718.R;
import com.zistone.factorytest0718.face.util.FaceServer;
import com.zistone.factorytest0718.util.MyImageUtil;
import com.zistone.factorytest0718.util.MyProgressDialogUtil;
import com.zistone.factorytest0718.util.MySoundPlayUtil;
import com.zistone.gpio.Gpio;
import com.zz.impl.idcard.IDCardDeviceImpl;
import com.zz.impl.idcard.IDCardInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FaceIdCompareChooseActivity extends AppCompatActivity {

    private static final String TAG = "FaceIdCompareChooseActivity";
    private static final int ACTION_CHOOSE_MAIN_IMAGE = 0x201;
    //用于进行比对的证件照名称
    private static final String IMAGE_NAME = "FaceIdCompareTest";
    private static final String PORT_NAME = "/dev/ttyHSL2";
    private static final int BAUDRATE = 115200;
    //读卡成功
    private static final int READ_CARD_SUCCESS = 100;
    //读卡失败
    private static final int READ_CARD_FAIL = 101;

    private ReadIDCardThread _readIDCardThread;
    private Handler _handler;
    private byte[] _message = new byte[100];
    private IDCardInterface _idCardInterface;
    //线程开关、是否正在读取
    private static volatile boolean _readThreadFlag = true, _radingFlag = false;
    private Button _btnReader;
    private ImageView _image;
    private TextView _txt;
    private FaceFeature _faceFeature;
    private FaceEngine _faceEngine;
    private int _faceEngineCode = -1;
    private Bitmap _bitmap;
    private List<FaceInfo> _faceInfoList;
    private boolean _registerSuccess = false;
    private Uri _imageUri;

    /**
     * 读取身份证的线程
     */
    class ReadIDCardThread extends Thread {
        @Override
        public void run() {
            synchronized (this) {
                while (!_readThreadFlag) {
                    if (_radingFlag) {
                        continue;
                    }
                    Message message = new Message();
                    _radingFlag = true;
                    int result = 0;
                    try {
                        //超时10秒
                        int timeOut = 10;
                        //不读取指纹
                        result = _idCardInterface.readIDCard(PORT_NAME, BAUDRATE, false, timeOut, _message);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    _radingFlag = false;
                    if (result != 0) {
                        try {
                            String str = new String(_message, "GBK");
                            message.what = READ_CARD_FAIL;
                            message.obj = String.format("错误信息:%s", str);
                            _handler.sendMessage(message);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        message.what = READ_CARD_SUCCESS;
                        _handler.sendMessage(message);
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private void InitHandlerMessage() {
        //初始化handle，绑定在主线程中的队列消息中
        _handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //读卡成功
                if (msg.what == READ_CARD_SUCCESS) {
                    //读取成功后停止循环读取，并发出提示音
                    _readThreadFlag = true;
                    //从模块获取读到的身份证图像
                    Bitmap bitmap = _idCardInterface.getPhotoBmp();
                    MyProgressDialogUtil.ShowProgressDialog(FaceIdCompareChooseActivity.this, false, null, "正在注册...");
                    //图像处理
                    ProcessImage(bitmap);
                    //                    //显示选择的本地图片
                    //                    runOnUiThread(() -> Glide.with(_image.getContext()).load(bitmap).into(_image));
                    MySoundPlayUtil.SystemSoundPlay(FaceIdCompareChooseActivity.this);
                    _btnReader.setText("模块读取");
                }
                //读卡失败
                else if (msg.what == READ_CARD_FAIL) {
                    _txt.setText(msg.obj.toString());
                    Log.e(TAG, msg.obj.toString());
                }
            }
        };
    }

    /**
     * 初始化引擎
     */
    private void InitEngine() {
        _faceEngine = new FaceEngine();
        _faceEngineCode = _faceEngine.init(this, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY, 16, 6, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_AGE | FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_GENDER | FaceEngine.ASF_FACE3DANGLE);
        Log.i(TAG, "初始化引擎：" + _faceEngineCode);
        if (_faceEngineCode != ErrorInfo.MOK) {
            _txt.setText("初始化引擎失败，错误代码：" + _faceEngineCode);
            Log.i(TAG, _txt.getText().toString());
        }
    }

    /**
     * 销毁引擎
     */
    private void UnInitEngine() {
        if (_faceEngine != null) {
            _faceEngineCode = _faceEngine.unInit();
            Log.i(TAG, "UnInitEngine: " + _faceEngineCode);
        }
    }

    /**
     * 图片处理的主要逻辑部分
     *
     * @param bitmap
     */
    public void ProcessImage(Bitmap bitmap) {
        //接口需要的bgr24宽度必须为4的倍数
        bitmap = ArcSoftImageUtil.getAlignedBitmap(bitmap, true);
        if (null != bitmap) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            //bitmap转bgr24
            byte[] bgr24 = ArcSoftImageUtil.createImageData(bitmap.getWidth(), bitmap.getHeight(), ArcSoftImageFormat.BGR24);
            int transformCode = ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
            if (transformCode == ArcSoftImageUtilError.CODE_SUCCESS) {
                _faceInfoList = new ArrayList<>();
                //检测图片中的人脸
                int detectCode = _faceEngine.detectFaces(bgr24, width, height, FaceEngine.CP_PAF_BGR24, _faceInfoList);
                if (detectCode != 0 || _faceInfoList.size() == 0) {
                    runOnUiThread(() -> {
                        _txt.setTextColor(Color.RED);
                        _txt.setText("该图片未检测到人脸");
                    });
                }
                //绘制bitmap
                Bitmap compyBitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
                //                Canvas canvas = new Canvas(compyBitmap);
                //                Paint paint = new Paint();
                //                paint.setAntiAlias(true);
                //                paint.setStrokeWidth(10);
                //                paint.setColor(Color.YELLOW);
                //                if (_faceInfoList.size() > 0) {
                //                    for (int i = 0; i < _faceInfoList.size(); i++) {
                //                        //绘制人脸框
                //                        paint.setStyle(Paint.Style.STROKE);
                //                        canvas.drawRect(_faceInfoList.get(i).getRect(), paint);
                //                        //绘制人脸序号
                //                        paint.setStyle(Paint.Style.FILL_AND_STROKE);
                //                        paint.setTextSize(_faceInfoList.get(i).getRect().width() / 2);
                //                        canvas.drawText("" + i, _faceInfoList.get(i).getRect().left, _faceInfoList.get(i).getRect().top, paint);
                //                    }
                //                }
                //显示经过重绘的本地图片
                runOnUiThread(() -> Glide.with(_image.getContext()).load(compyBitmap).into(_image));
                int faceProcessCode = _faceEngine.process(bgr24, width, height, FaceEngine.CP_PAF_BGR24, _faceInfoList, FaceEngine.ASF_AGE | FaceEngine.ASF_GENDER | FaceEngine.ASF_FACE3DANGLE);
                if (faceProcessCode != ErrorInfo.MOK) {
                    runOnUiThread(() -> {
                        _txt.setTextColor(Color.RED);
                        _txt.setText("人脸特征提取失败，错误代码：" + faceProcessCode + "\n");
                    });
                }
                //年龄信息结果
                List<AgeInfo> ageInfoList = new ArrayList<>();
                //性别信息结果
                List<GenderInfo> genderInfoList = new ArrayList<>();
                //三维角度结果
                List<Face3DAngle> face3DAngleList = new ArrayList<>();
                //获取年龄、性别、三维角度
                int ageCode = _faceEngine.getAge(ageInfoList);
                int genderCode = _faceEngine.getGender(genderInfoList);
                int face3DAngleCode = _faceEngine.getFace3DAngle(face3DAngleList);
                if ((ageCode | genderCode | face3DAngleCode) != ErrorInfo.MOK) {
                    runOnUiThread(() -> {
                        _txt.setTextColor(Color.RED);
                        _txt.setText("年龄、性别、人脸三维角度至少有一个检测失败，对应失败代码：" + ageCode + "，" + genderCode + "，" + face3DAngleCode + "\n");
                    });
                }
                //人脸比对数据显示
                if (_faceInfoList.size() == 0) {
                    _bitmap = null;
                } else {
                    _faceFeature = new FaceFeature();
                    //提取图片中的人脸特征
                    int res = _faceEngine.extractFaceFeature(bgr24, width, height, FaceEngine.CP_PAF_BGR24, _faceInfoList.get(0), _faceFeature);
                    if (res != ErrorInfo.MOK) {
                        //提取完毕后释放特征引擎
                        _faceFeature = null;
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    if (_faceInfoList.size() > 0) {
                        stringBuilder.append("人脸信息：\n");
                    }
                    for (int i = 0; i < _faceInfoList.size(); i++) {
                        int age = ageInfoList.get(i).getAge();
                        String gender = genderInfoList.get(i).getGender() == GenderInfo.MALE ? "男" : (genderInfoList.get(i).getGender() == GenderInfo.FEMALE ? "女" : "未知");
                        stringBuilder.append("人脸[").append(i).append("]:\n").append(_faceInfoList.get(i)).append("年龄：").append(age).append("，性别：").append(gender).append("\n人脸三维角度：").append(face3DAngleList.get(i)).append("\n");
                    }
                    runOnUiThread(() -> {
                        _txt.setTextColor(Color.DKGRAY);
                        _txt.setText(stringBuilder);
                    });
                    //将照片注册进人脸库，且始终保持人脸库只有一张证件照片，比对时通过人脸库
                    FaceServer.getInstance().ClearAllFaces(this);
                    _registerSuccess = FaceServer.getInstance().RegisterBgr24(this, bgr24, compyBitmap.getWidth(), compyBitmap.getHeight(), IMAGE_NAME);
                }
            } else {
                Log.e(TAG, "bitmap转image失败，错误代码：" + transformCode);
            }
        }
        runOnUiThread(() -> MyProgressDialogUtil.DismissAlertDialog());
    }

    /**
     * 从模块读取照片
     *
     * @param view
     */
    public void ReaderImage(View view) {
        _txt.setText("");
        //通过身份证模块读取照片
        if (_btnReader.getText().equals("模块读取")) {
            _readThreadFlag = false;
            _btnReader.setText("停   止");
            _readIDCardThread = new ReadIDCardThread();
            _readIDCardThread.start();
        } else {
            _readThreadFlag = true;
            _btnReader.setText("模块读取");
        }
    }

    /**
     * 从本地选择文件
     *
     * @param view
     */
    public void ChooseLocalImage(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, ACTION_CHOOSE_MAIN_IMAGE);
    }

    public void RealTimeCollection(View view) {
        if (null == _faceInfoList || _faceInfoList.size() == 0) {
            Toast.makeText(this, "请选择要进行比对的证件照片", Toast.LENGTH_SHORT).show();
            return;
        }
        if (_faceInfoList.size() > 1) {
            Toast.makeText(this, "请选择证件照进行对比", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!_registerSuccess) {
            Toast.makeText(this, "证件照未注册成功", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(new Intent(this, FaceIdCompareVerifyActivity.class));
    }

    /**
     * 选择本地图片后的回调
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null || data.getData() == null) {
            return;
        }
        if (requestCode == ACTION_CHOOSE_MAIN_IMAGE) {
            try {
                _imageUri = MyImageUtil.GetUri(data, this);
                _bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "获取图片失败：" + e.toString(), Toast.LENGTH_SHORT).show();
                _txt.setText("获取图片失败：" + e.toString());
                return;
            }
            if (null == _bitmap) {
                Toast.makeText(this, "获取图片失败", Toast.LENGTH_SHORT).show();
                _txt.setText("获取图片失败");
                return;
            }
            runOnUiThread(() -> MyProgressDialogUtil.ShowProgressDialog(this, false, null, "正在注册..."));
            new Thread(() -> {
                ProcessImage(_bitmap);
            }).start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Gpio.getInstance().set_gpio(0, 66);
        UnInitEngine();
        FaceServer.getInstance().UnInit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Gpio.getInstance().set_gpio(1, 66);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Gpio.getInstance().set_gpio(0, 66);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //保持亮屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_face_id_compare_choose);
        _btnReader = findViewById(R.id.btn_reader_faceid_compare_choose);
        _image = findViewById(R.id.img_faceid_compare_choose);
        _txt = findViewById(R.id.txt_faceid_compare_choose);
        InitEngine();
        FaceServer.getInstance().Init(this);
        _idCardInterface = new IDCardDeviceImpl();
        InitHandlerMessage();
    }

}

