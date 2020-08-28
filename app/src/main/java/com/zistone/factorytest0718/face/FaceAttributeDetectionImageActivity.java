package com.zistone.factorytest0718.face;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.ParcelableSpan;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.Face3DAngle;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceSimilar;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.enums.CompareModel;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.face.enums.DetectModel;
import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.bumptech.glide.Glide;
import com.zistone.factorytest0718.R;
import com.zistone.factorytest0718.util.MyProgressDialogUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 人脸属性检测（图片）
 */
public class FaceAttributeDetectionImageActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "FaceAttributeDetectionImageActivity";
    private static final int ACTION_CHOOSE_IMAGE = 0x201;

    private AlertDialog progressDialog;
    private Bitmap mBitmap = null;
    private ImageView _img;
    private Button _btnLocal, _btnStart;
    private TextView _txt;
    private FaceEngine faceEngine;
    private int faceEngineCode = -1;

    /**
     * 初始化引擎
     */
    private void InitEngine() {
        faceEngine = new FaceEngine();
        faceEngineCode = faceEngine.init(this, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_ALL_OUT, 16, 10, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_AGE | FaceEngine.ASF_GENDER | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_LIVENESS);
        Log.i(TAG, "初始化引擎：" + faceEngineCode);
        if (faceEngineCode != ErrorInfo.MOK) {
            _txt.setText("初始化引擎失败：" + faceEngineCode);
        }
    }

    /**
     * 销毁引擎
     */
    private void UnInitEngine() {
        if (faceEngine != null) {
            faceEngineCode = faceEngine.unInit();
            faceEngine = null;
            Log.i(TAG, "销毁引擎：" + faceEngineCode);
        }
    }

    /**
     * 图片处理的主要逻辑部分
     */
    public void ProcessImage() {
        /**
         * 1.准备操作（校验，显示，获取BGR）
         */
        if (mBitmap == null) {
            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.face_faces);
        }
        // 图像对齐
        Bitmap bitmap = ArcSoftImageUtil.getAlignedBitmap(mBitmap, true);
        final SpannableStringBuilder notificationSpannableStringBuilder = new SpannableStringBuilder();
        if (faceEngineCode != ErrorInfo.MOK) {
            CreateNotificationInfo(notificationSpannableStringBuilder, null, "人脸引擎未初始化！");
            return;
        }
        if (bitmap == null) {
            CreateNotificationInfo(notificationSpannableStringBuilder, null, "图片为空！");
            return;
        }
        if (faceEngine == null) {
            CreateNotificationInfo(notificationSpannableStringBuilder, null, "人脸引擎为空！");
            return;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        final Bitmap finalBitmap = bitmap;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Glide.with(_txt.getContext()).load(finalBitmap).into(_img);
            }
        });
        //bitmap转bgr24
        byte[] bgr24 = ArcSoftImageUtil.createImageData(bitmap.getWidth(), bitmap.getHeight(), ArcSoftImageFormat.BGR24);
        int transformCode = ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
        if (transformCode != ArcSoftImageUtilError.CODE_SUCCESS) {
            Log.e(TAG, "bitmap转换bgr24失败：" + transformCode);
            CreateNotificationInfo(notificationSpannableStringBuilder, new StyleSpan(Typeface.BOLD), "转换bitmap到图片数据失败", "错误代码：", String.valueOf(transformCode), "\n");
            return;
        }
        CreateNotificationInfo(notificationSpannableStringBuilder, new StyleSpan(Typeface.BOLD), "检测到图片，宽：", String.valueOf(width), "，高：", String.valueOf(height), "\n");
        List<FaceInfo> faceInfoList = new ArrayList<>();
        /**
         * 2.成功获取到了BGR24数据，开始人脸检测
         */
        //        ArcSoftImageInfo arcSoftImageInfo = new ArcSoftImageInfo(width,height,FaceEngine.CP_PAF_BGR24,new byte[][]{bgr24},new int[]{width * 3});
        //        Log.i(TAG, "processImage: " + arcSoftImageInfo.getPlanes()[0].length);
        //        int detectCode = faceEngine.detectFaces(arcSoftImageInfo, faceInfoList);
        int detectCode = faceEngine.detectFaces(bgr24, width, height, FaceEngine.CP_PAF_BGR24, DetectModel.RGB, faceInfoList);
        if (detectCode == ErrorInfo.MOK) {
            Log.i(TAG, "检测到人脸数据");
        }
        //绘制bitmap
        Bitmap bitmapForDraw = bitmap.copy(Bitmap.Config.RGB_565, true);
        Canvas canvas = new Canvas(bitmapForDraw);
        Paint paint = new Paint();
        CreateNotificationInfo(notificationSpannableStringBuilder, null, "检测结果：\n错误代码：", String.valueOf(detectCode), "，人脸数：", String.valueOf(faceInfoList.size()), "\n");
        /**
         * 3.若检测结果人脸数量大于0，则在bitmap上绘制人脸框并且重新显示到ImageView，若人脸数量为0，则无法进行下一步操作，操作结束
         */
        if (faceInfoList.size() > 0) {
            CreateNotificationInfo(notificationSpannableStringBuilder, new StyleSpan(Typeface.BOLD), "\n人脸区域：\n");
            paint.setAntiAlias(true);
            paint.setStrokeWidth(5);
            paint.setColor(Color.YELLOW);
            for (int i = 0; i < faceInfoList.size(); i++) {
                //绘制人脸框
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(faceInfoList.get(i).getRect(), paint);
                //绘制人脸序号
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                int textSize = faceInfoList.get(i).getRect().width() / 2;
                paint.setTextSize(textSize);
                canvas.drawText(String.valueOf(i), faceInfoList.get(i).getRect().left, faceInfoList.get(i).getRect().top, paint);
                CreateNotificationInfo(notificationSpannableStringBuilder, null, "人脸[", String.valueOf(i), "]:", faceInfoList.get(i).toString(), "\n");
            }
            //显示
            final Bitmap finalBitmapForDraw = bitmapForDraw;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Glide.with(_img.getContext()).load(finalBitmapForDraw).into(_img);
                }
            });
        } else {
            CreateNotificationInfo(notificationSpannableStringBuilder, null, "没有检测到人脸，退出操作！");
            return;
        }
        CreateNotificationInfo(notificationSpannableStringBuilder, null, "\n");
        /**
         * 4.上一步已获取到人脸位置和角度信息，传入给process函数，进行年龄、性别、三维角度、活体检测
         */
        long processStartTime = System.currentTimeMillis();
        int faceProcessCode = faceEngine.process(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList, FaceEngine.ASF_AGE | FaceEngine.ASF_GENDER | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_LIVENESS);

        if (faceProcessCode != ErrorInfo.MOK) {
            CreateNotificationInfo(notificationSpannableStringBuilder, new ForegroundColorSpan(Color.RED), "属性检测失败，错误代码：", String.valueOf(faceProcessCode), "\n");
        } else {
            Log.i(TAG, "人脸属性检测所耗时间：" + (System.currentTimeMillis() - processStartTime));
        }
        //年龄信息结果
        List<AgeInfo> ageInfoList = new ArrayList<>();
        //性别信息结果
        List<GenderInfo> genderInfoList = new ArrayList<>();
        //人脸三维角度结果
        List<Face3DAngle> face3DAngleList = new ArrayList<>();
        //活体检测结果
        List<LivenessInfo> livenessInfoList = new ArrayList<>();
        //获取年龄、性别、三维角度、活体结果
        int ageCode = faceEngine.getAge(ageInfoList);
        int genderCode = faceEngine.getGender(genderInfoList);
        int face3DAngleCode = faceEngine.getFace3DAngle(face3DAngleList);
        int livenessCode = faceEngine.getLiveness(livenessInfoList);

        if ((ageCode | genderCode | face3DAngleCode | livenessCode) != ErrorInfo.MOK) {
            CreateNotificationInfo(notificationSpannableStringBuilder, null, "年龄，性别，面部悬挂检测失败，错误代码：", String.valueOf(ageCode), "，", String.valueOf(genderCode), "，", String.valueOf(face3DAngleCode));

            return;
        }
        /**
         * 5.年龄、性别、三维角度已获取成功，添加信息到提示文字中
         */
        //年龄数据
        if (ageInfoList.size() > 0) {
            CreateNotificationInfo(notificationSpannableStringBuilder, new StyleSpan(Typeface.BOLD), "年龄：\n");
        }
        for (int i = 0; i < ageInfoList.size(); i++) {
            CreateNotificationInfo(notificationSpannableStringBuilder, null, "人脸[", String.valueOf(i), "]:", String.valueOf(ageInfoList.get(i).getAge()), "\n");
        }
        CreateNotificationInfo(notificationSpannableStringBuilder, null, "\n");

        //性别数据
        if (genderInfoList.size() > 0) {
            CreateNotificationInfo(notificationSpannableStringBuilder, new StyleSpan(Typeface.BOLD), "性别：\n");
        }
        for (int i = 0; i < genderInfoList.size(); i++) {
            CreateNotificationInfo(notificationSpannableStringBuilder, null, "人脸[", String.valueOf(i), "]:", genderInfoList.get(i).getGender() == GenderInfo.MALE ? "男" : (genderInfoList.get(i).getGender() == GenderInfo.FEMALE ? "女" : "未知"), "\n");
        }
        CreateNotificationInfo(notificationSpannableStringBuilder, null, "\n");
        //人脸三维角度数据
        if (face3DAngleList.size() > 0) {
            CreateNotificationInfo(notificationSpannableStringBuilder, new StyleSpan(Typeface.BOLD), "三维角度：\n");
            for (int i = 0; i < face3DAngleList.size(); i++) {
                CreateNotificationInfo(notificationSpannableStringBuilder, null, "人脸[", String.valueOf(i), "]:", face3DAngleList.get(i).toString(), "\n");
            }
        }
        CreateNotificationInfo(notificationSpannableStringBuilder, null, "\n");
        //活体检测数据
        if (livenessInfoList.size() > 0) {
            CreateNotificationInfo(notificationSpannableStringBuilder, new StyleSpan(Typeface.BOLD), "活体检测：\n");
            for (int i = 0; i < livenessInfoList.size(); i++) {
                String liveness = null;
                switch (livenessInfoList.get(i).getLiveness()) {
                    case LivenessInfo.ALIVE:
                        liveness = "活体";
                        break;
                    case LivenessInfo.NOT_ALIVE:
                        liveness = "非活体";
                        break;
                    case LivenessInfo.UNKNOWN:
                        liveness = "未知";
                        break;
                    case LivenessInfo.FACE_NUM_MORE_THAN_ONE:
                        liveness = "人脸数大于1";
                        break;
                    default:
                        liveness = "未知";
                        break;
                }
                CreateNotificationInfo(notificationSpannableStringBuilder, null, "人脸[", String.valueOf(i), "]:", liveness, "\n");
            }
        }
        CreateNotificationInfo(notificationSpannableStringBuilder, null, "\n");
        /**
         * 6.最后将图片内的所有人脸进行逐一比对并添加到提示文字中
         */
        if (faceInfoList.size() > 0) {
            FaceFeature[] faceFeatures = new FaceFeature[faceInfoList.size()];
            int[] extractFaceFeatureCodes = new int[faceInfoList.size()];
            CreateNotificationInfo(notificationSpannableStringBuilder, new StyleSpan(Typeface.BOLD), "特征提取：\n");
            for (int i = 0; i < faceInfoList.size(); i++) {
                faceFeatures[i] = new FaceFeature();
                //从图片解析出人脸特征数据
                extractFaceFeatureCodes[i] = faceEngine.extractFaceFeature(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList.get(i), faceFeatures[i]);
                if (extractFaceFeatureCodes[i] != ErrorInfo.MOK) {
                    CreateNotificationInfo(notificationSpannableStringBuilder, null, "人脸[", String.valueOf(i), "]特征：", "提取失败，错误代码：", String.valueOf(extractFaceFeatureCodes[i]), "\n");
                } else {
                    CreateNotificationInfo(notificationSpannableStringBuilder, null, "人脸[", String.valueOf(i), "]特征：", "提取成功\n");
                }
            }
            CreateNotificationInfo(notificationSpannableStringBuilder, null, "\n");
            //人脸特征的数量大于2，将所有特征进行比较
            if (faceFeatures.length >= 2) {
                CreateNotificationInfo(notificationSpannableStringBuilder, new StyleSpan(Typeface.BOLD), "相似度：\n");
                for (int i = 0; i < faceFeatures.length; i++) {
                    for (int j = i + 1; j < faceFeatures.length; j++) {
                        CreateNotificationInfo(notificationSpannableStringBuilder, new StyleSpan(Typeface.BOLD_ITALIC), "比较人脸[", String.valueOf(i), "]和人脸[", String.valueOf(j), "]：\n");
                        //若其中一个特征提取失败，则不进行比对
                        boolean canCompare = true;
                        if (extractFaceFeatureCodes[i] != 0) {
                            CreateNotificationInfo(notificationSpannableStringBuilder, null, "人脸[", String.valueOf(i), "]特征抽取失败，不进行比对！\n");
                            canCompare = false;
                        }
                        if (extractFaceFeatureCodes[j] != 0) {
                            CreateNotificationInfo(notificationSpannableStringBuilder, null, "人脸[", String.valueOf(j), "]特征抽取失败，不进行比对！\n");
                            canCompare = false;
                        }
                        if (!canCompare) {
                            continue;
                        }
                        FaceSimilar matching = new FaceSimilar();
                        //比对两个人脸特征获取相似度信息
                        faceEngine.compareFaceFeature(faceFeatures[i], faceFeatures[j], CompareModel.LIFE_PHOTO, matching);
                        //新增相似度比对结果信息
                        CreateNotificationInfo(notificationSpannableStringBuilder, null, "人脸[", String.valueOf(i), "]和人脸[", String.valueOf(j), "]的相似度：", String.valueOf(matching.getScore()), "\n");
                    }
                }
            }
        }
        runOnUiThread(() -> {
            _txt.setText(notificationSpannableStringBuilder);
            MyProgressDialogUtil.DismissAlertDialog();
        });
    }

    /**
     * 生成人脸比对信息
     *
     * @param stringBuilder 提示的字符串的存放对象
     * @param styleSpan     添加的字符串的格式
     * @param strings       字符串数组
     */
    private void CreateNotificationInfo(SpannableStringBuilder stringBuilder, ParcelableSpan styleSpan, String... strings) {
        if (stringBuilder == null || strings == null || strings.length == 0) {
            return;
        }
        int startLength = stringBuilder.length();
        for (String string : strings) {
            stringBuilder.append(string);
        }
        int endLength = stringBuilder.length();
        if (styleSpan != null) {
            stringBuilder.setSpan(styleSpan, startLength, endLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_CHOOSE_IMAGE) {
            if (data == null || data.getData() == null) {
                _txt.setText("图片获取失败！");
                return;
            }
            try {
                mBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            if (mBitmap == null) {
                _txt.setText("图片获取失败！");
                return;
            }
            Glide.with(_img.getContext()).load(mBitmap).into(_img);
        }
    }

    @Override
    protected void onDestroy() {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
        }
        mBitmap = null;

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = null;
        UnInitEngine();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_local_attribute_detection_img:
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, ACTION_CHOOSE_IMAGE);
                break;
            case R.id.btn_start_compare_attribute_detection_img:
                _btnStart.setClickable(false);
                MyProgressDialogUtil.ShowProgressDialog(this, false, null, "正在处理...");
                //图像转化操作和部分引擎调用比较耗时，建议放子线程操作
                Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                        ProcessImage();
                        emitter.onComplete();
                    }
                }).subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Object o) {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        _txt.setText(e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        _btnStart.setClickable(true);
                    }
                });
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_attribute_detection_image);
        _img = findViewById(R.id.img_attribute_detection_img);
        _img.setImageResource(R.drawable.face_faces);
        _btnLocal = findViewById(R.id.btn_local_attribute_detection_img);
        _btnLocal.setOnClickListener(this::onClick);
        _btnStart = findViewById(R.id.btn_start_compare_attribute_detection_img);
        _btnStart.setOnClickListener(this::onClick);
        _txt = findViewById(R.id.txt_face_attribute_detection_img);
        progressDialog = new AlertDialog.Builder(this).setTitle("正在进行比对").setView(new ProgressBar(this)).create();
        InitEngine();
    }

}
