package com.afar.osaio.smart.player.component;

import android.os.Bundle;
import android.text.TextUtils;

import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.encrypt.MD5Util;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.tool.TaskUtil;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.device.bean.FormatInfo;
import com.nooie.sdk.listener.OnGetFormatInfoListener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;

import java.util.HashMap;
import java.util.Map;

public class DeviceCmdComponent {

    private static final int RETRY_COUNT_SD_MOUNTING_MAX = 3;
    private static final int RETRY_COUNT_SD_MOUNTING_INTERVAL_TIME = 3 * 1000;

    private Bundle mParam;
    private Map<String, Integer> mRetryCountOfSDMountingMap = new HashMap<>();

    public DeviceCmdComponent() {
    }

    public DeviceCmdComponent(Bundle param) {
        setParam(param);
    }

    public void setParam(Bundle param) {
        mParam = param;
    }

    public void getFormatInfo(String deviceId, boolean isMounted, OnGetFormatInfoListener listener) {
        NooieLog.d("-->> debug DeviceCmdComponent getFormatInfo 1001 deviceId=" + deviceId + " isMounted=" + isMounted);
        if (isMounted) {
            DeviceCmdApi.getInstance().getFormatInfo(deviceId, listener);
            return;
        }
        String taskId = createGetFormatInfoTaskId(deviceId);
        updateRetryCountOfSDMounting(taskId, 1);
        getFormatInfoBeforeSDMounted(deviceId, taskId, listener);
    }

    public String createGetFormatInfoTaskId(String deviceId) {
        String taskIdStr = deviceId + System.currentTimeMillis();
        return MD5Util.MD5Hash(taskIdStr);
    }

    private void getFormatInfoBeforeSDMounted(String deviceId, String taskId, OnGetFormatInfoListener listener) {
        NooieLog.d("-->> debug DeviceCmdComponent getFormatInfoBeforeSDMounted 1001 deviceId=" + deviceId);
        DeviceCmdApi.getInstance().getFormatInfo(deviceId, new OnGetFormatInfoListener() {
            @Override
            public void onGetFormatInfo(int code, FormatInfo formatInfo) {
                int retryCountOfSDMounting = getRetryCountOfSDMounting(taskId);
                NooieLog.d("-->> debug DeviceCmdComponent getFormatInfoBeforeSDMounted 1002 deviceId=" + deviceId + code + " retryCountOfSDMounting=" + retryCountOfSDMounting);
                if (code == SDKConstant.CODE_CACHE) {
                    NooieLog.d("-->> debug DeviceCmdComponent getFormatInfoBeforeSDMounted 1003 deviceId=" + deviceId);
                    if (retryCountOfSDMounting == 1) {
                        NooieLog.d("-->> debug DeviceCmdComponent getFormatInfoBeforeSDMounted 1004 deviceId=" + deviceId);
                        listener.onGetFormatInfo(code, formatInfo);
                    }
                    return;
                }
                boolean isRetryForMounting = code == Constant.OK && formatInfo != null && formatInfo.getFormatStatus() == ConstantValue.NOOIE_SD_STATUS_MOUNTING
                        && retryCountOfSDMounting < RETRY_COUNT_SD_MOUNTING_MAX;
                increaseRetryCountOfSDMounting(taskId);
                NooieLog.d("-->> debug DeviceCmdComponent getFormatInfoBeforeSDMounted 1005 deviceId=" + deviceId);
                if (isRetryForMounting) {
                    int retryDelayTime = RETRY_COUNT_SD_MOUNTING_INTERVAL_TIME + retryCountOfSDMounting * 1000;
                    NooieLog.d("-->> debug DeviceCmdComponent getFormatInfoBeforeSDMounted 1006 deviceId=" + deviceId + " retryDelayTime=" + retryDelayTime);
                    TaskUtil.delayAction(retryDelayTime, new TaskUtil.OnDelayTimeFinishListener() {
                        @Override
                        public void onFinish() {
                            NooieLog.d("-->> debug DeviceCmdComponent getFormatInfoBeforeSDMounted 1007 deviceId=" + deviceId);
                            getFormatInfoBeforeSDMounted(deviceId, taskId, listener);
                        }
                    });
                } else if (listener != null) {
                    NooieLog.d("-->> debug DeviceCmdComponent getFormatInfoBeforeSDMounted 1008 deviceId=" + deviceId);
                    removeCountOfSDMounting(taskId);
                    listener.onGetFormatInfo(code, formatInfo);
                }
            }
        });
    }

    private void initRetryCountOfSDMountingMap() {
        if (mRetryCountOfSDMountingMap == null) {
            mRetryCountOfSDMountingMap = new HashMap<>();
        }
    }

    private void updateRetryCountOfSDMounting(String key, int count) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        initRetryCountOfSDMountingMap();
        mRetryCountOfSDMountingMap.put(key, count);
    }

    private int getRetryCountOfSDMounting(String key) {
        if (TextUtils.isEmpty(key) || mRetryCountOfSDMountingMap == null || !mRetryCountOfSDMountingMap.containsKey(key)) {
            return RETRY_COUNT_SD_MOUNTING_MAX;
        }
        return mRetryCountOfSDMountingMap.get(key);
    }

    private void increaseRetryCountOfSDMounting(String key) {
        if (TextUtils.isEmpty(key) || mRetryCountOfSDMountingMap == null || !mRetryCountOfSDMountingMap.containsKey(key)) {
            return;
        }
        updateRetryCountOfSDMounting(key, getRetryCountOfSDMounting(key) + 1);
    }

    private void removeCountOfSDMounting(String key) {
        if (TextUtils.isEmpty(key) || mRetryCountOfSDMountingMap == null || !mRetryCountOfSDMountingMap.containsKey(key)) {
            return;
        }
        mRetryCountOfSDMountingMap.remove(key);
    }
}
