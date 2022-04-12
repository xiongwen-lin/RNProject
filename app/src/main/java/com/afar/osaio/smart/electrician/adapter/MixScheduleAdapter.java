package com.afar.osaio.smart.electrician.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.electrician.bean.MixScheduleBean;
import com.afar.osaio.smart.electrician.bean.Schedule;
import com.afar.osaio.smart.electrician.bean.ScheduleHelper;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.suke.widget.SwitchButton;
import com.tuya.smart.sdk.bean.Timer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * ScheduleAdapter
 *
 * @author Administrator
 * @date 2019/3/26
 */
public class MixScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<MixScheduleBean> mSchedules = new ArrayList<>();

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(NooieApplication.mCtx).inflate(R.layout.item_device_schedule, parent, false);
        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ScheduleViewHolder) {
            ScheduleViewHolder viewHolder = (ScheduleViewHolder) holder;
            if (mSchedules.get(position).isTimerBean()) {
                updateTimerScheduleView(position, viewHolder);
            } else {
                updateScheduleView(position, viewHolder);
            }
        }
    }

    @Override
    public int getItemCount() {
        return CollectionUtil.isEmpty(mSchedules) ? 0 : mSchedules.size();
    }

    public void setData(List<MixScheduleBean> schedules) {
        mSchedules.clear();
        mSchedules.addAll(schedules);
        notifyDataSetChanged();
    }

    public List<MixScheduleBean> getData() {
        return this.mSchedules;
    }

    public void clearAll() {
        mSchedules.clear();
    }

    private void updateTimerScheduleView(final int position, final ScheduleViewHolder holder) {

        final Timer timerBean = mSchedules.get(position).getTimerBean();

        holder.tvItemScheduleFromTime.setText(timerBean.getTime());
        holder.tvItemScheduleWeekDays.setText(ScheduleHelper.getInstance().getLooperDes(timerBean.getLoops()));

        Map<String, Object> dpsMap = ScheduleHelper.getInstance().convertDps(timerBean.getValue());

        if (dpsMap == null || dpsMap.size() == 0|| TextUtils.isEmpty(timerBean.getDpId())) {

        } else {
            boolean isOn = (boolean) dpsMap.get(timerBean.getDpId());
            holder.tvItemScheduleFromState.setText(isOn ? "ON" : "OFF");
        }

        holder.vItemScheduleCenter.setVisibility(View.GONE);
        holder.tvItemScheduleToTime.setVisibility(View.GONE);
        holder.tvItemScheduleToState.setVisibility(View.GONE);
        holder.ivCycleTime.setVisibility(View.GONE);
        holder.tvRandomTime.setVisibility(View.GONE);

        boolean isOpen;
        if (timerBean.getStatus() == 1) {
            isOpen = true;
        } else {
            isOpen = false;
        }

        if (holder.btnItemScheduleSwitch.isChecked() != isOpen) {
            holder.btnItemScheduleSwitch.toggleNoCallback();
        }

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemTimerClick(position, timerBean);
                }
            }
        });

        holder.btnItemScheduleSwitch.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (mListener != null) {
                    NooieLog.e("------------>> btnItemScheduleSwitch  isChecked " + isChecked);
                    mListener.onTimerSwitchClick(timerBean, isChecked, position);
                }
            }
        });

    }

    public void itemRemoved(int position) {
        mSchedules.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mSchedules.size() - position);
    }

    public void itemTimerChanged(int position, Timer timer) {
        mSchedules.set(position, new MixScheduleBean(timer));
        notifyItemChanged(position);
    }

    private void updateScheduleView(final int position, final ScheduleViewHolder holder) {

        holder.tvItemScheduleFromTime.setText(mSchedules.get(position).getScheduleBean().getTimeOn());
        holder.tvItemScheduleToTime.setText(mSchedules.get(position).getScheduleBean().getTimeOff());
        holder.tvItemScheduleWeekDays.setText(mSchedules.get(position).getScheduleBean().getDes());
        holder.tvItemScheduleFromState.setText("ON");
        holder.tvItemScheduleToState.setText("OFF");

        holder.vItemScheduleCenter.setVisibility(View.VISIBLE);
        holder.tvItemScheduleToTime.setVisibility(View.VISIBLE);
        holder.tvItemScheduleToState.setVisibility(View.VISIBLE);


        Schedule scheduleBean = mSchedules.get(position).getScheduleBean();

        if (scheduleBean.isCycleTime() && scheduleBean.isShowCycleTime()) {
            holder.ivCycleTime.setVisibility(View.VISIBLE);
        } else {
            holder.ivCycleTime.setVisibility(View.GONE);
        }

        if (!scheduleBean.isCycleTime()) {
            holder.tvRandomTime.setVisibility(View.VISIBLE);
        } else {
            holder.tvRandomTime.setVisibility(View.GONE);
        }

        boolean isOpen = mSchedules.get(position).getScheduleBean().isOpen();
        if (holder.btnItemScheduleSwitch.isChecked() != isOpen) {
            holder.btnItemScheduleSwitch.toggleNoCallback();
        }

        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(mSchedules.get(position).getScheduleBean());
                }
            }
        });

        holder.btnItemScheduleSwitch.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                NooieLog.e("------------>> updateScheduleView btnItemScheduleSwitch  isChecked " + isChecked);
                if (mListener != null) {
                    String orginTime = mSchedules.get(position).getScheduleBean().getHexStr();
                    StringBuilder stringBuilder = new StringBuilder(isChecked ? "01" : "00");
                    String newTime = stringBuilder.append(orginTime.substring(2, orginTime.length())).toString();
                    mSchedules.get(position).getScheduleBean().setHexStr(newTime);
                    mListener.onSwitchClick(mSchedules.get(position).getScheduleBean(), orginTime, newTime, isChecked);
                }
            }
        });

    }

    private ScheduleAdapterListener mListener;

    public void setListener(ScheduleAdapterListener listener) {
        mListener = listener;
    }

    public static class ScheduleViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.btnItemScheduleSwitch)
        SwitchButton btnItemScheduleSwitch;
        @BindView(R.id.tvItemScheduleFromTime)
        TextView tvItemScheduleFromTime;
        @BindView(R.id.tvItemScheduleToTime)
        TextView tvItemScheduleToTime;
        @BindView(R.id.tvItemScheduleWeekDays)
        TextView tvItemScheduleWeekDays;
        @BindView(R.id.ivCycleTime)
        ImageView ivCycleTime;
        @BindView(R.id.tvItemScheduleFromState)
        TextView tvItemScheduleFromState;
        @BindView(R.id.vItemScheduleCenter)
        TextView vItemScheduleCenter;
        @BindView(R.id.tvItemScheduleToState)
        TextView tvItemScheduleToState;
        @BindView(R.id.tvRandomTime)
        TextView tvRandomTime;

        View container;

        public ScheduleViewHolder(View view) {
            super(view);
            container = view;
            ButterKnife.bind(this, view);
        }
    }

    public interface ScheduleAdapterListener {
        void onItemClick(Schedule schedule);

        void onItemTimerClick(int position, Timer timer);

        void onSwitchClick(Schedule schedule, String orginTime, String newTime, boolean isChecked);

        void onTimerSwitchClick(Timer time, boolean isChecked, int position);
    }
}
