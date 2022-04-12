package com.afar.osaio.smart.home.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.adapter.BaseAdapter;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.home.adapter.listener.BleApDeviceListener;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.widget.RoundedImageView.RoundedImageView;
import com.nooie.sdk.bean.IpcType;
import com.nooie.sdk.db.entity.BleApDeviceEntity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BleApDeviceAdapter extends BaseAdapter<BleApDeviceEntity, BleApDeviceListener, RecyclerView.ViewHolder> {

    private boolean mItemDragEnable = false;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ConstantValue.DRAG_CAMERA_TYPE) {
            return new DeviceDragViewHolder(createVHView(R.layout.item_device_drag, parent));
        } else {
            return new BleApDeviceVH(createVHView(R.layout.item_ble_ap_device, parent));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        BleApDeviceEntity data = getDataByPosition(position);
        if (data == null || holder == null) {
            return;
        }
        if (holder instanceof DeviceDragViewHolder) {
            DeviceDragViewHolder dragVH = (DeviceDragViewHolder) holder;
            dragVH.tvDeviceName.setText(data.getName());
        } else if (holder instanceof BleApDeviceVH) {
            BleApDeviceVH deviceVH = (BleApDeviceVH) holder;
            deviceVH.tvName.setText(data.getName());
            deviceVH.ivDeviceNamePoint.setImageResource(R.drawable.offline_circle);
            deviceVH.containerOffline.setVisibility(View.VISIBLE);
            if (NooieDeviceHelper.mergeIpcType(data.getModel()) == IpcType.MC120) {
                deviceVH.tvRefresh.setVisibility(View.GONE);
                deviceVH.tvRefresh.setOnClickListener(null);
            } else {
                deviceVH.tvRefresh.setVisibility(View.VISIBLE);
                deviceVH.tvRefresh.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mListener != null) {
                            mListener.onAccessClick(data);
                        }
                    }
                });
            }
            deviceVH.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemClickListener(data);
                    }
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mItemDragEnable) {
            return ConstantValue.DRAG_CAMERA_TYPE;
        } else {
            return super.getItemViewType(position);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);

            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return 2;
                }
            });
        }
    }

    public void setItemDragEnable(boolean enable) {
        mItemDragEnable = enable;
        notifyDataSetChanged();
    }

    public boolean isItemDragEnable() {
        return mItemDragEnable;
    }

    public void toggleItemDrag() {
        setItemDragEnable(!mItemDragEnable);
    }

    public static class BleApDeviceVH extends RecyclerView.ViewHolder {

        @BindView(R.id.ivDeviceNamePoint)
        ImageView ivDeviceNamePoint;
        @BindView(R.id.ivThumbnail)
        ImageView ivThumbnail;
        @BindView(R.id.ivThumbnailCover)
        RoundedImageView ivThumbnailCover;
        @BindView(R.id.tvName)
        TextView tvName;

        @BindView(R.id.containerOffline)
        ConstraintLayout containerOffline;
        @BindView(R.id.tvOfflineTip)
        TextView tvOfflineTip;
        @BindView(R.id.tvRefresh)
        TextView tvRefresh;

        @BindView(R.id.container)
        View container;

        public BleApDeviceVH(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public static class DeviceDragViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.itemDeviceDragContainer)
        View container;
        @BindView(R.id.tvDeviceName)
        TextView tvDeviceName;
        @BindView(R.id.ivDeviceThumb)
        ImageView ivDeviceThumb;
        @BindView(R.id.vDeviceShadowBottom)
        public View vDeviceShadowBottom;

        public DeviceDragViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
