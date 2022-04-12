package com.scenery7f.timeaxis.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.scenery7f.timeaxis.R;
import com.scenery7f.timeaxis.model.PeriodTime;
import com.scenery7f.timeaxis.util.DensityUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.util.Calendar.SECOND;

/**
 * Created by snoopy on 2017/9/15.
 */

public class TimerShaft extends RelativeLayout {

    /**
     * 公用变量
     */
    private Context mContext;

    private int halfWidth;
    private int halfHeight;

    private TextView timeTextView;

    private boolean mManuallyScroller = false;

    private int width;
    private int height;

    private int year;
    private int month;
    private int day;

    private OnTimeShaftListener onTimeShaftListener;

    public void setOnTimeShaftListener(OnTimeShaftListener onTimeShaftListener) {
        this.onTimeShaftListener = onTimeShaftListener;
    }
    //    private int marginTopBotton = 8;

    /**
     * TimerShaft 使用变量
     */
    private TimerHorizontalScrollView horizontalScrollView;
    private TimerVerticalScrollView verticalScrollView;
    private TimerView timerView;

    /**
     * TimerView 使用变量
     */
    private int scrollerBg = -1;
    private int markColor = Color.GREEN;// 画笔颜色
    private int scaleColor = Color.GRAY;
    private boolean isHorizontal = true;
    private int horizontalHeight;

    public List<PeriodTime> recordList = new ArrayList<>();

    public TimerShaft(Context context) {
        super(context);
        initUI(context);
    }

    public TimerShaft(Context context, int markColor, int scaleColor) {
        super(context);

        this.markColor = markColor;
        this.scaleColor = scaleColor;

        initUI(context);
    }

    public TimerShaft(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TimerShaft);
        markColor = array.getColor(R.styleable.TimerShaft_markColor, Color.GREEN);
        scaleColor = array.getColor(R.styleable.TimerShaft_scaleColor, Color.GRAY);
        isHorizontal = array.getBoolean(R.styleable.TimerShaft_isHorizontal, true);
        scrollerBg = array.getResourceId(R.styleable.TimerShaft_bg, -1);
        horizontalHeight = (int)(array.getDimension(R.styleable.TimerShaft_horizontalH, getResources().getDimension(R.dimen.date_select_height)));

        initUI(context);
    }

    private void initUI(Context context) {
        mContext = context;
        setDate(Calendar.getInstance().getTimeInMillis());

        DensityUtil.setContext(mContext);

        initView();

    }

    private void initView() {
        if ((isHorizontal && horizontalScrollView == null) ||
                (!isHorizontal && verticalScrollView == null)) {
            LayoutParams layoutParams;

            LinearLayout baseLinear = new LinearLayout(mContext);
            layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            baseLinear.setOrientation(isHorizontal ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
            baseLinear.setLayoutParams(layoutParams);
            baseLinear.setGravity(Gravity.CENTER);
            addView(baseLinear);

            RelativeLayout rl = new RelativeLayout(mContext);
            layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            rl.setLayoutParams(layoutParams);

            timeTextView = new TextView(mContext);
            layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            layoutParams.bottomMargin = DensityUtil.dip2px(1);
            layoutParams.topMargin = DensityUtil.dip2px(1);
            timeTextView.setLayoutParams(layoutParams);
            //timeTextView.setBackgroundColor(Color.BLACK);
            timeTextView.setTextSize(12);
            timeTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));

            /*
            if (isHorizontal) {
                timeTextView.setTextColor(Color.parseColor("#FFE9E8EA"));
            } else {
                timeTextView.setTextColor(Color.BLACK);
                timeTextView.setShadowLayer(1.0f, 1.0f, 1.0f, Color.GRAY);
            }
            */
            timeTextView.setTextColor(Color.parseColor("#5C9AD3"));
            //2018-08-08
            timeTextView.setText("00:00:00");

            // 滚动视图
            if (isHorizontal) {
                timeTextView.setPadding(DensityUtil.dip2px(2), DensityUtil.dip2px(1), DensityUtil.dip2px(2), DensityUtil.dip2px(1));
                baseLinear.addView(timeTextView);
                baseLinear.addView(rl);

                horizontalScrollView = new TimerHorizontalScrollView(mContext);
                if (scrollerBg != -1) {
                    horizontalScrollView.setBackgroundResource(scrollerBg);
                }

                layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, horizontalHeight);
                horizontalScrollView.setLayoutParams(layoutParams);
                horizontalScrollView.setHorizontalScrollBarEnabled(false);// 不显示滚动条
                rl.addView(horizontalScrollView);
            } else {
                timeTextView.setPadding(DensityUtil.dip2px(4), DensityUtil.dip2px(1), DensityUtil.dip2px(4), DensityUtil.dip2px(1));
                baseLinear.addView(timeTextView);
                baseLinear.addView(rl);

                verticalScrollView = new TimerVerticalScrollView(mContext);
                if (scrollerBg != -1) {
                    verticalScrollView.setBackgroundResource(scrollerBg);
                }

                layoutParams = new LayoutParams(getResources().getDimensionPixelSize(R.dimen.date_select_width), LayoutParams.WRAP_CONTENT);
                verticalScrollView.setLayoutParams(layoutParams);
                verticalScrollView.setVerticalScrollBarEnabled(false);// 不显示滚动条
                rl.addView(verticalScrollView);
            }

            // 添加视图
            LinearLayout linearLayout = new LinearLayout(mContext);
            layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            linearLayout.setLayoutParams(layoutParams);
            if (isHorizontal) {
                horizontalScrollView.addView(linearLayout);
            } else {
                verticalScrollView.addView(linearLayout);
            }

            // 初始化自定义视图，并添加
            timerView = new TimerView(mContext);
            //timerView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.transparent));
            layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            timerView.setLayoutParams(layoutParams);
            linearLayout.addView(timerView);

            // 创建红色指针线
            LineView redLine = new LineView(mContext);
            int w = DensityUtil.dip2px(14);
            int h = horizontalHeight;

            if (!isHorizontal) {
                w = getResources().getDimensionPixelSize(R.dimen.date_select_width);
                h = DensityUtil.dip2px(14);
            }

            layoutParams = new LayoutParams(w, h);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            redLine.setLayoutParams(layoutParams);
            rl.addView(redLine);
        }
    }

    /**
     * 设置数据并刷新页面
     *
     * @param recordList
     */
    public void setRecordList(List<PeriodTime> recordList) {
        this.recordList = recordList;
        timerView.postInvalidate();
        // move to first mark
        if (recordList.size() > 0) {
            moveScroll(recordList.get(0).getStartTime());
        } else {
            moveScroll(0, 0, 0);
        }
    }

    public boolean isInRecordList(long time) {
        for (int i = 0; i < recordList.size(); i++) {
            if (recordList.get(i).inThisPeriod(time)) {
                return true;
            }
        }
        return false;
    }

    /**
     * set date
     *
     * @param year
     * @param month
     * @param day
     */
    public void setDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public void setDate(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1;
        day = calendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 移动到指定时间
     *
     * @param calendar
     */
    public void moveScroll(Calendar calendar) {
        SimpleDateFormat fmtH = new SimpleDateFormat("HH");
        int m = calendar.get(Calendar.MINUTE);
        int h = Integer.valueOf(fmtH.format(calendar.getTime()));
        int s = calendar.get(SECOND);
        moveScroll(h, m, s);
    }

    /**
     * move to this position
     *
     * @param hour
     * @param minute
     * @param second
     */
    public void moveScroll(int hour, int minute, int second) {
        Log.d("debug","-->> DanalePlaybackActivity UtcTimerShaft moveScroll h=" + hour + " m=" + minute + " s=" + second);
        if (isHorizontal) {
            horizontalScrollView.scrollTo(timeToX(hour, minute, second) - halfWidth, 0);// 定位到指定位置
            timeTextView.setText(xToTimeStr(horizontalScrollView.getScrollX()));// 显示指定位置代表的时间
        } else {
            verticalScrollView.scrollTo(0, timeToX(hour, minute, second) - halfHeight);// 定位到指定位置
            timeTextView.setText(xToTimeStr(verticalScrollView.getScrollY()));// 显示指定位置代表的时间
        }
    }

    /**
     * 根据时间转换成坐标
     *
     * @param h
     * @param m
     * @param s
     * @return
     */
    private int timeToX(int h, int m, int s) {
        int x = halfWidth;
        if (!isHorizontal) {
            x = halfHeight;
        }

        x += h * 60 * 6 + m * 6 + (s % 10 <= 5 ? s / 10 : (s / 10 + 1));

        return x;
    }

    /**
     * 根据位置换成时间
     *
     * @param scrollX
     */

    private String xToTimeStr(int scrollX) {
        /*
        int s = scrollX % 6;

        scrollX -= s;
        scrollX /= 6;

        int m = scrollX % 60;

        scrollX -= m;

        int h = scrollX / 60;
        return (h > 9 ? h : ("0" + h)) + ":" + (m > 9 ? m : ("0" + m)) + ":" + s + "0";
        */

        long time = xToTime(scrollX);
        if (formatDate(year, month, day, 24, 0, 0) == time) {
            time -= 1;
        }

        //yyyy-MM-dd
        return getTimeString(time, "HH:mm:ss");
    }

    private long xToTime(int scrollX) {
        int s = scrollX % 6;

        scrollX -= s;
        scrollX /= 6;

        int m = scrollX % 60;

        scrollX -= m;

        int h = scrollX / 60;
        return formatDate(year, month, day, h, m, s * 10);
    }

    /**
     * format date time
     *
     * @param year
     * @param month
     * @param day
     * @return
     */
    public static long formatDate(int year, int month, int day, int hour, int minutes, int seconds) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder builder = new StringBuilder();
        builder.append(year);
        builder.append("-");
        if (month < 10) {
            builder.append(0);
        }
        builder.append(month);
        builder.append("-");
        if (day < 10) {
            builder.append(0);
        }
        builder.append(day);

        // 00:00:00
        builder.append(" ");
        if (hour < 10) {
            builder.append(0);
        }
        builder.append(hour);

        builder.append(":");
        if (minutes < 10) {
            builder.append(0);
        }
        builder.append(minutes);

        builder.append(":");
        if (seconds < 10) {
            builder.append(0);
        }
        builder.append(seconds);

        try {
            return dateFormat.parse(builder.toString()).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static String getTimeString(long time, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date date = new Date(time);
        return sdf.format(date);
    }

    public float getHorizontalScrollX() {
        return horizontalScrollView != null ? horizontalScrollView.getScrollX() : 0;
    }

    /**
     * ---------设置红色指针---------------------------------------------------
     */
    private class LineView extends View {

        private Paint line;

        public LineView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            // 绘制画笔
            initPaint();
            // 绘制竖线

            if (isHorizontal) {
                canvas.drawLine(DensityUtil.dip2px(7), 0, DensityUtil.dip2px(7), height - 0, line);
            } else {
                canvas.drawLine(0, DensityUtil.dip2px(7), width - 0, DensityUtil.dip2px(7), line);
            }

            // 绘制实心三角形
            Path path = new Path();
            if (isHorizontal) {
                path.moveTo(DensityUtil.dip2px(0), 0);// 左上角
                path.lineTo(DensityUtil.dip2px(14), 0);// 右上角
                path.lineTo(DensityUtil.dip2px(7), DensityUtil.dip2px(8) + 0); // 下角
            } else {
                path.moveTo(DensityUtil.dip2px(0), 0);// 左上角
                path.lineTo(DensityUtil.dip2px(7), DensityUtil.dip2px(7) + 0); // 右角
                path.lineTo(0, DensityUtil.dip2px(14));// 左下角
            }
            path.close();
            canvas.drawPath(path, line);
        }

        protected void initPaint() {
            line = new Paint();
            // 设置画笔为抗锯齿
            line.setAntiAlias(true);
            line.setStyle(Paint.Style.FILL);
            line.setStrokeWidth(DensityUtil.dip2px(1));
//            line.setColor(Color.parseColor("#FF5C9BD3"));
            line.setColor(Color.parseColor("#5C9AD3"));
        }

    }

    /**
     * ----------设置横向滚动轴-------------------------------------------------
     */
    private class TimerHorizontalScrollView extends HorizontalScrollView {

        private Handler mHandler;
        /**
         * 记录当前滚动的距离
         */
        private int saveX = 0;

        /**
         * 滚动监听间隔
         */
        private int scrollDelay = 50;
        /**
         * 滚动监听runnable
         */
        private Runnable scrollRunnable = new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (getScrollX() == saveX) {
                    //滚动停止  取消监听线程
                    mHandler.removeCallbacks(this);
                    if (onTimeShaftListener != null) {
                        long time = xToTime(getScrollX());
                        if (formatDate(year, month, day, 24, 0, 0) == time) {
                            time -= 1;
                        }
                        onTimeShaftListener.moveStop();
                        onTimeShaftListener.timeChangeOver(getTimeString(time, "yyyy-MM-dd HH:mm:ss"), time, mManuallyScroller);
                    }
                    mManuallyScroller = false;
                    return;
                } else {
                    //手指离开屏幕    view还在滚动的时候

                }
                saveX = getScrollX();
                mHandler.postDelayed(this, scrollDelay);
            }
        };

        public TimerHorizontalScrollView(Context context) {
            super(context);

            setHorizontalScrollBarEnabled(false);// 设置不显示滚动条
            saveX = getScrollX();
            mHandler = new Handler();
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    //手指在上面移动的时候   取消滚动监听线程
                    mHandler.removeCallbacks(scrollRunnable);
                    break;
                case MotionEvent.ACTION_UP:
                    //手指移动的时候
                    mHandler.post(scrollRunnable);
                    break;
                case MotionEvent.ACTION_DOWN:
                    mManuallyScroller = true;
                    //手指抬起的时候
                    if (onTimeShaftListener != null) {
                        onTimeShaftListener.moveStart();
                    }
                    break;
            }
            return super.onTouchEvent(ev);
        }

        @Override
        protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
            super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
            if (timeTextView != null) {
                timeTextView.setText(xToTimeStr(scrollX));
            }
            if (onTimeShaftListener != null) {
                onTimeShaftListener.timeChangeAction();
            }
        }
    }

    /**
     * ----------设置纵向滚动轴-------------------------------------------------
     */
    private class TimerVerticalScrollView extends ScrollView {

        private Handler mHandler;
        /**
         * 记录当前滚动的距离
         */
        private int saveY = 0;

        /**
         * 滚动监听间隔
         */
        private int scrollDelay = 50;
        /**
         * 滚动监听runnable
         */
        private Runnable scrollRunnable = new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (getScrollY() == saveY) {
                    //滚动停止  取消监听线程
                    mHandler.removeCallbacks(this);
                    if (onTimeShaftListener != null) {
                        long time = xToTime(getScrollY());
                        if (formatDate(year, month, day, 24, 0, 0) == time) {
                            time -= 1;
                        }
                        onTimeShaftListener.moveStop();
                        onTimeShaftListener.timeChangeOver(getTimeString(time, "yyyy-MM-dd HH:mm:ss"), time, mManuallyScroller);
                    }
                    mManuallyScroller = false;
                    return;
                } else {
                    //手指离开屏幕    view还在滚动的时候

                }
                saveY = getScrollY();
                mHandler.postDelayed(this, scrollDelay);
            }
        };

        public TimerVerticalScrollView(Context context) {
            super(context);
            saveY = getScrollY();
            mHandler = new Handler();
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    //手指在上面移动的时候   取消滚动监听线程
                    mHandler.removeCallbacks(scrollRunnable);
                    break;
                case MotionEvent.ACTION_UP:
                    //手指移动的时候
                    mHandler.post(scrollRunnable);
                    break;
                case MotionEvent.ACTION_DOWN:
                    mManuallyScroller = true;
                    //手指抬起的时候
                    if (onTimeShaftListener != null) {
                        onTimeShaftListener.moveStart();
                    }
                    break;
            }
            return super.onTouchEvent(ev);
        }

        @Override
        protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
            super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
            if (timeTextView != null) {
                timeTextView.setText(xToTimeStr(scrollY));
            }
            if (onTimeShaftListener != null) {
                onTimeShaftListener.timeChangeAction();
            }
        }
    }


    /**
     * ----------创建时间轴刻度---------------------------------------------------
     */
    private class TimerView extends View {

        private Paint scale;// 刻度
        private Paint mark;// 标记

        public TimerView(Context context) {
            super(context);
            initPaint();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            /*
            // 获取屏幕尺寸
            DisplayMetrics outMetrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(outMetrics);

            int width = outMetrics.widthPixels + 6 * 60 * 24;
            int height = horizontalHeight;

            if (!isHorizontal) {
                width = getResources().getDimensionPixelSize(R.dimen.date_select_width);
                height = outMetrics.heightPixels + 6 * 60 * 24;
            }

            //设置宽度和高度
            setMeasuredDimension(width, height);

            halfWidth = outMetrics.widthPixels / 2;
            halfHeight = outMetrics.heightPixels / 2;

            TimerShaft.this.width = width;
            TimerShaft.this.height = height;
            */

            // 获取屏幕尺寸
            //Log.d("debug", "-->> TimerView onMeasure mw=" + MeasureSpec.getSize(widthMeasureSpec) + " mh=" + MeasureSpec.getSize(heightMeasureSpec));
            DisplayMetrics outMetrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getRealMetrics(outMetrics);

            int widthSize = MeasureSpec.getSize(widthMeasureSpec) > 0 ? MeasureSpec.getSize(widthMeasureSpec) : outMetrics.widthPixels;
            int heightSize = MeasureSpec.getSize(heightMeasureSpec) > 0 ? MeasureSpec.getSize(heightMeasureSpec) : outMetrics.heightPixels;
            int width = widthSize + 6 * 60 * 24;
            int height = horizontalHeight;

            if (!isHorizontal) {
                width = getResources().getDimensionPixelSize(R.dimen.date_select_width);
                height = heightSize + 6 * 60 * 24;
            }

            //设置宽度和高度
            setMeasuredDimension(width, height);

            halfWidth = widthSize / 2;
            halfHeight = heightSize / 2;

            TimerShaft.this.width = width;
            TimerShaft.this.height = height;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            drawMark(canvas);
        }

        public List<PeriodTime> convertRecordPoint() {
            //weight权值用来修复相机时间加一秒和精度转换失真影响画刻度产生缝隙,以毫秒为单位
            int weight = 10 * 1000;
            int lastColor = 0;
            PeriodTime firstPt = null;
            PeriodTime lastPt = null;
            List<PeriodTime> result = new ArrayList<>();
            if (recordList.size() <= 1) {
                return recordList;
            }
            for (int i = 0; i < recordList.size(); i++) {
                PeriodTime pt = recordList.get(i);

                if (firstPt == null) {
                    firstPt = pt;
                }
                if ((lastPt != null && lastPt.getStopTime().getTimeInMillis() + weight < pt.getStartTime().getTimeInMillis()) || (lastColor != 0 && lastColor != pt.getColor())) {
                    result.add(new PeriodTime(firstPt.getStartTime(), lastPt.getStopTime(), firstPt.getColor(), firstPt.getRecordType()));
                    result.add(pt);

                    firstPt = null;
                    lastPt = pt;
                    lastColor = pt.getColor();
                    continue;
                }
                if (recordList.size() - 1 == i) {
                    result.add(new PeriodTime(firstPt.getStartTime(), pt.getStopTime(), firstPt.getColor(), firstPt.getRecordType()));
                }
                lastPt = pt;
                lastColor = pt.getColor();
            }
            return result;
        }

        /**
         * 绘制标记
         */
        private void drawMark(Canvas canvas) {
            /*
            if (recordList != null) {
                for (int i = 0; i < recordList.size(); i++) {
                    PeriodTime pt = recordList.get(i);
                    int startX = transform(pt.getStartTime());
                    int endX = transform(pt.getStopTime());

                    //过滤宽度为负数的时间段，否则有可能出现标记被覆盖
                    if (endX - startX < 0) {
                        continue;
                    }
                    mark.setColor(pt.getColor());
                    if (isHorizontal) {
                        canvas.drawRect(startX, 0, endX, height - 0, mark);//矩形
                    } else {
                        canvas.drawRect(0, startX, width - 0, endX, mark);//矩形
                    }
                }
            }

            drawScale(canvas);
            */

            if (recordList == null) {
                return;
            }
            List<PeriodTime> convertList = convertRecordPoint();

            if (convertList != null) {
                for (int i = 0; i < convertList.size(); i++) {
                    PeriodTime pt = convertList.get(i);
                    int startX = transform(pt.getStartTime());
                    int endX = transform(pt.getStopTime());

                    //过滤宽度为负数的时间段，否则有可能出现标记被覆盖
                    if (endX - startX < 0) {
                        continue;
                    }
                    mark.setColor(pt.getColor());
                    if (isHorizontal) {
                        canvas.drawRect(startX, 0, endX, height - 0, mark);//矩形
                    } else {
                        canvas.drawRect(0, startX, width - 0, endX, mark);//矩形
                    }
                }
            }

            drawScale(canvas);
        }


        /**
         * 绘制刻度
         *
         * @param canvas
         */
        private void drawScale(Canvas canvas) {

            /*
            if (isHorizontal) {
                //绘制上/下横线
                canvas.drawLine(0, 0, width, 0, scale);
                canvas.drawLine(0, height - 0, width, height - 0, scale);
            } else {
                //绘制左/右横线
                canvas.drawLine(0, 0, 0, height, scale);
                canvas.drawLine(width - 0, 0, width - 0, height, scale);
            }
            */

            Rect rect = new Rect();
            scale.getTextBounds("0", 0, 1, rect);
            int fontPadding = (height - rect.height()) / 2;

            // 绘制刻度
            if (isHorizontal) {
                for (int startx = halfWidth; startx <= width - halfWidth; startx += 6) {
                    int i = startx - halfWidth;
                    if (i % 15 == 0) {// 每个十五分钟为一个刻度
                        if (i % (15 * 6 * 4) == 0) { // 每个一小时为一个大刻度
                            //canvas.drawLine(startx, 0, startx, DensityUtil.dip2px(14), scale);// 上刻度
                            //canvas.drawLine(startx, height - 0, startx, height - DensityUtil.dip2px(14), scale);// 下刻度
                            int h = i / (15 * 6 * 4);
                            canvas.drawText((h > 9 ? h : "0" + h) + ":00", startx - DensityUtil.dip2px(14), /*DensityUtil.dip2px(26)*/height - fontPadding, scale);
                            //canvas.drawCircle(startx - DensityUtil.dip2px(14), /*DensityUtil.dip2px(26)*/height - fontPadding, 4, scale);
                            //canvas.drawCircle(startx - DensityUtil.dip2px(14), height / 2, 4, scale);
                        } else if (i % (15 * 6) == 0) {
                            //canvas.drawLine(startx, 0, startx, DensityUtil.dip2px(9), scale);// 上刻度
                            // canvas.drawLine(startx, height - 0, startx, height - DensityUtil.dip2px(9), scale);// 下刻度
                        } else {
                            // canvas.drawLine(startx, 0, startx, DensityUtil.dip2px(6), scale);// 上刻度
                            // canvas.drawLine(startx, height - 0, startx, height - DensityUtil.dip2px(6), scale);// 下刻度
                        }
                    } else {
                        // canvas.drawLine(startx, 0, startx, DensityUtil.dip2px(3), scale);// 上刻度
                        // canvas.drawLine(startx, height - 0, startx, height - DensityUtil.dip2px(3), scale);// 下刻度
                    }
                }
            } else {
                for (int startx = halfHeight; startx <= height - halfHeight; startx += 6) {
                    int i = startx - halfHeight;
                    if (i % 15 == 0) {// 每个十五分钟为一个刻度
                        if (i % (15 * 6 * 4) == 0) { // 每个一小时为一个大刻度
                            //canvas.drawLine(0, startx, DensityUtil.dip2px(14), startx, scale);// 上刻度
                            //canvas.drawLine(width - 0, startx, width - DensityUtil.dip2px(14), startx, scale);// 下刻度
                            int h = i / (15 * 6 * 4);
                            String text = (h > 9 ? h : "0" + h) + ":00";
                            int w = (int) scale.measureText(text);
                            canvas.drawText(text, /*DensityUtil.dip2px(26)*/(width - w) / 2, startx + rect.height() / 2, scale);
                            //canvas.drawCircle(startx - DensityUtil.dip2px(14), /*DensityUtil.dip2px(26)*/height   - fontPadding, 4, scale);
                            //canvas.drawCircle(startx - DensityUtil.dip2px(14), height / 2, 4, scale);
                        } else if (i % (15 * 6) == 0) {
                            //canvas.drawLine(0, startx, DensityUtil.dip2px(9), startx, scale);// 上刻度
                            //canvas.drawLine(width - 0, startx, width - DensityUtil.dip2px(9), startx, scale);// 下刻度
                        } else {
                            //canvas.drawLine(0, startx, DensityUtil.dip2px(6), startx, scale);// 上刻度
                            //canvas.drawLine(width - 0, startx, width - DensityUtil.dip2px(6), startx, scale);// 下刻度
                        }
                    } else {
                        //canvas.drawLine(0, startx, DensityUtil.dip2px(3), startx, scale);// 上刻度
                        //canvas.drawLine(width - 0, startx, width - DensityUtil.dip2px(3), startx, scale);// 下刻度
                    }
                }
            }
        }


        /**
         * 设置 画笔
         */
        private void initPaint() {
            scale = new Paint();
            // 设置画笔为抗锯齿
            scale.setAntiAlias(true);
            scale.setStyle(Paint.Style.FILL);
            scale.setStrokeWidth(DensityUtil.dip2px(1));
            scale.setColor(scaleColor);
            // 设置字体大小
            scale.setTextSize(DensityUtil.dip2px(10));

            mark = new Paint();
            // 设置画笔为抗锯齿
            mark.setAntiAlias(true);
            mark.setStyle(Paint.Style.FILL);
            mark.setColor(markColor);
        }

        private int transform(Calendar c) {

            SimpleDateFormat fmtH = new SimpleDateFormat("HH");
            int m = c.get(Calendar.MINUTE);
            int h = Integer.valueOf(fmtH.format(c.getTime()));
            int s = c.get(SECOND);

            return timeToX(h, m, s);
        }

    }

    public interface OnTimeShaftListener {
        void timeChangeOver(String timeStr, long time, boolean manuallyScroll);

        void timeChangeAction();

        void moveStart();

        void moveStop();
    }
}
