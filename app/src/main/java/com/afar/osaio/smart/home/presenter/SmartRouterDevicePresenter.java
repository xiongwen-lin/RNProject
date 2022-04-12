package com.afar.osaio.smart.home.presenter;

import android.text.TextUtils;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.routerdata.GetRouterDataFromCloud;
import com.afar.osaio.routerdata.SendHttpRequest;
import com.afar.osaio.smart.cache.SmartRouterDeviceCache;
import com.afar.osaio.smart.device.bean.ListDeviceItem;
import com.afar.osaio.smart.device.helper.SmartDeviceHelper;
import com.afar.osaio.smart.home.bean.SmartDeviceConstant;
import com.afar.osaio.smart.home.bean.SmartRouterDevice;
import com.afar.osaio.smart.home.contract.SmartRouterDeviceContract;
import com.afar.osaio.smart.routerlocal.RouterDao;
import com.afar.osaio.smart.routerlocal.RouterInfo;
import com.afar.osaio.smart.routerlocal.internet.InternetConnectionStatus;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.bean.SDKConstant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class SmartRouterDevicePresenter implements SmartRouterDeviceContract.Presenter {

    private SmartRouterDeviceContract.View mTaskView;
    private Subscription mLoadRouterDeviceTask = null;

    public SmartRouterDevicePresenter(SmartRouterDeviceContract.View view) {
        mTaskView = view;
    }

    @Override
    public void destroy() {
        if (mTaskView != null) {
            mTaskView = null;
        }
    }

    @Override
    public void refreshRouterDevice(String account) {
        // 本地通信
        GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(new SendHttpRequest.getRouterReturnInfo() {
            @Override
            public void routerReturnInfo(String info, String topicurlString) {
                String routerWifiMac = "";
                try {
                    if ("error".equals(info) && "getInitCfg".equals(topicurlString)) {
                    } else if (!"error".equals(info) && "getInitCfg".equals(topicurlString)) {
                        routerWifiMac = new JSONObject(info).getString("mac");
                    }
                } catch (Exception e) {
                }
                loadRouterDevices(account, routerWifiMac);
            }
        });
        try {
            routerDataFromCloud.getInitCfg();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadRouterDevices(String account, String routerWifiMac) {
        stopLoadRouterDevices();
        mLoadRouterDeviceTask = Observable.just("")
                .flatMap(new Func1<String, Observable<List<SmartRouterDevice>>>() {
                    @Override
                    public Observable<List<SmartRouterDevice>> call(String s) {
                        return Observable.just(queryDeviceFromDb(routerWifiMac));
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<SmartRouterDevice>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onLoadRouterDevices(SDKConstant.ERROR, null);
                        }
                    }

                    @Override
                    public void onNext(List<SmartRouterDevice> routerDevices) {
                        SmartRouterDeviceCache.getInstance().addDevices(routerDevices);
                        if (mTaskView != null) {
                            mTaskView.onLoadRouterDevices(SDKConstant.SUCCESS, routerDevices);
                        }
                    }
                });
    }

    @Override
    public void stopLoadRouterDevices() {
        if (mLoadRouterDeviceTask != null && !mLoadRouterDeviceTask.isUnsubscribed()) {
            mLoadRouterDeviceTask.unsubscribe();
        }
        mLoadRouterDeviceTask = null;
    }

    @Override
    public void deleteRouterDevice(String routerDevice) {
        Observable.just(routerDevice)
                .flatMap(new Func1<String, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(String deviceId) {
                        deleteRouter(deviceId);
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
                        if (mTaskView != null) {
                            mTaskView.onDeleteRouterDevice(SDKConstant.ERROR, routerDevice);
                        }
                    }

                    @Override
                    public void onNext(Boolean result) {
                        if (mTaskView != null) {
                            mTaskView.onDeleteRouterDevice(SDKConstant.SUCCESS, routerDevice);
                        }
                    }
                });
    }

    /**
     * 获取所有路由器
     *  0 ： 离线
     *  1 ： 在线
     *  2 ： 未连接
     *  路由器首页显示逻辑： 没有连接路由器： 不显示文案,点击弹窗提示
     *                     连接路由器wifi但是不能上外网,显示离线
     *                     连上路由器可以上外网,在线
     * @param routerWifiMac
     * @return
     */
    @Override
    public List<SmartRouterDevice> queryDeviceFromDb(String routerWifiMac) {
        List<RouterInfo> routerList = RouterDao.getInstance(NooieApplication.mCtx).findAllRouter();
        List<SmartRouterDevice> smartRouterDevices = new ArrayList<>();
        if (CollectionUtil.isEmpty(routerList)) {
            return smartRouterDevices;
        }

        for (RouterInfo routerInfo : CollectionUtil.safeFor(routerList)) {
            String routerName = routerInfo.getRouterName();
            String isBind = routerInfo.getIsbind();
            String routerMac = routerInfo.getRouterMac();
            boolean isOnline = !TextUtils.isEmpty(routerWifiMac) && routerWifiMac.equalsIgnoreCase(routerMac) && InternetConnectionStatus.isNetSystemUsable();

            SmartRouterDevice smartRouterDevice = new SmartRouterDevice();
            smartRouterDevice.deviceId = routerMac;
            smartRouterDevice.model = "";
            smartRouterDevice.deviceCategory = SmartDeviceConstant.DEVICE_CATEGORY_ROUTER;
            smartRouterDevice.deviceSubCategory = SmartDeviceConstant.DEVICE_SUB_CATEGORY_NORMAL;
            smartRouterDevice.deviceName = routerName;
            smartRouterDevice.deviceState = SmartDeviceHelper.convertDeviceState(isOnline);
            smartRouterDevice.deviceSwitchState = SmartDeviceConstant.DEVICE_SWITCH_STATE_ON;
            smartRouterDevice.bindType = SmartDeviceConstant.DEVICE_BIND_TYPE_OWNER;
            smartRouterDevice.deviceIconUrl = "";
            smartRouterDevice.isBind = isBind;
            smartRouterDevice.routerBindType = !isOnline ||
                    (!TextUtils.isEmpty(routerWifiMac) && !(routerWifiMac.equalsIgnoreCase(routerMac) || InternetConnectionStatus.isNetSystemUsable())) ? 2 : 0;

            smartRouterDevices.add(smartRouterDevice);
        }

        return smartRouterDevices;
    }

    @Override
    public List<SmartRouterDevice> getRouterDevices() {
        return SmartDeviceHelper.sortSmartRouterDevice(SmartRouterDeviceCache.getInstance().getAllCache());
    }

    private void deleteRouter(String routerDevice) {
        if (TextUtils.isEmpty(routerDevice)) {
            return;
        }
        RouterDao.getInstance(NooieApplication.mCtx).deleteRouter(routerDevice);
    }

}
