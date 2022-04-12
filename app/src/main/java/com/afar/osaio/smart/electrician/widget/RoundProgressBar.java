package com.afar.osaio.smart.electrician.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.nooie.common.utils.log.NooieLog;

/**
rpb_sweepStrokeWidth	    外弧线的宽度	dimension	2dp
rpb_sweepStrokeColor	    外弧线的颜色	color	Color.BLACK  外弧扫动时候的颜色
rpb_sweepStartAngle         外弧线的起始扫描角度	integer	-90
rpb_centerText	            中间文本	string	-
rpb_centerTextSize	        中间文本的文字大小	dimension	12sp
rpb_centerTextColor	        中间文本的文字颜色	color	Color.WHITE
rpb_centerBackgroundColor	中间区域的背景色	color	#808080
rpb_countDownTimeInMillis	倒计时的时间	integer	3000(ms)
rpb_progressDirection	    外弧扫过的方向	enum[forward(0),reverse(1)]	forward(0)
rpb_autoStart	            是否自动开启倒计时	boolean	true
rpb_drawOutsideWrapper	    是否绘制外弧wrapper	boolean	false     这个添加了才能绘制外弧
rpb_outsideWrapperColor	    外弧wrapper的颜色	color	#E8E8E8   外弧的颜色
rpb_supportEndToStart	    是否支持反转(true 绘制的progress=progress-360)	boolean	false
*/

public class RoundProgressBar extends View {
    /**
     * arcPaint
     */
    private Paint arcPaint;
    /**
     * arcRect
     */
    private RectF arcRect;
    /**
     * textPaint
     */
    private Paint textPaint;
    /**
     * arc StrokeWidth
     */
    private int strokeWidth;
    /**
     * countDown Arc StrokeColor
     */
    private int strokeColor;
    /**
     * progress
     */
    private int progress;
    /**
     * countDown millis default is 3000ms
     */
    private int countDownTimeMillis;
    /**
     * center background paint
     */
    private Paint centerBgPaint;
    /**
     * center background
     */
    private int centerBackground;
    /**
     * center text
     */
    private String centerText;
    /**
     * 中间图片的画笔
     */
    private Paint centerPicturePaint;
    /**
     * 中间图片
     */
    private Bitmap centerBitmap;
    /**
     * placeHolder if there is none text
     */
    private String emptyText = "100%";
    /**
     * center textColor
     */
    private int centerTextColor;
    /**
     * center textSize
     */
    private float centerTextSize;
    /**
     * measure text bounds
     */
    private Rect textBounds;
    /**
     * arc start angle default is -90
     */
    private int startAngle;
    /**
     * if is auto start,default is true
     */
    private boolean isAutoStart;
    /**
     * progress change listener
     */
    private ProgressChangeListener mProgressChangeListener;
    /**
     * arc sweep direction default is forward
     */
    private Direction mDirection;
    /**
     * direction index
     */
    private int directionIndex;
    private final Direction[] mDirections = {
            Direction.FORWARD,
            Direction.REVERSE
    };

    public enum Direction {
        /**
         * forward
         */
        FORWARD(0),
        /**
         * reverse
         */
        REVERSE(1);

        Direction(int ni) {
            nativeInt = ni;
        }

        final int nativeInt;
    }

    /**
     * value animator
     */
    private ValueAnimator animator;
    /**
     * if true draw outsideWrapper false otherwise
     */
    private boolean shouldDrawOutsideWrapper;
    /**
     * outsideWrapper color
     */
    private int outsideWrapperColor;
    /**
     * default space between view to bound
     */
    private int defaultSpace;
    /**
     * isSupport end to start, if true progress = progress - 360.
     */
    private boolean isSupportEts;

    private long currentTime;

    private int centerBitmapWidth;
    private int centerBitmapHeight;

    private boolean isSuccess;

    public RoundProgressBar(Context context) {
        this(context, null);
    }

    public RoundProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TeckinRoundProgressBar);
        //外弧宽度
        strokeWidth = a.getDimensionPixelSize(R.styleable.TeckinRoundProgressBar_rpb_sweepStrokeWidth, (int) dp2px(2));
        //外弧颜色
        strokeColor = a.getColor(R.styleable.TeckinRoundProgressBar_rpb_sweepStrokeColor, Color.BLACK);
        //外弧起始位置
        startAngle = a.getInteger(R.styleable.TeckinRoundProgressBar_rpb_sweepStartAngle, -90);
        //中间文本
        centerText = a.getString(R.styleable.TeckinRoundProgressBar_rpb_centerText);
        //中间文本大小
        centerTextSize = a.getDimension(R.styleable.TeckinRoundProgressBar_rpb_centerTextSize, sp2px(12));
        //中间文本颜色
        centerTextColor = a.getColor(R.styleable.TeckinRoundProgressBar_rpb_centerTextColor, Color.WHITE);
        //中间背景色
        centerBackground = a.getColor(R.styleable.TeckinRoundProgressBar_rpb_centerBackgroundColor, Color.parseColor("#808080"));
        //倒计时（毫秒）
        countDownTimeMillis = a.getInteger(R.styleable.TeckinRoundProgressBar_rpb_countDownTimeInMillis, 3 * 1000);
        //正或者反倒计时转动
        directionIndex = a.getInt(R.styleable.TeckinRoundProgressBar_rpb_progressDirection, 0);
        //是否自动开始计时
        isAutoStart = a.getBoolean(R.styleable.TeckinRoundProgressBar_rpb_autoStart, true);
        //是否绘制外层wrapper
        shouldDrawOutsideWrapper = a.getBoolean(R.styleable.TeckinRoundProgressBar_rpb_drawOutsideWrapper, false);
        //外层wrapper颜色
        outsideWrapperColor = a.getColor(R.styleable.TeckinRoundProgressBar_rpb_outsideWrapperColor, Color.parseColor("#E8E8E8"));
        //是否支持end to start
        isSupportEts = a.getBoolean(R.styleable.TeckinRoundProgressBar_rpb_supportEndToStart, false);
        a.recycle();
        defaultSpace = strokeWidth * 2;
        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        arcPaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(centerTextSize);
        textPaint.setTextAlign(Paint.Align.CENTER);

        centerBitmap = ((BitmapDrawable)getResources().getDrawable(R.drawable.connection_succeed)).getBitmap();
        centerBitmapWidth= centerBitmap.getWidth();
        centerBitmapHeight = centerBitmap.getHeight();

        centerBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        centerBgPaint.setStyle(Paint.Style.FILL);

        centerPicturePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        arcRect = new RectF();
        textBounds = new Rect();

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int minWidth = getMinWidth(widthMode, widthSize);
        int minHeight = getMinHeight(heightMode, heightSize);
        if (minWidth != minHeight) {
            int suggestedSize = Math.max(minWidth, minHeight);
            minWidth = suggestedSize;
            minHeight = suggestedSize;
        }
        setMeasuredDimension(minWidth, minHeight);

        NooieLog.e("onMeasure minWidth "+minWidth+"  minHeight "+minHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        arcRect.left = defaultSpace >> 1;
        arcRect.top = defaultSpace >> 1;
        arcRect.right = w - (defaultSpace >> 1);
        arcRect.bottom = h - (defaultSpace >> 1);

        NooieLog.e("onSizeChanged left "+arcRect.left+"  top "+arcRect.top+"  right "+arcRect.right+"  bottom "+arcRect.bottom);
    }

    /**
     * getMinWidth textHeight + paddingLeft + paddingRight + arcStrokeWidth * 2
     *
     * @param mode         mode
     * @param measuredSize measuredSize
     * @return minWidth
     */
    private int getMinWidth(int mode, int measuredSize) {
        int suggestSize = 0;
        switch (mode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                if (TextUtils.isEmpty(centerText)) {
                    textPaint.getTextBounds(emptyText, 0, emptyText.length(), textBounds);
                } else {
                    textPaint.getTextBounds(centerText, 0, centerText.length(), textBounds);
                }
                suggestSize = getPaddingLeft() + getPaddingRight() + textBounds.width() + defaultSpace;
                break;
            case MeasureSpec.EXACTLY:
                suggestSize = measuredSize;
                break;
            default:
        }
        return suggestSize;
    }

    /**
     * getMinHeight similar to {@link #getMinWidth(int, int)}.
     *
     * @param mode         mode
     * @param measuredSize measuredSize
     * @return minHeight
     */
    private int getMinHeight(int mode, int measuredSize) {
        int suggestSize = 0;
        switch (mode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                if (TextUtils.isEmpty(centerText)) {
                    textPaint.getTextBounds(emptyText, 0, emptyText.length(), textBounds);
                } else {
                    textPaint.getTextBounds(centerText, 0, centerText.length(), textBounds);
                }
                suggestSize = getPaddingTop() + getPaddingBottom() + textBounds.height() + defaultSpace;
                break;
            case MeasureSpec.EXACTLY:
                suggestSize = measuredSize;
                break;
            default:
        }
        return suggestSize;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        drawCenterBackground(canvas);
        if (shouldDrawOutsideWrapper) {
            drawOutsideWrapper(canvas);
        }

        drawArc(canvas);

        if (isSuccess){
            drawCenterPicture(canvas);
        }else {
            drawCenterText(canvas);
        }
    }

    /**
     * draw center background circle
     *
     * @param canvas
     */
    private void drawCenterBackground(Canvas canvas) {
        centerBgPaint.setColor(centerBackground);
        canvas.drawCircle(arcRect.centerX(), arcRect.centerY(), (arcRect.width() - (defaultSpace >> 2)) / 2, centerBgPaint);
    }

    /**
     * draw outside arc wrapper if needed
     *
     * @param canvas
     */
    private void drawOutsideWrapper(Canvas canvas) {
        arcPaint.setColor(outsideWrapperColor);
        canvas.drawArc(arcRect, 0, 360, false, arcPaint);
    }

    /**
     * draw sweep arc
     * core
     *
     * @param canvas
     */
    private void drawArc(Canvas canvas) {
        arcPaint.setStrokeWidth(strokeWidth);
        arcPaint.setColor(strokeColor);
        canvas.drawArc(arcRect, startAngle, isSupportEts ? progress - 360 : progress, false, arcPaint);
    }

    /**
     * draw centerText
     *
     * @param canvas
     */
    private void drawCenterText(Canvas canvas) {
        textPaint.setColor(centerTextColor);
        if (TextUtils.isEmpty(centerText)) {
            canvas.drawText(Math.abs((int) (progress / 3.6)) + "%", arcRect.centerX(), arcRect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2, textPaint);
        } else {
            canvas.drawText(centerText, arcRect.centerX(), arcRect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2, textPaint);
        }
    }

    /**
     * draw centerPicture
     *
     * @param canvas
     */
    private void drawCenterPicture(Canvas canvas) {
        NooieLog.e("drawCenterPicture left "+arcRect.left+"  top "+arcRect.top+"  right "+arcRect.right+"  bottom "+arcRect.bottom+ "  arcRect.centerX() "+arcRect.centerX()+"  arcRect.centerY() "+arcRect.centerY());
        canvas.drawBitmap(centerBitmap,arcRect.centerX()-centerBitmapWidth/2, arcRect.centerY()-centerBitmapHeight/2,centerPicturePaint);
    }

    /**
     * start
     */
    public void start() {
        initAnimator(countDownTimeMillis, mDirection);
    }

    /**
     * stop
     */
    public void stop() {
        if (animator != null) {
            animator.cancel();
        }
    }

    /**
     * pause
     */
    public void pause() {
        if (animator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                animator.pause();
            } else {
                currentTime = animator.getCurrentPlayTime();
                animator.cancel();
            }
        }
    }

    /**
     * resume
     */
    public void resume() {
        if (animator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                animator.resume();
            } else {
                animator.setCurrentPlayTime(currentTime);
                animator.start();
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setDirection(mDirections[directionIndex]);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop();
    }

    /**
     * init Animator
     *
     * @param duration  duration
     * @param direction sweep direction
     */
    private void initAnimator(int duration, Direction direction) {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
        int start = 0;
        int end = 360;
        if (direction == Direction.REVERSE) {
            start = 360;
            end = 0;
        }
        animator = ValueAnimator.ofInt(start, end).setDuration(duration);
        animator.setRepeatCount(0);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                progress = (int) animation.getAnimatedValue();
                if (mProgressChangeListener != null) {
                    mProgressChangeListener.onProgressChanged((int) (progress / 3.6));
                }
                invalidate();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mProgressChangeListener != null) {
                    mProgressChangeListener.onFinish();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


    }

    /**
     * set sweep arc strokeWidth
     *
     * @param strokeWidth strokeWidth
     */
    public void setStrokeWidth(int strokeWidth) {
        if (strokeWidth > 0) {
            this.strokeWidth = strokeWidth;
        }
    }

    /**
     * set sweep arc strokeColor
     *
     * @param strokeColor strokeColor
     */
    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
    }

    /**
     * set countDown millis
     *
     * @param countDownTimeMillis countDownTimeMillis
     */
    public void setCountDownTimeMillis(int countDownTimeMillis) {
        this.countDownTimeMillis = countDownTimeMillis;
    }

    public int getCountDownTimeMillis() {
        return this.countDownTimeMillis;
    }

    /**
     * set center background (color)
     *
     * @param centerBackground centerBackground
     */
    public void setCenterBackground(int centerBackground) {
        this.centerBackground = centerBackground;
    }

    /**
     * set center text
     * if is none , then start progress text countDown(100% ~ 0% | 0 % ~ 100%)
     *
     * @param centerText centerText
     */
    public void setCenterText(String centerText) {
        this.centerText = centerText;
    }

    /**
     * set center textColor
     *
     * @param centerTextColor centerTextColor
     */
    public void setCenterTextColor(int centerTextColor) {
        this.centerTextColor = centerTextColor;
    }

    /**
     * set center picture
     *
     * @param centerPicture centerPicture
     */


    /**
     * set center textSize
     *
     * @param centerTextSize centerTextSize
     */
    public void setCenterTextSize(float centerTextSize) {
        this.centerTextSize = centerTextSize;
    }

    /**
     * set sweep start angle
     *
     * @param startAngle start angle default is -90
     */
    public void setStartAngle(int startAngle) {
        this.startAngle = startAngle;
    }

    /**
     * set if is auto start
     *
     * @param isAutoStart if is auto start
     */
    public void setAutoStart(boolean isAutoStart) {
        this.isAutoStart = isAutoStart;
    }

    /**
     * set progressChange listener
     *
     * @param progressChangeListener progressChangeListener
     */
    public void setProgressChangeListener(ProgressChangeListener progressChangeListener) {
        mProgressChangeListener = progressChangeListener;
    }

    /**
     * set direction
     *
     * @param direction sweep direction
     */
    public void setDirection(Direction direction) {
        if (direction == null) {
            throw new RuntimeException("Direction is null");
        }
        mDirection = direction;
        switch (direction) {
            default:
            case FORWARD:
                progress = 0;
                break;
            case REVERSE:
                progress = 360;
                break;
        }
        if (isAutoStart) {
            start();
        }
    }

    /**
     * set progress by your self
     *
     * @param progress progress 0-360
     */
    public void setProgress(int progress) {
        if (progress > 360) {
            progress = 360;
        } else if (progress < 0) {
            progress = 0;
        }
        this.progress = progress;
        invalidate();
    }

    /**
     * set progress percent
     *
     * @param progressPercent 0-100
     */
    public void setProgressPercent(int progressPercent) {
        if (progressPercent > 100) {
            progressPercent = 100;
        } else if (progressPercent < 0) {
            progressPercent = 0;
        }
        this.progress = (int) (progressPercent * 3.6);
        invalidate();
    }

    public void setShouldDrawOutsideWrapper(boolean shouldDrawOutsideWrapper) {
        this.shouldDrawOutsideWrapper = shouldDrawOutsideWrapper;
    }

    public void setOutsideWrapperColor(int outsideWrapperColor) {
        this.outsideWrapperColor = outsideWrapperColor;
    }

    public boolean isSupportEts() {
        return isSupportEts;
    }

    public void setSupportEts(boolean supportEts) {
        isSupportEts = supportEts;
    }

    private float sp2px(float inParam) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, inParam, getContext().getResources().getDisplayMetrics());
    }

    private float dp2px(float dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return dp < 0 ? dp : Math.round(dp * displayMetrics.density);
    }

    public interface ProgressChangeListener {
        /**
         * onFinish
         */
        void onFinish();

        /**
         * onProgressChanged
         *
         * @param progress
         */
        void onProgressChanged(int progress);
    }

    public void setSuccess(){
        setStrokeColor(NooieApplication.mCtx.getResources().getColor(R.color.outsideWrapper));
        isSuccess = true;
        stop();
        setProgress(360);
    }

    public void release(){
        if (animator != null && animator.isRunning()){
            stop();
        }
    }

}

