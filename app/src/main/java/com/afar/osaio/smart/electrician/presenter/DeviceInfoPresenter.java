package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.smart.electrician.bean.DeviceHelper;
import com.afar.osaio.smart.electrician.bean.DeviceInfoBean;
import com.afar.osaio.smart.electrician.model.DeviceModel;
import com.afar.osaio.smart.electrician.model.IDeviceModel;
import com.afar.osaio.smart.electrician.view.IDeviceInfoView;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.SharedUserInfoBean;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;
import com.tuya.smart.sdk.api.IRequestCallback;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.HashMap;
import java.util.Map;

/**
 * DeviceInfoPresenter
 *
 * @author Administrator
 * @date 2019/3/18
 */
public class DeviceInfoPresenter implements IDeviceInfoPresenter {

    private IDeviceInfoView mDeviceInfoView;
    private IDeviceModel mDeviceModel;

    public DeviceInfoPresenter(IDeviceInfoView view, String deviceId) {
        mDeviceInfoView = view;
        mDeviceModel = new DeviceModel(deviceId);
    }

    @Override
    public void loadDeviceInfo(String deviceId) {

        DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(deviceId);
        if (mDeviceInfoView != null) {
            mDeviceInfoView.notifyLoadDeviceInfo(deviceBean);

        }
    }

    @Override
    public void release() {
        mDeviceModel.release();
    }


    public void queryShareDev(String devId){
        TuyaHomeSdk.getDeviceShareInstance().queryShareDevFromInfo(devId, new ITuyaResultCallback<SharedUserInfoBean>() {
            @Override
            public void onSuccess(SharedUserInfoBean result) {
                if (mDeviceInfoView != null) {
                    //mDeviceInfoView.notifyLoadDeviceInfo(deviceBean);
                }
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                if (mDeviceInfoView != null) {
                    //mDeviceInfoView.notifyLoadDeviceInfo(deviceBean);
                }
            }
        });

    }

    @Override
    public void getDeviceIp(String devId) {
        Map<String, Object> postData = new HashMap<String, Object>();
        postData.put("devId",devId);
        TuyaHomeSdk.getRequestInstance().requestWithApiName("tuya.m.device.get", "1.0", postData, new IRequestCallback() {
            @Override
            public void onSuccess(Object o) {
                DeviceInfoBean infoBean = DeviceHelper.convertDeviceInfoBean(o.toString());
                if (mDeviceInfoView != null){
                    mDeviceInfoView.getDeviceIpSuccess(infoBean);
                }
            }

            @Override
            public void onFailure(String s, String s1) {
                if (mDeviceInfoView != null){
                    mDeviceInfoView.getDeviceIpFail(s1);
                }
            }
        });
    }
}
