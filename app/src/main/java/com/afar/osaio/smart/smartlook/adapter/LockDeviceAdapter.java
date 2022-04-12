package com.afar.osaio.smart.smartlook.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.adapter.BaseAdapter;
import com.afar.osaio.smart.smartlook.adapter.listener.LockDeviceListener;
import com.nooie.sdk.db.entity.BleDeviceEntity;
import com.nooie.common.widget.ProgressWheel;
import com.suke.widget.SwitchButton;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LockDeviceAdapter extends BaseAdapter<BleDeviceEntity, LockDeviceListener, LockDeviceAdapter.LockDeviceVH> {

    @Override
    public LockDeviceVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new LockDeviceVH(createVHView(R.layout.item_lock_device, parent));
    }

    @Override
    public void onBindViewHolder(LockDeviceVH holder, int position) {
        BleDeviceEntity bleDeviceEntity = mDatas.get(position);
        holder.tvDeviceName.setText(bleDeviceEntity.getName());
        holder.pbBattery.stopSpinning();
        double progress = 360.0 * (bleDeviceEntity.getBattery() / 100.0);
        holder.pbBattery.setProgress((int)progress);
        StringBuilder progressSb = new StringBuilder();
        progressSb.append(bleDeviceEntity.getBattery());
        progressSb.append("%");
        holder.pbBattery.setText(progressSb.toString());
        holder.vLockDeviceContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(bleDeviceEntity);
                }
            }
        });
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

    public static class LockDeviceVH extends RecyclerView.ViewHolder {

        @BindView(R.id.vLockDeviceContainer)
        View vLockDeviceContainer;
        @BindView(R.id.ivDeviceIcon)
        ImageView ivDeviceIcon;
        @BindView(R.id.tvDeviceName)
        TextView tvDeviceName;
        @BindView(R.id.pbBattery)
        ProgressWheel pbBattery;
        @BindView(R.id.sbSwitchSleep)
        SwitchButton sbSwitchSleep;

        public LockDeviceVH(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
