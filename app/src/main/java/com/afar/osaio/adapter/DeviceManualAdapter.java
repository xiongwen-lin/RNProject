package com.afar.osaio.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.UserManualBean;
import com.nooie.common.utils.collection.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * DeviceManualAdapter
 *
 * @author Administrator
 * @date 2019/6/19
 */
public class DeviceManualAdapter extends RecyclerView.Adapter<DeviceManualAdapter.DevcieManualeViewHolder> {

    List<UserManualBean> mData = new ArrayList<>();
    private OnManualItemClickListener mListener;

    @Override
    public DevcieManualeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_normal_list, parent, false);
        return new DevcieManualeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DevcieManualeViewHolder holder, int position) {
        final UserManualBean userManualBean = mData.get(position);

        holder.tvNormalItemTitle.setText(userManualBean.getDeviceAlias());
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(userManualBean);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return CollectionUtil.isNotEmpty(mData) ? mData.size() : 0;
    }

    public void setData(List<UserManualBean> data) {
        if (mData != null && CollectionUtil.isNotEmpty(data)) {
            mData.clear();
            mData.addAll(data);
        }
    }

    public void setListener(OnManualItemClickListener listener) {
        mListener = listener;
    }

    public static class DevcieManualeViewHolder extends RecyclerView.ViewHolder {

        View container;
        @BindView(R.id.tvNormalItemTitle)
        TextView tvNormalItemTitle;

        public DevcieManualeViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container = view;
        }
    }

    public interface OnManualItemClickListener {
        void onItemClick(UserManualBean userManualBean);
    }
}
