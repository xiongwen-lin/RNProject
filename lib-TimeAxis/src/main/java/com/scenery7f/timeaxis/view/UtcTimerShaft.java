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
import androidx.core.content.ContextCompat;
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
import com.scenery7f.timeaxis.model.RecordType;
import com.scenery7f.timeaxis.util.DensityUtil;
import com.scenery7f.timeaxis.util.PeriodUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static java.util.Calendar.SECOND;

/**
 * Created by snoopy on 2017/9/15.
 */

public class UtcTimerShaft extends RelativeLayout {

    /**
     * SECTOR_NUM_PER_MINUTE 需要为整数，方便计算
     * SCALE_TIME_LEN 每一个刻度时长
     */
    public static final int HOUR_NUM = 24;
    public static final int MINUTE_NUM = 60;
    public static final int SECOND_NUM = 60;
    public static final int DAY_SECOND_NUM = HOUR_NUM * MINUTE_NUM * SECOND_NUM;
    public static final int SCROLL_TIME_PRECISION = 1;
    public static final int SECTOR_NUM_PER_MINUTE = MINUTE_NUM / SCROLL_TIME_PRECISION;
    public static final int SCALE_TIME_LEN = 30;

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
    private boolean isAsc = true;

    public List<PeriodTime> recordList = new ArrayList<>();

    private boolean isNormalRecord = false;

    public UtcTimerShaft(Context context) {
        super(context);
        initUI(context);
    }

    public UtcTimerShaft(Context context, int markColor, int scaleColor) {
        super(context);

        this.markColor = markColor;
        this.scaleColor = scaleColor;

        initUI(context);
    }

    public UtcTimerShaft(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TimerShaft);
        markColor = array.getColor(R.styleable.TimerShaft_markColor, Color.GREEN);
        scaleColor = array.getColor(R.styleable.TimerShaft_scaleColor, Color.GRAY);
        isHorizontal = array.getBoolean(R.styleable.TimerShaft_isHorizontal, true);
        scrollerBg = array.getResourceId(R.styleable.TimerShaft_bg, -1);
        horizontalHeight = (int)(array.getDimension(R.styleable.TimerShaft_horizontalH, getResources().getDimension(R.dimen.date_select_height)));
        isAsc = array.getBoolean(R.styleable.TimerShaft_isAsc, true);

        if (array != null) {
            array.recycle();
        }

        initUI(context);
    }

    private void initUI(Context context) {
        mContext = context;
        setDate(getUtcCalendar().getTimeInMillis());
        DensityUtil.setContext(mContext);
        initView();

    }

    private void initView() {
        if ((isHorizontal && horizontalScrollView == null) || (!isHorizontal && verticalScrollView == null)) {
            LayoutParams layoutParams;

            //最外层容器
            LinearLayout baseLinear = new LinearLayout(mContext);
            layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            baseLinear.setOrientation(isHorizontal ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL);
            baseLinear.setLayoutParams(layoutParams);
            addView(baseLinear);

            //滑动时间轴容器
            RelativeLayout rl = new RelativeLayout(mContext);
            layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            rl.setLayoutParams(layoutParams);

            //当前时间标签
            timeTextView = new TextView(mContext);
            layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(isHorizontal ? RelativeLayout.CENTER_HORIZONTAL : RelativeLayout.CENTER_VERTICAL);
            layoutParams.bottomMargin = DensityUtil.dip2px(1);
            layoutParams.topMargin = DensityUtil.dip2px(1);
            timeTextView.setLayoutParams(layoutParams);
            timeTextView.setBackgroundColor(isHorizontal ? Color.TRANSPARENT : ContextCompat.getColor(getContext(), R.color.playback_time_text_bg));
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
            timeTextView.setTextColor(Color.parseColor("#b4e555"));
            timeTextView.setText("00:00:00");

            // 滚动视图
            if (isHorizontal) {
                timeTextView.setPadding(DensityUtil.dip2px(2), DensityUtil.dip2px(1), DensityUtil.dip2px(2), DensityUtil.dip2px(1));
                baseLinear.setGravity(Gravity.CENTER);
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
                timeTextView.setPadding(DensityUtil.dip2px(4), DensityUtil.dip2px(23), DensityUtil.dip2px(0), DensityUtil.dip2px(25));
                baseLinear.setGravity(Gravity.TOP);
                baseLinear.addView(rl);
//                baseLinear.addView(timeTextView);

                verticalScrollView = new TimerVerticalScrollView(mContext);
                if (scrollerBg != -1) {
                    verticalScrollView.setBackgroundResource(scrollerBg);
                }

                //layoutParams = new LayoutParams(getResources().getDimensionPixelSize(R.dimen.date_select_width), LayoutParams.WRAP_CONTENT);
                layoutParams = new LayoutParams(DensityUtil.dip2px(120), LayoutParams.WRAP_CONTENT);
                verticalScrollView.setLayoutParams(layoutParams);
                verticalScrollView.setVerticalScrollBarEnabled(false);// 不显示滚动条
                rl.addView(verticalScrollView);

                layoutParams = (LayoutParams)timeTextView.getLayoutParams();
                layoutParams.leftMargin = DensityUtil.dip2px(60);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                rl.addView(timeTextView);
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
            //timerView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.time_view_bg));
            layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            timerView.setLayoutParams(layoutParams);
            linearLayout.addView(timerView);

            int w = isHorizontal ? DensityUtil.dip2px(14) : getResources().getDimensionPixelSize(R.dimen.date_select_width);
            int h = isHorizontal ? horizontalHeight : DensityUtil.dip2px(14);
            // 创建红色指针线
            LineView redLine = new LineView(mContext);
            layoutParams = new LayoutParams(w, h);
            if (isHorizontal) {
                //layoutParams.topMargin = DensityUtil.dip2px(25);
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            } else {
                layoutParams.topMargin = DensityUtil.dip2px(25);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            }
            redLine.setLayoutParams(layoutParams);
            rl.addView(redLine);
        }
    }

    public void setIsNormalRecord(boolean isNormalRecord) {
        this.isNormalRecord = isNormalRecord;
    }

    /**
     * 设置数据并刷新页面
     *
     * @param recordList
     */
    public void setRecordList(List<PeriodTime> recordList) {
        this.recordList = recordList != null ? recordList : new ArrayList<PeriodTime>();
        timerView.postInvalidate();
        // move to first mark
        if (recordList.size() > 0) {
            moveScroll(recordList.get(0).getStartTime());
        } else {
            moveScroll(0, 0, 0);
        }
    }

    public boolean isInRecordList(long time) {
        if (recordList == null) {
            return false;
        }
        for (int i = 0; i < recordList.size(); i++) {
            if (recordList.get(i) != null && recordList.get(i).inThisPeriodUtc(time)) {
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
        Calendar calendar = getUtcCalendar();
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
        fmtH.setTimeZone(TimeZone.getTimeZone("UTC"));
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
        //Log.d("debug","-->> NooiePlaybackActivity UtcTimerShaft moveScroll h=" + hour + " m=" + minute + " s=" + second);
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

//        x += h * 60 * 6 + m * 6 + (s % 10 <= 5 ? s / 10 : (s / 10 + 1));
        //x += (h * MINUTE_NUM * SECTOR_NUM_PER_MINUTE) + (m * SECTOR_NUM_PER_MINUTE) + (s / SCROLL_TIME_PRECISION);
        if (isAsc) {
            x += (h * MINUTE_NUM * SECTOR_NUM_PER_MINUTE) + (m * SECTOR_NUM_PER_MINUTE) + (s / SCROLL_TIME_PRECISION);
        } else {
            x += HOUR_NUM * MINUTE_NUM * SECTOR_NUM_PER_MINUTE - ((h * MINUTE_NUM * SECTOR_NUM_PER_MINUTE) + (m * SECTOR_NUM_PER_MINUTE) + (s / SCROLL_TIME_PRECISION));
        }

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
        /*
        int s = scrollX % 6;

        scrollX -= s;
        scrollX /= 6;

        int m = scrollX % 60;

        scrollX -= m;

        int h = scrollX / 60;
        return formatDate(year, month, day, h, m, s * 10);
        */

        int s = scrollX % SECTOR_NUM_PER_MINUTE;

        scrollX -= s;
        scrollX /= SECTOR_NUM_PER_MINUTE;

        int m = scrollX % MINUTE_NUM;

        scrollX -= m;

        int h = scrollX / MINUTE_NUM;

        //logTimeDirection(h, m, s, isAsc, "xToTime");
        int timeOfDirection = convertTimeByDirection((h * MINUTE_NUM * SECOND_NUM + m * SECOND_NUM + s), isAsc);
        h = convertHourByTime(timeOfDirection);
        m = convertMinuteByTime(timeOfDirection);
        s = convertSecondByTime(timeOfDirection);

        return formatDate(year, month, day, h, m, s * SCROLL_TIME_PRECISION);
    }

    private int convertTimeByDirection(int time, boolean isAse) {
        if (time < 0) {
            time = 0;
        } else if (time > DAY_SECOND_NUM) {
            time = DAY_SECOND_NUM;
        }

        if (!isAse) {
            time = DAY_SECOND_NUM - time;
        }
        return time;
    }

    private int convertHourByTime(int time) {
        if (time < 0) {
            time = 0;
        } else if (time >= DAY_SECOND_NUM) {
            time = DAY_SECOND_NUM - 1;
        }
        int h = time / (MINUTE_NUM * SECOND_NUM);
        return h;
    }

    private int convertMinuteByTime(int time) {
        if (time < 0) {
            time = 0;
        } else if (time >= DAY_SECOND_NUM) {
            time = DAY_SECOND_NUM - 1;
        }
        int m = (time / SECOND_NUM) % MINUTE_NUM;
        return m;
    }

    private int convertSecondByTime(int time) {
        if (time < 0) {
            time = 0;
        } else if (time >= DAY_SECOND_NUM) {
            time = DAY_SECOND_NUM - 1;
        }
        int s = time % SECOND_NUM;
        return s;
    }

    private void logTimeDirection(int h, int m, int s, boolean isAsc, String tag) {
        int time = h * MINUTE_NUM * SECOND_NUM + m * SECOND_NUM + s;
        Log.d("debug", "-->> UtcTimerShaft logTimeDirection " + tag + " before h=" + h+ " m=" + m + " s=" + s + " time=" + time + " isAsc=" + isAsc);
        int timeOfDirection = convertTimeByDirection(time, isAsc);
        int hOfDirection = convertHourByTime(timeOfDirection);
        int mOfDirection = convertMinuteByTime(timeOfDirection);
        int sOfDirection = convertSecondByTime(timeOfDirection);
        Log.d("debug", "-->> UtcTimerShaft logTimeDirection " + tag + " after hOfDirection=" + hOfDirection + " m=" + mOfDirection + " s=" + sOfDirection + " time=" + timeOfDirection + " isAsc=" + isAsc);
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
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
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
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date(time);
        return sdf.format(date);
    }

    public float getHorizontalScrollX() {
        return horizontalScrollView != null ? horizontalScrollView.getScrollX() : 0;
    }

    private Calendar getUtcCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        return calendar;
    }

    public void release() {
        if (recordList != null) {
            recordList.clear();
            recordList = null;
        }
        setOnTimeShaftListener(null);
        timeTextView = null;
        horizontalScrollView = null;
        verticalScrollView = null;
        timerView = null;
        removeAllViews();
        mContext = null;
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
                //canvas.drawLine(0, DensityUtil.dip2px(7), width - 0, DensityUtil.dip2px(7), line);
                canvas.drawLine(0, DensityUtil.dip2px(7), width - 0, DensityUtil.dip2px(7), line);
            }

            // 绘制实心三角形
            Path path = new Path();
            if (isHorizontal) {
                path.moveTo(DensityUtil.dip2px(0), 0);// 左上角
                path.lineTo(DensityUtil.dip2px(14), 0);// 右上角
                path.lineTo(DensityUtil.dip2px(7), DensityUtil.dip2px(8) + 0); // 下角
            } else {
                /*
                path.moveTo(DensityUtil.dip2px(0), 0);// 左上角
                path.lineTo(DensityUtil.dip2px(7), DensityUtil.dip2px(7) + 0); // 右角
                path.lineTo(0, DensityUtil.dip2px(14));// 左下角
                */
                path.moveTo(width, 0);// 左上角
                path.lineTo(width - DensityUtil.dip2px(7), DensityUtil.dip2px(7) + 0); // 右角
                path.lineTo(width, DensityUtil.dip2px(14));// 左下角
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
            line.setColor(ContextCompat.getColor(getContext(), R.color.scale_line));
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
                        if (formatDate(year, month, day, HOUR_NUM, 0, 0) == time) {
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
                        if (formatDate(year, month, day, HOUR_NUM, 0, 0) == time) {
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
                //Log.d("debug", "-->> TimerVerticalScrollView onOverScrolled time=" + xToTimeStr(scrollY));
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
            Log.d("debug", "-->> TimerView onMeasure mw=" + outMetrics.widthPixels + " sw=" + MeasureSpec.getSize(widthMeasureSpec));

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

            UtcTimerShaft.this.width = width;
            UtcTimerShaft.this.height = height;
            */

            // 获取屏幕尺寸
            //Log.d("debug", "-->> TimerView onMeasure mw=" + MeasureSpec.getSize(widthMeasureSpec) + " mh=" + MeasureSpec.getSize(heightMeasureSpec));
            DisplayMetrics outMetrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getRealMetrics(outMetrics);

            int widthSize = MeasureSpec.getSize(widthMeasureSpec) > 0 ? MeasureSpec.getSize(widthMeasureSpec) : outMetrics.widthPixels;
            int heightSize = MeasureSpec.getSize(heightMeasureSpec) > 0 ? MeasureSpec.getSize(heightMeasureSpec) : outMetrics.heightPixels;
            int width = widthSize + dayLength;
            int height = horizontalHeight;

            if (!isHorizontal) {
                width = getResources().getDimensionPixelSize(R.dimen.date_select_width);
                height = heightSize + dayLength;
//                height = 6 * 60 * 24;
            }

            widgetW = width;
            widgetH = height;

            //设置宽度和高度
            setMeasuredDimension(width * 2, height);

            halfWidth = widthSize / 2;
            //halfHeight = heightSize / 2;
            halfHeight = DensityUtil.dip2px(32);

            UtcTimerShaft.this.width = width;
            UtcTimerShaft.this.height = height;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            drawBg(canvas);
            //drawMark(canvas);

            //log(convertRecordPoint(RecordType.PLAN_RECORD), "Plan record");
            //log(filterRecordPoint(recordList, RecordType.PLAN_RECORD), "Filter Plan record");
            //log(filterRecordPoint(recordList, RecordType.MOTION_RECORD), "Filter motion record");
            //log(filterRecordPoint(recordList, RecordType.PIR_RECORD), "Filter pir record");
            if (isNormalRecord) {
                drawMark(canvas, convertRecordPoint(RecordType.PLAN_RECORD));
            } else {
                drawMark(canvas, convertRecordPoint(RecordType.PLAN_RECORD));
                drawMark(canvas, convertRecordPoint(RecordType.MOTION_RECORD));
                drawMark(canvas, convertRecordPoint(RecordType.SOUND_RECORD));
                drawMark(canvas, convertRecordPoint(RecordType.PIR_RECORD));
            }
            drawScale(canvas);
        }

//        private int dayLength = 24 * 60 * 6;
        private int dayLength = HOUR_NUM * MINUTE_NUM * SECTOR_NUM_PER_MINUTE;
        private int widgetW = 0;
        private int widgetH = 0;

        private void drawBg(Canvas canvas) {
            if (isHorizontal) {
                mark.setColor(Color.parseColor("#03141C"));
                canvas.drawRect(0, 0, widgetW, DensityUtil.dip2px(25), mark);
                mark.setColor(Color.parseColor("#0B2734"));
                canvas.drawRect(0, DensityUtil.dip2px(25), widgetW, DensityUtil.dip2px(50), mark);
            } else {
                mark.setColor(Color.parseColor("#1A000B4B"));
                canvas.drawRect(0, 0, DensityUtil.dip2px(55), widgetH, mark);
            }
        }

        public String getUtcTimeString(long time, String pattern) {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            TimeZone utc = TimeZone.getTimeZone("UTC");
            sdf.setTimeZone(utc);
            Date date = new Date(time);
            return sdf.format(date);
        }

        private boolean containsRecordType(List<RecordType> recordTypes, RecordType targetRecordType) {
            return recordTypes != null && recordTypes.contains(targetRecordType);
        }

        public List<PeriodTime> filterRecordPoint(List<PeriodTime> sourcPeriodTimes, RecordType targetRecordType) {
            if (sourcPeriodTimes == null || targetRecordType == null) {
                return new ArrayList<>();
            }

            List<PeriodTime> periodTimes = new ArrayList<>();
            for (PeriodTime periodTime : sourcPeriodTimes) {
                if (containsRecordType(periodTime.getRecordTypes(), targetRecordType)) {
                    periodTimes.add(periodTime);
                }
            }
            return periodTimes;
        }

        public List<PeriodTime> convertRecordPoint(RecordType targetRecordType) {
            //weight权值用来修复相机时间加一秒和精度转换失真影响画刻度产生缝隙,以毫秒为单位
            int weight = 1 * 1000;
            PeriodTime firstPt = null;
            PeriodTime lastPt = null;
            List<PeriodTime> result = new ArrayList<>();
            if (recordList == null || recordList.size() == 0) {
                return result;
            }

            List<PeriodTime> filterRecordList = filterRecordPoint(recordList, targetRecordType);
            for (int i = 0; i < filterRecordList.size(); i++) {
                PeriodTime pt = filterRecordList.get(i);

                if (firstPt == null) {
                    firstPt = pt;
                    if (filterRecordList.size() - 1 == i) {
                        result.add(new PeriodTime(pt.getStartTime(), pt.getStopTime(), PeriodUtil.getPeriodColor(getContext(), targetRecordType), targetRecordType));
                        break;
                    }
                }

                if (lastPt != null && (lastPt.getStopTime().getTimeInMillis() + weight < pt.getStartTime().getTimeInMillis())) {
                    result.add(new PeriodTime(firstPt.getStartTime(), lastPt.getStopTime(), PeriodUtil.getPeriodColor(getContext(), targetRecordType), targetRecordType));
                    //result.add(pt);

                    firstPt = pt;
                    lastPt = pt;
                    if (filterRecordList.size() - 1 == i) {
                        result.add(new PeriodTime(pt.getStartTime(), pt.getStopTime(), PeriodUtil.getPeriodColor(getContext(), targetRecordType), targetRecordType));
                        break;
                    }
                    continue;
                }

                if (firstPt != null && filterRecordList.size() - 1 == i) {
                    result.add(new PeriodTime(firstPt.getStartTime(), pt.getStopTime(), PeriodUtil.getPeriodColor(getContext(), targetRecordType), targetRecordType));
                    break;
                }

                lastPt = pt;
            }

            if (!isAsc && result != null && !result.isEmpty()) {
                Collections.reverse(result);
                //result = reverseRecordPoint(result);
            }
            return result;
        }

        private List<PeriodTime> reverseRecordPoint(List<PeriodTime> recordPoints) {
            if (recordPoints == null || recordPoints.isEmpty()) {
                return new ArrayList<>();
            }

            //log(recordPoints, "reverse before");
            List<PeriodTime> result = new ArrayList<>();
            for (int i = recordPoints.size() - 1; i >= 0 ; i--) {
                PeriodTime recordPoint = recordPoints.get(i);
                result.add(recordPoint);
            }
            //log(result, "reverse after");

            return result;
        }

        private void drawMark(Canvas canvas, List<PeriodTime> convertList) {
            if (convertList == null) {
                return;
            }

            for (int i = 0; i < convertList.size(); i++) {
                PeriodTime pt = convertList.get(i);
                int startX = isAsc ? transform(pt.getStartTime()) : transform(pt.getStopTime());
                int endX = isAsc ? transform(pt.getStopTime()) : transform(pt.getStartTime());

                //过滤宽度为负数的时间段，否则有可能出现标记被覆盖
                if (endX - startX < 0) {
                    continue;
                }
                mark.setColor(pt.getColor());
                if (isHorizontal) {
                    int top = height/2;
                    if (pt.getRecordType() != null && pt.getRecordType() == RecordType.MOTION_RECORD) {
                        top = top + DensityUtil.dip2px(2);
                    } else if (pt.getRecordType() != null && pt.getRecordType() == RecordType.SOUND_RECORD) {
                        //top = top + DensityUtil.dip2px(15);
                        top = top + height / 2 - DensityUtil.dip2px(7);
                    } else if (pt.getRecordType() != null && pt.getRecordType() == RecordType.PIR_RECORD) {
                        //top = top + DensityUtil.dip2px(5);
                        top = top + height / 4 - DensityUtil.dip2px(5) / 2;
                    }  else if (pt.getRecordType() != null && pt.getRecordType() == RecordType.PLAN_RECORD) {
                        top = top + height / 4 - DensityUtil.dip2px(5) / 2;
                    }
                    int bottom= top + DensityUtil.dip2px(5);
                    canvas.drawRect(startX, top, endX, bottom, mark);//矩形
                } else {
                    int left = 0;
                    if (pt.getRecordType() != null && pt.getRecordType() == RecordType.MOTION_RECORD) {
                        left = DensityUtil.dip2px(12);
                    } else if (pt.getRecordType() != null && pt.getRecordType() == RecordType.SOUND_RECORD) {
                        left = DensityUtil.dip2px(37);
                    } else if (pt.getRecordType() != null && pt.getRecordType() == RecordType.PIR_RECORD) {
                        //left = DensityUtil.dip2px(12);
                        left = DensityUtil.dip2px(55) / 2 - DensityUtil.dip2px(6) / 2;
                    } else if (pt.getRecordType() != null && pt.getRecordType() == RecordType.PLAN_RECORD) {
                        left = DensityUtil.dip2px(55) / 2 - DensityUtil.dip2px(6) / 2;
                    }
                    int right= left + DensityUtil.dip2px(6);
                    canvas.drawRect(left, startX, right, endX, mark);//矩形
                }
            }
        }

        public List<PeriodTime> convertRecordPoint() {
            if (recordList == null) {
                return new ArrayList<>();
            }
            //weight权值用来修复相机时间加一秒和精度转换失真影响画刻度产生缝隙,以毫秒为单位
            int weight = 10 * 1000;
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
                if ((lastPt != null && (lastPt.getStopTime().getTimeInMillis() + weight < pt.getStartTime().getTimeInMillis() || lastPt.getRecordType().getValue() != pt.getRecordType().getValue()))) {
                    result.add(new PeriodTime(firstPt.getStartTime(), lastPt.getStopTime(), firstPt.getColor(), firstPt.getRecordType()));
                    result.add(pt);

                    firstPt = null;
                    lastPt = pt;
                    continue;
                }
                if (recordList.size() - 1 == i) {
                    result.add(new PeriodTime(firstPt.getStartTime(), pt.getStopTime(), firstPt.getColor(), firstPt.getRecordType()));
                }
                lastPt = pt;
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

                    //Log.d("TAG", "-->> debug TimerView drawMark starttime=" + getUtcTimeString(pt.getStartTime().getTimeInMillis(), "HH:mm:ss") + " startx=" + startX + " endtiem=" + getUtcTimeString(pt.getStopTime().getTimeInMillis(), "HH:mm:ss") + " endx=" + endX);
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
                        int top = height/2;
                        if (pt.getRecordType() != null && pt.getRecordType() == RecordType.MOTION_RECORD) {
                            top = top + DensityUtil.dip2px(5);
                        } else if (pt.getRecordType() != null && pt.getRecordType() == RecordType.SOUND_RECORD) {
                            top = top + DensityUtil.dip2px(15);
                        }
                        int bottom= top + DensityUtil.dip2px(5);
                        canvas.drawRect(startX, top, endX, bottom, mark);//矩形
                    } else {
                        int left = 0;
                        if (pt.getRecordType() != null && pt.getRecordType() == RecordType.MOTION_RECORD) {
                            left = DensityUtil.dip2px(12);
                        } else if (pt.getRecordType() != null && pt.getRecordType() == RecordType.SOUND_RECORD) {
                            left = DensityUtil.dip2px(30);
                        }
                        int right= left + DensityUtil.dip2px(6);
                        //canvas.drawRect(0, startX, width - 0, endX, mark);//矩形
                        canvas.drawRect(left, startX, right, endX, mark);//矩形
                    }
                }
            }

            drawScale(canvas);
        }

        private void log(List<PeriodTime> periodTimes, String target) {
            if (periodTimes == null) {
                return;
            }
            for (PeriodTime periodTime : periodTimes) {
                Log.d("TAG", "-->> TimerView log target=" + target + " start=" + getUtcTimeString(periodTime.getStartTime().getTimeInMillis(), "yyyy-MM-dd HH:mm:ss") + " endtiem=" + getUtcTimeString(periodTime.getStopTime().getTimeInMillis(), "yyyy-MM-dd HH:mm:ss") + " type=" + periodTime.getRecordType());
            }
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
                int endX = dayLength + halfWidth;
                int timeTextTop = (height / 2 - rect.height())/2 > 0 ? height / 2 - (height / 2 - rect.height())/2 : 0;
                for (int startx = halfWidth; startx <= endX; startx += 6) {
//                    int i = startx - halfWidth;
//                    if (i % 15 == 0) {// 每个十五分钟为一个刻度
//                        if (i % (15 * 6 * 4) == 0) { // 每个一小时为一个大刻度
//                            //canvas.drawLine(startx, 0, startx, DensityUtil.dip2px(14), scale);// 上刻度
//                            //canvas.drawLine(startx, height - 0, startx, height - DensityUtil.dip2px(14), scale);// 下刻度
//                            int h = i / (15 * 6 * 4);
//                            canvas.drawText((h > 9 ? h : "0" + h) + ":00", startx - DensityUtil.dip2px(14),timeTextTop, scale);
//                            //canvas.drawCircle(startx - DensityUtil.dip2px(14), /*DensityUtil.dip2px(26)*/height - fontPadding, 4, scale);
//                            //canvas.drawCircle(startx - DensityUtil.dip2px(14), height / 2, 4, scale);
//                        } else if (i % (15 * 6) == 0) {
//                            //canvas.drawLine(startx, 0, startx, DensityUtil.dip2px(9), scale);// 上刻度
//                            // canvas.drawLine(startx, height - 0, startx, height - DensityUtil.dip2px(9), scale);// 下刻度
//                        } else {
//                            // canvas.drawLine(startx, 0, startx, DensityUtil.dip2px(6), scale);// 上刻度
//                            // canvas.drawLine(startx, height - 0, startx, height - DensityUtil.dip2px(6), scale);// 下刻度
//                        }
//                    } else {
//                        // canvas.drawLine(startx, 0, startx, DensityUtil.dip2px(3), scale);// 上刻度
//                        // canvas.drawLine(startx, height - 0, startx, height - DensityUtil.dip2px(3), scale);// 下刻度
//                    }
                    int i = startx - halfWidth;
                    if (i % (SCALE_TIME_LEN * SECTOR_NUM_PER_MINUTE) == 0) {// 每个30分钟为一个刻度
                        if (i % (MINUTE_NUM * SECTOR_NUM_PER_MINUTE) == 0) { // 每个1小时为一个大刻度
                            //int h = i / (MINUTE_NUM * SECTOR_NUM_PER_MINUTE);
                            int h = convertScaleHByDirection((i / (MINUTE_NUM * SECTOR_NUM_PER_MINUTE)), isAsc);
                            String text = (h > 9 ? h : "0" + h) + ":00";
                            canvas.drawText(text, startx - DensityUtil.dip2px(14),timeTextTop, scale);
                        }else {
                            //int h = i / (MINUTE_NUM * SECTOR_NUM_PER_MINUTE);
                            int h = isAsc ? convertScaleHByDirection((i / (MINUTE_NUM * SECTOR_NUM_PER_MINUTE)), isAsc) : convertScaleHByDirection((i / (MINUTE_NUM * SECTOR_NUM_PER_MINUTE)), isAsc) - 1;
                            String text = (h > 9 ? h : "0" + h) + ":30";
                            canvas.drawText(text, startx - DensityUtil.dip2px(14),timeTextTop, scale);
                        }
                    } else {
                    }
                }
            } else {
                int endX = dayLength + halfHeight;
                int timeTextLeft = width + DensityUtil.dip2px(8.0f);
                for (int startx = halfHeight; startx <= endX; startx += SECTOR_NUM_PER_MINUTE) {
//                    int i = startx - halfHeight;
//                    if (i % 15 == 0) {// 每个十五分钟为一个刻度
//                        if (i % (15 * 6 * 4) == 0) { // 每个一小时为一个大刻度
//                            //canvas.drawLine(0, startx, DensityUtil.dip2px(14), startx, scale);// 上刻度
//                            //canvas.drawLine(width - 0, startx, width - DensityUtil.dip2px(14), startx, scale);// 下刻度
//                            int h = i / (15 * 6 * 4);
//                            String text = (h > 9 ? h : "0" + h) + ":00";
//                            int w = (int) scale.measureText(text);
//                            canvas.drawText(text, timeTextLeft, startx + rect.height() / 2, scale);
//                            //canvas.drawCircle(startx - DensityUtil.dip2px(14), /*DensityUtil.dip2px(26)*/height   - fontPadding, 4, scale);
//                            //canvas.drawCircle(startx - DensityUtil.dip2px(14), height / 2, 4, scale);
//                        } else if (i % (15 * 6) == 0) {
//                            //canvas.drawLine(0, startx, DensityUtil.dip2px(9), startx, scale);// 上刻度
//                            //canvas.drawLine(width - 0, startx, width - DensityUtil.dip2px(9), startx, scale);// 下刻度
//                        } else {
//                            //canvas.drawLine(0, startx, DensityUtil.dip2px(6), startx, scale);// 上刻度
//                            //canvas.drawLine(width - 0, startx, width - DensityUtil.dip2px(6), startx, scale);// 下刻度
//                        }
//                    } else {
//                        //canvas.drawLine(0, startx, DensityUtil.dip2px(3), startx, scale);// 上刻度
//                        //canvas.drawLine(width - 0, startx, width - DensityUtil.dip2px(3), startx, scale);// 下刻度
//                    }

                    int i = startx - halfHeight;
                    if (i % (SCALE_TIME_LEN * SECTOR_NUM_PER_MINUTE) == 0) {// 每个30分钟为一个刻度
                        if (i % (MINUTE_NUM * SECTOR_NUM_PER_MINUTE) == 0) { // 每个1小时为一个大刻度
                            //int h = i / (MINUTE_NUM * SECTOR_NUM_PER_MINUTE);
                            int h = convertScaleHByDirection((i / (MINUTE_NUM * SECTOR_NUM_PER_MINUTE)), isAsc);
                            String text = (h > 9 ? h : "0" + h) + ":00";
                            int w = (int) scale.measureText(text);
                            canvas.drawText(text, timeTextLeft, startx + rect.height() / 2, scale);
                            //Log.d("debug", "-->> UtcTimerShaft drawScale per h i=" + i + " text=" + text);
                        } else {
                            //int h = i / (MINUTE_NUM * SECTOR_NUM_PER_MINUTE);
                            int h = isAsc ? convertScaleHByDirection((i / (MINUTE_NUM * SECTOR_NUM_PER_MINUTE)), isAsc) : convertScaleHByDirection((i / (MINUTE_NUM * SECTOR_NUM_PER_MINUTE)), isAsc) - 1;
                            String text = (h > 9 ? h : "0" + h) + ":30";
                            int w = (int) scale.measureText(text);
                            canvas.drawText(text, timeTextLeft, startx + rect.height() / 2, scale);
                            //Log.d("debug", "-->> UtcTimerShaft drawScale per half h i=" + i + " text=" + text);
                        }
                    } else {
                    }
                }
            }
        }

        private int convertScaleHByDirection(int h, boolean isAsc) {
            //Log.d("debug", "-->> TimerView convertScaleHByDirection before h=" + h + " isAsc=" + isAsc);
            if (h < 0) {
                h = 0;
            } else if (h > HOUR_NUM) {
                h = HOUR_NUM;
            }

            if (!isAsc) {
                h = HOUR_NUM - h;
            }
            //Log.d("debug", "-->> TimerView convertScaleHByDirection after h=" + h + " isAsc=" + isAsc);
            return h;
        }

        /**
         * 设置 画笔
         */
        private void initPaint() {
            scaleColor = isHorizontal ? ContextCompat.getColor(mContext, R.color.scale_text_horizontal) : ContextCompat.getColor(mContext, R.color.scale_text);
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
            fmtH.setTimeZone(TimeZone.getTimeZone("UTC"));
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
