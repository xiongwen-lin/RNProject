package com.afar.osaio.smart.device.adapter;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.DeviceSharedUserInfo;
import com.nooie.common.base.GlobalData;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by victor on 2018/7/3
 * Email is victor.qiao.0604@gmail.com
 */
public class AddPersonAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int EMPTY_TYPE = 0x01;
    private static final int NORMAL_TYPE = 0x02;

    private OnClickUserItemListener mOnClickUserItemListener;

    public void setOnClickUserItemListener(OnClickUserItemListener mOnClickUserItemListener) {
        this.mOnClickUserItemListener = mOnClickUserItemListener;
    }

    public interface OnClickUserItemListener {
        void onClickRemoveItem(View view, DeviceSharedUserInfo info);

        void onLongClickItem(View view, DeviceSharedUserInfo info);
    }

    private String mCurrentUser;
    private List<DeviceSharedUserInfo> mUserInfos;

    public AddPersonAdapter(@NonNull List<DeviceSharedUserInfo> mUserInfos) {
        this.mUserInfos = mUserInfos;
        mCurrentUser = GlobalData.getInstance().getAccount();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        if (type == EMPTY_TYPE) {
            View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.shared_user_empty, viewGroup, false);
            EmptyViewHolder emptyViewHolder = new EmptyViewHolder(view);
            return emptyViewHolder;
        } else {
            View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_my_share, viewGroup, false);
            AddPersonViewHolder viewHolder = new AddPersonViewHolder(view);
            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof AddPersonViewHolder) {
            AddPersonViewHolder addPersonViewHolder = (AddPersonViewHolder) viewHolder;

            String alias = mUserInfos.get(position).getUserAccount();
            addPersonViewHolder.tvAccount.setText(alias);
            if (mCurrentUser.equalsIgnoreCase(alias)) {
                // Owner
                addPersonViewHolder.tvAction.setText(R.string.camera_share_owner);
                addPersonViewHolder.tvAction.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.theme_text_color));

                addPersonViewHolder.container.setOnLongClickListener(null);
                addPersonViewHolder.tvAction.setOnClickListener(null);
            } else {
                // Other
                addPersonViewHolder.tvAction.setText(R.string.camera_share_remove);
                addPersonViewHolder.tvAction.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.theme_green));

                addPersonViewHolder.container.setTag(mUserInfos.get(position));
                addPersonViewHolder.container.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (mOnClickUserItemListener != null)
                            mOnClickUserItemListener.onLongClickItem(view, (DeviceSharedUserInfo) view.getTag());
                        return true;
                    }
                });

                addPersonViewHolder.tvAction.setTag(mUserInfos.get(position));
                addPersonViewHolder.tvAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mOnClickUserItemListener != null)
                            mOnClickUserItemListener.onClickRemoveItem(view, (DeviceSharedUserInfo) view.getTag());
                    }
                });
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mUserInfos.size() == 0)
            return EMPTY_TYPE;
        else
            return NORMAL_TYPE;
    }

    @Override
    public int getItemCount() {
        return mUserInfos.size() == 0 ? 1 : mUserInfos.size();
    }

    class AddPersonViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvAccount)
        TextView tvAccount;
        @BindView(R.id.tvAction)
        TextView tvAction;

        View container;

        public AddPersonViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            container = itemView;
        }
    }

    class EmptyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvContent)
        TextView tvContent;

        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
