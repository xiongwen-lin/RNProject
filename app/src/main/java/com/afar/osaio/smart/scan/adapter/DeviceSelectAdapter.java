package com.afar.osaio.smart.scan.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.afar.osaio.R;
import com.nooie.sdk.bean.IpcType;
import com.afar.osaio.util.GlideUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceSelectAdapter extends RecyclerView.Adapter<DeviceSelectAdapter.DeviceSelectViewHolder> {
    private Context mContext;
    private List<BindDevice> mDevices = new ArrayList<>();
    private DeviceSelectListener mListener;

    public DeviceSelectAdapter(Context context) {
        mContext = context;
    }

    @Override
    public DeviceSelectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DeviceSelectViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_device_select, parent, false));
    }

    @Override
    public void onBindViewHolder(final DeviceSelectViewHolder holder, int position) {
        int id = position;
        final BindDevice device = mDevices.get(id);
        IpcType ipcType = IpcType.getIpcType(device.getType());
        if (ipcType == IpcType.IPC_720 || ipcType == IpcType.IPC_1080) {
            GlideUtil.loadImageNormal(mContext, R.drawable.nooie_cam, R.drawable.nooie_cam, holder.ivDeviceIcon);
        } else if (ipcType == IpcType.IPC_100) {
            GlideUtil.loadImageNormal(mContext, R.drawable.nooie360_cam, R.drawable.nooie_cam, holder.ivDeviceIcon);
        } else if (ipcType == IpcType.IPC_200) {
            GlideUtil.loadImageNormal(mContext, R.drawable.nooie_outdoor_cam, R.drawable.nooie_cam, holder.ivDeviceIcon);
        }
        holder.tvDeviceName.setText(device.getName());
        holder.container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    holder.vDeviceSelectBg.setVisibility(View.VISIBLE);
                    //holder.tvDeviceName.setTextColor(mContext.getResources().getColor(R.color.theme_blue));
                } else if (event.getAction() == MotionEvent.ACTION_UP && mListener != null) {
                    holder.vDeviceSelectBg.setVisibility(View.GONE);
                    //holder.tvDeviceName.setTextColor(mContext.getResources().getColor(R.color.theme_text));
                    mListener.onItemClick(device);
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    holder.vDeviceSelectBg.setVisibility(View.GONE);
                    //holder.tvDeviceName.setTextColor(mContext.getResources().getColor(R.color.theme_text));
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDevices != null ? mDevices.size() : 0;
    }

    public void setData(List<BindDevice> devices) {
        if (devices != null) {
            mDevices.clear();
            mDevices.addAll(devices);
            notifyDataSetChanged();
        }
    }

    public void addData(BindDevice device) {
        if (mDevices != null && device != null) {
            mDevices.add(device);
            notifyDataSetChanged();
        }
    }

    public void setListener(DeviceSelectListener listener) {
        mListener = listener;
    }

    public static class DeviceSelectViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.ivDeviceIcon)
        ImageView ivDeviceIcon;
        @BindView(R.id.tvDeviceName)
        TextView tvDeviceName;
        @BindView(R.id.vDeviceSelectBg)
        View vDeviceSelectBg;
        View container;

        public DeviceSelectViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container = view;
        }

    }

    public interface DeviceSelectListener {
        void onItemClick(BindDevice device);
    }
}
