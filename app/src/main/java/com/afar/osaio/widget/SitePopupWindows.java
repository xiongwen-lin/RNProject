package com.afar.osaio.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.widget.adapter.SitePopupAdapter;

import java.util.ArrayList;
import java.util.List;

public class SitePopupWindows extends PopupWindow {
    private View mMenuView;
    private Activity context;
    private RecyclerView mRecyclerView;
    private SitePopupAdapter mAdapter;
    private List<String> regionList;

    public SitePopupWindows(Activity context) {
        super(context);
        this.context = context;
        init();
    }

    private void init() {
        // PopupWindow 导入
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        mMenuView = inflater.inflate(R.layout.layout_site_pop, null);
        mRecyclerView = (RecyclerView) mMenuView.findViewById(R.id.rcvRegion);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        setupRegion();
        mAdapter = new SitePopupAdapter();
        mAdapter.setRegions(regionList);
        mRecyclerView.setAdapter(mAdapter);
        // 导入布局
        this.setContentView(mMenuView);
        // 设置动画效果
        this.setAnimationStyle(R.style.TRM_ANIM_STYLE);
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

    private void setupRegion() {
        regionList = new ArrayList<>();
        regionList.add("US");
        regionList.add("DE");
        regionList.add("IT");
        regionList.add("UK");
        regionList.add("JP");
        regionList.add("FR");
        regionList.add("ES");
        regionList.add("CA");
    }

    public void setListener(SiteListener listener) {
        mAdapter.setListener(listener);
    }

    public interface SiteListener {
        void onSiteItemClick(int position, String region);
    }

}
