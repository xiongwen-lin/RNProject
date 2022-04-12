package com.afar.osaio.widget.adapter;

import android.content.Context;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afar.osaio.smart.player.delegate.IPlayerDelegate;
import com.afar.osaio.smart.player.delegate.PlayState;
import com.nooie.common.utils.collection.CollectionUtil;
import com.afar.osaio.R;
import com.nooie.common.utils.time.DateTimeUtil;
import com.afar.osaio.widget.CalenderBean;
import com.afar.osaio.widget.listener.OnRecyclerItemClickListener;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.nooie.common.utils.time.DateTimeUtil.PATTERN_MD_1;

/**
 * Created by victor on 2018/6/28
 * Email is victor.qiao.0604@gmail.com
 */
public class UtcDateAdapter extends RecyclerView.Adapter<UtcDateAdapter.DateViewHolder> {

    private static final int DATA_VIEW_TYPE_NORMAL = 0;
    private static final int DATA_VIEW_TYPE_LIVE = 1;
    private Context mCtx;
    private List<CalenderBean> mDates;
    private OnRecyclerItemClickListener listener;
    private boolean isHorizontal;
    private boolean mIsLive = true;
    private WeakReference<PlayState> mPlayStateRef;
    private WeakReference<IPlayerDelegate> mPlayerDelegateRef;
    private int mTextColor = R.color.theme_text_color;
    private int mSelectedTextColor = R.color.theme_green;
    private int mInvalidTextColor = R.color.gray_a1a1a1;

    public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener listener) {
        this.listener = listener;
    }

    public UtcDateAdapter(Context mCtx, boolean isHorizontal) {
        this.mCtx = mCtx;
        this.mDates = mDates;
        this.isHorizontal = isHorizontal;
    }

    public void setData(List<CalenderBean> dates) {
        mDates = dates;
        notifyDataSetChanged();
    }

    private long mCurrentSeekDay = 0L;
    public void setSelectDate(boolean isLive, Calendar calender) {
        mIsLive = isLive;
        if (mIsLive) {
            for (CalenderBean data : CollectionUtil.safeFor(mDates)) {
                if (data != null) {
                    data.setSelected(false);
                }
            }
            notifyDataSetChanged();
            return;
        }

        mCurrentSeekDay = calender != null ? calender.getTimeInMillis() : 0L;

        notifyDataSetChanged();
    }

    public int indexPosition(CalenderBean calenderBean) {
        if (calenderBean == null || calenderBean.getCalendar() == null) return -1;

        for (int i = 0; i < mDates.size(); i++) {
            boolean isMatch = !TextUtils.isEmpty(DateTimeUtil.getUtcTimeString(calenderBean.getCalendar().getTimeInMillis(), DateTimeUtil.PATTERN_YMD)) && DateTimeUtil.getUtcTimeString(calenderBean.getCalendar().getTimeInMillis(), DateTimeUtil.PATTERN_YMD).equalsIgnoreCase(DateTimeUtil.getUtcTimeString(mDates.get(i).getCalendar().getTimeInMillis(), DateTimeUtil.PATTERN_YMD));
            if (isMatch) {
                return i;
            }
        }
        return -1;
    }

    public CalenderBean currentSelectData() {
        for (CalenderBean item : mDates) {
            //NooieLog.d("-->> UtcDateAdapter currentSelectData day=" + DateTimeUtil.getUtcTimeString(item.getCalendar().getTimeInMillis(), DateTimeUtil.PATTERN_YMD));
            boolean isSelected = item.getCalendar() != null && !TextUtils.isEmpty(DateTimeUtil.getUtcTimeString(item.getCalendar().getTimeInMillis(), DateTimeUtil.PATTERN_YMD)) && DateTimeUtil.getUtcTimeString(item.getCalendar().getTimeInMillis(), DateTimeUtil.PATTERN_YMD).equalsIgnoreCase(DateTimeUtil.getUtcTimeString(mCurrentSeekDay, DateTimeUtil.PATTERN_YMD));
            if (isSelected) {
                return item;
            }
        }
        return null;
    }

    public void updateCurrentState(Calendar calender, boolean isLive, long currentSeekDay) {
        setSelectDate(isLive, calender);
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(isHorizontal ? R.layout.item_date_horizontal : R.layout.item_date_vertical, viewGroup, false);
        DateViewHolder holder = new DateViewHolder(view);
        if (viewType == DATA_VIEW_TYPE_LIVE) {
            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isPlayStarting()) {
                        if (listener != null) {
                            listener.onClickItem(null);
                        }
                        return;
                    }
                    setSelectDate(true,null);
                    if (listener != null) {
                        listener.onClickItem(null, true, mCurrentSeekDay);
                    }
                }
            });
            return holder;
        } else {
            holder.container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isPlayStarting() || !isLoadStorageFinish()) {
                        if (listener != null) {
                            listener.onClickItem(null);
                        }
                        return;
                    }
                    setSelectDate(false,(Calendar) view.getTag());
                    if (listener != null) {
                        listener.onClickItem((Calendar) view.getTag(), false, mCurrentSeekDay);
                    }
                }
            });
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder dateViewHolder, int i) {
        if (i == CollectionUtil.safeFor(mDates).size()) {
            dateViewHolder.container.setTag(null);
            dateViewHolder.tvName.setText(R.string.live);

            if (mIsLive) {
                dateViewHolder.tvName.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                dateViewHolder.tvName.setTextColor(ContextCompat.getColor(mCtx, mTextColor));
                dateViewHolder.ivDateSelect.setVisibility(View.VISIBLE);
            } else {
                dateViewHolder.tvName.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                dateViewHolder.tvName.setTextColor(ContextCompat.getColor(mCtx, mTextColor));
                dateViewHolder.ivDateSelect.setVisibility(View.GONE);
            }
        } else {
            dateViewHolder.container.setTag(mDates.get(i).getCalendar());
            if (DateTimeUtil.isTodayUtc(mDates.get(i).getCalendar().getTimeInMillis())) {
                dateViewHolder.tvName.setText(R.string.message_today);
            } else {
                dateViewHolder.tvName.setText(DateTimeUtil.getUtcTimeString(mDates.get(i).getCalendar().getTimeInMillis(), PATTERN_MD_1));
            }

            dateViewHolder.ivDateSelect.setVisibility(View.GONE);
            boolean isSelected = mDates.get(i).getCalendar() != null && !TextUtils.isEmpty(DateTimeUtil.getUtcTimeString(mDates.get(i).getCalendar().getTimeInMillis(), DateTimeUtil.PATTERN_YMD)) && DateTimeUtil.getUtcTimeString(mDates.get(i).getCalendar().getTimeInMillis(), DateTimeUtil.PATTERN_YMD).equalsIgnoreCase(DateTimeUtil.getUtcTimeString(mCurrentSeekDay, DateTimeUtil.PATTERN_YMD));
            if (!mIsLive && isSelected) {
                dateViewHolder.tvName.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                dateViewHolder.tvName.setTextColor(ContextCompat.getColor(mCtx, mSelectedTextColor));
                dateViewHolder.ivDateSelect.setVisibility(View.VISIBLE);
            } else if (!mDates.get(i).isHaveSDCardRecord()) {
                dateViewHolder.tvName.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                dateViewHolder.tvName.setTextColor(ContextCompat.getColor(mCtx, mInvalidTextColor));
            } else {
                dateViewHolder.tvName.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
                dateViewHolder.tvName.setTextColor(ContextCompat.getColor(mCtx, mTextColor));
            }
        }
    }

    @Override
    public int getItemCount() {
        return CollectionUtil.safeFor(mDates).size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == CollectionUtil.safeFor(mDates).size()) {
            return DATA_VIEW_TYPE_LIVE;
        } else {
            return DATA_VIEW_TYPE_NORMAL;
        }
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
        notifyDataSetChanged();
    }

    public void setInvalidTextColor(int invalidTextColor) {
        mInvalidTextColor = invalidTextColor;
        notifyDataSetChanged();
    }

    public void setPlayState(PlayState playState) {
        if (mPlayStateRef == null || mPlayStateRef.get() == null) {
            mPlayStateRef = new WeakReference<PlayState>(playState);
        }
    }

    public boolean isPlayStarting() {
        return mPlayStateRef != null && mPlayStateRef.get() != null && mPlayStateRef.get().getState() == PlayState.PLAY_STATE_START && (Math.abs(System.currentTimeMillis() - mPlayStateRef.get().getTime()) < PlayState.CMD_TIMEOUT);
    }

    public void setPlayerDelegate(IPlayerDelegate playerDelegate) {
        if (mPlayerDelegateRef == null || mPlayerDelegateRef.get() == null) {
            mPlayerDelegateRef = new WeakReference<>(playerDelegate);
        }
    }

    public boolean isLoadStorageFinish() {
        return mPlayerDelegateRef == null || mPlayerDelegateRef.get() == null || mPlayerDelegateRef.get().isLoadStorageFinish();
    }

    public void release() {
        if (mDates != null) {
            mDates.clear();
            mDates = null;
        }
        if (listener != null) {
            listener = null;
        }
        mCtx = null;
    }

    class DateViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvName)
        TextView tvName;
        @BindView(R.id.ivDateSelect)
        View ivDateSelect;

        View container;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            container = itemView;
        }
    }
}
