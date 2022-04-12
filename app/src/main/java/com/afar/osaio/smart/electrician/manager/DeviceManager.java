package com.afar.osaio.smart.electrician.manager;

import com.afar.osaio.smart.electrician.eventbus.HomeLoadingEvent;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.api.ITuyaHome;
import com.tuya.smart.home.sdk.api.ITuyaHomeChangeListener;
import com.tuya.smart.home.sdk.api.ITuyaHomeManager;
import com.tuya.smart.home.sdk.api.ITuyaHomeStatusListener;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.sdk.api.IDevListener;
import com.tuya.smart.sdk.api.IGroupListener;
import com.tuya.smart.sdk.api.ITuyaDevice;
import com.tuya.smart.sdk.api.ITuyaGroup;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceManager {

    private List<DeviceBean> mListAllDevices = new ArrayList<>();
    private List<DeviceBean> mPlugDevices = new ArrayList<>();
    private List<DeviceBean> mLampDevices = new ArrayList<>();
    private List<GroupBean> mPlugGroups = new ArrayList<>();
    private List<GroupBean> mLampGroups = new ArrayList<>();
    private List<GroupBean> mListAllGroups = new ArrayList<>();
    private HashMap<String, ITuyaDevice> mDeviceListener = new HashMap<>();
    private HashMap<String, ITuyaGroup> mGroupListener = new HashMap<>();


    private volatile static DeviceManager instance;

    private DeviceManager() {
    }

    public static DeviceManager getInstance() {
        if (instance == null) {
            synchronized (DeviceManager.class) {
                if (instance == null) {
                    instance = new DeviceManager();
                }
            }
        }
        return instance;
    }

    public void syncGetDevList(HomeBean homeBean) {

        //清除数据
        mListAllDevices.clear();
        mPlugDevices.clear();
        mLampDevices.clear();
        mListAllGroups.clear();
        mPlugGroups.clear();
        mLampGroups.clear();

        unRegisterDeviceChangeListener();
        unRegisterGroupChangeListener();

        //从网络获取设备列表
        FamilyManager.getInstance().setCurrentHome(homeBean);
        List<DeviceBean> deviceList = homeBean.getDeviceList();
        List<DeviceBean> sharedDeviceList = homeBean.getSharedDeviceList();
        List<GroupBean> groupList = homeBean.getGroupList();
        List<GroupBean> sharedGroupList = homeBean.getSharedGroupList();
        mListAllDevices.addAll(deviceList);
        mListAllDevices.addAll(sharedDeviceList);
        mListAllGroups.addAll(groupList);
        mListAllGroups.addAll(sharedGroupList);

        //把HomeBean回调给AllFragment
        if (mAllHomeBeanCallback != null) {
            mAllHomeBeanCallback.callbackHomeBean(homeBean);
        }

        if (mDeviceHomeBeanCallback != null) {
            mDeviceHomeBeanCallback.callbackHomeBean(homeBean);
        }

        if (mGroupHomeBeanCallback != null) {
            mGroupHomeBeanCallback.callbackHomeBean(homeBean);
        }

        for (DeviceBean deviceBean : mListAllDevices) {

            if (PowerStripHelper.getInstance().isLamp(deviceBean)) {
                mLampDevices.add(deviceBean);
            } else {
                mPlugDevices.add(deviceBean);
            }

            ITuyaDevice mTuyaDevice;

            if (!mDeviceListener.containsKey(deviceBean.getDevId())) {
                mTuyaDevice = TuyaHomeSdk.newDeviceInstance(deviceBean.getDevId());
                mDeviceListener.put(deviceBean.getDevId(), mTuyaDevice);
                mTuyaDevice.registerDevListener(new IDevListener() {
                    @Override
                    public void onDpUpdate(String devId, String dpStr) {
                        EventBus.getDefault().post(new HomeLoadingEvent(ConstantValue.HOME_HIDE_LOADING));
                        if (mAllDeviceListenerCallBack != null) {
                            mAllDeviceListenerCallBack.onDpUpdate(devId, dpStr);
                        }
                        if (mGroupDeviceListenerCallBack != null) {
                            mGroupDeviceListenerCallBack.onDpUpdate(devId, dpStr);
                        }
                        if (mPlugDeviceListenerCallBack != null) {
                            mPlugDeviceListenerCallBack.onDpUpdate(devId, dpStr);
                        }
                        if (mLampDeviceListenerCallBack != null) {
                            mLampDeviceListenerCallBack.onDpUpdate(devId, dpStr);
                        }
                        if (mWidgetDeviceListenerCallBack != null) {
                            mWidgetDeviceListenerCallBack.onDpUpdate(devId, dpStr);
                        }
                        if (mPowerManagerListenerCallBack != null) {
                            mPowerManagerListenerCallBack.onDpUpdate(devId, dpStr);
                        }
                        if (mPowerManagerMonthListenerCallBack != null) {
                            mPowerManagerMonthListenerCallBack.onDpUpdate(devId, dpStr);
                        }
                    }

                    @Override
                    public void onRemoved(String devId) {
                        if (mAllDeviceListenerCallBack != null) {
                            mAllDeviceListenerCallBack.onRemoved(devId);
                        }
                        if (mGroupDeviceListenerCallBack != null) {
                            mGroupDeviceListenerCallBack.onRemoved(devId);
                        }
                        if (mPlugDeviceListenerCallBack != null) {
                            mPlugDeviceListenerCallBack.onRemoved(devId);
                        }
                        if (mLampDeviceListenerCallBack != null) {
                            mLampDeviceListenerCallBack.onRemoved(devId);
                        }
                        if (mWidgetDeviceListenerCallBack != null) {
                            mWidgetDeviceListenerCallBack.onRemoved(devId);
                        }
                        if (mPowerManagerListenerCallBack != null) {
                            mPowerManagerListenerCallBack.onRemoved(devId);
                        }
                        if (mPowerManagerMonthListenerCallBack != null) {
                            mPowerManagerMonthListenerCallBack.onRemoved(devId);
                        }
                    }

                    @Override
                    public void onStatusChanged(String devId, boolean online) {
                        if (mAllDeviceListenerCallBack != null) {
                            mAllDeviceListenerCallBack.onStatusChanged(devId, online);
                        }
                        if (mGroupDeviceListenerCallBack != null) {
                            mGroupDeviceListenerCallBack.onStatusChanged(devId, online);
                        }
                        if (mPlugDeviceListenerCallBack != null) {
                            mPlugDeviceListenerCallBack.onStatusChanged(devId, online);
                        }
                        if (mLampDeviceListenerCallBack != null) {
                            mLampDeviceListenerCallBack.onStatusChanged(devId, online);
                        }
                        if (mWidgetDeviceListenerCallBack != null) {
                            mWidgetDeviceListenerCallBack.onStatusChanged(devId, online);
                        }
                        if (mPowerManagerListenerCallBack != null) {
                            mPowerManagerListenerCallBack.onStatusChanged(devId, online);
                        }
                        if (mPowerManagerMonthListenerCallBack != null) {
                            mPowerManagerMonthListenerCallBack.onStatusChanged(devId, online);
                        }
                    }

                    @Override
                    public void onNetworkStatusChanged(String devId, boolean status) {
                        if (mAllDeviceListenerCallBack != null) {
                            mAllDeviceListenerCallBack.onNetworkStatusChanged(devId, status);
                        }
                        if (mGroupDeviceListenerCallBack != null) {
                            mGroupDeviceListenerCallBack.onNetworkStatusChanged(devId, status);
                        }
                        if (mPlugDeviceListenerCallBack != null) {
                            mPlugDeviceListenerCallBack.onNetworkStatusChanged(devId, status);
                        }
                        if (mLampDeviceListenerCallBack != null) {
                            mLampDeviceListenerCallBack.onNetworkStatusChanged(devId, status);
                        }
                        if (mWidgetDeviceListenerCallBack != null) {
                            mWidgetDeviceListenerCallBack.onNetworkStatusChanged(devId, status);
                        }
                        if (mPowerManagerListenerCallBack != null) {
                            mPowerManagerListenerCallBack.onNetworkStatusChanged(devId, status);
                        }
                        if (mPowerManagerMonthListenerCallBack != null) {
                            mPowerManagerMonthListenerCallBack.onNetworkStatusChanged(devId, status);
                        }
                    }

                    @Override
                    public void onDevInfoUpdate(String devId) {
                        if (mAllDeviceListenerCallBack != null) {
                            mAllDeviceListenerCallBack.onDevInfoUpdate(devId);
                        }
                        if (mGroupDeviceListenerCallBack != null) {
                            mGroupDeviceListenerCallBack.onDevInfoUpdate(devId);
                        }
                        if (mPlugDeviceListenerCallBack != null) {
                            mPlugDeviceListenerCallBack.onDevInfoUpdate(devId);
                        }
                        if (mLampDeviceListenerCallBack != null) {
                            mLampDeviceListenerCallBack.onDevInfoUpdate(devId);
                        }
                        if (mWidgetDeviceListenerCallBack != null) {
                            mWidgetDeviceListenerCallBack.onDevInfoUpdate(devId);
                        }
                        if (mPowerManagerListenerCallBack != null) {
                            mPowerManagerListenerCallBack.onDevInfoUpdate(devId);
                        }
                        if (mPowerManagerMonthListenerCallBack != null) {
                            mPowerManagerMonthListenerCallBack.onDevInfoUpdate(devId);
                        }
                    }
                });
            }
        }

        for (GroupBean groupBean : mListAllGroups) {
            if (GroupHelper.getInstance().isLampGroup(groupBean)) {
                mLampGroups.add(groupBean);
            } else {
                mPlugGroups.add(groupBean);
            }
            final ITuyaGroup mTuyaGroup;
            if (mGroupListener.containsKey(groupBean.getId() + "")) {

            } else {
                mTuyaGroup = TuyaHomeSdk.newGroupInstance(groupBean.getId());
                mGroupListener.put(groupBean.getId() + "", mTuyaGroup);
                mTuyaGroup.registerGroupListener(new IGroupListener() {
                    @Override
                    public void onDpUpdate(long groupId, String dps) {
                        EventBus.getDefault().post(new HomeLoadingEvent(ConstantValue.HOME_HIDE_LOADING));
                        if (mAllGroupListenerCallBack != null) {
                            mAllGroupListenerCallBack.onDpUpdate(groupId, dps);
                        }
                        if (mPlugGroupListenerCallBack != null) {
                            mPlugGroupListenerCallBack.onDpUpdate(groupId, dps);
                        }
                        if (mLightGroupListenerCallBack != null) {
                            mLightGroupListenerCallBack.onDpUpdate(groupId, dps);
                        }
                        if (mWidgetGroupListenerCallBack != null) {
                            mWidgetGroupListenerCallBack.onDpUpdate(groupId, dps);
                        }
                    }

                    @Override
                    public void onDpCodeUpdate(long groupId, Map<String, Object> dpCodeMap) {

                    }

                    @Override
                    public void onGroupInfoUpdate(long groupId) {
                        if (mAllGroupListenerCallBack != null) {
                            mAllGroupListenerCallBack.onGroupInfoUpdate(groupId);
                        }
                        if (mPlugGroupListenerCallBack != null) {
                            mPlugGroupListenerCallBack.onGroupInfoUpdate(groupId);
                        }
                        if (mLightGroupListenerCallBack != null) {
                            mLightGroupListenerCallBack.onGroupInfoUpdate(groupId);
                        }
                        if (mWidgetGroupListenerCallBack != null) {
                            mWidgetGroupListenerCallBack.onGroupInfoUpdate(groupId);
                        }
                    }

                    @Override
                    public void onGroupRemoved(long groupId) {
                        if (mAllGroupListenerCallBack != null) {
                            mAllGroupListenerCallBack.onGroupRemoved(groupId);
                        }
                        if (mPlugGroupListenerCallBack != null) {
                            mPlugGroupListenerCallBack.onGroupRemoved(groupId);
                        }
                        if (mLightGroupListenerCallBack != null) {
                            mLightGroupListenerCallBack.onGroupRemoved(groupId);
                        }
                        if (mWidgetGroupListenerCallBack != null) {
                            mWidgetGroupListenerCallBack.onGroupRemoved(groupId);
                        }
                    }
                });
            }
        }

        //插座数据列表
        if (mPlugDeviceListCallback != null) {
            mPlugDeviceListCallback.callBackPlugDeviceList(mPlugDevices, mPlugGroups);
        }

        //智能灯数据列表
        if (mLampDeviceListCallback != null) {
            mLampDeviceListCallback.callBackLampDeviceList(mLampDevices, mLampGroups);
        }
    }

    private void unRegisterDeviceChangeListener() {
        for (Map.Entry<String, ITuyaDevice> entry : mDeviceListener.entrySet()) {
            entry.getValue().unRegisterDevListener();
            entry.getValue().onDestroy();
        }
        mDeviceListener.clear();
    }

    private void unRegisterGroupChangeListener() {
        for (Map.Entry<String, ITuyaGroup> entry : mGroupListener.entrySet()) {
            entry.getValue().unRegisterGroupListener();
            entry.getValue().onDestroy();
        }
        mGroupListener.clear();
    }

    //------------------- homeBean start---------------------
    public IHomeBeanCallback mAllHomeBeanCallback;
    public IHomeBeanCallback mDeviceHomeBeanCallback;
    public IHomeBeanCallback mGroupHomeBeanCallback;

    public void setAllHomeBeanCallback(IHomeBeanCallback homeBeanCallback) {
        this.mAllHomeBeanCallback = homeBeanCallback;
    }

    public void setDeviceHomeBeanCallback(IHomeBeanCallback homeBeanCallback) {
        this.mDeviceHomeBeanCallback = homeBeanCallback;
    }

    public void setGroupHomeBeanCallback(IHomeBeanCallback homeBeanCallback) {
        this.mGroupHomeBeanCallback = homeBeanCallback;
    }

    public interface IHomeBeanCallback {
        void callbackHomeBean(HomeBean homeBean);
    }
    //------------------- homeBean end---------------------


    //--------------- 所有插座和插座群组  start-------------------------
    public IPlugDeviceListCallback mPlugDeviceListCallback;

    public void setPlugDeviceListCallback(IPlugDeviceListCallback iPlugDeviceListCallback) {
        this.mPlugDeviceListCallback = iPlugDeviceListCallback;
    }

    public interface IPlugDeviceListCallback {
        void callBackPlugDeviceList(List<DeviceBean> plugDevices, List<GroupBean> plugGroups);
    }
    //------------------------ 所有插座和插座群组  end-------------------------


    //--------------- 所有智能灯和灯群组  start-------------------------
    public ILampDeviceListCallback mLampDeviceListCallback;

    public void setLampDeviceListCallback(ILampDeviceListCallback iLampDeviceListCallback) {
        this.mLampDeviceListCallback = iLampDeviceListCallback;
    }

    public interface ILampDeviceListCallback {
        void callBackLampDeviceList(List<DeviceBean> lampDevices, List<GroupBean> lampGroups);
    }
    //------------------------ 所有智能灯和灯群组 end-------------------------


    // ------------------ 对单个设备的监听 start --------------
    public IDeviceListenerCallBack mAllDeviceListenerCallBack;
    public IDeviceListenerCallBack mGroupDeviceListenerCallBack;
    public IDeviceListenerCallBack mPlugDeviceListenerCallBack;
    public IDeviceListenerCallBack mLampDeviceListenerCallBack;
    public IDeviceListenerCallBack mWidgetDeviceListenerCallBack;
    public IDeviceListenerCallBack mPowerManagerListenerCallBack;
    public IDeviceListenerCallBack mPowerManagerMonthListenerCallBack;

    public void setAllDeviceListenerCallBack(IDeviceListenerCallBack callBack) {
        this.mAllDeviceListenerCallBack = callBack;
    }

    public void setGroupDeviceListenerCallBack(IDeviceListenerCallBack callBack) {
        this.mGroupDeviceListenerCallBack = callBack;
    }

    public void setPlugDeviceListenerCallBack(IDeviceListenerCallBack callBack) {
        this.mPlugDeviceListenerCallBack = callBack;
    }

    public void setLampDeviceListenerCallBack(IDeviceListenerCallBack callBack) {
        this.mLampDeviceListenerCallBack = callBack;
    }

    public void setWidgetDeviceListenerCallBack(IDeviceListenerCallBack callBack) {
        this.mWidgetDeviceListenerCallBack = callBack;
    }

    public void setPowerManagerListenerCallBack(IDeviceListenerCallBack callBack) {
        this.mPowerManagerListenerCallBack = callBack;
    }

    public void setPowerManagerMonthListenerCallBack(IDeviceListenerCallBack callBack) {
        this.mPowerManagerMonthListenerCallBack = callBack;
    }

    public interface IDeviceListenerCallBack {

        void onDpUpdate(String devId, String dpStr);

        void onRemoved(String devId);

        void onStatusChanged(String devId, boolean online);

        void onNetworkStatusChanged(String devId, boolean status);

        void onDevInfoUpdate(String devId);
    }
    // ------------------ 对单个设备的监听 end--------------


    //------------------  对群组的监听 start--------------------
    public IGroupListenerCallBack mAllGroupListenerCallBack;
    public IGroupListenerCallBack mPlugGroupListenerCallBack;
    public IGroupListenerCallBack mLightGroupListenerCallBack;
    public IGroupListenerCallBack mWidgetGroupListenerCallBack;

    public void setAllGroupListenerCallBack(IGroupListenerCallBack groupListenerCallBack) {
        this.mAllGroupListenerCallBack = groupListenerCallBack;
    }

    public void setPlugGroupListenerCallBack(IGroupListenerCallBack groupListenerCallBack) {
        this.mPlugGroupListenerCallBack = groupListenerCallBack;
    }

    public void setLightGroupListenerCallBack(IGroupListenerCallBack groupListenerCallBack) {
        this.mLightGroupListenerCallBack = groupListenerCallBack;
    }

    public void setWidgetGroupListenerCallBack(IGroupListenerCallBack groupListenerCallBack) {
        this.mWidgetGroupListenerCallBack = groupListenerCallBack;
    }

    public interface IGroupListenerCallBack {

        void onDpUpdate(long groupId, String dps);

        void onGroupInfoUpdate(long groupId);

        void onGroupRemoved(long groupId);

    }
    //------------------  对群组的监听 end--------------------


    //-------------------- 对Home的监听 start----------------------------

    private ITuyaHomeManager mITuyaHomemanager;

    private ITuyaHomeChangeListener mITuyaHomeChangeListener = new ITuyaHomeChangeListener() {
        @Override
        public void onHomeAdded(long homeId) {
            NooieLog.e("-----ITuyaHomeChangeListener homeId  onHomeAdded" + homeId);
            if (mHomeChangeListenerCallBack != null) {
                mHomeChangeListenerCallBack.onHomeAdded(homeId);
            }
            if (mWidgetHomeChangeListenerCallBack != null) {
                mWidgetHomeChangeListenerCallBack.onHomeAdded(homeId);
            }
        }

        @Override
        public void onHomeInvite(long homeId, String homeName) {
            NooieLog.e("-----ITuyaHomeChangeListener homeId  onHomeInvite" + homeId);
            if (mHomeChangeListenerCallBack != null) {
                mHomeChangeListenerCallBack.onHomeInvite(homeId, homeName);
            }
            if (mWidgetHomeChangeListenerCallBack != null) {
                mWidgetHomeChangeListenerCallBack.onHomeInvite(homeId, homeName);
            }
        }

        @Override
        public void onHomeRemoved(long homeId) {
            NooieLog.e("-----ITuyaHomeChangeListener homeId  onHomeRemoved" + homeId);
            if (mHomeChangeListenerCallBack != null) {
                mHomeChangeListenerCallBack.onHomeRemoved(homeId);
            }
            if (mWidgetHomeChangeListenerCallBack != null) {
                mWidgetHomeChangeListenerCallBack.onHomeRemoved(homeId);
            }
        }

        @Override
        public void onHomeInfoChanged(long homeId) {
            NooieLog.e("-----ITuyaHomeChangeListener homeId  onHomeInfoChanged" + homeId);
            if (mHomeChangeListenerCallBack != null) {
                mHomeChangeListenerCallBack.onHomeInfoChanged(homeId);
            }
            if (mWidgetHomeChangeListenerCallBack != null) {
                mWidgetHomeChangeListenerCallBack.onHomeInfoChanged(homeId);
            }
        }

        @Override
        public void onSharedDeviceList(List<DeviceBean> sharedDeviceList) {
            NooieLog.e("-----ITuyaHomeChangeListener homeId  onSharedDeviceList");
            if (mHomeChangeListenerCallBack != null) {
                mHomeChangeListenerCallBack.onSharedDeviceList(sharedDeviceList);
            }
            if (mWidgetHomeChangeListenerCallBack != null) {
                mWidgetHomeChangeListenerCallBack.onSharedDeviceList(sharedDeviceList);
            }
        }

        @Override
        public void onSharedGroupList(List<GroupBean> sharedGroupList) {
            if (mHomeChangeListenerCallBack != null) {
                mHomeChangeListenerCallBack.onSharedGroupList(sharedGroupList);
            }
            if (mWidgetHomeChangeListenerCallBack != null) {
                mWidgetHomeChangeListenerCallBack.onSharedGroupList(sharedGroupList);
            }
        }

        @Override
        public void onServerConnectSuccess() {
            NooieLog.e("-----ITuyaHomeChangeListener homeId  onServerConnectSuccess");
            if (mHomeChangeListenerCallBack != null) {
                mHomeChangeListenerCallBack.onServerConnectSuccess();
            }
            if (mWidgetHomeChangeListenerCallBack != null) {
                mWidgetHomeChangeListenerCallBack.onServerConnectSuccess();
            }
        }
    };

    public void registerHomeChangeListener() {
        if (mITuyaHomemanager != null) {
            mITuyaHomemanager.unRegisterTuyaHomeChangeListener(mITuyaHomeChangeListener);
            mITuyaHomemanager.onDestroy();
            mITuyaHomemanager = null;
            mITuyaHomemanager = TuyaHomeSdk.getHomeManagerInstance();
            mITuyaHomemanager.registerTuyaHomeChangeListener(mITuyaHomeChangeListener);
        } else {
            mITuyaHomemanager = TuyaHomeSdk.getHomeManagerInstance();
            mITuyaHomemanager.registerTuyaHomeChangeListener(mITuyaHomeChangeListener);
        }
    }


    public interface IHomeChangeListenerCallBack {
        void onHomeAdded(long homeId);

        void onHomeInvite(long homeId, String homeName);

        void onHomeRemoved(long homeId);

        void onHomeInfoChanged(long homeId);

        void onSharedDeviceList(List<DeviceBean> sharedDeviceList);

        void onSharedGroupList(List<GroupBean> sharedGroupList);

        void onServerConnectSuccess();
    }

    public IHomeChangeListenerCallBack mHomeChangeListenerCallBack;
    public IHomeChangeListenerCallBack mWidgetHomeChangeListenerCallBack;

    public void setHomeChangeListenerCallBack(IHomeChangeListenerCallBack iHomeChangeListenerCallBack) {
        this.mHomeChangeListenerCallBack = iHomeChangeListenerCallBack;
    }

    public void setWidgetHomeChangeListenerCallBack(IHomeChangeListenerCallBack iHomeChangeListenerCallBack) {
        this.mWidgetHomeChangeListenerCallBack = iHomeChangeListenerCallBack;
    }
    //-------------------- 对Home的监听 end----------------------------


    //-------------------- 对Home状态的监听 start----------------------------

    private ITuyaHome mITuyaHome;

    public void registerHomeStatusListener(long homeId) {
        if (homeId == 0 || homeId == FamilyManager.DEFAULT_HOME_ID) {
            return;
        }
        if (mITuyaHome != null) {
            mITuyaHome.unRegisterHomeStatusListener(mITuyaHomeStatusListener);
            mITuyaHome = null;
        }
        mITuyaHome = TuyaHomeSdk.newHomeInstance(homeId);
        mITuyaHome.registerHomeStatusListener(mITuyaHomeStatusListener);
    }

    private ITuyaHomeStatusListener mITuyaHomeStatusListener = new ITuyaHomeStatusListener() {
        @Override
        public void onDeviceAdded(String devId) {
            if (mHomeStatusListenerCallBack != null) {
                mHomeStatusListenerCallBack.onDeviceAdded(devId);
            }

            if (mWidgetHomeStatusListenerCallBack != null) {
                mWidgetHomeStatusListenerCallBack.onDeviceAdded(devId);
            }
        }

        @Override
        public void onDeviceRemoved(String devId) {
            if (mHomeStatusListenerCallBack != null) {
                mHomeStatusListenerCallBack.onDeviceRemoved(devId);
            }

            if (mWidgetHomeStatusListenerCallBack != null) {
                mWidgetHomeStatusListenerCallBack.onDeviceRemoved(devId);
            }
        }

        @Override
        public void onGroupAdded(long groupId) {
            if (mHomeStatusListenerCallBack != null) {
                mHomeStatusListenerCallBack.onGroupAdded(groupId);
            }

            if (mWidgetHomeStatusListenerCallBack != null) {
                mWidgetHomeStatusListenerCallBack.onGroupAdded(groupId);
            }
        }

        @Override
        public void onGroupRemoved(long groupId) {
            if (mHomeStatusListenerCallBack != null) {
                mHomeStatusListenerCallBack.onGroupRemoved(groupId);
            }

            if (mWidgetHomeStatusListenerCallBack != null) {
                mWidgetHomeStatusListenerCallBack.onGroupRemoved(groupId);
            }
        }

        @Override
        public void onMeshAdded(String meshId) {
            if (mHomeStatusListenerCallBack != null) {
                mHomeStatusListenerCallBack.onMeshAdded(meshId);
            }
            if (mWidgetHomeStatusListenerCallBack != null) {
                mWidgetHomeStatusListenerCallBack.onMeshAdded(meshId);
            }
        }
    };

    public IHomeStatusListenerCallBack mHomeStatusListenerCallBack;
    public IHomeStatusListenerCallBack mWidgetHomeStatusListenerCallBack;

    public void setHomeStatusListenerCallBack(IHomeStatusListenerCallBack iHomeStatusListenerCallBack) {
        this.mHomeStatusListenerCallBack = iHomeStatusListenerCallBack;
    }

    public void setWidgetHomeStatusListenerCallBack(IHomeStatusListenerCallBack iHomeStatusListenerCallBack) {
        this.mWidgetHomeStatusListenerCallBack = iHomeStatusListenerCallBack;
    }

    public interface IHomeStatusListenerCallBack {
        void onDeviceAdded(String devId);

        void onDeviceRemoved(String devId);

        void onGroupAdded(long groupId);

        void onGroupRemoved(long groupId);

        void onMeshAdded(String meshId);
    }

    //-------------------- 对Home状态的监听 end----------------------------


    //-------------------- 数据刷新请求数据结果的回调 start ------------------
    public IHomeRefreshListenerCallBack mAllHomeRefreshListenerCallBack;
    public IHomeRefreshListenerCallBack mPlugHomeRefreshListenerCallBack;
    public IHomeRefreshListenerCallBack mLightHomeRefreshListenerCallBack;

    public void setAllHomeRefreshListenerCallBack(IHomeRefreshListenerCallBack iHomeRefreshListenerCallBack) {
        this.mAllHomeRefreshListenerCallBack = iHomeRefreshListenerCallBack;
    }

    public void setPlugHomeRefreshListenerCallBack(IHomeRefreshListenerCallBack iHomeRefreshListenerCallBack) {
        this.mPlugHomeRefreshListenerCallBack = iHomeRefreshListenerCallBack;
    }

    public void setLightHomeRefreshListenerCallBack(IHomeRefreshListenerCallBack iHomeRefreshListenerCallBack) {
        this.mLightHomeRefreshListenerCallBack = iHomeRefreshListenerCallBack;
    }

    public interface IHomeRefreshListenerCallBack {

        void onFragmentRefresh();

        void onFragmentStopRefresh();
    }


    public void notifyFragmentStropRefresh() {
        NooieLog.e("----->>> notifyAllFragmentStropRefresh");
        if (mAllHomeRefreshListenerCallBack != null) {
            mAllHomeRefreshListenerCallBack.onFragmentStopRefresh();
        }
        if (mPlugHomeRefreshListenerCallBack != null) {
            mPlugHomeRefreshListenerCallBack.onFragmentStopRefresh();
        }
        if (mLightHomeRefreshListenerCallBack != null) {
            mLightHomeRefreshListenerCallBack.onFragmentStopRefresh();
        }
    }
    //-------------------- 数据刷新请求数据结果的回调 end ------------------


    //--------------------- 家庭切换的监听 start--------------------

    private IHomeChangedListener mHomeChangedListener;

    public interface IHomeChangedListener {

        void onHomeChangedSuccess();

    }

    public void setHomeChangedListener(IHomeChangedListener listener) {
        mHomeChangedListener = listener;
    }

    public void doHomeChangedSuccess() {
        if (mHomeChangedListener != null) {
            mHomeChangedListener.onHomeChangedSuccess();
        }
    }

    //--------------------- 家庭切换的监听 end--------------------


    //释放资源
    public void release() {
        if (mITuyaHome != null) {
            mITuyaHome.unRegisterHomeStatusListener(mITuyaHomeStatusListener);
            mITuyaHome = null;
        }
        if (mITuyaHomemanager != null) {
            mITuyaHomemanager.unRegisterTuyaHomeChangeListener(mITuyaHomeChangeListener);
        }
    }

    //----------------- 数据通知显示 start ---------------

    private IHomeDataListener mHomeFragmentDataListener;

    public interface IHomeDataListener {
        void notifyLoadHomesSuccess(List<HomeBean> homes);

        void notifyLoadHomeDetailSuccess(HomeBean homeBean);
    }

    public void setHomeFragChangedListener(IHomeDataListener listener) {
        mHomeFragmentDataListener = listener;
    }

    public void notifyLoadHomesSuccess(List<HomeBean> homes) {
        if (mHomeFragmentDataListener != null) {
            mHomeFragmentDataListener.notifyLoadHomesSuccess(homes);
        }
    }

    public void notifyLoadHomeDetailSuccess(HomeBean homeBean) {
        if (mHomeFragmentDataListener != null) {
            mHomeFragmentDataListener.notifyLoadHomeDetailSuccess(homeBean);
        }
    }

    //----------------- 数据通知显示 end ---------------

    public List<DeviceBean> getAllTuyaDevice() {
        return mListAllDevices;
    }

    public void clearTyDeviceList() {
        if (CollectionUtil.isNotEmpty(mListAllDevices)) {
            mListAllDevices.clear();
        }
    }
}

