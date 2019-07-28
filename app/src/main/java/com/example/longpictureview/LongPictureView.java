package com.example.longpictureview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import java.io.IOException;

public class LongPictureView extends View implements GestureDetector.OnGestureListener, View.OnTouchListener {

    private GestureDetector mGestureDetector;
    private Scroller mScroller;
    private BitmapFactory.Options mOptions;
    private BitmapRegionDecoder mDecoder;
    private Bitmap mBitmap;
    private Rect mRect;

    private int mImageWight, mImageHeight;
    private int mViewWidth, mViewHeight;
    private float mScale;

    private Paint mBarPaint;
    private float mBarLength;

    public LongPictureView(Context context) {
        this(context, null);
    }

    public LongPictureView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LongPictureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mGestureDetector = new GestureDetector(context, this);
        mScroller = new Scroller(context);
        mOptions = new BitmapFactory.Options();
        mRect = new Rect();

        mBarPaint = new Paint();
        mBarPaint.setStrokeWidth(5);
        mBarPaint.setColor(context.getResources().getColor(R.color.color_FAA032));

        setOnTouchListener(this);
    }

    public void setImage(String fileUrl) {
        //先读取原图片的信息  宽、高
        mOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileUrl, mOptions);
        mImageWight = mOptions.outWidth;
        mImageHeight = mOptions.outHeight;

        //开启复用
        mOptions.inMutable = true;
        //设置格式成RBG_565，因为565 存储像素点占用内存小，一个像素点只需要两个字节
        mOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        mOptions.inJustDecodeBounds = false;

        try {
            //创建一个区域解码器
            mDecoder = BitmapRegionDecoder.newInstance(fileUrl, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        requestLayout();
        postInvalidate();
    }

    /**
     * 在测量的时候把我们需要的内存区域获取到  存入到mRect中
     *
     * @param
     * @return
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取测量的view的大小
        mViewWidth = getMeasuredWidth();
        mViewHeight = getMeasuredHeight();

        //如果解码器拿不到，表示没有设置过要显示的图片
        if (null == mDecoder) {
            return;
        }

        //确定要加载的图片的区域
        mRect.left = 0;
        mRect.top = 0;
        mRect.right = mImageWight;
        //获取一个缩放比例
        mScale = mViewWidth / (float) mImageWight;
        //高度就根据缩放比进行获取
        mRect.bottom = (int) (mViewHeight / mScale);
    }

    /**
     * 画出内容
     *
     * @param
     * @return
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //如果解码器拿不到，表示没有设置过要显示的图片
        if (null == mDecoder) {
            return;
        }

        //复用上一张bitmap
        mOptions.inBitmap = mBitmap;
        //解码指定区域
        mBitmap = mDecoder.decodeRegion(mRect, mOptions);
        //把得到的矩阵大小的内存进行缩放
        Matrix matrix = new Matrix();
        matrix.setScale(mScale, mScale);
        canvas.drawBitmap(mBitmap, matrix, null);

        //绘制滚动条
        if (mScroller.isFinished()) {
            canvas.drawLine(mViewWidth - 20, 0, mViewWidth - 20, 0, mBarPaint);
        } else {
            canvas.drawLine(mViewWidth - 20, 1.0f * mRect.top / mImageHeight * mViewHeight, mViewWidth - 20, 1.0f * mRect.top / mImageHeight * mViewHeight + mBarLength, mBarPaint);
        }
    }

    /**
     * 手按下的回调
     *
     * @param e
     * @return
     */
    @Override
    public boolean onDown(MotionEvent e) {
        //如果移动还没有停止，强制停止
        if (!mScroller.isFinished()) {
            mScroller.forceFinished(true);
        }

        //计算滚动条高度
        mBarLength = 1.0f * mViewHeight / mImageHeight * mViewHeight;

        //继续接受后续事件
        return true;
    }

    /**
     * @param e1        手势按下去的事件   开始获取坐标
     * @param e2        当前手势事件   获取当前坐标
     * @param distanceX x方向移动的距离
     * @param distanceY y方向移动的距离
     * @return
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        //上下移动的时候，需要改变显示的区域  改mRect
        mRect.offset(0, (int) distanceY);
        //处理移动时已经移到了两个顶端的问题
        if (mRect.bottom > mImageHeight) {
            mRect.bottom = mImageHeight;
            mRect.top = mImageHeight - (int) (mViewHeight / mScale);
        }
        if (mRect.top < 0) {
            mRect.top = 0;
            mRect.bottom = (int) (mViewHeight / mScale);
        }
        postInvalidate();

        return false;
    }

    /**
     * 处理惯性问题
     *
     * @param e1
     * @param e2
     * @param velocityX 每秒移动的x点
     * @param velocityY
     * @return
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        //做计算 -velocityY 正负号问题，(按下手指不拿开，屏幕跟着手势方向，若松开,则向相反方向滑动) 故用负值才能正常使用
        mScroller.fling(0, mRect.top, 0, (int) -velocityY, 0, 0, 0,
                mImageHeight - (int) (mViewHeight / mScale));
        return false;
    }

    /**
     * 使用上一个接口的计算结果
     */
    @Override
    public void computeScroll() {
        if (mScroller.isFinished()) {
            return;
        }

        //true 表示当前滑动还没有结束
        if (mScroller.computeScrollOffset()) {
            mRect.top = mScroller.getCurrY();
            mRect.bottom = mRect.top + (int) (mViewHeight / mScale);
            postInvalidate();
        }
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }
}