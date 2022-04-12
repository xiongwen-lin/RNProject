package com.afar.osaio.smart.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.DeviceTypeInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceTypeAdapter extends RecyclerView.Adapter<DeviceTypeAdapter.DeviceTypeViewHolder> {

    List<DeviceTypeInfo> deviceTypeList = new ArrayList<>();

    @NonNull
    @Override
    public DeviceTypeAdapter.DeviceTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(NooieApplication.mCtx);
        View view = inflater.inflate(R.layout.layout_device_type, parent,false);
        DeviceTypeViewHolder deviceTypeViewHolder = new DeviceTypeViewHolder(view);
        return deviceTypeViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceTypeAdapter.DeviceTypeViewHolder holder, int position) {
        DeviceTypeViewHolder deviceTypeViewHolder = holder;
        DeviceTypeInfo deviceType = deviceTypeList.get(position);
        deviceTypeViewHolder.container.setTag(position);

        deviceTypeViewHolder.deviceType.setText(deviceType.getDeviceType());
        deviceTypeViewHolder.deviceType.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, deviceType.isSelect() ? R.color.theme_text_color : R.color.black_4D010C11));
        deviceTypeViewHolder.viewLine.setVisibility(deviceType.isSelect() ? View.VISIBLE : View.GONE);
        /*if (deviceType.isSelect()) {
            deviceTypeViewHolder.deviceType.setBackgroundResource(R.drawable.bg_select_device_type2);
        } else {
            deviceTypeViewHolder.deviceType.setBackgroundResource(R.drawable.bg_select_device_type);
        }*/

        deviceTypeViewHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = (Integer) view.getTag();
                if (listener != null) listener.onSelectItem(pos);
            }
        });

    }

    @Override
    public int getItemCount() {
        return deviceTypeList.size() > 0 ? deviceTypeList.size() : 0;
    }

    public void setDeviceTypeInfo (List<DeviceTypeInfo> typeList) {
        if (typeList == null || typeList.size() <= 0 || deviceTypeList == null) {
            return;
        }

        if (deviceTypeList.size() > 0) {
            deviceTypeList.clear();
        }

        deviceTypeList.addAll(typeList);
        notifyDataSetChanged();
    }

    public void setSelectDeviceType (int position) {
        if (position < 0 || position >= deviceTypeList.size()) {
            return;
        }

        for (int i = 0; i < deviceTypeList.size(); i++) {
            if (i == position) {
                deviceTypeList.get(i).setSelect(true);
            } else {
                deviceTypeList.get(i).setSelect(false);
            }
        }

        notifyDataSetChanged();
    }

    private OnSetSelectDeviceTypeListener listener;
    public void setDeviceTypeSelectListener (OnSetSelectDeviceTypeListener onSetSelectDeviceTypeListener) {
        listener = onSetSelectDeviceTypeListener;
    }
    public interface OnSetSelectDeviceTypeListener {
        void onSelectItem(int position);
    }

    public class DeviceTypeViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.deviceType)
        TextView deviceType;
        @BindView(R.id.viewLine)
        View viewLine;

        View container;

        public DeviceTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            container = itemView;
        }
    }
}
