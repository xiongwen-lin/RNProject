package com.afar.osaio.smart.electrician.widget;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.smart.electrician.bean.ConnectModePopMenuItem;

import java.util.ArrayList;
import java.util.List;

public class ConnectModePopupWindows {

    public static final int MENU_FIRST = 1;
    public static final int MENU_SECOND = 2;
    public static final int MENU_THIRD = 3;
    public static final int MENU_FOURTH = 4;

    private Activity mContext;
    private PopupWindow mPopupWindow;
    private RecyclerView mRecyclerView;
    private View content;

    private ConnectModePopupMenuAdapter mAdapter;
    private List<ConnectModePopMenuItem> menuItemList;

    private static final int DEFAULT_HEIGHT = 480;
    private int popHeight = DEFAULT_HEIGHT;
    private int popWidth = RecyclerView.LayoutParams.WRAP_CONTENT;
    private boolean dimBackground = true;
    private boolean needAnimationStyle = true;

    private static final int DEFAULT_ANIM_STYLE = R.style.TRM_ANIM_STYLE;
    private int animationStyle;

    private float alpha = 0.75f;

    public ConnectModePopupWindows(Activity context) {
        this.mContext = context;
        init();
    }

    private void init() {
        content = LayoutInflater.from(mContext).inflate(R.layout.pop_home_page, null);
        mRecyclerView = (RecyclerView) content.findViewById(R.id.rcvHomePageMenu);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        menuItemList = new ArrayList<>();
        mAdapter = new ConnectModePopupMenuAdapter();
    }

    private PopupWindow getPopupWindow() {
        mPopupWindow = new PopupWindow(mContext);
        mPopupWindow.setContentView(content);
        mPopupWindow.setHeight(popHeight);
        mPopupWindow.setWidth(popWidth);
        if (needAnimationStyle) {
            mPopupWindow.setAnimationStyle(animationStyle <= 0 ? DEFAULT_ANIM_STYLE : animationStyle);
        }

        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable());
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (dimBackground) {
                    setBackgroundAlpha(alpha, 1f, 300);
                }
            }
        });

        mAdapter.setItems(menuItemList);
        mRecyclerView.setAdapter(mAdapter);
        return mPopupWindow;
    }

    public ConnectModePopupWindows setHeight(int height) {
        if (height <= 0 && height != RecyclerView.LayoutParams.MATCH_PARENT
                && height != RecyclerView.LayoutParams.WRAP_CONTENT) {
            this.popHeight = DEFAULT_HEIGHT;
        } else {
            this.popHeight = height;
        }
        return this;
    }

    public ConnectModePopupWindows setWidth(int width) {
        if (width <= 0 && width != RecyclerView.LayoutParams.MATCH_PARENT) {
            this.popWidth = RecyclerView.LayoutParams.WRAP_CONTENT;
        } else {
            this.popWidth = width;
        }
        return this;
    }

    /**
     * 添加单个菜单
     *
     * @param item
     * @return
     */
    public ConnectModePopupWindows addMenuItem(ConnectModePopMenuItem item) {
        menuItemList.add(item);
        return this;
    }

    /**
     * 添加多个菜单
     *
     * @param list
     * @return
     */
    public ConnectModePopupWindows addMenuList(List<ConnectModePopMenuItem> list) {
        menuItemList.addAll(list);
        return this;
    }

    /**
     * 是否让背景变暗
     *
     * @param b
     * @return
     */
    public ConnectModePopupWindows dimBackground(boolean b) {
        this.dimBackground = b;
        return this;
    }

    /**
     * 否是需要动画
     *
     * @param need
     * @return
     */
    public ConnectModePopupWindows needAnimationStyle(boolean need) {
        this.needAnimationStyle = need;
        return this;
    }

    /**
     * 设置动画
     *
     * @param style
     * @return
     */
    public ConnectModePopupWindows setAnimationStyle(int style) {
        this.animationStyle = style;
        return this;
    }

    public ConnectModePopupWindows setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        mAdapter.setListener(listener);
        return this;
    }

    public ConnectModePopupWindows showAsDropDown(View anchor) {
        showAsDropDown(anchor, 0, 0);
        return this;
    }

    public ConnectModePopupWindows showAsDropDown(View anchor, int xoff, int yoff) {
        if (mPopupWindow == null) {
            getPopupWindow();
        }
        if (!mPopupWindow.isShowing()) {
            mPopupWindow.showAsDropDown(anchor, xoff, yoff);
            if (dimBackground) {
                setBackgroundAlpha(1f, alpha, 240);
            }
        }
        return this;
    }

    private void setBackgroundAlpha(float from, float to, int duration) {
        final WindowManager.LayoutParams lp = mContext.getWindow().getAttributes();
        ValueAnimator animator = ValueAnimator.ofFloat(from, to);
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                lp.alpha = (float) animation.getAnimatedValue();
                mContext.getWindow().setAttributes(lp);
            }
        });
        animator.start();
    }

    public void dismiss() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    public interface OnMenuItemClickListener {
        void onMenuItemClick(int position);
    }
}
