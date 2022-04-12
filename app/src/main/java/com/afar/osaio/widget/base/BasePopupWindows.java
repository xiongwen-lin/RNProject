package com.afar.osaio.widget.base;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

import com.afar.osaio.R;

/**
 * BasePopupWindows
 *
 * @author Administrator
 * @date 2019/3/11
 */
abstract public class BasePopupWindows extends PopupWindow {
    private View mContentView;
    private Context context;

    public BasePopupWindows(Activity context) {
        super(context);
        this.context = context;
        init();
    }

    private void init() {
        // PopupWindow 导入
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContentView = inflater.inflate(getLayoutId(), null);

        // 导入布局
        this.setContentView(mContentView);
        // 设置动画效果
        this.setAnimationStyle(R.style.from_bottom_anim);
        this.setWidth(LayoutParams.FILL_PARENT);
        this.setHeight(LayoutParams.WRAP_CONTENT);
        // 设置可触
        this.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0x0000000);
        this.setBackgroundDrawable(dw);
        // 单击弹出窗以外处 关闭弹出窗
        mContentView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                int height = mContentView.findViewById(R.id.containerBtn).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });

        bindView(mContentView);
    }

    abstract public int getLayoutId();

    abstract public void bindView(View view);
}