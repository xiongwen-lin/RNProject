package com.afar.osaio.message.presenter;

import android.content.res.Resources;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.player.component.DeviceCmdComponent;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.cache.DeviceConfigureCache;
import com.nooie.sdk.db.entity.DeviceConfigureEntity;
import com.afar.osaio.smart.device.bean.DeviceInfo;
import com.afar.osaio.message.view.IDeviceMessageView;
import com.afar.osaio.smart.device.helper.NooieCloudHelper;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.common.bean.DataEffect;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.collection.ConvertUtil;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.api.network.base.bean.entity.DeviceMessage;
import com.nooie.sdk.api.network.base.bean.entity.PackInfoResult;
import com.nooie.sdk.api.network.device.DeviceService;
import com.nooie.sdk.api.network.message.MessageService;
import com.nooie.sdk.api.network.pack.PackService;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.device.bean.FormatInfo;
import com.nooie.sdk.listener.OnGetFormatInfoListener;
import com.nooie.sdk.processor.device.DeviceApi;

import java.lang.ref.WeakReference;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by victor on 2018/7/3
 * Email is victor.qiao.0604@gmail.com
 */
public class DeviceMsgPresenterImpl implements IDeviceMsgPresenter {
    private WeakReference<IDeviceMessageView> viewInterface;
    private boolean hasOpenedCloud;
    private boolean mHasSd = false;
    private int mNextPage = 0;
    private DeviceCmdComponent mDeviceCmdComponent = null;

    public DeviceMsgPresenterImpl(IDeviceMessageView viewInterface) {
        this.viewInterface = new WeakReference<>(viewInterface);
        mDeviceCmdComponent = new DeviceCmdComponent();
    }

    @Override
    public void destroy() {
        if (this.viewInterface != null) {
            this.viewInterface.clear();
            this.viewInterface = null;
        }
    }

    @Override
    public void loadWarningMessage(int page, String deviceId, int size) {
        if (page == 0) {
            mNextPage = 0;
        }

        NooieLog.d("-->> DeviceMsgPresenterImpl loadWarningMessage deviceId=" + deviceId + " mNextPage=" + DateTimeUtil.getTimeString(mNextPage * 1000L, DateTimeUtil.PATTERN_YMD_HMS_2));
        getWarningMessageObservable(mNextPage, deviceId, size)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<DeviceMessage>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (viewInterface != null && viewInterface.get() != null) {
                            viewInterface.get().onHandleFailed(deviceId, "");
                        }
                    }

                    @Override
                    public void onNext(List<DeviceMessage> messages) {
                        if (viewInterface != null && viewInterface.get() != null) {
                            viewInterface.get().onLoadWarningMessage(deviceId, messages, hasOpenedCloud);
                        }
                    }
                });
    }

    private Observable<List<DeviceMessage>> getWarningMessageObservable(int page, String deviceId, int size) {

        Observable<List<DeviceMessage>> deviceMsgObservable = MessageService.getService().getDeviceMsg(deviceId, 0, page, size)
                .flatMap(new Func1<BaseResponse<List<DeviceMessage>>, Observable<List<DeviceMessage>>>() {
                    @Override
                    public Observable<List<DeviceMessage>> call(BaseResponse<List<DeviceMessage>> response) {

                        if (response != null && response.getCode() == StateCode.SUCCESS.code) {

                            if (response.getData() == null || response.getData().size() == 0) {
                                return Observable.just(CollectionUtil.safeFor(response.getData()));
                            }

                            int lastMessageIndex = response.getData().size() - 1;
                            mNextPage = response.getData().get(lastMessageIndex).getTime();
                            NooieLog.d("-->> DeviceMsgPresenterImpl call lastMsgIndex=" + lastMessageIndex + " mNextPage=" + DateTimeUtil.getTimeString(mNextPage * 1000L, DateTimeUtil.PATTERN_YMD_HMS_2));
                        }
                        return Observable.just(CollectionUtil.safeFor(response.getData()));
                    }
                });

        return deviceMsgObservable;
    }

    @Override
    public void updateMsgReadState(int msgId, int type) {
        MessageService.getService().updateMsgStatus(String.valueOf(msgId), type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                    }
                });
    }

    @Override
    public void setDeviceMsgReadState(String deviceId) {
        MessageService.getService().setDeviceAllMsgRead(deviceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                    }
                });
    }

    public static String getWarnMsgDesc(DeviceMessage pushMsg) {
        Resources res = NooieApplication.get().getResources();
        StringBuilder sb = new StringBuilder();
        if (pushMsg != null) {
            switch (pushMsg.getType()) {
                case ApiConstant.DEVICE_MSG_TYPE_MOTION_DETECT:
                    sb.append(res.getString(R.string.warn_message_motion));
                    break;
                case ApiConstant.DEVICE_MSG_TYPE_SOUND_DETECT:
                    sb.append(res.getString(R.string.warn_message_sound));
                    break;
                case ApiConstant.DEVICE_MSG_TYPE_PIR_DETECT:
                    sb.append(res.getString(R.string.warn_message_pir));
                    break;
                case ApiConstant.DEVICE_MSG_TYPE_SD_LEAK:
                    sb.append(res.getString(R.string.warn_message_sd_leak));
                    break;
                default:
                    sb.append(res.getString(R.string.warn_message_other));
                    break;
            }
        } else {
            sb.append(res.getString(R.string.warn_message_other));
        }

        return sb.toString();
    }

    /**
     * Motion Detected
     * 4:52pm
     * Motion detected from Living Room camera at 4:46pm for 47s...
     *
     * @param pushMsg
     * @return
     */
    public static String getWarnMsgContent(DeviceMessage pushMsg) {
        Resources res = NooieApplication.get().getResources();
        if (pushMsg == null) {
            return res.getString(R.string.unknown);
        }

        DeviceInfo deviceInfo = NooieDeviceHelper.getDeviceInfoById(pushMsg.getUuid());
        String camera = deviceInfo != null && deviceInfo.getNooieDevice() != null ? deviceInfo.getNooieDevice().getName() : pushMsg.getUuid();
        String time = DateTimeUtil.getUtcTimeString(pushMsg.getDevice_time() * 1000L, DateTimeUtil.PATTERN_HMS);

        StringBuilder sb = new StringBuilder();
        if (pushMsg!= null) {
            switch (pushMsg.getType()) {
                case ApiConstant.DEVICE_MSG_TYPE_MOTION_DETECT:
                    sb.append(String.format(res.getString(R.string.message_motion_detect_info), camera, time));
                    break;
                case ApiConstant.DEVICE_MSG_TYPE_SOUND_DETECT:
                    sb.append(String.format(res.getString(R.string.message_sound_detect_info), camera, time));
                    break;
                case ApiConstant.DEVICE_MSG_TYPE_PIR_DETECT:
                    sb.append(String.format(res.getString(R.string.message_pir_detect_info), camera, time));
                    break;
                case ApiConstant.DEVICE_MSG_TYPE_SD_LEAK:
                    sb.append(res.getString(R.string.message_sd_leak_info));
                    break;
            }
        } else {
            sb.append(res.getString(R.string.warn_message_other));
        }

        return sb.toString();
    }

    @Override
    public void deleteDeviceMessages(List<String> msgIds) {
    }

    @Override
    public void deleteAllMessages(String deviceId) {
        MessageService.getService()
                .deleteAllDeviceMsg(deviceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (viewInterface != null && viewInterface.get() != null) {
                            viewInterface.get().notifyDeleteAllMsgResult(deviceId, "");
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && viewInterface != null && viewInterface.get() != null) {
                            viewInterface.get().notifyDeleteAllMsgResult(deviceId, ConstantValue.SUCCESS);
                        } else if (viewInterface != null && viewInterface.get() != null) {
                            viewInterface.get().notifyDeleteAllMsgResult(deviceId, "");
                        }
                    }
                });
    }

    @Override
    public void deleteNooieMessageByIds(String deviceId, List<String> msgIds) {
        String deleteMsgIds = ConvertUtil.convertListToString(msgIds);
        MessageService.getService()
                .deleteMsgById(deleteMsgIds, ApiConstant.MSG_TYPE_DEVICE)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (viewInterface != null && viewInterface.get() != null) {
                            viewInterface.get().notifyDeleteAllMsgResult(deviceId, "");
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && viewInterface != null && viewInterface.get() != null) {
                            viewInterface.get().notifyDeleteAllMsgResult(deviceId, ConstantValue.SUCCESS);
                        } else if (viewInterface != null && viewInterface.get() != null) {
                            viewInterface.get().notifyDeleteAllMsgResult(deviceId, "");
                        }
                    }
                });
    }

    @Override
    public void getNooieDeviceSdCardSate(String user, String deviceId, boolean isMounted) {
        NooieLog.d("-->> debug DeviceMessage monitor 2000 deviceId=" + deviceId + mHasSd);
        if (mDeviceCmdComponent != null) {
            mDeviceCmdComponent.getFormatInfo(deviceId, isMounted, new OnGetFormatInfoListener() {
                @Override
                public void onGetFormatInfo(int code, FormatInfo formatInfo) {
                    NooieLog.d("-->> debug DeviceMessage monitor 2001 deviceId=" + deviceId + mHasSd);
                    dealGetFormatInfo(code, formatInfo, deviceId);
                }
            });
        }
        /*
        DeviceCmdApi.getInstance().getFormatInfo(deviceId, new OnGetFormatInfoListener() {
            @Override
            public void onGetFormatInfo(int code, FormatInfo formatInfo) {
                NooieLog.d("-->> debug DeviceMessage monitor 2001 deviceId=" + deviceId + mHasSd);
                dealGetFormatInfo(code, formatInfo, deviceId);
            }
        });

         */
    }

    private void dealGetFormatInfo(int code, FormatInfo formatInfo, String deviceId) {
        NooieLog.d("-->> debug DeviceMessage monitor 2002 deviceId=" + deviceId + mHasSd);
        if (viewInterface == null || viewInterface.get() == null) {
            return;
        }
        NooieLog.d("-->> debug DeviceMessage monitor 2003 deviceId=" + deviceId + mHasSd + " code=" + code);
        if (code == SDKConstant.CODE_CACHE || code == Constant.OK) {
            NooieLog.d("-->> debug DeviceMessage monitor 2004 deviceId=" + deviceId + mHasSd);
            mHasSd = formatInfo != null ? NooieDeviceHelper.isHasSdCard(formatInfo.getFormatStatus()) : false;
            viewInterface.get().notifyHaveSDCardResult(code, deviceId, ConstantValue.SUCCESS, mHasSd);
        } else {
            NooieLog.d("-->> debug DeviceMessage monitor 2005 deviceId=" + deviceId + mHasSd);
            viewInterface.get().notifyHaveSDCardResult(code, deviceId, NooieApplication.get().getString(R.string.unknown), mHasSd);
        }
    }

    @Override
    public void checkNooieDeviceIsOpenCloud(String account, String uid, final String deviceId, int bindType) {
        getCloudStateFromConfigure(deviceId);
        PackService.getService().getPackInfo(deviceId, bindType)
                .flatMap(new Func1<BaseResponse<PackInfoResult>, Observable<BaseResponse<PackInfoResult>>>() {
                    @Override
                    public Observable<BaseResponse<PackInfoResult>> call(BaseResponse<PackInfoResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            DeviceApi.getInstance().updateConfigurePackInfo(false, account, deviceId, response.getData());
                        } else if (response != null && response.getCode() == StateCode.DEVICE_UNSUBSCRIBE_CLOUD.code) {
                            DeviceApi.getInstance().updateConfigurePackInfo(false, account, deviceId, NooieCloudHelper.createPackInfoResult());
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
                        loadWarningMessage(0, deviceId, 30);
                    }

                    @Override
                    public void onNext(BaseResponse<PackInfoResult> response) {
                        DeviceConfigureEntity configureEntity = DeviceConfigureCache.getInstance().getDeviceConfigure(deviceId);
                        int status = ApiConstant.CLOUD_STATE_UNSUBSCRIBE;
                        boolean isEvent = false;
                        int storageDayNum = 0;
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            DeviceInfo deviceInfo = NooieDeviceHelper.getDeviceInfoById(deviceId);
                            float deviceTimezone = deviceInfo != null && deviceInfo.getNooieDevice() != null ? deviceInfo.getNooieDevice().getZone() : CountryUtil.getCurrentTimeZone();
                            hasOpenedCloud = NooieCloudHelper.isOpenCloud(response.getData().getEnd_time(), deviceTimezone);
                            status = response.getData().getStatus();
                            isEvent = NooieCloudHelper.isEventCloud(response.getData().getIs_event());
                            storageDayNum = response.getData().getFile_time();
                        } else if (configureEntity != null) {
                            status = configureEntity.getStatus();
                            isEvent = NooieCloudHelper.isEventCloud(configureEntity.getIsEvent());
                            storageDayNum = configureEntity.getFileTime();
                        }
                        if (viewInterface != null && viewInterface.get() != null) {
                            viewInterface.get().onCheckPackInfo(deviceId, hasOpenedCloud, status, isEvent, storageDayNum);
                        }
                        loadWarningMessage(0, deviceId, 30);
                    }
                });
    }

    private void getCloudStateFromConfigure(String deviceId) {
        DeviceConfigureEntity configureEntity = DeviceConfigureCache.getInstance().getDeviceConfigure(deviceId);
        if (configureEntity != null) {
            float deviceTimezone = NooieDeviceHelper.getDeviceById(deviceId) != null ? NooieDeviceHelper.getDeviceById(deviceId).getZone() : CountryUtil.getCurrentTimeZone();
            hasOpenedCloud = NooieCloudHelper.isOpenCloud(configureEntity.getEndTime(), deviceTimezone);
            if (viewInterface != null && viewInterface.get() != null) {
                viewInterface.get().onCheckPackInfo(deviceId, hasOpenedCloud, configureEntity.getStatus(), NooieCloudHelper.isEventCloud(configureEntity.getIsEvent()), configureEntity.getFileTime());
            }
        }
    }

    @Override
    public void checkIsOwnerDevice(String deviceId, final DataEffect<Boolean> dataEffect) {
        DeviceInfo deviceInfo = NooieDeviceHelper.getDeviceInfoById(deviceId);
        if (deviceInfo != null && deviceInfo.getNooieDevice() != null && viewInterface != null && viewInterface.get() != null) {
            dataEffect.setValue(deviceInfo.getNooieDevice().getBind_type() == ApiConstant.BIND_TYPE_OWNER);
            dataEffect.setEffective(true);
            viewInterface.get().notifyCheckDataEffectResult(deviceId, dataEffect.getKey(), dataEffect);
        } else {
            DeviceService.getService().getDeviceInfo(deviceId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<BaseResponse<BindDevice>>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                        }

                        @Override
                        public void onNext(BaseResponse<BindDevice> response) {
                            if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null && viewInterface != null && viewInterface.get() != null) {
                                dataEffect.setValue(response.getData().getBind_type() == ApiConstant.BIND_TYPE_OWNER);
                                dataEffect.setEffective(true);
                                viewInterface.get().notifyCheckDataEffectResult(deviceId, dataEffect.getKey(), dataEffect);
                            }
                        }
                    });
        }
    }
}
