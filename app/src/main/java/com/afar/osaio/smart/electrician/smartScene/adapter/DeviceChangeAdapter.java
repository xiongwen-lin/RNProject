package com.afar.osaio.smart.electrician.smartScene.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.util.ConstantValue;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceChangeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<DeviceBean> mDevices = new ArrayList<>();
    private DeviceItemListener mListener;

    public void setData(List<DeviceBean> devices) {
        mDevices.clear();
        mDevices.addAll(devices);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_scene_device, viewGroup, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof DeviceViewHolder) {
            final DeviceViewHolder holder = (DeviceViewHolder) viewHolder;
            final DeviceBean deviceBean = mDevices.get(i);

            if (deviceBean != null) {
                DrawableCrossFadeFactory drawableCrossFadeFactory = new DrawableCrossFadeFactory.Builder(ConstantValue.DURATION_MILLIS).setCrossFadeEnabled(true).build();
                Glide.with(NooieApplication.mCtx)
                        .load(deviceBean.getIconUrl())
                        .apply(new RequestOptions().placeholder(R.drawable.home_plug_icon).error(R.drawable.home_plug_icon))
                        .transition(DrawableTransitionOptions.with(drawableCrossFadeFactory))
                        .into(holder.ivIcon);
                holder.tvName.setText(deviceBean.getName());
            }

            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemClick(deviceBean);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvName)
        TextView tvName;
        @BindView(R.id.ivIcon)
        ImageView ivIcon;
        View container;

        public DeviceViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container = view;
        }
    }

    public void setListener(DeviceItemListener listener) {
        mListener = listener;
    }

    public interface DeviceItemListener {
        void onItemClick(DeviceBean device);
    }
}
