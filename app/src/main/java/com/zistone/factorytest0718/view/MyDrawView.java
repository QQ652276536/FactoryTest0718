package com.zistone.factorytest0718.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * 用于测试誉兴通设备的触摸屏控件
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class MyDrawView extends SurfaceView implements SurfaceHolder.Callback {

    protected SurfaceHolder _surfaceHolder;

    public MyDrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        _surfaceHolder = getHolder();
        _surfaceHolder.addCallback(this);
        _surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        setZOrderOnTop(true);
    }

    public void ClearDraw() {
        Canvas canvas = _surfaceHolder.lockCanvas();
        if (null != canvas) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }
        _surfaceHolder.unlockCanvasAndPost(canvas);
    }

    public void DrawTouchFocusRect(Rect rect, Paint paint) {
        Canvas canvas = _surfaceHolder.lockCanvas();
        canvas.drawColor(Color.TRANSPARENT);
        //左下角
        canvas.drawRect(rect.left - 2, rect.bottom, rect.left + 20, rect.bottom + 2, paint);
        canvas.drawRect(rect.left - 2, rect.bottom - 20, rect.left, rect.bottom, paint);
        //左上角
        canvas.drawRect(rect.left - 2, rect.top - 2, rect.left + 20, rect.top, paint);
        canvas.drawRect(rect.left - 2, rect.top, rect.left, rect.top + 20, paint);
        //右上角
        canvas.drawRect(rect.right - 20, rect.top - 2, rect.right + 2, rect.top, paint);
        canvas.drawRect(rect.right, rect.top, rect.right + 2, rect.top + 20, paint);
        //右下角
        canvas.drawRect(rect.right - 20, rect.bottom, rect.right + 2, rect.bottom + 2, paint);
        canvas.drawRect(rect.right, rect.bottom - 20, rect.right + 2, rect.bottom, paint);
        _surfaceHolder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

}
