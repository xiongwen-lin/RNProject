package com.afar.osaio.smart.electrician.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.electrician.smartScene.adapter.HourWheelAdapter;
import com.afar.osaio.smart.electrician.smartScene.adapter.MinutesWheelAdapter;
import com.contrarywind.listener.OnItemSelectedListener;
import com.contrarywind.view.WheelView;

public class SelectTimePopupWindows extends PopupWindow {
    private View mMenuView;
    private Context context;
    private SelectTimeListener mListener;

    private WheelView wheelStartFromHour;
    private WheelView wheelStartFromMinutes;
    private WheelView wheelStartToHour;
    private WheelView wheelStartToMinutes;
    private HourWheelAdapter mHourFromWheelAdapter;
    private MinutesWheelAdapter mMinutesFromWheelAdapter;
    private HourWheelAdapter mHourToWheelAdapter;
    private MinutesWheelAdapter mMinutesToWheelAdapter;
    private String startTime;
    private String endTime;
    private TextView tvFromTime;
    private TextView tvToTime;
    private TextView tvAddDay;

    public SelectTimePopupWindows(Activity context) {
        super(context);
        this.context = context;
        init();
    }

    private void init() {
        // PopupWindow 导入
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.layout_pop_timer, null);
        Button btnCancel = mMenuView.findViewById(R.id.btnCancel);
        Button btnConfirm = mMenuView.findViewById(R.id.btnConfirm);
        tvFromTime = mMenuView.findViewById(R.id.tvFromTime);
        tvToTime = mMenuView.findViewById(R.id.tvToTime);
        tvAddDay = mMenuView.findViewById(R.id.tvAddDay);
        wheelStartFromHour = mMenuView.findViewById(R.id.wheelStartFromHour);
        wheelStartFromMinutes = mMenuView.findViewById(R.id.wheelStartFromMinutes);
        wheelStartToHour = mMenuView.findViewById(R.id.wheelStartToHour);
        wheelStartToMinutes = mMenuView.findViewById(R.id.wheelStartToMinutes);

        mHourFromWheelAdapter = new HourWheelAdapter();
        mMinutesFromWheelAdapter = new MinutesWheelAdapter();

        wheelStartFromHour.setAdapter(mHourFromWheelAdapter);
        wheelStartFromHour.setCurrentItem(0);

        wheelStartFromMinutes.setAdapter(mMinutesFromWheelAdapter);
        wheelStartFromMinutes.setCurrentItem(0);

        wheelStartFromHour.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                startTime = addZeroForNum(String.valueOf(getFromCurrentH()), 2) + ":" + addZeroForNum(String.valueOf(getFromCurrentM()), 2);
                tvFromTime.setText(startTime);
                tvAddDay.setVisibility(isOverOneDay() ? View.VISIBLE : View.GONE);
            }
        });

        wheelStartFromMinutes.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                startTime = addZeroForNum(String.valueOf(getFromCurrentH()), 2) + ":" + addZeroForNum(String.valueOf(getFromCurrentM()), 2);
                tvFromTime.setText(startTime);
                tvAddDay.setVisibility(isOverOneDay() ? View.VISIBLE : View.GONE);
            }
        });

        mHourToWheelAdapter = new HourWheelAdapter();
        mMinutesToWheelAdapter = new MinutesWheelAdapter();

        wheelStartToHour.setAdapter(mHourToWheelAdapter);
        wheelStartToHour.setCurrentItem(23);

        wheelStartToMinutes.setAdapter(mMinutesToWheelAdapter);
        wheelStartToMinutes.setCurrentItem(59);

        wheelStartToHour.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                endTime = addZeroForNum(String.valueOf(getToCurrentH()), 2) + ":" + addZeroForNum(String.valueOf(getToCurrentM()), 2);
                tvToTime.setText(endTime);
                tvAddDay.setVisibility(isOverOneDay() ? View.VISIBLE : View.GONE);
            }
        });

        wheelStartToMinutes.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                endTime = addZeroForNum(String.valueOf(getToCurrentH()), 2) + ":" + addZeroForNum(String.valueOf(getToCurrentM()), 2);
                tvToTime.setText(endTime);
                tvAddDay.setVisibility(isOverOneDay() ? View.VISIBLE : View.GONE);
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    if (isOverOneDay()) {
                        mListener.onGetStartEndTime(startTime, endTime, NooieApplication.mCtx.getResources().getString(R.string.next_day));
                    } else {
                        //mListener.onGetStartEndTime(startTime, endTime, TeckinApplication.mCtx.getResources().getString(R.string.same_day));
                        mListener.onGetStartEndTime(startTime, endTime,"");
                    }
                    dismiss();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                dismiss();
            }
        });

        // 导入布局
        this.setContentView(mMenuView);
        // 设置动画效果
        this.setAnimationStyle(R.style.from_bottom_anim);
        this.setWidth(ViewGroup.LayoutParams.FILL_PARENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置可触
        this.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0x0000000);
        this.setBackgroundDrawable(dw);
        // 单击弹出窗以外处 关闭弹出窗
        mMenuView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                int height = mMenuView.findViewById(R.id.containerBtn).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });
    }

    public void setListener(SelectTimeListener listener) {
        this.mListener = listener;
    }

    public interface SelectTimeListener {
        void onGetStartEndTime(String start, String end, String overOneDay);
    }

    private boolean isOverOneDay() {
        return getFromCurrentH() * 60 + getFromCurrentM() > getToCurrentH() * 60 + getToCurrentM();
    }

    private int getFromCurrentH() {
        int currentH = 0;
        if (wheelStartFromHour != null && mHourFromWheelAdapter != null) {
            currentH = mHourFromWheelAdapter.getValue(wheelStartFromHour.getCurrentItem());
        }
        return currentH;
    }

    private int getFromCurrentM() {
        int currentM = 0;
        if (wheelStartFromMinutes != null && mMinutesFromWheelAdapter != null) {
            currentM = mMinutesFromWheelAdapter.getValue(wheelStartFromMinutes.getCurrentItem());
        }
        return currentM;
    }

    private int getToCurrentH() {
        int currentH = 0;
        if (wheelStartToHour != null && mHourToWheelAdapter != null) {
            currentH = mHourToWheelAdapter.getValue(wheelStartToHour.getCurrentItem());
        }
        return currentH;
    }

    private int getToCurrentM() {
        int currentM = 0;
        if (wheelStartToMinutes != null && mMinutesToWheelAdapter != null) {
            currentM = mMinutesToWheelAdapter.getValue(wheelStartToMinutes.getCurrentItem());
        }
        return currentM;
    }

    //左边补零
    public String addZeroForNum(String str, int strLength) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer sb = new StringBuffer();
                sb.append("0").append(str);// 左补0
                str = sb.toString();
                strLen = str.length();
            }
        }
        return str;
    }

    public void setStartTime(int startHour, int startMin) {
        wheelStartFromHour.setCurrentItem(startHour);
        wheelStartFromMinutes.setCurrentItem(startMin);
        startTime = addZeroForNum(String.valueOf(getFromCurrentH()), 2) + ":" + addZeroForNum(String.valueOf(getFromCurrentM()), 2);
        tvFromTime.setText(startTime);
        tvAddDay.setVisibility(isOverOneDay() ? View.VISIBLE : View.GONE);
    }

    public void setEndTime(int endHour, int endMin) {
        wheelStartToHour.setCurrentItem(endHour);
        wheelStartToMinutes.setCurrentItem(endMin);
        endTime = addZeroForNum(String.valueOf(getToCurrentH()), 2) + ":" + addZeroForNum(String.valueOf(getToCurrentM()), 2);
        tvToTime.setText(endTime);
        tvAddDay.setVisibility(isOverOneDay() ? View.VISIBLE : View.GONE);
    }

}
