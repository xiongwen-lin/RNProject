package com.afar.osaio.smart.setting.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.afar.osaio.smart.event.RouterOnLineStateEvent;
import com.afar.osaio.smart.router.RouterBaseActivity;
import com.contrarywind.listener.OnItemSelectedListener;
import com.contrarywind.view.WheelView;
import com.afar.osaio.R;
import com.afar.osaio.bean.DetectionSchedule;
import com.afar.osaio.routerdata.GetRouterDataFromCloud;
import com.afar.osaio.routerdata.SendHttpRequest;
import com.afar.osaio.smart.device.bean.ParentalControlDeviceInfo;
import com.afar.osaio.smart.setting.adapter.HourWheelAdapter;
import com.afar.osaio.smart.setting.adapter.MinutesWheelAdapter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.SelectWeekView;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 设置时间
 */
public class RouterDeviceCreateDetectionScheduleActivity extends RouterBaseActivity implements SendHttpRequest.getRouterReturnInfo {

    public static void toRouterDeviceCreateDetectionScheduleActivity(Activity from, int requestCode, DetectionSchedule schedule, int scheduleId) {
        Intent intent = new Intent(from, RouterDeviceCreateDetectionScheduleActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_TYPE, schedule);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, scheduleId);
        from.startActivityForResult(intent, requestCode);
    }

    public static void toRouterDeviceCreateDetectionScheduleActivity(Activity from, int requestCode, DetectionSchedule schedule, int scheduleId, ParentalControlDeviceInfo onLinedeviceList) {
        Intent intent = new Intent(from, RouterDeviceCreateDetectionScheduleActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_TYPE, schedule);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, scheduleId);
        intent.putExtra(ConstantValue.INTENT_NOLINE_DEVICE_LIST, (Serializable) onLinedeviceList);
        from.startActivityForResult(intent, requestCode);
    }

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.wheelCsFromStartHour)
    WheelView wheelCsFromStartHour;
    @BindView(R.id.wheelCsFromStartMinutes)
    WheelView wheelCsFromStartMinutes;
    @BindView(R.id.wheelCsToStartHour)
    WheelView wheelCsToStartHour;
    @BindView(R.id.wheelCsToStartMinutes)
    WheelView wheelCsToStartMinutes;
    @BindView(R.id.swvCreateSchedule)
    SelectWeekView swvCreateSchedule;
    @BindView(R.id.tvFromLabel)
    TextView tvFromLabel;
    @BindView(R.id.tvToLabel)
    TextView tvToLabel;
    @BindView(R.id.tvDay1)
    TextView tvDay1;
    @BindView(R.id.tvDay2)
    TextView tvDay2;
    @BindView(R.id.startTime)
    TextView startTime;
    @BindView(R.id.endTime)
    TextView endTime;

    private String localClassName = "";
    DetectionSchedule mSchedule;
    HourWheelAdapter mStartHourAdapter;
    MinutesWheelAdapter mStartMinutesAdapter;
    HourWheelAdapter mEndHourAdapter;
    MinutesWheelAdapter mEndMinutesAdapter;

    ParentalControlDeviceInfo onLinedevice = null;
    private Integer[] weeksArray = {1, 2, 3, 4, 5, 6, 7}; // 未选任何日期时,默认全选
    JSONObject jsonObject = new JSONObject();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_create_detection_schedule);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            return;
        } else {
            mSchedule = (DetectionSchedule) getCurrentIntent().getSerializableExtra(ConstantValue.INTENT_KEY_DATA_TYPE);
            onLinedevice = (ParentalControlDeviceInfo) getCurrentIntent().getSerializableExtra(ConstantValue.INTENT_NOLINE_DEVICE_LIST);

            if (mSchedule == null || !mSchedule.isEffective()) {
                mSchedule = new DetectionSchedule(0, 24 * 60 - 1, true);
                int scheduleId = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_DATA_ID, 0);
                mSchedule.setId(scheduleId);
            }
            NooieLog.d("-->> NooieCreateDetectionScheduleActivity initData schedule effective=" + mSchedule.isEffective());
        }

        getNtpCfg();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.create_detection_schedule_title);
        setupTimePicker(mSchedule);
        syncFromWheelView();
    }

    private void setupTimePicker(DetectionSchedule schedule) {
        mStartHourAdapter = new HourWheelAdapter();
        mStartMinutesAdapter = new MinutesWheelAdapter();
        mEndHourAdapter = new HourWheelAdapter();
        mEndMinutesAdapter = new MinutesWheelAdapter();

        wheelCsFromStartHour.setAdapter(mStartHourAdapter);
        wheelCsFromStartMinutes.setAdapter(mStartMinutesAdapter);
        wheelCsToStartHour.setAdapter(mEndHourAdapter);
        wheelCsToStartMinutes.setAdapter(mEndMinutesAdapter);

        wheelCsFromStartHour.setCurrentItem(mStartHourAdapter.indexOf(String.valueOf(schedule.getStartH())));
        wheelCsFromStartMinutes.setCurrentItem(mStartMinutesAdapter.indexOf(String.valueOf(schedule.getStartM())));
        wheelCsToStartHour.setCurrentItem(mEndHourAdapter.indexOf(String.valueOf(schedule.getEndH())));
        wheelCsToStartMinutes.setCurrentItem(mEndMinutesAdapter.indexOf(String.valueOf(schedule.getEndM())));
        wheelCsFromStartHour.post(new Runnable() {
            @Override
            public void run() {
                updateFromAndToView();
            }
        });

        wheelCsFromStartHour.setTag(mStartHourAdapter.indexOf(String.valueOf(schedule.getStartH())));
        wheelCsFromStartMinutes.setTag(mStartMinutesAdapter.indexOf(String.valueOf(schedule.getStartM())));
        wheelCsToStartHour.setTag(mEndHourAdapter.indexOf(String.valueOf(schedule.getEndH())));
        wheelCsToStartMinutes.setTag(mEndMinutesAdapter.indexOf(String.valueOf(schedule.getEndM())));

        StringBuffer startTimeString = new StringBuffer();
        StringBuffer endTimeString = new StringBuffer();
        startTimeString.append(mStartHourAdapter.indexOf(String.valueOf(schedule.getStartH())) < 10
                ? "0" + mStartHourAdapter.indexOf(String.valueOf(schedule.getStartH()))
                : mStartHourAdapter.indexOf(String.valueOf(schedule.getStartH())));
        startTimeString.append(":").append(mStartMinutesAdapter.indexOf(String.valueOf(schedule.getStartM())) < 10
                ? "0" + mStartMinutesAdapter.indexOf(String.valueOf(schedule.getStartM()))
                : mStartMinutesAdapter.indexOf(String.valueOf(schedule.getStartM())));

        endTimeString.append(mEndHourAdapter.indexOf(String.valueOf(schedule.getEndH())) < 10
                ? "0" + mEndHourAdapter.indexOf(String.valueOf(schedule.getEndH()))
                : mEndHourAdapter.indexOf(String.valueOf(schedule.getEndH())));
        endTimeString.append(":").append(mEndMinutesAdapter.indexOf(String.valueOf(schedule.getEndM())) < 10
                ? "0" + mEndMinutesAdapter.indexOf(String.valueOf(schedule.getEndM()))
                : mEndMinutesAdapter.indexOf(String.valueOf(schedule.getEndM())));

        startTime.setText(startTimeString);
        endTime.setText(endTimeString);

        NooieLog.d("------------> mSchedule: " + mStartHourAdapter.indexOf(String.valueOf(schedule.getStartH()))
                + "  " + mStartMinutesAdapter.indexOf(String.valueOf(schedule.getStartM()))
                + "  " + mEndHourAdapter.indexOf(String.valueOf(schedule.getEndH()))
                + "  " + mEndMinutesAdapter.indexOf(String.valueOf(schedule.getEndM()))
                + "  " + startTimeString
                + "  " + endTimeString);

        swvCreateSchedule.setBtnSelected(schedule.getWeekDays());
    }

    private void syncFromWheelView() {
        if (checkNull(wheelCsFromStartHour, wheelCsFromStartMinutes, wheelCsToStartHour, wheelCsToStartMinutes,
                mStartHourAdapter, mStartMinutesAdapter, mEndHourAdapter, mEndMinutesAdapter)) {
            return;
        }

        wheelCsFromStartHour.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                wheelCsFromStartHour.setTag(index);
                StringBuffer timeString = new StringBuffer();
                if (index < 10) {
                    timeString.append("0").append(index);
                } else {
                    timeString.append(index);
                }
                timeString.append(":").append((int) wheelCsFromStartMinutes.getTag() < 10
                        ? "0" + (int) wheelCsFromStartMinutes.getTag()
                        : (int) wheelCsFromStartMinutes.getTag());
                startTime.setText(timeString);
                wheelCsFromStartHour.setCurrentItem(index);
            }
        });

        wheelCsFromStartMinutes.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                wheelCsFromStartMinutes.setTag(index);
                StringBuffer timeString = new StringBuffer();
                timeString.append((int) wheelCsFromStartHour.getTag() < 10
                        ? "0" + wheelCsFromStartHour.getTag()
                        : wheelCsFromStartHour.getTag()).append(":");
                if (index < 10) {
                    timeString.append("0").append(index);
                } else {
                    timeString.append(index);
                }
                startTime.setText(timeString);
                wheelCsFromStartMinutes.setCurrentItem(index);
            }
        });

        wheelCsToStartHour.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                wheelCsToStartHour.setTag(index);
                StringBuffer timeString = new StringBuffer();
                if (index < 10) {
                    timeString.append("0").append(index);
                } else {
                    timeString.append(index);
                }
                timeString.append(":").append((int) wheelCsToStartMinutes.getTag() < 10
                        ? "0" + (int) wheelCsToStartMinutes.getTag()
                        : (int) wheelCsToStartMinutes.getTag());
                endTime.setText(timeString);
                wheelCsToStartHour.setCurrentItem(index);
            }
        });

        wheelCsToStartMinutes.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                wheelCsToStartMinutes.setTag(index);
                StringBuffer timeString = new StringBuffer();
                timeString.append((int) wheelCsToStartHour.getTag() < 10
                        ? "0" + wheelCsToStartHour.getTag()
                        : wheelCsToStartHour.getTag()).append(":");
                if (index < 10) {
                    timeString.append("0").append(index);
                } else {
                    timeString.append(index);
                }
                endTime.setText(timeString);
                wheelCsToStartMinutes.setCurrentItem(index);
            }
        });
    }

    private void updateFromAndToView() {
        if (isDestroyed() || checkNull(wheelCsFromStartHour, tvFromLabel, tvToLabel, tvDay1, tvDay2)) {
            return;
        }
        //NooieLog.d("-->> NooieCreateDetectionScheduleActivity updateFromAndToView height=" + wheelCsFromStartHour.getItemHeight());
        int itemHeight = (int) wheelCsFromStartHour.getItemHeight() + 2;
        ViewGroup.LayoutParams layoutParams = tvFromLabel.getLayoutParams();
        layoutParams.height = (int) wheelCsFromStartHour.getItemHeight() + 2;
        tvFromLabel.setLayoutParams(layoutParams);
        ViewGroup.LayoutParams layoutParams2 = tvToLabel.getLayoutParams();
        layoutParams2.height = itemHeight;
        tvToLabel.setLayoutParams(layoutParams2);
        ViewGroup.LayoutParams layoutParams3 = tvDay1.getLayoutParams();
        layoutParams3.height = itemHeight;
        tvDay1.setLayoutParams(layoutParams3);
        ViewGroup.LayoutParams layoutParams4 = tvDay2.getLayoutParams();
        layoutParams4.height = itemHeight;
        tvDay2.setLayoutParams(layoutParams4);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void resumeData() {
    }

    private void release() {
        ivLeft = null;
        tvTitle = null;
        if (wheelCsFromStartHour != null) {
            wheelCsFromStartHour.setAdapter(null);
        }
        if (wheelCsFromStartMinutes != null) {
            wheelCsFromStartMinutes.setAdapter(null);
        }
        if (wheelCsToStartHour != null) {
            wheelCsToStartHour.setAdapter(null);
        }
        if (wheelCsToStartMinutes != null) {
            wheelCsToStartMinutes.setAdapter(null);
        }
        if (swvCreateSchedule != null) {
            swvCreateSchedule.release();
            swvCreateSchedule = null;
        }
        tvFromLabel = null;
        tvToLabel = null;
        tvDay1 = null;
        tvDay2 = null;
        if (mStartHourAdapter != null) {
            mStartHourAdapter = null;
        }
        if (mStartMinutesAdapter != null) {
            mStartMinutesAdapter = null;
        }
        if (mEndHourAdapter != null) {
            mEndHourAdapter = null;
        }
        if (mEndMinutesAdapter != null) {
            mEndMinutesAdapter = null;
        }
        if (mSchedule != null) {
            mSchedule.clear();
            mSchedule = null;
        }
    }

    @OnClick({R.id.ivLeft, R.id.btnSave})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnSave:
                saveDetectionSchedule();
                break;
        }
    }

    private void saveDetectionSchedule() {
        if (checkNull(wheelCsFromStartHour, wheelCsFromStartMinutes, wheelCsToStartHour, wheelCsToStartMinutes, swvCreateSchedule, mSchedule)) {
            return;
        }

        int start = mStartHourAdapter.getValue(wheelCsFromStartHour.getCurrentItem()) * 60 + mStartMinutesAdapter.getValue(wheelCsFromStartMinutes.getCurrentItem());
        int end = mEndHourAdapter.getValue(wheelCsToStartHour.getCurrentItem()) * 60 + mEndMinutesAdapter.getValue(wheelCsToStartMinutes.getCurrentItem());
        if (start >= end) {
            ToastUtil.showToast(this, R.string.notifications_invalid_tip);
            return;
        }
        mSchedule.setStart(start);
        mSchedule.setEnd(end);
        if (swvCreateSchedule.getSelectedDays().size() == 0) {
            mSchedule.setWeekDays(Arrays.asList(weeksArray));
        } else {
            mSchedule.setWeekDays(swvCreateSchedule.getSelectedDays());
        }

        mSchedule.setOpen(CollectionUtil.isNotEmpty(swvCreateSchedule.getSelectedDays()));

        if (onLinedevice != null) {
            showLoadingDialog();
            setRouterTimeRules(mSchedule);
        } else {
            Intent intent = new Intent();
            intent.putExtra(ConstantValue.INTENT_KEY_DATA_TYPE, mSchedule);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    private void setRouterTimeRules(DetectionSchedule schedule) {
        schedule.setDeviceName(onLinedevice.getDeviceName());
        schedule.setDeviceMac(onLinedevice.getDeviceMac());
        setParentalRules(schedule);
    }


    /**
     * 设置 家长管理规则
     *
     * @param schedule
     */
    private void setParentalRules(DetectionSchedule schedule) {
        StringBuffer stringBuffer = new StringBuffer();
        StringBuilder startTime = new StringBuilder();
        StringBuilder endTime = new StringBuilder();
        List<Integer> listDay = schedule.getWeekDays();

        if (listDay.size() < 0) {
            return;
        }

        if (listDay.size() == 0) {
            schedule.resetWeekDays(true);
            listDay = schedule.getWeekDays();
        }

        for (int i = 0; i < listDay.size(); i++) {
            // 原日期采用国外方式,周天为一周第一天,也就是用了1代表  周1--6 用了 2--7代表,这边设置路由器需要转化下格式
            stringBuffer.append("" + (listDay.get(i) != 1 ? listDay.get(i) - 1 : 7));
            if (i != (listDay.size() - 1)) {
                stringBuffer.append(";");
            }
        }
        startTime.append(schedule.getStartH()).append(":").append(schedule.getStartM());
        endTime.append(schedule.getEndH()).append(":").append(schedule.getEndM());


        setRouterParentalRules(schedule, stringBuffer);
        EventBus.getDefault().post(new ParentalControlDeviceInfo(schedule.getDeviceMac(), schedule.getDeviceName(), startTime.toString(), endTime.toString(), schedule.getWeekDays(), true, false));
    }

    private void setRouterParentalRules(DetectionSchedule schedule, StringBuffer stringBuffer) {
        try {
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            routerDataFromCloud.setParentalRules(schedule.getDeviceMac(), schedule.getDeviceName(),
                    stringBuffer.toString(), schedule.getStartH() + ":" + schedule.getStartM(),
                    schedule.getEndH() + ":" + schedule.getEndM(), "1", "0");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getNtpCfg() {
        showLoadingDialog();
        GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
        try {
            routerDataFromCloud.getNtpCfg();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 强制同步时区
     */
    private void setNtpCfg() {
        GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
        try {
            routerDataFromCloud.setNtpCfg("UTC" + timeZoneConversion()
                    , "1", jsonObject.getString("server"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                setNtpCfg();
            } else {
                EventBus.getDefault().post(new RouterOnLineStateEvent());
            }
        }
    };

    @Override
    public void routerReturnInfo(String info, String topicurlString) {
        hideLoadingDialog();
        try {
            Message message = new Message();
            if ("setParentalRules".equals(topicurlString) && !"error".equals(info)) {
                finish();
            } else if ("getNtpCfg".equals(topicurlString) && !"error".equals(info)) {
                jsonObject = new JSONObject(info);
                message.what = 0;
                handler.sendMessage(message);
            } else if ("setNtpCfg".equals(topicurlString) && !"error".equals(info)) {

            } else {
                message.what = 1;
                handler.sendMessage(message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
    }

    @Override
    public void showLoadingDialog() {
        showLoading("");
    }

    @Override
    public void hideLoadingDialog() {
        hideLoading();
    }

    @Override
    public String timeZoneConversion() {
        return super.timeZoneConversion();
    }
}

