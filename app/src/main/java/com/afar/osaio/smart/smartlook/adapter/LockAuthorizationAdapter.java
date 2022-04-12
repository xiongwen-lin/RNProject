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
import com.nooie.sdk.db.entity.LockAuthorizationEntity;
import com.nooie.common.utils.collection.CollectionUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LockAuthorizationAdapter extends RecyclerView.Adapter<LockAuthorizationAdapter.LockAuthorizationVH> {

    private List<LockAuthorizationEntity> mDatas;
    private LockAuthorizationListener mListener;

    @Override
    public LockAuthorizationVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_lock_authorization, parent, false);
        return new LockAuthorizationVH(view);
    }

    @Override
    public void onBindViewHolder(LockAuthorizationVH holder, int position) {
        LockAuthorizationEntity lockAuthorization = mDatas.get(position);
        String name = TextUtils.isEmpty(lockAuthorization.getName()) ? NooieApplication.mCtx.getString(R.string.lock_authorization_no_name) : lockAuthorization.getName();
        holder.tvAuthorizationTitle.setText(name);
        holder.tvAuthorizationCode.setText(lockAuthorization.getCode());
        holder.vLockAuthorizationContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(lockAuthorization);
                }
            }
        });

        holder.btnDeleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemDeleteClick(lockAuthorization);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return CollectionUtil.safeFor(mDatas).size();
    }

    public void setData(List<LockAuthorizationEntity> datas) {
        if (mDatas == null) {
            mDatas = new ArrayList<>();
        }

        mDatas.clear();
        mDatas.addAll(CollectionUtil.safeFor(datas));
        notifyDataSetChanged();
    }

    public void removeData(LockAuthorizationEntity lockAuthorizationEntity) {
        if (lockAuthorizationEntity == null || TextUtils.isEmpty(lockAuthorizationEntity.getCode()) || CollectionUtil.isEmpty(mDatas)) {
            return;
        }
        Iterator<LockAuthorizationEntity> iterator = mDatas.iterator();
        while (iterator.hasNext()) {
            LockAuthorizationEntity authorizationEntity = iterator.next();
            if (authorizationEntity != null && lockAuthorizationEntity.getCode().equalsIgnoreCase(authorizationEntity.getCode())) {
                iterator.remove();
                notifyDataSetChanged();
                break;
            }
        }
    }

    public void setListener(LockAuthorizationListener listener) {
        mListener = listener;
    }

    public static class LockAuthorizationVH extends RecyclerView.ViewHolder {

        @BindView(R.id.vLockAuthorizationContainer)
        View vLockAuthorizationContainer;
        @BindView(R.id.ivAuthorizationIcon)
        ImageView ivAuthorizationIcon;
        @BindView(R.id.tvAuthorizationTitle)
        TextView tvAuthorizationTitle;
        @BindView(R.id.tvAuthorizationCode)
        TextView tvAuthorizationCode;
        @BindView(R.id.btnDeleteIcon)
        ImageView btnDeleteIcon;

        public LockAuthorizationVH(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public interface LockAuthorizationListener {
        void onItemClick(LockAuthorizationEntity lockRecordEntity);

        void onItemDeleteClick(LockAuthorizationEntity lockRecordEntity);
    }

}
