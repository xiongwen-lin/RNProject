package com.afar.osaio.smart.electrician.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.electrician.manager.PowerStripHelper;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * HomeDeviceAdapter
 *
 * @author Administrator
 * @date 2019/3/13
 */
public class HomeDeviceAdapter extends RecyclerView.Adapter<HomeDeviceAdapter.HomeDeviceViewHolder> {

    private List<DeviceBean> mDevices = new ArrayList<>();
    private HomeDeviceListener mListener;

    @Override
    public HomeDeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_home_owner, parent, false);
        return new HomeDeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HomeDeviceViewHolder holder, int position) {
        if (position < mDevices.size() - 1) {
            final DeviceBean device = mDevices.get(position);
            if (PowerStripHelper.getInstance().isFloorLamp(device)) {
                holder.ivIcon.setImageResource(R.drawable.manage_lamp);
            } else if (PowerStripHelper.getInstance().isLampStrip(device)) {
                holder.ivIcon.setImageResource(R.drawable.manage_light_strip);
            } else if (PowerStripHelper.getInstance().isLightModulator(device)) {
                holder.ivIcon.setImageResource(R.drawable.manage_modulator);
            } else if (PowerStripHelper.getInstance().isWallSwitch(device)
                    || PowerStripHelper.getInstance().isMultiWallSwitch(device)) {
                holder.ivIcon.setImageResource(R.drawable.manage_switch);
            } else if (PowerStripHelper.getInstance().isPlug(device)) {
                holder.ivIcon.setImageResource(R.drawable.manage_device);
            } else if (PowerStripHelper.getInstance().isPowerStrip(device)
                    || PowerStripHelper.getInstance().isDimmerPlug(device)) {
                holder.ivIcon.setImageResource(R.drawable.manage_power_strip);
            } else {
                holder.ivIcon.setImageResource(R.drawable.manage_lamp);
            }

            holder.tvName.setVisibility(View.VISIBLE);
            holder.tvName.setText(device.getName());

            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemClickListener(device);
                    }
                }
            });
        } else {
            holder.ivIcon.setImageResource(R.drawable.add_device_default);
            holder.tvName.setVisibility(View.GONE);

            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onAddClickListener();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    public void setData(List<DeviceBean> devices) {
        mDevices.clear();
        mDevices.addAll(devices);
        mDevices.add(new DeviceBean());
        notifyDataSetChanged();
    }

    public List<DeviceBean> getData() {
        return mDevices;
    }

    public void setListener(HomeDeviceListener listener) {
        mListener = listener;
    }

    public static class HomeDeviceViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivIcon)
        ImageView ivIcon;
        @BindView(R.id.tvName)
        TextView tvName;
        View container;

        public HomeDeviceViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container = view;
        }

    }

    public interface HomeDeviceListener {
        void onAddClickListener();

        void onItemClickListener(DeviceBean device);
    }

}
