package com.afar.osaio.smart.electrician.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.electrician.bean.DeviceGroupingBean;
import com.afar.osaio.smart.electrician.bean.DeviceTypeBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceSortAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int DEVICE_GROUP_TITLE = 0x01;
    private static final int DEVICE_TYPE = 0x02;

    private List<Object> deviceGroupings = new ArrayList<>();
    private OnItemClickListener mListener;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == DEVICE_GROUP_TITLE) {
            //View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_type_title, parent, false);
            View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_type_space, parent, false);
            return new DeviceTitleViewHolder(view);
        } else {
            View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_type_device, parent, false);
            return new DeviceTypeViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof DeviceTitleViewHolder) {
            DeviceTitleViewHolder holder = (DeviceTitleViewHolder) viewHolder;
           /* DeviceGroupingBean deviceGrouping = (DeviceGroupingBean) deviceGroupings.get(i);
            holder.tvTypeTitle.setText(deviceGrouping.getGroupingTitle());
            holder.tvTypeTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onClick(holder.itemView, i, deviceGrouping.getGroupingTitle());
                    }
                }
            });*/
        } else if (viewHolder instanceof DeviceTypeViewHolder) {
            DeviceTypeViewHolder holder = (DeviceTypeViewHolder) viewHolder;
            if (deviceGroupings.size()==0 || i> deviceGroupings.size()){
                return;
            }
            DeviceTypeBean deviceType = (DeviceTypeBean) deviceGroupings.get(i);
            if (deviceType != null){
                holder.tvDevice.setText(deviceType.getDeviceName());
                holder.ivDevice.setImageResource(deviceType.getDevicePic());
            }
            holder.container.setOnClickListener(v -> {
                if (mListener != null && deviceType != null) {
                    mListener.onClick(holder.itemView, i, deviceType.getDeviceName());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return deviceGroupings.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (deviceGroupings.get(position) instanceof DeviceGroupingBean) {
            return DEVICE_GROUP_TITLE;
        } else {
            return DEVICE_TYPE;
        }
    }

    public void setData(List<Object> groupingBeans) {
        deviceGroupings.clear();
        deviceGroupings.addAll(groupingBeans);
        notifyDataSetChanged();
    }

    public static class DeviceTitleViewHolder extends RecyclerView.ViewHolder {

        /*@BindView(R.id.tvTypeTitle)
        TextView tvTypeTitle;*/


        public DeviceTitleViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public static class DeviceTypeViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivDevice)
        ImageView ivDevice;
        @BindView(R.id.tvDevice)
        TextView tvDevice;

        View container;

        public DeviceTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            container = itemView;
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(View itemView, int position, String name);
    }

}

