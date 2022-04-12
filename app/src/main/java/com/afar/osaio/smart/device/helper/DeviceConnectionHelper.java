package com.afar.osaio.smart.device.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.afar.osaio.account.helper.MyAccountHelper;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.listener.ConnectShortLinkDeviceListener;
import com.afar.osaio.smart.device.listener.ReceiveDeviceCmdListener;
import com.afar.osaio.smart.device.listener.ShortLinkKeepListener;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.nooie.common.utils.notify.NotificationUtil;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.cache.DeviceConnectionCache;
import com.afar.osaio.smart.device.contract.DeviceConnectionContract;
import com.afar.osaio.smart.device.listener.DeviceConnectionListener;
import com.afar.osaio.smart.device.presenter.DeviceConnectionPresenter;
import com.nooie.common.base.GlobalData;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.device.DeviceCmdService;
import com.nooie.sdk.device.bean.DeviceConnInfo;
import com.nooie.sdk.receiver.NetworkManagerReceiver;

import java.util.ArrayList;
import java.util.List;

/**
 * DeviceConnectionHelper
 * 设备连接辅助类
 * @author Administrator
 * @date 2020/7/28
 */
public class DeviceConnectionHelper implements DeviceConnectionContract.View {

    public static final int CONNECT_SHORT_LINK_DEVICE_SUCCESS = 1;
    public static final int CONNECT_SHORT_LINK_DEVICE_RECEIVE_FAIL = 2;
    public static final int CONNECT_SHORT_LINK_DEVICE_PARAM_ERROR = 3;
    public static final int CONNECT_SHORT_LINK_DEVICE_CONNECT_P2P_FAIL = 4;
    public static final int CONNECT_SHORT_LINK_DEVICE_COUNTDOWN_SUCCESS = 5;
    public static final int CONNECT_SHORT_LINK_DEVICE_COUNTDOWN_ERROR = 6;
    public static final int CONNECT_SHORT_LINK_DEVICE_EXIST = 7;
    public static final int CONNECT_SHORT_LINK_DEVICE_CANCEL = 8;

    public static final int SHORT_LINK_KEEP_TYPE_START = 1;
    public static final int SHORT_LINK_KEEP_TYPE_TIME_END = 2;
    public static final int SHORT_LINK_KEEP_TYPE_STOP = 3;
    public static final int SHORT_LINK_KEEP_TYPE_ERROR = 4;

    public static final int SEND_HEART_BEAT = 4 * 1000;
    public final static long SENDING_HEART_BEAT_LIMIT_TIME = 15 * 1000;
    public static final int DEVICE_HB_CONNECTION_ERROR_MAX_COUNT = 3;
    public static final int RESULT_SEND_HEART_BEAT_SENDING = 1;
    public static final int RESULT_SEND_HEART_BEAT_TIME_OUT = 2;
    public static final int RESULT_SEND_HEART_BEAT_FINISH = 3;
    public static final int HB_CMD_RETRY_MAX_COUNT = 3;

    private DeviceConnectionContract.Presenter mPresenter;
    private Context mContext;
    private List<DeviceConnectionListener> mListeners;
    private ShortLinkKeepListener mShortLinkKeepListener;
    private DeviceCmdReceiver mDeviceCmdReceiver;
    private ReceiveDeviceCmdListener mShortLinkConnectListener;
    private ReceiveDeviceCmdListener mQuickShortLinkConnectListener;
    private boolean mIsDestroyShortLink = true;

    private DeviceConnectionHelper() {
        setPresenter(new DeviceConnectionPresenter(this));
        registerNetworkManagerReceiver();
        registerDeviceCmdReceiver();
    }

    private static class DeviceConnectionHelperHolder {
        private static final DeviceConnectionHelper INSTANCE = new DeviceConnectionHelper();
    }

    public static DeviceConnectionHelper getInstance() {
        return DeviceConnectionHelperHolder.INSTANCE;
    }

    @Override
    public void setPresenter(@NonNull DeviceConnectionContract.Presenter presenter) {
        mPresenter = presenter;
    }

    public void init(Context context) {
        mContext = context;
    }

    public void destroy() {
        unRegisterNetworkManagerReceiver();
        if (mPresenter != null) {
            mPresenter.destroy();
            mPresenter = null;
        }
    }

    public void removeConnectionsForAp() {
        List<String> deviceIds = new ArrayList<>();
        for (DeviceConnInfo deviceConnInfo : CollectionUtil.safeFor(DeviceConnectionCache.getInstance().getAllDeviceConnInfo())) {
            if (deviceConnInfo != null && !TextUtils.isEmpty(deviceConnInfo.getUuid())) {
                deviceIds.add(deviceConnInfo.getUuid());
            }
        }
        removeAllConnection(deviceIds);
    }

    public void removeAllConnection(List<String> deviceIds) {
        if (CollectionUtil.isEmpty(deviceIds)) {
            return;
        }
        for (String deviceId : CollectionUtil.safeFor(deviceIds)) {
            DeviceConnectionCache.getInstance().removeConnection(deviceId);
        }
    }

    private NetworkManagerReceiver mNetworkManagerReceiver;

    private void registerNetworkManagerReceiver() {
        if (mNetworkManagerReceiver == null) {
            mNetworkManagerReceiver = new NetworkManagerReceiver() {
                @Override
                public void onNetworkChanged() {
                    NooieLog.d("-->> DeviceConnectionHelper registerNetworkManagerReceiver onNetworkChanged user=" + GlobalData.getInstance().getAccount());
                    if (MyAccountHelper.getInstance().isLogin() && mPresenter != null) {
                        mPresenter.tryToReconnectWhenWifiChanged(mContext, GlobalData.getInstance().getUid());
                    }
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
            //intentFilter.addAction(SDKConstant.ACTION_NETWORK_MANAGER_ON_DETECTED);
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

    public void startCheckDeviceConnection(String user) {
        NooieLog.d("-->> debug DeviceConnectionHelper startCheckDeviceConnection user=" + user);
        if (mContext == null || TextUtils.isEmpty(user)) {
            return;
        }
        if (mPresenter != null) {
            mPresenter.setIsPauseCheckConn(false);
            //mPresenter.checkDeviceConnection(mContext, user);
            mPresenter.checkDevicesConnection(mContext, user);
        }
    }

    public void stopCheckDeviceConnection() {
        if (mPresenter != null) {
            //mPresenter.stopCheckDeviceConnection();
            mPresenter.stopCheckDevicesConnection();
        }
    }

    public void switchCheckDeviceConnection(boolean isPause) {
        if (mPresenter != null) {
            mPresenter.setIsPauseCheckConn(isPause);
        }
    }

    public void addListener(DeviceConnectionListener listener) {
        if (mListeners == null) {
            mListeners = new ArrayList<>();
        }
        mListeners.add(listener);
    }

    public void removeListener(DeviceConnectionListener listener) {
        if (CollectionUtil.isEmpty(mListeners) || !mListeners.contains(listener)) {
            return;
        }
        mListeners.remove(listener);
    }

    public void setShortLinkKeepListener(ShortLinkKeepListener listener) {
        this.mShortLinkKeepListener = listener;
    }

    private void notifyReInitDeviceConn() {
        if (CollectionUtil.isEmpty(mListeners)) {
            return;
        }
        for (DeviceConnectionListener listener : CollectionUtil.safeFor(mListeners)) {
            if (listener != null) {
                listener.onReInitDeviceConn();
            }
        }
    }

    @Override
    public void onCheckDeviceConnectResult(boolean isReconnect, String user, String deviceId, String pDeviceId) {
        NooieLog.d("-->> debug DeviceConnectionHelper onCheckDeviceConnectResult isReconnect=" + isReconnect + " deviceId=" + deviceId + " pDeviceId=" + pDeviceId);
        if (isReconnect) {
            notifyReInitDeviceConn();
        }
    }

    @Override
    public void onShortLinkKeepResult(String taskId, int code) {
        notifyShortLinkKeep(taskId, code);
    }

    @Override
    public void onSendHeartBeatResult(int code) {
    }

    public void startSendHeartBeat(String deviceId) {
        if (mPresenter != null) {
            mPresenter.startSendHeartBeat(deviceId);
        }
    }

    public void stopSendHeartBeat() {
        if (mPresenter != null) {
            mPresenter.stopSendHeartBeat();
        }
    }

    public boolean checkDirectConnectionIsError() {
        return mPresenter != null ? mPresenter.checkDirectConnectionIsError() : false;
    }

    public void startConnectShortLinkDevice(String taskId, String account, BindDevice device, ConnectShortLinkDeviceListener listener) {
        if (TextUtils.isEmpty(taskId) || TextUtils.isEmpty(account) || device == null || mPresenter == null) {
            if (listener != null) {
                listener.onResult(CONNECT_SHORT_LINK_DEVICE_PARAM_ERROR, taskId, account, new String());
            }
            return;
        }
        mShortLinkConnectListener = new ReceiveDeviceCmdListener() {
            @Override
            public void onReceiveDeviceCmdConnect(String action, String deviceId) {
                if (listener != null) {
                    if (DeviceCmdService.CONNECT_CREATE.equals(action)) {
                        listener.onResult(CONNECT_SHORT_LINK_DEVICE_SUCCESS, taskId, account, deviceId);
                    } else {
                        listener.onResult(CONNECT_SHORT_LINK_DEVICE_RECEIVE_FAIL, taskId, account, deviceId);
                    }
                }
            }
        };
        mPresenter.startConnectShortLinkDevice(taskId, account, device, listener);
    }

    public void stopConnectShortLinkDevice() {
        if (mPresenter != null) {
            mPresenter.stopConnectShortLinkDevice();
        }
        mShortLinkConnectListener = null;
    }

    public void startQuickConnectShortLinkDevice(String taskId, String account, String deviceId, String model, boolean isSubDevice, int connectionMode, ConnectShortLinkDeviceListener listener) {
        NooieLog.d("-->> debug BaseActivity connectSortLinkDevice: 1000 sortLinkDevice account=" + account + " deviceId=" + deviceId + " taskId=" + taskId);
        if (TextUtils.isEmpty(taskId) || TextUtils.isEmpty(account) || TextUtils.isEmpty(deviceId) || mPresenter == null) {
            if (listener != null) {
                listener.onResult(CONNECT_SHORT_LINK_DEVICE_PARAM_ERROR, taskId, account, deviceId);
            }
            return;
        }
        if (ApHelper.getInstance().checkIsApDirectConnectionMode() || !NooieDeviceHelper.isSortLinkDevice(model, isSubDevice, connectionMode)) {
            if (listener != null) {
                listener.onResult(DeviceConnectionHelper.CONNECT_SHORT_LINK_DEVICE_CANCEL, taskId, account, deviceId);
            }
            return;
        }
        NooieLog.d("-->> debug NooieDeviceSettingActivity tryConnectShortLinkDevice: 1001 sortLinkDevice deviceId=" + deviceId + " taskId=" + taskId);
        mQuickShortLinkConnectListener = new ReceiveDeviceCmdListener() {
            @Override
            public void onReceiveDeviceCmdConnect(String action, String deviceId) {
                if (listener != null) {
                    if (DeviceCmdService.CONNECT_CREATE.equals(action)) {
                        listener.onResult(CONNECT_SHORT_LINK_DEVICE_SUCCESS, taskId, account, deviceId);
                    } else {
                        listener.onResult(CONNECT_SHORT_LINK_DEVICE_RECEIVE_FAIL, taskId, account, deviceId);
                    }
                }
            }
        };
        mPresenter.startShortLinkKeepTask(taskId);
        if (DeviceConnectionCache.getInstance().isConnectionExist(deviceId)) {
            if (listener != null) {
                listener.onResult(DeviceConnectionHelper.CONNECT_SHORT_LINK_DEVICE_EXIST, taskId, account, deviceId);
            }
            return;
        }
        mPresenter.connectShortLinkDevice(taskId, account, deviceId, listener);
    }

    public void stopQuickConnectShortLinkDevice() {
        if (mPresenter != null) {
            mPresenter.stopQuickConnectShortLinkDevice();
        }
        mQuickShortLinkConnectListener = null;
    }

    public void stopShortLinkKeepTask() {
        if (mPresenter != null) {
            mPresenter.stopShortLinkKeepTask();
        }
    }

    public void registerDeviceCmdReceiver() {
        unregisterDeviceCmdReceiver();
        mDeviceCmdReceiver = new DeviceCmdReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DeviceCmdService.CONNECT_CREATE);
        intentFilter.addAction(DeviceCmdService.CONNECT_BROKE);
        try {
            NotificationUtil.registerLocalReceiver(NooieApplication.mCtx, mDeviceCmdReceiver, intentFilter);
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
    }

    public void unregisterDeviceCmdReceiver() {
        if (mDeviceCmdReceiver == null) {
            return;
        }
        try {
            NotificationUtil.unregisterLocalReceiver(NooieApplication.mCtx, mDeviceCmdReceiver);
            mDeviceCmdReceiver = null;
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
    }

    public void notifyReceiveDeviceCmdConnect(String action, String deviceId) {
        if (mShortLinkConnectListener != null) {
            mShortLinkConnectListener.onReceiveDeviceCmdConnect(action, deviceId);
        }
        if (mQuickShortLinkConnectListener != null) {
            mQuickShortLinkConnectListener.onReceiveDeviceCmdConnect(action, deviceId);
        }
        if (CollectionUtil.isEmpty(mListeners)) {
            return;
        }
        for (DeviceConnectionListener listener : CollectionUtil.safeFor(mListeners)) {
            if (listener != null) {
                listener.onReceiveDeviceCmdConnect(action, deviceId);
            }
        }
    }

    public void notifyShortLinkKeep(String taskId, int type) {
        if (mShortLinkKeepListener != null) {
            mShortLinkKeepListener.onShortLinkKeep(taskId, type);
        }
    }

    public void setIsDestroyShortLink(boolean isDestroyShortLink) {
        mIsDestroyShortLink = isDestroyShortLink;
    }

    public boolean checkIsDestroyShortLink() {
        return mIsDestroyShortLink;
    }

    class DeviceCmdReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                String deviceId = intent.getStringExtra(DeviceCmdService.CONNECT_UUID_KEY);
                NooieLog.d("-->> DeviceCmdReceiver onReceive B action=" + action + " deviceId=" + deviceId);
                notifyReceiveDeviceCmdConnect(action, deviceId);
            } else {
                notifyReceiveDeviceCmdConnect(new String(), new String());
            }
        }
    }
}
