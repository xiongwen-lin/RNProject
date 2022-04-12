package com.afar.osaio.widget.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.smart.player.delegate.IPlayerDelegate;
import com.afar.osaio.smart.player.delegate.PlayState;
import com.afar.osaio.widget.CalenderBean;
import com.afar.osaio.widget.listener.UtcSelectDataListener;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.time.DateTimeUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.nooie.common.utils.time.DateTimeUtil.PATTERN_MD_1;

/**
 * Created by victor on 2018/6/28
 * Email is victor.qiao.0604@gmail.com
 */
public class UtcSelectDateAdapter extends RecyclerView.Adapter<UtcSelectDateAdapter.DateViewHolder> {

    private static final int DATA_VIEW_TYPE_NORMAL = 0;
    private Context mCtx;
    private List<CalenderBean> mDates;
    private UtcSelectDataListener mListener;
    private boolean isHorizontal;
    private WeakReference<PlayState> mPlayStateRef;
    private WeakReference<IPlayerDelegate> mPlayerDelegateRef;
    private int mTextColor = R.color.theme_text_color;
    private int mSelectedTextColor = R.color.theme_green;
    private int mInvalidTextColor = R.color.gray_a1a1a1;
    private long mCurrentSeekDay = 0L;

    public void setListener(UtcSelectDataListener listener) {
        this.mListener = listener;
    }

    public UtcSelectDateAdapter(Context mCtx, boolean isHorizontal) {
        this.mCtx = mCtx;
        this.isHorizontal = isHorizontal;
        mDates = new ArrayList<>();
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(isHorizontal ? R.layout.item_date_horizontal : R.layout.item_date_vertical, viewGroup, false);
        DateViewHolder holder = new DateViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder dateViewHolder, int position) {
        if (dateViewHolder == null) {
            return;
        }
        CalenderBean data = mDates != null && CollectionUtil.isIndexSafe(position, CollectionUtil.size(mDates)) ? mDates.get(position) : null;
        if (data == null || data.getCalendar() == null) {
            dateViewHolder.container.setOnClickListener(null);
            return;
        }
        if (DateTimeUtil.isTodayUtc(data.getCalendar().getTimeInMillis())) {
            dateViewHolder.tvName.setText(R.string.message_today);
        } else {
            dateViewHolder.tvName.setText(DateTimeUtil.getUtcTimeString(data.getCalendar().getTimeInMillis(), PATTERN_MD_1));
        }

        dateViewHolder.ivDateSelect.setVisibility(View.GONE);
        boolean isSelected = data.getCalendar() != null && !TextUtils.isEmpty(DateTimeUtil.getUtcTimeString(data.getCalendar().getTimeInMillis(), DateTimeUtil.PATTERN_YMD)) && DateTimeUtil.getUtcTimeString(data.getCalendar().getTimeInMillis(), DateTimeUtil.PATTERN_YMD).equalsIgnoreCase(DateTimeUtil.getUtcTimeString(mCurrentSeekDay, DateTimeUtil.PATTERN_YMD));
        if (isSelected) {
            dateViewHolder.tvName.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            dateViewHolder.tvName.setTextColor(ContextCompat.getColor(mCtx, mSelectedTextColor));
            dateViewHolder.ivDateSelect.setVisibility(View.VISIBLE);
        } else if (!data.isHaveSDCardRecord()) {
            dateViewHolder.tvName.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            dateViewHolder.tvName.setTextColor(ContextCompat.getColor(mCtx, mInvalidTextColor));
        } else {
            dateViewHolder.tvName.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            dateViewHolder.tvName.setTextColor(ContextCompat.getColor(mCtx, mTextColor));
        }
        dateViewHolder.container.setOnClickListener(data.isHaveSDCardRecord() ? new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                if (isPlayStarting() || !isLoadStorageFinish()) {
                    if (mListener != null) {
                        mListener.onClickItem(null, mCurrentSeekDay);
                    }
                    return;
                }

                 */
                Calendar selectData = data != null ? data.getCalendar() : null;
                setSelectDate(selectData);
                if (mListener != null) {
                    mListener.onClickItem(selectData, mCurrentSeekDay);
                }
            }
        } : null);
    }

    @Override
    public int getItemCount() {
        return CollectionUtil.size(mDates);
    }

    @Override
    public int getItemViewType(int position) {
        return DATA_VIEW_TYPE_NORMAL;
    }

    public void setData(List<CalenderBean> dates) {
        if (mDates == null) {
            mDates = new ArrayList<>();
        }
        mDates.clear();
        if (CollectionUtil.isNotEmpty(dates)) {
            mDates.addAll(dates);
        }
        notifyDataSetChanged();
    }

    public void setSelectDate(Calendar calender) {
        if (calender == null || calender.getTimeInMillis() < 0) {
            return;
        }
        mCurrentSeekDay = calender.getTimeInMillis();
        notifyDataSetChanged();
    }

    public int indexPosition(CalenderBean calenderBean) {
        if (calenderBean == null || calenderBean.getCalendar() == null || CollectionUtil.isEmpty(mDates)) {
            return -1;
        }

        for (int i = 0; i < mDates.size(); i++) {
            boolean isMatch = !TextUtils.isEmpty(DateTimeUtil.getUtcTimeString(calenderBean.getCalendar().getTimeInMillis(), DateTimeUtil.PATTERN_YMD)) && DateTimeUtil.getUtcTimeString(calenderBean.getCalendar().getTimeInMillis(), DateTimeUtil.PATTERN_YMD).equalsIgnoreCase(DateTimeUtil.getUtcTimeString(mDates.get(i).getCalendar().getTimeInMillis(), DateTimeUtil.PATTERN_YMD));
            if (isMatch) {
                return i;
            }
        }
        return -1;
    }

    public CalenderBean currentSelectData() {
        if (CollectionUtil.isEmpty(mDates)) {
            return null;
        }
        for (CalenderBean item : mDates) {
            //NooieLog.d("-->> UtcDateAdapter currentSelectData day=" + DateTimeUtil.getUtcTimeString(item.getCalendar().getTimeInMillis(), DateTimeUtil.PATTERN_YMD));
            boolean isSelected = item.getCalendar() != null && !TextUtils.isEmpty(DateTimeUtil.getUtcTimeString(item.getCalendar().getTimeInMillis(), DateTimeUtil.PATTERN_YMD)) && DateTimeUtil.getUtcTimeString(item.getCalendar().getTimeInMillis(), DateTimeUtil.PATTERN_YMD).equalsIgnoreCase(DateTimeUtil.getUtcTimeString(mCurrentSeekDay, DateTimeUtil.PATTERN_YMD));
            if (isSelected) {
                return item;
            }
        }
        return null;
    }

    public void updateCurrentState(Calendar calender, long currentSeekDay) {
        setSelectDate(calender);
    }

    public void updateSelectDataList(int[] recDateList) {
        if (recDateList == null || recDateList.length <= 0 || recDateList.length != CollectionUtil.size(mDates)) {
            return;
        }
        int recDateLen = recDateList.length;
        int dateSize = CollectionUtil.size(mDates);
        for (int i = 0; i < dateSize; i++) {
            //recDateList返回的是倒序的日期
            int recDateIndex = (recDateLen - 1) - i;
            if (recDateIndex >= 0) {
                int recentDayState = recDateList[recDateIndex];
//                recentDayState = 1;
                mDates.get(i).setHaveSDCardRecord(recentDayState != 0);
            } else {
                mDates.get(i).setHaveSDCardRecord(false);
            }
        }
        notifyDataSetChanged();
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
        if (mListener != null) {
            mListener = null;
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
