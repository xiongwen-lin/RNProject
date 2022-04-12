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

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.CurrentDeviceParam;
import com.afar.osaio.bean.ShortLinkDeviceParam;
import com.afar.osaio.widget.LabelSwTagItemView;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.bean.DeviceComplexSetting;
import com.nooie.sdk.bean.IpcType;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.widget.LabelSwItemView;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.device.bean.DevAllSettingsV2;
import com.nooie.sdk.device.bean.ICRMode;
import com.nooie.sdk.device.bean.hub.CameraInfo;
import com.suke.widget.SwitchButton;
import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.bean.DeviceSettingBean;
import com.afar.osaio.smart.setting.view.DeviceAudioAndVideoContract;
import com.afar.osaio.smart.setting.presenter.DeviceAudioAndVideoPresenter;
import com.afar.osaio.util.ConstantValue;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DeviceAudioAndVideoActivity extends BaseActivity implements DeviceAudioAndVideoContract.View{

    public static void toDeviceAudioAndVideoActivity(Context from, String deviceId, DeviceSettingBean param, String model, int connectionMode, boolean isSubDevice, boolean isLpDevice) {
        Intent intent = new Intent(from, DeviceAudioAndVideoActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM, param);
        intent.putExtra(ConstantValue.INTENT_KEY_IPC_MODEL, model);
        intent.putExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, connectionMode);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM_1, isSubDevice);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM_2, isLpDevice);
        from.startActivity(intent);
    }

    private DeviceAudioAndVideoContract.Presenter mPresenter;

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.switchAudioRecord)
    SwitchButton switchAudioRecord;
    @BindView(R.id.switchRotateImg)
    SwitchButton switchRotateImg;
    @BindView(R.id.switchDesktopWidget)
    SwitchButton switchDesktopWidget;
    @BindView(R.id.tvNightVisionOff)
    TextView tvNightVisionOff;
    @BindView(R.id.tvNightVisionAuto)
    TextView tvNightVisionAuto;
    @BindView(R.id.containerAudioRecord)
    View containerAudioRecord;
    @BindView(R.id.livMotionTracking)
    LabelSwItemView livMotionTracking;
    @BindView(R.id.livNightVision)
    LabelSwItemView livNightVision;
    @BindView(R.id.livEnergyMode)
    LabelSwTagItemView livEnergyMode;
    @BindView(R.id.livWaterMark)
    LabelSwItemView livWaterMark;
    @BindView(R.id.tvNightVisionInfrared)
    TextView tvNightVisionInfrared;
    @BindView(R.id.tvNightVisionLight)
    TextView tvNightVisionLight;

    private String mDeviceId;
    private DeviceSettingBean mDeviceSettingBean;
    private DevAllSettingsV2 mDevAllSettings;
    private CameraInfo mCameraInfo;
    private boolean mIsSubDevice;
    private boolean mIsLpDevice;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_audio_and_video);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    private void initData() {
        if (getCurrentIntent() != null) {
            mDeviceId = getDeviceId();
            mDeviceSettingBean = (DeviceSettingBean)getCurrentIntent().getSerializableExtra(ConstantValue.INTENT_KEY_DATA_PARAM);
            mIsSubDevice = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_DATA_PARAM_1, false);
            mIsLpDevice = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_DATA_PARAM_2, false);
        }

        new DeviceAudioAndVideoPresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.camera_settings_video_and_audio);
        displayNightVisionControl(NooieDeviceHelper.isSupportNightVisionLight(getDeviceModel()));
        setupDeviceSetting(mDeviceSettingBean);
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
        registerShortLinkKeepListener();
    }

    private void resumeData() {
        if (mIsLpDevice) {
            if (mCameraInfo != null) {
                if (mDeviceSettingBean == null) {
                    mDeviceSettingBean = new DeviceSettingBean();
                }
                mDeviceSettingBean.setIcr(NooieDeviceHelper.convertICRMode(mCameraInfo.ir).getIntValue());
                refreshDeviceSetting(mDeviceSettingBean);
            } else if (mPresenter != null) {
                showLoading();
                mPresenter.getDeviceSetting(mDeviceId, getDeviceModel());
            }

            if (mPresenter != null) {
                mPresenter.getRotateImage(mDeviceId, mIsSubDevice);
                if (NooieDeviceHelper.isSupportEnergyMode(getDeviceModel())) {
                    mPresenter.getEnergyMode(mDeviceId);
                }
                if (NooieDeviceHelper.isSupportWatermark(getDeviceModel())) {
                    mPresenter.getWaterMark(mDeviceId);
                }
            }
            return;
        }

        if (mDevAllSettings != null && mDevAllSettings.commSettings != null) {
            if (mDeviceSettingBean == null) {
                mDeviceSettingBean = new DeviceSettingBean();
            }

            mDeviceSettingBean.setAudioRecOpen(mDevAllSettings.commSettings.audioRec == ConstantValue.CMD_STATE_ENABLE);
            mDeviceSettingBean.setRotateOn(mDevAllSettings.commSettings.flip == ConstantValue.CMD_STATE_ENABLE);
            mDeviceSettingBean.setIcr(mDevAllSettings.commSettings.icr);
            mDeviceSettingBean.setMotionTracking(mDevAllSettings.commSettings.motTrack == ConstantValue.CMD_STATE_ENABLE);

            refreshDeviceSetting(mDeviceSettingBean);
        } else if (mPresenter != null) {
            showLoading();
            mPresenter.getDeviceSetting(mDeviceId, getDeviceModel());
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
        release();
    }

    private void release() {
        ivLeft = null;
        tvTitle = null;
        if (switchAudioRecord != null) {
            switchAudioRecord.setOnCheckedChangeListener(null);
            switchAudioRecord = null;
        }

        if (switchRotateImg != null) {
            switchRotateImg.setOnCheckedChangeListener(null);
            switchRotateImg = null;
        }

        if (switchDesktopWidget != null) {
            switchDesktopWidget.setOnCheckedChangeListener(null);
            switchDesktopWidget = null;
        }

        if (livMotionTracking != null) {
            livMotionTracking.release();
            livMotionTracking = null;
        }
        if (livNightVision != null) {
            livNightVision.release();
            livNightVision = null;
        }
        if (livEnergyMode != null) {
            livEnergyMode.release();
            livEnergyMode = null;
        }
        if (livWaterMark != null) {
            livWaterMark.release();
            livWaterMark = null;
        }
        tvNightVisionOff = null;
        tvNightVisionAuto = null;
        containerAudioRecord = null;
        mDeviceSettingBean = null;
        mDevAllSettings = null;
        mCameraInfo = null;
    }

    @OnClick({R.id.ivLeft, R.id.tvNightVisionOff, R.id.tvNightVisionAuto, R.id.tvNightVisionInfrared, R.id.tvNightVisionLight})
    public void onViewClick(View view) {
        switch(view.getId()) {
            case R.id.ivLeft:
                setIsGotoOtherPage(true);
                finish();
                break;
            case R.id.tvNightVisionOff: {
                setDeviceNightVision(ConstantValue.DEVICE_LIGHT_MODE_OFF);
                break;
            }
            case R.id.tvNightVisionAuto: {
                setDeviceNightVision(ConstantValue.DEVICE_LIGHT_MODE_AUTO);
                break;
            }
            case R.id.tvNightVisionInfrared: {
                setDeviceNightVision(ConstantValue.DEVICE_LIGHT_MODE_IR);
                break;
            }
            case R.id.tvNightVisionLight: {
                setDeviceNightVision(ConstantValue.DEVICE_LIGHT_MODE_COLOR);
                break;
            }
        }
    }

    private void setupDeviceSetting(DeviceSettingBean deviceSetting) {
        containerAudioRecord.setVisibility(mIsLpDevice ? View.GONE : View.VISIBLE);
        switchAudioRecord.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (mPresenter != null) {
                    mPresenter.setRecordWithAudioStatus(mDeviceId, isChecked);
                }
            }
        });

        switchRotateImg.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (mPresenter != null) {
                    showLoading();
                    mPresenter.setRotateImage(mDeviceId, isChecked, mIsSubDevice);
                }
            }
        });

        boolean showMotionTracking = getCurrentIntent() != null && NooieDeviceHelper.isSupportPtzControl(IpcType.getIpcType(getDeviceModel()))
                && getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC) != ConstantValue.CONNECTION_MODE_AP_DIRECT && !mIsSubDevice && !mIsLpDevice;
        livMotionTracking.setVisibility(showMotionTracking ? View.VISIBLE : View.GONE);
        livMotionTracking.displayLabelRightSw(View.VISIBLE);
        livMotionTracking.setLabelRightSwListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (mPresenter != null) {
                    showLoading();
                    mPresenter.setMotionTrackingStatus(mDeviceId, isChecked);
                }
            }
        });

        /*
        livNightVision.setVisibility(View.VISIBLE);
        livNightVision.displayLabelRightSw(View.VISIBLE);
        livNightVision.setLabelRightSwListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked) {
                    setDeviceNightVision(ConstantValue.NOOIE_NIGHT_VISION_MODE_AUTO);
                } else {
                    setDeviceNightVision(ConstantValue.NOOIE_NIGHT_VISION_MODE_DAY);
                }
            }
        });

         */

        livEnergyMode.setLabelTag(getString(R.string.audio_and_energy_mode_tip));
        livEnergyMode.setTagColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.black_7a010C11));
        livEnergyMode.displayLabelTag(View.VISIBLE);
        livEnergyMode.displayLabelRightSw(View.VISIBLE);
        livEnergyMode.setLabelRightSwListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (mPresenter != null) {
                    showLoading();
                    mPresenter.setEnergyMode(mDeviceId, isChecked);
                }
            }
        });
        livWaterMark.setVisibility(NooieDeviceHelper.isSupportWatermark(getDeviceModel()) ? View.VISIBLE : View.GONE);
        livWaterMark.displayLabelRightSw(View.VISIBLE);
        livWaterMark.setLabelRightSwListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (mPresenter != null) {
                    showLoading();
                    mPresenter.setWaterMark(mDeviceId, isChecked);
                }
            }
        });

        refreshDeviceSetting(deviceSetting);
    }

    private void refreshDeviceSetting(DeviceSettingBean deviceSetting) {
        if (checkNull(deviceSetting, switchAudioRecord, switchRotateImg)) {
            return;
        }
        boolean isAudioRecordOpen = deviceSetting != null ? deviceSetting.isAudioRecOpen() : false;
        boolean isRotateOn = deviceSetting != null ? deviceSetting.isRotateOn() : false;
//        int icr = deviceSetting != null ? deviceSetting.getIcr() : ICRMode.ICR_MODE_NIGHT.getIntValue();
        int icr = deviceSetting != null ? deviceSetting.getIcr() : ConstantValue.DEVICE_LIGHT_MODE_OFF;
        boolean isMotionTracking = deviceSetting != null ? deviceSetting.isMotionTracking() : false;

        if (switchAudioRecord.isChecked() != isAudioRecordOpen) {
            switchAudioRecord.toggleNoCallback();
        }

        if (switchRotateImg.isChecked() != isRotateOn) {
            switchRotateImg.toggleNoCallback();
        }

//        if (icr == ICRMode.ICR_MODE_DAY.getIntValue()) {
//            selectIcrMode(false);
//        } else if (icr == ICRMode.ICR_MODE_AUTO.getIntValue()) {
//            selectIcrMode(true);
//        }
        selectIcrMode(icr);

        if (livMotionTracking.isLabelRightSwCheck() != isMotionTracking) {
            livMotionTracking.toggleLabelRightSw();
        }
    }

    private void setDeviceNightVision(int mode) {
        if (mPresenter != null) {
            showLoading();
            mPresenter.setNightVision(mDeviceId, getDeviceModel(), mode, mIsSubDevice);
        }
        selectIcrMode(mode);
//        switch (mode) {
//            case ConstantValue.NOOIE_NIGHT_VISION_MODE_AUTO: {
//                if (mPresenter != null) {
//                    showLoading();
//                    mPresenter.setNightVision(mDeviceId, getDeviceModel(), ICRMode.ICR_MODE_AUTO, mIsSubDevice);
//                }
//                selectIcrMode(true);
//                break;
//            }
//            case ConstantValue.NOOIE_NIGHT_VISION_MODE_DAY: {
//                if (mPresenter != null) {
//                    showLoading();
//                    mPresenter.setNightVision(mDeviceId, getDeviceModel(), ICRMode.ICR_MODE_DAY, mIsSubDevice);
//                }
//                selectIcrMode(false);
//                break;
//            }
//        }
    }

    private void selectIcrMode(int mode) {

        if (checkNull(tvNightVisionAuto, tvNightVisionOff)) {
            return;
        }

        tvNightVisionOff.setTextColor(getResources().getColor(R.color.gray_a1a1a1));
        tvNightVisionInfrared.setTextColor(getResources().getColor(R.color.gray_a1a1a1));
        tvNightVisionLight.setTextColor(getResources().getColor(R.color.gray_a1a1a1));
        tvNightVisionAuto.setTextColor(getResources().getColor(R.color.gray_a1a1a1));
//        int mode = auto ? 0 : 1;
        switch (mode) {
            case ConstantValue.DEVICE_LIGHT_MODE_AUTO: {
                tvNightVisionAuto.setTextColor(getResources().getColor(R.color.theme_green));
                break;
            }
            case ConstantValue.DEVICE_LIGHT_MODE_OFF: {
                tvNightVisionOff.setTextColor(getResources().getColor(R.color.theme_green));
                break;
            }
            case ConstantValue.DEVICE_LIGHT_MODE_COLOR: {
                tvNightVisionLight.setTextColor(getResources().getColor(R.color.theme_green));
                break;
            }
            case ConstantValue.DEVICE_LIGHT_MODE_IR: {
                tvNightVisionInfrared.setTextColor(getResources().getColor(R.color.theme_green));
                break;
            }
        }
//        if (auto) {
//            tvNightVisionAuto.setTextColor(getResources().getColor(R.color.theme_blue));
//            tvNightVisionOff.setTextColor(getResources().getColor(R.color.gray_a1a1a1));
//        } else {
//            tvNightVisionAuto.setTextColor(getResources().getColor(R.color.gray_a1a1a1));
//            tvNightVisionOff.setTextColor(getResources().getColor(R.color.theme_blue));
//        }
        boolean auto = mode == ConstantValue.DEVICE_LIGHT_MODE_AUTO;
        boolean isShowEnergyMode = NooieDeviceHelper.isSupportEnergyMode(getDeviceModel()) && auto;
        livEnergyMode.setVisibility(isShowEnergyMode ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setPresenter(@NonNull DeviceAudioAndVideoContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onGetDeviceSetting(int code, DeviceComplexSetting complexSetting) {
        if (isDestroyed()) {
            return;
        }
        if (code != Constant.OK || complexSetting == null) {
            return;
        }
        hideLoading();
        if (complexSetting.getDevAllSettings() != null) {
            mDevAllSettings = complexSetting.getDevAllSettings();
            if (mDeviceSettingBean == null) {
                mDeviceSettingBean = new DeviceSettingBean();
            }
            mDeviceSettingBean.setAudioRecOpen(mDevAllSettings.commSettings.audioRec == ConstantValue.CMD_STATE_ENABLE);
            mDeviceSettingBean.setRotateOn(mDevAllSettings.commSettings.flip == ConstantValue.CMD_STATE_ENABLE);
            mDeviceSettingBean.setIcr(mDevAllSettings.commSettings.icr);
            mDeviceSettingBean.setMotionTracking(mDevAllSettings.commSettings.motTrack == ConstantValue.CMD_STATE_ENABLE);
            refreshDeviceSetting(mDeviceSettingBean);
        } else if (complexSetting.getCameraInfo() != null) {
            mCameraInfo = complexSetting.getCameraInfo();
            if (mDeviceSettingBean == null) {
                mDeviceSettingBean = new DeviceSettingBean();
            }
            mDeviceSettingBean.setIcr(NooieDeviceHelper.convertICRMode(mCameraInfo.ir).getIntValue());
            refreshDeviceSetting(mDeviceSettingBean);
        }
    }

    @Override
    public void onGetAllSettingResult(String msg, DevAllSettingsV2 settings) {
        if (isDestroyed()) {
            return;
        }

        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(msg)) {
            if (settings != null && settings.commSettings != null) {
                mDevAllSettings = settings;
                if (mDeviceSettingBean == null) {
                    mDeviceSettingBean = new DeviceSettingBean();
                }

                mDeviceSettingBean.setAudioRecOpen(mDevAllSettings.commSettings.audioRec == ConstantValue.CMD_STATE_ENABLE);
                mDeviceSettingBean.setRotateOn(mDevAllSettings.commSettings.flip == ConstantValue.CMD_STATE_ENABLE);
                mDeviceSettingBean.setIcr(mDevAllSettings.commSettings.icr);
                mDeviceSettingBean.setMotionTracking(mDevAllSettings.commSettings.motTrack == ConstantValue.CMD_STATE_ENABLE);

                refreshDeviceSetting(mDeviceSettingBean);
            }
        }
    }

    @Override
    public void notifyGetRecordWidthAudioSuccess(boolean open) {
        if (isDestroyed() || checkNull(switchAudioRecord)) {
            return;
        }

        if (switchAudioRecord.isChecked() != open) {
            switchAudioRecord.toggleNoCallback();
        }
    }

    @Override
    public void notifyGetRecordWidthAudioFailed(String message) {
    }

    @Override
    public void notifySetRecordWidthAudioResult(String result) {
        if (isDestroyed()) {
            return;
        }

        if (result.equals(ConstantValue.SUCCESS)) {
        }
    }

    @Override
    public void notifyGetRotateImageSuccess(boolean on) {
        if (isDestroyed() || checkNull(switchRotateImg)) {
            return;
        }

        if (mDeviceSettingBean != null) {
            mDeviceSettingBean.setRotateOn(on);
        }

        if (switchRotateImg.isChecked() != on) {
            switchRotateImg.toggleNoCallback();
        }
    }

    @Override
    public void notifyGetRotateImageFailed(String message) {
    }

    @Override
    public void notifySetRotateImageResult(String result) {
        if (isDestroyed()) {
            return;
        }

        hideLoading();
        if (result.equals(ConstantValue.SUCCESS)) {
        }
    }

    @Override
    public void notifySetNightVisionResult(String result) {
        if (isDestroyed()) {
            return;
        }

        hideLoading();
        if (ConstantValue.SUCCESS.equals(result)) {
        }
    }

    @Override
    public void notifyGetNightVisionResult(String result, ICRMode mode) {
        if (isDestroyed()) {
            return;
        }
        if (ConstantValue.SUCCESS.equals(result)) {
            if (mode == ICRMode.ICR_MODE_DAY) {
                selectIcrMode(ConstantValue.DEVICE_LIGHT_MODE_OFF);
            }else if (mode == ICRMode.ICR_MODE_AUTO) {
                selectIcrMode(ConstantValue.DEVICE_LIGHT_MODE_AUTO);
            }
        }
    }

    @Override
    public void onGetCamInfoResult(String result, CameraInfo info) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result) && info != null) {
            mCameraInfo = info;
            if (mDeviceSettingBean == null) {
                mDeviceSettingBean = new DeviceSettingBean();
            }
            mDeviceSettingBean.setIcr(NooieDeviceHelper.convertICRMode(mCameraInfo.ir).getIntValue());
            refreshDeviceSetting(mDeviceSettingBean);
        }
    }

    @Override
    public void onSetMotionTrackingResult(String result) {
        if (isDestroyed()) {
            return;
        }

        hideLoading();
    }

    @Override
    public void onGetEnergyMode(int state, boolean open) {
        if (isDestroyed()) {
            return;
        }
        if (state == SDKConstant.SUCCESS) {
            refreshEnergyModeView(open);
        }
    }

    @Override
    public void onSetEnergyMode(int state) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
    }

    @Override
    public void onGetWaterMark(int state, boolean open) {
        if (isDestroyed()) {
            return;
        }
        if (state == SDKConstant.SUCCESS) {
            refreshWaterMarkView(open);
        }
    }

    @Override
    public void onSetWaterMark(int state) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
    }

    @Override
    public boolean checkIsAddDeviceApHelperListener() {
        return true;
    }

    @Override
    public ShortLinkDeviceParam getShortLinkDeviceParam() {
        if (getCurrentIntent() == null) {
            return null;
        }
        ShortLinkDeviceParam shortLinkDeviceParam = new ShortLinkDeviceParam(mUid, mDeviceId, getDeviceModel(), mIsSubDevice, false, getConnectionMode());
        return shortLinkDeviceParam;
    }

    @Override
    public CurrentDeviceParam getCurrentDeviceParam() {
        if (TextUtils.isEmpty(getDeviceId())) {
            return null;
        }
        CurrentDeviceParam currentDeviceParam = null;
        if (getConnectionMode() == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            currentDeviceParam = new CurrentDeviceParam();
            currentDeviceParam.setDeviceId(getDeviceId());
            currentDeviceParam.setConnectionMode(getConnectionMode());
            currentDeviceParam.setModel(getDeviceModel());
        } else {
        }
        return currentDeviceParam;
    }

    private void refreshNightVisionView(boolean open) {
        if (checkNull(livNightVision)) {
            return;
        }
        if (livNightVision.isLabelRightSwCheck() != open) {
            livNightVision.toggleLabelRightSw();
        }
    }


    private void refreshEnergyModeView(boolean open) {
        if (checkNull(livEnergyMode)) {
            return;
        }
        if (livEnergyMode.isLabelRightSwCheck() != open) {
            livEnergyMode.toggleLabelRightSw();
        }
    }

    private void refreshWaterMarkView(boolean open) {
        if (checkNull(livWaterMark)) {
            return;
        }
        if (livWaterMark.isLabelRightSwCheck() != open) {
            livWaterMark.toggleLabelRightSw();
        }
    }

    private void displayNightVisionControl(boolean isSupportLight) {
        tvNightVisionAuto.setVisibility(isSupportLight ? View.VISIBLE : View.GONE);
        tvNightVisionLight.setVisibility(isSupportLight ? View.VISIBLE : View.GONE);
    }

    private String getDeviceId() {
        if (getCurrentIntent() == null) {
            return null;
        }
        return getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
    }

    private int getConnectionMode() {
        int connectionMode = getCurrentIntent() != null ? getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC) : ConstantValue.CONNECTION_MODE_QC;
        return connectionMode;
    }

    private String getDeviceModel() {
        if (getCurrentIntent() == null) {
            return null;
        }
        return getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_IPC_MODEL);
    }
}
