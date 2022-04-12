package com.afar.osaio.smart.mixipc.presenter;


import android.text.TextUtils;

import com.afar.osaio.smart.mixipc.contract.ChangeDevicePasswordContract;
import com.afar.osaio.util.ConstantValue;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.device.bean.NooieHotspot;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.listener.OnGetHotspotListener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;

public class ChangeDevicePasswordPresenter implements ChangeDevicePasswordContract.Presenter {

    private ChangeDevicePasswordContract.View mTaskView;

    public ChangeDevicePasswordPresenter(ChangeDevicePasswordContract.View view) {
        mTaskView = view;
        mTaskView.setPresenter(this);
    }

    @Override
    public void destroy() {
        if (mTaskView != null) {
            mTaskView.setPresenter(null);
            mTaskView = null;
        }
    }

    @Override
    public void checkDeviceHotSpotPw(String deviceId, String ssid, String pw, String oldPw) {
        DeviceCmdApi.getInstance().getHotspot(deviceId, new OnGetHotspotListener() {
            @Override
            public void onResult(int code, NooieHotspot nooieHotspot) {
                /*
                if (nooieHotspot != null) {
                    NooieLog.d("-->> changedevicepw ssid=" + nooieHotspot.ssid + " psd=" + nooieHotspot.psd);
                }
                 */
                if (code == SDKConstant.CODE_CACHE) {
                    return;
                }
                if (code == Constant.OK) {
                    boolean checkOldPsValid = nooieHotspot != null && (TextUtils.isEmpty(nooieHotspot.psd) || nooieHotspot.psd.equals(oldPw));
                    if (checkOldPsValid) {
                        setDeviceHotSpotPw(deviceId, ssid, pw);
                    } else if (mTaskView != null) {
                        mTaskView.onSetDeviceHotSpot(SDKConstant.ERROR, ConstantValue.CHANGE_BLE_AP_DEVICE_PASSWORD_RESULT_OLD_PW_ERROR);
                    }
                } else if (mTaskView != null) {
                    mTaskView.onSetDeviceHotSpot(SDKConstant.ERROR, ConstantValue.CHANGE_BLE_AP_DEVICE_PASSWORD_RESULT_FAIL);
                }
            }
        });

    }

    private void setDeviceHotSpotPw(String deviceId, String ssid, String pw) {
        NooieHotspot nooieHotspot = new NooieHotspot(pw, ssid);
        DeviceCmdApi.getInstance().setHotspot(deviceId, nooieHotspot, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                if (mTaskView != null) {
                    mTaskView.onSetDeviceHotSpot((code == Constant.OK ? SDKConstant.SUCCESS : SDKConstant.ERROR), (code == Constant.OK ? ConstantValue.CHANGE_BLE_AP_DEVICE_PASSWORD_RESULT_SUCCESS : ConstantValue.CHANGE_BLE_AP_DEVICE_PASSWORD_RESULT_FAIL));
                }
            }
        });
    }
}
