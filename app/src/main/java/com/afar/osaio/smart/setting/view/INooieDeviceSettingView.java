package com.afar.osaio.smart.setting.view;

import com.nooie.sdk.api.network.base.bean.entity.PackInfoResult;
import com.nooie.sdk.bean.DeviceComplexSetting;
import com.nooie.sdk.device.bean.DevAllSettingsV2;
import com.afar.osaio.base.mvp.IBaseView;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.device.bean.DevInfo;
import com.nooie.sdk.device.bean.PirStateV2;
import com.nooie.sdk.device.bean.NooieMediaMode;
import com.nooie.sdk.device.bean.hub.CameraInfo;

/**
 * Created by victor on 2018/7/4
 * Email is victor.qiao.0604@gmail.com
 */
public interface INooieDeviceSettingView extends IBaseView {

    void onGetDeviceSetting(int code, DeviceComplexSetting complexSetting);

    void onGetAllSettingResult(String msg, DevAllSettingsV2 settings);

    void onGetDeviceInfo(String result, DevInfo devInfo);

    void onNotifyGetDeviceSuccess(BindDevice device);

    void onNotifyGetDeviceFailed(String msg);

    void showLoadingDialog();

    void hideLoadingDialog();

    /**
     * LED
     *
     * @param message
     */
    void notifyGetLedFailed(String message);

    void notifyGetLedSuccess(boolean open);

    void notifySetLedResult(String result);

    /**
     * Remove camera
     *
     * @param result
     */
    void notifyRemoveCameraResult(String result);

    /**
     * restart device
     *
     * @param deviceId
     */
    void notifyRestartDeviceSuccess(String deviceId);

    void notifyRestartDeviceFailed(String message);

    /**
     * factory reset
     * @param result
     */
    void notifyFactoryResetResult(String result);

    void notifySetSyncTimeResult(String result);

    void notifyLoadPackInfoResult(String msg, PackInfoResult result);

    void onGetCamInfoResult(String result, CameraInfo info);

    void onSetDeviceFDModeResult(String result);

    void onSetDevicePDModeResult(String result);

    void onUpdateDeviceNoticeResult(String result);

    void onGetPirState(int state, PirStateV2 pirState);

    void onSetSiren(int state);

    void onGetFileSettingMode(int state, NooieMediaMode mode);

    void onRemoveApDevice(int state);
}
