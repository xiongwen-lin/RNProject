package com.afar.osaio.widget;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.afar.osaio.R;
import com.afar.osaio.widget.adapter.RelativePopupMenuAdapter;
import com.afar.osaio.widget.bean.RelativePopMenuItem;
import com.nooie.common.utils.collection.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

public class RelativePopupMenu {

    public static final int MENU_FIRST = 1;
    public static final int MENU_SECOND = 2;
    public static final int MENU_THIRD = 3;
    public static final int MENU_FOURTH = 4;

    private Activity mContext;
    private PopupWindow mPopupWindow;
    private RecyclerView mRecyclerView;
    private ImageView ivTriangleUp;
    private View content;

    private RelativePopupMenuAdapter mAdapter;
    private List<RelativePopMenuItem> menuItemList;

    private static final int DEFAULT_HEIGHT = 480;
    private int popHeight = DEFAULT_HEIGHT;
    private int popWidth = RecyclerView.LayoutParams.WRAP_CONTENT;
    private boolean showIcon = true;
    private boolean dimBackground = true;
    private boolean needAnimationStyle = true;
    private boolean mShowTriangleUp = false;

    private static final int DEFAULT_ANIM_STYLE = R.style.TRM_ANIM_STYLE;
    private int animationStyle;

    private float alpha = 0.75f;

    public RelativePopupMenu(Activity context) {
        this.mContext = context;
        init();
    }

    private void init() {
        content = LayoutInflater.from(mContext).inflate(R.layout.menu_home_page, null);
        mRecyclerView = (RecyclerView) content.findViewById(R.id.rcvHomePageMenu);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        ivTriangleUp = content.findViewById(R.id.ivTriangleUp);
        menuItemList = new ArrayList<>();
        mAdapter = new RelativePopupMenuAdapter();
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

    public RelativePopupMenu setHeight(int height) {
        if (height <= 0 && height != RecyclerView.LayoutParams.MATCH_PARENT
                && height != RecyclerView.LayoutParams.WRAP_CONTENT) {
            this.popHeight = DEFAULT_HEIGHT;
        } else {
            this.popHeight = height;
        }
        return this;
    }

    public RelativePopupMenu setWidth(int width) {
        if (width <= 0 && width != RecyclerView.LayoutParams.MATCH_PARENT) {
            this.popWidth = RecyclerView.LayoutParams.WRAP_CONTENT;
        } else {
            this.popWidth = width;
        }
        return this;
    }

    /**
     * 是否显示菜单图标
     *
     * @param show
     * @return
     */
    public RelativePopupMenu showIcon(boolean show) {
        this.showIcon = show;
        return this;
    }

    /**
     * 是否显示popupwindow顶部小三角
     *
     * @param showTriangleUp
     * @return
     */
    public RelativePopupMenu showTriangle(boolean showTriangleUp) {
        this.mShowTriangleUp = showTriangleUp;
        refreshTriangleUpView();
        return this;
    }

    private void refreshTriangleUpView() {
        ivTriangleUp.setVisibility(mShowTriangleUp ? View.VISIBLE : View.GONE);
    }

    /**
     * 添加单个菜单
     *
     * @param item
     * @return
     */
    public RelativePopupMenu addMenuItem(RelativePopMenuItem item) {
        if (item == null) {
            return this;
        }
        if (menuItemList == null) {
            menuItemList = new ArrayList<>();
        }
        menuItemList.add(item);
        return this;
    }

    /**
     * 添加多个菜单
     *
     * @param list
     * @return
     */
    public RelativePopupMenu addMenuList(List<RelativePopMenuItem> list) {
        if (CollectionUtil.isEmpty(list)) {
            return this;
        }
        if (menuItemList == null) {
            menuItemList = new ArrayList<>();
        }
        menuItemList.clear();
        menuItemList.addAll(list);
        return this;
    }

    public RelativePopupMenu setMenuList(List<RelativePopMenuItem> list) {
        if (menuItemList == null) {
            menuItemList = new ArrayList<>();
        }
        menuItemList.clear();
        menuItemList.addAll(CollectionUtil.safeFor(list));
        return this;
    }

    /**
     * 是否让背景变暗
     *
     * @param b
     * @return
     */
    public RelativePopupMenu dimBackground(boolean b) {
        this.dimBackground = b;
        return this;
    }

    /**
     * 否是需要动画
     *
     * @param need
     * @return
     */
    public RelativePopupMenu needAnimationStyle(boolean need) {
        this.needAnimationStyle = need;
        return this;
    }

    /**
     * 设置动画
     *
     * @param style
     * @return
     */
    public RelativePopupMenu setAnimationStyle(int style) {
        this.animationStyle = style;
        return this;
    }

    public RelativePopupMenu setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        mAdapter.setListener(listener);
        return this;
    }

    public RelativePopupMenu showAsDropDown(View anchor) {
        showAsDropDown(anchor, 0, 0);
        return this;
    }

    public RelativePopupMenu showAsDropDown(View anchor, int xoff, int yoff) {
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

    public void refreshMenuView(List<RelativePopMenuItem> menuItems) {
        setMenuList(menuItems);
        if (mAdapter != null) {
            mAdapter.setItems(menuItems);
        }
    }

    public void refreshMenuView() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
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
