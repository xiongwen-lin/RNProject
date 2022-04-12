package com.afar.osaio.smart.electrician.model;

import com.google.gson.Gson;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.android.device.bean.UpgradeInfoBean;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IDevListener;
import com.tuya.smart.sdk.api.IGetOtaInfoCallback;
import com.tuya.smart.sdk.api.IOtaListener;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaDevice;
import com.tuya.smart.sdk.api.ITuyaOta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeviceModel
 *
 * @author Administrator
 * @date 2019/3/8
 */
public class DeviceModel implements IDeviceModel {

    private ITuyaDevice mDevice;
    private ITuyaOta mOta;

    public DeviceModel(String deviceId) {
        mDevice = TuyaHomeSdk.newDeviceInstance(deviceId);
        mOta = TuyaHomeSdk.newOTAInstance(deviceId);
    }

    @Override
    public void sendCommand(String key, Object dps, IResultCallback callback) {
        NooieLog.e("---->> sendCommand params "+ dps.toString());
        Map<String, Object> dpsMap = new HashMap<>();
        dpsMap.put(key, dps);
        sendCommands(dpsMap, callback);
    }

    @Override
    public void sendCommands(Map<String, Object> dpsMap, IResultCallback callback) {
        NooieLog.e("---->> sendCommands params "+new Gson().toJson(dpsMap));
        mDevice.publishDps(new Gson().toJson(dpsMap), callback);
    }

    @Override
    public void queryDeviceDp(String dpId, IResultCallback callback) {
        mDevice.getDp(dpId, callback);
    }

    @Override
    public void queryDeviceDps(List<String> dpIds, IResultCallback callback) {
        mDevice.getDpList(dpIds, callback);
    }

    @Override
    public void removeDevice(IResultCallback callback) {
        mDevice.removeDevice(callback);
    }

    @Override
    public void getOtaInfo(final IGetOtaInfoCallback callback) {
        mOta.getOtaInfo(new IGetOtaInfoCallback() {
            @Override
            public void onSuccess(List<UpgradeInfoBean> infoList) {
                if (callback != null) {
                    callback.onSuccess(infoList);
                }
            }

            @Override
            public void onFailure(String code, String msg) {
                if (callback != null) {
                    callback.onFailure(code, msg);
                }
            }
        });
    }

    @Override
    public void registerOtaListener(IOtaListener listener) {
        mOta.setOtaListener(listener);
    }

    @Override
    public void startOta() {
        mOta.startOta();
    }

    @Override
    public void registerListener(IDevListener listener) {
        mDevice.registerDevListener(listener);
    }

    @Override
    public void unRegisterListener() {
        mDevice.unRegisterDevListener();
    }

    @Override
    public void release() {
        mDevice.onDestroy();
        mOta.onDestroy();
    }

    @Override
    public void resetFactory(IResultCallback callback) {
        mDevice.resetFactory(callback);
    }
}
