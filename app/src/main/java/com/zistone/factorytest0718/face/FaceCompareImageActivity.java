package com.zistone.factorytest0718.face;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.Face3DAngle;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceSimilar;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.bumptech.glide.Glide;
import com.zistone.factorytest0718.R;
import com.zistone.factorytest0718.face.model.ItemShowInfo;
import com.zistone.factorytest0718.face.widget.MultiFaceInfoAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FaceCompareImageActivity extends AppCompatActivity {

    private static final String TAG = "FaceCompareImageActivity";
    private static final int ACTION_CHOOSE_MAIN_IMAGE = 0x201;
    private static final int ACTION_ADD_RECYCLER_ITEM_IMAGE = 0x202;
    private static final int TYPE_MAIN = 0;
    private static final int TYPE_ITEM = 1;

    private ImageView _mainImage;
    private TextView _txtMainImageInfo;
    private FaceFeature _mainFeature;
    private FaceEngine _faceEngine;
    private MultiFaceInfoAdapter _multiFaceInfoAdapter;
    private List<ItemShowInfo> _showInfoList;
    private int _faceEngineCode = -1;
    private Bitmap _mainBitmap;

    /**
     * 初始化引擎
     */
    private void InitEngine() {
        _faceEngine = new FaceEngine();
        _faceEngineCode = _faceEngine.init(this, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY, 16, 6, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_AGE | FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_GENDER | FaceEngine.ASF_FACE3DANGLE);
        Log.i(TAG, "初始化引擎：" + _faceEngineCode);
        if (_faceEngineCode != ErrorInfo.MOK) {
            _txtMainImageInfo.setText("初始化引擎失败，错误代码：" + _faceEngineCode);
            Log.i(TAG, _txtMainImageInfo.getText().toString());
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
     * @param type
     */
    public void ProcessImage(Bitmap bitmap, int type) {
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
        long start = System.currentTimeMillis();
        byte[] bgr24 = ArcSoftImageUtil.createImageData(bitmap.getWidth(), bitmap.getHeight(), ArcSoftImageFormat.BGR24);
        int transformCode = ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
        if (transformCode != ArcSoftImageUtilError.CODE_SUCCESS) {
            Toast.makeText(this,"bitmap转image失败，错误代码：" + transformCode,Toast.LENGTH_SHORT).show();
            return;
        }
        if (bgr24 != null) {
            List<FaceInfo> faceInfoList = new ArrayList<>();
            //人脸检测
            int detectCode = _faceEngine.detectFaces(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList);
            if (detectCode != 0 || faceInfoList.size() == 0) {
                Toast.makeText(this, "该图片未检测到人脸", Toast.LENGTH_SHORT).show();
                return;
            }
            //绘制bitmap
            bitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStrokeWidth(10);
            paint.setColor(Color.YELLOW);
            if (faceInfoList.size() > 0) {
                for (int i = 0; i < faceInfoList.size(); i++) {
                    //绘制人脸框
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawRect(faceInfoList.get(i).getRect(), paint);
                    //绘制人脸序号
                    paint.setStyle(Paint.Style.FILL_AND_STROKE);
                    paint.setTextSize(faceInfoList.get(i).getRect().width() / 2);
                    canvas.drawText("" + i, faceInfoList.get(i).getRect().left, faceInfoList.get(i).getRect().top, paint);
                }
            }
            int faceProcessCode = _faceEngine.process(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList, FaceEngine.ASF_AGE | FaceEngine.ASF_GENDER | FaceEngine.ASF_FACE3DANGLE);
            if (faceProcessCode != ErrorInfo.MOK) {
                _txtMainImageInfo.setText("人脸特征提取失败，错误代码：" + faceProcessCode);
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
                _txtMainImageInfo.setText("年龄、性别、人脸三维角度至少有一个检测失败，对应代码：" + ageCode + "，" + genderCode + "，" + face3DAngleCode);
                return;
            }
            //人脸比对数据显示
            if (faceInfoList.size() > 0) {
                if (type == TYPE_MAIN) {
                    int size = _showInfoList.size();
                    _showInfoList.clear();
                    _multiFaceInfoAdapter.notifyItemRangeRemoved(0, size);
                    _mainFeature = new FaceFeature();
                    int res = _faceEngine.extractFaceFeature(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList.get(0), _mainFeature);
                    if (res != ErrorInfo.MOK) {
                        _mainFeature = null;
                    }
                    Glide.with(_mainImage.getContext()).load(bitmap).into(_mainImage);
                    StringBuilder stringBuilder = new StringBuilder();
                    if (faceInfoList.size() > 0) {
                        stringBuilder.append("人脸信息：\n\n");
                    }
                    for (int i = 0; i < faceInfoList.size(); i++) {
                        stringBuilder.append("人脸[").append(i).append("]:\n").append(faceInfoList.get(i)).append("\n" + "年龄：").append(ageInfoList.get(i).getAge()).append("\n性别：").append(genderInfoList.get(i).getGender() == GenderInfo.MALE ? "男" : (genderInfoList.get(i).getGender() == GenderInfo.FEMALE ? "女" : "未知")).append("\n人脸三维角度：").append(face3DAngleList.get(i)).append("\n\n");
                    }
                    _txtMainImageInfo.setText(stringBuilder);
                } else if (type == TYPE_ITEM) {
                    FaceFeature faceFeature = new FaceFeature();
                    int res = _faceEngine.extractFaceFeature(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList.get(0), faceFeature);
                    if (res == 0) {
                        FaceSimilar faceSimilar = new FaceSimilar();
                        int compareResult = _faceEngine.compareFaceFeature(_mainFeature, faceFeature, faceSimilar);
                        if (compareResult == ErrorInfo.MOK) {
                            ItemShowInfo showInfo = new ItemShowInfo(bitmap, ageInfoList.get(0).getAge(), genderInfoList.get(0).getGender(), faceSimilar.getScore());
                            _showInfoList.add(showInfo);
                            _multiFaceInfoAdapter.notifyItemInserted(_showInfoList.size() - 1);
                        } else {
                            _txtMainImageInfo.setText("比对失败，错误码为：" + compareResult);
                        }
                    }
                }
            } else {
                if (type == TYPE_MAIN) {
                    _mainBitmap = null;
                }
            }
        } else {
            _txtMainImageInfo.setText("不能从位图得到BGR24！");
        }
    }

    /**
     * 从本地选择文件
     *
     * @param action 可为选择主图{@link #ACTION_CHOOSE_MAIN_IMAGE}或者选择item图{@link #ACTION_ADD_RECYCLER_ITEM_IMAGE}
     */
    public void ChooseLocalImage(int action) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, action);
    }

    public void AddItemFace(View view) {
        if (_mainBitmap == null) {
            Toast.makeText(this,"请先选择主图",Toast.LENGTH_SHORT).show();
            return;
        }
        ChooseLocalImage(ACTION_ADD_RECYCLER_ITEM_IMAGE);
    }

    public void ChooseMainImage(View view) {
        ChooseLocalImage(ACTION_CHOOSE_MAIN_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null || data.getData() == null) {
            Toast.makeText(this,"获取图片失败",Toast.LENGTH_SHORT).show();
            return;
        }
        if (requestCode == ACTION_CHOOSE_MAIN_IMAGE) {
            try {
                _mainBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this,"获取图片失败",Toast.LENGTH_SHORT).show();
                return;
            }
            if (_mainBitmap == null) {
                Toast.makeText(this,"获取图片失败",Toast.LENGTH_SHORT).show();
                return;
            }
            ProcessImage(_mainBitmap, TYPE_MAIN);
        } else if (requestCode == ACTION_ADD_RECYCLER_ITEM_IMAGE) {
            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this,"获取图片失败",Toast.LENGTH_SHORT).show();
                return;
            }
            if (bitmap == null) {
                Toast.makeText(this,"获取图片失败",Toast.LENGTH_SHORT).show();
                return;
            }
            if (_mainFeature == null) {
                return;
            }
            ProcessImage(bitmap, TYPE_ITEM);
        }
    }

    @Override
    protected void onDestroy() {
        UnInitEngine();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //保持亮屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_face_compare_image);
        _mainImage = findViewById(R.id.img_main_face_compare_img);
        _txtMainImageInfo = findViewById(R.id.txt_main_face_compare_img);
        RecyclerView recyclerFaces = findViewById(R.id.recycler_add_face_compare_img);
        _showInfoList = new ArrayList<>();
        _multiFaceInfoAdapter = new MultiFaceInfoAdapter(_showInfoList, this);
        recyclerFaces.setAdapter(_multiFaceInfoAdapter);
        recyclerFaces.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerFaces.setLayoutManager(new LinearLayoutManager(this));
        InitEngine();
    }

}

