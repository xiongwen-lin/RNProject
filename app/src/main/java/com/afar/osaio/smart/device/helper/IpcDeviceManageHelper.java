package com.afar.osaio.smart.device.helper;

import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;

import com.afar.osaio.base.ActivityStack;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.ShortLinkDeviceParam;
import com.afar.osaio.smart.device.bean.DeviceInfo;
import com.afar.osaio.smart.device.presenter.IIpcDeviceManagePresenter;
import com.afar.osaio.smart.device.presenter.IpcDeviceManagePresenter;
import com.afar.osaio.smart.player.activity.NooiePlayActivity;
import com.afar.osaio.smart.scan.activity.AddCameraSelectActivity;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.afar.osaio.smart.setting.activity.DetectionSettingActivity;
import com.afar.osaio.util.ConstantValue;
import com.apemans.platformbridge.constant.BridgeConstant;
import com.apemans.platformbridge.listener.IBridgeResultListener;
import com.dylanc.longan.ActivityKt;
import com.nooie.common.base.GlobalData;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.notify.NotificationUtil;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.base.AppStateManager;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.bean.IpcType;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.receiver.NetworkManagerReceiver;

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2022/2/21 11:39 上午
 * 说明:
 *
 * 备注:
 *
 ***********************************************************/
public class IpcDeviceManageHelper {

    public final static String NET_SPOT_NORMAL = "NORMAL";
    public final static String NET_SPOT_CONNECTED = "CONNECTED";
    public final static String NET_SPOT_DISCONNECTED = "DISCONNECTED";

    public MutableLiveData<String> netSpotConnectionState = new MutableLiveData<String>(NET_SPOT_NORMAL);

    private IIpcDeviceManagePresenter mPresenter = null;
    private NetworkManagerReceiver mNetworkManagerReceiver;
    private CustomAppStateManagerListener mAppStateManagerListener = null;
    private DeviceApHelperListener mDeviceApHelperListener;

    private IpcDeviceManageHelper() {
        mPresenter = new IpcDeviceManagePresenter();
        registerDeviceApHelperListener();
        registerNetworkManagerReceiver();
        registerAppStateListener();
    }

    private static class IpcDeviceManageHelperInstance {
        private static final IpcDeviceManageHelper INSTANCE = new IpcDeviceManageHelper();
    }

    public static IpcDeviceManageHelper getInstance() {
        return IpcDeviceManageHelperInstance.INSTANCE;
    }

    public void release() {
        unregisterDeviceApHelperListener();
        unRegisterNetworkManagerReceiver();
        unRegisterAppStateListener();
    }

    public void openAddDevice() {
        if (ActivityKt.getTopActivity() != null) {
            AddCameraSelectActivity.toAddCameraSelectActivity(ActivityKt.getTopActivity());
        }
    }

    public void openLiveAsSingle(String deviceId) {
        DeviceInfo deviceInfo = NooieDeviceHelper.getDeviceInfoById(deviceId);
        String deviceModel = deviceInfo != null && deviceInfo.getNooieDevice() != null ? deviceInfo.getNooieDevice().getType(): IpcType.IPC_1080.getType();
        if (ActivityKt.getTopActivity() != null) {
            NooiePlayActivity.startPlayActivityBySingleTop(ActivityKt.getTopActivity(), deviceId, deviceModel, ConstantValue.NOOIE_PLAYBACK_TYPE_LIVE, ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_NORMAL, 0, ConstantValue.ROUTE_SOURCE_NORMAL, ConstantValue.CONNECTION_MODE_QC);
        }
    }

    public void openPlaybackAsSingle(String deviceId, long seekTime, boolean isCloud) {
        int playbackType = isCloud ? ConstantValue.NOOIE_PLAYBACK_TYPE_CLOUD : ConstantValue.NOOIE_PLAYBACK_TYPE_SD;
        DeviceInfo deviceInfo = NooieDeviceHelper.getDeviceInfoById(deviceId);
        String deviceModel = deviceInfo != null && deviceInfo.getNooieDevice() != null ? deviceInfo.getNooieDevice().getType(): IpcType.IPC_1080.getType();
        if (ActivityKt.getTopActivity() != null) {
            NooiePlayActivity.startPlayActivityBySingleTop(ActivityKt.getTopActivity(), deviceId, deviceModel, playbackType, ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_DIRECT, seekTime, ConstantValue.ROUTE_SOURCE_NORMAL, ConstantValue.CONNECTION_MODE_QC);
        }
    }

    public void openSensitivityPage(String deviceId) {
        BindDevice device = NooieDeviceHelper.getDeviceById(deviceId);
        if (device == null || TextUtils.isEmpty(device.getType())) {
            return;
        }
        if (ActivityKt.getTopActivity() != null) {
            DetectionSettingActivity.toDetectionSettingActivity(ActivityKt.getTopActivity(), deviceId, device.getType());
        }
    }

    public void refreshNetSpotConnection() {
        if (mPresenter != null) {
            mPresenter.checkBleApDeviceConnecting(new IpcDeviceManagePresenter.OnCheckBleApDeviceConnecting() {
                @Override
                public void onCheckBleApDeviceConnecting(String state) {
                    updateNetSpotConnectionState(state);
                }
            });
        }
    }

    public void stopAPDirectConnection(String model, IBridgeResultListener listener) {
        if (mPresenter != null) {
            mPresenter.stopAPDirectConnection(model, new IpcDeviceManagePresenter.OnStopAPDirectConnection() {
                @Override
                public void onStopAPDirectConnection(int state) {
                    updateNetSpotConnectionState(NET_SPOT_DISCONNECTED);
                    if (listener != null) {
                        listener.onResult(BridgeConstant.RESULT_SUCCESS, null);
                    }
                }
            });
        }
    }

    public boolean checkIsNetSpot() {
        return ApHelper.getInstance().checkBleApDeviceConnectingExist();
    }

    public ShortLinkDeviceParam getShortLinkDeviceParam(String uuid) {
        BindDevice device = NooieDeviceHelper.getDeviceById(uuid);
        if (device == null) {
            return null;
        }
        String model = device.getType();
        int connectionMode = checkIsNetSpot() ? ConstantValue.CONNECTION_MODE_AP_DIRECT : ConstantValue.CONNECTION_MODE_QC;
        boolean isSubDevice = NooieDeviceHelper.isSubDevice(device.getPuuid(), device.getType());
        ShortLinkDeviceParam shortLinkDeviceParam = new ShortLinkDeviceParam(GlobalData.getInstance().getUid(), uuid, model, isSubDevice, false, connectionMode);
        return shortLinkDeviceParam;
    }

    public boolean checkIsShortLink(String uuid) {
        return NooieDeviceHelper.isSortLinkDevice(getShortLinkDeviceParam(uuid));
    }

    private void updateNetSpotConnectionState(String state) {
        if (NET_SPOT_CONNECTED.equalsIgnoreCase(state) || NET_SPOT_DISCONNECTED.equalsIgnoreCase(state)) {
            netSpotConnectionState.setValue(state);
        }
    }

    private void gotoHomePage(int type) {
        //todo go to home page
    }

    private void registerNetworkManagerReceiver() {
        if (mNetworkManagerReceiver == null) {
            mNetworkManagerReceiver = new NetworkManagerReceiver() {

                @Override
                public void onNetworkChanged() {
                    ApHelper.getInstance().notifyNetworkChange();
                }

                @Override
                public void onNetworkDetected(Bundle data) {
                }

                @Override
                public void onNetworkOperated(Bundle data) {
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SDKConstant.ACTION_NETWORK_MANAGER_ON_CHANGED);
            intentFilter.addAction(SDKConstant.ACTION_NETWORK_MANAGER_ON_DETECTED);
            intentFilter.addAction(SDKConstant.ACTION_NETWORK_MANAGER_ON_OPERATED);
            NotificationUtil.registerLocalReceiver(NooieApplication.mCtx, mNetworkManagerReceiver, intentFilter);
        }
    }

    private void unRegisterNetworkManagerReceiver() {
        if (mNetworkManagerReceiver != null) {
            NotificationUtil.unregisterLocalReceiver(NooieApplication.mCtx, mNetworkManagerReceiver);
            mNetworkManagerReceiver = null;
        }
    }

    private void registerAppStateListener() {
        if (mAppStateManagerListener == null) {
            mAppStateManagerListener = new CustomAppStateManagerListener();
        }
        AppStateManager.getInstance().addListener(mAppStateManagerListener);
    }

    private void unRegisterAppStateListener() {
        if (mAppStateManagerListener != null) {
            AppStateManager.getInstance().removeListener(mAppStateManagerListener);
        }
    }

    private void registerDeviceApHelperListener() {
        if (mDeviceApHelperListener == null) {
            mDeviceApHelperListener = new DeviceApHelperListener();
        }
        ApHelper.getInstance().addListener(mDeviceApHelperListener);
    }

    private void unregisterDeviceApHelperListener() {
        if (mDeviceApHelperListener != null) {
            unregisterDeviceApHelperListener();
            mDeviceApHelperListener = null;
        }
    }

    private class CustomAppStateManagerListener implements AppStateManager.AppStateManagerListener {

        @Override
        public void onAppBackground() {
            NooieLog.d("-->> debug BaseActivity onAppBackground");
            ApHelper.getInstance().checkBleApDeviceConnectionBackgroundKeepingTask(true, null);
        }

        @Override
        public void onAppForeground() {
            NooieLog.d("-->> debug BaseActivity onAppForeground");
            ApHelper.getInstance().checkBleApDeviceConnectionBackgroundKeepingTask(false, new ApHelper.BleApConnectionBackgroundKeepingListener() {
                @Override
                public void onResult(int state, Bundle param) {
                    NooieLog.d("-->> debug BaseActivity onAppForeground 1002 state=" + state + " isEnterApDevicePage=" +  ApHelper.getInstance().getIsEnterApDevicePage());
                    if (state == ApHelper.BLE_AP_CONNECTION_KEEPING_BACKGROUND_STATE_DISCONNECTED && ApHelper.getInstance().getIsEnterApDevicePage()) {
                        NooieLog.d("-->> debug BaseActivity onAppForeground 1003");
                        gotoHomePage(0);
                    }
                }
            });
        }
    }

    private class DeviceApHelperListener implements ApHelper.ApHelperListener {

        @Override
        public void onApHeartBeatResponse(int code) {
            NooieLog.d("-->> debug DeviceApHelperListener onApHeartBeatResponse: code=" + code + " checkApDirectConnectionIsError=" + ApHelper.getInstance().checkApDirectConnectionIsError() + " currentConnectionMode=" + ApHelper.getInstance().getCurrentConnectionMode());
            if (code == Constant.OK && ApHelper.getInstance().checkBleApDeviceConnectingExist()) {
            } else if (code == Constant.ERROR && ApHelper.getInstance().checkApDirectConnectionIsError()) {
                if (mPresenter != null) {
                    mPresenter.stopAPDirectConnection("", new IpcDeviceManagePresenter.OnStopAPDirectConnection() {
                        @Override
                        public void onStopAPDirectConnection(int state) {
                            updateNetSpotConnectionState(NET_SPOT_DISCONNECTED);
                        }
                    });
                }
            }
        }

        @Override
        public void onNetworkChange() {
            NooieLog.d("-->> debug DeviceApHelperListener onNetworkChange: ");
            if (mPresenter != null) {
                mPresenter.checkApDirectWhenNetworkChange(new IpcDeviceManagePresenter.OnCheckApDirectWhenNetworkChange() {
                    @Override
                    public void onCheckApDirectWhenNetworkChange(String state) {
                        updateNetSpotConnectionState(state);
                    }
                });
            }
        }
    }
}
