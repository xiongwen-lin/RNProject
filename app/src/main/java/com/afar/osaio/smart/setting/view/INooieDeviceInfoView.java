package com.afar.osaio.smart.setting.view;

import com.afar.osaio.base.mvp.IBaseView;
import com.afar.osaio.smart.device.bean.NooieDevice;
import com.nooie.sdk.api.network.base.bean.entity.AppVersionResult;
import com.nooie.sdk.device.bean.DevInfo;
import com.nooie.sdk.device.bean.hub.CameraInfo;

/**
 * INooieDeviceInfoView
 *
 * @author Administrator
 * @date 2019/4/19
 */
public interface INooieDeviceInfoView extends IBaseView {

    void onLoadDeviceSuccess(NooieDevice device);

    void onLoadDeviceFailed(String msg);

    void onGetDeviceInfo(String result, DevInfo devInfo);

    void notifyDeviceRenameState(String result);

    void onLoadFirmwareInfoSuccess(AppVersionResult result);

    void onLoadFirmwareInfoFailed(String msg);

    void onQueryNooieDeviceUpdateStatusSuccess(int type, int process);

    void onQueryNooieDeviceUpdateStatusFailed(String msg);

    void onStartUpdateDeviceResult(String result);

    void onGetCamInfoResult(String result, CameraInfo cameraInfo);

    void onCheckDeviceUpgradeScheduleResult(String result, boolean isUpgradeFinish);

}
