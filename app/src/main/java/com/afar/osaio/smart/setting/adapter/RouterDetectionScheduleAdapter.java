package com.afar.osaio.smart.setting.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.DetectionSchedule;
import com.nooie.common.utils.collection.CollectionUtil;
import com.suke.widget.SwitchButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RouterDetectionScheduleAdapter extends RecyclerView.Adapter<RouterDetectionScheduleAdapter.DetectionScheduleViewHolder> {

    private List<DetectionSchedule> mSchedules = new ArrayList<>();
    private DetectionScheduleListener mListener;

    @Override
    public DetectionScheduleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_router_detection_schedule, parent, false);
        return new DetectionScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DetectionScheduleViewHolder holder, int position) {
        final DetectionSchedule schedule = mSchedules.get(position);
        StringBuilder scheduleSb = new StringBuilder();
        scheduleSb.append(formatTime(schedule.getStartH(), schedule.getStartM()));
        scheduleSb.append(" - ");
        scheduleSb.append(formatTime(schedule.getEndH(), schedule.getEndM()));
        holder.tvItemScheduleTime.setText(scheduleSb.toString());

        holder.tvItemScheduleWeekDays.setText(convertWeekDays(schedule.getWeekDays()));

        if (!"router".equals(schedule.getScheduleType())) {
            //holder.btnItemScheduleSwitch.setVisibility(View.VISIBLE);
            //holder.router_wifi_arrow.setVisibility(View.GONE);
            holder.btnItemScheduleSwitch.setVisibility(View.GONE);
            holder.router_wifi_arrow.setVisibility(View.VISIBLE);
            if (holder.btnItemScheduleSwitch.isChecked() != schedule.isOpen()) {
                holder.btnItemScheduleSwitch.toggleNoCallback();
            }
        } else {
            holder.btnItemScheduleSwitch.setVisibility(View.GONE);
            holder.router_wifi_arrow.setVisibility(View.VISIBLE);
        }

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(schedule, position);
                }
            }
        });
        holder.btnItemScheduleSwitch.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (mListener != null) {
                    schedule.setOpen(isChecked);
                    //schedule.resetWeekDays(true);
                    mListener.onItemSwitch(schedule, isChecked, position);
                }
            }
        });
    }

    private String formatTime(int h, int m) {
        StringBuilder timeSb = new StringBuilder();
        timeSb.append(h < 10 ? "0" + h : h);
        timeSb.append(":");
        timeSb.append(m < 10 ? "0" + m : m);
        return timeSb.toString();
    }

    private static final int WEEKDAY_LEN = 5;
    private static final int WEEKEND_LEN = 2;
    private static final int EVERY_DAY_LEN = 7;
    private String convertWeekDays(List<Integer> weekDays) {
        if (CollectionUtil.isEmpty(weekDays)) {
            return "";
        }
        if (weekDays.contains(Calendar.SUNDAY) && CollectionUtil.size(weekDays) > 1) {
            Iterator<Integer> dayIterator = weekDays.iterator();
            while (dayIterator.hasNext()) {
                if (dayIterator.next() == Calendar.SUNDAY) {
                    dayIterator.remove();
                }
            }
            weekDays.add(Calendar.SUNDAY);
        }
        StringBuilder weekDaySb = new StringBuilder();
        for (Integer weekDay : CollectionUtil.safeFor(weekDays)) {
            switch (weekDay) {
                case Calendar.MONDAY :
                    weekDaySb.append(NooieApplication.mCtx.getString(R.string.w_monday));
                    weekDaySb.append(" ");
                    break;
                case Calendar.TUESDAY :
                    weekDaySb.append(NooieApplication.mCtx.getString(R.string.w_tuesday));
                    weekDaySb.append(" ");
                    break;
                case Calendar.WEDNESDAY :
                    weekDaySb.append(NooieApplication.mCtx.getString(R.string.w_wednesday));
                    weekDaySb.append(" ");
                    break;
                case Calendar.THURSDAY :
                    weekDaySb.append(NooieApplication.mCtx.getString(R.string.w_thursday));
                    weekDaySb.append(" ");
                    break;
                case Calendar.FRIDAY :
                    weekDaySb.append(NooieApplication.mCtx.getString(R.string.w_friday));
                    weekDaySb.append(" ");
                    break;
                case Calendar.SATURDAY :
                    weekDaySb.append(NooieApplication.mCtx.getString(R.string.w_saturday));
                    weekDaySb.append(" ");
                    break;
                case Calendar.SUNDAY :
                    weekDaySb.append(NooieApplication.mCtx.getString(R.string.w_sunday));
                    weekDaySb.append("");
                    break;
            }
        }

        /*
        if (!(weekDays.contains(Calendar.SATURDAY) || weekDays.contains(Calendar.SUNDAY))) {
            weekDaySb.setLength(0);
            weekDaySb.append(NooieApplication.mCtx.getString(R.string.weekdays));
        } else if (!(weekDays.contains(Calendar.MONDAY) || weekDays.contains(Calendar.TUESDAY) || weekDays.contains(Calendar.WEDNESDAY) || weekDays.contains(Calendar.THURSDAY) || weekDays.contains(Calendar.FRIDAY))) {
            weekDaySb.setLength(0);
            weekDaySb.append(NooieApplication.mCtx.getString(R.string.weekends));
        }
        */
        if (weekDays.size() == EVERY_DAY_LEN && weekDays.contains(Calendar.MONDAY) && weekDays.contains(Calendar.TUESDAY) && weekDays.contains(Calendar.WEDNESDAY) && weekDays.contains(Calendar.THURSDAY) && weekDays.contains(Calendar.FRIDAY) && weekDays.contains(Calendar.SATURDAY) && weekDays.contains(Calendar.SUNDAY)) {
            weekDaySb.setLength(0);
            weekDaySb.append(NooieApplication.mCtx.getString(R.string.every_day));
        } else if (weekDays.size() == WEEKDAY_LEN && weekDays.contains(Calendar.MONDAY) && weekDays.contains(Calendar.TUESDAY) && weekDays.contains(Calendar.WEDNESDAY) && weekDays.contains(Calendar.THURSDAY) && weekDays.contains(Calendar.FRIDAY)) {
            weekDaySb.setLength(0);
            weekDaySb.append(NooieApplication.mCtx.getString(R.string.weekdays));
        } else if (weekDays.size() == WEEKEND_LEN && weekDays.contains(Calendar.SATURDAY) && weekDays.contains(Calendar.SUNDAY)) {
            weekDaySb.setLength(0);
            weekDaySb.append(NooieApplication.mCtx.getString(R.string.weekends));
        }
        return weekDaySb.toString();
    }

    @Override
    public int getItemCount() {
        return mSchedules.size();
    }

    public void setData(List<DetectionSchedule> schedules) {
        if (CollectionUtil.isEmpty(schedules)) {
            return;
        }

        if (mSchedules == null) {
            mSchedules = new ArrayList<>();
        }

        mSchedules.clear();
        mSchedules.addAll(schedules);
        notifyDataSetChanged();
    }

    public void updataSchedule(int position, DetectionSchedule schedules) {
        if (mSchedules.size() > position && position >= 0) {
            mSchedules.set(position, schedules);
            notifyDataSetChanged();
        }
    }

    public void removeSchedule(int position) {
        if (mSchedules.size() > position && position >= 0) {
            mSchedules.remove(position);
            notifyDataSetChanged();
        }
    }

    public void clearData() {
        if (mSchedules != null) {
            mSchedules.clear();
            mSchedules = null;
        }
    }

    public void setListener(DetectionScheduleListener listener) {
        mListener = listener;
    }

    public interface DetectionScheduleListener {
        void onItemClick(DetectionSchedule schedule, int position);
        void onItemSwitch(DetectionSchedule schedule, boolean isChecked, int position);
    }

    public static class DetectionScheduleViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.container)
        View container;
        @BindView(R.id.tvItemScheduleTime)
        TextView tvItemScheduleTime;
        @BindView(R.id.tvItemScheduleWeekDays)
        TextView tvItemScheduleWeekDays;
        @BindView(R.id.btnItemScheduleSwitch)
        SwitchButton btnItemScheduleSwitch;
        @BindView(R.id.router_wifi_arrow)
        ImageView router_wifi_arrow;

        public DetectionScheduleViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
