package com.afar.osaio.smart.mixipc.adapter;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.adapter.BaseAdapter;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.mixipc.adapter.listener.ScanBluetoothDeviceListener;
import com.afar.osaio.smart.mixipc.profile.bean.BleDevice;
import com.afar.osaio.smart.mixipc.profile.helper.CommonBluetoothHelper;
import com.nooie.common.utils.collection.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ScanBluetoothDeviceAdapter extends BaseAdapter<BleDevice, ScanBluetoothDeviceListener, ScanBluetoothDeviceAdapter.ScanBluetoothDeviceVH> {

    private BleDevice mSelectedBleDevice = null;

    @Override
    public ScanBluetoothDeviceAdapter.ScanBluetoothDeviceVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ScanBluetoothDeviceVH(createVHView(R.layout.item_scan_bluetooth_device, parent));
    }

    @Override
    public void onBindViewHolder(ScanBluetoothDeviceAdapter.ScanBluetoothDeviceVH holder, int position) {
        BleDevice bleDevice = mDatas.get(position);
        if (bleDevice == null || bleDevice.getDevice() == null || holder == null) {
            return;
        }
        boolean isConnecting = checkIsConnectingBluetoothDevice(bleDevice);
        holder.tvScanBluetoothDeviceName.setText(bleDevice.getDevice().getName());
        holder.tvScanBluetoothDeviceConnect.setEnabled(!checkIsConnectingBluetoothState());
        holder.tvScanBluetoothDeviceConnect.setTextColor(!checkIsConnectingBluetoothState() ? ContextCompat.getColor(NooieApplication.mCtx, R.color.theme_green) : ContextCompat.getColor(NooieApplication.mCtx, R.color.theme_subtext_color));
        holder.tvScanBluetoothDeviceConnect.setVisibility(isConnecting ? View.GONE : View.VISIBLE);
        holder.ivScanBluetoothDeviceLoading.setVisibility(isConnecting ? View.VISIBLE : View.GONE);
        holder.tvScanBluetoothDeviceConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder == null) {
                    return;
                }
                int indexId = holder.getAdapterPosition();
                BleDevice selectBleDevice = getDataByPosition(indexId);
                if (selectBleDevice == null) {
                    return;
                }
                setSelectedBleDevice(selectBleDevice);
                if (mListener != null) {
                    mListener.onItemClick(selectBleDevice);
                }
            }
        });
    }

    public void setSelectedBleDevice(BleDevice bleDevice) {
        this.mSelectedBleDevice = bleDevice;
        notifyDataSetChanged();
    }

    public void updateBluetoothDevice(List<BleDevice> devices) {
        if (CollectionUtil.isEmpty(devices)) {
            return;
        }

        List<BleDevice> newBleDevice = filterNewBleDevice(devices, getData());
        if (CollectionUtil.isEmpty(newBleDevice)) {
            return;
        }

        if (mDatas == null) {
            mDatas = new ArrayList<>();
        }
        mDatas.addAll(newBleDevice);
        notifyDataSetChanged();
    }

    public void resetBluetoothDevice(List<BleDevice> devices) {
        setSelectedBleDevice(null);
        setData(devices);
    }

    public boolean checkIsConnectingBluetoothState() {
        return mSelectedBleDevice != null && mSelectedBleDevice.getDevice() != null;
    }

    private boolean checkIsConnectingBluetoothDevice(BleDevice bleDevice) {
        if (bleDevice == null || bleDevice.getDevice() == null || mSelectedBleDevice == null || mSelectedBleDevice.getDevice() == null) {
            return false;
        }
        boolean isConnecting = !TextUtils.isEmpty(bleDevice.getDevice().getName()) && bleDevice.getDevice().getName().equalsIgnoreCase(mSelectedBleDevice.getDevice().getName());
        return isConnecting;
    }

    private List<BleDevice> filterNewBleDevice(List<BleDevice> bleDeviceList, List<BleDevice> currentBleDeviceList) {
        if (CollectionUtil.isEmpty(bleDeviceList) || CollectionUtil.isEmpty(currentBleDeviceList)) {
            return bleDeviceList;
        }
        List<String> currentBleDeviceAddressList = new ArrayList<>();
        for (BleDevice bleDevice : currentBleDeviceList) {
            if (CommonBluetoothHelper.checkBleDeviceValid(bleDevice)) {
                currentBleDeviceAddressList.add(bleDevice.getDevice().getAddress());
            }
        }

        List<BleDevice> newBleDeviceList = new ArrayList<>();
        for (BleDevice bleDevice : bleDeviceList) {
            if (CommonBluetoothHelper.checkBleDeviceValid(bleDevice) && !currentBleDeviceAddressList.contains(bleDevice.getDevice().getAddress())) {
                newBleDeviceList.add(bleDevice);
            }
        }
        return newBleDeviceList;
    }

    public static class ScanBluetoothDeviceVH extends RecyclerView.ViewHolder {

        @BindView(R.id.vScanBluetoothDevice)
        View vScanBluetoothDevice;
        @BindView(R.id.tvScanBluetoothDeviceName)
        TextView tvScanBluetoothDeviceName;
        @BindView(R.id.tvScanBluetoothDeviceConnect)
        TextView tvScanBluetoothDeviceConnect;
        @BindView(R.id.ivScanBluetoothDeviceLoading)
        ImageView ivScanBluetoothDeviceLoading;

        public ScanBluetoothDeviceVH(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
