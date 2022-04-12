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
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.message.bean.SystemMessage;
import com.nooie.common.utils.time.DateTimeUtil;
import com.afar.osaio.util.DialogUtils;
import com.nooie.common.utils.configure.LanguageUtil;
import com.nooie.common.utils.tool.SystemUtil;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by victor on 2018/7/3
 * Email is victor.qiao.0604@gmail.com
 */
public class SystemMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_NORMAL = 0x01;
    private static final int TYPE_EMPTY = 0x02;

    private Context mCtx;
    private List<SystemMessage> mMessages;
    private DateFormat mFormatter;
    private Map<String, String> mDateTags = new HashMap<>();
    private OnClickMessageListener mListener;

    public void setOnClickMessageListener(OnClickMessageListener mListener) {
        this.mListener = mListener;
    }

    public interface OnClickMessageListener {
        void onClickRefuse(SystemMessage message);

        void onClickAgree(SystemMessage message);

        void onClickMessage(SystemMessage message);
    }

    public SystemMessageAdapter(Context mCtx) {
        this.mCtx = mCtx;
        mFormatter = new SimpleDateFormat("MM/dd/yyyy", LanguageUtil.getLocal(NooieApplication.get()));
    }

    public synchronized void setDataSet(@NonNull List<SystemMessage> messages) {
        mMessages = messages;
        Collections.sort(mMessages);
        refreshDateTags();
        notifyDataSetChanged();
    }

    public synchronized void insertItemsFromTail(@NonNull List<SystemMessage> messages) {
        if (mMessages != null) {
            Collections.sort(messages);
            int startPosition = mMessages.size();
            int itemCount = messages.size();
            mMessages.addAll(messages);
            refreshDateTags();
            notifyItemRangeChanged(startPosition, itemCount);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int type) {
        if (type == TYPE_EMPTY) {
            View view = LayoutInflater.from(mCtx).inflate(R.layout.item_message_empty, viewGroup, false);
            return new EmptyViewHolder(view);
        } else {
            //View view = LayoutInflater.from(mCtx).inflate(R.layout.item_system_message, viewGroup, false);
            View view = LayoutInflater.from(mCtx).inflate(R.layout.item_system_message_new, viewGroup, false);
            SystemMsgViewHolder holder = new SystemMsgViewHolder(view);
            holder.tvReject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null)
                        mListener.onClickRefuse(mMessages.get((Integer) view.getTag()));
                }
            });

            holder.tvAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null)
                        mListener.onClickAgree(mMessages.get((Integer) view.getTag()));
                }
            });

            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = (Integer) view.getTag();
                    if (mMessages.get(position).isShowAgreeReject()) {
                        showHandlerDialog(mMessages.get(position));
                    } else {
                        if (mListener != null)
                            mListener.onClickMessage(mMessages.get((Integer) view.getTag()));
                        //DialogUtils.showInformationDialog(mCtx, mMessages.get(position).getTitle(), mMessages.get(position).getContent());
                        showClickItem(mMessages.get(position));
                    }
                }
            });

            return holder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder instanceof SystemMsgViewHolder) {
            SystemMsgViewHolder holder = (SystemMsgViewHolder) viewHolder;

            long createTime = mMessages.get(position).getUtcTime();
            String hmStr = DateTimeUtil.formatDate(NooieApplication.mCtx, createTime, DateTimeUtil.PATTERN_YMD_HMS_1);
            /*
            String dayStr = String.valueOf(DateTimeUtil.getDayOfMonth(createTime));
            String monthStr = DateTimeUtil.getMonthDisplayName(createTime);
            String yearStr = String.valueOf(DateTimeUtil.getYear(createTime));
            String dmyStr = String.format("%1$s %2$s %3$s", dayStr, monthStr, yearStr);
            */

            /*
            if (mDateTags.containsKey(mMessages.get(position).getId())) {
                holder.tvMsgDay.setVisibility(View.VISIBLE);
            } else {
                holder.tvMsgDay.setVisibility(View.GONE);
            }
            */

            //holder.tvMsgDay.setText(dmyStr);
            //holder.tvMsgTime.setText(hmStr);
            holder.tvTitle.setText(mMessages.get(position).getTitle());
            holder.tvTime.setText(hmStr);
            holder.tvMsg.setText(mMessages.get(position).getContent());

            holder.container.setTag(position);

            if (mMessages.get(position).isShowAgreeReject()) {
                holder.tvAccept.setVisibility(View.VISIBLE);
                holder.tvAccept.setTag(position);
                holder.tvReject.setVisibility(View.VISIBLE);
                holder.tvReject.setTag(position);

                holder.tvState.setVisibility(View.GONE);
                holder.ivUnread.setVisibility(View.VISIBLE);
            } else {
                holder.tvAccept.setVisibility(View.INVISIBLE);
                holder.tvReject.setVisibility(View.INVISIBLE);

                holder.tvState.setVisibility(View.VISIBLE);
                holder.tvState.setText(mMessages.get(position).getState());
                holder.ivUnread.setVisibility(View.INVISIBLE);
            }

            holder.ivUnread.setVisibility(View.INVISIBLE);
        }
    }

    private void showHandlerDialog(final SystemMessage message) {
        if (message == null) return;

        DialogUtils.showConfirmWithSubMsgDialog(mCtx, message.getTitle(), message.getContent(), R.string.reject, R.string.accept, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                if (mListener != null)
                    mListener.onClickAgree(message);
            }

            @Override
            public void onClickLeft() {
                if (mListener != null)
                    mListener.onClickRefuse(message);

            }
        });
    }

    private void showClickItem(final SystemMessage message) {
        if (message != null && message.getMessage() != null && message.getMessage().getType() == ApiConstant.SYS_MSG_TYPE_SYS_ACTIVE) {
            DialogUtils.showInformationDialog(mCtx, message.getTitle(), message.getContent(), mCtx.getString(R.string.home_ad_go_to_brower), true, true, new DialogUtils.OnClickInformationDialogLisenter() {
                @Override
                public void onConfirmClick() {
                    if (message != null && message.getMessage() != null && message.getMessage().getMsg() != null) {
                        SystemUtil.gotoBrower(mCtx, message.getMessage().getMsg().getUrl());
                    }
                }
            });
        } else if (message != null) {
            DialogUtils.showInformationDialog(mCtx, message.getTitle(), message.getContent(), true);
        }
    }

    @Override
    public int getItemCount() {
        return (mMessages == null || mMessages.size() == 0) ? 1 : mMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mMessages == null || mMessages.size() == 0) {
            return TYPE_EMPTY;
        } else {
            return TYPE_NORMAL;
        }
    }

    /**
     * 获取同一天中消息的最新那一条的id
     *
     * @param msgs
     * @return
     */
    private Map<String, String> getDateTags(List<SystemMessage> msgs) {
        Map<String, String> dateTags = new HashMap<>();
        for (SystemMessage msg : msgs) {
            if (!dateTags.containsValue(DateTimeUtil.formatDate(NooieApplication.mCtx, msg.getUtcTime(), DateTimeUtil.PATTERN_YMD))) {
                //Log.d("debug","-->> SystemMessageAdapter getDateTags msgId=" + msg.getId());
                dateTags.put(msg.getId(), DateTimeUtil.formatDate(NooieApplication.mCtx, msg.getUtcTime(), DateTimeUtil.PATTERN_YMD));
            }
        }

        return dateTags;
    }

    /**
     * 每次加载数据都要刷新
     */
    private void refreshDateTags() {
        if (mMessages == null) {
            return;
        }
        mDateTags.clear();
        mDateTags.putAll(getDateTags(mMessages));
    }

    class SystemMsgViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvTitle)
        TextView tvTitle;
        @BindView(R.id.tvTime)
        TextView tvTime;
        @BindView(R.id.tvMsg)
        TextView tvMsg;
        @BindView(R.id.tvAccept)
        TextView tvAccept;
        @BindView(R.id.tvReject)
        TextView tvReject;
        @BindView(R.id.tvState)
        TextView tvState;
        @BindView(R.id.ivUnread)
        ImageView ivUnread;
        @BindView(R.id.tvMsgDay)
        TextView tvMsgDay;
        @BindView(R.id.tvMsgTime)
        TextView tvMsgTime;
        @BindView(R.id.ivMsgTimePoint)
        ImageView ivMsgTimePoint;

        View container;

        public SystemMsgViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            container = itemView;
        }
    }

    class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
