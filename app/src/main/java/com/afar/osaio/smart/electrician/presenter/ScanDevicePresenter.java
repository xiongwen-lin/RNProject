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
                    //todo 做失败的处理
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
                     * 1001        网络错误
                     * 1002        配网设备激活接口调用失败，接口调用不成功
                     * 1003        配网设备激活失败，设备找不到。
                     * 1004        token 获取失败
                     * 1005        设备没有上线
                     * 1006        配网超时
                     * @param errorMsg
                     */
                    @Override
                    public void onError(String errorCode, String errorMsg) {
                        NooieLog.e("___>> EC_MODE onError errorCode " + errorCode + " errorMsg " + errorMsg);
                        dealActiveError(ConstantValue.EC_MODE, errorCode, errorMsg);
                    }

                    /**
                     * 设备配网成功,且设备上线（手机可以直接控制），可以通过
                     * @param deviceBean
                     */
                    @Override
                    public void onActiveSuccess(DeviceBean deviceBean) {
                        NooieLog.e("___>> EC_MODE onActiveSuccess ");
                        dealActiveSuccess(deviceBean);
                    }

                    /**
                     *
                     * @param step device_find 发现设备 | device_bind_success 设备绑定成功，但还未上线，此时设备处于离线状态，无法控制设备。
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

// mScanDeviceBean 来自于扫描回调的 ScanDeviceBean
                multiModeActivatorBean.deviceType = deviceType; // 设备类型
                multiModeActivatorBean.uuid = uuid; // 设备 uuid
                multiModeActivatorBean.address = address; // 设备地址
                multiModeActivatorBean.mac = mac; // 设备 mac
                multiModeActivatorBean.ssid = ssid; // Wi-Fi SSID
                multiModeActivatorBean.pwd = pw; // Wi-Fi 密码
                multiModeActivatorBean.token = token; // 获取的 Token
                multiModeActivatorBean.homeId = FamilyManager.getInstance().getCurrentHomeId(); // 当前家庭 homeId
                multiModeActivatorBean.timeout = 120000; // 超时时间

// 开始配网
                TuyaHomeSdk.getActivator().newMultiModeActivator().startActivator(multiModeActivatorBean, new IMultiModeActivatorListener() {
                    @Override
                    public void onSuccess(DeviceBean deviceBean) {
                        NooieLog.e("startActivator()___>> blue_MODE onSuccess ");
                        dealActiveSuccess(deviceBean);
                        Log.e("","onSuccess(DeviceBean deviceBean)=" +deviceBean.toString());
                        // 配网成功
                    }

                    @Override
                    public void onFailure(int code, String msg, Object handle) {
                        NooieLog.e("startActivator()___>> blue_MODE onError errorCode " + code + " errorMsg " + msg);
                        if (isBlueOn){// 配网失败 TODO 蓝牙关闭时，涂鸦配网暂停，不跳失败页面（蓝牙重新连接可重新配网）
                            dealActiveError(ConstantValue.EC_MODE, code+"", msg);
                        }
                    }
                });
            }
        }
    }

    /**
     * 配网成功
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
     * 配网失败 --- 停止配网
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
