package com.zistone.factorytest0718.face;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.zistone.factorytest0718.face.widget.MultiFaceInfoAdapter;
import com.zistone.factorytest0718.util.MyImageUtil;
import com.zistone.factorytest0718.util.MyProgressDialogUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FaceIdCompareMenuActivity extends AppCompatActivity {

    private static final String TAG = "FaceIdCompareMenuActivity";
    private static final int ACTION_CHOOSE_MAIN_IMAGE = 0x201;
    //用于进行比对的证件照名称
    private static final String IMAGE_NAME = "FaceIdCompareTest";

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
        if (bitmap == null || _faceEngine == null) {
            return;
        }
        //接口需要的bgr24宽度必须为4的倍数
        bitmap = ArcSoftImageUtil.getAlignedBitmap(bitmap, true);
        if (bitmap == null) {
            return;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        //bitmap转bgr24
        byte[] bgr24 = ArcSoftImageUtil.createImageData(bitmap.getWidth(), bitmap.getHeight(), ArcSoftImageFormat.BGR24);
        int transformCode = ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
        if (transformCode != ArcSoftImageUtilError.CODE_SUCCESS) {
            Toast.makeText(this, "bitmap转image失败，错误代码：" + transformCode, Toast.LENGTH_SHORT).show();
            return;
        }
        if (bgr24 != null) {
            _faceInfoList = new ArrayList<>();
            //人脸检测
            int detectCode = _faceEngine.detectFaces(bgr24, width, height, FaceEngine.CP_PAF_BGR24, _faceInfoList);
            if (detectCode != 0 || _faceInfoList.size() == 0) {
                Toast.makeText(this, "该图片未检测到人脸", Toast.LENGTH_SHORT).show();
                _txt.setText("该图片未检测到人脸");
                return;
            }
            //绘制bitmap
            Bitmap compyBitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
            Canvas canvas = new Canvas(compyBitmap);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(10);
            paint.setColor(Color.YELLOW);
            if (_faceInfoList.size() > 0) {
                for (int i = 0; i < _faceInfoList.size(); i++) {
                    //绘制人脸框
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawRect(_faceInfoList.get(i).getRect(), paint);
                    //绘制人脸序号
                    paint.setStyle(Paint.Style.FILL_AND_STROKE);
                    paint.setTextSize(_faceInfoList.get(i).getRect().width() / 2);
                    canvas.drawText("" + i, _faceInfoList.get(i).getRect().left, _faceInfoList.get(i).getRect().top, paint);
                }
            }
            int faceProcessCode = _faceEngine.process(bgr24, width, height, FaceEngine.CP_PAF_BGR24, _faceInfoList, FaceEngine.ASF_AGE | FaceEngine.ASF_GENDER | FaceEngine.ASF_FACE3DANGLE);
            if (faceProcessCode != ErrorInfo.MOK) {
                _txt.setText("人脸特征提取失败，错误代码：" + faceProcessCode);
                Toast.makeText(this, "人脸特征提取失败，错误代码：" + faceProcessCode, Toast.LENGTH_SHORT).show();
                return;
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
                _txt.setText("年龄、性别、人脸三维角度至少有一个检测失败，对应代码：" + ageCode + "，" + genderCode + "，" + face3DAngleCode);
                return;
            }
            //人脸比对数据显示
            if (_faceInfoList.size() > 0) {
                _faceFeature = new FaceFeature();
                //提取人脸特征
                int res = _faceEngine.extractFaceFeature(bgr24, width, height, FaceEngine.CP_PAF_BGR24, _faceInfoList.get(0), _faceFeature);
                if (res != ErrorInfo.MOK) {
                    _faceFeature = null;
                }
                runOnUiThread(() -> {
                    Glide.with(_image.getContext()).load(compyBitmap).into(_image);
                    StringBuilder stringBuilder = new StringBuilder();
                    if (_faceInfoList.size() > 0) {
                        stringBuilder.append("人脸信息：\n");
                    }
                    for (int i = 0; i < _faceInfoList.size(); i++) {
                        int age = ageInfoList.get(i).getAge();
                        String gender = genderInfoList.get(i).getGender() == GenderInfo.MALE ? "男" : (genderInfoList.get(i).getGender() == GenderInfo.FEMALE ? "女" : "未知");
                        stringBuilder.append("人脸[").append(i).append("]:\n").append(_faceInfoList.get(i)).append("年龄：").append(age).append("，性别：").append(gender).append("\n人脸三维角度：").append(face3DAngleList.get(i)).append("\n");
                    }
                    _txt.setText(stringBuilder);
                });
                //将照片注册进人脸库，且始终保持人脸库只有一张证件照片，比对时通过人脸库
                FaceServer.getInstance().ClearAllFaces(this);
                _registerSuccess = FaceServer.getInstance().RegisterBgr24(this, bgr24, compyBitmap.getWidth(), compyBitmap.getHeight(), IMAGE_NAME);
            } else {
                _bitmap = null;
            }
        } else {
            _txt.setText("不能从位图得到BGR24！");
        }
        runOnUiThread(() -> MyProgressDialogUtil.DismissProgressDialog());
    }

    /**
     * 从读卡器读取照片
     *
     * @param view
     */
    public void ReaderImage(View view) {
        Toast.makeText(this, "该设备不支持读卡器读取", Toast.LENGTH_SHORT).show();
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null || data.getData() == null) {
            Toast.makeText(this, "获取图片失败", Toast.LENGTH_SHORT).show();
            _txt.setText("获取图片失败");
            return;
        }
        if (requestCode == ACTION_CHOOSE_MAIN_IMAGE) {
            try {
                _imageUri = MyImageUtil.geturi(data, this);
                _bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "获取图片失败：" + e.toString(), Toast.LENGTH_SHORT).show();
                _txt.setText("获取图片失败：" + e.toString());
                return;
            }
            if (_bitmap == null) {
                Toast.makeText(this, "获取图片失败", Toast.LENGTH_SHORT).show();
                _txt.setText("获取图片失败");
                return;
            }
            MyProgressDialogUtil.ShowProgressDialog(this, false, null, "正在注册...");
            new Thread(() -> {
                ProcessImage(_bitmap);
            }).start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        UnInitEngine();
        FaceServer.getInstance().UnInit();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //保持亮屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_face_id_compare_menu);
        _image = findViewById(R.id.img_faceid_compare_menu);
        _txt = findViewById(R.id.txt_faceid_compare_menu);
        InitEngine();
        FaceServer.getInstance().Init(this);
    }

}

