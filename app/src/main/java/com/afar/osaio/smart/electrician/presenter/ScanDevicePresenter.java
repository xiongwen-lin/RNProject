package com.afar.osaio.smart.electrician.presenter;

import android.text.TextUtils;
import android.util.Log;

import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.model.IScanDeviceModel;
import com.afar.osaio.smart.electrician.model.ScanDeviceModel;
import com.afar.osaio.smart.electrician.view.IScanDeviceView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IMultiModeActivatorListener;
import com.tuya.smart.sdk.api.ITuyaActivatorGetToken;
import com.tuya.smart.sdk.api.ITuyaSmartActivatorListener;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.MultiModeActivatorBean;
import com.tuya.smart.sdk.enums.ActivatorAPStepCode;
import com.tuya.smart.sdk.enums.ActivatorEZStepCode;

/**
 * ScanDevicePresenter
 *
 * @author Administrator
 * @date 2019/3/5
 */
public class ScanDevicePresenter implements IScanDevicePresenter {

    private IScanDeviceView mView;
    private IScanDeviceModel mScanDeviceModel;
    private boolean  isBlueOn = false;


    public ScanDevicePresenter(IScanDeviceView view) {
        mView = view;
        mScanDeviceModel = new ScanDeviceModel();
    }

    private  void setBlueState(boolean  on ){
        isBlueOn = on;
    }

    @Override
    public void startDeviceSearch(final int mode, final String ssid, final String pw, String token,int deviceType,String uuid,String address,String mac) {
        long homeId = FamilyManager.getInstance().getCurrentHomeId();

        if (TextUtils.isEmpty(ssid) || homeId == 0 || homeId == FamilyManager.DEFAULT_HOME_ID) {
            return;
        }

        NooieLog.e("----> startDeviceSearch mode " + mode + " ssid " + ssid + " pw " + pw + " token " + token);

        if (mode == ConstantValue.AP_MODE) {
            NooieLog.e("----> startDeviceSearch mode " + mode + " ssid " + ssid + " pw " + pw + " token " + token + "   setDeviceToken ");
            setDeviceToken(mode, ssid, pw, token,deviceType,uuid,address,uuid);
        } else {

            TuyaHomeSdk.getActivatorInstance().getActivatorToken(homeId, new ITuyaActivatorGetToken() {
                @Override
                public void onSuccess(String devicetoken) {
                    setDeviceToken(mode, ssid, pw, devicetoken,deviceType,uuid,address,uuid);
                }

                @Override
                public void onFailure(String s, String s1) {
                    //todo ??????????????????
                }
            });
        }
    }

    @Override
    public void stopDeviceSearch() {
        stopSearch();
    }

    @Override
    public void setDeviceBlueState(String uuid, boolean isOn) {
        if (!isOn){
            TuyaHomeSdk.getActivator().newMultiModeActivator().stopActivator(uuid);
        }
        setBlueState(isOn);
    }


    @Override
    public void release() {
        destorySearch();
    }

    private void setDeviceToken(int mode, String ssid, String pw, String token,int deviceType,String uuid,String address,String mac) {
        if (!TextUtils.isEmpty(token)) {
            if (mode == ConstantValue.EC_MODE) {
                mScanDeviceModel.setEC(ssid, pw, token, new ITuyaSmartActivatorListener() {
                    /**
                     *
                     * @param errorCode:
                     * 1001        ????????????
                     * 1002        ????????????????????????????????????????????????????????????
                     * 1003        ?????????????????????????????????????????????
                     * 1004        token ????????????
                     * 1005        ??????????????????
                     * 1006        ????????????
                     * @param errorMsg
                     */
                    @Override
                    public void onError(String errorCode, String errorMsg) {
                        NooieLog.e("___>> EC_MODE onError errorCode " + errorCode + " errorMsg " + errorMsg);
                        dealActiveError(ConstantValue.EC_MODE, errorCode, errorMsg);
                    }

                    /**
                     * ??????????????????,????????????????????????????????????????????????????????????
                     * @param deviceBean
                     */
                    @Override
                    public void onActiveSuccess(DeviceBean deviceBean) {
                        NooieLog.e("___>> EC_MODE onActiveSuccess ");
                        dealActiveSuccess(deviceBean);
                    }

                    /**
                     *
                     * @param step device_find ???????????? | device_bind_success ?????????????????????????????????????????????????????????????????????????????????????????????
                     * @param o devId (String)  |dev (DeviceBean)
                     */
                    @Override
                    public void onStep(String step, Object o) {
                        NooieLog.e("___>> EC_MODE onStep  step " + step + " o " + String.valueOf(o));
                        switch (step) {
                            case ActivatorEZStepCode.DEVICE_FIND: {
                                break;
                            }
                            case ActivatorEZStepCode.DEVICE_BIND_SUCCESS: {
                                break;
                            }
                        }
                    }
                });
                startSearch();
            } else if (mode == ConstantValue.AP_MODE) {

                NooieLog.e("___>> AP_MODE setAP  ssid " + ssid + " pw " + pw + " token " + token);

                mScanDeviceModel.setAP(ssid, pw, token, new ITuyaSmartActivatorListener() {
                    @Override
                    public void onError(String errorCode, String errorMsg) {
                        NooieLog.e("___>> AP_MODE onError  errorCode " + errorCode + " errorMsg " + errorMsg);
                        dealActiveError(ConstantValue.AP_MODE, errorCode, errorMsg);
                    }

                    @Override
                    public void onActiveSuccess(DeviceBean deviceBean) {
                        NooieLog.e("___>> AP_MODE onActiveSuccess ");
                        dealActiveSuccess(deviceBean);
                    }

                    @Override
                    public void onStep(String step, Object o) {
                        NooieLog.e("___>> AP_MODE onStep step " + step + "  o " + String.valueOf(o));
                        switch (step) {
                            case ActivatorAPStepCode.DEVICE_FIND: {
                                break;
                            }
                            case ActivatorAPStepCode.DEVICE_BIND_SUCCESS: {
                                break;
                            }
                        }
                    }
                });
                startSearch();
            }else if (mode == ConstantValue.BLUE_MODE) {
                MultiModeActivatorBean multiModeActivatorBean = new MultiModeActivatorBean();

// mScanDeviceBean ???????????????????????? ScanDeviceBean
                multiModeActivatorBean.deviceType = deviceType; // ????????????
                multiModeActivatorBean.uuid = uuid; // ?????? uuid
                multiModeActivatorBean.address = address; // ????????????
                multiModeActivatorBean.mac = mac; // ?????? mac
                multiModeActivatorBean.ssid = ssid; // Wi-Fi SSID
                multiModeActivatorBean.pwd = pw; // Wi-Fi ??????
                multiModeActivatorBean.token = token; // ????????? Token
                multiModeActivatorBean.homeId = FamilyManager.getInstance().getCurrentHomeId(); // ???????????? homeId
                multiModeActivatorBean.timeout = 120000; // ????????????

// ????????????
                TuyaHomeSdk.getActivator().newMultiModeActivator().startActivator(multiModeActivatorBean, new IMultiModeActivatorListener() {
                    @Override
                    public void onSuccess(DeviceBean deviceBean) {
                        NooieLog.e("startActivator()___>> blue_MODE onSuccess ");
                        dealActiveSuccess(deviceBean);
                        Log.e("","onSuccess(DeviceBean deviceBean)=" +deviceBean.toString());
                        // ????????????
                    }

                    @Override
                    public void onFailure(int code, String msg, Object handle) {
                        NooieLog.e("startActivator()___>> blue_MODE onError errorCode " + code + " errorMsg " + msg);
                        if (isBlueOn){// ???????????? TODO ????????????????????????????????????????????????????????????????????????????????????????????????
                            dealActiveError(ConstantValue.EC_MODE, code+"", msg);
                        }
                    }
                });
            }
        }
    }

    /**
     * ????????????
     *
     * @param deviceBean
     */
    private void dealActiveSuccess(DeviceBean deviceBean) {
        stopDeviceSearch();
        if (mView != null) {
            mView.onDeviceSearchSuccess(deviceBean);
        }
    }

    /**
     * ???????????? --- ????????????
     *
     * @param mode
     * @param errorCode
     * @param errorMsg
     */
    private void dealActiveError(int mode, String errorCode, String errorMsg) {
        stopDeviceSearch();
        if (mView != null) {
            mView.onDeviceSearchFailed();
        }
    }

    private void startSearch() {
        mScanDeviceModel.start();
    }

    private void stopSearch() {
        mScanDeviceModel.cancel();
    }

    private void destorySearch() {
        mScanDeviceModel.destroy();
    }


}
