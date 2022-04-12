package com.afar.osaio.smart.device.Component;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.View;

import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.afar.osaio.smart.home.adapter.DevicesAdapter;
import com.afar.osaio.smart.device.bean.ListDeviceItem;
import com.afar.osaio.widget.callback.ItemDragCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceSortComponent {

    private RecyclerView rvDevice;

    private DevicesAdapter mDevicesAdapter;
    private ItemTouchHelper mDeviceItemTouchHelper;
    private Map<String, Integer> mOriginDevicesSortMap = new HashMap<>();
    private Map<String, String> mBindIdAndDeviceIdMap = new HashMap<>();
    private List<ListDeviceItem> mSortDeviceList = new ArrayList<>();

    public void setupItemTouchHelper() {
        mDeviceItemDragCallback.setLongPressDragEnable(true);
        mDeviceItemTouchHelper = new ItemTouchHelper(mDeviceItemDragCallback);
        mDeviceItemTouchHelper.attachToRecyclerView(rvDevice);
    }

    ItemDragCallback mDeviceItemDragCallback = new ItemDragCallback() {
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
        if (mDeviceItemTouchHelper != null) {
            mDeviceItemTouchHelper.startDrag(holder);
        }
    }

    public void setDragEnable(boolean enable) {
        if (mDeviceItemDragCallback != null) {
            mDeviceItemDragCallback.setLongPressDragEnable(enable);
        }
    }

    public void setDeviceRv(RecyclerView rvDevice) {
        this.rvDevice = rvDevice;
    }

    public void setDeviceAdapter(DevicesAdapter deviceAdapter) {
        this.mDevicesAdapter = deviceAdapter;
    }

    public void cloneDeviceList() {
        if (mSortDeviceList != null) {
            mSortDeviceList.clear();
        } else {
            mSortDeviceList = new ArrayList<>();
        }
        if (mDevicesAdapter != null && CollectionUtil.isNotEmpty(mDevicesAdapter.getData())) {
            mSortDeviceList.addAll(mDevicesAdapter.getData());
            for (int i = 0; i < mDevicesAdapter.getData().size(); i++) {
                ListDeviceItem deviceItem = mDevicesAdapter.getData().get(i);
                if (deviceItem != null && deviceItem.getBindDevice() != null) {
                    mBindIdAndDeviceIdMap.put(String.valueOf(deviceItem.getBindDevice().getId()), deviceItem.getDeviceId());
                    mOriginDevicesSortMap.put(String.valueOf(deviceItem.getBindDevice().getId()), deviceItem.getBindDevice().getSort());
                    //按顺序重排
                    //deviceItem.getBindDevice().setSort(i+1);
                    //mSortDeviceList.add(deviceItem);
                }
            }
        }
    }

    public void clearDeviceList() {
        if (mSortDeviceList != null) {
            mSortDeviceList.clear();
        }

        if (mOriginDevicesSortMap != null) {
            mOriginDevicesSortMap.clear();
        }

        if(mBindIdAndDeviceIdMap != null) {
            mBindIdAndDeviceIdMap.clear();
        }
    }

    public Map<String, Integer> getUpdateSortDevices() {
        return filterUpdateSortDevice(mOriginDevicesSortMap, getCurrentDeviceSort());
    }

    public Map<String, Integer> getCurrentDeviceSort() {
        Map<String, Integer> currentDeviceSort = new HashMap<>();
        for (int i = 0; i < CollectionUtil.size(mSortDeviceList); i++) {
            if (mSortDeviceList.get(i) != null && mSortDeviceList.get(i).getBindDevice() != null) {
                currentDeviceSort.put(String.valueOf(mSortDeviceList.get(i).getBindDevice().getId()), i + 1);
            }
        }
        return currentDeviceSort;
    }

    public Map<String, String> getDeviceIdAndIdMap() {
        return new HashMap<>(mBindIdAndDeviceIdMap);
    }

    private Map<String, Integer> filterUpdateSortDevice(Map<String, Integer> source, Map<String, Integer> target) {
        Map<String, Integer> result = new HashMap<>(16);
        if (target == null || target.isEmpty()) {
            return result;
        }

        if (source == null || source.isEmpty()) {
            result.putAll(target);
            return result;
        }

        for (Map.Entry<String, Integer> item : target.entrySet()) {
            boolean isNoNeedUpdateSort = item != null && source.containsKey(item.getKey()) && source.get(item.getKey()).equals(item.getValue());
            if (!isNoNeedUpdateSort) {
                result.put(item.getKey(), item.getValue());
            }
        }

        return result;
    }

    private void dealOnItemMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        NooieLog.d("-->> DeviceSortComponent dealOnItemMove 1001");
        boolean isItemCanMoved = mDevicesAdapter != null && CollectionUtil.isNotEmpty(mDevicesAdapter.getData()) && CollectionUtil.isNotEmpty(mSortDeviceList)
                && viewHolder != null && target != null
                && CollectionUtil.isIndexSafe(viewHolder.getAdapterPosition(), CollectionUtil.size(mDevicesAdapter.getData())) && CollectionUtil.isIndexSafe(target.getAdapterPosition(), CollectionUtil.size(mDevicesAdapter.getData()))
                && CollectionUtil.isIndexSafe(viewHolder.getAdapterPosition(), CollectionUtil.size(mSortDeviceList)) && CollectionUtil.isIndexSafe(target.getAdapterPosition(), CollectionUtil.size(mSortDeviceList));
        if (isItemCanMoved) {
            NooieLog.d("-->> DeviceSortComponent dealOnItemMove 1001 vposition=" + viewHolder.getAdapterPosition() + " tposition=" + target.getAdapterPosition());
            //log1(mDevicesAdapter.getData());
            mDevicesAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            log();
            Collections.swap(mDevicesAdapter.getData(), viewHolder.getAdapterPosition(), target.getAdapterPosition());
            Collections.swap(mSortDeviceList, viewHolder.getAdapterPosition(), target.getAdapterPosition());
            log();
            NooieLog.d("-->> DeviceSortComponent dealOnItemMove 1001 vposition=" + viewHolder.getAdapterPosition() + " tposition=" + target.getAdapterPosition());
        }
    }

    private void dealOnSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        NooieLog.d("-->> DeviceSortComponent dealOnSelectedChanged 1001");
        if (viewHolder instanceof DevicesAdapter.DeviceDragViewHolder) {
            DevicesAdapter.DeviceDragViewHolder deviceDragViewHolder = (DevicesAdapter.DeviceDragViewHolder) viewHolder;
            if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            } else if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                deviceDragViewHolder.vDeviceShadowBottom.setVisibility(View.VISIBLE);
            }
        }
    }

    private void dealClearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        NooieLog.d("-->> DeviceSortComponent dealClearView 1001");
        if (viewHolder instanceof DevicesAdapter.DeviceDragViewHolder) {
            DevicesAdapter.DeviceDragViewHolder deviceDragViewHolder = (DevicesAdapter.DeviceDragViewHolder) viewHolder;
            deviceDragViewHolder.vDeviceShadowBottom.setVisibility(View.GONE);
        }
        if (mDevicesAdapter != null) {
            mDevicesAdapter.notifyDataSetChanged();
        }
    }

    private void log() {
        for (int i = 0; i < mSortDeviceList.size(); i++) {
            ListDeviceItem deviceItem = mSortDeviceList.get(i);
            NooieLog.d("-->> DeviceSortComponent log device id=" + deviceItem.getDeviceId() + " sort=" + (i + 1));
        }
    }

    private void log1(List<ListDeviceItem> deviceItems) {
        for (int i = 0; i < deviceItems.size(); i++) {
            ListDeviceItem deviceItem = deviceItems.get(i);
            NooieLog.d("-->> DeviceSortComponent log1 device id=" + deviceItem.getDeviceId() + " sort=" + (i + 1));
        }
    }
}
