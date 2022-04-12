package com.afar.osaio.smart.home.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.bean.ParentalControlDeviceInfo;
import com.nooie.common.utils.collection.CollectionUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class ParentalControlAdapter extends RecyclerView.Adapter<ParentalControlAdapter.ParentalControlViewHolder> {

    private List<ParentalControlDeviceInfo> mDeviceList = new ArrayList<>();
    private StringBuffer stringBuffer = new StringBuffer();

    @NonNull
    @NotNull
    @Override
    public ParentalControlViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(NooieApplication.mCtx);
        View view = layoutInflater.inflate(R.layout.layout_parental_control_item, parent, false);
        return new ParentalControlViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull @NotNull ParentalControlViewHolder parentalControlViewHolder, int position) {
        ParentalControlDeviceInfo parentalControlDeviceInfo = mDeviceList.get(position);
        parentalControlViewHolder.container.setTag(position);

        parentalControlViewHolder.itemName.setVisibility(View.VISIBLE);
        parentalControlViewHolder.itemName.setText(parentalControlDeviceInfo.getDeviceName());
        if ("".equals(parentalControlDeviceInfo.getStartTime())) {
            parentalControlViewHolder.saveTime.setVisibility(View.GONE);
        } else {
            String[] startTimeArray = parentalControlDeviceInfo.getStartTime().split(":");
            String[] endTimeArray = parentalControlDeviceInfo.getEndTime().split(":");
            String startTime = "";
            String endTime = "";
            String startTimeH = ""; // 开始时间的小时
            String startTimeM = ""; // 开始时间分钟
            String endTimeH = ""; // 结束时间的小时
            String endTimeM = ""; // 结束时间的分钟

            if (Integer.parseInt(startTimeArray[0]) < 10) {
                startTimeH = "0" + startTimeArray[0];
            } else {
                startTimeH = startTimeArray[0];
            }

            if (Integer.parseInt(startTimeArray[1]) < 10) {
                startTimeM = ":0" + startTimeArray[1];
            } else {
                startTimeM = ":" + startTimeArray[1];
            }
            startTime = startTimeH + startTimeM;


            if (Integer.parseInt(endTimeArray[0]) < 10) {
                endTimeH = "0" + endTimeArray[0];
            } else {
                endTimeH = endTimeArray[0];
            }

            if (Integer.parseInt(endTimeArray[1]) < 10) {
                endTimeM = ":0" + endTimeArray[1];
            } else {
                endTimeM = ":" + endTimeArray[1];
            }

            endTime = endTimeH + endTimeM;

            parentalControlViewHolder.saveTime.setText(startTime + " - " + endTime);
        }

        if (parentalControlDeviceInfo.getSaveDays() != null) {
            parentalControlViewHolder.tvDays.setVisibility(View.VISIBLE);
            parentalControlViewHolder.tvDays.setText(convertWeekDays(parentalControlDeviceInfo.getSaveDays()));
        } else {
            parentalControlViewHolder.tvDays.setVisibility(View.GONE);
        }
        /*parentalControlViewHolder.ivDeviceState.setVisibility(View.VISIBLE);
        parentalControlViewHolder.ivDeviceState.setImageResource(
                parentalControlDeviceInfo.isOnlineState() ? R.drawable.device_title_point_default
                                                          : R.drawable.device_title_point_default);*/
        if (parentalControlDeviceInfo.isShowStatus()) {
            parentalControlViewHolder.tvDeviceState.setText(R.string.router_protected);
            parentalControlViewHolder.iv_arrow.setVisibility(View.GONE);
            parentalControlViewHolder.saveTime.setVisibility(View.GONE);
            parentalControlViewHolder.tvDays.setVisibility(View.GONE);
        } else {
            parentalControlViewHolder.tvDeviceState.setVisibility(View.GONE);
        }


        parentalControlViewHolder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    int position = (int) view.getTag();
                    listener.ParentalControlItemClick(parentalControlDeviceInfo.getDeviceMac(), parentalControlDeviceInfo.getDeviceName(), parentalControlDeviceInfo.isOnlineState());
                }
            }
        });
    }

    public void setData(List<ParentalControlDeviceInfo> list) {
        if (list == null || list.size() < 0) {
            return;
        }

        mDeviceList.clear();
        mDeviceList.addAll(list);
        notifyDataSetChanged();
    }

    public void setUpdataStatus(String deviceMac, boolean isProtected) {
        if (mDeviceList == null || mDeviceList.size() <= 0) {
            return;
        }

        for (int i = 0; i < mDeviceList.size(); i++) {
            if (mDeviceList.get(i).getDeviceMac().equals(deviceMac)) {
                mDeviceList.get(i).setOnlineState(isProtected);
                notifyDataSetChanged();
                break;
            }

        }
    }

    public void updata() {
        notifyDataSetChanged();
    }

    public void removeDevice(int position) {
        if (mDeviceList != null && position < mDeviceList.size() && position >= 0) {
            mDeviceList.remove(position);
            notifyItemRemoved(position);
            notifyDataSetChanged();
        }
    }

    private OnClickParentalControlItemLicktener listener;

    public void setParentalControlClickListener(OnClickParentalControlItemLicktener listener) {
        this.listener = listener;
    }

    public interface OnClickParentalControlItemLicktener {
        public void ParentalControlItemClick(String device, String deviceName, boolean isProtected);
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
                case Calendar.MONDAY:
                    weekDaySb.append(NooieApplication.mCtx.getString(R.string.w_monday));
                    weekDaySb.append(" ");
                    break;
                case Calendar.TUESDAY:
                    weekDaySb.append(NooieApplication.mCtx.getString(R.string.w_tuesday));
                    weekDaySb.append(" ");
                    break;
                case Calendar.WEDNESDAY:
                    weekDaySb.append(NooieApplication.mCtx.getString(R.string.w_wednesday));
                    weekDaySb.append(" ");
                    break;
                case Calendar.THURSDAY:
                    weekDaySb.append(NooieApplication.mCtx.getString(R.string.w_thursday));
                    weekDaySb.append(" ");
                    break;
                case Calendar.FRIDAY:
                    weekDaySb.append(NooieApplication.mCtx.getString(R.string.w_friday));
                    weekDaySb.append(" ");
                    break;
                case Calendar.SATURDAY:
                    weekDaySb.append(NooieApplication.mCtx.getString(R.string.w_saturday));
                    weekDaySb.append(" ");
                    break;
                case Calendar.SUNDAY:
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
        return mDeviceList.size() > 0 ? mDeviceList.size() : 0;
    }

    public static class ParentalControlViewHolder extends RecyclerView.ViewHolder {

        ImageView imgIcon;

        TextView itemName;

        TextView saveTime;

        TextView tvDays;
        /*@BindView(R.id.ivDeviceState)
        ImageView ivDeviceState;*/

        TextView tvDeviceState;

        ImageView iv_arrow;

        View container;

        public ParentalControlViewHolder(@NonNull View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            itemName = itemView.findViewById(R.id.itemName);
            saveTime = itemView.findViewById(R.id.saveTime);
            tvDays = itemView.findViewById(R.id.tvDays);
            tvDeviceState = itemView.findViewById(R.id.tvDeviceState);
            iv_arrow = itemView.findViewById(R.id.iv_arrow);
            container = itemView;
        }
    }
}
