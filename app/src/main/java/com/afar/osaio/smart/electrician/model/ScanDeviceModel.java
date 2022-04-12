package com.afar.osaio.smart.electrician.model;

import com.afar.osaio.base.NooieApplication;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.builder.ActivatorBuilder;
import com.tuya.smart.sdk.api.ITuyaActivator;
import com.tuya.smart.sdk.api.ITuyaSmartActivatorListener;
import com.tuya.smart.sdk.enums.ActivatorModelEnum;

import static com.tuya.smart.sdk.enums.ActivatorModelEnum.TY_AP;
import static com.tuya.smart.sdk.enums.ActivatorModelEnum.TY_EZ;

/**
 * ScanDeviceModel
 *
 * @author Administrator
 * @date 2019/3/5
 */
public class ScanDeviceModel implements IScanDeviceModel {

    public static final String STATUS_FAILURE_WITH_GET_TOKEN = "1004";
    private static final long CONFIG_TIME_OUT = 100;

    private ITuyaActivator mTuyaActivator;
    private ActivatorModelEnum mModelEnum;

    @Override
    public void start() {
        if (mTuyaActivator != null) {
            mTuyaActivator.start();
        }
    }

    @Override
    public void cancel() {
        if (mTuyaActivator != null) {
            mTuyaActivator.stop();
        }
    }

    @Override
    public void setEC(String ssid, String password, String token, ITuyaSmartActivatorListener listener) {
        mModelEnum = TY_EZ;
        mTuyaActivator = TuyaHomeSdk.getActivatorInstance().newMultiActivator(new ActivatorBuilder()
                .setSsid(ssid)
                .setContext(NooieApplication.mCtx)
                .setPassword(password)
                .setActivatorModel(TY_EZ)
                .setTimeOut(CONFIG_TIME_OUT)
                .setToken(token).setListener(listener));
    }

    @Override
    public void setAP(String ssid, String password, String token, ITuyaSmartActivatorListener listener) {
        mModelEnum = TY_AP;
        mTuyaActivator = TuyaHomeSdk.getActivatorInstance().newActivator(new ActivatorBuilder()
                .setSsid(ssid)
                .setContext(NooieApplication.mCtx)
                .setPassword(password)
                .setActivatorModel(TY_AP)
                .setTimeOut(CONFIG_TIME_OUT)
                .setToken(token).setListener(listener));

    }

    @Override
    public void destroy() {
        if (mTuyaActivator != null) {
            mTuyaActivator.onDestroy();
        }
    }
}
