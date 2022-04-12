package com.afar.osaio.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.afar.osaio.R;
import com.afar.osaio.widget.bean.TabItemBean;
import com.afar.osaio.widget.adapter.TabFlowNormalAdapter;
import com.afar.osaio.widget.listener.TabFlowNormalListener;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.zhengsr.tablib.view.flow.TabFlowLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2021/11/30 2:15 下午
 * 说明:
 *
 * 备注:
 *
 ***********************************************************/
public class TabFlowNormalView extends LinearLayout {

    @BindView(R.id.tflTabFlowNormal)
    TabFlowLayout tflTabFlowNormal;
    @BindView(R.id.vTabFlowNormalDivider)
    View vTabFlowNormalDivider;
    private TabFlowNormalAdapter mTabAdapter = null;
    private List<TabItemBean> mTabList = new ArrayList<>();

    public TabFlowNormalView(Context context) {
        super(context);
        init();
    }

    public TabFlowNormalView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setTabs(List<TabItemBean> tabs) {
        if (mTabList == null) {
            mTabList = new ArrayList<>();
        }
        mTabList.clear();
        mTabList.addAll(CollectionUtil.safeFor(tabs));
        sortTabList(mTabList);
        if (mTabAdapter != null) {
            mTabAdapter.notifyDataChanged();
        }
    }

    public void addTabs(List<TabItemBean> tabs) {
        if (mTabList == null) {
            mTabList = new ArrayList<>();
        }
        mTabList.addAll(CollectionUtil.safeFor(tabs));
        sortTabList(mTabList);
        if (mTabAdapter != null) {
            mTabAdapter.notifyDataChanged();
        }
    }

    public void updateTabs(List<TabItemBean> tabs) {
        if (mTabList == null) {
            mTabList = new ArrayList<>();
        }
        if (CollectionUtil.isEmpty(tabs)) {
            return;
        }
        List<TabItemBean> tmpTabs = new ArrayList<>();
        tmpTabs.addAll(tabs);
        List<String> tabTags = new ArrayList<>();
        for (TabItemBean tabItemBean : CollectionUtil.safeFor(mTabList)) {
            if (tabItemBean != null && !TextUtils.isEmpty(tabItemBean.tag)) {
                tabTags.add(tabItemBean.tag);
            }
        }
        Iterator<TabItemBean> tabItemBeanIterator = tmpTabs.iterator();
        TabItemBean tmpItem = null;
        while (tabItemBeanIterator.hasNext()) {
            tmpItem = tabItemBeanIterator.next();
            if (tmpItem != null && tabTags.contains(tmpItem.tag)) {
                tabItemBeanIterator.remove();
            }
        }
        mTabList.addAll(CollectionUtil.safeFor(tmpTabs));
        sortTabList(mTabList);
        if (mTabAdapter != null) {
            mTabAdapter.notifyDataChanged();
        }
    }

    public void setListener(TabFlowNormalListener listener) {
        if (mTabAdapter != null) {
            mTabAdapter.setListener(listener);
        }
    }

    public void setDividerVisible(int visibility) {
        if (vTabFlowNormalDivider != null) {
            vTabFlowNormalDivider.setVisibility(visibility);
        }
    }

    public void updateTabAdapterCurrentSelectionIndex(TabItemBean currentTab, List<TabItemBean> tabs) {
        if (currentTab == null || CollectionUtil.isEmpty(tabs) || mTabAdapter == null) {
            return;
        }
        try {
            int currentSelectionIndex = -1;
            int tabsSize = CollectionUtil.size(tabs);
            TabItemBean tabItemBean = null;
            for (int i = 0; i < tabsSize; i++) {
                tabItemBean = tabs.get(i);
                if (tabItemBean != null && !TextUtils.isEmpty(tabItemBean.tag) && tabItemBean.tag.equalsIgnoreCase(currentTab.tag)) {
                    currentSelectionIndex = i;
                }
            }
            if (currentSelectionIndex >= 0) {
                mTabAdapter.setCurrentSelectionIndex(currentSelectionIndex);
            }
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
    }

    public void updateItemSelect(TabItemBean currentTab, List<TabItemBean> tabs) {
        if (currentTab == null || CollectionUtil.isEmpty(tabs) || tflTabFlowNormal == null) {
            return;
        }
        try {
            int currentSelectionIndex = -1;
            int tabsSize = CollectionUtil.size(tabs);
            TabItemBean tabItemBean = null;
            for (int i = 0; i < tabsSize; i++) {
                tabItemBean = tabs.get(i);
                if (tabItemBean != null && !TextUtils.isEmpty(tabItemBean.tag) && tabItemBean.tag.equalsIgnoreCase(currentTab.tag)) {
                    currentSelectionIndex = i;
                }
            }
            if (currentSelectionIndex >= 0) {
                tflTabFlowNormal.setItemClickByOutside(currentSelectionIndex);
            }
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
    }

    private void init() {
        View lvpView = LayoutInflater.from(getContext()).inflate(R.layout.layout_tab_flow_normal, this, false);
        addView(lvpView);
        bindView(lvpView);
        setupTabLayoutView();
    }

    private void bindView(View view) {
        ButterKnife.bind(this, view);
    }

    private void setupTabLayoutView() {
        mTabAdapter = new TabFlowNormalAdapter(R.layout.item_tab_flow_normal, mTabList);
        tflTabFlowNormal.setAdapter(mTabAdapter);
    }

    private List<TabItemBean> sortTabList(List<TabItemBean> itemBeans) {
        if (CollectionUtil.isEmpty(itemBeans)) {
            return itemBeans;
        }
        Collections.sort(itemBeans, new Comparator<TabItemBean>() {
            @Override
            public int compare(TabItemBean item1, TabItemBean item2) {
                int sort1 = item1 != null ? item1.sort : 0;
                int sort2 = item2 != null ? item2.sort : 0;
                return sort1 > sort2 ? 1 : -1;
            }
        });
        return itemBeans;
    }

}
