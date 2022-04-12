package com.afar.osaio.smart.electrician.adapter;

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
 * HomeShareUserAdapter
 *
 * @author Administrator
 * @date 2019/3/13
 */
public class HomeShareUserAdapter extends RecyclerView.Adapter<HomeShareUserAdapter.HomeManagerMemberViewHolder> {

    private List<SharedUserInfoBean> mShareUsers = new ArrayList<>();
    private HomeShareUserListener mListener;

    @Override
    public HomeManagerMemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_home_owner, parent, false);
        return new HomeManagerMemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HomeManagerMemberViewHolder holder, int position) {
        final SharedUserInfoBean shareUserInfo = mShareUsers.get(position);
        Glide.with(NooieApplication.mCtx)
                .load(shareUserInfo.getIconUrl())
                .apply(new RequestOptions().placeholder(R.drawable.user).error(R.drawable.user).centerCrop())
                .into(holder.ivIcon);

        holder.tvName.setText(shareUserInfo.getRemarkName());

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(shareUserInfo);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mShareUsers.size();
    }

    public void setData(List<SharedUserInfoBean> sharedUserInfoBeanList) {
        mShareUsers.clear();
        mShareUsers.addAll(sharedUserInfoBeanList);
        notifyDataSetChanged();
    }

    public void setListener(HomeShareUserListener listener) {
        mListener = listener;
    }

    public static class HomeManagerMemberViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivIcon)
        ImageView ivIcon;
        @BindView(R.id.tvName)
        TextView tvName;
        View container;

        public HomeManagerMemberViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            container = view;
        }

    }

    public interface HomeShareUserListener {
        void onItemClick(SharedUserInfoBean sharedUserInfoBean);
    }

}

