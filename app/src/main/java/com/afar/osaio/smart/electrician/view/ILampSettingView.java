package com.afar.osaio.smart.electrician.view;

import com.afar.osaio.smart.electrician.bean.PowerStripName;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.entity.BindTyDeviceResult;
import com.tuya.smart.android.device.bean.UpgradeInfoBean;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.List;

public interface ILampSettingView {

    void notifyLoadDeviceInfo(DeviceBean device);

    void notifyRemoveDeviceState(String msg);

    void notifyLoadOtaInfoSuccess(UpgradeInfoBean upgradeInfo);

    void notifyLoadOtaInfoFailed(String msg);

    void notifyResetFactory(String msg);

    void notifyOnDevInfoUpdateState(DeviceBean deviceBean);

    void notifyloadSwitchNameSuccess(List<PowerStripName> powerStripNames);

    void notifyloadSwitchNameFail(String errorCode, String errorMsg);

    void notifyShowThirdParty(BindTyDeviceResult.BindTyDevice bindTyDevice);
}
