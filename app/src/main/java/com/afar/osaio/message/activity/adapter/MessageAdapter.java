package com.afar.osaio.message.activity.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.message.activity.DeviceMessageActivity;
import com.afar.osaio.message.activity.SystemMessageActivity;
import com.afar.osaio.message.bean.InboxBean;
import com.nooie.common.utils.collection.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by victor on 2018/7/9
 * Email is victor.qiao.0604@gmail.com
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    public static final String SYSTEM_MESSAGE_ID = "system_message";
    public static int OPEN_DELETE_MSG = 0;
    public static int CLOSE_DELETE_MSG = 1;

    private Context mCtx;
    private List<InboxBean> mMessage;
    private MessageAdapterListener mListener;
    private int mDeleteState = CLOSE_DELETE_MSG;

    public MessageAdapter(Context mCtx, List<InboxBean> mMessage) {
        this.mCtx = mCtx;
        this.mMessage = mMessage;
    }

    public MessageAdapter(Context mCtx) {
        this.mCtx = mCtx;
        mMessage = new ArrayList<>();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.item_message_new, viewGroup, false);
        MessageViewHolder holder = new MessageViewHolder(view);
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDeleteState == OPEN_DELETE_MSG) {
                    return;
                }
                InboxBean inboxBean = (InboxBean) view.getTag();
                if (inboxBean != null && inboxBean.getId().equals(SYSTEM_MESSAGE_ID)) {
                    SystemMessageActivity.toSystemMessageActivity(mCtx);
                } else if (inboxBean != null) {
                    DeviceMessageActivity.toDeviceMsgActivity(mCtx, inboxBean.getId(), inboxBean.getName());
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, final int i) {
        messageViewHolder.tvContent.setText(mMessage.get(i).getName());
        if (mDeleteState == OPEN_DELETE_MSG) {
            messageViewHolder.ivUnread.setVisibility(View.GONE);
            messageViewHolder.ivNext.setVisibility(View.GONE);
            messageViewHolder.tvUnreadCount.setVisibility(View.VISIBLE);
            messageViewHolder.tvUnreadCount.setText(mCtx.getString(R.string.app_settings_clear));
            messageViewHolder.tvUnreadCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onMessageDelete(mMessage.get(i).getId(), mMessage.get(i).getId());
                    }
                }
            });
        } else {
            messageViewHolder.tvUnreadCount.setVisibility(View.VISIBLE);
            messageViewHolder.ivUnread.setVisibility(View.VISIBLE);
            messageViewHolder.ivNext.setVisibility(View.VISIBLE);
            messageViewHolder.tvUnreadCount.setOnClickListener(null);

            if (mMessage.get(i).getUnreadCount() > 0) {
                // messageViewHolder.tvUnreadCount.setText(String.valueOf(mMessage.get(i).getUnreadCount()));
                //messageViewHolder.tvUnreadCount.setText(R.string.new_message);
                messageViewHolder.ivUnread.setVisibility(View.VISIBLE);
            } else {
                messageViewHolder.tvUnreadCount.setText("");
                messageViewHolder.ivUnread.setVisibility(View.INVISIBLE);
            }
        }
        InboxBean messageItem = mMessage.get(i);
        if (messageItem != null) {
            if (messageItem.getId().equals(SYSTEM_MESSAGE_ID)) {
                messageViewHolder.ivIcon.setImageResource(R.drawable.ic_news_news);
                messageViewHolder.tvUnreadCount.setVisibility(View.INVISIBLE);
            } else {
                messageViewHolder.ivIcon.setImageResource(R.drawable.ic_news_device);
                messageViewHolder.tvUnreadCount.setVisibility(View.VISIBLE);
            }
        }
        messageViewHolder.container.setTag(mMessage.get(i));
    }

    @Override
    public int getItemCount() {
        return CollectionUtil.size(mMessage);
    }

    public void setDeleteState(int state) {
        mDeleteState = state;
        notifyDataSetChanged();
    }

    public void setData(List<InboxBean> dataList) {
        if (mMessage == null) {
            mMessage = new ArrayList<>();
        }
        mMessage.clear();
        mMessage.addAll(CollectionUtil.safeFor(dataList));
        notifyDataSetChanged();
    }

    public void setListener(MessageAdapterListener listener) {
        mListener = listener;
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvContent)
        TextView tvContent;
        @BindView(R.id.tvUnreadCount)
        TextView tvUnreadCount;
        @BindView(R.id.ivUnread)
        ImageView ivUnread;
        @BindView(R.id.ivNext)
        ImageView ivNext;
        @BindView(R.id.ivIcon)
        ImageView ivIcon;
        View container;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            container = itemView;
        }
    }

    public interface MessageAdapterListener {
        void onMessageDelete(String msgType, String id);
    }
}
