package com.afar.osaio.smart.smartlook.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.nooie.sdk.db.entity.BleDeviceEntity;
import com.nooie.common.utils.collection.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LockAccountAdapter extends RecyclerView.Adapter<LockAccountAdapter.LockAccountVH> {

    private List<BleDeviceEntity> mDatas;
    private LockAccountListener mListener;

    @Override
    public LockAccountVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_lock_account, parent, false);
        return new LockAccountVH(view);
    }

    @Override
    public void onBindViewHolder(LockAccountVH holder, int position) {
        BleDeviceEntity bleDeviceEntity = mDatas.get(position);
        String name = TextUtils.isEmpty(bleDeviceEntity.getName()) ? NooieApplication.mCtx.getString(R.string.lock_authorization_no_name) : bleDeviceEntity.getName();
        holder.tvDeviceName.setText(name);
        holder.tvAccountPhone.setText(bleDeviceEntity.getPhone());
        holder.tvAccountPassword.setText(bleDeviceEntity.getPassword());
        holder.vLockAccountContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(bleDeviceEntity);
                }
            }
        });

        holder.btnDeleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemDeleteClick(bleDeviceEntity);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return CollectionUtil.safeFor(mDatas).size();
    }

    public void setData(List<BleDeviceEntity> datas) {
        if (mDatas == null) {
            mDatas = new ArrayList<>();
        }

        mDatas.clear();
        mDatas.addAll(CollectionUtil.safeFor(datas));
        notifyDataSetChanged();
    }

    public void setListener(LockAccountListener listener) {
        mListener = listener;
    }

    public static class LockAccountVH extends RecyclerView.ViewHolder {

        @BindView(R.id.vLockAccountContainer)
        View vLockAccountContainer;
        @BindView(R.id.tvDeviceNameLabel)
        TextView tvDeviceNameLabel;
        @BindView(R.id.tvDeviceName)
        TextView tvDeviceName;
        @BindView(R.id.tvAccountPhoneLabel)
        TextView tvAccountPhoneLabel;
        @BindView(R.id.tvAccountPhone)
        TextView tvAccountPhone;
        @BindView(R.id.tvAccountPasswordLabel)
        TextView tvAccountPasswordLabel;
        @BindView(R.id.tvAccountPassword)
        TextView tvAccountPassword;
        @BindView(R.id.btnDeleteIcon)
        ImageView btnDeleteIcon;

        public LockAccountVH(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public interface LockAccountListener {
        void onItemClick(BleDeviceEntity bleDeviceEntity);

        void onItemDeleteClick(BleDeviceEntity bleDeviceEntity);
    }

}
