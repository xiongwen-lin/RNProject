package com.afar.osaio.smart.device.Component;

import android.text.TextUtils;
import android.util.ArrayMap;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.smart.home.adapter.BleApDeviceAdapter;
import com.afar.osaio.widget.callback.ItemDragCallback;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.db.entity.BleApDeviceEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BleApDeviceSortComponent {

    private RecyclerView rvDevice;

    private BleApDeviceAdapter mAdapter;
    private ItemTouchHelper mPresetPointItemTouchHelper;
    private BleApDeviceSortComponentListener mListener;
    private List<BleApDeviceEntity> mOriginalData = new ArrayList<>();

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
            dealOnItemMove(recyclerView, viewHolder, target);
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            dealOnSelectedChanged(viewHolder, actionState);
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            dealClearView(recyclerView, viewHolder);
        }
    };

    public void startDrag(RecyclerView.ViewHolder holder) {
        if (mPresetPointItemTouchHelper != null) {
            mPresetPointItemTouchHelper.startDrag(holder);
        }
    }

    public void setDragEnable(boolean enable) {
        if (mPresetPointItemDragCallback != null) {
            mPresetPointItemDragCallback.setLongPressDragEnable(enable);
        }
    }

    public void setRv(RecyclerView rvDevice) {
        this.rvDevice = rvDevice;
    }

    public void setAdapter(BleApDeviceAdapter adapter) {
        this.mAdapter = adapter;
    }

    public void setListener(BleApDeviceSortComponentListener listener) {
        mListener = listener;
    }

    public void release() {
        setListener(null);
        clearOriginalData();
    }

    private boolean isMoveEnable(int position, int targetPosition) {
        if (position < 0 || targetPosition < 0 || mAdapter == null || CollectionUtil.isEmpty(mAdapter.getData())) {
            return false;
        }
        return CollectionUtil.isIndexSafe(position, mAdapter.getData().size()) && CollectionUtil.isIndexSafe(position, mAdapter.getData().size());
    }

    private void cloneOriginalData(List<BleApDeviceEntity> datas) {
        if (mOriginalData == null) {
            mOriginalData = new ArrayList<>();
        }
        mOriginalData.clear();
        if (CollectionUtil.isNotEmpty(datas)) {
            mOriginalData.addAll(datas);
        }
    }

    private void clearOriginalData() {
        if (mOriginalData != null) {
            mOriginalData.clear();
        }
    }

    private boolean checkPresetPointDataChange(List<BleApDeviceEntity> originalData, List<BleApDeviceEntity> currentData) {
        if (CollectionUtil.isEmpty(originalData) || CollectionUtil.isEmpty(currentData)) {
            return false;
        }
        if (CollectionUtil.size(originalData) != CollectionUtil.size(currentData)) {
            return true;
        }
        boolean isDataChanged = false;
        for (int i = 0; i < CollectionUtil.size(originalData); i++) {
            isDataChanged = originalData.get(i) != null && currentData.get(i) != null && originalData.get(i).getDeviceId() != null &&
                    !originalData.get(i).getDeviceId().equalsIgnoreCase(currentData.get(i).getDeviceId());
            if (isDataChanged) {
                break;
            }
        }
        return isDataChanged;
    }

    private void dealOnItemMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        if (viewHolder == null || target == null || !isMoveEnable(viewHolder.getAdapterPosition(), target.getAdapterPosition())) {
            return;
        }
        if (mAdapter != null) {
            log("before Adapter data", mAdapter.getData());
            mAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            Collections.swap(mAdapter.getData(), viewHolder.getAdapterPosition(), target.getAdapterPosition());
            log("after Adapter data", mAdapter.getData());
            if (mListener != null) {
                mListener.onDataChange(convertCurrentBleApDeviceSortMap(mAdapter.getData()), mAdapter.getData());
            }
        }
    }

    private void dealOnSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        NooieLog.d("-->> debug PresetPointSortComponent onSelectedChanged: actionState" + actionState);
//        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && mAdapter != null) {
//            cloneOriginalData(mAdapter.getData());
//        }
    }

    private void dealClearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        NooieLog.d("-->> debug PresetPointSortComponent clearView: ");
//        if (mAdapter != null && checkPresetPointDataChange(mOriginalData, mAdapter.getData())) {
//        }
//        clearOriginalData();
    }

    private Map<String, Integer> convertCurrentBleApDeviceSortMap(List<BleApDeviceEntity> data) {
        if (CollectionUtil.isEmpty(data)) {
            return null;
        }
        Map<String, Integer> result = new ArrayMap<>();
        for (int i = 0; i < CollectionUtil.size(data); i++) {
            BleApDeviceEntity entity = data.get(i);
            if (entity != null && !TextUtils.isEmpty(entity.getDeviceId())) {
                result.put(entity.getDeviceId(), i + 1);
            }
        }
        return result;
    }

    private void log(String tag, List<BleApDeviceEntity> datas) {
        for (int i = 0; i < CollectionUtil.size(datas); i++) {
//            BleApDeviceEntity data = datas.get(i);
//            NooieLog.d("-->> debug PresetPointSortComponent log: " + tag + " name=" + data.getName() + " position=" + data.getPosition() + " id=" + data.getId());
        }
    }

    public interface BleApDeviceSortComponentListener {

        void onDataChange(Map<String, Integer> sortMap, List<BleApDeviceEntity> dataList);
    }
}
