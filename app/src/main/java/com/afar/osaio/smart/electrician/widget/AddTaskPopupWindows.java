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


public class AddTaskPopupWindows extends PopupWindow {
    private View mMenuView;
    private Context context;
    private AddTaskListener mListener;

    public AddTaskPopupWindows(Activity context) {
        super(context);
        this.context = context;
        init();
    }

    private void init() {
        // PopupWindow 导入
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.layout_pop_task, null);
        View llRunDevice = mMenuView.findViewById(R.id.llRunDevice);
        View llSelectSmart = mMenuView.findViewById(R.id.llSelectSmart);
        View llDelay = mMenuView.findViewById(R.id.llDelay);

        llRunDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onRunDeviceClick();
                    dismiss();
                }
            }
        });

        llSelectSmart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onSelectSmartClick();
                    dismiss();
                }
            }
        });

        llDelay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onDelayClick();
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

    public void setListener(AddTaskListener listener) {
        this.mListener = listener;
    }

    public interface AddTaskListener {
        void onRunDeviceClick();

        void onSelectSmartClick();

        void onDelayClick();
    }

}
