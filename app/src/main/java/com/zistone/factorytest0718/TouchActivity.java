package com.zistone.factorytest0718;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * 触摸屏测试
 *
 * @author LiWei
 * @date 2020/7/18 9:33
 * @email 652276536@qq.com
 */
public class TouchActivity extends BaseActivity {

    private static final String TAG = "TouchActivity";

    /**
     * 自定义View类用于绘制内容
     */
    class MyTouchView extends View {
        private static final String TAG = "MyTouchView";
        //屏幕四个角的四个正方形框的宽高，在不包含它的基础上计算格子的数量
        private static final int FOUR_CORNERS_SIZE = 90;

        private Paint _paint;
        private Point _point;
        private Context _context;
        //屏幕的宽高
        private int _screenWidth, _screenHeight;
        //减去角落的正方形的大小以后的屏幕的宽高，后面计算格子的都使用的这两个参数
        private int _screenSubWidth, _screenSubHeight;
        //角落的正方形的斜边
        private double _oblique = Math.sqrt(FOUR_CORNERS_SIZE * FOUR_CORNERS_SIZE + FOUR_CORNERS_SIZE * FOUR_CORNERS_SIZE);
        //上下各边不包含正方形在内的格子数量
        private int _topBottomRectCount;
        //左右各边不包含正方形在内的格子数量
        private int _leftRightRectCount;
        //要绘制的三角形
        private TouchTriangle[] _touchTriangleArray;
        //要绘制的格子
        private TouchRect[] _touchRectArray;
        //因为是绘制的正方形，所以每个格子的宽高都是一样的，在计算格子数量的时候肯定会有余数
        private int _surplusWidth, _surplusHeight;

        /**
         * 封装一个类用于判断绘制的三角形是否被触摸过
         */
        class TouchTriangle {
            public Point _pointA;
            public Point _pointB;
            public Point _pointC;
            public Path _path;
            public boolean _isTouch = false;

            public TouchTriangle(Point pointA, Point pointB, Point pointC) {
                _path = new Path();
                _pointA = pointA;
                _pointB = pointB;
                _pointC = pointC;
                _path.moveTo(pointA.x, pointA.y);
                _path.lineTo(pointB.x, pointB.y);
                _path.lineTo(pointC.x, pointC.y);
                _path.close();
            }
        }

        /**
         * 封装一个类用于判断绘制的格子是否被触摸过
         */
        class TouchRect {
            public Rect _rect;
            public boolean _isTouch = false;

            public TouchRect() {
                _rect = new Rect();
            }
        }

        /**
         * 构造函数
         *
         * @param context
         */
        public MyTouchView(Context context) {
            super(context);
            _context = context;
            //描述屏幕有关的信息结构，比如大小、密度、字体
            DisplayMetrics displayMetrics = context.getApplicationContext().getResources().getDisplayMetrics();
            //屏幕的宽高
            _screenWidth = displayMetrics.widthPixels;
            _screenHeight = displayMetrics.heightPixels;
            //减去角落的正方形的大小以后的屏幕的宽高
            _screenSubWidth = _screenWidth - FOUR_CORNERS_SIZE * 2;
            _screenSubHeight = _screenHeight - FOUR_CORNERS_SIZE * 2;
            //上下各边不包含正方形在内的格子数量
            _topBottomRectCount = _screenSubWidth / FOUR_CORNERS_SIZE;
            //左右各边不包含正方形在内的格子数量
            _leftRightRectCount = _screenSubHeight / FOUR_CORNERS_SIZE;
            //整除不尽的宽高
            _surplusWidth = _screenSubWidth % FOUR_CORNERS_SIZE;
            _surplusHeight = _screenSubHeight % FOUR_CORNERS_SIZE;
            //有余数则每条边需要补一个格子
            if (_surplusWidth > 0) {
                _topBottomRectCount += 1;
            }
            if (_surplusHeight > 0) {
                _leftRightRectCount += 1;
            }
            Log.i(TAG, "设备屏幕的宽：" + _screenWidth + "，高：" + _screenHeight + "\r\n不包含正方形在内的屏幕的宽：" + _screenSubWidth + "，高：" + _screenSubHeight + "\r\n整除不尽的宽：" + _surplusWidth + "，高：" + _surplusHeight + "\r\n上下各边的格子数量：" + _topBottomRectCount + "\r\n左右各边的格子数量：" + _leftRightRectCount);
            _point = new Point();
            _paint = new Paint();
            _paint.setStyle(Paint.Style.STROKE);
            _paint.setColor(Color.BLACK);
            _paint.setAntiAlias(true);
            //初始化所有的三角形的状态
            InitTriangleRect();
            //初始化所有的格子状态
            InitSpecialRect();
        }

        /**
         * 初始化所有三角形的状态和位置
         */
        private void InitTriangleRect() {
            _touchTriangleArray = new TouchTriangle[8];
            //左上角的两个
            Point point00 = new Point(0, 0);
            Point point01 = new Point(FOUR_CORNERS_SIZE, FOUR_CORNERS_SIZE);
            Point point02 = new Point(0, FOUR_CORNERS_SIZE);
            _touchTriangleArray[0] = new TouchTriangle(point00, point01, point02);
            _touchTriangleArray[0]._isTouch = false;
            Point point10 = new Point(0, 0);
            Point point11 = new Point(FOUR_CORNERS_SIZE, 0);
            Point point12 = new Point(FOUR_CORNERS_SIZE, FOUR_CORNERS_SIZE);
            _touchTriangleArray[1] = new TouchTriangle(point10, point11, point12);
            _touchTriangleArray[1]._isTouch = false;
            //右上角的两个
            Point point20 = new Point(_screenSubWidth + FOUR_CORNERS_SIZE, 0);
            Point point21 = new Point(_screenWidth, 0);
            Point point22 = new Point(_screenSubWidth + FOUR_CORNERS_SIZE, FOUR_CORNERS_SIZE);
            _touchTriangleArray[2] = new TouchTriangle(point20, point21, point22);
            _touchTriangleArray[2]._isTouch = false;
            Point point30 = new Point(_screenWidth, 0);
            Point point31 = new Point(_screenWidth, FOUR_CORNERS_SIZE);
            Point point32 = new Point(_screenSubWidth + FOUR_CORNERS_SIZE, FOUR_CORNERS_SIZE);
            _touchTriangleArray[3] = new TouchTriangle(point30, point31, point32);
            _touchTriangleArray[3]._isTouch = false;
            //左下角的两个
            Point point40 = new Point(0, _screenSubHeight + FOUR_CORNERS_SIZE);
            Point point41 = new Point(FOUR_CORNERS_SIZE, _screenSubHeight + FOUR_CORNERS_SIZE);
            Point point42 = new Point(0, _screenHeight);
            _touchTriangleArray[4] = new TouchTriangle(point40, point41, point42);
            _touchTriangleArray[4]._isTouch = false;
            Point point50 = new Point(FOUR_CORNERS_SIZE, _screenSubHeight + FOUR_CORNERS_SIZE);
            Point point51 = new Point(FOUR_CORNERS_SIZE, _screenHeight);
            Point point52 = new Point(0, _screenHeight);
            _touchTriangleArray[5] = new TouchTriangle(point50, point51, point52);
            _touchTriangleArray[5]._isTouch = false;
            //右下角的两个
            Point point60 = new Point(_screenSubWidth + FOUR_CORNERS_SIZE, _screenSubHeight + FOUR_CORNERS_SIZE);
            Point point61 = new Point(_screenWidth, _screenHeight);
            Point point62 = new Point(_screenSubWidth + FOUR_CORNERS_SIZE, _screenHeight);
            _touchTriangleArray[6] = new TouchTriangle(point60, point61, point62);
            _touchTriangleArray[6]._isTouch = false;
            Point point70 = new Point(_screenSubWidth + FOUR_CORNERS_SIZE, _screenSubHeight + FOUR_CORNERS_SIZE);
            Point point71 = new Point(_screenWidth, _screenSubHeight + FOUR_CORNERS_SIZE);
            Point point72 = new Point(_screenWidth, _screenHeight);
            _touchTriangleArray[7] = new TouchTriangle(point70, point71, point72);
            _touchTriangleArray[7]._isTouch = false;
        }

        /**
         * 初始化所有格子的状态和位置
         */
        private void InitSpecialRect() {
            _touchRectArray = new TouchRect[_topBottomRectCount * 2 + _leftRightRectCount * 2];
            //因为这里用的是数组，所以绘制的格子位置是按照数组下标顺序计算，为了方便，这里位置顺序为：上-右-下-左
            for (int i = 0; i < _touchRectArray.length; i++) {
                _touchRectArray[i] = new TouchRect();
                _touchRectArray[i]._isTouch = false;
            }
            //上-右-下-左的长度
            int topLength = _topBottomRectCount;
            int topRightLength = _topBottomRectCount + _leftRightRectCount;
            int topRightBottomLength = _topBottomRectCount * 2 + _leftRightRectCount;
            int topRightBottomLeftLength = _touchRectArray.length;
            //上边每个格子的位置，从最左边不包含正方形的位置开始
            for (int i = 0; i < topLength; i++) {
                //从零开始计算格子的位置
                _touchRectArray[i]._rect.left = (i + 1) * FOUR_CORNERS_SIZE;
                //固定，零
                _touchRectArray[i]._rect.top = 0;
                _touchRectArray[i]._rect.right = FOUR_CORNERS_SIZE * (i + 2);
                //有余数的情况下最后一个格子的宽度=余数
                if (_surplusWidth > 0 && i == topLength - 1) {
                    _touchRectArray[i]._rect.right = _screenWidth - FOUR_CORNERS_SIZE;
                }
                //固定，不包含正方形的屏幕高度加上一个正方形大小
                _touchRectArray[i]._rect.bottom = FOUR_CORNERS_SIZE;
            }
            //右边每个格子的位置，从最上边不包含正方形的位置开始
            for (int i = topLength; i < topRightLength; i++) {
                int tempIndex = i - _topBottomRectCount;
                //固定，屏幕宽度减去一个正方形大小
                _touchRectArray[i]._rect.left = _screenWidth - FOUR_CORNERS_SIZE;
                //这里的减法是保证计算是从对应的边的第一个位置开始
                _touchRectArray[i]._rect.top = (tempIndex + 1) * FOUR_CORNERS_SIZE;
                //固定，屏幕宽度
                _touchRectArray[i]._rect.right = _screenWidth;
                _touchRectArray[i]._rect.bottom = (tempIndex + 2) * FOUR_CORNERS_SIZE;
                //有余数的情况下最后一个格子的高度=余数
                if (_surplusHeight > 0 && i == topRightLength - 1) {
                    _touchRectArray[i]._rect.bottom = _screenHeight - FOUR_CORNERS_SIZE;
                }
            }
            //下边每个格子的位置，从最右边不包含正方形的位置开始
            for (int i = topRightLength; i < topRightBottomLength; i++) {
                int tempIndex = i - _topBottomRectCount - _leftRightRectCount;
                int tempWidth = _screenWidth - FOUR_CORNERS_SIZE;
                //这里的减法是保证计算是从对应的边的最后一个位置开始
                _touchRectArray[i]._rect.left = tempWidth - (tempIndex + 1) * FOUR_CORNERS_SIZE;
                //固定，屏幕高度减去一个正方形的大小
                _touchRectArray[i]._rect.top = _screenHeight - FOUR_CORNERS_SIZE;
                _touchRectArray[i]._rect.right = tempWidth - (tempIndex) * FOUR_CORNERS_SIZE;
                //固定，屏幕高度
                _touchRectArray[i]._rect.bottom = _screenHeight;
                //有余数的情况下最后一个格子的宽度=余数
                if (_surplusWidth > 0 && i == topRightBottomLength - 1) {
                    _touchRectArray[i]._rect.left = FOUR_CORNERS_SIZE;
                    _touchRectArray[i]._rect.right = FOUR_CORNERS_SIZE + _surplusWidth;
                }
            }
            //左边每个格子的位置，从最下边不包含正方形的位置开始
            for (int i = topRightBottomLength; i < topRightBottomLeftLength; i++) {
                int tempIndex = i - _topBottomRectCount * 2 - _leftRightRectCount;
                int tempHeight = _screenHeight - FOUR_CORNERS_SIZE;
                //固定，零
                _touchRectArray[i]._rect.left = 0;
                //这里的减法是保证计算是从对应的边的最后一个位置开始
                _touchRectArray[i]._rect.top = tempHeight - (tempIndex + 1) * FOUR_CORNERS_SIZE;
                //固定，一个正方形的大小
                _touchRectArray[i]._rect.right = FOUR_CORNERS_SIZE;
                _touchRectArray[i]._rect.bottom = tempHeight - (tempIndex) * FOUR_CORNERS_SIZE;
                //有余数的情况下最后一个格子的高度=余数
                if (_surplusHeight > 0 && i == topRightBottomLeftLength - 1) {
                    _touchRectArray[i]._rect.top = FOUR_CORNERS_SIZE;
                    _touchRectArray[i]._rect.bottom = FOUR_CORNERS_SIZE + _surplusHeight;
                }
            }
        }

        /**
         * 绘制所有内容
         *
         * @param canvas
         */
        private void DrawViewRect(Canvas canvas) {
            //绘制三角形
            if (null != _touchTriangleArray) {
                for (int i = 0; i < _touchTriangleArray.length; i++) {
                    //已触摸
                    if (_touchTriangleArray[i]._isTouch) {
                        _paint.setStyle(Paint.Style.FILL);
                        _paint.setColor(Color.GREEN);
                    } else {
                        _paint.setStyle(Paint.Style.STROKE);
                        _paint.setColor(Color.BLACK);
                    }
                    canvas.drawPath(_touchTriangleArray[i]._path, _paint);
                }
            }
            //绘制格子
            if (null != _touchRectArray) {
                for (int i = 0; i < _touchRectArray.length; i++) {
                    //已触摸
                    if (_touchRectArray[i]._isTouch) {
                        _paint.setStyle(Paint.Style.FILL);
                        _paint.setColor(Color.GREEN);
                    } else {
                        _paint.setStyle(Paint.Style.STROKE);
                        _paint.setColor(Color.BLACK);
                    }
                    canvas.drawRect(_touchRectArray[i]._rect, _paint);
                }
            }
        }

        /**
         * 判断位置是否在三角形区域内
         *
         * @param pointA 三角形的第一个点
         * @param pointB 三角形的第二个点
         * @param pointC 三角形的第三个点
         * @param pointX 要判断的位置
         * @return
         */
        private boolean InTriangleArea(Point pointA, Point pointB, Point pointC, Point pointX) {
            double abc = CalcTriangleArea(pointA, pointB, pointC);
            double abx = CalcTriangleArea(pointA, pointB, pointX);
            double acx = CalcTriangleArea(pointA, pointC, pointX);
            double bcx = CalcTriangleArea(pointB, pointC, pointX);
            if (abc == abx + acx + bcx)
                return true;
            else
                return false;
        }

        /**
         * 计算三角形的面积
         *
         * @param point1
         * @param point2
         * @param point3
         * @return
         */
        private double CalcTriangleArea(Point point1, Point point2, Point point3) {
            return Math.abs(point1.x * point2.y + point2.x * point3.y + point3.x * point1.y - point2.x * point1.y - point3.x * point2.y - point1.x * point3.y);
        }

        /**
         * 判断当前三角形是否被触摸
         */
        private void JudgeCurrentTriangleIsTouch() {
            for (int i = 0; i < _touchTriangleArray.length; i++) {
                //根据当前触摸点的坐标判断是否滑动，且判断Touch事件的Point坐标，防止因为默认值触发绘制事件
                if (InTriangleArea(_touchTriangleArray[i]._pointA, _touchTriangleArray[i]._pointB, _touchTriangleArray[i]._pointC, _point) && _point.x > 0 && _point.y > 0)
                    _touchTriangleArray[i]._isTouch = true;
                //根据业务逻辑，每触摸一个三角形都需要判断所有三角形的触摸状态，以便判断是否测试通过
                JudgeAllTriangleIsTouch();
            }
        }

        /**
         * 判断当前格子是否被触摸
         */
        private void JudgeCurrentRectIsTouch() {
            for (int i = 0; i < _touchRectArray.length; i++) {
                //根据当前触摸点的坐标判断是否滑动
                if ((_point.x - _touchRectArray[i]._rect.left > 0) && (_touchRectArray[i]._rect.right - _point.x > 0) && (_point.y - _touchRectArray[i]._rect.top > 0) && (_touchRectArray[i]._rect.bottom - _point.y > 0))
                    _touchRectArray[i]._isTouch = true;
                //根据业务逻辑，每触摸一个格子都需要判断所有格子的触摸状态，以便判断是否测试通过
                JudgeAllRectIsTouch();
            }
        }

        /**
         * 判断所有三角形是否被触摸
         *
         * @return
         */
        private boolean JudgeAllTriangleIsTouch() {
            int tempJudge = _touchTriangleArray.length;
            for (int i = 0; i < _touchTriangleArray.length; i++) {
                if (_touchTriangleArray[i]._isTouch) {
                    tempJudge--;
                }
            }
            if (tempJudge == 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
                return true;
            }
            return false;
        }

        /**
         * 判断所有格子是否被触摸
         *
         * @return
         */
        private boolean JudgeAllRectIsTouch() {
            for (int i = 0; i < _touchRectArray.length; i++) {
                if (!_touchRectArray[i]._isTouch) {
                    return false;
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "所有格子已触摸完毕");
                    Pass();
                }
            });
            return true;
        }

        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            JudgeCurrentTriangleIsTouch();
            JudgeCurrentRectIsTouch();
            canvas.drawColor(Color.WHITE);
            DrawViewRect(canvas);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    _point.x = (int) event.getX();
                    _point.y = (int) event.getY();
                    JudgeCurrentTriangleIsTouch();
                    JudgeCurrentRectIsTouch();
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return true;
        }

        @Override
        public boolean onKeyUp(int keyCode, KeyEvent event) {
            return false;
        }

        @Override
        public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
            return true;
        }

    }

    private MyTouchView _myTouchView = null;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _myTouchView = new MyTouchView(this);
        //全屏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(_myTouchView);
    }

}
