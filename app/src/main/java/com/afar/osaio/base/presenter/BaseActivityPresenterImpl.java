package com.afar.osaio.base.presenter;

import com.afar.osaio.base.view.IBaseActivityView;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.device.DeviceService;
import com.nooie.sdk.processor.device.DeviceApi;

import java.lang.ref.WeakReference;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by victor on 2018/8/21
 * Email is victor.qiao.0604@gmail.com
 */
public class BaseActivityPresenterImpl implements IBaseActivityPresenter {
    private WeakReference<IBaseActivityView> view;

    public BaseActivityPresenterImpl(IBaseActivityView view) {
        this.view = new WeakReference<IBaseActivityView>(view);
    }

    @Override
    public void handleNooieSharedDevice(int msgId, int sharedId, boolean agree) {
        DeviceService.getService().feedbackShare(msgId, sharedId, agree ? ApiConstant.SYS_MSG_SHARE_STATUS_ACCEPT : ApiConstant.SYS_MSG_SHARE_STATUS_REJECT)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (view.get() != null) {
                            view.get().notifyHandleShareDeviceFailed(StateCode.UNKNOWN.code);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && view.get() != null) {
                            view.get().notifyHandleShareDeviceSuccess();
                        } else if (view.get() != null) {
                            view.get().notifyHandleShareDeviceFailed(response != null ? response.getCode() : StateCode.UNKNOWN.code);
                        }
                    }
                });
    }

    @Override
    public void changeDeviceUpgradeState(final String user, final String deviceId, final int platform, final int upgradeState) {
        Observable.just("")
                .flatMap(new Func1<String, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(String s) {
                        //DeviceCacheService.getInstance().updateUpgradeState(user, deviceId, platform, upgradeState);
                        DeviceApi.getInstance().updateDeviceUpgradeStatus(false, user, deviceId, platform, upgradeState);
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

//    @Override
//    public void connectShortLinkDevice(String taskId, String account, String deviceId, ConnectShortLinkDeviceListener listener) {
//        NooieLog.d("-->> debug BaseActivityPresenterImpl connectShortLinkDevice: 1000 sortLinkDevice account=" + account + " deviceId=" + deviceId + " taskId=" + taskId);
//        if (TextUtils.isEmpty(taskId) || TextUtils.isEmpty(account) || TextUtils.isEmpty(deviceId)) {
//            listener.onResult(DeviceConnectionHelper.CONNECT_SHORT_LINK_DEVICE_PARAM_ERROR, taskId, account, deviceId);
//            return;
//        }
//        BindDevice device = NooieDeviceHelper.getDeviceById(deviceId);
//        if (device != null) {
//            DeviceConnectionHelper.getInstance().startQuickConnectShortLinkDevice(taskId, account, device, listener);
//            return;
//        }
//        DeviceService.getService().getDeviceInfo(deviceId)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<BaseResponse<BindDevice>>() {
//                    @Override
//                    public void onCompleted() {
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        if (listener != null) {
//                            listener.onResult(DeviceConnectionHelper.CONNECT_SHORT_LINK_DEVICE_PARAM_ERROR, taskId, account, deviceId);
//                        }
//                    }
//
//                    @Override
//                    public void onNext(BaseResponse<BindDevice> response) {
//                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
//                            DeviceConnectionHelper.getInstance().startQuickConnectShortLinkDevice(taskId, account, response.getData(), listener);
//                        } else if (listener != null) {
//                            listener.onResult(DeviceConnectionHelper.CONNECT_SHORT_LINK_DEVICE_PARAM_ERROR, taskId, account, deviceId);
//                        }
//                    }
//                });
//    }
}
