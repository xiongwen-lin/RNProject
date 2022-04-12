package com.afar.osaio.widget;

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

public class RouterGuestWifiPopupWindows extends PopupWindow {
    private View mMenuView;
    private Context context;
    private OnClickOpenWifiListener myOnClick;

    public interface OnClickOpenWifiListener {
        void onRouterGuestWifiPopupClick(int position);
    }

    public RouterGuestWifiPopupWindows(Activity context, OnClickOpenWifiListener myOnClick) {
        super(context);
        this.context = context;
        this.myOnClick = myOnClick;
        Init();
    }

    private void Init() {
        // TODO Auto-generated method stub
        // PopupWindow 导入
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.router_guest_pop_wifi, null);
        Button btn_cancel = mMenuView.findViewById(R.id.btnCancel);

        TextView open_System = mMenuView.findViewById(R.id.open_System);
        TextView WPA2_Personal = mMenuView.findViewById(R.id.WPA2_Personal);
        TextView WPA3_Personal = mMenuView.findViewById(R.id.WPA3_Personal);
        TextView WPA21_Personal = mMenuView.findViewById(R.id.WPA21_Personal);
        TextView WPA31_Personal = mMenuView.findViewById(R.id.WPA31_Personal);
        TextView WPA2_Enterprise = mMenuView.findViewById(R.id.WPA2_Enterprise);
        TextView WPA21_Enterprise = mMenuView.findViewById(R.id.WPA21_Enterprise);

        open_System.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myOnClick != null) myOnClick.onRouterGuestWifiPopupClick(0);
                dismiss();
            }
        });

        WPA2_Personal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myOnClick != null) myOnClick.onRouterGuestWifiPopupClick(1);
                dismiss();
            }
        });

        WPA3_Personal.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (myOnClick != null) myOnClick.onRouterGuestWifiPopupClick(2);
                dismiss();
            }
        });

        WPA21_Personal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myOnClick != null) myOnClick.onRouterGuestWifiPopupClick(3);
                dismiss();
            }
        });

        WPA31_Personal.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (myOnClick != null) myOnClick.onRouterGuestWifiPopupClick(4);
                dismiss();
            }
        });

        WPA2_Enterprise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myOnClick != null) myOnClick.onRouterGuestWifiPopupClick(5);
                dismiss();
            }
        });

        WPA21_Enterprise.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (myOnClick != null) myOnClick.onRouterGuestWifiPopupClick(6);
                dismiss();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (myOnClick != null) myOnClick.onRouterGuestWifiPopupClick(7);
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
        ColorDrawable dw = new ColorDrawable(0x33010C11); // 33010C11  0xcc010c11
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
}