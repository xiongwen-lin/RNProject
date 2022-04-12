package com.afar.osaio.smart.smartlook.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.nooie.sdk.db.entity.LockRecordEntity;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.time.DateTimeUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LockRecordAdapter extends RecyclerView.Adapter<LockRecordAdapter.LockRecordVH> {

    private List<LockRecordEntity> mDatas;
    private LockRecordListener mListener;

    @Override
    public LockRecordVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_lock_record, parent, false);
        return new LockRecordVH(view);
    }

    @Override
    public void onBindViewHolder(LockRecordVH holder, int position) {
        LockRecordEntity lockRecord = mDatas.get(position);
        holder.tvRecordTitle.setText(lockRecord.getName());
        holder.tvRecordTime.setText(DateTimeUtil.getTimeString(lockRecord.getTime(), DateTimeUtil.PATTERN_YMD_HMS_1));
        holder.vLockRecordContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(lockRecord);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return CollectionUtil.safeFor(mDatas).size();
    }

    public void setData(List<LockRecordEntity> datas) {
        if (mDatas == null) {
            mDatas = new ArrayList<>();
        }

        mDatas.clear();
        mDatas.addAll(CollectionUtil.safeFor(datas));
        notifyDataSetChanged();
    }

    public void setListener(LockRecordListener listener) {
        mListener = listener;
    }

    public static class LockRecordVH extends RecyclerView.ViewHolder {

        @BindView(R.id.vLockRecordContainer)
        View vLockRecordContainer;
        @BindView(R.id.ivRecordIcon)
        ImageView ivRecordIcon;
        @BindView(R.id.tvRecordTitle)
        TextView tvRecordTitle;
        @BindView(R.id.tvRecordTime)
        TextView tvRecordTime;

        public LockRecordVH(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public interface LockRecordListener {
        void onItemClick(LockRecordEntity lockRecordEntity);
    }

}
