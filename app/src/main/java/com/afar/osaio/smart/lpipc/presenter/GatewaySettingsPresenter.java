package com.afar.osaio.smart.lpipc.presenter;

import android.text.TextUtils;

import com.nooie.sdk.cache.DeviceConnectionCache;
import com.afar.osaio.smart.cache.DeviceInfoCache;
import com.afar.osaio.smart.cache.DeviceListCache;
import com.afar.osaio.smart.cache.GatewayDeviceCache;
import com.nooie.sdk.db.dao.DeviceCacheService;
import com.nooie.sdk.db.dao.GatewayDeviceService;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.lpipc.contract.GatewaySettingsContract;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.GatewayDevice;
import com.nooie.sdk.api.network.device.DeviceService;
import com.nooie.sdk.processor.device.DeviceApi;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class GatewaySettingsPresenter implements GatewaySettingsContract.Presenter {

    private GatewaySettingsContract.View mTaskView;

    public GatewaySettingsPresenter(GatewaySettingsContract.View view) {
        mTaskView = view;
        mTaskView.setPresenter(this);
    }

    @Override
    public void destroy() {
        if (mTaskView != null) {
            mTaskView.setPresenter(null);
            mTaskView = null;
        }
    }

    private Subscription mGetGatewayDeviceTask;
    @Override
    public void getGatewayDevices(String user, String uid) {
        stopGetGatewayDeviceTask();
        mGetGatewayDeviceTask = Observable.just(user)
                .flatMap(new Func1<String, Observable<BaseResponse<List<GatewayDevice>>>>() {
                    @Override
                    public Observable<BaseResponse<List<GatewayDevice>>> call(String user) {
                        NooieLog.d("-->> GatewaySettingsPresenter getGatewayDevices time1");
                        List<GatewayDevice> gatewayDevices = GatewayDeviceService.getInstance().getGatewayDevices(user);
                        GatewayDeviceCache.getInstance().addDevices(gatewayDevices);
                        NooieLog.d("-->> GatewaySettingsPresenter getGatewayDevices time2");
                        return DeviceService.getService().getGatewayDevices();
                    }
                })
                .flatMap(new Func1<BaseResponse<List<GatewayDevice>>, Observable<List<GatewayDevice>>>() {
                    @Override
                    public Observable<List<GatewayDevice>> call(BaseResponse<List<GatewayDevice>> response) {
                        NooieLog.d("-->> GatewaySettingsPresenter getGatewayDevices time3");
                        List<GatewayDevice> gatewayDevices = new ArrayList<>();
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            if (CollectionUtil.isNotEmpty(response.getData())) {
                                for (int i = 0; i < response.getData().size(); i++) {
                                    if (response.getData().get(i) != null && CollectionUtil.isNotEmpty(response.getData().get(i).getChild())) {
                                        for (int j = 0; j < response.getData().get(i).getChild().size(); j++) {
                                            if (response.getData().get(i).getChild().get(j) != null) {
                                                response.getData().get(i).getChild().get(j).setPuuid(response.getData().get(i).getUuid());
                                                response.getData().get(i).getChild().get(j).setBind_type(ApiConstant.BIND_TYPE_OWNER);
                                            }
                                        }
                                    }
                                }
                                gatewayDevices.addAll(response.getData());
                            }
                            GatewayDeviceCache.getInstance().clearCacheDeviceIds();
                            GatewayDeviceCache.getInstance().addCacheDeviceIds(NooieDeviceHelper.getGatewayDeviceIds(gatewayDevices));
                            GatewayDeviceCache.getInstance().compareDevcieListCache(user);
                            GatewayDeviceCache.getInstance().addDevices(gatewayDevices);
                            //GatewayDeviceCache.getInstance().updateDevicesInDb();
                            DeviceApi.getInstance().updateGatewayDevices(false, user, gatewayDevices);
                            NooieLog.d("-->> GatewaySettingsPresenter getGatewayDevices time4");
                        }
                        return Observable.just(gatewayDevices);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<GatewayDevice>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onGetGatewayDevicesResult(ConstantValue.ERROR, null);
                        }
                    }

                    @Override
                    public void onNext(List<GatewayDevice> gatewayDevices) {
                        NooieLog.d("-->> GatewaySettingsPresenter getGatewayDevices time5");
                        if (mTaskView != null) {
                            mTaskView.onGetGatewayDevicesResult(ConstantValue.SUCCESS, gatewayDevices);
                        }
                        preConnectGatewayDevices(gatewayDevices, user, uid);
                    }
                });
    }

    private void stopGetGatewayDeviceTask() {
        if (mGetGatewayDeviceTask != null && !mGetGatewayDeviceTask.isUnsubscribed()) {
            mGetGatewayDeviceTask.unsubscribe();
            mGetGatewayDeviceTask = null;
        }
    }

    private  void preConnectGatewayDevices(List<GatewayDevice> gatewayDevices, String account, String uid) {
        if (CollectionUtil.isEmpty(gatewayDevices) || TextUtils.isEmpty(uid)) {
            return;
        }

        NooieDeviceHelper.tryConnectionToGatewayDevice(uid, gatewayDevices, false);
    }

    @Override
    public void removeSubDevice(String account, String deviceId, String pDeviceId) {
        DeviceService.getService().deleteDevice(deviceId)
                .flatMap(new Func1<BaseResponse, Observable<BaseResponse>>() {
                    @Override
                    public Observable<BaseResponse> call(BaseResponse response) {
                        if (response != null && (response.getCode() == StateCode.SUCCESS.code || response.getCode() == StateCode.UUID_NOT_EXISTED.code)) {
                            DeviceInfoCache.getInstance().removeCacheById(deviceId);
                            DeviceListCache.getInstance().removeCacheById(deviceId);
                            DeviceCacheService.getInstance().deleteDevice(account, deviceId);
                            DeviceConnectionCache.getInstance().removeConnection(deviceId);
                        }
                        return Observable.just(response);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onDeleteSubDeviceResult(ConstantValue.ERROR, deviceId, pDeviceId);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (mTaskView != null) {
                            mTaskView.onDeleteSubDeviceResult(response != null && response.getCode() == StateCode.SUCCESS.code ? ConstantValue.SUCCESS : ConstantValue.ERROR, deviceId, pDeviceId);
                        }
                    }
                });
    }
}
