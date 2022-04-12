package com.afar.osaio.smart.lpipc.adapter;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.adapter.BaseAdapter;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.lpipc.adapter.listener.GatewaySubDeviceListener;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GatewaySubDeviceAdapter extends BaseAdapter<BindDevice, GatewaySubDeviceListener, GatewaySubDeviceAdapter.GatewaySubDeviceVH> {

    @Override
    public GatewaySubDeviceVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GatewaySubDeviceVH(createVHView(R.layout.item_gateway_sub_device, parent));
    }

    @Override
    public void onBindViewHolder(GatewaySubDeviceVH holder, int position) {
        BindDevice device = mDatas.get(position);
        holder.ivLabelIcon.setImageResource(R.drawable.sub_device_icon);
        holder.tvLabelTitle.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.theme_text_color));
        holder.tvLabelTitle.setText(device.getName());

        holder.vGatewayContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onDeviceItemClick(device);
                }
            }
        });
    }

    public static class GatewaySubDeviceVH extends RecyclerView.ViewHolder {

        @BindView(R.id.vGatewayContainer)
        View vGatewayContainer;
        @BindView(R.id.tvLabelTitle)
        TextView tvLabelTitle;
        @BindView(R.id.ivLabelIcon)
        ImageView ivLabelIcon;

        public GatewaySubDeviceVH(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
