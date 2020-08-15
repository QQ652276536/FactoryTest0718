package com.zistone.factorytest0718.face.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceSimilar;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.arcsoft.imageutil.ArcSoftRotateDegree;
import com.zistone.factorytest0718.face.constants.CompareResult;
import com.zistone.factorytest0718.face.model.FaceRegisterInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 人脸库操作类，包含注册和搜索
 */
public class FaceServer {

    public static final String TAG = "FaceServer";
    public static final String IMG_SUFFIX = ".jpg";
    //存放注册图的目录
    private static final String SAVE_IMG_DIR = "/sdcard/Factory0718/FaceIdCompare/imgs";
    //存放特征的目录
    private static final String SAVE_FEATURE_DIR = "/sdcard/Factory0718/FaceIdCompare/features";

    public static FaceEngine _faceEngine = null;
    public static FaceServer _faceServer = null;
    public static List<FaceRegisterInfo> _faceRegisterInfoList;
    public static String _rootPath;
    //是否正在搜索人脸，保证搜索操作单线程进行
    public boolean _isProcessing = false;

    public static FaceServer getInstance() {
        if (_faceServer == null) {
            synchronized (FaceServer.class) {
                if (_faceServer == null) {
                    _faceServer = new FaceServer();
                }
            }
        }
        return _faceServer;
    }

    /**
     * 初始化
     *
     * @param context 上下文对象
     * @return 是否初始化成功
     */
    public boolean Init(Context context) {
        synchronized (this) {
            if (_faceEngine == null && context != null) {
                _faceEngine = new FaceEngine();
                int engineCode = _faceEngine.init(context, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY, 16, 1, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT);
                if (engineCode == ErrorInfo.MOK) {
                    InitFaceList(context);
                    return true;
                } else {
                    _faceEngine = null;
                    Log.e(TAG, "人脸库初始化失败，错误代码：" + engineCode);
                    return false;
                }
            }
            return false;
        }
    }

    /**
     * 销毁
     */
    public void UnInit() {
        synchronized (this) {
            if (_faceRegisterInfoList != null) {
                _faceRegisterInfoList.clear();
                _faceRegisterInfoList = null;
            }
            if (_faceEngine != null) {
                _faceEngine.unInit();
                _faceEngine = null;
            }
        }
    }

    /**
     * 初始化人脸特征数据以及人脸特征数据对应的注册图
     *
     * @param context 上下文对象
     */
    private void InitFaceList(Context context) {
        synchronized (this) {
            if (_rootPath == null) {
                _rootPath = context.getFilesDir().getAbsolutePath();
            }
            File featureDir = new File(_rootPath + File.separator + SAVE_FEATURE_DIR);
            if (!featureDir.exists() || !featureDir.isDirectory()) {
                return;
            }
            File[] featureFiles = featureDir.listFiles();
            if (featureFiles == null || featureFiles.length == 0) {
                return;
            }
            _faceRegisterInfoList = new ArrayList<>();
            for (File featureFile : featureFiles) {
                try {
                    FileInputStream fis = new FileInputStream(featureFile);
                    byte[] feature = new byte[FaceFeature.FEATURE_SIZE];
                    fis.read(feature);
                    fis.close();
                    _faceRegisterInfoList.add(new FaceRegisterInfo(feature, featureFile.getName()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int GetFaceNumber(Context context) {
        synchronized (this) {
            if (context == null) {
                return 0;
            }
            if (_rootPath == null) {
                _rootPath = context.getFilesDir().getAbsolutePath();
            }
            File featureFileDir = new File(_rootPath + File.separator + SAVE_FEATURE_DIR);
            int featureCount = 0;
            if (featureFileDir.exists() && featureFileDir.isDirectory()) {
                String[] featureFiles = featureFileDir.list();
                featureCount = featureFiles == null ? 0 : featureFiles.length;
            }
            int imageCount = 0;
            File imgFileDir = new File(_rootPath + File.separator + SAVE_IMG_DIR);
            if (imgFileDir.exists() && imgFileDir.isDirectory()) {
                String[] imageFiles = imgFileDir.list();
                imageCount = imageFiles == null ? 0 : imageFiles.length;
            }
            return featureCount > imageCount ? imageCount : featureCount;
        }
    }

    public int ClearAllFaces(Context context) {
        synchronized (this) {
            if (context == null) {
                return 0;
            }
            if (_rootPath == null) {
                _rootPath = context.getFilesDir().getAbsolutePath();
            }
            if (_faceRegisterInfoList != null) {
                _faceRegisterInfoList.clear();
            }
            File featureFileDir = new File(_rootPath + File.separator + SAVE_FEATURE_DIR);
            int deletedFeatureCount = 0;
            if (featureFileDir.exists() && featureFileDir.isDirectory()) {
                File[] featureFiles = featureFileDir.listFiles();
                if (featureFiles != null && featureFiles.length > 0) {
                    for (File featureFile : featureFiles) {
                        if (featureFile.delete()) {
                            deletedFeatureCount++;
                        }
                    }
                }
            }
            int deletedImageCount = 0;
            File imgFileDir = new File(_rootPath + File.separator + SAVE_IMG_DIR);
            if (imgFileDir.exists() && imgFileDir.isDirectory()) {
                File[] imgFiles = imgFileDir.listFiles();
                if (imgFiles != null && imgFiles.length > 0) {
                    for (File imgFile : imgFiles) {
                        if (imgFile.delete()) {
                            deletedImageCount++;
                        }
                    }
                }
            }
            return deletedFeatureCount > deletedImageCount ? deletedImageCount : deletedFeatureCount;
        }
    }

    /**
     * 用于预览时注册人脸
     *
     * @param context  上下文对象
     * @param nv21     NV21数据
     * @param width    NV21宽度
     * @param height   NV21高度
     * @param faceInfo {@link FaceEngine#detectFaces(byte[], int, int, int, List)}获取的人脸信息
     * @param name     保存的名字，若为空则使用时间戳
     * @return 是否注册成功
     */
    public boolean RegisterNv21(Context context, byte[] nv21, int width, int height, FaceInfo faceInfo, String name) {
        synchronized (this) {
            if (_faceEngine == null || context == null || nv21 == null || width % 4 != 0 || nv21.length != width * height * 3 / 2) {
                Log.e(TAG, "RegisterNv21：参数无效！");
                return false;
            }
            if (_rootPath == null) {
                _rootPath = context.getFilesDir().getAbsolutePath();
            }
            //特征存储的文件夹
            File featureDir = new File(_rootPath + File.separator + SAVE_FEATURE_DIR);
            if (!featureDir.exists() && !featureDir.mkdirs()) {
                Log.e(TAG, "RegisterNv21：创建用于存储人脸特征的文件失败！");
                return false;
            }
            //图片存储的文件夹
            File imgDir = new File(_rootPath + File.separator + SAVE_IMG_DIR);
            if (!imgDir.exists() && !imgDir.mkdirs()) {
                Log.e(TAG, "RegisterNv21：创建用于存储图片的文件失败！");
                return false;
            }
            FaceFeature faceFeature = new FaceFeature();
            //特征提取
            int code = _faceEngine.extractFaceFeature(nv21, width, height, FaceEngine.CP_PAF_NV21, faceInfo, faceFeature);
            if (code != ErrorInfo.MOK) {
                Log.e(TAG, "RegisterNv21：人脸特征提取失败，错误代码：" + code);
                return false;
            } else {
                String userName = name == null ? String.valueOf(System.currentTimeMillis()) : name;
                try {
                    //保存注册结果（注册图、特征数据），为了美观，扩大rect截取注册图
                    Rect cropRect = GetBestRect(width, height, faceInfo.getRect());
                    if (cropRect == null) {
                        Log.e(TAG, "RegisterNv21：注册图为空！");
                        return false;
                    }
                    cropRect.left &= ~3;
                    cropRect.top &= ~3;
                    cropRect.right &= ~3;
                    cropRect.bottom &= ~3;
                    File file = new File(imgDir + File.separator + userName + IMG_SUFFIX);
                    //创建一个头像的Bitmap，存放旋转结果图
                    Bitmap headBmp = GetHeadImage(nv21, width, height, faceInfo.getOrient(), cropRect,
                            ArcSoftImageFormat.NV21);
                    FileOutputStream fosImage = new FileOutputStream(file);
                    headBmp.compress(Bitmap.CompressFormat.JPEG, 100, fosImage);
                    fosImage.close();
                    FileOutputStream fosFeature = new FileOutputStream(featureDir + File.separator + userName);
                    fosFeature.write(faceFeature.getFeatureData());
                    fosFeature.close();
                    //内存中的数据同步
                    if (_faceRegisterInfoList == null) {
                        _faceRegisterInfoList = new ArrayList<>();
                    }
                    _faceRegisterInfoList.add(new FaceRegisterInfo(faceFeature.getFeatureData(), userName));
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
    }

    /**
     * 用于注册照片人脸
     *
     * @param context 上下文对象
     * @param bgr24   bgr24数据
     * @param width   bgr24宽度
     * @param height  bgr24高度
     * @param name    保存的名字，若为空则使用时间戳
     * @return 是否注册成功
     */
    public boolean RegisterBgr24(Context context, byte[] bgr24, int width, int height, String name) {
        synchronized (this) {
            if (_faceEngine == null || context == null || bgr24 == null || width % 4 != 0 || bgr24.length != width * height * 3) {
                Log.e(TAG, "RegisterBgr24：参数无效！");
                return false;
            }
            if (_rootPath == null) {
                _rootPath = context.getFilesDir().getAbsolutePath();
            }
            //特征存储的文件夹
            File featureDir = new File(_rootPath + File.separator + SAVE_FEATURE_DIR);
            if (!featureDir.exists() && !featureDir.mkdirs()) {
                Log.e(TAG, "RegisterBgr24：创建用于存储人脸特征的失败！");
                return false;
            }
            //图片存储的文件夹
            File imgDir = new File(_rootPath + File.separator + SAVE_IMG_DIR);
            if (!imgDir.exists() && !imgDir.mkdirs()) {
                Log.e(TAG, "RegisterBgr24：创建用于存储图片的文件失败！");
                return false;
            }
            //人脸检测
            List<FaceInfo> faceInfoList = new ArrayList<>();
            int code = _faceEngine.detectFaces(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList);
            if (code == ErrorInfo.MOK && faceInfoList.size() > 0) {
                FaceFeature faceFeature = new FaceFeature();
                //特征提取
                code = _faceEngine.extractFaceFeature(bgr24, width, height, FaceEngine.CP_PAF_BGR24, faceInfoList.get(0), faceFeature);
                String userName = name == null ? String.valueOf(System.currentTimeMillis()) : name;
                try {
                    //保存注册结果（注册图、特征数据），为了美观，扩大rect截取注册图
                    if (code == ErrorInfo.MOK) {
                        Rect cropRect = GetBestRect(width, height, faceInfoList.get(0).getRect());
                        if (cropRect == null) {
                            Log.e(TAG, "RegisterBgr24：注册图为空！");
                            return false;
                        }
                        cropRect.left &= ~3;
                        cropRect.top &= ~3;
                        cropRect.right &= ~3;
                        cropRect.bottom &= ~3;
                        File file = new File(imgDir + File.separator + userName + IMG_SUFFIX);
                        FileOutputStream fosImage = new FileOutputStream(file);
                        //创建一个头像的Bitmap，存放旋转结果图
                        Bitmap headBmp = GetHeadImage(bgr24, width, height, faceInfoList.get(0).getOrient(), cropRect
                                , ArcSoftImageFormat.BGR24);
                        //保存到本地
                        headBmp.compress(Bitmap.CompressFormat.JPEG, 100, fosImage);
                        fosImage.close();
                        //保存特征数据
                        FileOutputStream fosFeature = new FileOutputStream(featureDir + File.separator + userName);
                        fosFeature.write(faceFeature.getFeatureData());
                        fosFeature.close();
                        //内存中的数据同步
                        if (_faceRegisterInfoList == null) {
                            _faceRegisterInfoList = new ArrayList<>();
                        }
                        _faceRegisterInfoList.add(new FaceRegisterInfo(faceFeature.getFeatureData(), userName));
                        return true;
                    } else {
                        Log.e(TAG, "RegisterBgr24：人脸特征提取失败，错误代码：" + code);
                        return false;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                Log.e(TAG, "RegisterBgr24：没有检测到人脸，错误代码：" + code);
                return false;
            }
        }
    }

    /**
     * 截取合适的头像并旋转，保存为注册头像
     *
     * @param originImageData 原始的BGR24数据
     * @param width           BGR24图像宽度
     * @param height          BGR24图像高度
     * @param orient          人脸角度
     * @param cropRect        裁剪的位置
     * @param imageFormat     图像格式
     * @return 头像的图像数据
     */
    private Bitmap GetHeadImage(byte[] originImageData, int width, int height, int orient, Rect cropRect,
                                ArcSoftImageFormat imageFormat) {
        byte[] headImageData = ArcSoftImageUtil.createImageData(cropRect.width(), cropRect.height(), imageFormat);
        int cropCode = ArcSoftImageUtil.cropImage(originImageData, headImageData, width, height, cropRect, imageFormat);
        if (cropCode != ArcSoftImageUtilError.CODE_SUCCESS) {
            throw new RuntimeException("注册头像失败，错误代码：" + cropCode);
        }
        //判断人脸旋转角度，若不为0度则旋转注册图
        byte[] rotateHeadImageData = null;
        int rotateCode;
        int cropImageWidth;
        int cropImageHeight;
        //90度或270度的情况，需要宽高互换
        if (orient == FaceEngine.ASF_OC_90 || orient == FaceEngine.ASF_OC_270) {
            cropImageWidth = cropRect.height();
            cropImageHeight = cropRect.width();
        } else {
            cropImageWidth = cropRect.width();
            cropImageHeight = cropRect.height();
        }
        ArcSoftRotateDegree rotateDegree = null;
        switch (orient) {
            case FaceEngine.ASF_OC_90:
                rotateDegree = ArcSoftRotateDegree.DEGREE_270;
                break;
            case FaceEngine.ASF_OC_180:
                rotateDegree = ArcSoftRotateDegree.DEGREE_180;
                break;
            case FaceEngine.ASF_OC_270:
                rotateDegree = ArcSoftRotateDegree.DEGREE_90;
                break;
            case FaceEngine.ASF_OC_0:
            default:
                rotateHeadImageData = headImageData;
                break;
        }
        //非0度的情况，旋转图像
        if (rotateDegree != null) {
            rotateHeadImageData = new byte[headImageData.length];
            rotateCode = ArcSoftImageUtil.rotateImage(headImageData, rotateHeadImageData, cropRect.width(), cropRect.height(), rotateDegree, imageFormat);
            if (rotateCode != ArcSoftImageUtilError.CODE_SUCCESS) {
                throw new RuntimeException("旋转图像失败，错误代码：" + rotateCode);
            }
        }
        //创建一个Bitmap，并将图像数据存放到Bitmap中
        Bitmap headBmp = Bitmap.createBitmap(cropImageWidth, cropImageHeight, Bitmap.Config.RGB_565);
        if (ArcSoftImageUtil.imageDataToBitmap(rotateHeadImageData, headBmp, imageFormat) != ArcSoftImageUtilError.CODE_SUCCESS) {
            throw new RuntimeException("image转为bitmap失败！");
        }
        return headBmp;
    }

    /**
     * 在特征库中搜索
     *
     * @param faceFeature 传入特征数据
     * @return 比对结果
     */
    public CompareResult GetTopOfFaceLib(FaceFeature faceFeature) {
        if (_faceEngine == null || _isProcessing || faceFeature == null || _faceRegisterInfoList == null || _faceRegisterInfoList.size() == 0) {
            return null;
        }
        FaceFeature tempFaceFeature = new FaceFeature();
        FaceSimilar faceSimilar = new FaceSimilar();
        float maxSimilar = 0;
        int maxSimilarIndex = -1;
        _isProcessing = true;
        for (int i = 0; i < _faceRegisterInfoList.size(); i++) {
            tempFaceFeature.setFeatureData(_faceRegisterInfoList.get(i).getFeatureData());
            _faceEngine.compareFaceFeature(faceFeature, tempFaceFeature, faceSimilar);
            if (faceSimilar.getScore() > maxSimilar) {
                maxSimilar = faceSimilar.getScore();
                maxSimilarIndex = i;
            }
        }
        _isProcessing = false;
        if (maxSimilarIndex != -1) {
            return new CompareResult(_faceRegisterInfoList.get(maxSimilarIndex).getName(), maxSimilar);
        }
        return null;
    }

    /**
     * 将图像中需要截取的Rect向外扩张一倍，若扩张一倍会溢出，则扩张到边界，若Rect已溢出，则收缩到边界
     *
     * @param width   图像宽度
     * @param height  图像高度
     * @param srcRect 原Rect
     * @return 调整后的Rect
     */
    private static Rect GetBestRect(int width, int height, Rect srcRect) {
        if (srcRect == null) {
            return null;
        }
        Rect rect = new Rect(srcRect);
        //原rect边界已溢出宽高的情况
        int maxOverFlow = Math.max(-rect.left, Math.max(-rect.top, Math.max(rect.right - width, rect.bottom - height)));
        if (maxOverFlow >= 0) {
            rect.inset(maxOverFlow, maxOverFlow);
            return rect;
        }
        //原rect边界未溢出宽高的情况
        int padding = rect.height() / 2;
        //若以此padding扩张rect会溢出，取最大padding为四个边距的最小值
        if (!(rect.left - padding > 0 && rect.right + padding < width && rect.top - padding > 0 && rect.bottom + padding < height)) {
            padding = Math.min(Math.min(Math.min(rect.left, width - rect.right), height - rect.bottom), rect.top);
        }
        rect.inset(-padding, -padding);
        return rect;
    }

}
