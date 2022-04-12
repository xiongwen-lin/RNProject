package com.afar.osaio.smart.push.firebase.analytics;

import android.os.Bundle;

import com.afar.osaio.base.NooieApplication;
import com.nooie.common.utils.network.NetworkUtil;

/**
 * FirebaseEventManager
 *
 * @author Administrator
 * @date 2021/6/12
 */
public class FirebaseEventManager {

    private FirebaseEventManager() {
    }

    private static class FirebaseEventManagerHolder {
        private static final FirebaseEventManager INSTANCE = new FirebaseEventManager();
    }

    public static FirebaseEventManager getInstance() {
        return FirebaseEventManagerHolder.INSTANCE;
    }

    public void sendLoginResultEvent(boolean result, String message) {
        Bundle param = new Bundle();
        param.putString(FirebaseConstant.KEY_RESULT, (result ? "Success" : "Fail"));
        param.putString(FirebaseConstant.KEY_MESSAGE_TYPE, message);
        param.putString(FirebaseConstant.KEY_NETWORK_TYPE, NetworkUtil.networkType(NooieApplication.mCtx));
        FirebaseAnalyticsManager.getInstance().logEvent(FirebaseConstant.EVENT_LOGIN_RESULT, param);
    }

    public void sendDeviceBindingEvent(String deviceId, String result) {
        Bundle param = new Bundle();
        param.putString(FirebaseConstant.KEY_DEVICE_UUID, deviceId);
        param.putString(FirebaseConstant.KEY_RESULT, result);
        FirebaseAnalyticsManager.getInstance().logEvent(FirebaseConstant.EVENT_DEVICE_BINDING, param);
    }

    public void sendCloudPackPageStartLoading(String deviceId, String deviceName, String deviceModel, String origin) {
        Bundle param = new Bundle();
        param.putString(FirebaseConstant.KEY_DEVICE_UUID, deviceId);
        param.putString(FirebaseConstant.KEY_DEVICE_NAME, deviceName);
        param.putString(FirebaseConstant.KEY_DEVICE_MODEL, deviceModel);
        param.putString(FirebaseConstant.KEY_ORIGIN, origin);
        FirebaseAnalyticsManager.getInstance().logEvent(FirebaseConstant.EVENT_CLOUD_PACK_PAGE_START_LOADING, param);
    }

    public void sendCloudPackPageFinishLoading(String deviceId, String deviceName, String deviceModel, String origin, String language, String enterMark, String result) {
        Bundle param = new Bundle();
        param.putString(FirebaseConstant.KEY_DEVICE_UUID, deviceId);
        param.putString(FirebaseConstant.KEY_DEVICE_NAME, deviceName);
        param.putString(FirebaseConstant.KEY_DEVICE_MODEL, deviceModel);
        param.putString(FirebaseConstant.KEY_ORIGIN, origin);
        param.putString(FirebaseConstant.KEY_LANGUAGE, language);
        param.putString(FirebaseConstant.KEY_ENTER_MARK, enterMark);
        param.putString(FirebaseConstant.KEY_RESULT, result);
        FirebaseAnalyticsManager.getInstance().logEvent(FirebaseConstant.EVENT_CLOUD_PACK_PAGE_FINISH_LOADING, param);
    }

}