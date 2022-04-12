package com.afar.osaio.smart.setting.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.contrarywind.view.WheelView;
import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.bean.DetectionSchedule;
import com.afar.osaio.smart.setting.adapter.MinutesWheelAdapter;
import com.afar.osaio.smart.setting.adapter.HourWheelAdapter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.SelectWeekView;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NooieCreateDetectionScheduleActivity extends BaseActivity {

    public static void toNooieCreateDetectionScheduleActivity(Activity from, int requestCode, String deviceId, DetectionSchedule schedule, int scheduleId) {
        Intent intent = new Intent(from, NooieCreateDetectionScheduleActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_TYPE, schedule);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, scheduleId);
        from.startActivityForResult(intent, requestCode);
    }

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
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

    DetectionSchedule mSchedule;
    HourWheelAdapter mStartHourAdapter;
    MinutesWheelAdapter mStartMinutesAdapter;
    HourWheelAdapter mEndHourAdapter;
    MinutesWheelAdapter mEndMinutesAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_detection_schedule);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            return;
        } else {
            mSchedule = (DetectionSchedule)getCurrentIntent().getSerializableExtra(ConstantValue.INTENT_KEY_DATA_TYPE);
            if (mSchedule == null || !mSchedule.isEffective()) {
                mSchedule = new DetectionSchedule(0, 24 * 60 - 1, true);
                int scheduleId = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_DATA_ID, 0);
                mSchedule.setId(scheduleId);
            }
            NooieLog.d("-->> NooieCreateDetectionScheduleActivity initData schedule effective=" + mSchedule.isEffective());
        }
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.create_detection_schedule_title);
        ivRight.setVisibility(View.VISIBLE);
        ivRight.setImageResource(R.drawable.menu_confirm_icon_state_list);
        setupTimePicker(mSchedule);
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

        swvCreateSchedule.setBtnSelected(schedule.getWeekDays());
    }

    private void updateFromAndToView() {
        if (isDestroyed() || checkNull(wheelCsFromStartHour, tvFromLabel, tvToLabel, tvDay1, tvDay2)) {
            return;
        }
        //NooieLog.d("-->> NooieCreateDetectionScheduleActivity updateFromAndToView height=" + wheelCsFromStartHour.getItemHeight());
        int itemHeight = (int)wheelCsFromStartHour.getItemHeight() + 2;
        ViewGroup.LayoutParams layoutParams = tvFromLabel.getLayoutParams();
        layoutParams.height = (int)wheelCsFromStartHour.getItemHeight() + 2;
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

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
    }

    private void release() {
        ivLeft = null;
        tvTitle = null;
        ivRight = null;
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

    @OnClick({R.id.ivLeft, R.id.ivRight})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.ivRight:
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
        mSchedule.setWeekDays(swvCreateSchedule.getSelectedDays());
        mSchedule.setOpen(CollectionUtil.isNotEmpty(swvCreateSchedule.getSelectedDays()));

        Intent intent = new Intent();
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_TYPE, mSchedule);
        setResult(RESULT_OK, intent);
        finish();
    }
}
