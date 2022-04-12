package com.afar.osaio.widget.adapter;

import android.content.Context;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.nooie.common.utils.time.DateTimeUtil;
import com.afar.osaio.widget.CalenderBean;
import com.afar.osaio.widget.listener.OnRecyclerItemClickListener;
import com.nooie.common.utils.collection.CollectionUtil;

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
public class PlaybackDateAdapter extends RecyclerView.Adapter<PlaybackDateAdapter.DateViewHolder> {
    private Context mCtx;
    private List<CalenderBean> mDates = new ArrayList<>();
    private OnRecyclerItemClickListener listener;
    private boolean isHorizontal;

    public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener listener) {
        this.listener = listener;
    }

    public PlaybackDateAdapter(Context mCtx, boolean isHorizontal) {
        this.mCtx = mCtx;
        this.isHorizontal = isHorizontal;
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
        if (calender == null) return;

        for (int i = 0; i < mDates.size(); i++) {
            if (mDates.get(i).getCalendar().getTimeInMillis() == calender.getTimeInMillis()) {
                mDates.get(i).setSelected(true);
            } else {
                mDates.get(i).setSelected(false);
            }
        }

        notifyDataSetChanged();
    }

    public int indexPosition(CalenderBean calenderBean) {
        if (calenderBean == null || calenderBean.getCalendar() == null) return -1;

        for (int i = 0; i < mDates.size(); i++) {
            if (mDates.get(i).getCalendar().getTimeInMillis() == calenderBean.getCalendar().getTimeInMillis()) {
                return i;
            }
        }
        return -1;
    }

    public CalenderBean currentSelectData() {
        for (CalenderBean item : mDates) {
            if (item.isSelected()) {
                return item;
            }
        }
        return null;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mCtx).inflate(isHorizontal ? R.layout.item_date_horizontal : R.layout.item_playback_date_vertical, viewGroup, false);
        DateViewHolder holder = new DateViewHolder(view);

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectDate((Calendar) view.getTag());
                if (listener != null) listener.onClickItem((Calendar) view.getTag());
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder dateViewHolder, int i) {
        dateViewHolder.container.setTag(mDates.get(i).getCalendar());
        /*
        if (DateTimeUtil.isTodayUtc(mDates.get(i).getCalendar().getTimeInMillis())) {
            dateViewHolder.tvDate.setText(R.string.message_today);
        } else {
        }
        */

        long timeInMillis = mDates.get(i).getCalendar().getTimeInMillis();
        StringBuilder dateSb = new StringBuilder();
        dateSb.append(DateTimeUtil.isToday(timeInMillis) ? mCtx.getString(R.string.message_today) : DateTimeUtil.getDisplayName(NooieApplication.mCtx, timeInMillis, Calendar.DAY_OF_WEEK, Calendar.SHORT));
        dateSb.append(" ");
        dateSb.append(DateTimeUtil.getTimeString(timeInMillis, PATTERN_MD_1));
        dateViewHolder.tvDate.setText(dateSb.toString());

        if (mDates.get(i).isSelected()) {
            dateViewHolder.tvDate.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            dateViewHolder.tvDate.setTextColor(ContextCompat.getColor(mCtx, R.color.theme_white));
        } else if (!mDates.get(i).isHaveSDCardRecord()) {
            dateViewHolder.tvDate.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            dateViewHolder.tvDate.setTextColor(ContextCompat.getColor(mCtx, R.color.input_text_color));
        } else {
            dateViewHolder.tvDate.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            dateViewHolder.tvDate.setTextColor(ContextCompat.getColor(mCtx, R.color.theme_white));
        }
    }

    @Override
    public int getItemCount() {
        return mDates == null ? 0 : mDates.size();
    }

    class DateViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvDate)
        TextView tvDate;

        View container;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            container = itemView;
        }
    }
}
