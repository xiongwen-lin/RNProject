package com.afar.osaio.smart.home.adapter;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.adapter.BaseAdapter;
import com.afar.osaio.smart.device.bean.ListDeviceItem;
import com.afar.osaio.smart.device.helper.SmartDeviceHelper;
import com.afar.osaio.smart.home.adapter.listener.SmartNormalDeviceListener;
import com.afar.osaio.smart.home.bean.SmartBaseDevice;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.SmartNormalDeviceView;
import com.afar.osaio.widget.listener.SmartNormalDeviceViewListener;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.widget.NEventTextView;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SmartNormalDeviceAdapter extends BaseAdapter<SmartBaseDevice, SmartNormalDeviceListener, RecyclerView.ViewHolder> {

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ConstantValue.ADD_CAMERA_TYPE) {
            return new AddCameraViewHolder(createVHView(R.layout.item_add_device, parent));
        } else {
            return new SmartNormalDeviceVH(createVHView(R.layout.item_smart_normal_device, parent));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder == null) {
            return;
        }
        if (holder instanceof AddCameraViewHolder) {
            NooieLog.d("-->> debug AddCameraViewHolder ");
            AddCameraViewHolder addVH = (AddCameraViewHolder) holder;
            /*addVH.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onAddDeviceBtnClick();
                    }
                }
            });*/
            addVH.vAddDevice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onAddDeviceBtnClick();
                    }
                }
            });
            addVH.btnAddDevice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onAddDeviceBtnClick();
                    }
                }
            });
        } else if (holder instanceof SmartNormalDeviceVH) {
            NooieLog.d("-->> debug SmartNormalDeviceVH ");
            SmartBaseDevice data = getDataByPosition(position);
            if (data == null) {
                NooieLog.d("-->> debug data null position " + position);
                return;
            }
            NooieLog.d("-->> debug data : " + data.toString() + " position " + position);
            SmartNormalDeviceVH deviceVH = (SmartNormalDeviceVH) holder;
            deviceVH.vSmartNormalDeviceItem.refreshView(data);
            deviceVH.vSmartNormalDeviceItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        NooieLog.e("--> debug dealOnItemClick vSmartNormalDeviceItem onClick");
                        mListener.onItemClick(data);
                    }
                }
            });
            /*
            deviceVH.vSmartNormalDeviceItem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mListener != null) {
                        return mListener.onItemLongClick(data);
                    }
                    return false;
                }
            });
             */
            deviceVH.vSmartNormalDeviceItem.setListener(new SmartNormalDeviceViewListener() {
                @Override
                public void onSwitchBtnClick(SmartBaseDevice device, boolean on) {
                    if (mListener != null) {
                        NooieLog.e("-->> debug dealOnItemClick vSmartNormalDeviceItem onSwitchBtnClick");
                        mListener.onSwitchBtnClick(device, on);
                    }
                }
            });
        }
    }

    public void removeTyDeviceBean(String deviceId) {
        if (CollectionUtil.isNotEmpty(mDatas)) {
            Iterator iterator = mDatas.iterator();
            while (iterator.hasNext()) {
                SmartBaseDevice device = (SmartBaseDevice) iterator.next();
                if (deviceId.equals(device.deviceId) && device.deviceCategory.equals("TUYA")) {
                    iterator.remove();
                    notifyDataSetChanged();
                }
            }
        }
    }

    public void updateTyDeviceBean(DeviceBean deviceBean) {
        if (CollectionUtil.isNotEmpty(mDatas)) {
            for (int i = 0; i < mDatas.size(); i++) {
                SmartBaseDevice device = mDatas.get(i);
                if (device.deviceCategory.equals("TUYA") && device.deviceId.equals(deviceBean.getDevId())) {
                    mDatas.set(i, SmartDeviceHelper.convertSmartTyDevice(deviceBean));
                    notifyItemChanged(i);
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (CollectionUtil.isEmpty(getData())) {
            return ConstantValue.ADD_CAMERA_TYPE;
        } else {
            return super.getItemViewType(position);
        }
    }

    @Override
    public int getItemCount() {
        if (CollectionUtil.isEmpty(getData())) {
            return 1;
        } else {
            return CollectionUtil.size(getData());
        }
    }

    public void updateItemOpenStatusForCamera(String deviceId, boolean switchOn) {
        if (CollectionUtil.isEmpty(getData())) {
            return;
        }
        int size = CollectionUtil.size(getData());
        for (int i = 0; i < size; i++) {
            SmartBaseDevice item = getDataByPosition(i);
            if (item != null && !TextUtils.isEmpty(item.deviceId) && item.deviceId.equalsIgnoreCase(deviceId)) {
                getDataByPosition(i).deviceSwitchState = SmartDeviceHelper.convertDeviceSwitchState(switchOn);
                break;
            }
        }
    }

    public static class SmartNormalDeviceVH extends RecyclerView.ViewHolder {

        @BindView(R.id.vSmartNormalDeviceItem)
        SmartNormalDeviceView vSmartNormalDeviceItem;

        public SmartNormalDeviceVH(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public static class AddCameraViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.vAddDeviceContainer)
        View container;
        @BindView(R.id.vAddDevice)
        View vAddDevice;
        @BindView(R.id.btnAddDevice)
        NEventTextView btnAddDevice;

        public AddCameraViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
