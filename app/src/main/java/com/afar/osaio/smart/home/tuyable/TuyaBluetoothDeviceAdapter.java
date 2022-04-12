package com.afar.osaio.smart.home.tuyable;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.adapter.BaseAdapter;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.mixipc.adapter.listener.ScanBluetoothDeviceListener;
import com.afar.osaio.smart.mixipc.profile.bean.BleDevice;
import com.afar.osaio.util.ConstantValue;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TuyaBluetoothDeviceAdapter extends BaseAdapter<DeviceBleInfoBean, TuyaBluetoothDeviceAdapter.TuyaBluetoothDeviceListener, TuyaBluetoothDeviceAdapter.ScanBluetoothDeviceVH> {

    private DeviceBleInfoBean mSelectedBleDevice = null;
    private  int choosePosition  = -1;

    @Override
    public TuyaBluetoothDeviceAdapter.ScanBluetoothDeviceVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ScanBluetoothDeviceVH(createVHView(R.layout.item_tuya_ble_device, parent));
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(TuyaBluetoothDeviceAdapter.ScanBluetoothDeviceVH holder, int position) {
        DeviceBleInfoBean deviceBleInfoBean = mDatas.get(position);
        if (deviceBleInfoBean == null || deviceBleInfoBean.getScanDeviceBean() == null || holder == null) {
            return;
        }
        if (choosePosition == position){
            holder.ivScanBluetoothDeviceLoading.setVisibility( View.VISIBLE);

            ObjectAnimator btnSignInAnimator = ObjectAnimator.ofFloat( holder.ivScanBluetoothDeviceLoading, "Rotation", 0, 360);
            btnSignInAnimator.setDuration(2000);
            btnSignInAnimator.setRepeatCount(-1);
            btnSignInAnimator.start();
        }else{
            holder.ivScanBluetoothDeviceLoading.setVisibility( View.GONE);

        }
        holder.tvItemAddress.setText(deviceBleInfoBean.getScanDeviceBean().getAddress());
        if ( deviceBleInfoBean.getConfigProductInfoBean() != null){
            holder.deviceName.setText(deviceBleInfoBean.getConfigProductInfoBean().getName());
            DrawableCrossFadeFactory drawableCrossFadeFactory = new DrawableCrossFadeFactory.Builder(ConstantValue.DURATION_MILLIS).setCrossFadeEnabled(true).build();
            Glide.with(NooieApplication.mCtx)
                    .load(deviceBleInfoBean.getConfigProductInfoBean().getIcon())
                    .apply(new RequestOptions().placeholder(R.drawable.home_plug_icon).error(R.drawable.home_plug_icon))
                    .transition(DrawableTransitionOptions.with(drawableCrossFadeFactory))
                    .into(holder.ivItemSmartDevice);
        }
        holder.layBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder == null) {
                    return;
                }
                choosePosition = position;
                notifyDataSetChanged();
                if (mListener != null) {
                    mListener.onItemClick(deviceBleInfoBean);
                }
            }
        });
    }



    public interface TuyaBluetoothDeviceListener {

        void onItemClick(DeviceBleInfoBean deviceBleInfoBean);
    }

    public void setSelectedBleDevice(DeviceBleInfoBean bleDevice) {
        this.mSelectedBleDevice = bleDevice;
        notifyDataSetChanged();
    }



    public void resetBluetoothDevice(List<DeviceBleInfoBean> devices) {
        setSelectedBleDevice(null);
        setData(devices);
    }




    public static class ScanBluetoothDeviceVH extends RecyclerView.ViewHolder {

        @BindView(R.id.ivItemSmartDevice)
        ImageView ivItemSmartDevice;
        @BindView(R.id.tvItemSmartNormalDeviceName)
        TextView deviceName;
        @BindView(R.id.tvItemSmartAddress)
        TextView tvItemAddress;
        @BindView(R.id.layBlue)
        ConstraintLayout layBlue;
        @BindView(R.id.ivScanBluetoothDeviceLoading)
        ImageView ivScanBluetoothDeviceLoading;

        public ScanBluetoothDeviceVH(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
