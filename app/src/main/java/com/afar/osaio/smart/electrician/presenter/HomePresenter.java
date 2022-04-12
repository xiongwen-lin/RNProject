package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.electrician.bean.SwitchBean;
import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.model.DeviceModel;
import com.afar.osaio.smart.electrician.model.GroupModel;
import com.afar.osaio.smart.electrician.model.IDeviceModel;
import com.afar.osaio.smart.electrician.model.IGroupModel;
import com.afar.osaio.smart.electrician.view.IHomeView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.base.GlobalData;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.configure.LanguageUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.network.NetworkUtil;
import com.nooie.common.utils.tool.ShellUtil;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.BannerResult;
import com.nooie.sdk.api.network.base.core.NetConfigure;
import com.nooie.sdk.api.network.message.MessageService;
import com.tuya.smart.api.service.MicroServiceManager;
import com.tuya.smart.commonbiz.bizbundle.family.api.AbsBizBundleFamilyService;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.DeviceAndGroupInRoomBean;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.bean.WeatherBean;
import com.tuya.smart.home.sdk.callback.IIGetHomeWetherSketchCallBack;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * HomePresenter
 *
 * @author Administrator
 * @date 2019/2/28
 */
public class HomePresenter implements IHomePresenter {

    private IHomeView homeView;
    private Map<Long, IGroupModel> mGroupModels = new HashMap<>();
    private Map<String, IDeviceModel> mDeviceModels = new HashMap<>();

    public HomePresenter(IHomeView view) {
        homeView = view;
    }

    @Override
    public void createDefaultHome(final int times) {
        FamilyManager.getInstance().getHomeList(new ITuyaGetHomeListCallback() {
            @Override
            public void onSuccess(List<HomeBean> list) {
                if (CollectionUtil.isEmpty(list)) {
                    List<String> rooms = new ArrayList<>();
                    rooms.add("room");
                    NooieLog.e("-----------> createDefaultHome ConstantValue.HOME_DEFALUT_NAME " + ConstantValue.HOME_DEFALUT_NAME);
                    FamilyManager.getInstance().createHome(ConstantValue.HOME_DEFALUT_NAME, rooms, new ITuyaHomeResultCallback() {
                        @Override
                        public void onSuccess(HomeBean homeBean) {
                            NooieLog.e("----------->createDefaultHome ConstantValue.HOME_DEFALUT_NAME onSuccess " + ConstantValue.HOME_DEFALUT_NAME);
                            AbsBizBundleFamilyService service = MicroServiceManager.getInstance().findServiceByInterface(AbsBizBundleFamilyService.class.getName());
                            //设置为当前家庭的homeId
                            service.setCurrentHomeId(homeBean.getHomeId());
                            FamilyManager.getInstance().setCurrentHome(homeBean);
                            loadHomes();
                            loadHomeDetail(homeBean.getHomeId());
                        }

                        @Override
                        public void onError(String s, String s1) {
                            NooieLog.e("------->>>> createDefaultHome createHome onError  code " + s + " msg " + s1);
                        }
                    });
                } else {
                    NooieLog.e("----------->createDefaultHome FamilyManager.getInstance().getCurrentHomeId() " + FamilyManager.getInstance().getCurrentHomeId());
                    if (FamilyManager.getInstance().getCurrentHomeId() != FamilyManager.DEFAULT_HOME_ID) {
                        for (HomeBean homeBean : list) {
                            if (homeBean.getHomeId() == FamilyManager.getInstance().getCurrentHomeId()) {
                                NooieLog.e("----------->createDefaultHome for FamilyManager.getInstance().getCurrentHomeId() " + FamilyManager.getInstance().getCurrentHomeId());
                                AbsBizBundleFamilyService service = MicroServiceManager.getInstance().findServiceByInterface(AbsBizBundleFamilyService.class.getName());
                                service.setCurrentHomeId(FamilyManager.getInstance().getCurrentHomeId());
                                loadHomeDetail(FamilyManager.getInstance().getCurrentHomeId());
                                loadHomes();
                                return;
                            }
                        }
                        NooieLog.e("----------->createDefaultHome list.get(0) " + list.get(0).getHomeId());
                        AbsBizBundleFamilyService service = MicroServiceManager.getInstance().findServiceByInterface(AbsBizBundleFamilyService.class.getName());
                        service.setCurrentHomeId(list.get(0).getHomeId());
                        FamilyManager.getInstance().setCurrentHome(list.get(0));
                        loadHomeDetail(list.get(0).getHomeId());
                        loadHomes();
                    } else {
                        NooieLog.e("----------->createDefaultHome  === list.get(0) " + list.get(0).getHomeId());
                        AbsBizBundleFamilyService service = MicroServiceManager.getInstance().findServiceByInterface(AbsBizBundleFamilyService.class.getName());
                        service.setCurrentHomeId(list.get(0).getHomeId());
                        FamilyManager.getInstance().setCurrentHome(list.get(0));
                        loadHomeDetail(list.get(0).getHomeId());
                        loadHomes();
                    }
                }
            }

            @Override
            public void onError(String errorCode, String errorMsg) {
                NooieLog.e("------------->createDefaultHome errorCode " + errorCode + " errorMsg " + errorMsg);
                if (times == 0) {
                    createDefaultHome(1);
                } else if (homeView != null) {
                    homeView.notifyLoadHomesFailed(errorMsg);
                }
            }
        });
    }

    @Override
    public void loadHomeDetail(final long homeId) {
        NooieLog.e("---------> loadHomeDetail  homeId " + homeId);
        if (homeId == FamilyManager.DEFAULT_HOME_ID) {
            return;
        }
        TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                NooieLog.e("--------->getHomeDetail loadHomeDetail onSuccess homeId " + homeBean.getHomeId());
                FamilyManager.getInstance().setCurrentHome(homeBean);
                if (homeView != null) {
                    homeView.loadHomeDetailSuccess(homeBean);
                }
            }

            @Override
            public void onError(String code, String msg) {
                if (homeView != null) {
                    NooieLog.e("--------->getHomeDetail loadHomeDetail code " + code + " msg " + msg + "  homeId " + homeId);
                    homeView.loadHomeDetailFailed(msg);
                }
            }
        });
    }

    @Override
    public void loadOnServerConnectHome(final long homeId) {
        NooieLog.e("---------> loadOnServerConnectHome loadHomeDetail  homeId " + homeId);
        if (homeId == FamilyManager.DEFAULT_HOME_ID) {
            return;
        }
        TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                NooieLog.e("--------->getHomeDetail loadOnServerConnectHome loadHomeDetail onSuccess homeId " + homeBean.getHomeId());
                FamilyManager.getInstance().setCurrentHome(homeBean);
                if (homeView != null) {
                    homeView.loadHomeDetailSuccess(homeBean);
                }
            }

            @Override
            public void onError(String code, String msg) {
                if (homeView != null) {
                    NooieLog.e("--------->getHomeDetail loadOnServerConnectHome code " + code + " msg " + msg + "  homeId " + homeId);
                }
            }
        });
    }

    @Override
    public void controlGroup(long groupId, boolean open) {
        SwitchBean switchBean = new SwitchBean(ConstantValue.DP_ID_SWITCH_ON);
        switchBean.setOpen(open);
        IGroupModel groupModel;
        if (mGroupModels.containsKey(groupId) && mGroupModels.get(groupId) != null) {
            groupModel = mGroupModels.get(groupId);
        } else {
            groupModel = new GroupModel(groupId);
            mGroupModels.put(groupId, groupModel);
        }

        groupModel.sendCommand(switchBean.getDpId(), switchBean.isOpen(), new IResultCallback() {
            @Override
            public void onError(String code, String msg) {
                NooieLog.e("---------->>. controlGroup onError code " + code + " msg " + msg);
                if (homeView != null) {
                    homeView.notifyControlGroupState(code);
                }
            }

            @Override
            public void onSuccess() {
                NooieLog.e("---------->>. controlGroup onSuccess ");
                if (homeView != null) {
                    homeView.notifyControlGroupState(ConstantValue.SUCCESS);
                }
            }
        });
    }

    @Override
    public void controlLampGroup(long groupId, String dpId, boolean open) {
        IGroupModel groupModel;
        if (mGroupModels.containsKey(groupId) && mGroupModels.get(groupId) != null) {
            groupModel = mGroupModels.get(groupId);
        } else {
            groupModel = new GroupModel(groupId);
            mGroupModels.put(groupId, groupModel);
        }
        groupModel.sendCommand(dpId, open, new IResultCallback() {
            @Override
            public void onError(String code, String msg) {
                if (homeView != null) {
                    homeView.notifyControlGroupState(code);
                }
            }

            @Override
            public void onSuccess() {
                if (homeView != null) {
                    homeView.notifyControlGroupState(ConstantValue.SUCCESS);
                }
            }
        });
    }

    @Override
    public void controlDevice(final String deviceId, boolean open) {
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
                if (homeView != null) {
                    homeView.notifyControlDeviceState(code, msg);
                }
            }

            @Override
            public void onSuccess() {
                if (homeView != null) {
                    homeView.notifyControlDeviceSuccess(ConstantValue.SUCCESS);
                }
            }
        });
    }

    @Override
    public void controlPowerStrip(String deviceId, String dpId, boolean open) {
        IDeviceModel deviceModel;
        if (mDeviceModels.containsKey(deviceId) && mDeviceModels.get(deviceId) != null) {
            deviceModel = mDeviceModels.get(deviceId);
        } else {
            deviceModel = new DeviceModel(deviceId);
            mDeviceModels.put(deviceId, deviceModel);
        }

        deviceModel.sendCommand(dpId, open, new IResultCallback() {
            @Override
            public void onError(String code, String msg) {
                if (homeView != null) {
                    homeView.notifyControlDeviceState(code, msg);
                }
            }

            @Override
            public void onSuccess() {
                if (homeView != null) {
                    homeView.notifyControlDeviceSuccess(ConstantValue.SUCCESS);
                }
            }
        });

    }

    //控制智能灯
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
                if (homeView != null) {
                    homeView.notifyControlDeviceState(code, msg);
                }
            }

            @Override
            public void onSuccess() {
                if (homeView != null) {
                    homeView.notifyControlDeviceSuccess(ConstantValue.SUCCESS);
                }
            }
        });
    }

    @Override
    public void release() {
        if (mGroupModels != null) {
            for (Map.Entry<Long, IGroupModel> groupModel : mGroupModels.entrySet()) {
                if (groupModel != null && groupModel.getValue() != null) {
                    groupModel.getValue().release();
                }
            }
            mGroupModels.clear();
        }

        if (mDeviceModels != null) {
            for (Map.Entry<String, IDeviceModel> groupModel : mDeviceModels.entrySet()) {
                if (groupModel != null && groupModel.getValue() != null) {
                    groupModel.getValue().release();
                }
            }
            mDeviceModels.clear();
        }
    }

    @Override
    public void loadHomes() {
        FamilyManager.getInstance().getHomeList(new ITuyaGetHomeListCallback() {
            @Override
            public void onSuccess(List<HomeBean> homes) {
                if (homeView != null) {
                    homeView.notifyLoadHomesSuccess(homes);
                }
            }

            @Override
            public void onError(String error, String msg) {
                if (homeView != null) {
                    homeView.notifyLoadHomesFailed(msg);
                }
            }
        });
    }

    @Override
    public void changeCurrentHome(final long homeId) {
        NooieLog.e("---------> changeCurrentHome  homeId " + homeId);
        FamilyManager.getInstance().updateCurrentHomeById(homeId, new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                NooieLog.e("---------> changeCurrentHome onSuccess homeId " + homeBean.getHomeId());
                if (homeView != null) {
                    homeView.notifyChangeHomeState(ConstantValue.SUCCESS);
                }
            }

            @Override
            public void onError(String code, String msg) {

                NooieLog.e("---------> changeCurrentHome onError code " + code + " msg " + msg + "  homeId " + homeId);
                if (homeView != null) {
                    homeView.notifyChangeHomeState(msg);
                }
            }
        });
    }

    @Override
    public void sortDevice(long roomId, List<DeviceAndGroupInRoomBean> list) {
        TuyaHomeSdk.newRoomInstance(roomId).sortDevInRoom(list, new IResultCallback() {
            @Override
            public void onError(String code, String msg) {
                if (homeView != null) {
                    homeView.notifyChangeHomeState(code);
                }
            }

            @Override
            public void onSuccess() {
                if (homeView != null) {
                    homeView.notifyChangeHomeState(ConstantValue.SUCCESS);
                }
            }
        });
    }

    @Override
    public void loadDeviceBean(String deviceId) {
        DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(deviceId);
        if (homeView != null) {
            homeView.notifyLoadDeviceSuccess(deviceId, deviceBean);
        }
    }

    @Override
    public void loadGroupBean(long groupId) {
        GroupBean groupBean = TuyaHomeSdk.getDataInstance().getGroupBean(groupId);
        if (homeView != null) {
            homeView.notifyLoadGroupSuccess(groupId, groupBean);
        }
    }

    @Override
    public void controlBrightness(String deviceId, String dpId, final int brightnessValue) {
        IDeviceModel deviceModel;
        if (mDeviceModels.containsKey(deviceId) && mDeviceModels.get(deviceId) != null) {
            deviceModel = mDeviceModels.get(deviceId);
        } else {
            deviceModel = new DeviceModel(deviceId);
            mDeviceModels.put(deviceId, deviceModel);
        }

        if (deviceModel != null) {
            deviceModel.sendCommand(dpId, brightnessValue, new IResultCallback() {
                @Override
                public void onError(String code, String msg) {

                    NooieLog.e("------>>>controlBrightness code " + code + "  msg " + msg);

                    if (homeView != null) {
                        homeView.notifyControlDeviceState(code, msg);
                    }
                }

                @Override
                public void onSuccess() {
                    NooieLog.e("------>>> onSuccess Control Brightness  " + brightnessValue);
                    if (homeView != null) {
                        homeView.notifyControlDeviceSuccess(ConstantValue.SUCCESS);
                    }
                }
            });
        }
    }

    /**
     * 改变消息的状态
     *
     * @param id
     * @param type
     */
    @Override
    public void updateMessageStatus(String id, int type) {

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
                    if (homeView != null) {
                        homeView.notifyControlDeviceState(code, error);
                    }
                }

                @Override
                public void onSuccess() {
                    if (homeView != null) {
                        homeView.notifyControlDeviceSuccess(ConstantValue.SUCCESS);
                    }
                }
            });
        }
    }

    @Override
    public void getHomeList() {
        FamilyManager.getInstance().getHomeList(new ITuyaGetHomeListCallback() {
            @Override
            public void onSuccess(List<HomeBean> list) {
                if (homeView != null) {
                    homeView.notifyLoadHomeListSuccess(ConstantValue.SUCCESS, list);
                }
            }

            @Override
            public void onError(String errorCode, String errorMsg) {
                NooieLog.e("------------->getHomeList errorCode " + errorCode + " errorMsg " + errorMsg);
                if (homeView != null) {
                    homeView.notifyLoadHomeListSuccess(errorMsg, null);

                }
            }
        });
    }

    @Override
    public void checkNetworkStatus() {
        Observable.just(1)
                .flatMap(new Function<Integer, ObservableSource<Boolean>>() {
                    @Override
                    public ObservableSource<Boolean> apply(Integer integer) throws Exception {
                        boolean isNetworkUsable = false;
                        boolean isConnected = NetworkUtil.isConnected(NooieApplication.mCtx);
                        if (isConnected) {
                            List<String> addresses = new ArrayList<>();
                            addresses.add("www.baidu.com");
                            addresses.add("www.google.com");
                            try {
                                for (String address : CollectionUtil.safeFor(addresses)) {
                                    ShellUtil.CommandResult result = NetworkUtil.pingAddressResult(address, 1);
                                    if (result != null && result.result == 0) {
                                        isNetworkUsable = true;
                                        break;
                                    }
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        NooieLog.d("-->> HomePresenter checkNetworkStatus isConnected=" + isConnected + " isNetworkUsable=" + isNetworkUsable);
                        return Observable.just(isNetworkUsable);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onError(Throwable e) {
                        if (homeView != null) {
                            homeView.onCheckNetworkStatus(ConstantValue.ERROR, false);
                        }
                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean isNetworkUsable) {
                        if (homeView != null) {
                            homeView.onCheckNetworkStatus(ConstantValue.SUCCESS, isNetworkUsable);
                        }
                    }
                });
    }

    @Override
    public void setWeather(double lon, double lat) {
        if (FamilyManager.getInstance().getCurrentHomeId() == FamilyManager.DEFAULT_HOME_ID) {
            return;
        }
        TuyaHomeSdk.newHomeInstance(FamilyManager.getInstance().getCurrentHomeId()).getHomeWeatherSketch(lon, lat, new IIGetHomeWetherSketchCallBack() {
            @Override
            public void onSuccess(WeatherBean result) {
                if (homeView != null) {
                    homeView.onGetWeatherSuccess(result);
                }
            }

            @Override
            public void onFailure(String errorCode, String errorMsg) {
                if (homeView != null) {
                    homeView.onGetWeatherFail(errorCode, errorMsg);
                }
            }
        });
    }

    @Override
    public void loadBanner() {
        MessageService.getService().getBannerList(ConstantValue.BANNER_PARAM_CODE, LanguageUtil.getLocal(NooieApplication.mCtx).getLanguage(), GlobalData.getInstance().getUid(), NetConfigure.getInstance().getAppId())
                .subscribeOn(rx.schedulers.Schedulers.io())
                .observeOn(rx.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(new rx.Observer<BaseResponse<BannerResult>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        NooieLog.d("-->> test banner e=" + e.toString());
                    }

                    @Override
                    public void onNext(BaseResponse<BannerResult> bannerResultBaseResponse) {
                        if (homeView != null && bannerResultBaseResponse.getCode() == StateCode.SUCCESS.code && bannerResultBaseResponse.getData() != null) {
                            homeView.onLoadBannerSuccess(bannerResultBaseResponse.getData().getContent_page_list());
                        } else {
                            NooieLog.d("-->> test banner bannerResultBaseResponse getCode " + bannerResultBaseResponse.getCode());
                        }
                    }
                });
    }
}
