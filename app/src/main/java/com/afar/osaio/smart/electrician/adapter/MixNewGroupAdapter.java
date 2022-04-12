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
import com.afar.osaio.smart.electrician.bean.MixDeviceBean;
import com.afar.osaio.smart.electrician.manager.GroupHelper;
import com.afar.osaio.util.ConstantValue;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.nooie.common.utils.collection.CollectionUtil;
import com.tuya.smart.sdk.bean.GroupBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * DeviceAdapter
 *
 * @author Administrator
 * @date 2019/3/7
 */
public class MixNewGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int type;
    private static final int VIEW_GRID = 0x01;
    private static final int VIEW_LINEAR = 0x02;
    private boolean isOpen;

    private List<MixDeviceBean> mDevices = new ArrayList<>();
    private GroupItemListener mListener;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_GRID) {
            View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_group_grid, parent, false);
            return new GridViewHolder(view);
        } else {
            View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_group_linear, parent, false);
            return new LinearViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof GridViewHolder) {
            final GridViewHolder holder = (GridViewHolder) viewHolder;
            final MixDeviceBean mixDevice = mDevices.get(position);
            if (mixDevice.isGroupBean()) {
                showGroupGridItem(holder, mixDevice);
            }

            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemClick(mixDevice);
                    }
                }
            });
        } else if (viewHolder instanceof LinearViewHolder) {
            final LinearViewHolder holder = (LinearViewHolder) viewHolder;
            final MixDeviceBean mixDevice = mDevices.get(position);
            if (mixDevice.isGroupBean()) {
                showGroupLinearItem(holder, mixDevice);
            }

            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemClick(mixDevice);
                    }
                }
            });
        }
    }

    private void showGroupLinearItem(final LinearViewHolder holder, final MixDeviceBean mixDevice) {
        GroupBean group = mixDevice.getGroupBean();

        if (group != null) {
            DrawableCrossFadeFactory drawableCrossFadeFactory = new DrawableCrossFadeFactory.Builder(ConstantValue.DURATION_MILLIS).setCrossFadeEnabled(true).build();
            Glide.with(NooieApplication.mCtx)
                    .load(group.getIconUrl())
                    .apply(new RequestOptions().placeholder(R.drawable.ic_list_placeholder).error(R.drawable.ic_list_placeholder))
                    .transition(DrawableTransitionOptions.with(drawableCrossFadeFactory))
                    .into(holder.ivDeviceIcon);

            holder.tvDeviceName.setText(group.getName());


            holder.backgroundOffline.setVisibility(View.GONE);
            holder.ivDeviceSwitch.setVisibility(View.VISIBLE);
            //boolean isOpen = (boolean) group.getDps().get(ConstantValue.DP_ID_SWITCH_ON);
            if (GroupHelper.getInstance().isLampGroup(group) && !GroupHelper.getInstance().isOldDPLamp(group)) {
                isOpen = (boolean) group.getDps().get(ConstantValue.LAMP_DP_ID_SWITCH_ON);
            } else {
                isOpen = (boolean) group.getDps().get(ConstantValue.DP_ID_SWITCH_ON);
            }

            if (isOpen) {
                holder.ivDeviceSwitch.setImageResource(R.drawable.ic_public_switch_on);
            } else {
                holder.ivDeviceSwitch.setImageResource(R.drawable.ic_public_switch_off);
            }

            holder.ivDeviceSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onSwitchClick(mixDevice, !isOpen);
                    }
                }
            });
        }
    }

    private void showGroupGridItem(final GridViewHolder holder, final MixDeviceBean mixDevice) {
        GroupBean group = mixDevice.getGroupBean();

        if (group != null) {
            DrawableCrossFadeFactory drawableCrossFadeFactory = new DrawableCrossFadeFactory.Builder(ConstantValue.DURATION_MILLIS).setCrossFadeEnabled(true).build();
            Glide.with(NooieApplication.mCtx)
                    .load(group.getIconUrl())
                    .apply(new RequestOptions().placeholder(R.drawable.ic_list_placeholder).error(R.drawable.ic_list_placeholder))
                    .transition(DrawableTransitionOptions.with(drawableCrossFadeFactory))
                    .into(holder.ivDeviceIcon);

            holder.tvDeviceName.setText(group.getName());


            holder.backgroundOffline.setVisibility(View.GONE);
            holder.ivDeviceSwitch.setVisibility(View.VISIBLE);
            //boolean isOpen = (boolean) group.getDps().get(ConstantValue.DP_ID_SWITCH_ON);
            if (GroupHelper.getInstance().isLampGroup(group) && !GroupHelper.getInstance().isOldDPLamp(group)) {
                isOpen = (boolean) group.getDps().get(ConstantValue.LAMP_DP_ID_SWITCH_ON);
            } else {
                isOpen = (boolean) group.getDps().get(ConstantValue.DP_ID_SWITCH_ON);
            }


            if (isOpen) {
                holder.ivDeviceSwitch.setImageResource(R.drawable.ic_public_switch_on);
            } else {
                holder.ivDeviceSwitch.setImageResource(R.drawable.ic_public_switch_off);
            }

            holder.ivDeviceSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onSwitchClick(mixDevice, !isOpen);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return CollectionUtil.isEmpty(mDevices) ? 0 : mDevices.size();
    }

    @Override
    public int getItemViewType(int position) {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<MixDeviceBean> getData() {
        return mDevices;
    }

    public void setData(List<MixDeviceBean> devices) {
        mDevices.clear();
        mDevices.addAll(devices);
        notifyDataSetChanged();
    }

    public void updateItemChange(int position, MixDeviceBean mixDeviceBean) {
        mDevices.set(position, mixDeviceBean);
        notifyItemChanged(position);
    }

    public void itemRemoved(int positon) {
        mDevices.remove(positon);
        notifyDataSetChanged();
    }

    public void setListener(GroupItemListener listener) {
        mListener = listener;
    }

    public static class GridViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivDeviceIcon)
        ImageView ivDeviceIcon;
        @BindView(R.id.tvDeviceName)
        TextView tvDeviceName;
        @BindView(R.id.ivDeviceSwitch)
        ImageView ivDeviceSwitch;
        View container;

        @BindView(R.id.background_offline)
        View backgroundOffline;

        public GridViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container = view;
        }
    }

    public static class LinearViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivDeviceIcon)
        ImageView ivDeviceIcon;
        @BindView(R.id.tvDeviceName)
        TextView tvDeviceName;
        @BindView(R.id.ivDeviceSwitch)
        ImageView ivDeviceSwitch;
        View container;
        @BindView(R.id.background_offline)
        View backgroundOffline;

        public LinearViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container = view;
        }
    }

    public interface GroupItemListener {
        void onItemClick(MixDeviceBean device);

        void onSwitchClick(MixDeviceBean device, boolean isChecked);
    }
}

