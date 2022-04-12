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
import com.afar.osaio.smart.electrician.widget.SquareLinearLayout;
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
public class ManageLampDeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int TAG_COMMON = 0;
    private final int TAG_SELECT = 1;

    private List<DeviceBean> mDevices = new ArrayList<>();
    private DeviceItemListener mListener;
    private Set<String> mGroupDeviceIds = new HashSet<>();
    private int tagType;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_manage_lamp_device, parent, false);
        return new DeviceViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {

        if (viewHolder instanceof DeviceViewHolder) {
            DeviceViewHolder holder = (DeviceViewHolder)viewHolder;
            final DeviceBean device = mDevices.get(position);

            DrawableCrossFadeFactory drawableCrossFadeFactory = new DrawableCrossFadeFactory.Builder(ConstantValue.DURATION_MILLIS).setCrossFadeEnabled(true).build();
            Glide.with(NooieApplication.mCtx)
                    .load(device.getIconUrl())
                    .apply(new RequestOptions().centerCrop().placeholder(R.drawable.ic_list_placeholder).error(R.drawable.ic_list_placeholder))
                    .transition(DrawableTransitionOptions.with(drawableCrossFadeFactory))
                    .into(holder.ivIcon);

            holder.tvName.setText(device.getName());

            if (tagType == TAG_SELECT){//选择状态
                if (mGroupDeviceIds.contains(device.getDevId())){
                    holder.sllDevice.setBackgroundResource(R.drawable.item_new_blue_state_list_radius);
                }else {
                    holder.sllDevice.setBackgroundResource(R.drawable.item_white_state_list_radius_15);
                }
            }else {//非选择状态
                if (device.getIsOnline()){
                    holder.sllDevice.setBackgroundResource(R.drawable.item_white_state_list_radius_15);
                }else {
                    holder.sllDevice.setBackgroundResource(R.drawable.item_offline_state_list_radius);
                }

            }

            holder.sllDevice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (tagType == TAG_COMMON){
                        if (mListener != null) {
                            mListener.onItemClick(device);
                        }
                    }else {
                        if (mGroupDeviceIds.contains(device.getDevId())){
                            mGroupDeviceIds.remove(device.getDevId());
                            notifyItemChanged(position);
                        }else {
                            mGroupDeviceIds.add(device.getDevId());
                            notifyItemChanged(position);
                        }
                    }
                }
            });

        } else {
            EmptyViewHolder holder = (EmptyViewHolder)viewHolder;
            holder.tvContent.setText(R.string.selected_device_empty);
        }
    }

    @Override
    public int getItemCount() {
        return CollectionUtil.isEmpty(mDevices) ? 0 : mDevices.size();
    }

    public void setTagType(int tagType){
        this.tagType = tagType;
        notifyDataSetChanged();
    }

    public int getTagType(){
        return tagType;
    }

    public void setSelectedAll(){
        for (DeviceBean device: mDevices) {
            if (!mGroupDeviceIds.contains(device.getDevId())){
                mGroupDeviceIds.add(device.getDevId());
            }
        }
        notifyDataSetChanged();
    }

    public void setData(List<DeviceBean> devices) {
        mDevices.clear();
        mDevices.addAll(devices);
        notifyDataSetChanged();
    }

    public ArrayList<String> getSelectedDevice() {
        ArrayList<String> selectedDevice = new ArrayList<>();
        for(String deviceId : mGroupDeviceIds) {
            selectedDevice.add(deviceId);
        }
        return selectedDevice;
    }

    public void addGroupDeviceIds(String deviceId){
        mGroupDeviceIds.add(deviceId);
    }

    public void setListener(DeviceItemListener listener) {
        mListener = listener;
    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.sllDevice)
        SquareLinearLayout sllDevice;
        @BindView(R.id.ivIcon)
        ImageView ivIcon;
        @BindView(R.id.tvName)
        TextView tvName;

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
