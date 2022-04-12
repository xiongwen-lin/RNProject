package com.afar.osaio.smart.electrician.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.tuya.smart.home.sdk.bean.SharedUserInfoBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * DeviceShareUsersAdapter
 *
 * @author Administrator
 * @date 2019/5/14
 */
public class DeviceShareUsersAdapter extends RecyclerView.Adapter<DeviceShareUsersAdapter.HomeManagerMemberViewHolder> {

    public static final String SHARE_DEFAULT_NAME = "注册用户";
    private List<SharedUserInfoBean> mShareUsers = new ArrayList<>();
    private DeviceShareUserListListener mListener;

    @Override
    public HomeManagerMemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_device_share_users, parent, false);
        return new HomeManagerMemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HomeManagerMemberViewHolder holder, int position) {
        final SharedUserInfoBean sharedUserInfoBean = mShareUsers.get(position);
        Glide.with(NooieApplication.mCtx)
                .load(sharedUserInfoBean.getIconUrl())
                .apply(new RequestOptions().placeholder(R.drawable.user).error(R.drawable.user).centerCrop())
                .into(holder.ivshareUserImg);

        if (!TextUtils.isEmpty(sharedUserInfoBean.getRemarkName()) && !sharedUserInfoBean.getRemarkName().equals(SHARE_DEFAULT_NAME)) {
            holder.tvShareUserName.setText(sharedUserInfoBean.getRemarkName());
        } else {
            holder.tvShareUserName.setText(sharedUserInfoBean.getUserName());
        }

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(sharedUserInfoBean);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mShareUsers == null ? 0 : mShareUsers.size();
    }

    public void setData(List<SharedUserInfoBean> shareUsers) {
        mShareUsers.clear();
        mShareUsers.addAll(shareUsers);
        notifyDataSetChanged();
    }

    public void setListener(DeviceShareUserListListener listener) {
        mListener = listener;
    }

    public static class HomeManagerMemberViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivshareUserImg)
        ImageView ivshareUserImg;
        @BindView(R.id.tvShareUserName)
        TextView tvShareUserName;
        View container;

        public HomeManagerMemberViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container = view;
        }

    }

    public interface DeviceShareUserListListener {
        void onItemClick(SharedUserInfoBean sharedUserInfoBean);
    }

}
