package com.afar.osaio.smart.electrician.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.electrician.eventbus.StyleEvent;
import com.afar.osaio.smart.electrician.smartScene.adapter.StyleAdapter;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class StylePopupWindows extends PopupWindow {
    private View mMenuView;
    private Context context;
    private String displayColor;

    private StyleAdapter styleAdapter;

    public StylePopupWindows(Activity context, String color) {
        super(context);
        this.context = context;
        this.displayColor = color;
        init();
    }

    private void init() {
        // PopupWindow 导入
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.layout_style, null);
        RecyclerView rcvStyle = mMenuView.findViewById(R.id.rcvStyle);
        List<String> styleList = new ArrayList<>();
        styleList.add(ConstantValue.SMART_SCENE_COLOR_ONE);
        styleList.add(ConstantValue.SMART_SCENE_COLOR_TWO);
        styleList.add(ConstantValue.SMART_SCENE_COLOR_THREE);
        styleList.add(ConstantValue.SMART_SCENE_COLOR_FOUR);
        styleList.add(ConstantValue.SMART_SCENE_COLOR_FIVE);
        styleList.add(ConstantValue.SMART_SCENE_COLOR_SIX);
        styleList.add(ConstantValue.SMART_SCENE_COLOR_SEVEN);
        styleList.add(ConstantValue.SMART_SCENE_COLOR_EIGHT);
        styleList.add(ConstantValue.SMART_SCENE_COLOR_NINE);
        styleList.add(ConstantValue.SMART_SCENE_COLOR_TEN);
        styleList.add(ConstantValue.SMART_SCENE_COLOR_ELEVEN);
        styleList.add(ConstantValue.SMART_SCENE_COLOR_TWELVE);
        styleAdapter = new StyleAdapter(styleList);
        styleAdapter.setListener(new StyleAdapter.StyleItemListener() {
            @Override
            public void onItemClick(int position, String color) {
                styleAdapter.changeSelected(position);
                EventBus.getDefault().post(new StyleEvent(color));
                NooieLog.e("----------color  " + color);
            }
        });

        GridLayoutManager layoutManager = new GridLayoutManager(NooieApplication.mCtx, 4);
        rcvStyle.setLayoutManager(layoutManager);
        rcvStyle.setAdapter(styleAdapter);

        NooieLog.e("---------displayColor " + displayColor);
        for (int i = 0; i < styleList.size(); i++) {
            if (displayColor.equals(styleList.get(i))) {
                styleAdapter.changeSelected(i);
            }
        }

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
                int height = mMenuView.findViewById(R.id.rcvStyle).getTop();
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
