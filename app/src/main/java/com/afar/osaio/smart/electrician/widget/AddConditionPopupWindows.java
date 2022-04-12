package com.afar.osaio.smart.electrician.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.afar.osaio.R;


public class AddConditionPopupWindows extends PopupWindow {
    private View mMenuView;
    private Context context;
    private AddConditionListener mListener;

    public AddConditionPopupWindows(Activity context) {
        super(context);
        this.context = context;
        init();
    }

    private void init() {
        // PopupWindow 导入
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.layout_pop_condition, null);
        View llWeatherChange = mMenuView.findViewById(R.id.llWeatherChange);
        View llSchedule = mMenuView.findViewById(R.id.llSchedule);
        View llDeviceChange = mMenuView.findViewById(R.id.llDeviceChange);

        llWeatherChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onWeatherChangeClick();
                    dismiss();
                }
            }
        });

        llSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onScheduleClick();
                    dismiss();
                }
            }
        });

        llDeviceChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onDeviceChangeClick();
                    dismiss();
                }
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

    public void setListener(AddConditionListener listener) {
        this.mListener = listener;
    }

    public interface AddConditionListener {
        void onWeatherChangeClick();

        void onScheduleClick();

        void onDeviceChangeClick();
    }

}
