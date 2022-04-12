package com.afar.osaio.widget;

/**
 * Created by victor on 2018/9/5
 * Email is victor.qiao.0604@gmail.com
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;

import com.afar.osaio.R;

public class PhotoPopupWindows extends PopupWindow {
    private View mMenuView;
    private Context context;
    private OnClickSelectPhotoListener myOnClick;

    public interface OnClickSelectPhotoListener {
        void onClick(boolean takePhoto);
    }

    public PhotoPopupWindows(Activity context, OnClickSelectPhotoListener myOnClick) {
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
        mMenuView = inflater.inflate(R.layout.pop_select_photo, null);
        Button btn_camera = mMenuView
                .findViewById(R.id.btnTakePhoto);
        Button btn_photo = mMenuView
                .findViewById(R.id.btnAlbum);
        Button btn_cancel = mMenuView
                .findViewById(R.id.btnCancel);

        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myOnClick != null) myOnClick.onClick(true);
                dismiss();
            }
        });

        btn_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myOnClick != null) myOnClick.onClick(false);
                dismiss();
            }
        });

        btn_cancel.setOnClickListener(new OnClickListener() {

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
        this.setWidth(LayoutParams.FILL_PARENT);
        this.setHeight(LayoutParams.WRAP_CONTENT);
        // 设置可触
        this.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0xcc010c11);
        this.setBackgroundDrawable(dw);
        // 单击弹出窗以外处 关闭弹出窗
        mMenuView.setOnTouchListener(new OnTouchListener() {

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