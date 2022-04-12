package com.afar.osaio.test;

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
import com.afar.osaio.smart.home.presenter.SmartIpcDevicePresenter;
import com.afar.osaio.smart.home.presenter.SmartRouterDevicePresenter;
import com.afar.osaio.smart.home.presenter.SmartTuyaDevicePresenter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.bean.TabItemBean;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
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

public class SmartDeviceListTestPresenter implements SmartDeviceListTestContract.Presenter {

    private Map<String, IDeviceModel> mDeviceModels = new HashMap<>();

    private static final int PAGE_MAX_DEVICE_NUM = 100;

    private SmartDeviceListTestContract.View mTasksView;
    private SmartIpcDevicePresenter mIpcDevicePresenter = null;
    private SmartTuyaDevicePresenter mTuyaDevicePresenter = null;
    private SmartRouterDevicePresenter mRouterDevicePresenter = null;
    private SmartIpcDeviceContract.View mIpcDeviceView = null;
    private SmartTuyaDeviceContract.View mTuyaDeviceView = null;
    private SmartRouterDeviceContract.View mRouterDeviceView = null;

    private Map<String, Integer> mLastCategoryMap= null;

    public SmartDeviceListTestPresenter(SmartDeviceListTestContract.View tasksView, SmartIpcDeviceContract.View ipcDeviceView, SmartTuyaDeviceContract.View tuyaDeviceView) {
        this.mTasksView = tasksView;
        this.mTasksView.setPresenter(this);
        mIpcDeviceView = ipcDeviceView;
        mTuyaDeviceView = tuyaDeviceView;
        mIpcDevicePresenter = new SmartIpcDevicePresenter(mIpcDeviceView);
        mTuyaDevicePresenter = new SmartTuyaDevicePresenter(mTuyaDeviceView);
        mRouterDevicePresenter = new SmartRouterDevicePresenter(mRouterDeviceView);
    }

    private long mCurrentHomeId = FamilyManager.getInstance().getCurrentHomeId();
    public void tryRefreshDeviceCategory(String account, String uid) {
        createDefaultHome(0, new IGetHomeIdCallback() {
            @Override
            public void onResult(int code, long homeId) {
                if (code == SDKConstant.SUCCESS) {
                    refreshDeviceCategory1(account, uid, homeId);
                } else {
                    refreshDeviceCategory1(account, uid, 1);
                }
            }
        });
    }

    public void refreshDeviceCategory1(String account, String uid, long homeId) {
    }

    public void createDefaultHome(int times, IGetHomeIdCallback callback) {
        if (FamilyManager.getInstance().getCurrentHomeId() > 0) {
            if (callback != null) {
                callback.onResult(SDKConstant.SUCCESS, FamilyManager.getInstance().getCurrentHomeId());
            }
            return;
        }
        NooieLog.e("----------->SmartDeviceListTestPresenter1 createDefaultHome ConstantValue.HOME_DEFALUT_NAME " + ConstantValue.HOME_DEFALUT_NAME);
        FamilyManager.getInstance().getHomeList(new ITuyaGetHomeListCallback() {
            @Override
            public void onSuccess(List<HomeBean> list) {
                if (CollectionUtil.isEmpty(list)) {
                    List<String> rooms = new ArrayList<>();
                    rooms.add("room");
                    NooieLog.e("----------->SmartDeviceListTestPresenter2 createDefaultHome ConstantValue.HOME_DEFALUT_NAME " + ConstantValue.HOME_DEFALUT_NAME);
                    FamilyManager.getInstance().createHome(ConstantValue.HOME_DEFALUT_NAME, rooms, new ITuyaHomeResultCallback() {
                        @Override
                        public void onSuccess(HomeBean homeBean) {
                            NooieLog.e("----------->SmartDeviceListTestPresenter3 createDefaultHome ConstantValue.HOME_DEFALUT_NAME onSuccess " + ConstantValue.HOME_DEFALUT_NAME);
                            AbsBizBundleFamilyService service = MicroServiceManager.getInstance().findServiceByInterface(AbsBizBundleFamilyService.class.getName());
                            //设置为当前家庭的homeId
                            service.setCurrentHomeId(homeBean.getHomeId());
                            FamilyManager.getInstance().setCurrentHome(homeBean);
                            mTasksView.getHomeId(homeBean.getHomeId());
                        }

                        @Override
                        public void onError(String s, String s1) {
                            NooieLog.e("------->>>> createDefaultHome createHome onError  code " + s + " msg " + s1);
                        }
                    });
                } else {
                    NooieLog.e("----------->SmartDeviceListTestPresenter4 createDefaultHome FamilyManager.getInstance().getCurrentHomeId() " + FamilyManager.getInstance().getCurrentHomeId());
                    if (FamilyManager.getInstance().getCurrentHomeId() != FamilyManager.DEFAULT_HOME_ID) {
                        for (HomeBean homeBean : list) {
                            if (homeBean.getHomeId() == FamilyManager.getInstance().getCurrentHomeId()) {
                                NooieLog.e("----------->SmartDeviceListTestPresenter5 createDefaultHome for FamilyManager.getInstance().getCurrentHomeId() " + FamilyManager.getInstance().getCurrentHomeId());
                                AbsBizBundleFamilyService service = MicroServiceManager.getInstance().findServiceByInterface(AbsBizBundleFamilyService.class.getName());
                                service.setCurrentHomeId(FamilyManager.getInstance().getCurrentHomeId());
                                mTasksView.getHomeId(homeBean.getHomeId());
                                return;
                            }
                        }
                        NooieLog.e("----------->SmartDeviceListTestPresenter6 createDefaultHome list.get(0) " + list.get(0).getHomeId());
                        AbsBizBundleFamilyService service = MicroServiceManager.getInstance().findServiceByInterface(AbsBizBundleFamilyService.class.getName());
                        service.setCurrentHomeId(list.get(0).getHomeId());
                        FamilyManager.getInstance().setCurrentHome(list.get(0));
                        mTasksView.getHomeId(list.get(0).getHomeId());
                    } else {
                        NooieLog.e("----------->SmartDeviceListTestPresenter7 createDefaultHome  === list.get(0) " + list.get(0).getHomeId());
                        AbsBizBundleFamilyService service = MicroServiceManager.getInstance().findServiceByInterface(AbsBizBundleFamilyService.class.getName());
                        service.setCurrentHomeId(list.get(0).getHomeId());
                        FamilyManager.getInstance().setCurrentHome(list.get(0));
                        mTasksView.getHomeId(list.get(0).getHomeId());
                    }
                }
            }

            @Override
            public void onError(String errorCode, String errorMsg) {
                NooieLog.e("------------->createDefaultHome errorCode " + errorCode + " errorMsg " + errorMsg);
                if (times == 0) {
                    //createDefaultHome(1);
                } else if (mTasksView != null) {
                   //todo 加载家庭失败
                }
            }
        });
    }

    public interface IGetHomeIdCallback {

        void onResult(int code, long homeId);

    }

   /* @Override
    public void createDefaultHome(int times) {

    }*/

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
    public void refreshDeviceCategory(String account, String uid, long homeId) {
        TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                int tuyaDeviceSize = homeBean != null ? CollectionUtil.size(homeBean.getDeviceList()) : 0;
                checkDeviceCategoryExist(account, uid, homeId, tuyaDeviceSize);
            }

            @Override
            public void onError(String errorCode, String errorMsg) {
                checkDeviceCategoryExist(account, uid, homeId, -1);
            }
        });
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
                            mTasksView.onRefreshDeviceCategory(account, uid, homeId, tabItemBeans);
                        }
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
                            mTasksView.onRefreshDeviceCategory(account, uid, homeId, tabItemBeans);
                        }
                    }
                });
    }

    @Override
    public void controlDevice(String deviceId, boolean open) {
        SwitchBean switchBean = new SwitchBean(ConstantValue.DP_ID_SWITCH_ON);
        switchBean.setOpen(open);
        IDeviceModel deviceModel;
        if (mDeviceModels.containsKey(deviceId) && mDeviceModels.get(deviceId) != null) {
            deviceModel = mDeviceModels.get(deviceId);
        } else {
            deviceModel = new DeviceModel(deviceId);
            mDeviceModels.put(deviceId, deviceModel);
        }

        deviceModel.sendCommand(switchBean.getDpId(), switchBean.isOpen(), new IResultCallback() {
            @Override
            public void onError(String code, String msg) {

            }

            @Override
            public void onSuccess() {

            }
        });
    }

    @Override
    public void controlLamp(String devId, String dpId, boolean open) {
        IDeviceModel deviceModel;
        if (mDeviceModels.containsKey(devId) && mDeviceModels.get(devId) != null) {
            deviceModel = mDeviceModels.get(devId);
        } else {
            deviceModel = new DeviceModel(devId);
            mDeviceModels.put(devId, deviceModel);
        }

        deviceModel.sendCommand(dpId, open, new IResultCallback() {
            @Override
            public void onError(String code, String msg) {

            }

            @Override
            public void onSuccess() {

            }
        });
    }

    @Override
    public void controlStrip(String devId, Map<String, Object> dpsMap) {
        IDeviceModel deviceModel;
        if (mDeviceModels.containsKey(devId) && mDeviceModels.get(devId) != null) {
            deviceModel = mDeviceModels.get(devId);
        } else {
            deviceModel = new DeviceModel(devId);
            mDeviceModels.put(devId, deviceModel);
        }
        if (dpsMap != null) {
            deviceModel.sendCommands(dpsMap, new IResultCallback() {
                @Override
                public void onError(String code, String error) {

                }

                @Override
                public void onSuccess() {

                }
            });
        }
    }

    private List<TabItemBean> createCategoryList(Map<String, Integer> categoryMap, Map<String, Integer> lastCategoryMap) {
        List<TabItemBean> tabItemBeans = new ArrayList<>();
        tabItemBeans.add(SmartDeviceHelper.createTabItemBean(ConstantValue.TAB_DEVICE_CATEGORY_ALL, ConstantValue.TAB_DEVICE_CATEGORY_ALL, 1, 1));
        if (categoryMap == null) {
            return tabItemBeans;
        }
        if (categoryMap.containsKey(ConstantValue.TAB_DEVICE_CATEGORY_CAMERA)) {
            int value = categoryMap.get(ConstantValue.TAB_DEVICE_CATEGORY_CAMERA);
            if (value == -1) {
                value = lastCategoryMap != null && lastCategoryMap.containsKey(ConstantValue.TAB_DEVICE_CATEGORY_CAMERA) ? lastCategoryMap.get(ConstantValue.TAB_DEVICE_CATEGORY_CAMERA) : 0;
            }
            if (value > 0) {
                tabItemBeans.add(SmartDeviceHelper.createTabItemBean(ConstantValue.TAB_DEVICE_CATEGORY_CAMERA, ConstantValue.TAB_DEVICE_CATEGORY_CAMERA, 1, 2));
            }
        }

        if (categoryMap.containsKey(ConstantValue.TAB_DEVICE_CATEGORY_TUYA)) {
            int value = categoryMap.get(ConstantValue.TAB_DEVICE_CATEGORY_TUYA);
            if (value == -1) {
                value = lastCategoryMap != null && lastCategoryMap.containsKey(ConstantValue.TAB_DEVICE_CATEGORY_TUYA) ? lastCategoryMap.get(ConstantValue.TAB_DEVICE_CATEGORY_TUYA) : 0;
            }
            if (value > 0) {
                tabItemBeans.add(SmartDeviceHelper.createTabItemBean(ConstantValue.TAB_DEVICE_CATEGORY_TUYA, ConstantValue.TAB_DEVICE_CATEGORY_TUYA, 1, 3));
            }
        }

        if (categoryMap.containsKey(ConstantValue.TAB_DEVICE_CATEGORY_ROUTER)) {
            int value = categoryMap.get(ConstantValue.TAB_DEVICE_CATEGORY_ROUTER);
            if (value == -1) {
                value = lastCategoryMap != null && lastCategoryMap.containsKey(ConstantValue.TAB_DEVICE_CATEGORY_ROUTER) ? lastCategoryMap.get(ConstantValue.TAB_DEVICE_CATEGORY_ROUTER) : 0;
            }
            if (value > 0) {
                tabItemBeans.add(SmartDeviceHelper.createTabItemBean(ConstantValue.TAB_DEVICE_CATEGORY_ROUTER, ConstantValue.TAB_DEVICE_CATEGORY_ROUTER, 1, 4));
            }
        }
        return tabItemBeans;
    }

}
