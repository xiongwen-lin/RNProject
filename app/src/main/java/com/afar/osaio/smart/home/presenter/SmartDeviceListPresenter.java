package com.afar.osaio.smart.home.presenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.helper.SmartDeviceHelper;
import com.afar.osaio.smart.electrician.bean.SwitchBean;
import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.model.DeviceModel;
import com.afar.osaio.smart.electrician.model.IDeviceModel;
import com.afar.osaio.smart.home.bean.SmartCameraDevice;
import com.afar.osaio.smart.home.bean.SmartRouterDevice;
import com.afar.osaio.smart.home.bean.SmartTyDevice;
import com.afar.osaio.smart.home.contract.SmartDeviceListContract;
import com.afar.osaio.smart.home.contract.SmartIpcDeviceContract;
import com.afar.osaio.smart.home.contract.SmartRouterDeviceContract;
import com.afar.osaio.smart.home.contract.SmartTuyaDeviceContract;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.afar.osaio.test.SmartDeviceListTestPresenter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.bean.TabItemBean;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.notify.NotificationUtil;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.BindDeviceResult;
import com.nooie.sdk.api.network.device.DeviceService;
import com.nooie.sdk.bean.SDKConstant;
import com.tuya.smart.api.service.MicroServiceManager;
import com.tuya.smart.commonbiz.bizbundle.family.api.AbsBizBundleFamilyService;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.api.IResultCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class SmartDeviceListPresenter implements SmartDeviceListContract.Presenter {

    private Map<String, IDeviceModel> mDeviceModels = new HashMap<>();

    private static final int PAGE_MAX_DEVICE_NUM = 100;

    private SmartDeviceListContract.View mTasksView;
    private SmartIpcDevicePresenter mIpcDevicePresenter = null;
    private SmartTuyaDevicePresenter mTuyaDevicePresenter = null;
    private SmartRouterDevicePresenter mRouterDevicePresenter = null;
    private SmartIpcDeviceContract.View mIpcDeviceView = null;
    private SmartTuyaDeviceContract.View mTuyaDeviceView = null;
    private SmartRouterDeviceContract.View mRouterDeviceView = null;

    private Map<String, Integer> mLastCategoryMap = null;
    private boolean mDeviceCategoryChecking = false;

    private DeviceChangeBroadcastReceiver mDeviceChangeBroadcastReceiver;

    public SmartDeviceListPresenter(SmartDeviceListContract.View tasksView, SmartIpcDeviceContract.View ipcDeviceView, SmartTuyaDeviceContract.View tuyaDeviceView) {
        this.mTasksView = tasksView;
        this.mTasksView.setPresenter(this);
        mIpcDeviceView = ipcDeviceView;
        mTuyaDeviceView = tuyaDeviceView;
        mIpcDevicePresenter = new SmartIpcDevicePresenter(mIpcDeviceView);
        mTuyaDevicePresenter = new SmartTuyaDevicePresenter(mTuyaDeviceView);
        mRouterDevicePresenter = new SmartRouterDevicePresenter(mRouterDeviceView);
        mLastCategoryMap = new HashMap<>();
        mLastCategoryMap.put(ConstantValue.TAB_DEVICE_CATEGORY_CAMERA, 0);
        mLastCategoryMap.put(ConstantValue.TAB_DEVICE_CATEGORY_TUYA, 0);
        mLastCategoryMap.put(ConstantValue.TAB_DEVICE_CATEGORY_ROUTER, 0);
    }

    @Override
    public void destroy() {
        if (mTasksView != null) {
            mTasksView.setPresenter(null);
            mTasksView = null;
        }
        if (mIpcDeviceView != null) {
            mIpcDevicePresenter = null;
            mIpcDeviceView = null;
        }
        if (mTuyaDeviceView != null) {
            mTuyaDevicePresenter = null;
            mIpcDeviceView = null;
        }
    }

    @Override
    public void tryRefreshDeviceCategory(String account, String uid) {
        if (ApHelper.getInstance().checkBleApDeviceConnectingExist()) {
            if (mIpcDevicePresenter != null) {
                mIpcDevicePresenter.checkBleApDeviceConnecting();
                mIpcDevicePresenter.checkApDirectWhenNetworkChange();
            }
            return;
        }
        createDefaultHome(0, new IGetHomeIdCallback() {
            @Override
            public void onResult(int code, long homeId) {
//                if (code == SDKConstant.SUCCESS) {
//                    refreshDeviceCategory(account, uid, homeId);
//                } else {
//                    refreshDeviceCategory(account, uid, homeId);
//                }
                Map<String, Integer> categoryMap = mLastCategoryMap;
                List<TabItemBean> tabItemBeans = createCategoryList(categoryMap, mLastCategoryMap);
                mLastCategoryMap = categoryMap;
                if (mTasksView != null) {
                    mTasksView.onRefreshDeviceCategory(account, uid, homeId, tabItemBeans, true);
                }
            }
        });
    }

    @Override
    public void refreshDevice(String account, String uid, long homeId) {
        if (mTuyaDevicePresenter != null) {
            mTuyaDevicePresenter.loadHomeDetail(homeId);
        }
        if (mIpcDevicePresenter != null) {
            mIpcDevicePresenter.refreshIpcDevices(account, uid);
        }
        if (mRouterDevicePresenter != null) {
            mRouterDevicePresenter.refreshRouterDevice(account);
        }
    }

    @Override
    public List<SmartCameraDevice> getCameraDevices() {
        List<SmartCameraDevice> result = new ArrayList<>();
        if (mIpcDevicePresenter != null) {
            result.addAll(CollectionUtil.safeFor(mIpcDevicePresenter.getIpcDevices()));
        }
        return result;
    }

    @Override
    public List<SmartTyDevice> getTyDevices() {
        List<SmartTyDevice> result = new ArrayList<>();
        if (mTuyaDevicePresenter != null) {
            result.addAll(CollectionUtil.safeFor(mTuyaDevicePresenter.getSmartTyDevices()));
        }
        return result;
    }

    @Override
    public List<SmartRouterDevice> getRouterDevices() {
        List<SmartRouterDevice> result = new ArrayList<>();
        if (mRouterDevicePresenter != null) {
            result.addAll(CollectionUtil.safeFor(mRouterDevicePresenter.getRouterDevices()));
        }
        return result;
    }

    @Override
    public void refreshDeviceCategory(String account, String uid, long homeId) {
        mDeviceCategoryChecking = true;
        TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                int tuyaDeviceSize = homeBean != null ? CollectionUtil.size(homeBean.getDeviceList()) : 0;
                checkDeviceCategoryExist(account, uid, homeId, tuyaDeviceSize);
            }

            @Override
            public void onError(String errorCode, String errorMsg) {
                NooieLog.e("--------->getHomeDetail refreshDeviceCategory onError msg="+errorMsg);
                checkDeviceCategoryExist(account, uid, homeId, -1);
            }
        });
    }

    @Override
    public void registerDeviceChangeReceiver() {
        if (mDeviceChangeBroadcastReceiver == null) {
            mDeviceChangeBroadcastReceiver = new DeviceChangeBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter(ConstantValue.BROADCAST_KEY_REMOVE_CAMERA);
            intentFilter.addAction(ConstantValue.BROADCAST_KEY_UPDATE_CAMERA);
            NotificationUtil.registerLocalReceiver(NooieApplication.mCtx, mDeviceChangeBroadcastReceiver, intentFilter);
        }
    }

    @Override
    public void unRegisterDeviceChangeReceiver() {
        if (mDeviceChangeBroadcastReceiver != null) {
            NotificationUtil.unregisterLocalReceiver(NooieApplication.mCtx, mDeviceChangeBroadcastReceiver);
            mDeviceChangeBroadcastReceiver = null;
        }
    }

    @Override
    public void checkBleApDeviceConnecting() {
        if (mIpcDevicePresenter != null) {
            mIpcDevicePresenter.checkBleApDeviceConnecting();
        }
    }

    @Override
    public void checkApDirectWhenNetworkChange() {
        if (mIpcDevicePresenter != null) {
            mIpcDevicePresenter.checkApDirectWhenNetworkChange();
        }
    }

    @Override
    public void checkBeforeConnectBleDevice(String bleDeviceId, String model, String ssid) {
        if (mIpcDevicePresenter != null) {
            mIpcDevicePresenter.checkBeforeConnectBleDevice(bleDeviceId, model, ssid);
        }
    }

    @Override
    public void stopAPDirectConnection(String model) {
        if (mIpcDevicePresenter != null) {
            mIpcDevicePresenter.stopAPDirectConnection(model);
        }
    }

    @Override
    public void updateDeviceOpenStatus(String deviceId, boolean on) {
        if (mIpcDevicePresenter != null) {
            mIpcDevicePresenter.updateDeviceOpenStatus(deviceId, on);
        }
    }

    @Override
    public void getDeviceOpenStatus(String deviceId) {
        if (mIpcDevicePresenter != null) {
            mIpcDevicePresenter.getDeviceOpenStatus(deviceId);
        }
    }

    @Override
    public void updateApDeviceOpenStatus(String deviceSsid, String deviceId, boolean on) {
        if (mIpcDevicePresenter != null) {
            mIpcDevicePresenter.updateApDeviceOpenStatus(deviceSsid, deviceId, on);
        }
    }

    @Override
    public void removeIpcDevice(String account, String deviceId) {
        if (mIpcDevicePresenter != null) {
            mIpcDevicePresenter.removeIpcDevice(account, deviceId);
        }
    }

    @Override
    public void controlDevice(String deviceId, boolean open) {
        if (mTuyaDevicePresenter != null) {
            mTuyaDevicePresenter.controlDevice(deviceId, open);
        }
    }

    @Override
    public void controlLamp(String devId, String dpId, boolean open) {
        if (mTuyaDevicePresenter != null) {
            mTuyaDevicePresenter.controlLamp(devId, dpId, open);
        }
    }

    @Override
    public void controlStrip(String devId, Map<String, Object> dpsMap) {
        if (mTuyaDevicePresenter != null) {
            mTuyaDevicePresenter.controlStrip(devId, dpsMap);
        }
    }

    @Override
    public void loadDeviceBean(String deviceId) {
        if (mTuyaDevicePresenter != null) {
            mTuyaDevicePresenter.loadDeviceBean(deviceId);
        }
    }

    @Override
    public void updateDeviceCategory(String account, String uid, String tabDeviceCategory, int deviceSize) {
        if (mDeviceCategoryChecking || TextUtils.isEmpty(tabDeviceCategory) || mLastCategoryMap == null || mLastCategoryMap.isEmpty()) {
            return;
        }
        if (mLastCategoryMap.containsKey(tabDeviceCategory)) {
            int value = mLastCategoryMap.get(tabDeviceCategory);
            if (value < 1 && deviceSize > 0) {
                mLastCategoryMap.put(tabDeviceCategory, deviceSize);
            }
            if (value != deviceSize) {
                mLastCategoryMap.put(tabDeviceCategory, deviceSize);
            }
        }
        List<TabItemBean> tabItemBeans = createCategoryList(mLastCategoryMap, mLastCategoryMap);
        if (mTasksView != null) {
            long homeId = FamilyManager.getInstance().getCurrentHomeId() > 0 ? FamilyManager.getInstance().getCurrentHomeId() : 1;
            mTasksView.onRefreshDeviceCategory(account, uid, homeId, tabItemBeans, false);
        }
    }


    private void checkDeviceCategoryExist(String account, String uid, long homeId, int tuyaDeviceSize) {
        Map<String, Integer> categoryMap = new HashMap<>();
        categoryMap.put(ConstantValue.TAB_DEVICE_CATEGORY_TUYA, tuyaDeviceSize);
        Observable.just(1)
                .flatMap(new Func1<Integer, Observable<BaseResponse<BindDeviceResult>>>() {
                    @Override
                    public Observable<BaseResponse<BindDeviceResult>> call(Integer integer) {
                        try {
                            int routerCount = 0;
                            if (mRouterDevicePresenter != null) {
                                routerCount = CollectionUtil.size(mRouterDevicePresenter.queryDeviceFromDb(""));
                            }
                            categoryMap.put(ConstantValue.TAB_DEVICE_CATEGORY_ROUTER, routerCount);
                        } catch (Exception e) {
                            categoryMap.put(ConstantValue.TAB_DEVICE_CATEGORY_ROUTER, -1);
                        }
                        return DeviceService.getService().getBindDevices(1, PAGE_MAX_DEVICE_NUM);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<BindDeviceResult>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        categoryMap.put(ConstantValue.TAB_DEVICE_CATEGORY_CAMERA, -1);
                        List<TabItemBean> tabItemBeans = createCategoryList(categoryMap, mLastCategoryMap);
                        mLastCategoryMap = categoryMap;
                        if (mTasksView != null) {
                            mTasksView.onRefreshDeviceCategory(account, uid, homeId, tabItemBeans, true);
                        }
                        mDeviceCategoryChecking = false;
                    }

                    @Override
                    public void onNext(BaseResponse<BindDeviceResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code) {
                            int ipcCount = response.getData() != null && response.getData().getData() != null ? CollectionUtil.size(response.getData().getData()) : 0;
                            categoryMap.put(ConstantValue.TAB_DEVICE_CATEGORY_CAMERA, ipcCount);
                        } else {
                            categoryMap.put(ConstantValue.TAB_DEVICE_CATEGORY_CAMERA, -1);
                        }
                        List<TabItemBean> tabItemBeans = createCategoryList(categoryMap, mLastCategoryMap);
                        mLastCategoryMap = categoryMap;
                        if (mTasksView != null) {
                            mTasksView.onRefreshDeviceCategory(account, uid, homeId, tabItemBeans, true);
                        }
                        mDeviceCategoryChecking = false;
                    }
                });
    }

    private List<TabItemBean> createCategoryList(Map<String, Integer> categoryMap, Map<String, Integer> lastCategoryMap) {
        List<TabItemBean> tabItemBeans = new ArrayList<>();
        tabItemBeans.add(SmartDeviceHelper.createTabItemBean(SmartDeviceHelper.convertTabItemTitle(NooieApplication.mCtx, ConstantValue.TAB_DEVICE_CATEGORY_ALL), ConstantValue.TAB_DEVICE_CATEGORY_ALL, 1, 1));
        if (categoryMap == null) {
            return tabItemBeans;
        }
        if (categoryMap.containsKey(ConstantValue.TAB_DEVICE_CATEGORY_CAMERA)) {
            String title = SmartDeviceHelper.convertTabItemTitle(NooieApplication.mCtx, ConstantValue.TAB_DEVICE_CATEGORY_CAMERA);
            int value = categoryMap.get(ConstantValue.TAB_DEVICE_CATEGORY_CAMERA);
            if (value == -1) {
                value = lastCategoryMap != null && lastCategoryMap.containsKey(ConstantValue.TAB_DEVICE_CATEGORY_CAMERA) ? lastCategoryMap.get(ConstantValue.TAB_DEVICE_CATEGORY_CAMERA) : 0;
            }
            if (value > 0) {
                tabItemBeans.add(SmartDeviceHelper.createTabItemBean(title, ConstantValue.TAB_DEVICE_CATEGORY_CAMERA, 1, 2));
            }
        }

        if (categoryMap.containsKey(ConstantValue.TAB_DEVICE_CATEGORY_TUYA)) {
            String title = SmartDeviceHelper.convertTabItemTitle(NooieApplication.mCtx, ConstantValue.TAB_DEVICE_CATEGORY_TUYA);
            int value = categoryMap.get(ConstantValue.TAB_DEVICE_CATEGORY_TUYA);
            if (value == -1) {
                value = lastCategoryMap != null && lastCategoryMap.containsKey(ConstantValue.TAB_DEVICE_CATEGORY_TUYA) ? lastCategoryMap.get(ConstantValue.TAB_DEVICE_CATEGORY_TUYA) : 0;
            }
            if (value > 0) {
                tabItemBeans.add(SmartDeviceHelper.createTabItemBean(title, ConstantValue.TAB_DEVICE_CATEGORY_TUYA, 1, 3));
            }
        }

        if (categoryMap.containsKey(ConstantValue.TAB_DEVICE_CATEGORY_ROUTER)) {
            String title = SmartDeviceHelper.convertTabItemTitle(NooieApplication.mCtx, ConstantValue.TAB_DEVICE_CATEGORY_ROUTER);
            int value = categoryMap.get(ConstantValue.TAB_DEVICE_CATEGORY_ROUTER);
            if (value == -1) {
                value = lastCategoryMap != null && lastCategoryMap.containsKey(ConstantValue.TAB_DEVICE_CATEGORY_ROUTER) ? lastCategoryMap.get(ConstantValue.TAB_DEVICE_CATEGORY_ROUTER) : 0;
            }
            if (value > 0) {
                tabItemBeans.add(SmartDeviceHelper.createTabItemBean(title, ConstantValue.TAB_DEVICE_CATEGORY_ROUTER, 1, 4));
            }
        }
        return tabItemBeans;
    }

    private void createDefaultHome(int times, IGetHomeIdCallback callback) {
        if (FamilyManager.getInstance().getCurrentHomeId() > 0) {
            if (callback != null) {
                callback.onResult(SDKConstant.SUCCESS, FamilyManager.getInstance().getCurrentHomeId());
            }
            return;
        }
        NooieLog.e("-----------> SmartDeviceListPresenter createDefaultHome ConstantValue.HOME_DEFALUT_NAME " + ConstantValue.HOME_DEFALUT_NAME);
        FamilyManager.getInstance().getHomeList(new ITuyaGetHomeListCallback() {
            @Override
            public void onSuccess(List<HomeBean> list) {
                if (CollectionUtil.isEmpty(list)) {
                    List<String> rooms = new ArrayList<>();
                    rooms.add("room");
                    NooieLog.e("-----------> SmartDeviceListPresenter createDefaultHome homeId " + list.get(0).getHomeId());
                    FamilyManager.getInstance().createHome(ConstantValue.HOME_DEFALUT_NAME, rooms, new ITuyaHomeResultCallback() {
                        @Override
                        public void onSuccess(HomeBean homeBean) {
                            NooieLog.e("-----------> SmartDeviceListPresenter createDefaultHome homeId " + homeBean.getHomeId());
                            AbsBizBundleFamilyService service = MicroServiceManager.getInstance().findServiceByInterface(AbsBizBundleFamilyService.class.getName());
                            //设置为当前家庭的homeId
                            service.setCurrentHomeId(homeBean.getHomeId());
                            FamilyManager.getInstance().setCurrentHome(homeBean);
                            if (callback != null) {
                                callback.onResult(SDKConstant.SUCCESS, homeBean.getHomeId());
                            }
                        }

                        @Override
                        public void onError(String s, String s1) {
                            NooieLog.e("------->>>> createDefaultHome createHome onError  code " + s + " msg " + s1);
                        }
                    });
                } else {
                    NooieLog.e("-----------> SmartDeviceListPresenter createDefaultHome FamilyManager.getInstance().getCurrentHomeId() " + FamilyManager.getInstance().getCurrentHomeId());
                    if (FamilyManager.getInstance().getCurrentHomeId() != FamilyManager.DEFAULT_HOME_ID) {
                        for (HomeBean homeBean : list) {
                            if (homeBean.getHomeId() == FamilyManager.getInstance().getCurrentHomeId()) {
                                NooieLog.e("-----------> SmartDeviceListPresenter createDefaultHome for FamilyManager.getInstance().getCurrentHomeId() " + FamilyManager.getInstance().getCurrentHomeId());
                                AbsBizBundleFamilyService service = MicroServiceManager.getInstance().findServiceByInterface(AbsBizBundleFamilyService.class.getName());
                                service.setCurrentHomeId(FamilyManager.getInstance().getCurrentHomeId());
                                if (callback != null) {
                                    callback.onResult(SDKConstant.SUCCESS, homeBean.getHomeId());
                                }
                                return;
                            }
                        }
                        NooieLog.e("-----------> SmartDeviceListPresenter createDefaultHome list.get(0) " + list.get(0).getHomeId());
                        AbsBizBundleFamilyService service = MicroServiceManager.getInstance().findServiceByInterface(AbsBizBundleFamilyService.class.getName());
                        service.setCurrentHomeId(list.get(0).getHomeId());
                        FamilyManager.getInstance().setCurrentHome(list.get(0));
                        if (callback != null) {
                            callback.onResult(SDKConstant.SUCCESS, list.get(0).getHomeId());
                        }
                    } else {
                        NooieLog.e("-----------> SmartDeviceListPresenter createDefaultHome  === list.get(0) " + list.get(0).getHomeId());
                        AbsBizBundleFamilyService service = MicroServiceManager.getInstance().findServiceByInterface(AbsBizBundleFamilyService.class.getName());
                        service.setCurrentHomeId(list.get(0).getHomeId());
                        FamilyManager.getInstance().setCurrentHome(list.get(0));
                        if (callback != null) {
                            callback.onResult(SDKConstant.SUCCESS, list.get(0).getHomeId());
                        }
                    }
                }
            }

            @Override
            public void onError(String errorCode, String errorMsg) {
                NooieLog.e("------------->createDefaultHome errorCode " + errorCode + " errorMsg " + errorMsg);
                if (times == 0) {
                    createDefaultHome(1, callback);
                } else if (mTasksView != null) {
                    //todo 加载家庭失败
                    if (callback != null) {
                        callback.onResult(SDKConstant.ERROR, 1);
                    }
                }
            }
        });
    }

    private class DeviceChangeBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mTasksView != null) {
                mTasksView.onReceiveDeviceChange();
            }
        }
    }

    public interface IGetHomeIdCallback {

        void onResult(int code, long homeId);

    }

}
