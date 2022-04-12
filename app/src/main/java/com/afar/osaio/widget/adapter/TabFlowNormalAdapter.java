package com.afar.osaio.widget.adapter;

import android.view.View;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.widget.bean.TabItemBean;
import com.afar.osaio.util.CompatUtil;
import com.afar.osaio.widget.listener.TabFlowNormalListener;
import com.nooie.common.utils.log.NooieLog;
import com.zhengsr.tablib.view.adapter.TabFlowAdapter;

import java.util.List;

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2021/11/30 11:29 上午
 * 说明:
 *
 * 备注:
 *
 ***********************************************************/
public class TabFlowNormalAdapter extends TabFlowAdapter<TabItemBean> {

    private TabFlowNormalListener mListener;
    private int mCurrentSelectionIndex = 0;

    public TabFlowNormalAdapter(int layoutId, List<TabItemBean> data) {
        super(layoutId, data);
    }

    @Override
    public void onItemSelectState(View view, boolean isSelected) {
        super.onItemSelectState(view, isSelected);
        NooieLog.e("--------->TabFlowNormalAdapter isSelected "+isSelected);
        if (isSelected) {
            setTextColor(view, R.id.item_tab_flow_normal_text, CompatUtil.getColor(NooieApplication.mCtx, R.color.theme_green));
        } else {
            setTextColor(view, R.id.item_tab_flow_normal_text, CompatUtil.getColor(NooieApplication.mCtx, R.color.theme_text_color));
        }
    }

    @Override
    public void bindView(View view, TabItemBean data, int position) {
        NooieLog.e("--------->TabFlowNormalAdapter position "+position+" tag "+data.tag);
        setText(view, R.id.item_tab_flow_normal_text, data.title)
                .setTextColor(view, R.id.item_tab_flow_normal_text, CompatUtil.getColor(NooieApplication.mCtx, R.color.theme_text_color));

        /**
        if (position == 0) {
            setVisible(view, R.id.item_tab_flow_normal_msg, true);
        }
         */
        if (position == mCurrentSelectionIndex) {
            setTextColor(view, R.id.item_tab_flow_normal_text, CompatUtil.getColor(NooieApplication.mCtx, R.color.theme_green));
        } else {
            setTextColor(view, R.id.item_tab_flow_normal_text, CompatUtil.getColor(NooieApplication.mCtx, R.color.theme_text_color));
        }
    }

    @Override
    public void onItemClick(View view, TabItemBean data, int position) {
        //super.onItemClick(view, data, position);
        mCurrentSelectionIndex = position;
        if (mListener != null) {
            mListener.onItemClick(view, data, position);
        }
    }

    public void setListener(TabFlowNormalListener listener) {
        mListener = listener;
    }

    public void setCurrentSelectionIndex(int index) {
        mCurrentSelectionIndex = index;
    }

}
