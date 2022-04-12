package com.afar.osaio.smart.setting.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.AlarmLevel;
import com.afar.osaio.bean.DetectionSchedule;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.setting.presenter.INooieDetectionPresenter;
import com.afar.osaio.smart.setting.presenter.NooieDetectionPresenter;
import com.afar.osaio.smart.setting.view.INooieDetectionView;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.nooie.common.utils.collection.CollectionUtil;

import com.nooie.data.EventDictionary;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.device.bean.MTAreaInfo;
import com.suke.widget.SwitchButton;


import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

// set/get motion detectin level workflow
/*

                                        ---- ON ---- save to cache ------
                                       /                                 \
                                      /                                   \
 ---- set level ---- sleep state -----                                     ----- finish ----
                                      \                                   /
                                       \                                 /
                                        ---- OFF ---- save to camera ----

                                       ---- ON ---- get from cache ------
                                      /                                  \
                                     /                                    \
 ---- get level ---- sleep state ----                                      ----- finish ----
                                     \                                    /
                                      \                                  /
                                       ---- OFF ---- get fron camera ----

 NOTE: 当关闭sleep时会把缓存里的值设置过去
 */

public class NooieDetectionActivity extends BaseActivity implements INooieDetectionView {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.tvDetectionTip)
    TextView tvDetectionTip;
    @BindView(R.id.tvDetectionSwitch)
    TextView tvDetectionSwitch;
    @BindView(R.id.sbDetectionSwitch)
    SwitchButton sbDetectionSwitch;
    @BindView(R.id.tvDetectionLow)
    TextView tvDetectionLow;
    @BindView(R.id.tvDetectionMedium)
    TextView tvDetectionMedium;
    @BindView(R.id.tvDetectionHigh)
    TextView tvDetectionHigh;
    @BindView(R.id.vDetectionSensitivityContainer)
    View vDetectionSensitivityContainer;
    @BindView(R.id.vDetectionZoneContainer)
    View vDetectionZoneContainer;
    @BindView(R.id.tvDetectionZoneState)
    TextView tvDetectionZoneState;
    @BindView(R.id.vDetectionScheduleContainer)
    View vDetectionScheduleContainer;
    @BindView(R.id.tvDetectionScheduleTime)
    TextView tvDetectionScheduleTime;
    
    private static final int SENSITIVITY_SELECTED = 0X40;
    private static final int SENSITIVITY_NO_SELECTED = 0X50;

    private AlarmLevel mDetectLevelCurrent;
    private String mDeviceId;
    private boolean mOpenCamera;
    private INooieDetectionPresenter mDetectionPresenter;

    private int mDetectType;

    public static void toNooieDetectionActivity(Context from, String deviceId, int detectType, boolean openCamera) {
        Intent intent = new Intent(from, NooieDetectionActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.NOOIE_INTENT_KEY_DETECT_TYPE, detectType);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM, openCamera);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
            return;
        } else {
            mDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
            mDetectType = getCurrentIntent().getIntExtra(ConstantValue.NOOIE_INTENT_KEY_DETECT_TYPE, ConstantValue.NOOIE_DETECT_TYPE_MOTION);
            mOpenCamera = true;//getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_DATA_PARAM, true);
            mDetectionPresenter = new NooieDetectionPresenter(this);
        }
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        if (mDetectType == ConstantValue.NOOIE_DETECT_TYPE_MOTION) {
            tvTitle.setText(R.string.camera_settings_motion_detection);
            tvDetectionTip.setText(R.string.detection_tip_of_motion);
            tvDetectionSwitch.setText(R.string.camera_settings_motion_detection);
        } else {
            tvTitle.setText(R.string.camera_settings_sound_detection);
            tvDetectionTip.setText(R.string.detection_tip_of_sound);
            tvDetectionSwitch.setText(R.string.camera_settings_sound_detection);
        }

        ivRight.setVisibility(View.GONE);

        displayDetectionSetting(false);
        sbDetectionSwitch.setOnCheckedChangeListener(mDetectionSwitchListener);
        tvDetectionLow.setTag(SENSITIVITY_NO_SELECTED);
        tvDetectionLow.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.gray_a1a1a1));
        tvDetectionMedium.setTag(SENSITIVITY_NO_SELECTED);
        tvDetectionMedium.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.gray_a1a1a1));
        tvDetectionHigh.setTag(SENSITIVITY_NO_SELECTED);
        tvDetectionHigh.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.gray_a1a1a1));
    }

    private void displayDetectionSetting(boolean show) {
        if (isDestroyed() || checkNull(vDetectionSensitivityContainer, vDetectionZoneContainer, vDetectionScheduleContainer)) {
            return;
        }

        displayViewAll(false, vDetectionSensitivityContainer, vDetectionZoneContainer, vDetectionScheduleContainer);
        vDetectionSensitivityContainer.setVisibility(show ? View.VISIBLE: View.GONE);
        displayDetectionZone(show, mDetectType);
        vDetectionScheduleContainer.setVisibility(show ? View.VISIBLE : View.GONE);
        if (sbDetectionSwitch.isChecked() != show) {
            sbDetectionSwitch.toggleNoCallback();
        }
    }

    private void displayDetectionZone(boolean show, int detectType) {
        if (checkNull(vDetectionZoneContainer)) {
            return;
        }
        BindDevice device = getDevice(mDeviceId);
        boolean isShowDetectionZone = show && (detectType == ConstantValue.NOOIE_DETECT_TYPE_MOTION) && (device != null && NooieDeviceHelper.isSupportDetectionZone(device.getType(), device.getVersion()));
        vDetectionZoneContainer.setVisibility(isShowDetectionZone ? View.VISIBLE : View.GONE);
    }

    private void loadData() {
        if (mDetectionPresenter != null) {
            //mDetectionPresenter.getSleepStatus(mDeviceId);
            getDetectionLevel();
            mDetectionPresenter.getDetectionSchedules(mDetectType, mDeviceId);
            if (mDetectType == ConstantValue.NOOIE_DETECT_TYPE_MOTION) {
                mDetectionPresenter.getMtAreaInfo(mDeviceId);
            }
        }
    }

    private SwitchButton.OnCheckedChangeListener mDetectionSwitchListener = new SwitchButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(SwitchButton view, boolean isChecked) {
            if (mDetectionPresenter == null) {
                return;
            }
            if (isChecked) {
                mDetectLevelCurrent = AlarmLevel.High;
                //mDetectionPresenter.setDetectionLevel(mDetectType, mDeviceId, mDetectLevelCurrent, mOpenCamera);
                setDetectionLevel(mDetectLevelCurrent);
                setupSensitivity(mDetectLevelCurrent);
            } else {
                mDetectLevelCurrent = AlarmLevel.Close;
                //mDetectionPresenter.setDetectionLevel(mDetectType, mDeviceId, mDetectLevelCurrent, mOpenCamera);
                setDetectionLevel(mDetectLevelCurrent);
                setupSensitivity(mDetectLevelCurrent);
            }
        }
    };

    private void setDetectionLevel(AlarmLevel detectLevelCurrent) {
        if (mDetectionPresenter != null) {
            showLoading();
            mDetectionPresenter.setDetectionLevel(mDetectType, mDeviceId, detectLevelCurrent, mOpenCamera);
        }
    }

    private void getDetectionLevel() {
        if (mDetectionPresenter != null) {
            showLoading();
            mDetectionPresenter.getDetectionLevel(mDetectType, mDeviceId, mOpenCamera);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDetectionPresenter != null) {
            mDetectionPresenter.destroy();
            mDetectionPresenter = null;
        }
        release();
    }

    private void release() {
        ivLeft = null;
        tvTitle = null;
        ivRight = null;
        tvDetectionTip = null;
        tvDetectionSwitch = null;
        if (sbDetectionSwitch != null) {
            sbDetectionSwitch.setOnCheckedChangeListener(null);
        }
        tvDetectionLow = null;
        tvDetectionMedium = null;
        tvDetectionHigh = null;
        vDetectionSensitivityContainer = null;
        vDetectionZoneContainer = null;
        tvDetectionZoneState = null;
        vDetectionScheduleContainer = null;
        tvDetectionScheduleTime = null;
    }

    @OnClick({R.id.ivLeft, R.id.tvDetectionLow, R.id.tvDetectionMedium, R.id.tvDetectionHigh, R.id.vDetectionZoneContainer, R.id.vDetectionScheduleContainer})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.tvDetectionLow:
                if ((Integer) tvDetectionLow.getTag() == SENSITIVITY_NO_SELECTED) {
                    mDetectLevelCurrent = AlarmLevel.Low;
                    //mDetectionPresenter.setDetectionLevel(mDetectType, mDeviceId, mDetectLevelCurrent, mOpenCamera);
                    setDetectionLevel(mDetectLevelCurrent);
                    setupSensitivity(mDetectLevelCurrent);
                }
                break;
            case R.id.tvDetectionMedium:
                if ((Integer) tvDetectionMedium.getTag() == SENSITIVITY_NO_SELECTED) {
                    mDetectLevelCurrent = AlarmLevel.Medium;
                    //mDetectionPresenter.setDetectionLevel(mDetectType, mDeviceId, mDetectLevelCurrent, mOpenCamera);
                    setDetectionLevel(mDetectLevelCurrent);
                    setupSensitivity(mDetectLevelCurrent);
                }
                break;
            case R.id.tvDetectionHigh:
                if ((Integer) tvDetectionHigh.getTag() == SENSITIVITY_NO_SELECTED) {
                    mDetectLevelCurrent = AlarmLevel.High;
                    //mDetectionPresenter.setDetectionLevel(mDetectType, mDeviceId, mDetectLevelCurrent, mOpenCamera);
                    setDetectionLevel(mDetectLevelCurrent);
                    setupSensitivity(mDetectLevelCurrent);
                }
                break;
            case R.id.vDetectionZoneContainer:
                NooieDetectionZoneActivity.toNooieDetectionZoneActivity(NooieDetectionActivity.this, ConstantValue.REQUEST_CODE_SET_DETECTION_ZONE, mDeviceId);
                break;
            case R.id.vDetectionScheduleContainer:
                NooieDetectionScheduleActivity.toNooieDetectionScheduleActivity(NooieDetectionActivity.this, mDeviceId, mDetectType);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ConstantValue.REQUEST_CODE_SET_DETECTION_ZONE:
                    break;
            }
        }
    }

    @Override
    public void showLoadingDialog() {
        //showLoading();
    }

    @Override
    public void hideLoadingDialog() {
        //hideLoading();
    }

    @Override
    public void showError(String err) {
        if (isDestroyed()) {
            return;
        }

        ToastUtil.showToast(this, err);
    }

    @Override
    public void onGetSoundDetection(AlarmLevel detectLevel) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
        displayDetectionSetting(detectLevel != AlarmLevel.Close);
        this.mDetectLevelCurrent = detectLevel;
        setupSensitivity(detectLevel);
    }

    @Override
    public void notifySetDetectionResult(String result) {
        if (isDestroyed()) {
            return;
        }

        hideLoading();
        ToastUtil.showToast(this, ConstantValue.SUCCESS.equalsIgnoreCase(result) ? R.string.success : R.string.get_fail);
    }

    @Override
    public void notifyGetSleepStateSuccess(boolean openCamera) {
        mOpenCamera = openCamera;

        //showLoading();
        //mDetectionPresenter.getDetectionLevel(mDetectType, mDeviceId, mOpenCamera);
        getDetectionLevel();
        if (mDetectType == ConstantValue.NOOIE_DETECT_TYPE_MOTION) {
        } else if (mDetectType == ConstantValue.NOOIE_DETECT_TYPE_SOUND) {
        }
    }

    @Override
    public void notifyGetSleepStateFailed(String message) {
        if (isDestroyed()) {
            return;
        }

        //showLoading();
        ToastUtil.showToast(this, message);
        //mDetectionPresenter.getDetectionLevel(mDetectType, mDeviceId, mOpenCamera);
        getDetectionLevel();
    }

    @Override
    public void notifySetSleepStateResult(String result) {
        if (isDestroyed()) {
            return;
        }

        if (result.equals(ConstantValue.SUCCESS)) {
            //ToastUtil.showToast(NooieApplication.mCtx, R.string.success);
        } else {
            ToastUtil.showToast(this, result);
        }
    }

    private void setupSensitivity(AlarmLevel soundDetection) {
        tvDetectionLow.setTag(SENSITIVITY_NO_SELECTED);
        tvDetectionLow.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.gray_a1a1a1));
        tvDetectionMedium.setTag(SENSITIVITY_NO_SELECTED);
        tvDetectionMedium.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.gray_a1a1a1));
        tvDetectionHigh.setTag(SENSITIVITY_NO_SELECTED);
        tvDetectionHigh.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.gray_a1a1a1));

        if (soundDetection == AlarmLevel.Close) {
        } else if (soundDetection == AlarmLevel.Low) {
            tvDetectionLow.setTag(SENSITIVITY_SELECTED);
            tvDetectionLow.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.theme_green));
        } else if (soundDetection == AlarmLevel.Medium) {
            tvDetectionMedium.setTag(SENSITIVITY_SELECTED);
            tvDetectionMedium.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.theme_green));
        } else if (soundDetection == AlarmLevel.High) {
            tvDetectionHigh.setTag(SENSITIVITY_SELECTED);
            tvDetectionHigh.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.theme_green));
        }
    }

    @Override
    public void notifyGetDetectionSchedulesSuccess(int detectType, List<DetectionSchedule> schedules) {
        if (isDestroyed() || checkNull(tvDetectionScheduleTime) || CollectionUtil.isEmpty(schedules) || schedules.size() < 3) {
            return;
        }

        DetectionSchedule detectionSchedule = schedules.get(0);
        for (DetectionSchedule schedule : CollectionUtil.safeFor(schedules)) {
            if (schedule != null && schedule.isOpen()) {
                detectionSchedule = schedule;
                break;
            }
        }

        if (detectionSchedule != null) {
            StringBuilder timeSb = new StringBuilder();
            timeSb.append(detectionSchedule.getStartH() < 10 ? "0" + detectionSchedule.getStartH() : detectionSchedule.getStartH());
            timeSb.append(":");
            timeSb.append(detectionSchedule.getStartM() < 10 ? "0" + detectionSchedule.getStartM() : detectionSchedule.getStartM());
            timeSb.append(" - ");
            timeSb.append(detectionSchedule.getEndH() < 10 ? "0" + detectionSchedule.getEndH() : detectionSchedule.getEndH());
            timeSb.append(":");
            timeSb.append(detectionSchedule.getEndM() < 10 ? "0" + detectionSchedule.getEndM() : detectionSchedule.getEndM());
            tvDetectionScheduleTime.setText(timeSb.toString());
        }
    }

    @Override
    public void onGetMtAreaInfo(String result, MTAreaInfo info) {
        if (isDestroyed()) {
            return;
        }
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            refreshDetectionZoneView(info);
        } else {
            refreshDetectionZoneView(null);
        }
    }

    private void refreshDetectionZoneView(MTAreaInfo info) {
        if (checkNull(tvDetectionZoneState)) {
            return;
        }
        if (info == null) {
            tvDetectionZoneState.setText(R.string.cam_setting_night_vision_type_off);
            return;
        }
        tvDetectionZoneState.setText(info.state ? R.string.cam_setting_night_vision_type_on : R.string.cam_setting_night_vision_type_off);
    }

    private BindDevice getDevice(String deviceId) {
        return NooieDeviceHelper.getDeviceById(deviceId);
    }

    @Override
    public String getEventId(int trackType) {
        int detectType = getIntent() != null ? getIntent().getIntExtra(ConstantValue.NOOIE_INTENT_KEY_DETECT_TYPE, ConstantValue.NOOIE_DETECT_TYPE_MOTION) : ConstantValue.NOOIE_DETECT_TYPE_MOTION;
        if (detectType == ConstantValue.NOOIE_DETECT_TYPE_MOTION) {
            return EventDictionary.EVENT_ID_ACCESS_MOTION_DETECTION;
        } else if (detectType == ConstantValue.NOOIE_DETECT_TYPE_SOUND) {
            return EventDictionary.EVENT_ID_ACCESS_SOUND_DETECTION;
        }
        return null;
    }

    @Override
    public String getPageId() {
        int detectType = getIntent() != null ? getIntent().getIntExtra(ConstantValue.NOOIE_INTENT_KEY_DETECT_TYPE, ConstantValue.NOOIE_DETECT_TYPE_MOTION) : ConstantValue.NOOIE_DETECT_TYPE_MOTION;
        if (detectType == ConstantValue.NOOIE_DETECT_TYPE_MOTION) {
            return EventDictionary.EVENT_PAGE_MOTION_DETECTION;
        } else if (detectType == ConstantValue.NOOIE_DETECT_TYPE_SOUND) {
            return EventDictionary.EVENT_PAGE_SOUND_DETECTION;
        }
        return null;
    }
}
