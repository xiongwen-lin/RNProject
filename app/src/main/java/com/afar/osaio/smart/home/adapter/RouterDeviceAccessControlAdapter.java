package com.afar.osaio.smart.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.bean.RouterDeviceAccessControlInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RouterDeviceAccessControlAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<RouterDeviceAccessControlInfo> routerDeviceAccessControlInfoList = new ArrayList<>();

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(NooieApplication.mCtx);
        View view = layoutInflater.inflate(R.layout.layout_router_device_access, parent, false);
        RouterDeviceAccessControlViewHolder routerDeviceAccessControlViewHolder = new RouterDeviceAccessControlViewHolder(view);
        return routerDeviceAccessControlViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        RouterDeviceAccessControlViewHolder holder1 = (RouterDeviceAccessControlViewHolder)holder;
        RouterDeviceAccessControlInfo accessControlInfo = routerDeviceAccessControlInfoList.get(position);
        holder1.container.setTag(position);

        holder1.deviceName.setText(accessControlInfo.getDeviceName());
        //holder1.ruleTime.setText(accessControlInfo.getRuleTime());
        if (accessControlInfo.getItemType() == 1) {
            holder1.agree.setVisibility(View.VISIBLE);
        } else {
            holder1.agree.setVisibility(View.GONE);
        }
        holder1.refuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int num = (int)holder1.container.getTag();
                if (listener != null) {
                    listener.dealwithItem(num, accessControlInfo.getDeviceMac(), accessControlInfo.getDeviceName(), false, accessControlInfo.getRuleName());
                }
            }
        });

        holder1.agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int num = (int)holder1.container.getTag();
                if (listener != null) {
                    listener.dealwithItem(num,  accessControlInfo.getDeviceMac(), accessControlInfo.getDeviceName(), true, accessControlInfo.getRuleName());
                }
            }
        });
    }

    public void setData(List<RouterDeviceAccessControlInfo> deviceAccessControlInfoList) {
        if (deviceAccessControlInfoList == null || deviceAccessControlInfoList.size() <= 0) {
            return;
        }

        routerDeviceAccessControlInfoList.clear();
        routerDeviceAccessControlInfoList.addAll(deviceAccessControlInfoList);
        notifyDataSetChanged();
    }

    public void removeDevice(int position) {
        if (routerDeviceAccessControlInfoList == null || routerDeviceAccessControlInfoList.size() <= 0) {
            return;
        }

        if (position < 0 || position >= routerDeviceAccessControlInfoList.size()) {
            return;
        }

        routerDeviceAccessControlInfoList.remove(position);
        notifyDataSetChanged();
    }

    private OnAccessControlClick listener;
    public void setOnAccessControlListener(OnAccessControlClick listener) {
        this.listener = listener;
    }

    public interface OnAccessControlClick {
        public void dealwithItem(int position, String deviceMac, String deviceName, boolean isSure, String ruleName);
    }

    @Override
    public int getItemCount() {
        return routerDeviceAccessControlInfoList.size() > 0 ? routerDeviceAccessControlInfoList.size() : 0;
    }

    public class RouterDeviceAccessControlViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivIcon)
        ImageView ivIcon;
        @BindView(R.id.deviceName)
        TextView deviceName;
        @BindView(R.id.ruleTime)
        TextView ruleTime;
        @BindView(R.id.refuse)
        TextView refuse;
        @BindView(R.id.agree)
        TextView agree;

        View container;

        public RouterDeviceAccessControlViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView;
            ButterKnife.bind(this,itemView);
        }
    }
}
