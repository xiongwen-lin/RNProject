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
import com.tuya.smart.home.sdk.bean.MemberBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * HomeOwnerAdapter
 *
 * @author Administrator
 * @date 2019/3/13
 */
public class HomeOwnerAdapter extends RecyclerView.Adapter<HomeOwnerAdapter.HomeManagerMemberViewHolder> {

    private List<MemberBean> mMembers = new ArrayList<>();
    private HomeOwnerListener mListener;

    @Override
    public HomeManagerMemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_home_owner, parent, false);
        return new HomeManagerMemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(HomeManagerMemberViewHolder holder, int position) {
        final MemberBean member = mMembers.get(position);
        Glide.with(NooieApplication.mCtx)
                .load(member.getHeadPic())
                .apply(new RequestOptions().placeholder(R.drawable.user).error(R.drawable.user).centerCrop())
                .into(holder.ivIcon);

        holder.tvName.setText(member.getNickName());

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(member);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMembers.size();
    }

    public void setData(List<MemberBean> members) {
        mMembers.clear();
        mMembers.addAll(members);
        notifyDataSetChanged();
    }

    public void setListener(HomeOwnerListener listener) {
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

    public interface HomeOwnerListener {
        void onItemClick(MemberBean member);
    }

}

