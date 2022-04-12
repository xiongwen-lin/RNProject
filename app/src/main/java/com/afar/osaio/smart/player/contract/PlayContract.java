package com.afar.osaio.smart.player.contract;

import androidx.annotation.NonNull;

import com.afar.osaio.smart.device.bean.CloudFileBean;
import com.nooie.sdk.api.network.base.bean.entity.AppVersionResult;
import com.nooie.sdk.api.network.base.bean.entity.PackInfoResult;
import com.nooie.sdk.db.entity.DeviceHardVersionEntity;
import com.nooie.sdk.device.bean.RecordFragment;
import com.nooie.sdk.device.listener.OnSwitchStateListener;
import com.nooie.sdk.listener.OnActionResultListener;
import com.afar.osaio.base.mvp.IBasePresenter;
import com.afar.osaio.smart.device.bean.CloudRecordInfo;

import java.util.List;

public interface PlayContract {

    interface View {

        void setPresenter(@NonNull Presenter presenter);

        void onLoadFirmwareInfoSuccess(AppVersionResult result);

        void onLoadFirmwareInfoFailed(String msg);

        void notifyGetDeviceStorageState(boolean isOpenCloud, boolean isHasSDCard, int status);

        void onLoadPackInfoSuccess(PackInfoResult result);

        void onLoadSDCardRecentDaySuccess(int[] recentDays);

        void onRequestShortLinkDeviceFormatInfo(String user, String deviceId, final boolean isOpenCloud, int status, boolean isSubDevice, boolean isShortLinkDevice);

        void onLoadStorageResult(long time);

        void onLoadDeviceSdCardRecordSuccess(List<CloudRecordInfo> cloudRecordInfos, String taskId);

        void onLoadDeviceSdCardRecordFailed(String msg);

        void onLoadDeviceCloudRecordSuccess(String taskId, List<CloudRecordInfo> cloudRecordInfos, List<RecordFragment> recordFragments, String fileType, int expireDate, String picType, String filePrefix);

        void onLoadDeviceCloudRecordFailed(String msg);

        void onLoadDetections(String result, int requestType, int page, long timeStamp, long seekTime, String fileType, int expiration, String filePrefix, int bindType);

        void onLoadDeviceMsgResult(String result, int requestType, String account, String uid, String deviceId, List<CloudFileBean> cloudFileBeans, String fileType, int expiration, String filePrefix);

        void onQueryDeviceUpdateState(int type);

        void showNoRecording(boolean isEmpty, long time);

        void onLpCameraPlayFinish(String result, String deviceId, int playType);

        void onQueryDeviceGuide(String result, boolean isShowGuide);

        void onQueryDeviceTalkGuide(String result, boolean isShowGuide);

        void onShowTalkBubble(String result);

        void onGetFlashLight(int state, boolean on);

        void onSetFlashLight(int state);

        void displayLoading(boolean show);

        void onLpDeviceCountDownTask(int state, int type, String deviceId);

        void onGetApDeviceUpgradeInfo(int state, DeviceHardVersionEntity result, String version);

    }

    interface Presenter extends IBasePresenter {

        void destroy();

//        void detachView();

        void getDeviceStorageState(String user, final String deviceId, boolean isRequsetSd, boolean isSubDevice, int connectionMode, int bindType, boolean isShortLinkDevice);

        void getDeviceFormatInfo(String user, String deviceId, final boolean isOpenCloud, int status, boolean isSubDevice, boolean isShortLinkDevice);

        void getStorageInfoByAp(String user, String deviceId, boolean isRequest, boolean isSubDevice);

        void loadSDCardRecentDay(String deviceId, boolean isSubDevice);

        void loadDeviceSdCardRecordList(final String deviceId, long start, boolean isLpDevice, String taskId);

        void loadDeviceCloudRecordList(final String deviceId, long start, boolean isLpDevice, String account, int bindType, String taskId);

        void stopLoadRecordTask();

        void loadMoreDeviceMsgByTime(String account, String uid, int page, String deviceId, long time, int direction, long timeStamp, int type, int rows, String sort, String fileType, int expiration, String filePrefix, int bindType);

        void stopLoadMoreDeviceMsgTask();

        void loadDeviceMsgByTime(String account, String uid, String deviceId, long time, int direction, long timeStamp, int type, int rows, String sort, String fileType, int expiration, String filePrefix, int bindType);

        void stopLoadDeviceMsgTask();

        void loadFirmwareVersion(String deviceId, String model);

        void queryNooieDeviceUpdateStatus(String deviceId, String account);

        void setDeviceAlarmAudio(String deviceId, boolean on, int id, int time, int num, OnActionResultListener listener);

        void getDeviceAlarmAudio(String deviceId, OnSwitchStateListener listener);

        void resetDataEffectCache();

        void startLpCameraPlayTask(String deviceId, int playType);

        void stopLpCameraPlayTask();

        boolean isLpCameraPlayTaskRunning();

        void queryDeviceGuide(String deviceId, String account, boolean isOwner);

        void queryDeviceTalkGuide(String deviceId, String account, boolean isOwner);

        void stopQueryDeviceTalkGuide(boolean isDestroy);

        void checkDeviceConfigure(String account , String deviceId);

        void setTimeForApDevice(String deviceId, long time, String model);

        void startTalkBubbleTask();

        void stopTalkBubbleTask();

        void sendRecordEventTracking(String path);

        void updateFileToMediaStore(String account, String path, String mediaType);

        void startLpCameraPlayBackTask(String deviceId, int playType);

        void stopLpCameraPlayBackTask();

        void startLpCameraShortLinkTask(String deviceId);

        void stopLpCameraShortLinkTask();

        void getApDeviceUpgradeInfo(String model, String version);

        void getFlashLight(String deviceId);

        void setFlashLight(String deviceId, boolean on);

        boolean getIsDeviceDetectionOn();

        void checkBeforeLoadDeviceSdCardRecordList(String deviceId, long start, boolean isLpDevice, String taskId, boolean isShortLinkDevice);

    }

}
