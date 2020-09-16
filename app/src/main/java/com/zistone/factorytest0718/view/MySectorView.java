package com.zistone.factorytest0718.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

/**
 * 扇形图控件
 *
 * @author LiWei
 * @date 2020/9/15 17:44
 * @email 652276536@qq.com
 */
public class MySectorView extends View {

    private static final String TAG = "MySectorView";

    private int[] _colors = {Color.BLUE, Color.DKGRAY, Color.CYAN, Color.RED, Color.GREEN};
    private Paint _paint;
    private ArrayList<ViewData> _viewDatas;
    private int _width;
    private int _height;
    private RectF _rectF;

    public static class ViewData {
        public String name;
        public int value;
        public int color;
        public float percentage;
        public float angle;

        public ViewData(int value, String name) {
            this.value = value;
            this.name = name;
        }
    }

    public MySectorView(Context context) {
        super(context);
        initPaint();
    }

    public MySectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public MySectorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    public void setData(ArrayList<ViewData> _viewDatas) {
        this._viewDatas = _viewDatas;
        initData();
        invalidate();
    }

    private void initPaint() {
        _paint = new Paint();
        _paint.setColor(Color.WHITE);
        _paint.setStyle(Paint.Style.FILL);
        _paint.setTextSize(30);
        _rectF = new RectF();
    }

    private void initData() {
        if (null == _viewDatas || _viewDatas.size() == 0) {
            return;
        }
        float sumValue = 0;
        for (int i = 0; i < _viewDatas.size(); i++) {
            ViewData viewData = _viewDatas.get(i);
            sumValue += viewData.value;
            int j = i % _colors.length;
            viewData.color = _colors[j];
        }
        for (ViewData data : _viewDatas) {
            //计算百分比
            float percentage = data.value / sumValue;
            //对应的角度
            float angle = percentage * 360;
            data.percentage = percentage;
            data.angle = angle;
        }
    }

    @Override
    protected void onSizeChanged(int _width, int _height, int oldw, int oldh) {
        super.onSizeChanged(_width, _height, oldw, oldh);
        this._width = _width;
        this._height = _height;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.i(TAG, "宽：" + _width + "，高：" + _height);
        canvas.translate(_width / 2, _height / 2);
        float currentStartAngle = 0;
        //饼状图半径(取宽高里最小的值)
        float r = (float) (Math.max(_width, _height) / 2);
        Log.i(TAG, "饼状图半径：" + r);
        //设置将要用来画扇形的矩形的轮廓
        _rectF.set(-r, -r, r, r);
        //根据菜单列表计算每个弧的角度
        float everyAngle = 360 / _viewDatas.size();
        for (int i = 0; i < _viewDatas.size(); i++) {
            ViewData viewData = _viewDatas.get(i);
            _paint.setColor(viewData.color);
            //绘制扇形(通过绘制圆弧)
            canvas.drawArc(_rectF, currentStartAngle, viewData.angle, true, _paint);
            Log.i(TAG, "扇形" + i + "角度：" + viewData.angle);
            //绘制扇形上文字
            float textAngle = currentStartAngle + viewData.angle / 2;
            Log.i(TAG, "文字[" + i + "]角度：" + textAngle);
            _paint.setColor(Color.BLACK);
            float x = (float) ((r + 0) / 2 * Math.cos(textAngle * Math.PI / 180));
            float y = (float) ((r + 0) / 2 * Math.sin(textAngle * Math.PI / 180));
            Log.i(TAG, (r + 0) / 2 + "文字[" + i + "]坐标：" + x + "，" + y);
            _paint.setColor(Color.YELLOW);
            canvas.drawText(viewData.name, x, y, _paint);
            currentStartAngle += viewData.angle;
        }
    }

}