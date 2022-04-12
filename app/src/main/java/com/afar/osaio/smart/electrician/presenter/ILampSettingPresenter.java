package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;
import com.tuya.smart.sdk.api.IOtaListener;

/**
 * IPowerStripSettingPresenter
 *
 * @author Administrator
 * @date 2019/6/26
 */
public interface ILampSettingPresenter extends IBasePresenter {

    void loadDeviceInfo(String deviceId);

    void removeDevice();

    void loadOtaInfo();

    void registerOtaListener(IOtaListener listener);

    void startOta();

    void release();

    void removeShareDevice(String devId);

    void resetFactory();

    void onDevInfoUpdate(String deviceId);

    void isSupportThirdParty(String productId);

}
