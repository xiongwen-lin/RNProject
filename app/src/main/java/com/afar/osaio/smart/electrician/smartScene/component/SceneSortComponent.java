package com.afar.osaio.smart.electrician.smartScene.component;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.smart.electrician.smartScene.adapter.SortAdapter;
import com.afar.osaio.widget.callback.ItemDragCallback;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SceneSortComponent {

    private RecyclerView rvDevice;

    private SortAdapter mSortAdapter;
    private ItemTouchHelper mDeviceItemTouchHelper;
    private List<SceneBean> mSortSceneList = new ArrayList<>();

    public void setupItemTouchHelper() {
        mDeviceItemTouchHelper = new ItemTouchHelper(mDeviceItemDragCallback);
        mDeviceItemTouchHelper.attachToRecyclerView(rvDevice);
    }

    ItemDragCallback mDeviceItemDragCallback = new ItemDragCallback() {
        @Override
        public void onItemMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            if (mSortAdapter != null) {
                mSortAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                log();
                Collections.swap(mSortSceneList, viewHolder.getAdapterPosition(), target.getAdapterPosition());
                log();
            }
        }
    };

    public void startDrag(RecyclerView.ViewHolder holder) {
        if (mDeviceItemTouchHelper != null) {
            mDeviceItemTouchHelper.startDrag(holder);
        }
    }

    public void setSortList() {
        if (mSortAdapter != null && CollectionUtil.isNotEmpty(mSortAdapter.getData())) {
            mSortSceneList.clear();
            mSortSceneList.addAll(mSortAdapter.getData());
        }
    }

    public void setDeviceRv(RecyclerView rvDevice) {
        this.rvDevice = rvDevice;
    }

    public void setDeviceAdapter(SortAdapter sortAdapter) {
        this.mSortAdapter = sortAdapter;
    }

    private void log() {
        for (int i = 0; i < mSortSceneList.size(); i++) {
            SceneBean sceneBean = mSortSceneList.get(i);
            NooieLog.d("-->> SceneSortComponent log scene id=" + sceneBean.getId() + " sort=" + (i + 1));
        }
    }

    public List<SceneBean> getSceneBeanList() {
        return mSortSceneList;
    }
}
