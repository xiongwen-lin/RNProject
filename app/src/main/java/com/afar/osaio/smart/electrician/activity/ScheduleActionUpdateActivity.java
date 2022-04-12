package com.afar.osaio.smart.electrician.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.ActivityStack;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.electrician.bean.DeviceHelper;
import com.afar.osaio.smart.electrician.bean.ScheduleHelper;
import com.afar.osaio.smart.electrician.manager.GroupHelper;
import com.afar.osaio.smart.electrician.presenter.IScheduleActionUpdatePresenter;
import com.afar.osaio.smart.electrician.presenter.ScheduleActionUpdatePresenter;
import com.afar.osaio.smart.electrician.util.DialogUtil;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.view.ISheduleActionUpdateView;
import com.afar.osaio.smart.setting.adapter.HourWheelAdapter;
import com.afar.osaio.smart.setting.adapter.MinutesWheelAdapter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.SelectWeekView;
import com.contrarywind.listener.OnItemSelectedListener;
import com.contrarywind.view.WheelView;
import com.google.gson.Gson;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * ScheduleActionUpdateActivity
 *
 * @author Administrator
 * @date 2019/6/5
 */
public class ScheduleActionUpdateActivity extends BaseActivity implements ISheduleActionUpdateView {

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
    private boolean isScheduleDevice;
    private String mOperateType;//单个设备定点定时；排插群组定点定时；灯的群组定点定时
    private IScheduleActionUpdatePresenter mScheduleActionUpdatePresenter;
    private com.tuya.smart.sdk.bean.Timer mTimer;

    public static void toScheduleActionUpdateActivity(Context from, String deviceId, long groupId, String timer, String operate) {
        Intent intent = new Intent(from, ScheduleActionUpdateActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_GROUP_ID, groupId);
        intent.putExtra("timer", timer);
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
            String timerJson = getCurrentIntent().getStringExtra("timer");
            mTimer = new Gson().fromJson(timerJson, com.tuya.smart.sdk.bean.Timer.class);
            isScheduleDevice = !TextUtils.isEmpty(mDeviceId);
            mOperateType = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SCHEDULE_GROUP_OPERATE);
            mScheduleActionUpdatePresenter = new ScheduleActionUpdatePresenter(this);
            setTimerSelected();
        }
    }


    @OnClick({R.id.btnCreateScheduleOn, R.id.btnCreateScheduleOff, R.id.ivLeft, R.id.ivRight})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.btnCreateScheduleOn:
            case R.id.btnCreateScheduleOff:
                toggleSwitchBtn();
                break;
            case R.id.ivLeft:
                finish();
                break;
            case R.id.ivRight:
                doSchedule();
                break;
        }
    }

    private void doSchedule() {
        String time = getCurrentH() + ":" + getCurrentM();
        String looper = ScheduleHelper.getInstance().getLooper(getSelectedWeekDays());
        String onOrOff;
        if ((Integer) btnCreateScheduleOn.getTag() == SWITCH_ON) {
            onOrOff = "true";
        } else {
            onOrOff = "false";
        }
        String instructFormat = " [{\"time\":\"%s\",\"dps\":{\"%s\":%s}}]";
        String dpId = "1";

        DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(mDeviceId);
        if (deviceBean != null) {
            if (!deviceBean.getIsOnline()) {
                DialogUtil.showInformationDialog(this, getResources().getString(R.string.timer_mutually_exclusive_tip), getResources().getString(R.string.device_off_line_tip));
            }
        }

        if (mOperateType.equals(ConstantValue.SHEDULE_FOR_SINGLE_DEVICE)) {//单插定点定时
            dpId = DeviceHelper.getInstance().getSwitch_id();
        } else if (mOperateType.equals(ConstantValue.GROUP_FOR_PLUG)) {//插座的定点定时
            dpId = GroupHelper.getInstance().getSwitch_1_id();
        } else if (mOperateType.equals(ConstantValue.GROUP_FOR_LAMP)) {//智能灯群组的定点定时
            dpId = "20";
        }

        String instruct = String.format(instructFormat, time, dpId, onOrOff);

        if (isScheduleDevice) {
            showLoadingDialog();
            mScheduleActionUpdatePresenter.updateTimerWithTask(mDeviceId, looper, mDeviceId, mTimer.getTimerId(), instruct);
        } else {
            showLoadingDialog();
            mScheduleActionUpdatePresenter.updateTimerWithTask(String.valueOf(mGroupId), looper, String.valueOf(mGroupId), mTimer.getTimerId(), instruct);
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
        wheelStartHour.setCurrentItem(0);

        wheelStartMinutes.setAdapter(mMinutesWheelAdapter);
        wheelStartMinutes.setCurrentItem(0);

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

    public void setTimerSelected() {
        if (mTimer != null) {
            try {
                Map<String, Object> dpsMap = ScheduleHelper.getInstance().convertDps(mTimer.getValue());
                if (dpsMap != null || dpsMap.size() > 0) {
                    boolean isOn = (boolean) dpsMap.get(mTimer.getDpId());
                    if (isOn) {
                        btnCreateScheduleOn.setTag(SWITCH_ON);
                    } else {
                        btnCreateScheduleOn.setTag(SWITCH_OFF);
                    }
                    switchBtnOnAndOff(isOn);
                }
                String[] timeSplit = mTimer.getTime().split(":");
                wheelStartHour.setCurrentItem(Integer.parseInt(timeSplit[0]));
                wheelStartMinutes.setCurrentItem(Integer.parseInt(timeSplit[1]));
                swvCreateSchedule.setActionTimerSelectedDays(mTimer.getLoops());
            } catch (Exception e) {
                NooieLog.e("----->>> e " + e.getMessage());
            }
        }
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
    public void notifyUpdateTimerWithTaskSuccess() {
        hideLoadingDialog();
        if (isScheduleDevice) {
            //DeviceScheduleActivity.toDeviceScheduleActivity(this, mDeviceId, false);
            finish();
        } else {
          /*ActivityStack.instance().removeGroupActivity();
          GroupActivity.toGroupActivity(this,mGroupId);*/
            ActivityStack.instance().removeGroupScheduleActivity();
            GroupScheduleActivity.toGroupScheduleActivity(this, mGroupId);
            finish();
        }
    }

    @Override
    public void notifyUpdateTimerWithTaskFail(String errorCode, String errorMessage) {
        hideLoadingDialog();
        ErrorHandleUtil.toastTuyaError(this, errorMessage);
    }
}
