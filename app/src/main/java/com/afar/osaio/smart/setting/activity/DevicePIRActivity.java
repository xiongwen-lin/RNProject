package com.afar.osaio.smart.setting.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.CurrentDeviceParam;
import com.afar.osaio.bean.DetectionSchedule;
import com.afar.osaio.bean.ShortLinkDeviceParam;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.setting.presenter.DevicePIRPresenter;
import com.afar.osaio.smart.setting.view.DevicePIRContract;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.device.bean.PirStateV2;
import com.nooie.sdk.device.bean.SensitivityLevel;
import com.suke.widget.SwitchButton;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DevicePIRActivity extends BaseActivity implements DevicePIRContract.View {

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

    private SensitivityLevel mDetectLevelCurrent;
    private String mDeviceId;

    private DevicePIRContract.Presenter mPresenter;

    public static void toDevicePIRActivity(Context from, Bundle param) {
        Intent intent = new Intent(from, DevicePIRActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_START_PARAM, param);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
            return;
        } else {
            mDeviceId = getDeviceId();
            new DevicePIRPresenter(this);
        }

        tvTitle.setText(R.string.cam_setting_pir_detection);
        tvDetectionTip.setText(R.string.device_pir_tip);
        tvDetectionSwitch.setText(R.string.cam_setting_pir_detection);

        ivRight.setVisibility(View.GONE);

        displayDetectionSetting(false);
        setupPirModeView();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
    }

    private void setupPirModeView() {
        tvDetectionLow.setTag(SENSITIVITY_NO_SELECTED);
        tvDetectionLow.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.gray_a1a1a1));
        tvDetectionMedium.setTag(SENSITIVITY_NO_SELECTED);
        tvDetectionMedium.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.gray_a1a1a1));
        tvDetectionHigh.setTag(SENSITIVITY_NO_SELECTED);
        tvDetectionHigh.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.gray_a1a1a1));

        sbDetectionSwitch.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (mPresenter == null) {
                    return;
                }
                mDetectLevelCurrent = SensitivityLevel.SENSITIVITY_LEVEL_LOW;
                if (mPresenter != null) {
                    showLoading();
                    PirStateV2 pirState = new PirStateV2();
                    pirState.enable = isChecked;
                    mPresenter.setDevicePIRMode(mDeviceId, pirState, 1);
                }
                setupSensitivity(mDetectLevelCurrent);
            }
        });
    }

    private void displayDetectionSetting(boolean show) {
        if (isDestroyed() || checkNull(vDetectionSensitivityContainer, vDetectionZoneContainer, vDetectionScheduleContainer)) {
            return;
        }

        displayViewAll(false, vDetectionSensitivityContainer, vDetectionZoneContainer, vDetectionScheduleContainer);
        vDetectionSensitivityContainer.setVisibility(show ? View.VISIBLE: View.GONE);
        vDetectionZoneContainer.setVisibility(View.GONE);
        boolean isShowSchedule = show && getConnectionMode() != ConstantValue.CONNECTION_MODE_AP_DIRECT;
        vDetectionScheduleContainer.setVisibility(isShowSchedule ? View.VISIBLE : View.GONE);
        if (sbDetectionSwitch.isChecked() != show) {
            sbDetectionSwitch.toggleNoCallback();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
        registerShortLinkKeepListener();
    }

    private void resumeData() {
        if (mPresenter != null) {
            mPresenter.getDevicePIRMode(mDeviceId);
            if (getConnectionMode() == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            } else {
                mPresenter.getDeviceDetectionSchedule(mDeviceId, isFirstLaunch());
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unRegisterShortLinkKeepListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.destroy();
        }
        releaseRes();
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
                setIsGotoOtherPage(true);
                finish();
                break;
            case R.id.tvDetectionLow:
                if (tvDetectionLow.getTag() != null && (Integer) tvDetectionLow.getTag() == SENSITIVITY_NO_SELECTED) {
                    mDetectLevelCurrent = SensitivityLevel.SENSITIVITY_LEVEL_LOW;
                    if (mPresenter != null) {
                        showLoading();
                        PirStateV2 pirState = new PirStateV2();
                        pirState.sensitivityLevel = mDetectLevelCurrent;
                        mPresenter.setDevicePIRMode(mDeviceId, pirState, 2);
                    }
                    setupSensitivity(mDetectLevelCurrent);
                }
                break;
            case R.id.tvDetectionMedium:
                if (tvDetectionMedium.getTag() != null && (Integer) tvDetectionMedium.getTag() == SENSITIVITY_NO_SELECTED) {
                    mDetectLevelCurrent = SensitivityLevel.SENSITIVITY_LEVEL_MIDDLE;
                    if (mPresenter != null) {
                        showLoading();
                        PirStateV2 pirState = new PirStateV2();
                        pirState.sensitivityLevel = mDetectLevelCurrent;
                        mPresenter.setDevicePIRMode(mDeviceId, pirState, 2);
                    }
                    setupSensitivity(mDetectLevelCurrent);
                }
                break;
            case R.id.tvDetectionHigh:
                if (tvDetectionHigh.getTag() != null && (Integer) tvDetectionHigh.getTag() == SENSITIVITY_NO_SELECTED) {
                    mDetectLevelCurrent = SensitivityLevel.SENSITIVITY_LEVEL_HIGH;
                    if (mPresenter != null) {
                        showLoading();
                        PirStateV2 pirState = new PirStateV2();
                        pirState.sensitivityLevel = mDetectLevelCurrent;
                        mPresenter.setDevicePIRMode(mDeviceId, pirState, 2);
                    }
                    setupSensitivity(mDetectLevelCurrent);
                }
                break;
            case R.id.vDetectionZoneContainer:
                NooieDetectionZoneActivity.toNooieDetectionZoneActivity(this, ConstantValue.REQUEST_CODE_SET_DETECTION_ZONE, mDeviceId);
                break;
            case R.id.vDetectionScheduleContainer:
                setIsGotoOtherPage(true);
                NooieDetectionScheduleActivity.toNooieDetectionScheduleActivity(this, mDeviceId, ConstantValue.DETECT_TYPE_PIR);
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
    public void setPresenter(@NonNull DevicePIRContract.Presenter presenter) {
        mPresenter = presenter;
    }

    private void setupSensitivity(SensitivityLevel level) {
        tvDetectionLow.setTag(SENSITIVITY_NO_SELECTED);
        tvDetectionLow.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.gray_a1a1a1));
        tvDetectionMedium.setTag(SENSITIVITY_NO_SELECTED);
        tvDetectionMedium.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.gray_a1a1a1));
        tvDetectionHigh.setTag(SENSITIVITY_NO_SELECTED);
        tvDetectionHigh.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.gray_a1a1a1));

        if (level == SensitivityLevel.SENSITIVITY_LEVEL_LOW) {
            tvDetectionLow.setTag(SENSITIVITY_SELECTED);
            tvDetectionLow.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.theme_green));
        } else if (level == SensitivityLevel.SENSITIVITY_LEVEL_MIDDLE) {
            tvDetectionMedium.setTag(SENSITIVITY_SELECTED);
            tvDetectionMedium.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.theme_green));
        } else if (level == SensitivityLevel.SENSITIVITY_LEVEL_HIGH) {
            tvDetectionHigh.setTag(SENSITIVITY_SELECTED);
            tvDetectionHigh.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.theme_green));
        }
    }

    @Override
    public void onSetPIRModeResult(String result, PirStateV2 pirState, int operationType) {
        if (isDestroyed()) {
            return;
        }

        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            if (pirState != null) {
                if (operationType == 1) {
                    displayDetectionSetting(pirState.enable);
                }
                this.mDetectLevelCurrent = pirState.sensitivityLevel;
                setupSensitivity(pirState.sensitivityLevel);
            }
        }
    }

    @Override
    public void onGetPIRModeResult(String result, PirStateV2 pirState, int operationType) {
        if (isDestroyed()) {
            return;
        }

        hideLoading();
        if (pirState != null) {
            displayDetectionSetting(pirState.enable);
            this.mDetectLevelCurrent = pirState.sensitivityLevel;
            setupSensitivity(pirState.sensitivityLevel);
            syncApDevicePirPlan(pirState.enable);
        }
    }

    @Override
    public void onGetDeviceDetectionScheduleResult(String result, List<DetectionSchedule> schedules) {
        if (isDestroyed() || checkNull(tvDetectionScheduleTime) || CollectionUtil.isEmpty(schedules)) {
            return;
        }

        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
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
    }

    @Override
    public ShortLinkDeviceParam getShortLinkDeviceParam() {
        BindDevice device = NooieDeviceHelper.getDeviceById(mDeviceId);
        if (device == null) {
            return null;
        }
        String model = device.getType();
        boolean isSubDevice = NooieDeviceHelper.isSubDevice(device.getPuuid(), device.getType());
        ShortLinkDeviceParam shortLinkDeviceParam = new ShortLinkDeviceParam(mUid, mDeviceId, model, isSubDevice, false, ConstantValue.CONNECTION_MODE_QC);
        return shortLinkDeviceParam;
    }

    @Override
    public CurrentDeviceParam getCurrentDeviceParam() {
        if (TextUtils.isEmpty(mDeviceId)) {
            return null;
        }
        BindDevice device = NooieDeviceHelper.getDeviceById(mDeviceId);
        String model = device != null ? device.getType() : null;
        CurrentDeviceParam currentDeviceParam = null;
        if (getConnectionMode() == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            currentDeviceParam = new CurrentDeviceParam();
            currentDeviceParam.setDeviceId(mDeviceId);
            currentDeviceParam.setConnectionMode(getConnectionMode());
            currentDeviceParam.setModel(model);
        } else {
        }
        return currentDeviceParam;
    }

    private void syncApDevicePirPlan(boolean pirEnable) {
        if (getConnectionMode() != ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            return;
        }
        if (mPresenter != null) {
            mPresenter.setApDevicePirPlan(getDeviceId(), pirEnable);
        }
    }

    private String getDeviceId() {
        if (getStartParam() == null) {
            return null;
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_DEVICE_ID);
    }

    private int getConnectionMode() {
        if (getStartParam() == null) {
            return ConstantValue.CONNECTION_MODE_QC;
        }
        return getStartParam().getInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC);
    }
}
