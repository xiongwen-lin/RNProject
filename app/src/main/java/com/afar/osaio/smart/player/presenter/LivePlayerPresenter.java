package com.afar.osaio.smart.player.presenter;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.afar.osaio.bean.PlaybackCloudData;
import com.afar.osaio.smart.player.component.DeviceCmdComponent;
import com.afar.osaio.smart.player.contract.PlayContract;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.nooie.common.utils.file.MediaStoreUtil;
import com.nooie.common.utils.json.GsonHelper;
import com.nooie.common.utils.tool.RxUtil;
import com.nooie.data.EventDictionary;
import com.nooie.eventtracking.EventTrackingApi;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.cache.DeviceConfigureCache;
import com.nooie.sdk.db.dao.DeviceConfigureService;
import com.afar.osaio.smart.db.dao.DeviceGuideService;
import com.nooie.common.base.GlobalData;
import com.nooie.common.utils.file.FileUtil;
import com.nooie.sdk.db.dao.DeviceHardVersionService;
import com.nooie.sdk.db.entity.DeviceConfigureEntity;
import com.nooie.sdk.db.entity.DeviceGuideEntity;
import com.afar.osaio.smart.device.bean.DeviceInfo;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.api.network.base.bean.entity.DeviceMessage;
import com.nooie.sdk.api.network.base.bean.entity.aws.AwsFileInfo;
import com.nooie.sdk.api.network.base.bean.entity.aws.AwsFileListResult;
import com.nooie.sdk.api.network.cloud.CloudService;
import com.nooie.sdk.api.network.message.MessageService;
import com.nooie.sdk.db.entity.DeviceHardVersionEntity;
import com.nooie.sdk.device.bean.AlarmSoundRequest;
import com.nooie.sdk.device.bean.MotionDetectLevel;
import com.nooie.sdk.device.bean.PirStateV2;
import com.nooie.sdk.device.bean.RecordFragment;
import com.nooie.sdk.device.listener.OnMotionDetectLevelListener;
import com.nooie.sdk.listener.OnGetFormatInfoListener;
import com.nooie.sdk.listener.OnGetPirStateV2Listener;
import com.nooie.sdk.listener.OnGetRecDatesListener;
import com.nooie.sdk.listener.OnGetSdcardRecordListener;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.bean.CloudFileBean;
import com.afar.osaio.smart.device.bean.CloudRecordInfo;
import com.afar.osaio.smart.device.bean.ListDeviceItem;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;
import com.nooie.sdk.processor.device.DeviceApi;
import com.scenery7f.timeaxis.model.RecordType;
import com.nooie.sdk.db.dao.DeviceCacheService;
import com.nooie.sdk.db.entity.DeviceEntity;
import com.afar.osaio.smart.device.helper.NooieCloudHelper;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.bean.DataEffect;
import com.nooie.common.bean.DataEffectCache;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.AppVersionResult;
import com.nooie.sdk.api.network.base.bean.entity.DeviceUpdateStatusResult;
import com.nooie.sdk.api.network.base.bean.entity.PackInfoResult;
import com.nooie.sdk.api.network.device.DeviceService;
import com.nooie.sdk.api.network.pack.PackService;
import com.nooie.sdk.api.network.setting.SettingService;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.device.bean.FormatInfo;
import com.nooie.sdk.device.bean.SpeakerInfo;
import com.nooie.sdk.device.listener.OnSwitchStateListener;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.listener.OnGetSpeakerInfoListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * LivePlayerPresenter
 *
 * @author Administrator
 * @date 2019/4/30
 */
public class LivePlayerPresenter implements PlayContract.Presenter {

    private PlayContract.View mPlayerView;
    private Subscription mLpCameraPlayTask;
    private Subscription mLpCameraPlayBackTask;
    private Subscription mLpCameraShortLinkTask;
    private boolean mIsDeviceDetectionOn = true;
    private DeviceCmdComponent mDeviceCmdComponent = null;

    public LivePlayerPresenter(PlayContract.View view) {
        mPlayerView = view;
        mPlayerView.setPresenter(this);
        setupDataEffectCache();
        mDeviceCmdComponent = new DeviceCmdComponent();
    }

    @Override
    public void destroy() {
        if (mPlayerView != null) {
            mPlayerView.setPresenter(null);
            mPlayerView = null;
        }
    }

    private static final String DE_KEY_CLOUD = "cloud";
    private static final String DE_KEY_SD = "sd";
    private DataEffectCache mDataEffectCache;
    private void setupDataEffectCache() {
        mDataEffectCache = new DataEffectCache();
    }

    @Override
    public void resetDataEffectCache() {
        if (mDataEffectCache != null) {
            mDataEffectCache.clear();
        }
    }

    private void updateCloudDe(boolean isOpenCloud) {
        DataEffect<Boolean> cloudDe = new DataEffect<>();
        cloudDe.setKey(DE_KEY_CLOUD);
        cloudDe.setValue(isOpenCloud);
        cloudDe.setEffective(true);
        mDataEffectCache.put(cloudDe.getKey(), cloudDe);
    }

    private void updateSdDe(boolean isHasSD) {
        DataEffect<Boolean> sdDe = new DataEffect<>();
        sdDe.setKey(DE_KEY_SD);
        sdDe.setValue(isHasSD);
        sdDe.setEffective(true);
        mDataEffectCache.put(sdDe.getKey(), sdDe);
    }

    @Override
    public void getDeviceStorageState(String user, final String deviceId, final boolean isRequsetSd, boolean isSubDevice, int connectionMode, int bindType, boolean isShortLinkDevice) {
        if (connectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            getDeviceSdCardState(user, deviceId, false, ApiConstant.CLOUD_STATE_UNSUBSCRIBE, true, isSubDevice, isShortLinkDevice);
            return;
        }
        getStorageInfoFromConfigure(deviceId, isRequsetSd, isShortLinkDevice);
        PackService.getService().getPackInfo(deviceId, bindType)
                .flatMap(new Func1<BaseResponse<PackInfoResult>, Observable<BaseResponse<PackInfoResult>>>() {
                    @Override
                    public Observable<BaseResponse<PackInfoResult>> call(BaseResponse<PackInfoResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            DeviceApi.getInstance().updateConfigurePackInfo(false, user, deviceId, response.getData());
                        } else if (response != null && response.getCode() == StateCode.DEVICE_UNSUBSCRIBE_CLOUD.code) {
                            DeviceApi.getInstance().updateConfigurePackInfo(false, user, deviceId, NooieCloudHelper.createPackInfoResult());
                        }
                        return Observable.just(response);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<PackInfoResult>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mPlayerView != null) {
                            boolean isOpenCloud = mDataEffectCache != null && mDataEffectCache.isDataEffective(DE_KEY_CLOUD) ? ((DataEffect<Boolean>)mDataEffectCache.get(DE_KEY_CLOUD)).getValue() : false;
                            getDeviceSdCardState(user, deviceId, isOpenCloud, ApiConstant.CLOUD_STATE_UNSUBSCRIBE, isRequsetSd, isSubDevice, isShortLinkDevice);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<PackInfoResult> response) {
                        boolean isOpenCloud = mDataEffectCache != null && mDataEffectCache.isDataEffective(DE_KEY_CLOUD) ? ((DataEffect<Boolean>)mDataEffectCache.get(DE_KEY_CLOUD)).getValue() : false;
                        int status = ApiConstant.CLOUD_STATE_UNSUBSCRIBE;
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            DeviceInfo deviceInfo = NooieDeviceHelper.getDeviceInfoById(deviceId);
                            float deviceTimezone = deviceInfo != null && deviceInfo.getNooieDevice() != null ? deviceInfo.getNooieDevice().getZone() : CountryUtil.getCurrentTimeZone();
                            isOpenCloud = NooieCloudHelper.isOpenCloud(response.getData().getEnd_time(), deviceTimezone);
                            updateCloudDe(isOpenCloud);
                            if (mPlayerView != null) {
                                mPlayerView.onLoadPackInfoSuccess(response.getData());
                            }
                            status = response.getData().getStatus();
                        } else if (response != null && response.getCode() == StateCode.DEVICE_UNSUBSCRIBE_CLOUD.code) {
                        }

                        boolean isHasSD = mDataEffectCache != null && mDataEffectCache.isDataEffective(DE_KEY_SD) ? ((DataEffect<Boolean>)mDataEffectCache.get(DE_KEY_SD)).getValue() : false;
                        if (mPlayerView != null) {
                            mPlayerView.notifyGetDeviceStorageState(isOpenCloud, isHasSD, status);
                        }
                        getDeviceSdCardState(user, deviceId, isOpenCloud, status, isRequsetSd, isSubDevice, isShortLinkDevice);
                    }
                });
    }

    @Override
    public void getDeviceFormatInfo(String user, String deviceId, final boolean isOpenCloud, int status, boolean isSubDevice, boolean isShortLinkDevice) {
        if (mDeviceCmdComponent != null) {
            mDeviceCmdComponent.getFormatInfo(deviceId, false, new OnGetFormatInfoListener() {
                @Override
                public void onGetFormatInfo(int code, FormatInfo formatInfo) {
                    dealGetFormatInfo(code, formatInfo, isOpenCloud, status, deviceId, isSubDevice);
                }
            });
        }

        getDetectionSetting(deviceId, isOpenCloud);
    }

    private void getDeviceSdCardState(String user, String deviceId, final boolean isOpenCloud, int status, boolean isRequest, boolean isSubDevice, boolean isShortLinkDevice) {
        if (!isRequest) {
            if (mPlayerView != null) {
                mPlayerView.onLoadStorageResult(0);
                mPlayerView.notifyGetDeviceStorageState(isOpenCloud, false, status);
            }
            return;
        }

        if (isShortLinkDevice) {
            if (mPlayerView != null) {
                mPlayerView.onLoadStorageResult(0);
                mPlayerView.onRequestShortLinkDeviceFormatInfo(user, deviceId, isOpenCloud, status, isSubDevice, isShortLinkDevice);
            }
            return;
        }

        DeviceCmdApi.getInstance().getFormatInfo(deviceId, new OnGetFormatInfoListener() {
            @Override
            public void onGetFormatInfo(int code, FormatInfo formatInfo) {
                dealGetFormatInfo(code, formatInfo, isOpenCloud, status, deviceId, isSubDevice);
            }
        });
        getDetectionSetting(deviceId, isOpenCloud);
    }

    private void dealGetFormatInfo(int code, FormatInfo formatInfo, boolean isOpenCloud, int status, String deviceId, boolean isSubDevice) {
        if (mPlayerView == null) {
            return;
        }
        if (code == SDKConstant.CODE_CACHE && formatInfo != null) {
            NooieLog.d("-->> LivePlayerPresenter dealGetFormatInfo cache free=" + formatInfo.getFree() + " total=" + formatInfo.getTotal() + " status=" + formatInfo.getFormatStatus() + " progress=" + formatInfo.getProgress());
            boolean isHasSdCard = NooieDeviceHelper.isHasSdCard(formatInfo.getFormatStatus());
            updateSdDe(isHasSdCard);
            mPlayerView.notifyGetDeviceStorageState(isOpenCloud, isHasSdCard, status);
        } else if (code == Constant.OK && formatInfo != null) {
            NooieLog.d("-->> LivePlayerPresenter dealGetFormatInfo free=" + formatInfo.getFree() + " total=" + formatInfo.getTotal() + " status=" + formatInfo.getFormatStatus() + " progress=" + formatInfo.getProgress());
            boolean isHasSdCard = NooieDeviceHelper.isHasSdCard(formatInfo.getFormatStatus());
            updateSdDe(isHasSdCard);
            mPlayerView.notifyGetDeviceStorageState(isOpenCloud, isHasSdCard, status);
            if (isHasSdCard) {
                loadSDCardRecentDay(deviceId, isSubDevice);
            }
            mPlayerView.onLoadStorageResult(0);
        } else {
            boolean isHasSdCard = mDataEffectCache != null && mDataEffectCache.isDataEffective(DE_KEY_SD) ? ((DataEffect<Boolean>)mDataEffectCache.get(DE_KEY_SD)).getValue() : false;
            mPlayerView.notifyGetDeviceStorageState(isOpenCloud, isHasSdCard, status);
            if (isHasSdCard) {
                loadSDCardRecentDay(deviceId, isSubDevice);
            }
            mPlayerView.onLoadStorageResult(0);
        }
    }

    private void getStorageInfoFromConfigure(String deviceId, boolean isRequsetSd, boolean isShortLinkDevice) {
        DeviceConfigureEntity configureEntity = DeviceConfigureCache.getInstance().getDeviceConfigure(deviceId);
        FormatInfo formatInfo = isRequsetSd && DeviceConfigureCache.getInstance().getDeviceCmdResult(deviceId) != null ? DeviceConfigureCache.getInstance().getDeviceCmdResult(deviceId).getFormatInfo() : null;
        if (configureEntity != null && mPlayerView != null) {
            float deviceTimezone = NooieDeviceHelper.getDeviceById(deviceId) != null ? NooieDeviceHelper.getDeviceById(deviceId).getZone() : CountryUtil.getCurrentTimeZone();
            boolean isOpenCloud = NooieCloudHelper.isOpenCloud(configureEntity.getEndTime(), deviceTimezone);
            boolean isHasSd = formatInfo != null ? NooieDeviceHelper.isHasSdCard(formatInfo.getFormatStatus()) : false;
            updateCloudDe(isOpenCloud);
            updateSdDe(isHasSd);
            mPlayerView.notifyGetDeviceStorageState(isOpenCloud, isHasSd, configureEntity.getStatus());
            if (isOpenCloud) {
                mPlayerView.onLoadPackInfoSuccess(NooieCloudHelper.createPackInfoResult(configureEntity));
            }
        }
    }

    @Override
    public void getStorageInfoByAp(String user, String deviceId, boolean isRequest, boolean isSubDevice) {
        if (!isRequest) {
            if (mPlayerView != null) {
                mPlayerView.onLoadStorageResult(0);
                mPlayerView.notifyGetDeviceStorageState(false, false, ApiConstant.CLOUD_STATE_UNSUBSCRIBE);
            }
            return;
        }

        tryGetFormatInfo(deviceId);
    }

    private void tryGetFormatInfo(String deviceId) {
        DeviceCmdApi.getInstance().getFormatInfo(deviceId, new OnGetFormatInfoListener() {
            @Override
            public void onGetFormatInfo(int code, FormatInfo info) {
                boolean isHasSdCard = false;
                if (code == SDKConstant.CODE_CACHE) {
                    if (info != null && mPlayerView != null) {
                        NooieLog.d("-->> LivePlayerPresenter getDeviceSdCardState cache free=" + info.getFree() + " total=" + info.getTotal() + " status=" + info.getFormatStatus() + " progress=" + info.getProgress());
                        isHasSdCard = NooieDeviceHelper.isHasSdCard(info.getFormatStatus());
                        mPlayerView.notifyGetDeviceStorageState(false, isHasSdCard, ApiConstant.CLOUD_STATE_UNSUBSCRIBE);
                    }
                    return;
                }

                if (code == Constant.OK && info != null) {
                    NooieLog.d("-->> LivePlayerPresenter getDeviceSdCardState free=" + info.getFree() + " total=" + info.getTotal() + " status=" + info.getFormatStatus() + " progress=" + info.getProgress());
                    isHasSdCard = NooieDeviceHelper.isHasSdCard(info.getFormatStatus());
                    updateSdDe(isHasSdCard);
                } else {
                    if (mDataEffectCache != null && mDataEffectCache.isDataEffective(DE_KEY_SD)) {
                        isHasSdCard = ((DataEffect<Boolean>)mDataEffectCache.get(DE_KEY_SD)).getValue();
                    } else if (DeviceConfigureCache.getInstance().getDeviceCmdResult(deviceId) != null && DeviceConfigureCache.getInstance().getDeviceCmdResult(deviceId).getFormatInfo() != null) {
                        isHasSdCard = NooieDeviceHelper.isHasSdCard(DeviceConfigureCache.getInstance().getDeviceCmdResult(deviceId).getFormatInfo().getFormatStatus());
                    }
                }
                if (mPlayerView != null) {
                    mPlayerView.notifyGetDeviceStorageState(false, isHasSdCard, ApiConstant.CLOUD_STATE_UNSUBSCRIBE);
                }
                if (isHasSdCard) {
                    loadSDCardRecentDay(deviceId, false);
                }
                if (mPlayerView != null) {
                    mPlayerView.onLoadStorageResult(0);
                }
            }
        });
    }

    @Override
    public void loadSDCardRecentDay(String deviceId, boolean isSubDevice) {
        DeviceCmdApi.getInstance().getSDCardRecDay(deviceId, new OnGetRecDatesListener() {
            @Override
            public void onRecDates(int code, int[] list, int today) {
                if ((code == SDKConstant.CODE_CACHE || code == Constant.OK) && mPlayerView != null) {
                    mPlayerView.onLoadSDCardRecentDaySuccess((list != null ? list : new int[]{}));
                } else if (mPlayerView != null) {
                    mPlayerView.onLoadSDCardRecentDaySuccess(null);
                }
            }
        });
    }

    @Override
    public void loadDeviceSdCardRecordList(final String deviceId, final long start, boolean isLpDevice, String taskId) {
        displayLoading(true);

        DeviceCmdApi.getInstance().getSDCardRecordList(deviceId, start, new OnGetSdcardRecordListener() {
            @Override
            public void onGetSdcardRecordInfo(int code, RecordFragment[] records) {
                List<CloudRecordInfo> result = new ArrayList<>();
                if (code == Constant.OK && records != null) {
                    int dayLen = 24 * 3600;
                    int dayEndTime = (int)(start + DateTimeUtil.DAY_SECOND_COUNT);
                    for (RecordFragment record : records) {
                        //NooieLog.d("-->> LivePlayerPresenter onGetSdcardRecordInfo start=" + record.getStart() + " len=" + record.getLen() + " startTime=" + DateTimeUtil.getTimeString(record.getStart() * 1000L, DateTimeUtil.PATTERN_YMD_HMS_1));
                        if (record.getStart() >= start && record.getStart() < dayEndTime && record.getLen() > 0 && record.getLen() < dayLen) {
                            //NooieLog.d("-->> LivePlayerPresenter onGetSdcardRecordInfo filter startIndexTime=" + start + " start=" + record.getStart() + " len=" + record.getLen() + " startTime=" + DateTimeUtil.getTimeString(record.getStart() * 1000L, DateTimeUtil.PATTERN_YMD_HMS_1));
                            int len = record.getLen();
                            if (record.getStart() + record.getLen() - dayEndTime > 0) {
                                len = dayEndTime - record.getStart();
                            }
                            CloudRecordInfo cloudRecordInfo = new CloudRecordInfo(deviceId, 1, record.getStart() * 1000L, len * 1000L, RecordType.PLAN_RECORD, true);
                            List<RecordType> recordTypes = new ArrayList<>();
                            recordTypes.add(RecordType.PLAN_RECORD);
                            cloudRecordInfo.setRecordTypes(recordTypes);
                            result.add(cloudRecordInfo);
                        }
                    }
                    if (mPlayerView != null) {
                        mPlayerView.onLoadDeviceSdCardRecordSuccess(result, taskId);
                    }
                } else if (code == Constant.ERROR && mPlayerView != null) {
                    mPlayerView.onLoadDeviceSdCardRecordSuccess(new ArrayList<CloudRecordInfo>(), taskId);
                }
            }
        });
    }

    private Subscription mLoadCloudRecordListTask;
    @Override
    public void loadDeviceCloudRecordList(final String deviceId, final long start, boolean isLpDevice, String account, int bindType, String taskId) {
        stopLoadDeviceMsgTask();
        stopLoadMoreDeviceMsgTask();
        stopLoadCloudRecordList();
        displayLoading(true);
        mLoadCloudRecordListTask = CloudService.getService().getFileListInApp(deviceId, GlobalData.getInstance().getUid(), DateTimeUtil.getUtcTimeString(start * 1000, "yyyyMMdd"), bindType)
                .flatMap(new Func1<BaseResponse<AwsFileListResult>, Observable<PlaybackCloudData>>() {
                    @Override
                    public Observable<PlaybackCloudData> call(BaseResponse<AwsFileListResult> response) {
                        PlaybackCloudData playbackCloudData = new PlaybackCloudData();
                        playbackCloudData.setResponse(response);
                        List<CloudRecordInfo> result = new ArrayList<>();
                        List<RecordFragment> recordFragments = new ArrayList<>();
                        String fileType = "";
                        String picType = "";
                        String filePrefix = "";
                        int expireDate = 7;
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null && response.getData().getFileinfo() != null) {
                            //NooieLog.d("-->> PlaybackPlayerPresenter onNext loadDeviceCloudRecordList currentUtcTime=" + DateTimeUtil.getUtcTimeString(DateTimeUtil.getUtcCalendar().getTimeInMillis(), DateTimeUtil.PATTERN_YMD_HMS_2) + " expireTime=" + DateTimeUtil.getUtcTimeString(response.getData().getExpirationtime() * 1000L, DateTimeUtil.PATTERN_YMD_HMS_2));
                            List<AwsFileInfo> fileInfos = response.getData().getFileinfo();
                            for (AwsFileInfo fileInfo : CollectionUtil.safeFor(fileInfos)) {

                                /*
                                int m = -1;
                                int s = -1;
                                int p = -1;
                                if (fileInfo.getE() != null) {
                                    m = fileInfo.getE().getM();
                                    s = fileInfo.getE().getS();
                                    p = fileInfo.getE().getP();
                                }
                                NooieLog.d("-->> LivePlayerPresenter onNext starttime=" + DateTimeUtil.getUtcTimeString((start + fileInfo.getS()) * 1000, DateTimeUtil.PATTERN_YMD_HMS_1) + " endtime=" + DateTimeUtil.getUtcTimeString((start + fileInfo.getS() + fileInfo.getL()) * 1000, DateTimeUtil.PATTERN_YMD_HMS_1) + " detection valid=" + (fileInfo != null && fileInfo.getE() != null && (NooieCloudHelper.isDetectionAvailable(fileInfo.getE().getM()) || NooieCloudHelper.isDetectionAvailable(fileInfo.getE().getS()))) + " m=" + m + " s=" + s + " p=" + p);
                                 */

                                if (fileInfo != null) {
                                    long startTime = fileInfo.getS() < 0 ? start * 1000L : (start + fileInfo.getS()) * 1000L;
                                    long timeLen = fileInfo.getS() < 0 ? (fileInfo.getL() + fileInfo.getS()) * 1000L : fileInfo.getL() * 1000L;
                                    if (timeLen <= 0) {
                                        continue;
                                    }
                                    //RecordType recordType = fileInfo.getE() != null && (fileInfo.getE().getM() != -1 || fileInfo.getE().getS() != -1) ? RecordType.ALERT_RECORD : RecordType.PLAN_RECORD;
                                    RecordType recordType = RecordType.PLAN_RECORD;
                                    if (fileInfo.getE() != null && NooieCloudHelper.isDetectionAvailable(fileInfo.getE().getM())) {
                                        recordType = RecordType.MOTION_RECORD;
                                    } else if (fileInfo.getE() != null && NooieCloudHelper.isDetectionAvailable(fileInfo.getE().getS())) {
                                        recordType = RecordType.SOUND_RECORD;
                                    } else if (fileInfo.getE() != null && NooieCloudHelper.isDetectionAvailable(fileInfo.getE().getP())) {
                                        recordType = RecordType.PIR_RECORD;
                                    }

                                    CloudRecordInfo cloudRecordInfo = new CloudRecordInfo(deviceId, 1, startTime, timeLen, recordType, true);
                                    //CloudRecordInfo cloudRecordInfo = new CloudRecordInfo(deviceId, 1, (start + fileInfo.getS()) * 1000, fileInfo.getL() * 1000, recordType, true);
                                    List<RecordType> recordTypes = new ArrayList<>();
                                    if (fileInfo.getE() != null && NooieCloudHelper.isDetectionAvailable(fileInfo.getE().getM())) {
                                        recordTypes.add(RecordType.MOTION_RECORD);
                                    }
                                    if (fileInfo.getE() != null && NooieCloudHelper.isDetectionAvailable(fileInfo.getE().getS())) {
                                        recordTypes.add(RecordType.SOUND_RECORD);
                                    }
                                    if (isLpDevice && fileInfo.getE() != null && NooieCloudHelper.isDetectionAvailable(fileInfo.getE().getP())) {
                                        recordTypes.add(RecordType.PIR_RECORD);
                                    }

                                    if (CollectionUtil.isEmpty(recordTypes)) {
                                        recordTypes.add(RecordType.PLAN_RECORD);
                                    }
                                    cloudRecordInfo.setRecordTypes(recordTypes);

                                    if (fileInfo.getE() != null && (NooieCloudHelper.isDetectionAvailable(fileInfo.getE().getM()) || NooieCloudHelper.isDetectionAvailable(fileInfo.getE().getS()) || NooieCloudHelper.isDetectionAvailable(fileInfo.getE().getP()))) {
                                        CloudFileBean cloudFileBean = new CloudFileBean();
                                        cloudFileBean.setDeviceId(response.getData().getDeviceid());
                                        cloudFileBean.setUserId(response.getData().getUserid());
                                        cloudFileBean.setFileType(response.getData().getFiletype());
                                        cloudFileBean.setPicType(response.getData().getPictype());
                                        cloudFileBean.setStartTime(fileInfo.getS());
                                        cloudFileBean.setExpiration(response.getData().getStorage());
                                        cloudFileBean.setBindType(bindType);
                                        cloudFileBean.setMotionDetectionTime(fileInfo.getE().getM());
                                        cloudFileBean.setSoundDetectionTime(fileInfo.getE().getS());
                                        cloudFileBean.setPirDetectionTime(fileInfo.getE().getP());
                                        cloudFileBean.setBaseTime(start * 1000L);
                                        cloudFileBean.setFileUrl(FileUtil.getDetectionThumbnailFilePath(NooieApplication.mCtx, account, deviceId, (start + fileInfo.getS())));
                                        cloudRecordInfo.setCloudFileBean(cloudFileBean);
                                    }

                                    result.add(cloudRecordInfo);

                                    RecordFragment recordFragment = new RecordFragment();
                                    recordFragment.setStart(fileInfo.getS());
                                    recordFragment.setLen(fileInfo.getL());
                                    recordFragments.add(recordFragment);
                                }
                                /* 根据某时间点调试对应数据
                                if (DateTimeUtil.getUtcTimeString((start + fileInfo.getS()) * 1000, DateTimeUtil.PATTERN_HMS).equalsIgnoreCase("15:40:30")) {
                                    break;
                                }
                                 */
                            }
                            fileType = response.getData().getFiletype() != null ? response.getData().getFiletype() : "";
                            picType = response.getData().getPictype() != null ? response.getData().getPictype() : "";
                            filePrefix = response.getData().getFilePrefix() != null ? response.getData().getFilePrefix() : "";
                            expireDate = response.getData().getStorage();
                            playbackCloudData.setResult(result);
                            playbackCloudData.setRecordFragments(recordFragments);
                            playbackCloudData.setFileType(fileType);
                            playbackCloudData.setPicType(picType);
                            playbackCloudData.setFilePrefix(filePrefix);
                            playbackCloudData.setExpireDate(expireDate);
                        }
                        return Observable.just(playbackCloudData);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<PlaybackCloudData>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mPlayerView != null) {
                            mPlayerView.onLoadDeviceCloudRecordSuccess(taskId, new ArrayList<CloudRecordInfo>(), new ArrayList<RecordFragment>(), "mp3", 7, "jpeg", "");
                        }
                    }

                    @Override
                    public void onNext(PlaybackCloudData playbackCloudData) {
                        //NooieLog.d("-->> LivePlayerPresenter loadDeviceCloudRecordList response totaltime=" + (System.currentTimeMillis()-requestStartTime));
                        List<CloudRecordInfo> result = new ArrayList<>();
                        List<RecordFragment> recordFragments = new ArrayList<>();
                        String fileType = "";
                        String picType = "";
                        String filePrefix = "";
                        int expireDate = 7;
                        BaseResponse<AwsFileListResult> response = null;
                        if (playbackCloudData != null && playbackCloudData.getResponse() != null && playbackCloudData.getResponse().getCode() == StateCode.SUCCESS.code) {
                            result = playbackCloudData.getResult();
                            recordFragments = playbackCloudData.getRecordFragments();
                            fileType = playbackCloudData.getFileType();
                            picType = playbackCloudData.getPicType();
                            filePrefix = playbackCloudData.getFilePrefix();
                            expireDate = playbackCloudData.getExpireDate();
                        }
                        if (mPlayerView != null) {
                            mPlayerView.onLoadDeviceCloudRecordSuccess(taskId, result, recordFragments, fileType, expireDate, picType, filePrefix);
                        }
                    }
                });
    }

    public void stopLoadCloudRecordList() {
        if (mLoadCloudRecordListTask != null && !mLoadCloudRecordListTask.isUnsubscribed()) {
            mLoadCloudRecordListTask.unsubscribe();
        }
        displayLoading(false);
    }

    @Override
    public void stopLoadRecordTask() {
        stopLoadDeviceMsgTask();
        stopLoadMoreDeviceMsgTask();
        stopLoadCloudRecordList();
    }

    private Subscription mLoadMoreDeviceMsgTask = null;
    private int mNextPage = 0;
    private int mLastLoadMoreTime = 0;

    /**
     *
     * @param account
     * @param uid
     * @param page 获取侦测消息必须
     * @param deviceId 获取侦测消息必须
     * @param time 获取侦测消息必须，获取起点的时间戳（毫秒）
     * @param direction 获取侦测消息必须
     * @param timeStamp 获取侦测消息必须，当天凌晨时间戳（s） + 24 * 60 * 60
     * @param type 获取侦测消息必须
     * @param rows 获取侦测消息必须
     * @param sort 获取侦测消息必须，正序"ASC"，逆序"DESC"
     * @param fileType
     * @param expiration
     * @param filePrefix
     */
    @Override
    public void loadMoreDeviceMsgByTime(String account, String uid, int page, String deviceId, long time, int direction, long timeStamp, int type, int rows, String sort, String fileType, int expiration, String filePrefix, int bindType) {
        if (page == 0) {
            stopLoadMoreDeviceMsgTask();
            mNextPage = (int)(time / 1000L);
        } else if (mNextPage == mLastLoadMoreTime) {
            return;
        }
        mLastLoadMoreTime = mNextPage;
        long zeroTime = direction == ApiConstant.DEVICE_MSG_DIRECTION_FORWARD ? (timeStamp / 1000L - DateTimeUtil.DAY_SECOND_COUNT) : (timeStamp / 1000L);
        mLoadMoreDeviceMsgTask = MessageService.getService().getDeviceMsgByTime(deviceId, mNextPage, direction, (int)zeroTime, type, rows, sort)
                .flatMap(new Func1<BaseResponse<List<DeviceMessage>>, Observable<List<CloudFileBean>>>() {
                    @Override
                    public Observable<List<CloudFileBean>> call(BaseResponse<List<DeviceMessage>> response) {
                        List<CloudFileBean> cloudFileBeans = new ArrayList<>();
                        if (response != null && response.getCode() == StateCode.SUCCESS.code) {
                            if (CollectionUtil.isNotEmpty(response.getData())) {
                                mNextPage = response.getData().get(response.getData().size() - 1).getDevice_time();
                                mNextPage = mNextPage > 1 ? mNextPage - 1 : mNextPage;
                            } else {
                                return Observable.just(cloudFileBeans);
                            }
                            for (DeviceMessage deviceMessage : CollectionUtil.safeFor(response.getData())) {
                                if (deviceMessage != null && deviceMessage.getType() != ApiConstant.DEVICE_MSG_TYPE_SD_LEAK) {
                                    //NooieLog.d("-->> LivePlayerPresenter loadMoreDeviceMsgByTime call deviceId=" + deviceMessage.getUuid() + " time=" + DateTimeUtil.getUtcTimeString(deviceMessage.getDevice_time() * 1000L, DateTimeUtil.PATTERN_YMD_HMS_2) + " files=" + deviceMessage.getFiles());
                                    //long baseTime = direction == ApiConstant.DEVICE_MSG_DIRECTION_FORWARD ? (timeStamp / 1000L) : (timeStamp / 1000L - DateTimeUtil.DAY_SECOND_COUNT);
                                    long baseTime = timeStamp / 1000L - DateTimeUtil.DAY_SECOND_COUNT;
                                    long startTime = deviceMessage.getDevice_time() - baseTime;
                                    //NooieLog.d("-->> LivePlayerPresenter loadMoreDeviceMsgByTime call deviceId=" + deviceId + " baseTime=" + baseTime + " startTime=" + startTime);
                                    CloudFileBean cloudFileBean = new CloudFileBean();
                                    cloudFileBean.setDeviceId(deviceMessage.getUuid());
                                    cloudFileBean.setUserId(uid);
                                    cloudFileBean.setFileType("");
                                    cloudFileBean.setPicType(fileType);
                                    cloudFileBean.setStartTime(startTime);
                                    cloudFileBean.setExpiration(expiration);
                                    cloudFileBean.setBindType(bindType);
                                    cloudFileBean.setMotionDetectionTime(deviceMessage.getType() == ApiConstant.DEVICE_MSG_TYPE_MOTION_DETECT ? 0 : -1);
                                    cloudFileBean.setSoundDetectionTime(deviceMessage.getType() == ApiConstant.DEVICE_MSG_TYPE_SOUND_DETECT ? 0 : -1);
                                    cloudFileBean.setPirDetectionTime(deviceMessage.getType() == ApiConstant.DEVICE_MSG_TYPE_PIR_DETECT ? 0 : -1);
                                    cloudFileBean.setBaseTime(baseTime * 1000L);
                                    cloudFileBean.setFileUrl(FileUtil.getDetectionThumbnailFilePath(NooieApplication.mCtx, account, deviceId, deviceMessage.getDevice_time()));
                                    cloudFileBean.setPreSignUrl(deviceMessage.getFiles());
                                    cloudFileBean.setRecordType(NooieDeviceHelper.convertRecordTypeByMsgType(deviceMessage.getType()).getValue());
                                    cloudFileBeans.add(cloudFileBean);
                                }
                            }
                        }
                        return Observable.just(cloudFileBeans);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<CloudFileBean>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mPlayerView != null) {
                            mPlayerView.onLoadDeviceMsgResult(ConstantValue.ERROR, ConstantValue.CLOUD_RECORD_REQUEST_MORE, account, uid, deviceId, CollectionUtil.emptyList(), fileType, expiration, filePrefix);
                        }
                    }

                    @Override
                    public void onNext(List<CloudFileBean> cloudFileBeans) {
                        if (mPlayerView != null) {
                            mPlayerView.onLoadDeviceMsgResult(ConstantValue.SUCCESS, ConstantValue.CLOUD_RECORD_REQUEST_MORE, account, uid, deviceId, cloudFileBeans, fileType, expiration, filePrefix);
                        }
                    }
                });
    }

    @Override
    public void stopLoadMoreDeviceMsgTask() {
        if (mLoadMoreDeviceMsgTask != null && !mLoadMoreDeviceMsgTask.isUnsubscribed()) {
            mLoadMoreDeviceMsgTask.unsubscribe();
            mLoadMoreDeviceMsgTask = null;
            mLastLoadMoreTime = 0;
        }
    }

    private Subscription mLoadDeviceMsgTask = null;
    @Override
    public void loadDeviceMsgByTime(String account, String uid, String deviceId, long time, int direction, long timeStamp, int type, int rows, String sort, String fileType, int expiration, String filePrefix, int bindType) {
        stopLoadDeviceMsgTask();
        long zeroTime = direction == ApiConstant.DEVICE_MSG_DIRECTION_FORWARD ? (timeStamp / 1000L - DateTimeUtil.DAY_SECOND_COUNT) : (timeStamp / 1000L);
        mLoadDeviceMsgTask = MessageService.getService().getDeviceMsgByTime(deviceId, (int)(time / 1000L), direction, (int)zeroTime, type, rows, sort)
                .flatMap(new Func1<BaseResponse<List<DeviceMessage>>, Observable<List<CloudFileBean>>>() {
                    @Override
                    public Observable<List<CloudFileBean>> call(BaseResponse<List<DeviceMessage>> response) {
                        List<CloudFileBean> cloudFileBeans = new ArrayList<>();
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && CollectionUtil.isNotEmpty(response.getData())) {
                            for (DeviceMessage deviceMessage : CollectionUtil.safeFor(response.getData())) {
                                if (deviceMessage != null && deviceMessage.getType() != ApiConstant.DEVICE_MSG_TYPE_SD_LEAK) {
                                    //NooieLog.d("-->> LivePlayerPresenter loadDeviceMsgByTime call deviceId=" + deviceMessage.getUuid() + " time=" + DateTimeUtil.getUtcTimeString(deviceMessage.getDevice_time() * 1000L, DateTimeUtil.PATTERN_YMD_HMS_2) + " files=" + deviceMessage.getFiles());
                                    //long baseTime = direction == ApiConstant.DEVICE_MSG_DIRECTION_FORWARD ? (timeStamp / 1000L) : (timeStamp / 1000L - DateTimeUtil.DAY_SECOND_COUNT);
                                    long baseTime = timeStamp / 1000L - DateTimeUtil.DAY_SECOND_COUNT;
                                    long startTime = deviceMessage.getDevice_time() - baseTime;
                                    //NooieLog.d("-->> LivePlayerPresenter loadDeviceMsgByTime call deviceId=" + deviceId + " baseTime=" + baseTime + " startTime=" + startTime);
                                    CloudFileBean cloudFileBean = new CloudFileBean();
                                    cloudFileBean.setDeviceId(deviceMessage.getUuid());
                                    cloudFileBean.setUserId(uid);
                                    cloudFileBean.setFileType("");
                                    cloudFileBean.setPicType(fileType);
                                    cloudFileBean.setStartTime(startTime);
                                    cloudFileBean.setExpiration(expiration);
                                    cloudFileBean.setBindType(bindType);
                                    cloudFileBean.setMotionDetectionTime(deviceMessage.getType() == ApiConstant.DEVICE_MSG_TYPE_MOTION_DETECT ? 1 : -1);
                                    cloudFileBean.setSoundDetectionTime(deviceMessage.getType() == ApiConstant.DEVICE_MSG_TYPE_SOUND_DETECT ? 1 : -1);
                                    cloudFileBean.setPirDetectionTime(deviceMessage.getType() == ApiConstant.DEVICE_MSG_TYPE_PIR_DETECT ? 1 : -1);
                                    cloudFileBean.setBaseTime(baseTime * 1000L);
                                    cloudFileBean.setFileUrl(FileUtil.getDetectionThumbnailFilePath(NooieApplication.mCtx, account, deviceId, deviceMessage.getDevice_time()));
                                    cloudFileBean.setPreSignUrl(deviceMessage.getFiles());
                                    cloudFileBean.setRecordType(NooieDeviceHelper.convertRecordTypeByMsgType(deviceMessage.getType()).getValue());
                                    cloudFileBeans.add(cloudFileBean);
                                }
                            }
                        }
                        return Observable.just(cloudFileBeans);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<CloudFileBean>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mPlayerView != null) {
                            mPlayerView.onLoadDeviceMsgResult(ConstantValue.ERROR, ConstantValue.CLOUD_RECORD_REQUEST_NORMAL, account, uid, deviceId, CollectionUtil.emptyList(), fileType, expiration, filePrefix);
                        }
                    }

                    @Override
                    public void onNext(List<CloudFileBean> cloudFileBeans) {
                        if (mPlayerView != null) {
                            mPlayerView.onLoadDeviceMsgResult(ConstantValue.SUCCESS, ConstantValue.CLOUD_RECORD_REQUEST_NORMAL, account, uid, deviceId, cloudFileBeans, fileType, expiration, filePrefix);
                        }
                    }
                });
    }

    @Override
    public void stopLoadDeviceMsgTask() {
        if (mLoadDeviceMsgTask != null && !mLoadDeviceMsgTask.isUnsubscribed()) {
            mLoadDeviceMsgTask.unsubscribe();
            mLoadDeviceMsgTask = null;
        }
    }

    private void displayLoading(boolean show) {
        if (mPlayerView != null) {
            mPlayerView.displayLoading(show);
        }
    }

    @Override
    public void loadFirmwareVersion(final String deviceId, final String model) {
        DeviceService.getService().getDeviceUpdateStatus(deviceId)
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<BaseResponse<DeviceUpdateStatusResult>, Observable<BaseResponse<AppVersionResult>>>() {
                    @Override
                    public Observable<BaseResponse<AppVersionResult>> call(BaseResponse<DeviceUpdateStatusResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null && response.getData().getType() == ApiConstant.DEVICE_UPDATE_TYPE_NORMAL) {
                            return SettingService.getService().getHardVersion(model);
                        }
                        return Observable.just(null);
                    }
                })
                .flatMap(new Func1<BaseResponse<AppVersionResult>, Observable<AppVersionResult>>() {
                    @Override
                    public Observable<AppVersionResult> call(BaseResponse<AppVersionResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            return getAppVersionObservable(deviceId, response.getData());
                        }
                        return Observable.just(null);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AppVersionResult>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mPlayerView != null) {
                            mPlayerView.onLoadFirmwareInfoFailed(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(AppVersionResult result) {
                        if (result != null && mPlayerView != null) {
                            mPlayerView.onLoadFirmwareInfoSuccess(result);
                        } else if (mPlayerView != null) {
                            mPlayerView.onLoadFirmwareInfoFailed("");
                        }
                    }
                });
    }

    private Observable<AppVersionResult> getAppVersionObservable(String deviceId, AppVersionResult result) {
        Observable<BaseResponse<BindDevice>> deviceInfoObservable = DeviceService.getService().getDeviceInfo(deviceId);
        return Observable.zip(Observable.just(result), deviceInfoObservable, new Func2<AppVersionResult, BaseResponse<BindDevice>, AppVersionResult>() {
            @Override
            public AppVersionResult call(AppVersionResult result, BaseResponse<BindDevice> response) {
                if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null && result != null) {
                    result.setCurrentVersionCode(response.getData().getVersion());
                }
                return result;
            }
        });
    }

    @Override
    public void queryNooieDeviceUpdateStatus(final String deviceId, final String account) {
        Observable.just(deviceId)
                .flatMap(new Func1<String, Observable<BaseResponse<DeviceUpdateStatusResult>>>() {
                    @Override
                    public Observable<BaseResponse<DeviceUpdateStatusResult>> call(String devId) {
                        DeviceEntity deviceEntity = DeviceCacheService.getInstance().getDevice(account, deviceId);
                        if (deviceEntity != null && deviceEntity.getUpgradeState() == ApiConstant.DEVICE_UPDATE_TYPE_NORMAL) {
                            BaseResponse<DeviceUpdateStatusResult> response = new BaseResponse<>();
                            response.setCode(StateCode.SUCCESS.code);
                            DeviceUpdateStatusResult result = new DeviceUpdateStatusResult();
                            result.setType(ApiConstant.DEVICE_UPDATE_TYPE_NORMAL);
                            response.setData(result);
                            return Observable.just(response);
                        }
                        return DeviceService.getService().getDeviceUpdateStatus(deviceId);
                    }
                })
                .flatMap(new Func1<BaseResponse<DeviceUpdateStatusResult>, Observable<BaseResponse<DeviceUpdateStatusResult>>>() {
                    @Override
                    public Observable<BaseResponse<DeviceUpdateStatusResult>> call(BaseResponse<DeviceUpdateStatusResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            int upgradeState = response.getData().getType() == ApiConstant.DEVICE_UPDATE_TYPE_NORMAL || response.getData().getType() == ApiConstant.DEVICE_UPDATE_TYPE_UPATE_FINISH ? ApiConstant.DEVICE_UPDATE_TYPE_NORMAL : ApiConstant.DEVICE_UPDATE_TYPE_DOWNLOAD_START;
                            //DeviceCacheService.getInstance().updateUpgradeState(account, deviceId, ListDeviceItem.DEVICE_PLATFORM_NOOIE, upgradeState);
                            DeviceApi.getInstance().updateDeviceUpgradeStatus(false, account, deviceId, ListDeviceItem.DEVICE_PLATFORM_NOOIE, upgradeState);
                        }
                        return Observable.just(response);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<DeviceUpdateStatusResult>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mPlayerView != null) {
                            mPlayerView.onQueryDeviceUpdateState(ApiConstant.DEVICE_UPDATE_TYPE_NORMAL);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<DeviceUpdateStatusResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            mPlayerView.onQueryDeviceUpdateState(response.getData().getType());
                        } else if (mPlayerView != null) {
                            mPlayerView.onQueryDeviceUpdateState(ApiConstant.DEVICE_UPDATE_TYPE_NORMAL);
                        }
                    }
                });
    }

    @Override
    public void setDeviceAlarmAudio(String deviceId, boolean on, int id, int time, int num, final OnActionResultListener listener) {
        AlarmSoundRequest request = new AlarmSoundRequest();
        request.open = on ? 1 : 0;
        request.type = id;
        request.dur = time;
        request.times = num;
        DeviceCmdApi.getInstance().setAlarmSound(deviceId, request, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                NooieLog.d("-->> LivePlayerPresenter setDeviceAlarmAudio code=" + code);
                if (listener != null) {
                    listener.onResult(code);
                }
            }
        });
    }

    @Override
    public void getDeviceAlarmAudio(String deviceId, final OnSwitchStateListener listener) {
        DeviceCmdApi.getInstance().getSpeakerInfo(deviceId, new OnGetSpeakerInfoListener() {
            @Override
            public void onSpeakerInfo(int code, SpeakerInfo info) {
                if (code == SDKConstant.CODE_CACHE) {
                    return;
                }
                if (code == Constant.OK && info != null && listener != null) {
                    NooieLog.d("-->> LivePlayerPresenter onSpeakerInfo code=" + code + " alarmAble=" + info.alarmSoundOpen + " talkAble=" + info.talkSoundOpen);
                    boolean enable = !info.alarmSoundOpen && !info.talkSoundOpen;
                    listener.onStateInfo(Constant.OK, enable);
                } else if (listener != null) {
                    listener.onStateInfo(Constant.ERROR, false);
                }
            }
        });
    }

    @Override
    public void startLpCameraPlayTask(String deviceId, int playType) {
        stopLpCameraPlayTask();
        mLpCameraPlayTask = Observable.just(deviceId)
                .delay(ConstantValue.LP_CAMERA_PLAY_LIMIT_TIME, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mPlayerView != null) {
                            mPlayerView.onLpCameraPlayFinish(ConstantValue.ERROR, deviceId, playType);
                        }
                    }

                    @Override
                    public void onNext(String deviceId) {
                        if (mPlayerView != null) {
                            mPlayerView.onLpCameraPlayFinish(ConstantValue.SUCCESS, deviceId, playType);
                        }
                    }
                });
    }

    @Override
    public void stopLpCameraPlayTask() {
        if (mLpCameraPlayTask != null && !mLpCameraPlayTask.isUnsubscribed()) {
            mLpCameraPlayTask.unsubscribe();
            mLpCameraPlayTask = null;
        }
    }

    @Override
    public boolean isLpCameraPlayTaskRunning() {
        return mLpCameraPlayTask != null && !mLpCameraPlayTask.isUnsubscribed();
    }

    @Override
    public void queryDeviceGuide(String deviceId, String account, boolean isOwner) {
        Observable.just(deviceId)
                .flatMap(new Func1<String, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(String deviceId) {
                        DeviceGuideEntity deviceGuideEntity = DeviceGuideService.getInstance().getDeviceGuide(deviceId, account);
                        if ((deviceGuideEntity == null || deviceGuideEntity.getUsed() != ConstantValue.DEVICE_GUIDE_USED) && isOwner) {
                            Bundle data = new Bundle();
                            data.putInt(DeviceGuideService.KEY_USED, ConstantValue.DEVICE_GUIDE_USED);
                            DeviceGuideService.getInstance().updateDeviceGuide(deviceId, account, data);
                            return Observable.just(true);
                        }
                        return Observable.just(false);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mPlayerView != null) {
                            mPlayerView.onQueryDeviceGuide(ConstantValue.ERROR, false);
                        }
                    }

                    @Override
                    public void onNext(Boolean isShowGuide) {
                        if (mPlayerView != null) {
                            mPlayerView.onQueryDeviceGuide(ConstantValue.SUCCESS, isShowGuide);
                        }
                    }
                });
    }

    private DeviceGuideEntity mDeviceGuideEntity = null;
    private Subscription mQueryDeviceTalkGuideTask = null;
    @Override
    public void queryDeviceTalkGuide(String deviceId, String account, boolean isOwner) {
        stopQueryDeviceTalkGuide(false);
        if (!isOwner || (mDeviceGuideEntity != null && mDeviceGuideEntity.getTalkUsed() == ConstantValue.DEVICE_GUIDE_USED)) {
            if (mPlayerView != null) {
                mPlayerView.onQueryDeviceTalkGuide(ConstantValue.SUCCESS, false);
            }
            return;
        }
        mQueryDeviceTalkGuideTask = Observable.just(deviceId)
                .flatMap(new Func1<String, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(String deviceId) {
                        mDeviceGuideEntity = DeviceGuideService.getInstance().getDeviceGuide(deviceId, account);
                        boolean isShowGuide = mDeviceGuideEntity == null || mDeviceGuideEntity.getTalkUsed() != ConstantValue.DEVICE_GUIDE_USED;
                        if (mDeviceGuideEntity == null) {
                            mDeviceGuideEntity = new DeviceGuideEntity();
                            mDeviceGuideEntity.setUuid(deviceId);
                            mDeviceGuideEntity.setAccount(account);
                            mDeviceGuideEntity.setTalkUsed(ConstantValue.DEVICE_GUIDE_USED);
                        }
                        Bundle data = new Bundle();
                        data.putInt(DeviceGuideService.KEY_TALK_USED, ConstantValue.DEVICE_GUIDE_USED);
                        DeviceGuideService.getInstance().updateDeviceGuide(deviceId, account, data);
                        return Observable.just(isShowGuide);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mPlayerView != null) {
                            mPlayerView.onQueryDeviceTalkGuide(ConstantValue.ERROR, false);
                        }
                    }

                    @Override
                    public void onNext(Boolean isShowGuide) {
                        if (mPlayerView != null) {
                            mPlayerView.onQueryDeviceTalkGuide(ConstantValue.SUCCESS, isShowGuide);
                        }
                    }
                });
    }

    @Override
    public void stopQueryDeviceTalkGuide(boolean isDestroy) {
        if (isDestroy) {
            mDeviceGuideEntity = null;
        }
        if (mQueryDeviceTalkGuideTask != null && !mQueryDeviceTalkGuideTask.isUnsubscribed()) {
            mQueryDeviceTalkGuideTask.unsubscribe();
            mQueryDeviceTalkGuideTask = null;
        }
    }

    @Override
    public void checkDeviceConfigure(String account , String deviceId) {
        NooieLog.d("-->> LivePlayerPresenter checkDeviceConfigure isDayLight=" + CountryUtil.isCurrentDaylight());
        if (!CountryUtil.isCurrentDaylight()) {
            return;
        }
        Observable.just(DeviceConfigureCache.getInstance().getDeviceConfigure(deviceId))
                .flatMap(new Func1<DeviceConfigureEntity, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(DeviceConfigureEntity configureEntity) {
                        boolean isActive = false;
                        if (configureEntity != null && configureEntity.isActive()) {
                            isActive = true;
                        } else {
                            DeviceConfigureEntity deviceConfigureEntity = DeviceConfigureService.getInstance().getDeviceConfigure(account, deviceId);
                            isActive = deviceConfigureEntity != null && deviceConfigureEntity.isActive();
                        }
                        NooieLog.d("-->> LivePlayerPresenter checkDeviceConfigure call isActive=" + isActive);
                        return Observable.just(isActive);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Boolean isActive) {
                        if (isActive) {
                            return;
                        }
                        setSyncTime(account, deviceId, 1);
                    }
                });
    }

    private static final int SYNC_TIME_COUNT = 2;
    private int mSyncTimeCount = 1;
    private void setSyncTime(String account, String deviceId, int mode) {
        DeviceCmdApi.getInstance().setSyncTime(deviceId, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                if (code == Constant.OK) {
                    RxUtil.wrapperObservable("LivePlayerPresenter setSyncTime", DeviceApi.getInstance().updateConfigureActive(true, account, deviceId, true));
                } else if (mSyncTimeCount < SYNC_TIME_COUNT) {
                    mSyncTimeCount++;
                    setSyncTime(account, deviceId, mode);
                }
            }
        });
    }

    @Override
    public void setTimeForApDevice(String deviceId, long time, String model) {
        ApHelper.getInstance().setupApDeviceTime(deviceId, model, time, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                NooieLog.d("-->> LivePlayerPresenter setTimeForApDevice onResult code=" + code);
                if (code == Constant.OK) {
                    //loadSDCardRecentDay(deviceId, false);
                }
            }
        });
    }

    private static final int SHOW_TALK_BUBBLE_TIME = 3 * 1000;
    private Subscription mStartTalkBubbleTask = null;
    @Override
    public void startTalkBubbleTask() {
        stopTalkBubbleTask();
        mStartTalkBubbleTask = Observable.just(1)
                .delay(SHOW_TALK_BUBBLE_TIME, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mPlayerView != null) {
                            mPlayerView.onShowTalkBubble(ConstantValue.ERROR);
                        }
                    }

                    @Override
                    public void onNext(Integer integer) {
                        if (mPlayerView != null) {
                            mPlayerView.onShowTalkBubble(ConstantValue.SUCCESS);
                        }
                    }
                });
    }

    @Override
    public void stopTalkBubbleTask() {
        if (mStartTalkBubbleTask != null && !mStartTalkBubbleTask.isUnsubscribed()) {
            mStartTalkBubbleTask.unsubscribe();
        }
    }

    @Override
    public void sendRecordEventTracking(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        Observable.just(path)
                .flatMap(new Func1<String, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(String s) {
                        try {
                            File recordFile = new File(path);
                            if (recordFile.exists() && recordFile.isFile()) {
                                ArrayMap<String, Object> externalMap = new ArrayMap<>();
                                externalMap.put(EventDictionary.EXTERNAL_KEY_FILE_SIZE, FileUtil.getFormatSize(recordFile.length()));
                                //NooieLog.d("-->> debug LivePlayerPresenter sendRecordEventTracking: file size=" + FileUtil.getFormatSize(recordFile.length()) + " external=" + GsonHelper.convertToJson(externalMap));
                                EventTrackingApi.getInstance().trackNormalEvent(EventDictionary.EVENT_ID_CLICK_RECORD, GsonHelper.convertToJson(externalMap));
                            }
                        } catch (Exception e) {
                            NooieLog.printStackTrace(e);
                        }
                        return Observable.just(true);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Boolean result) {
                    }
                });
    }

    @Override
    public void updateFileToMediaStore(String account, String path, String mediaType) {
        Observable.just(mediaType)
                .flatMap(new Func1<String, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(String type) {
                        if (TextUtils.isEmpty(type) || !new File(path).exists()) {
                            return Observable.just(true);
                        }
                        StringBuilder relativeSubFolderSb = new StringBuilder();
                        relativeSubFolderSb.append(ConstantValue.APP_TAG_PREFIX).append(File.separator).append(account);
                        if (MediaStoreUtil.MEDIA_TYPE_IMAGE_JPEG.equalsIgnoreCase(type)) {
                            relativeSubFolderSb.append(File.separator).append(FileUtil.SnapshotDir);
                            MediaStoreUtil.createMediaStoreFileForImage(NooieApplication.mCtx, path, relativeSubFolderSb.toString(), null, null, type, null);
                        } else if (MediaStoreUtil.MEDIA_TYPE_VIDEO_MP4.equalsIgnoreCase(type)) {
                            relativeSubFolderSb.append(File.separator).append(FileUtil.VideoDir);
                            MediaStoreUtil.createMediaStoreFileForVideo(NooieApplication.mCtx, path, relativeSubFolderSb.toString(), null, null, type, null);
                        }
                        return Observable.just(true);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Boolean result) {
                    }
                });
    }

    @Override
    public void startLpCameraPlayBackTask(String deviceId, int playType) {
        stopLpCameraPlayBackTask();
        mLpCameraPlayBackTask = Observable.just(deviceId)
                .delay(ConstantValue.LP_CAMERA_PLAY_LIMIT_TIME, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mPlayerView != null) {
                            mPlayerView.onLpDeviceCountDownTask(SDKConstant.ERROR, ConstantValue.LP_DEVICE_COUNTDOWN_TYPE_PLAYBACK, deviceId);
                        }
                    }

                    @Override
                    public void onNext(String deviceId) {
                        if (mPlayerView != null) {
                            mPlayerView.onLpDeviceCountDownTask(SDKConstant.SUCCESS, ConstantValue.LP_DEVICE_COUNTDOWN_TYPE_PLAYBACK, deviceId);
                        }
                    }
                });
    }

    @Override
    public void stopLpCameraPlayBackTask() {
        if (mLpCameraPlayBackTask != null && !mLpCameraPlayBackTask.isUnsubscribed()) {
            mLpCameraPlayBackTask.unsubscribe();
            mLpCameraPlayBackTask = null;
        }
    }

    @Override
    public void startLpCameraShortLinkTask(String deviceId) {
        stopLpCameraShortLinkTask();
        mLpCameraShortLinkTask = Observable.just(deviceId)
                .delay(ConstantValue.LP_CAMERA_PLAY_LIMIT_TIME, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mPlayerView != null) {
                            mPlayerView.onLpDeviceCountDownTask(SDKConstant.ERROR, ConstantValue.LP_DEVICE_COUNTDOWN_TYPE_SHORT_LINK, deviceId);
                        }
                    }

                    @Override
                    public void onNext(String deviceId) {
                        if (mPlayerView != null) {
                            mPlayerView.onLpDeviceCountDownTask(SDKConstant.SUCCESS, ConstantValue.LP_DEVICE_COUNTDOWN_TYPE_SHORT_LINK, deviceId);
                        }
                    }
                });
    }

    @Override
    public void stopLpCameraShortLinkTask() {
        if (mLpCameraShortLinkTask != null && !mLpCameraShortLinkTask.isUnsubscribed()) {
            mLpCameraShortLinkTask.unsubscribe();
            mLpCameraShortLinkTask = null;
        }
    }

    Subscription mGetApDeviceUpgradeInfoTask = null;
    @Override
    public void getApDeviceUpgradeInfo(String model, String version) {
        mGetApDeviceUpgradeInfoTask = Observable.just(model)
                .flatMap(new Func1<String, Observable<DeviceHardVersionEntity>>() {
                    @Override
                    public Observable<DeviceHardVersionEntity> call(String model) {
                        List<DeviceHardVersionEntity> deviceHardVersions = DeviceHardVersionService.getInstance().getDeviceHardVersions(model);
                        if (CollectionUtil.isEmpty(deviceHardVersions)) {
                            return Observable.just(null);
                        }
                        DeviceHardVersionEntity deviceHardVersionEntity = deviceHardVersions.get(0);
                        int size = deviceHardVersions.size();
                        if (size == 1) {
                            return Observable.just(deviceHardVersionEntity);
                        }
                        for (int i = 1; i < size; i++) {
                            boolean isNewestVersion = !TextUtils.isEmpty(deviceHardVersionEntity.getVersionCode()) && !TextUtils.isEmpty(deviceHardVersions.get(i).getVersionCode())
                                    && NooieDeviceHelper.compareVersion(deviceHardVersions.get(i).getVersionCode(), deviceHardVersionEntity.getVersionCode()) > 0;
                            if (isNewestVersion) {
                                deviceHardVersionEntity = deviceHardVersions.get(i);
                            }
                        }
                        return Observable.just(deviceHardVersionEntity);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<DeviceHardVersionEntity>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mPlayerView != null) {
                            mPlayerView.onGetApDeviceUpgradeInfo(SDKConstant.ERROR, null, "");
                        }
                    }

                    @Override
                    public void onNext(DeviceHardVersionEntity result) {
                        if (mPlayerView != null) {
                            mPlayerView.onGetApDeviceUpgradeInfo(SDKConstant.SUCCESS, result, version);
                        }
                    }
                });
    }

    @Override
    public void getFlashLight(String deviceId) {
        DeviceCmdApi.getInstance().getLight(deviceId, new OnSwitchStateListener() {
            @Override
            public void onStateInfo(int code, boolean on) {
                if (mPlayerView != null) {
                    mPlayerView.onGetFlashLight((code == Constant.OK ? SDKConstant.SUCCESS : SDKConstant.ERROR), on);
                }
            }
        });
    }

    @Override
    public void setFlashLight(String deviceId, boolean on) {
        DeviceCmdApi.getInstance().setLight(deviceId, on, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                if (mPlayerView != null) {
                    mPlayerView.onSetFlashLight((code == Constant.OK ? SDKConstant.SUCCESS : SDKConstant.ERROR));
                }
            }
        });
    }

    @Override
    public boolean getIsDeviceDetectionOn() {
        return mIsDeviceDetectionOn;
    }

    @Override
    public void checkBeforeLoadDeviceSdCardRecordList(String deviceId, long start, boolean isLpDevice, String taskId, boolean isShortLinkDevice) {
        if (!isShortLinkDevice) {
            loadDeviceSdCardRecordList(deviceId, start, isLpDevice, taskId);
            return;
        }
        if (mDeviceCmdComponent != null) {
            mDeviceCmdComponent.getFormatInfo(deviceId, false, new OnGetFormatInfoListener() {
                @Override
                public void onGetFormatInfo(int code, FormatInfo formatInfo) {
                    if (code == SDKConstant.CODE_CACHE) {
                        return;
                    }
                    loadDeviceSdCardRecordList(deviceId, start, isLpDevice, taskId);
                }
            });
        }
    }

    private void getDetectionSetting(String deviceId, boolean isOpenCloud) {
        BindDevice device = NooieDeviceHelper.getDeviceById(deviceId);
        boolean isNotRequest = !isOpenCloud || device == null || TextUtils.isEmpty(device.getType()) || device.getOnline() != ApiConstant.BIND_TYPE_OWNER
                || device.getOnline() != ApiConstant.ONLINE_STATUS_ON;
        if (isNotRequest) {
            return;
        }
        if (NooieDeviceHelper.isLpDevice(device.getType())) {
            DeviceCmdApi.getInstance().getPir(deviceId, new OnGetPirStateV2Listener() {
                @Override
                public void onGetPirStateV2(int code, PirStateV2 pirStateV2) {
                    if (code == Constant.OK) {
                        mIsDeviceDetectionOn = pirStateV2 != null && pirStateV2.enable;
                    }
                }
            });
        } else {
            DeviceCmdApi.getInstance().getMotionDetectLevel(deviceId, new OnMotionDetectLevelListener() {
                @Override
                public void onMotionDetectInfo(int code, MotionDetectLevel motionDetectLevel) {
                    if (code == Constant.OK) {
                        mIsDeviceDetectionOn = motionDetectLevel != null && motionDetectLevel != MotionDetectLevel.MOTION_DETECT_CLOSE;
                    }
                }
            });
        }
    }
}
