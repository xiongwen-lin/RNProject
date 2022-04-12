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
import com.afar.osaio.smart.electrician.widget.SquareLinearLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.nooie.common.utils.log.NooieLog;
import com.suke.widget.SwitchButton;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * HomeManagerDeviceAdapter
 *
 * @author Administrator
 * @date 2019/3/13
 */
public class HomeManagerDeviceAdapter extends RecyclerView.Adapter<HomeManagerDeviceAdapter.HomeManagerDeviceViewHolder> {

    private List<DeviceBean> mDevices = new ArrayList<>();
    private HomeManagerDeviceListener mListener;

    @Override
    public HomeManagerDeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_home_manager_device, parent, false);
        return new HomeManagerDeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HomeManagerDeviceViewHolder holder, int position) {
        final DeviceBean device = mDevices.get(position);
        Glide.with(NooieApplication.mCtx)
                .load(device.getIconUrl())
                .apply(new RequestOptions().placeholder(R.drawable.ic_list_placeholder).error(R.drawable.ic_list_placeholder).centerCrop())
                .into(holder.ivHomeManagerDeviceIcon);
        holder.tvHomeManagerDeviceName.setText(device.getName());

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClickListener(device);
                }
            }
        });

        if (device.getIsOnline()) {
            holder.backgroundOffline.setVisibility(View.GONE);
            holder.tvOffline.setVisibility(View.GONE);
            holder.btnDeviceSwitch.setVisibility(View.VISIBLE);
            holder.sllDevice.setBackgroundResource(R.drawable.item_white_state_list_radius_15);
            boolean isOpen = PowerStripHelper.getInstance().isDeviceOpen(device);
            if (holder.btnDeviceSwitch.isChecked() != isOpen) {
                holder.btnDeviceSwitch.toggleNoCallback();
            }
            holder.btnDeviceSwitch.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                    if (mListener != null) {
                        NooieLog.e("----------onCheckedChanged isChecked "+isChecked);
                        mListener.onSwitchClick(device, isChecked);
                    }
                }
            });
        } else {
            holder.btnDeviceSwitch.setVisibility(View.GONE);
            holder.backgroundOffline.setVisibility(View.VISIBLE);
            holder.tvOffline.setVisibility(View.VISIBLE);

            //holder.sllDevice.setBackgroundResource(R.drawable.item_offline_state_list_radius);
        }
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    public void updateItemChange(int position, DeviceBean deviceBean) {
        mDevices.set(position, deviceBean);
        notifyItemChanged(position);
    }

    public void setData(List<DeviceBean> devices) {
        mDevices.clear();
        mDevices.addAll(devices);
        notifyDataSetChanged();
    }

    public List<DeviceBean> getData() {
        return mDevices;
    }

    public void setListener(HomeManagerDeviceListener listener) {
        mListener = listener;
    }

    public static class HomeManagerDeviceViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.sllDevice)
        SquareLinearLayout sllDevice;
        @BindView(R.id.ivHomeManagerDeviceIcon)
        ImageView ivHomeManagerDeviceIcon;
        @BindView(R.id.tvHomeManagerDeviceName)
        TextView tvHomeManagerDeviceName;
        View container;
        @BindView(R.id.background_offline)
        View backgroundOffline;
        @BindView(R.id.btnDeviceSwitch)
        SwitchButton btnDeviceSwitch;
        @BindView(R.id.tvOffline)
        TextView tvOffline;

        public HomeManagerDeviceViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container = view;
        }

    }

    public interface HomeManagerDeviceListener {
        void onItemClickListener(DeviceBean device);

        void onSwitchClick(DeviceBean device, boolean isChecked);
    }

}
