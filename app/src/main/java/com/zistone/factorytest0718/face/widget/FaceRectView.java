package com.zistone.factorytest0718.face.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.zistone.factorytest0718.face.model.DrawInfo;
import com.zistone.factorytest0718.face.util.DrawHelper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 用于显示人脸信息的控件
 */
public class FaceRectView extends View {

    //默认人脸框厚度
    private static final int DEFAULT_FACE_RECT_THICKNESS = 6;

    private CopyOnWriteArrayList<DrawInfo> _drawInfoList = new CopyOnWriteArrayList<>();
    private Paint _paint;

    public FaceRectView(Context context) {
        this(context, null);
    }

    public FaceRectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        _paint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (_drawInfoList != null && _drawInfoList.size() > 0) {
            for (int i = 0; i < _drawInfoList.size(); i++) {
                DrawHelper.DrawFaceRect(canvas, _drawInfoList.get(i), DEFAULT_FACE_RECT_THICKNESS, _paint);
            }
        }
    }

    public void ClearFaceInfo() {
        _drawInfoList.clear();
        postInvalidate();
    }

    public void AddFaceInfo(DrawInfo faceInfo) {
        _drawInfoList.add(faceInfo);
        postInvalidate();
    }

    public void AddFaceInfo(List<DrawInfo> faceInfoList) {
        _drawInfoList.addAll(faceInfoList);
        postInvalidate();
    }

}