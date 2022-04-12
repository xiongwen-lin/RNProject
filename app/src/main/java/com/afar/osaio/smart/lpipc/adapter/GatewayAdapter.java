package com.afar.osaio.smart.lpipc.adapter;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.adapter.BaseAdapter;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.lpipc.adapter.listener.GatewayListener;
import com.afar.osaio.smart.lpipc.adapter.listener.GatewaySubDeviceListener;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.api.network.base.bean.entity.GatewayDevice;

import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GatewayAdapter extends BaseAdapter<GatewayDevice, GatewayListener, GatewayAdapter.GatewayVH> {

    @Override
    public GatewayVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GatewayVH(createVHView(R.layout.item_gateway, parent));
    }

    @Override
    public void onBindViewHolder(GatewayVH holder, int position) {
        GatewayDevice gatewayDevice = mDatas.get(position);
        holder.tvLabelTitle.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.theme_text_color));
        holder.tvLabelTitle.setText(getGatewayTitle(gatewayDevice.getUuid()));
        bindGatewaySubDevice(holder.rcvLpCameraOfGateway, gatewayDevice.getChild());

        holder.vGatewayContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onGatewayItemClick(gatewayDevice);
                }
            }
        });
    }

    private void bindGatewaySubDevice(RecyclerView rcvSubDevice, List<BindDevice> devices) {
        if (rcvSubDevice != null && CollectionUtil.isNotEmpty(devices)) {
            rcvSubDevice.setVisibility(View.VISIBLE);
            LinearLayoutManager layoutManager = new LinearLayoutManager(NooieApplication.mCtx);
            rcvSubDevice.setLayoutManager(layoutManager);
            GatewaySubDeviceAdapter gatewaySubDeviceAdapter = new GatewaySubDeviceAdapter();
            gatewaySubDeviceAdapter.setData(devices);
            gatewaySubDeviceAdapter.setListener(new GatewaySubDeviceListener() {
                @Override
                public void onDeviceItemClick(BindDevice device) {
                    if (device != null && mListener != null) {
                        mListener.onGatewaySubDeviceClick(device);
                    }
                }
            });
            rcvSubDevice.setAdapter(gatewaySubDeviceAdapter);
        } else {
            rcvSubDevice.setVisibility(View.GONE);
        }
    }

    public void removeSubDevice(String pDeviceId, String deviceId) {
        if (TextUtils.isEmpty(pDeviceId) || !TextUtils.isEmpty(deviceId) || CollectionUtil.isEmpty(mDatas)) {
            return;
        }
        for (int i = 0; i < CollectionUtil.size(mDatas); i++) {
            if (mDatas.get(i) != null && pDeviceId.equalsIgnoreCase(mDatas.get(i).getUuid()) && CollectionUtil.isNotEmpty(mDatas.get(i).getChild())) {
                Iterator<BindDevice> deviceIterator = mDatas.get(i).getChild().iterator();
                while (deviceIterator.hasNext()) {
                    BindDevice bindDevice = deviceIterator.next();
                    if (bindDevice != null && deviceId.equalsIgnoreCase(bindDevice.getUuid())) {
                        NooieLog.d("-->> GatewayAdapter removeSubDevice pDeviceId=" + pDeviceId + " deviceId=" + deviceId);
                        deviceIterator.remove();
                        break;
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    private String getGatewayTitle(String deviceId) {
        return new StringBuilder()
                .append(NooieApplication.mCtx.getString(R.string.home_gateway))
                .append("(")
                .append(deviceId)
                .append(")")
                .toString();
    }

    public static class GatewayVH extends RecyclerView.ViewHolder {

        @BindView(R.id.vGatewayContainer)
        View vGatewayContainer;
        @BindView(R.id.tvLabelTitle)
        TextView tvLabelTitle;
        @BindView(R.id.rcvLpCameraOfGateway)
        RecyclerView rcvLpCameraOfGateway;

        public GatewayVH(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
