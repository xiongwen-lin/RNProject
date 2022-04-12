package com.afar.osaio.smart.electrician.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.ActivityStack;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.electrician.bean.ScheduleHelper;
import com.afar.osaio.smart.electrician.manager.GroupHelper;
import com.afar.osaio.smart.electrician.manager.PowerStripHelper;
import com.afar.osaio.smart.electrician.presenter.IScheduleActionPresenter;
import com.afar.osaio.smart.electrician.presenter.ScheduleActionPresenter;
import com.afar.osaio.smart.electrician.util.DateUtil;
import com.afar.osaio.smart.electrician.util.DialogUtil;
import com.afar.osaio.smart.electrician.view.ISheduleActionView;
import com.afar.osaio.smart.setting.adapter.HourWheelAdapter;
import com.afar.osaio.smart.setting.adapter.MinutesWheelAdapter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.SelectWeekView;
import com.contrarywind.listener.OnItemSelectedListener;
import com.contrarywind.view.WheelView;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * ScheduleActionActivity
 * 群组或单插设置定点定时
 *
 * @author Administrator
 * @date 2019/6/4
 */
public class ScheduleActionActivity extends BaseActivity implements ISheduleActionView {

    public final static int SWITCH_ON = 1;
    public final static int SWITCH_OFF = 0;

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.btnCreateScheduleOn)
    TextView btnCreateScheduleOn;
    @BindView(R.id.btnCreateScheduleOff)
    TextView btnCreateScheduleOff;
    @BindView(R.id.wheelStartHour)
    WheelView wheelStartHour;
    @BindView(R.id.wheelStartMinutes)
    WheelView wheelStartMinutes;
    @BindView(R.id.swvCreateSchedule)
    SelectWeekView swvCreateSchedule;
    @BindView(R.id.tvAtLabel)
    TextView tvAtLabel;
    @BindView(R.id.tvDay)
    TextView tvDay;

    private HourWheelAdapter mHourWheelAdapter;
    private MinutesWheelAdapter mMinutesWheelAdapter;
    private String mDeviceId;
    private long mGroupId;
    private String mOperateType;//单个设备定点定时；排插群组定点定时；灯的群组定点定时
    private IScheduleActionPresenter mScheduleActionPresenter;

    public static void toScheduleActionActivity(Context from, String deviceId, long groupId, String operate) {
        Intent intent = new Intent(from, ScheduleActionActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_GROUP_ID, groupId);
        intent.putExtra(ConstantValue.INTENT_KEY_SCHEDULE_GROUP_OPERATE, operate);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_action);
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.normal_timer);
        ivRight.setImageResource(R.drawable.define_black);
        setupSwitchBtn();
        setupFromAndToTime();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
            return;
        } else {
            mDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
            mGroupId = getCurrentIntent().getLongExtra(ConstantValue.INTENT_KEY_GROUP_ID, 0);
            mOperateType = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SCHEDULE_GROUP_OPERATE);
            mScheduleActionPresenter = new ScheduleActionPresenter(this);
        }
    }

    @OnClick({R.id.btnCreateScheduleOn, R.id.btnCreateScheduleOff, R.id.ivRight, R.id.ivLeft})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.btnCreateScheduleOn:
            case R.id.btnCreateScheduleOff:
                toggleSwitchBtn();
                break;
            case R.id.ivRight:
                long time = System.currentTimeMillis();
                int CLICK_TIME = 3000;
                if (time - lastClickTime > CLICK_TIME) {
                    lastClickTime = time;
                    doSchedule();
                }
                break;
            case R.id.ivLeft:
                finish();
                break;
        }
    }

    private void updateFromAndToView() {
        NooieLog.d("-->> NooieCreateDetectionScheduleActivity updateFromAndToView height=" + wheelStartHour.getItemHeight());
        int itemHeight = (int) wheelStartHour.getItemHeight() + 2;
        ViewGroup.LayoutParams layoutParams = tvAtLabel.getLayoutParams();
        layoutParams.height = (int) wheelStartHour.getItemHeight() + 2;
        tvAtLabel.setLayoutParams(layoutParams);
        ViewGroup.LayoutParams layoutParams2 = tvDay.getLayoutParams();
        layoutParams2.height = itemHeight;
        tvDay.setLayoutParams(layoutParams2);
    }

    private void doSchedule() {
        int currentH = getCurrentH();
        int currentM = getCurrentM();
        String time = currentH + ":" + currentM;
        String looper = ScheduleHelper.getInstance().getLooper(getSelectedWeekDays());
        DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(mDeviceId);
        if (deviceBean != null) {
            if (!deviceBean.getIsOnline()) {
                DialogUtil.showInformationDialog(this, getResources().getString(R.string.timer_mutually_exclusive_tip), getResources().getString(R.string.device_off_line_tip));
            }
        }
        if (mOperateType.equals(ConstantValue.SHEDULE_FOR_SINGLE_DEVICE)) {//单插定点定时
            doDeviceSchedule(time, looper);
        } else if (mOperateType.equals(ConstantValue.GROUP_FOR_PLUG)) {//插座群组定点定时
            tvTitle.setText(R.string.group_schedule);
            doGroupScheduleForPlug(time, looper);
        } else if (mOperateType.equals(ConstantValue.GROUP_FOR_LAMP)) {//智能灯群组的定点定时
            doGroupScheduleForLamp(time, looper);
        }
    }

    private void doGroupScheduleForLamp(String time, String looper) {
        Map<String, Object> dpsMap = new HashMap<>();
       /* if ((Integer) btnCreateScheduleOn.getTag() == SWITCH_ON) {
            dpsMap.put(PowerStripHelper.getInstance().getLed_switch_id(), true);
        } else {
            dpsMap.put(PowerStripHelper.getInstance().getLed_switch_id(), false);
        }*/
        if ((Integer) btnCreateScheduleOn.getTag() == SWITCH_ON) {
            dpsMap.put(PowerStripHelper.getInstance().getSwitch_led_id(), true);
        } else {
            dpsMap.put(PowerStripHelper.getInstance().getSwitch_led_id(), false);
        }
        showLoadingDialog();
        mScheduleActionPresenter.setScheduleAtion(String.valueOf(mGroupId), String.valueOf(mGroupId), looper, dpsMap, time);
    }

    private void doGroupScheduleForPlug(String time, String looper) {
        Map<String, Object> dpsMap = new HashMap<>();
        if ((Integer) btnCreateScheduleOn.getTag() == SWITCH_ON) {
            dpsMap.put(GroupHelper.getInstance().getSwitch_1_id(), true);
        } else {
            dpsMap.put(GroupHelper.getInstance().getSwitch_1_id(), false);
        }
        showLoadingDialog();
        mScheduleActionPresenter.setScheduleAtion(String.valueOf(mGroupId), String.valueOf(mGroupId), looper, dpsMap, time);
    }

    private void doDeviceSchedule(String time, String looper) {
        Map<String, Object> dpsMap = new HashMap<>();
        if ((Integer) btnCreateScheduleOn.getTag() == SWITCH_ON) {
            dpsMap.put(PowerStripHelper.getInstance().getSwitch_1_id(), true);
        } else {
            dpsMap.put(PowerStripHelper.getInstance().getSwitch_1_id(), false);
        }
        showLoadingDialog();
        mScheduleActionPresenter.setScheduleAtion(mDeviceId, mDeviceId, looper, dpsMap, time);
    }

    private void toggleSwitchBtn() {
        if ((int) btnCreateScheduleOn.getTag() == SWITCH_ON) {
            btnCreateScheduleOn.setTag(SWITCH_OFF);
            switchBtnOnAndOff(false);
        } else {
            btnCreateScheduleOn.setTag(SWITCH_ON);
            switchBtnOnAndOff(true);
        }
    }

    private void setupSwitchBtn() {
        btnCreateScheduleOn.setTag(SWITCH_ON);
        switchBtnOnAndOff(true);
    }

    private void switchBtnOnAndOff(boolean on) {
        if (on) {
            btnCreateScheduleOn.setTextColor(getResources().getColor(R.color.theme_white));
            btnCreateScheduleOff.setTextColor(getResources().getColor(R.color.theme_white));
            btnCreateScheduleOn.setBackgroundResource(R.drawable.button_schedule_action_on_radius_20);
            btnCreateScheduleOff.setBackgroundResource(R.drawable.button_schedule_action_off_radius_20);
        } else {
            btnCreateScheduleOn.setTextColor(getResources().getColor(R.color.theme_white));
            btnCreateScheduleOff.setTextColor(getResources().getColor(R.color.theme_white));
            btnCreateScheduleOn.setBackgroundResource(R.drawable.button_schedule_action_off_radius_20);
            btnCreateScheduleOff.setBackgroundResource(R.drawable.button_schedule_action_on_radius_20);
        }
    }

    private void setupFromAndToTime() {

        mHourWheelAdapter = new HourWheelAdapter();
        mMinutesWheelAdapter = new MinutesWheelAdapter();

        wheelStartHour.setAdapter(mHourWheelAdapter);
        wheelStartHour.setCurrentItem(DateUtil.getHour());

        wheelStartMinutes.setAdapter(mMinutesWheelAdapter);
        wheelStartMinutes.setCurrentItem(DateUtil.getMinute());

        wheelStartHour.post(new Runnable() {
            @Override
            public void run() {
                updateFromAndToView();
            }
        });

        wheelStartHour.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {

            }
        });

        wheelStartMinutes.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {

            }
        });
    }

    private int getCurrentH() {
        int currentH = 0;
        if (wheelStartHour != null && mHourWheelAdapter != null) {
            currentH = mHourWheelAdapter.getValue(wheelStartHour.getCurrentItem());
        }
        return currentH;
    }

    private int getCurrentM() {
        int currentM = 0;
        if (wheelStartMinutes != null && mMinutesWheelAdapter != null) {
            currentM = mMinutesWheelAdapter.getValue(wheelStartMinutes.getCurrentItem());
        }
        return currentM;
    }

    private List<Integer> getSelectedWeekDays() {
        return swvCreateSchedule.getSelectedDays();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void notifyScheduleAtionSuccess() {
        hideLoadingDialog();
        if (mOperateType.equals(ConstantValue.SHEDULE_FOR_SINGLE_DEVICE)) {
            //DeviceScheduleActivity.toDeviceScheduleActivity(this, mDeviceId, false);
            finish();
        } else {
            ActivityStack.instance().removeGroupScheduleActivity();
            GroupScheduleActivity.toGroupScheduleActivity(this, mGroupId);
            finish();
        }
    }

    @Override
    public void notifyScheduleActionFail(String error) {
        hideLoadingDialog();
    }

    private static long lastClickTime = 0;
}
