package com.afar.osaio.smart.electrician.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.util.ConstantValue;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.nooie.common.utils.collection.CollectionUtil;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * DeviceAdapter
 *
 * @author Administrator
 * @date 2019/3/7
 */
public class SelectedDeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_NORMAL = 0x01;
    private static final int TYPE_EMPTY = 0x02;

    private List<DeviceBean> mDevices = new ArrayList<>();
    private DeviceItemListener mListener;
    private Set<String> mDeviceIds = new HashSet<>();
    private Set<String> mGroupDeviceIds = new HashSet<>();

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_NORMAL) {
            View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_selected_device, parent, false);
            return new DeviceViewHolder(view);
        } else {
            View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_message_empty, parent, false);
            return new EmptyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        if (viewHolder instanceof DeviceViewHolder) {
            final DeviceViewHolder holder = (DeviceViewHolder) viewHolder;
            final DeviceBean device = mDevices.get(position);

            DrawableCrossFadeFactory drawableCrossFadeFactory = new DrawableCrossFadeFactory.Builder(ConstantValue.DURATION_MILLIS).setCrossFadeEnabled(true).build();
            Glide.with(NooieApplication.mCtx)
                    .load(device.getIconUrl())
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.ic_list_placeholder).error(R.drawable.ic_list_placeholder))
                    .transition(DrawableTransitionOptions.with(drawableCrossFadeFactory))
                    .into(holder.ivDeviceIcon);

            holder.tvDeviceName.setText(device.getName());

            if (mGroupDeviceIds.contains(device.getDevId())) {
                holder.cbDeviceSelected.setChecked(mGroupDeviceIds.contains(device.getDevId()));
                mDeviceIds.add(device.getDevId());
            }

            holder.cbDeviceSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        mDeviceIds.add(device.getDevId());
                    } else if (mDeviceIds.contains(device.getDevId())) {
                        mDeviceIds.remove(device.getDevId());
                    }
                }
            });

            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.cbDeviceSelected.setChecked(!holder.cbDeviceSelected.isChecked());
                }
            });
        } else {
            EmptyViewHolder holder = (EmptyViewHolder) viewHolder;
            holder.tvContent.setText(R.string.selected_device_empty);
        }
    }

    @Override
    public int getItemCount() {
        return CollectionUtil.isEmpty(mDevices) ? 1 : mDevices.size();
    }

    @Override
    public int getItemViewType(int position) {
        return CollectionUtil.isEmpty(mDevices) ? TYPE_EMPTY : TYPE_NORMAL;
    }

    public void setData(List<DeviceBean> devices) {
        mDeviceIds.clear();
        mDevices.clear();
        mDevices.addAll(devices);
        notifyDataSetChanged();
    }

    public ArrayList<String> getSelectedDevice() {
        ArrayList<String> selectedDevice = new ArrayList<>();
        for (String deviceId : mDeviceIds) {
            selectedDevice.add(deviceId);
        }
        return selectedDevice;
    }

    public void addGroupDeviceIds(String deviceId) {
        mGroupDeviceIds.add(deviceId);
    }

    public void setListener(DeviceItemListener listener) {
        mListener = listener;
    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivDeviceIcon)
        ImageView ivDeviceIcon;
        @BindView(R.id.tvDeviceName)
        TextView tvDeviceName;
        @BindView(R.id.cbDeviceSelected)
        CheckBox cbDeviceSelected;
        View container;

        public DeviceViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container = view;
        }
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvContent)
        TextView tvContent;

        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public interface DeviceItemListener {
        void onItemClick(DeviceBean device);
    }
}
