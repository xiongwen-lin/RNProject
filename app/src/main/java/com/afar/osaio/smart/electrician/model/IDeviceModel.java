package com.afar.osaio.smart.electrician.model;

import com.afar.osaio.base.mvp.IBaseModel;
import com.tuya.smart.sdk.api.IDevListener;
import com.tuya.smart.sdk.api.IGetOtaInfoCallback;
import com.tuya.smart.sdk.api.IOtaListener;
import com.tuya.smart.sdk.api.IResultCallback;

import java.util.List;
import java.util.Map;

/**
 * IDeviceModel
 *
 * @author Administrator
 * @date 2019/3/8
 */
public interface IDeviceModel extends IBaseModel {

    void sendCommand(String key, Object dps, IResultCallback callback);

    void sendCommands(Map<String, Object> dpsMap, IResultCallback callback);

    void queryDeviceDp(String dpId, IResultCallback callback);

    void queryDeviceDps(List<String> dpIds, IResultCallback callback);

    void removeDevice(IResultCallback callback);

    void getOtaInfo(IGetOtaInfoCallback callback);

    void registerOtaListener(IOtaListener listener);

    void startOta();

    void registerListener(IDevListener listener);

    void unRegisterListener();

    void release();

    void resetFactory(IResultCallback callback);
}
