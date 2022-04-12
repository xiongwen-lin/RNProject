package com.afar.osaio.smart.smartlook.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.smartlook.bean.BleDevice;
import com.nooie.common.utils.collection.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ScanBleDeviceAdapter extends RecyclerView.Adapter<ScanBleDeviceAdapter.ScanBleDeviceVH> {

    private List<BleDevice> mDatas;
    private ScanBleDeviceListener mListener;

    @Override
    public ScanBleDeviceAdapter.ScanBleDeviceVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_scan_ble_device, parent, false);
        return new ScanBleDeviceVH(view);
    }

    @Override
    public void onBindViewHolder(ScanBleDeviceAdapter.ScanBleDeviceVH holder, int position) {
        BleDevice bleDevice = mDatas.get(position);
        holder.tvScanBleDeviceName.setText(bleDevice.getDevice().getName());
        holder.tvScanBleDeviceRssi.setText(String.format(NooieApplication.mCtx.getString(R.string.scan_device_item_rssi_label), bleDevice.getRssi()));
        holder.vScanBleDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(bleDevice);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return CollectionUtil.safeFor(mDatas).size();
    }

    public void setData(List<BleDevice> datas) {
        if (mDatas == null) {
            mDatas = new ArrayList<>();
        }

        mDatas.clear();
        mDatas.addAll(CollectionUtil.safeFor(datas));
        notifyDataSetChanged();
    }

    public void setListener(ScanBleDeviceListener listener) {
        mListener = listener;
    }

    public static class ScanBleDeviceVH extends RecyclerView.ViewHolder {

        @BindView(R.id.vScanBleDevice)
        View vScanBleDevice;
        @BindView(R.id.tvScanBleDeviceName)
        TextView tvScanBleDeviceName;
        @BindView(R.id.tvScanBleDeviceRssi)
        TextView tvScanBleDeviceRssi;

        public ScanBleDeviceVH(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public interface ScanBleDeviceListener {
        void onItemClick(BleDevice bleDevice);
    }

}
