package com.afar.osaio.smart.setting.component;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.afar.osaio.smart.device.helper.DeviceSettingHelper;
import com.afar.osaio.smart.setting.adapter.PresetPointAdapter;
import com.afar.osaio.widget.callback.ItemDragCallback;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.entity.PresetPointConfigure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PresetPointSortComponent {

    private RecyclerView rvDevice;

    private PresetPointAdapter mPresetPointAdapter;
    private ItemTouchHelper mPresetPointItemTouchHelper;
    private PresetPointSortComponentListener mListener;
    private List<PresetPointConfigure> mOriginalData = new ArrayList<>();

    public void setupItemTouchHelper() {
        mPresetPointItemDragCallback.setLongPressDragEnable(true);
        mPresetPointItemTouchHelper = new ItemTouchHelper(mPresetPointItemDragCallback);
        mPresetPointItemTouchHelper.attachToRecyclerView(rvDevice);
    }

    ItemDragCallback mPresetPointItemDragCallback = new ItemDragCallback() {

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags;
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if(layoutManager instanceof GridLayoutManager){
                //dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END;
                dragFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            }else{
                dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            }
            int swipeFlags = 0;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public void onItemMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            if (viewHolder == null || target == null || !isMoveEnable(viewHolder.getAdapterPosition(), target.getAdapterPosition())) {
                return;
            }
            if (mPresetPointAdapter != null) {
                log("before Adapter data", mPresetPointAdapter.getData());
                mPresetPointAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                Collections.swap(mPresetPointAdapter.getData(), viewHolder.getAdapterPosition(), target.getAdapterPosition());
                log("after Adapter data", mPresetPointAdapter.getData());
            }
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            NooieLog.d("-->> debug PresetPointSortComponent onSelectedChanged: actionState" + actionState);
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && mPresetPointAdapter != null) {
                cloneOriginalData(mPresetPointAdapter.getData());
            }
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            NooieLog.d("-->> debug PresetPointSortComponent clearView: ");
            if (mPresetPointAdapter != null && checkPresetPointDataChange(mOriginalData, mPresetPointAdapter.getData())) {
                List<PresetPointConfigure> sortPresetPointConfigureList = DeviceSettingHelper.sortPresetPointConfigureList(mPresetPointAdapter.getData());
                log("sort data", sortPresetPointConfigureList);
                if (CollectionUtil.isNotEmpty(sortPresetPointConfigureList)) {
                    mPresetPointAdapter.setData(sortPresetPointConfigureList);
                    if (mListener != null) {
                        mListener.onDataChange(sortPresetPointConfigureList);
                    }
                }
            }
            clearOriginalData();
        }
    };

    public void startDrag(RecyclerView.ViewHolder holder) {
        if (mPresetPointItemTouchHelper != null) {
            mPresetPointItemTouchHelper.startDrag(holder);
        }
    }

    public void setRv(RecyclerView rvDevice) {
        this.rvDevice = rvDevice;
    }

    public void setAdapter(PresetPointAdapter adapter) {
        this.mPresetPointAdapter = adapter;
    }

    public void setListener(PresetPointSortComponentListener listener) {
        mListener = listener;
    }

    public void release() {
        setListener(null);
        clearOriginalData();
    }

    private boolean isMoveEnable(int position, int targetPosition) {
        if (position < 0 || targetPosition < 0 || mPresetPointAdapter == null || mPresetPointAdapter.getData() == null) {
            return false;
        }
        return position < mPresetPointAdapter.getData().size() && targetPosition < mPresetPointAdapter.getData().size();
    }

    private void cloneOriginalData(List<PresetPointConfigure> presetPointConfigures) {
        if (mOriginalData == null) {
            mOriginalData = new ArrayList<>();
        }
        mOriginalData.clear();
        if (CollectionUtil.isNotEmpty(presetPointConfigures)) {
            mOriginalData.addAll(presetPointConfigures);
        }
    }

    private void clearOriginalData() {
        if (mOriginalData != null) {
            mOriginalData.clear();
        }
    }

    private boolean checkPresetPointDataChange(List<PresetPointConfigure> originalData, List<PresetPointConfigure> currentData) {
        if (CollectionUtil.isEmpty(originalData) || CollectionUtil.isEmpty(currentData)) {
            return false;
        }
        if (CollectionUtil.size(originalData) != CollectionUtil.size(currentData)) {
            return true;
        }
        boolean isDataChanged = false;
        for (int i = 0; i < CollectionUtil.size(originalData); i++) {
            isDataChanged = originalData.get(i) != null && currentData.get(i) != null && originalData.get(i).getPosition() != currentData.get(i).getPosition();
            if (isDataChanged) {
                break;
            }
        }
        return isDataChanged;
    }

    private void log(String tag, List<PresetPointConfigure> presetPointConfigures) {
        for (int i = 0; i < CollectionUtil.size(presetPointConfigures); i++) {
            PresetPointConfigure configure = presetPointConfigures.get(i);
            NooieLog.d("-->> debug PresetPointSortComponent log: " + tag + " name=" + configure.getName() + " position=" + configure.getPosition() + " id=" + configure.getId());
        }
    }

    public interface PresetPointSortComponentListener {

        void onDataChange(List<PresetPointConfigure> presetPointConfigures);
    }
}
