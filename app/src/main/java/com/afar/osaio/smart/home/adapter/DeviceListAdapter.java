package com.afar.osaio.smart.home.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.adapter.BaseAdapter;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.DeviceItem;
import com.afar.osaio.bean.ProductType;
import com.afar.osaio.smart.device.Component.BleApDeviceSortComponent;
import com.afar.osaio.smart.device.Component.DeviceSortComponent;
import com.afar.osaio.smart.device.bean.ListDeviceItem;
import com.afar.osaio.smart.home.adapter.listener.BleApDeviceListener;
import com.afar.osaio.smart.home.adapter.listener.DeviceListListener;
import com.afar.osaio.smart.smartlook.adapter.listener.LockDeviceListener;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.sdk.db.entity.BleApDeviceEntity;
import com.nooie.widget.NEventTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceListAdapter extends BaseAdapter<DeviceItem, DeviceListListener, RecyclerView.ViewHolder> {

    private DevicesAdapter.OnItemClickListener mIpcListener;
    private BleApDeviceListener mBleApIpcListener;
    private LockDeviceListener mLockListener;
    private BleApDeviceSortComponent mBleApDeviceSortComponent = null;
    private DeviceSortComponent mDeviceSortComponent = null;
    private boolean mItemDragEnable = false;
    private Map<String, Integer> mBleApDeviceSortMap = new ArrayMap<>();

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ConstantValue.ADD_CAMERA_TYPE) {
            return new AddCameraViewHolder(createVHView(R.layout.item_add_device, parent));
        } else {
            return new DeviceListVH(createVHView(R.layout.item_device_item, parent));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DeviceItem deviceItem = getDataByPosition(position);
        setupDeviceItemView(holder, deviceItem);
    }

    @Override
    public int getItemCount() {
        return CollectionUtil.isEmpty(mDatas) ? 1 : CollectionUtil.size(mDatas);
    }

    @Override
    public int getItemViewType(int position) {
        if (CollectionUtil.isEmpty(mDatas)) {
            return ConstantValue.ADD_CAMERA_TYPE;
        } else {
            return super.getItemViewType(position);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);

            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return 2;
                }
            });
        }
    }

    private void setupDeviceItemView(RecyclerView.ViewHolder holder, DeviceItem deviceItem) {
        if (holder == null) {
            return;
        }
        if (holder instanceof AddCameraViewHolder) {
            AddCameraViewHolder addCameraVH = (AddCameraViewHolder) holder;
            addCameraVH.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onAddDevice();
                    }
                }
            });
            addCameraVH.btnAddDevice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onAddDevice();
                    }
                }
            });
        } else if (holder instanceof DeviceListVH) {
            if (deviceItem == null || CollectionUtil.isEmpty(deviceItem.getDatas())) {
                return;
            }
            DeviceListVH deviceListVH = (DeviceListVH) holder;
            LinearLayoutManager layoutManager = new LinearLayoutManager(NooieApplication.mCtx);
            deviceListVH.rcvDevice.setLayoutManager(layoutManager);
            deviceListVH.ivDeviceItemArrow.setVisibility(isMultiDeviceType() ? View.VISIBLE : View.GONE);
            deviceListVH.tvDeviceItemProduct.setVisibility(isMultiDeviceType() ? View.VISIBLE : View.GONE);
            if (ProductType.PRODUCT_IPC == deviceItem.getProductType()) {
                deviceListVH.ivDeviceItemArrow.setImageResource(R.drawable.home_item_wifi_icon);
                deviceListVH.tvDeviceItemProduct.setText(NooieApplication.mCtx.getString(R.string.connection_mode_qc_title_hc_320));
                List<ListDeviceItem> listDeviceItems = (List<ListDeviceItem>)deviceItem.getDatas();
                DevicesAdapter devicesAdapter = new DevicesAdapter();
                devicesAdapter.setOnItemClickListener(mIpcListener);
                devicesAdapter.setData(listDeviceItems);
                deviceListVH.rcvDevice.setAdapter(devicesAdapter);
                setupDeviceSortComponent(mItemDragEnable, deviceListVH.rcvDevice, devicesAdapter);
            } else if (ProductType.PRODUCT_BLE_AP_IPC == deviceItem.getProductType()) {
                if (deviceItem == null || CollectionUtil.isEmpty(deviceItem.getDatas())) {
                    return;
                }
                deviceListVH.ivDeviceItemArrow.setImageResource(R.drawable.home_item_ble_ap_icon);
                deviceListVH.tvDeviceItemProduct.setText(NooieApplication.mCtx.getString(R.string.connection_mode_dc_title_hc_320));
                List<BleApDeviceEntity> bleApDeviceEntities = (List<BleApDeviceEntity>)deviceItem.getDatas();
                BleApDeviceAdapter bleApDeviceAdapter = new BleApDeviceAdapter();
                bleApDeviceAdapter.setListener(mBleApIpcListener);
                bleApDeviceAdapter.setData(bleApDeviceEntities);
                deviceListVH.rcvDevice.setAdapter(bleApDeviceAdapter);
                setupBleApDeviceSortComponent(mItemDragEnable, deviceListVH.rcvDevice, bleApDeviceAdapter);
            }
        }
    }

    public void setIpcListener(DevicesAdapter.OnItemClickListener ipcListener) {
        mIpcListener = ipcListener;
    }

    public void setBleApIpcListener(BleApDeviceListener bleApIpcListener) {
        mBleApIpcListener = bleApIpcListener;
    }

    public void setLockListener(LockDeviceListener lockListener) {
        mLockListener = lockListener;
    }

    public void setDeviceCortComponent(DeviceSortComponent component) {
        mDeviceSortComponent = component;
    }

    public void toggleItemDrag() {
        setItemDragEnable(!mItemDragEnable);
    }

    public boolean isItemDragEnable() {
        return mItemDragEnable;
    }

    public void updateItemOpenStatus(ProductType productType, String deviceId, int status) {
        if (CollectionUtil.isEmpty(mDatas)) {
            return;
        }
        if (productType == ProductType.PRODUCT_IPC) {
            for (DeviceItem deviceItem : mDatas) {
                if (deviceItem != null && deviceItem.getProductType() == productType && CollectionUtil.isNotEmpty(deviceItem.getDatas())) {
                    updateItemOpenStatusForIpc((List<ListDeviceItem>)deviceItem.getDatas(), deviceId, status);
                }
            }
        }
        notifyDataSetChanged();
    }

    public Map<String, Integer> getBleDeviceSortMap() {
        return mBleApDeviceSortMap;
    }

    public boolean isDeviceSortEnable() {
        if (CollectionUtil.isEmpty(getData())) {
            return false;
        }
        boolean isSortEnable = false;
        for (DeviceItem deviceItem : getData()) {
            if (deviceItem != null && CollectionUtil.size(deviceItem.getDatas()) > 1) {
                isSortEnable = true;
                break;
            }
        }
        return isSortEnable;
    }

    private boolean isMultiDeviceType() {
        if (CollectionUtil.size(getData()) < 2) {
            return false;
        }
        List<ProductType> productTypeList = new ArrayList<>();
        for (DeviceItem deviceItem : getData()) {
            if (deviceItem != null && deviceItem.getProductType() != null && CollectionUtil.isNotEmpty(deviceItem.getDatas()) && !productTypeList.contains(deviceItem.getProductType())) {
                productTypeList.add(deviceItem.getProductType());
            }
        }
        return CollectionUtil.size(productTypeList) > 1;
    }

    private void setItemDragEnable(boolean enable) {
        mItemDragEnable = enable;
        notifyDataSetChanged();
    }

    private void setupBleApDeviceSortComponent(boolean itemDragEnable, RecyclerView rvList, BleApDeviceAdapter adapter) {
        if (rvList == null || adapter == null) {
            return;
        }

        if (itemDragEnable) {
            mBleApDeviceSortComponent = new BleApDeviceSortComponent();
            mBleApDeviceSortComponent.setRv(rvList);
            mBleApDeviceSortComponent.setAdapter(adapter);
            mBleApDeviceSortComponent.setupItemTouchHelper();
            mBleApDeviceSortComponent.setListener(new BleApDeviceSortComponent.BleApDeviceSortComponentListener() {
                @Override
                public void onDataChange(Map<String, Integer> sortMap, List<BleApDeviceEntity> dataList) {
                    updateBleApSortMap(sortMap);
                }
            });
        } else {
        }
        adapter.setItemDragEnable(itemDragEnable);
        if (mBleApDeviceSortComponent != null) {
            mBleApDeviceSortComponent.setDragEnable(itemDragEnable);
        }
        releaseBleApDeviceSortComponent(!itemDragEnable);
    }

    private void releaseBleApDeviceSortComponent(boolean isRelease) {
        if (isRelease && mBleApDeviceSortComponent != null) {
            mBleApDeviceSortComponent.release();
            mBleApDeviceSortComponent = null;
        }
    }

    private void setupDeviceSortComponent(boolean itemDragEnable, RecyclerView rvList, DevicesAdapter adapter) {
        if (rvList == null || adapter == null || mDeviceSortComponent == null) {
            return;
        }
        if (itemDragEnable) {
            mDeviceSortComponent.setDeviceRv(rvList);
            mDeviceSortComponent.setDeviceAdapter(adapter);
            mDeviceSortComponent.setupItemTouchHelper();
            mDeviceSortComponent.cloneDeviceList();
        } else {
        }
        adapter.setItemDragEnable(itemDragEnable);
        if (mDeviceSortComponent != null) {
            mDeviceSortComponent.setDragEnable(itemDragEnable);
        }
        releaseDeviceSortComponent(!itemDragEnable);
    }

    private void releaseDeviceSortComponent(boolean isRelease) {
        if (isRelease && mDeviceSortComponent != null)  {
            mDeviceSortComponent.clearDeviceList();
        }
    }

    private void updateItemOpenStatusForIpc(List<ListDeviceItem> deviceList, String deviceId, int status) {
        if (CollectionUtil.isEmpty(deviceList)) {
            return;
        }
        for (int i = 0; i < CollectionUtil.safeFor(deviceList).size(); i++) {
            ListDeviceItem item = deviceList.get(i);
            if (item != null && !TextUtils.isEmpty(item.getDeviceId()) && item.getDeviceId().equalsIgnoreCase(deviceId)) {
                item.setOpenStatus(status);
                if (item.getBindDevice() != null) {
                    item.getBindDevice().setOpen_status(status);
                }
                break;
            }
        }
    }

    private void initBleApSortMap() {
        if (mBleApDeviceSortMap == null) {
            mBleApDeviceSortMap = new ArrayMap<>();
        }
    }

    private void updateBleApSortMap(Map<String, Integer> sortMap) {
        initBleApSortMap();
        mBleApDeviceSortMap.clear();
        if (sortMap != null && !sortMap.isEmpty()) {
            mBleApDeviceSortMap.putAll(sortMap);
        }
    }

    public static class DeviceListVH extends RecyclerView.ViewHolder {

        @BindView(R.id.ivDeviceItemArrow)
        ImageView ivDeviceItemArrow;
        @BindView(R.id.tvDeviceItemProduct)
        TextView tvDeviceItemProduct;
        @BindView(R.id.btnDeviceItemMore)
        TextView btnDeviceItemMore;
        @BindView(R.id.rcvDevice)
        RecyclerView rcvDevice;

        public DeviceListVH(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public static class AddCameraViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.vAddDeviceContainer)
        View container;
        @BindView(R.id.btnAddDevice)
        NEventTextView btnAddDevice;

        public AddCameraViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
