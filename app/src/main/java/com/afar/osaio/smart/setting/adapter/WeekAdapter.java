package com.afar.osaio.smart.setting.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.protocol.bean.DayNotificationsPlanPeriod;
import com.afar.osaio.protocol.bean.Week;
import com.afar.osaio.util.Util;
import com.nooie.common.utils.time.DateTimeUtil;
import com.suke.widget.SwitchButton;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by victor on 2018/8/2
 * Email is victor.qiao.0604@gmail.com
 */
public class WeekAdapter extends RecyclerView.Adapter<WeekAdapter.WeekViewHolder> {

    private Context mCtx;
    private List<DayNotificationsPlanPeriod> list;

    private OnClickWeekItem onClickWeekItem;

    public void setOnClickWeekItem(OnClickWeekItem onClickWeekItem) {
        this.onClickWeekItem = onClickWeekItem;
    }

    public interface OnClickWeekItem {
        void onClickItem(DayNotificationsPlanPeriod dayNotificationsPlanPeriod);

        void onSwitchChanged(Week week, boolean open);
    }

    public WeekAdapter(@NonNull Context mCtx, @NonNull List<DayNotificationsPlanPeriod> list) {
        this.mCtx = mCtx;
        this.list = list;
    }

    @NonNull
    @Override
    public WeekViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.item_notifications_week, viewGroup, false);
        WeekViewHolder weekViewHolder = new WeekViewHolder(view);
        weekViewHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickWeekItem != null) {
                    onClickWeekItem.onClickItem((DayNotificationsPlanPeriod) view.getTag());
                }
            }
        });
        return weekViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final WeekViewHolder weekViewHolder, final int i) {
        weekViewHolder.tvWeek.setText(Util.formatWeek(list.get(i).getWeek()));
        String start = DateTimeUtil.formatDayTimeByMinute(list.get(i).getStart());
        String stop = DateTimeUtil.formatDayTimeByMinute(list.get(i).getEnd());
        weekViewHolder.tvTime.setText(String.format("%s-%s", start, stop));

        if (weekViewHolder.switchStatus.isChecked() != list.get(i).isOpen()) {
            weekViewHolder.switchStatus.toggleNoCallback();
        }

        if (list.get(i).isOpen()) {
            weekViewHolder.tvTime.setVisibility(View.VISIBLE);
        } else {
            weekViewHolder.tvTime.setVisibility(View.INVISIBLE);
        }

        weekViewHolder.switchStatus.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (onClickWeekItem != null)
                    onClickWeekItem.onSwitchChanged(list.get(i).getWeek(), isChecked);
            }
        });

        weekViewHolder.container.setTag(list.get(i));
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    class WeekViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvWeek)
        TextView tvWeek;
        @BindView(R.id.tvTime)
        TextView tvTime;
        @BindView(R.id.switchStatus)
        SwitchButton switchStatus;
        @BindView(R.id.container)
        RelativeLayout container;

        public WeekViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
