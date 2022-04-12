package com.afar.osaio.smart.device.adapter;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.bean.DeviceInfo;
import com.nooie.common.utils.collection.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * NooiePlayerDevicesAdapter
 *
 * @author Administrator
 * @date 2019/5/6
 */
public class NooiePlayerDevicesAdapter extends RecyclerView.Adapter<NooiePlayerDevicesAdapter.LiveDeviceViewHolder> {
    private List<DeviceInfo> deviceList;

    private OnClickLiveDeviceItemListener listener;

    public interface OnClickLiveDeviceItemListener {
        void onClickItem(String deviceId);
    }

    public void setListener(OnClickLiveDeviceItemListener listener) {
        this.listener = listener;
    }

    public NooiePlayerDevicesAdapter(@NonNull List<DeviceInfo> deviceList) {
        //this.deviceList = deviceList;
        setData(deviceList);
    }

    @NonNull
    @Override
    public LiveDeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_live_device, parent, false);
        return new LiveDeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LiveDeviceViewHolder holder, final int position) {
        holder.itemContainer.setBackgroundResource(R.drawable.device_item_state_list);
        if (deviceList.get(position) != null) {
            String deviceName = deviceList.get(position).getNooieDevice() != null ? deviceList.get(position).getNooieDevice().getName() : "";
            holder.tvName.setText(deviceName);

            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null)
                        listener.onClickItem(deviceList.get(position).getDeviceId());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return CollectionUtil.size(deviceList);
    }

    public void setData(List<DeviceInfo> devices) {
        if (deviceList == null) {
            deviceList = new ArrayList<>();
        }
        if (CollectionUtil.isEmpty(devices)) {
            return;
        }

        deviceList.clear();
        deviceList.addAll(devices);
        notifyDataSetChanged();
    }

    public void release() {
        if (deviceList != null) {
            deviceList.clear();
            deviceList = null;
        }

        listener = null;
    }

    static class LiveDeviceViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.container)
        ConstraintLayout itemContainer;
        @BindView(R.id.tvName)
        TextView tvName;

        View container;

        public LiveDeviceViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            container = itemView;
        }
    }
}
