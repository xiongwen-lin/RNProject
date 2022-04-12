package com.afar.osaio.smart.lpipc.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.adapter.BaseAdapter;
import com.afar.osaio.bean.LpSuitAddDeviceBean;
import com.afar.osaio.smart.lpipc.adapter.listener.LpSuitAddDeviceListener;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LpSuitAddDeviceAdapter extends BaseAdapter<LpSuitAddDeviceBean, LpSuitAddDeviceListener, LpSuitAddDeviceAdapter.GLpSuitAddDeviceVH> {

    @Override
    public GLpSuitAddDeviceVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GLpSuitAddDeviceVH(createVHView(R.layout.item_lp_suit_add, parent));
    }

    @Override
    public void onBindViewHolder(GLpSuitAddDeviceVH holder, int position) {
        LpSuitAddDeviceBean device = mDatas.get(position);
        holder.ivLpSuitAddDeviceIcon.setImageResource(device.getIconRes());
        holder.tvLpSuitAddDeviceTitle.setText(device.getTitle());
        holder.tvLpSuitAddDeviceDesc.setText(device.getDesc());

        holder.vLpSuitAddDeviceContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(device);
                }
            }
        });
    }

    public static class GLpSuitAddDeviceVH extends RecyclerView.ViewHolder {

        @BindView(R.id.vLpSuitAddDeviceContainer)
        View vLpSuitAddDeviceContainer;
        @BindView(R.id.tvLpSuitAddDeviceTitle)
        TextView tvLpSuitAddDeviceTitle;
        @BindView(R.id.tvLpSuitAddDeviceDesc)
        TextView tvLpSuitAddDeviceDesc;
        @BindView(R.id.ivLpSuitAddDeviceIcon)
        ImageView ivLpSuitAddDeviceIcon;

        public GLpSuitAddDeviceVH(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
