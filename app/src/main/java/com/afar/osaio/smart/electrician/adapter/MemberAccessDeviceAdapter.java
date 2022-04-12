package com.afar.osaio.smart.electrician.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.tuya.smart.home.sdk.bean.DeviceShareBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * MemberAccessDeviceAdapter
 *
 * @author Administrator
 * @date 2019/3/13
 */
public class MemberAccessDeviceAdapter extends RecyclerView.Adapter<MemberAccessDeviceAdapter.HomeManagerDeviceViewHolder> {

    private List<DeviceShareBean> mDevices = new ArrayList<>();
    private MemberAccessDeviceListener mListener;
    private boolean isShowDelete = false;

    @Override
    public HomeManagerDeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_member_access_device, parent, false);
        return new HomeManagerDeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HomeManagerDeviceViewHolder holder, final int position) {
        final DeviceShareBean device = mDevices.get(position);
        Glide.with(NooieApplication.mCtx)
                .load(device.getIconUrl())
                .apply(new RequestOptions().placeholder(R.drawable.modify).error(R.drawable.modify).centerCrop())
                .into(holder.ivHomeManagerDeviceIcon);
        holder.tvHomeManagerDeviceName.setText(device.getDeviceName());

        holder.ivDelete.setVisibility(isShowDelete ? View.VISIBLE : View.GONE);

        holder.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onDeleteClick(device,position);
                }
            }
        });

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(device,position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    public void setData(List<DeviceShareBean> devices) {
        mDevices.clear();
        mDevices.addAll(devices);
        notifyDataSetChanged();
    }

    public void setData(List<DeviceShareBean> devices,boolean isShowDelete) {
        mDevices.clear();
        mDevices.addAll(devices);
        this.isShowDelete = isShowDelete;
        notifyDataSetChanged();
    }

    public DeviceShareBean getDeviceByPosition(int position){
        return mDevices.get(position);
    }


    public void setListener(MemberAccessDeviceListener listener) {
        mListener = listener;
    }

    public void setShowDelete(boolean showDelete) {
        this.isShowDelete = showDelete;
        notifyDataSetChanged();
    }

    public String getDeicesIds() {
        StringBuffer sb = new StringBuffer();
        for (DeviceShareBean deviceShareBean : mDevices) {
            sb.append(deviceShareBean.getDevId()).append(",");
        }
        return sb.toString();
    }

    public static class HomeManagerDeviceViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivHomeManagerDeviceIcon)
        ImageView ivHomeManagerDeviceIcon;
        @BindView(R.id.tvHomeManagerDeviceName)
        TextView tvHomeManagerDeviceName;
        @BindView(R.id.ivDelete)
        ImageView ivDelete;
        View container;

        public HomeManagerDeviceViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container = view;
        }
    }

    public interface MemberAccessDeviceListener {
        void onDeleteClick(DeviceShareBean device,int position);
        void onItemClick(DeviceShareBean device,int postion);
    }

}
