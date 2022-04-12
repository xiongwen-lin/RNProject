package com.afar.osaio.smart.bridge.helper;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;

import com.afar.osaio.R;
import com.afar.osaio.base.ActivityStack;
import com.afar.osaio.bean.ApDeviceInfo;
import com.afar.osaio.smart.bridge.bean.ConnectBleApDeviceResult;
import com.afar.osaio.smart.bridge.presenter.SmartIpcDevicePresenter;
import com.afar.osaio.smart.bridge.presenter.contract.ISmartIpcDevicePresenter;
import com.afar.osaio.smart.device.bean.DeviceInfo;
import com.afar.osaio.smart.device.helper.IpcDeviceManageHelper;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.device.helper.SmartDeviceHelper;
import com.afar.osaio.smart.mixipc.activity.BluetoothScanActivity;
import com.afar.osaio.smart.mixipc.activity.ConnectApDeviceActivity;
import com.afar.osaio.smart.player.activity.NooiePlayActivity;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.afar.osaio.util.ConstantValue;
import com.apemans.platformbridge.bean.YRBindDeviceResult;
import com.apemans.platformbridge.bridge.contract.IBridgeDeviceHelper;
import com.apemans.platformbridge.bean.YRPlatformDevice;
import com.apemans.platformbridge.constant.BridgeConstant;
import com.apemans.platformbridge.listener.IBridgeResultListener;
import com.apemans.quickui.superdialog.BaseActionSuperDialog;
import com.apemans.quickui.superdialog.SmartDialog;
import com.dylanc.longan.ActivityKt;
import com.ly.genjidialog.other.DialogGravity;
import com.nooie.common.base.GlobalData;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.bean.IpcType;
import com.nooie.sdk.encrypt.NooieEncryptService;

import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/***********************************************************
* 作者: zhengruidong@apemans.com
* 日期: 2022/2/16 11:03 上午
* 说明:
*
* 备注:
*
***********************************************************/

public class YRBridgeDeviceHelper implements IBridgeDeviceHelper {

    public ISmartIpcDevicePresenter smartIpcDevicePresenter = new SmartIpcDevicePresenter();

    @Override
    public void queryDeviceList(String uid, String account, YRBindDeviceResult bindDeviceResult, IBridgeResultListener<List<YRPlatformDevice>> listener) {
        smartIpcDevicePresenter.queryDeviceList(account, uid, bindDeviceResult, listener);
    }

    @Override
    public YRPlatformDevice queryNetSpotDevice() {
        return smartIpcDevicePresenter.queryNetSpotDevice();
    }

    @Override
    public void refreshNetSpotConnection() {
        IpcDeviceManageHelper.getInstance().refreshNetSpotConnection();
    }

    @Override
    public void stopAPDirectConnection(String model, IBridgeResultListener listener) {
        IpcDeviceManageHelper.getInstance().stopAPDirectConnection(model, listener);
    }

    @Override
    public LiveData<String> getNetSpotConnectionState() {
        return IpcDeviceManageHelper.getInstance().netSpotConnectionState;
    }

    @Override
    public boolean checkIsNetSpot() {
        return IpcDeviceManageHelper.getInstance().checkIsNetSpot();
    }

    @Override
    public String encryptUid(String uid) {
        if (TextUtils.isEmpty(uid)) {
            return "";
        }
        return NooieEncryptService.getInstance().getTuyaPsd(uid);
    }

    @Override
    public void openAddDevicePage() {
        IpcDeviceManageHelper.getInstance().openAddDevice();
    }

    @Override
    public void openLiveAsSingle(String deviceId) {
        IpcDeviceManageHelper.getInstance().openLiveAsSingle(deviceId);
    }

    @Override
    public void openPlaybackAsSingle(String deviceId, long seekTime, boolean isCloud) {
        IpcDeviceManageHelper.getInstance().openPlaybackAsSingle(deviceId, seekTime, isCloud);
    }

    @Override
    public void openSensitivityPage(String deviceId) {
        IpcDeviceManageHelper.getInstance().openSensitivityPage(deviceId);
    }

    @Override
    public void deviceItemClick(String deviceId, String deviceInfoType, String model, String deviceSsid, String bleDeviceId, Activity activity, IBridgeResultListener<String> listener) {

        if (ActivityKt.getTopActivity() == null) {
            return;
        }

        if (SmartDeviceHelper.checkDeviceInfoTypeIsBleDirectLink(deviceInfoType)) {
            gotoPlayForDvDevice(ApHelper.getInstance().getCurrentApDeviceInfo());
            if (listener != null) {
                listener.onResult(BridgeConstant.RESULT_SUCCESS, "");
            }
        } else if (SmartDeviceHelper.checkDeviceInfoTypeIsBleNetSpot(deviceInfoType)) {
            tryGotoConnectBleApDevice(deviceId, model, deviceSsid, bleDeviceId);
            if (listener != null) {
                listener.onResult(BridgeConstant.RESULT_SUCCESS, "");
            }
        } else {
            DeviceInfo deviceInfo = NooieDeviceHelper.getDeviceInfoById(deviceId);
            String deviceModel = deviceInfo != null ? deviceInfo.getModel() : "";
            String deviceName = deviceInfo != null && deviceInfo.getNooieDevice() != null ? deviceInfo.getNooieDevice().getName() : "";
            boolean isOnline = deviceInfo != null && deviceInfo.getNooieDevice() != null ? deviceInfo.getNooieDevice().getOnline() == ApiConstant.ONLINE_STATUS_ON : false;
            boolean isCloud = deviceInfo != null ? deviceInfo.isOpenCloud() : false;
            if (isOnline || isCloud) {
                NooiePlayActivity.startPlayActivity(ActivityKt.getTopActivity(), deviceId, deviceModel, ConstantValue.NOOIE_PLAYBACK_TYPE_LIVE, ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_NORMAL, 0, ConstantValue.ROUTE_SOURCE_NORMAL, ConstantValue.CONNECTION_MODE_QC, new String());
            } else {
                showDeleteIpcDeviceDialog(GlobalData.getInstance().getAccount(), deviceId, deviceName, activity, listener);
            }
        }

    }

    @Override
    public void deviceItemSwitch(String deviceId, String deviceInfoType, String deviceSsid, boolean state, IBridgeResultListener<Boolean> listener) {
        if (SmartDeviceHelper.checkDeviceInfoTypeIsBleDirectLink(deviceInfoType)) {
            smartIpcDevicePresenter.updateApDeviceOpenStatus(deviceSsid, deviceId, state, listener);
        } else {
            smartIpcDevicePresenter.updateDeviceOpenStatus(deviceId, state, listener);
        }
    }

    @Override
    public void sendCmd(String cmd, IBridgeResultListener<String> listener) {
        smartIpcDevicePresenter.sendCmd(cmd, listener);
    }

    @Override
    public void queryAllIpcDevice(IBridgeResultListener<String> listener) {
        smartIpcDevicePresenter.queryAllIpcDevice(listener);
    }

    private void gotoPlayForDvDevice(ApDeviceInfo deviceInfo) {
        if (ActivityKt.getTopActivity() == null) {
            return;
        }
        if (deviceInfo == null || deviceInfo.getBindDevice() == null || TextUtils.isEmpty(deviceInfo.getBindDevice().getUuid()) || TextUtils.isEmpty(deviceInfo.getBindDevice().getType())) {
            return;
        }
        String deviceId = deviceInfo.getBindDevice().getUuid();
        String model = deviceInfo.getBindDevice().getType();
        String deviceSsid = deviceInfo.getDeviceSsid();
        NooiePlayActivity.startPlayActivity(ActivityKt.getTopActivity(), deviceId, model, ConstantValue.NOOIE_PLAYBACK_TYPE_LIVE, ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_NORMAL, 0, ConstantValue.ROUTE_SOURCE_ADD_DEVICE, ConstantValue.CONNECTION_MODE_AP_DIRECT, deviceSsid);
    }

    private void tryGotoConnectBleApDevice(String deviceId, String model, String ssid, String bleDeviceId) {
        if (!NooieDeviceHelper.checkBleApDeviceValid(deviceId, model, ssid)) {
            return;
        }
        if (smartIpcDevicePresenter != null) {
            smartIpcDevicePresenter.checkBeforeConnectBleDevice(bleDeviceId, model, ssid, new IBridgeResultListener<ConnectBleApDeviceResult>() {
                @Override
                public void onResult(@Nullable String code, @Nullable ConnectBleApDeviceResult data) {
                    boolean result = data != null ? data.result : false;
                    gotoConnectBleApDevice(result, bleDeviceId, model, ssid);
                }
            });
        }
    }

    private void gotoConnectBleApDevice(boolean isHotSpotMatching, String bleDeviceId, String model, String ssid) {
        if (ActivityKt.getTopActivity() == null || TextUtils.isEmpty(model)) {
            return;
        }
        Bundle param = new Bundle();
        param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, model);
        if (NooieDeviceHelper.mergeIpcType(model) == IpcType.MC120) {
            param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_AP_DIRECT);
            param.putBoolean(ConstantValue.INTENT_KEY_DATA_PARAM, false);
            ConnectApDeviceActivity.toConnectApDeviceActivity(ActivityKt.getTopActivity(), param);
        } else if (NooieDeviceHelper.mergeIpcType(model) == IpcType.HC320 && isHotSpotMatching) {
            param.putString(ConstantValue.INTENT_KEY_SSID, ssid);
            param.putString(ConstantValue.INTENT_KEY_BLE_DEVICE, bleDeviceId);
            param.putString(ConstantValue.INTENT_KEY_PSD, "12345678");
            param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_AP_DIRECT);
            param.putBoolean(ConstantValue.INTENT_KEY_DATA_PARAM, false);
            ConnectApDeviceActivity.toConnectApDeviceActivity(ActivityKt.getTopActivity(), param);
        } else if (NooieDeviceHelper.mergeIpcType(model) == IpcType.HC320) {
            param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_AP_DIRECT);
            param.putInt(ConstantValue.INTENT_KEY_DATA_PARAM_1, ConstantValue.BLUETOOTH_SCAN_TYPE_EXIST);
            param.putString(ConstantValue.INTENT_KEY_BLE_DEVICE, bleDeviceId);
            BluetoothScanActivity.toBluetoothScanActivity(ActivityKt.getTopActivity(), param);
        }
    }

    private void showDeleteIpcDeviceDialog(String account, String deviceId, String name, Activity activity, IBridgeResultListener<String> listener) {
        if (activity == null || !(activity instanceof AppCompatActivity)) {
            return;
        }
        AppCompatActivity ac = (AppCompatActivity) activity;
        if (ac != null) {
            SmartDialog.Companion.build(ac.getSupportFragmentManager(), ac, null)
                    .setTitle(ac.getString(R.string.camera_settings_remove_camera_confirm))
                    .setContentText(String.format(ac.getString(R.string.camera_settings_remove_info_confirm), name))
                    .setPositiveTextName(ac.getString(R.string.key_message_Subscribe))
                    .setNegativeTextName(ac.getString(R.string.key_message_Cancel))
                    .setOnPositive(new Function1<BaseActionSuperDialog, Unit>() {
                        @Override
                        public Unit invoke(BaseActionSuperDialog baseActionSuperDialog) {
                            if (baseActionSuperDialog != null) {
                                baseActionSuperDialog.dismiss();
                            }
                            if (!TextUtils.isEmpty(deviceId) && smartIpcDevicePresenter != null) {
                                smartIpcDevicePresenter.removeIpcDevice(account, deviceId, listener);
                            }
                            return null;
                        }
                    })
                    .setOnNegative(new Function1<BaseActionSuperDialog, Unit>() {
                        @Override
                        public Unit invoke(BaseActionSuperDialog baseActionSuperDialog) {
                            if (baseActionSuperDialog != null) {
                                baseActionSuperDialog.dismiss();
                            }
                            if (listener != null) {
                                listener.onResult(BridgeConstant.RESULT_SUCCESS, "remove_cancel");
                            }
                            return null;
                        }
                    })
                    .show(DialogGravity.CENTER_CENTER, R.style.AlphaEnterExitAnimation);
        }
    }
}
