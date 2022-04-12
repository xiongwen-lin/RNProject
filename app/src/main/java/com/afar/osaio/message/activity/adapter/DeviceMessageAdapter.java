package com.afar.osaio.message.activity.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.DeviceMessage;
import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.message.presenter.DeviceMsgPresenterImpl;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.common.utils.configure.LanguageUtil;
import com.mcxtzhang.swipemenulib.SwipeMenuLayout;
import com.nooie.common.utils.collection.CollectionUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by victor on 2018/7/3
 * Email is victor.qiao.0604@gmail.com
 */
public class DeviceMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_NORMAL = 0x01;
    private static final int TYPE_EMPTY = 0x02;

    private Context mCtx;
    private List<DeviceMessage> mMsgs;
    private OnMsgItemClickListener mListener;

    private DateFormat mFormatter;
    private Map<String, String> mDateTags = new HashMap<>();
    private List<String> mDeleteMsgIds = new ArrayList<>();
    private boolean mIsDeletingMsg = false;

    public interface OnMsgItemClickListener {
        void onItemGoToViewClick(DeviceMessage message);

        void onClickItem(DeviceMessage message);

        void onDeleteItem(int position, DeviceMessage message);
    }

    public void setOnMsgItemClickListener(OnMsgItemClickListener listener) {
        mListener = listener;
    }

    public DeviceMessageAdapter(Context ctx) {
        this.mCtx = ctx;
        mFormatter = new SimpleDateFormat("MM/dd/yyyy", LanguageUtil.getLocal(NooieApplication.get()));
    }

    public synchronized void setDataSet(@NonNull List<DeviceMessage> messages) {
        mMsgs = messages;
        refreshDateTags();
        notifyDataSetChanged();
    }

    public synchronized List<DeviceMessage> getDataSet() {
        return mMsgs;
    }

    public List<String> getDeleteMsgIds() {
        return mDeleteMsgIds;
    }

    public boolean isDeleteMsg(String msgId) {
        return CollectionUtil.isNotEmpty(mDeleteMsgIds) && mDeleteMsgIds.contains(msgId);
    }

    public void setIsDeletingMsg(boolean isDeletingMsg) {
        mIsDeletingMsg = isDeletingMsg;
        mDeleteMsgIds.clear();
        notifyDataSetChanged();
    }

    public void setAllSelect() {
        if (CollectionUtil.isEmpty(mMsgs)) {
            return;
        }
        boolean isSelectAll = mDeleteMsgIds.size() == mMsgs.size();
        mDeleteMsgIds.clear();
        if (isSelectAll) {
            notifyDataSetChanged();
            return;
        }
        for (DeviceMessage msg : CollectionUtil.safeFor(mMsgs)) {
            if (!mDeleteMsgIds.contains(String.valueOf(msg.getId()))) {
                mDeleteMsgIds.add(String.valueOf(msg.getId()));
            }
        }
        notifyDataSetChanged();
    }

    public synchronized void insertItemsFromTail(@NonNull List<DeviceMessage> messages) {
        if (mMsgs != null) {
            int startPosition = mMsgs.size();
            int itemCount = messages.size();
            mMsgs.addAll(messages);
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
            //View view = LayoutInflater.from(mCtx).inflate(R.layout.item_camera_message, viewGroup, false);
            View view = LayoutInflater.from(mCtx).inflate(R.layout.item_camera_message_new, viewGroup, false);
            DeviceMsgViewHolder holder = new DeviceMsgViewHolder(view);
            holder.smlContainer.setIos(false).setLeftSwipe(true).setEnabled(false);
            /*
            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null && !mIsDeletingMsg)
                        mListener.onClickItem(mMsgs.get((Integer) view.getTag()));
                }
            });
            */

            holder.tvGoToView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null && !mIsDeletingMsg)
                        mListener.onItemGoToViewClick(mMsgs.get((Integer) view.getTag()));
                }
            });

            /*
            holder.ivGoToView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null && !mIsDeletingMsg) {
                        mListener.onItemGoToViewClick(mMsgs.get((Integer) view.getTag()));
                    }
                }
            });
            */
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof DeviceMsgViewHolder) {
            DeviceMessage deviceMessage = mMsgs.get(position);
            final DeviceMsgViewHolder holder = (DeviceMsgViewHolder) viewHolder;
            holder.container.setTag(position);
            holder.tvGoToView.setTag(position);
            //holder.ivGoToView.setTag(position);
            if (mMsgs.get(position).getType() == ApiConstant.DEVICE_MSG_TYPE_SD_LEAK || mIsDeletingMsg) {
                holder.rlItemRightContainer.setVisibility(mIsDeletingMsg ? View.VISIBLE : View.GONE);
                holder.ivGoToView.setVisibility(View.GONE);
            } else {
                holder.rlItemRightContainer.setVisibility(View.VISIBLE);
                holder.ivGoToView.setVisibility(View.VISIBLE);
            }

            long createTime = mMsgs.get(position).getDevice_time() * 1000L;
            String hmStr = DateTimeUtil.getUtcTimeString(createTime, DateTimeUtil.PATTERN_HM);
            String dayStr = String.valueOf(DateTimeUtil.getUtcDayOfMonth(createTime));
            String monthStr = DateTimeUtil.getUtcMonthDisplayName(NooieApplication.mCtx, createTime);
            String yearStr = String.valueOf(DateTimeUtil.getUtcYear(createTime));

            String dmyStr = String.format("%1$s %2$s %3$s", dayStr, monthStr, yearStr);

            if (mDateTags.containsKey(String.valueOf(mMsgs.get(position).getId()))) {
                holder.tvMsgDay.setVisibility(View.VISIBLE);
            } else {
                holder.tvMsgDay.setVisibility(View.GONE);
            }

            //Log.d("debug", "-->> DeviceMsgAdapter date time=" + new Date(mMsgs.get(position).getCreateTime()).toString() + " m=" + monthStr + "hm=" + hmStr);
            holder.tvMsgDay.setText(dmyStr);
            holder.tvMsgTime.setText(hmStr);
            holder.tvTitle.setText(DeviceMsgPresenterImpl.getWarnMsgDesc(mMsgs.get(position)));
            holder.tvMsg.setText(DeviceMsgPresenterImpl.getWarnMsgContent(mMsgs.get(position)));
            //holder.ivUnread.setVisibility(mMsgs.get(position).isRead() ? View.INVISIBLE : View.VISIBLE);
            holder.ivUnread.setVisibility(View.INVISIBLE);

            //NooieLog.d("-->> DeviceMessageAdapter onBindViewHolder deviceMsg msgId=" + deviceMessage.getId() + " status=" + deviceMessage.getStatus());
            /*
            holder.ivMsgTimePoint.setImageResource(deviceMessage.getStatus() == ApiConstant.DEVICE_MSG_STATUS_READ ? R.drawable.button_round_gray_state : R.drawable.button_round_blue_state);
            holder.tvMsgTime.setTextColor(deviceMessage.getStatus() == ApiConstant.DEVICE_MSG_STATUS_READ ? ContextCompat.getColor(NooieApplication.mCtx, R.color.gray_a1a1a1) : ContextCompat.getColor(NooieApplication.mCtx, R.color.theme_text_color));
            holder.tvTitle.setTextColor(deviceMessage.getStatus() == ApiConstant.DEVICE_MSG_STATUS_READ ? ContextCompat.getColor(NooieApplication.mCtx, R.color.gray_a1a1a1) : ContextCompat.getColor(NooieApplication.mCtx, R.color.theme_text_color));
            holder.tvMsg.setTextColor(deviceMessage.getStatus() == ApiConstant.DEVICE_MSG_STATUS_READ ? ContextCompat.getColor(NooieApplication.mCtx, R.color.gray_a1a1a1) : ContextCompat.getColor(NooieApplication.mCtx, R.color.theme_text_color));
            */

            Glide.with(NooieApplication.mCtx)
                    .load(mMsgs.get(position).getFiles())
                    .apply(new RequestOptions()
                            .dontTransform().transform(new MultiTransformation<Bitmap>(new CenterCrop(), new RoundedCorners(DisplayUtil.dpToPx(NooieApplication.mCtx, 16))))
                            .placeholder(R.drawable.default_preview_thumbnail)
                            .format(DecodeFormat.PREFER_RGB_565)
                            .error(R.drawable.default_preview_thumbnail)
                    )
                    .transition(withCrossFade())
                    .into(holder.ivGoToView);

            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder != null && mListener != null && !mIsDeletingMsg) {
                        int indexId = holder.getAdapterPosition();
                        if (CollectionUtil.isNotEmpty(mMsgs) && CollectionUtil.isIndexSafe(indexId, mMsgs.size())) {
                            /*
                            if (mMsgs.get(indexId) != null && mMsgs.get(indexId).getStatus() != ApiConstant.DEVICE_MSG_STATUS_READ) {
                                mMsgs.get(indexId).setStatus(ApiConstant.DEVICE_MSG_STATUS_READ);
                                notifyItemChanged(indexId);
                            }
                            */
                            mListener.onClickItem(mMsgs.get(indexId));
                        }
                    }
                }
            });

            holder.ivGoToView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder != null && mListener != null && !mIsDeletingMsg) {
                        int indexId = holder.getAdapterPosition();
                        if (CollectionUtil.isNotEmpty(mMsgs) && CollectionUtil.isIndexSafe(indexId, mMsgs.size())) {
                            /*
                            if (mMsgs.get(indexId) != null && mMsgs.get(indexId).getStatus() != ApiConstant.DEVICE_MSG_STATUS_READ) {
                                mMsgs.get(indexId).setStatus(ApiConstant.DEVICE_MSG_STATUS_READ);
                                notifyItemChanged(indexId);
                            }
                            */
                            mListener.onClickItem(mMsgs.get(indexId));
                        }
                    }
                }
            });

            holder.ivDelSelect.setVisibility(mIsDeletingMsg ? View.VISIBLE : View.GONE);
            if (mIsDeletingMsg) {
                boolean isDeleteSelect = isDeleteMsg(String.valueOf(mMsgs.get(position).getId()));
                if (isDeleteSelect) {
                    holder.ivDelSelect.setImageResource(R.drawable.selected_icon);
                } else {
                    holder.ivDelSelect.setImageResource(R.drawable.select_icon);
                }
                holder.rlItemRightContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int id = holder.getAdapterPosition();
                        String msgId = String.valueOf(mMsgs.get(id).getId());
                        if (isDeleteMsg(msgId)) {
                            mDeleteMsgIds.remove(msgId);
                            holder.ivDelSelect.setImageResource(R.drawable.select_icon);
                        } else {
                            mDeleteMsgIds.add(msgId);
                            holder.ivDelSelect.setImageResource(R.drawable.selected_icon);
                        }
                    }
                });
            } else {
                holder.rlItemRightContainer.setOnClickListener(null);
            }

            holder.btnDevMsgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        int id = holder.getAdapterPosition();
                        mListener.onDeleteItem(id, mMsgs.get(id));
                        mMsgs.remove(id);
                        notifyItemRemoved(id);
                    }
                }
            });
        } else if (viewHolder instanceof EmptyViewHolder) {
        }
    }

    @Override
    public int getItemCount() {
        return (mMsgs == null || mMsgs.size() == 0) ? 1 : mMsgs.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mMsgs == null || mMsgs.size() == 0) {
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
    private Map<String, String> getDateTags(List<DeviceMessage> msgs) {
        Map<String, String> dateTags = new HashMap<>();
        for (DeviceMessage msg : msgs) {
            if (!dateTags.containsValue(DateTimeUtil.formatDate(NooieApplication.mCtx, msg.getTime() * 1000L, DateTimeUtil.PATTERN_YMD))) {
                //Log.d("debug","-->> DeviceMessageAdapter getDateTags msgId=" + msg.getMsgId());
                dateTags.put(String.valueOf(msg.getId()), DateTimeUtil.formatDate(NooieApplication.mCtx, msg.getTime() * 1000L, DateTimeUtil.PATTERN_YMD));
            }
        }

        return dateTags;
    }

    /**
     * 每次加载数据都要刷新
     */
    private void refreshDateTags() {
        mDateTags.clear();
        mDateTags.putAll(getDateTags(mMsgs));
    }

    public static class DeviceMsgViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.smlContainer)
        SwipeMenuLayout smlContainer;
        @BindView(R.id.devMsgContent)
        View devMsgContent;
        @BindView(R.id.btnDevMsgDelete)
        TextView btnDevMsgDelete;
        @BindView(R.id.tvTitle)
        TextView tvTitle;
        @BindView(R.id.tvTime)
        TextView tvTime;
        @BindView(R.id.tvMsg)
        TextView tvMsg;
        @BindView(R.id.tvGoToView)
        TextView tvGoToView;
        @BindView(R.id.ivUnread)
        ImageView ivUnread;
        @BindView(R.id.tvMsgDay)
        TextView tvMsgDay;
        @BindView(R.id.tvMsgTime)
        TextView tvMsgTime;
        @BindView(R.id.ivMsgTimePoint)
        ImageView ivMsgTimePoint;
        @BindView(R.id.ivGoToView)
        ImageView ivGoToView;
        @BindView(R.id.ivDelSelect)
        ImageView ivDelSelect;
        @BindView(R.id.rlItemRightContainer)
        View rlItemRightContainer;

        View container;

        public DeviceMsgViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            //container = itemView;
            container = devMsgContent;
        }
    }

    class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
