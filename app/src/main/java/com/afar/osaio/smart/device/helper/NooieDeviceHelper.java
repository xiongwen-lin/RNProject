package com.afar.osaio.smart.device.helper;

import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.text.TextUtils;

import com.afar.osaio.R;
import com.afar.osaio.bean.ApDeviceInfo;
import com.afar.osaio.bean.BleApDeviceInfo;
import com.afar.osaio.bean.DeviceCompatibleVersion;
import com.afar.osaio.bean.SelectDeviceBean;
import com.afar.osaio.bean.SelectProduct;
import com.afar.osaio.bean.ShortLinkDeviceParam;
import com.afar.osaio.smart.cache.BleApDeviceInfoCache;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.nooie.common.utils.json.GsonHelper;
import com.nooie.common.utils.notify.NotificationUtil;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.data.EventDictionary;
import com.nooie.data.entity.external.ConnectionModeDNEventBean;
import com.nooie.data.entity.external.ConnectionResultDNEventBean;
import com.nooie.data.entity.external.DistributionNetworkEventBean;
import com.nooie.data.entity.external.NameDeviceDNEventBean;
import com.nooie.data.entity.external.SelectDeviceTypeDNEventBean;
import com.nooie.data.entity.external.SendApStateDNEventBean;
import com.nooie.data.entity.external.UuidRepeatDNEventBean;
import com.nooie.eventtracking.EventTrackingApi;
import com.nooie.sdk.api.network.base.bean.entity.TwoAuthDevice;
import com.nooie.sdk.cache.DeviceConnectionCache;
import com.nooie.common.base.GlobalData;
import com.nooie.sdk.db.entity.BleApDeviceEntity;
import com.nooie.sdk.db.entity.DeviceConfigureEntity;
import com.afar.osaio.widget.CalenderBean;
import com.google.firebase.iid.FirebaseInstanceId;
import com.afar.osaio.base.NooieApplication;
import com.nooie.sdk.bean.IpcType;
import com.afar.osaio.smart.device.bean.DeviceInfo;
import com.afar.osaio.smart.device.bean.ListDeviceItem;
import com.afar.osaio.smart.cache.DeviceInfoCache;
import com.afar.osaio.smart.cache.DeviceListCache;
import com.nooie.sdk.db.entity.DeviceEntity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ServiceUtils;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.data.DataHelper;
import com.nooie.common.utils.data.StringHelper;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.network.IPv4IntTransformer;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.api.network.base.bean.entity.GatewayDevice;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.device.DeviceCmdService;
import com.nooie.sdk.device.bean.AreaRect;
import com.nooie.sdk.device.bean.ConnType;
import com.nooie.sdk.device.bean.DeviceConnInfo;
import com.nooie.sdk.device.bean.ICRMode;
import com.nooie.sdk.device.bean.MTAreaInfo;
import com.nooie.sdk.device.bean.hub.IRMode;
import com.nooie.sdk.helper.DeviceHelper;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;
import com.nooie.sdk.processor.device.DeviceApi;
import com.scenery7f.timeaxis.model.RecordType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * NooieDeviceHelper
 *
 * @author Administrator
 * @date 2019/4/13
 */
public class NooieDeviceHelper {

    private final static int LP_POWER_MIN = 3400;
    private final static int LP_POWER_MAX = 4100;
    private final static int LP_POWER_RANGE_UP = 4300;
    private final static int LP_POWER_RANGE_DOWN = 0;

    private static final int LOW_BATTERY_VALUE = 3600;
    private static final int POWER_RANGE_UP_MC_120 = 4200;
    private static final int POWER_RANGE_DOWN_MC_120 = 3400;
    private static final int POWER_RANGE_UP_HC_320 = 6000;
    private static final int POWER_RANGE_DOWN_HC_320 = 4500;
    private final static int EXTERNAL_POWER_HC_320 = 4500;

    private static String mDistributeNetworkId = null;
    private static Map<String, String> mModelNameMap = null;

    public static Map<String, ListDeviceItem> savedHistoryDevices(List<ListDeviceItem> devices) {
        Map<String, ListDeviceItem> historyDevices = new HashMap<>();
        for (ListDeviceItem device : CollectionUtil.safeFor(devices)) {
            if (device.getDevicePlatform() == ListDeviceItem.DEVICE_PLATFORM_DANA) {
                historyDevices.put(device.getDeviceId(), device);
            }
        }

        return historyDevices;
    }

    public static List<ListDeviceItem> convertNooieDevice(List<BindDevice> bindDevices) {

        List<ListDeviceItem> devices = new ArrayList<>();
        for (BindDevice device : CollectionUtil.safeFor(bindDevices)) {
            devices.add(new ListDeviceItem(device));
        }

        return devices;
    }

    public static List<ListDeviceItem> convertDeviceFromCache(List<DeviceEntity> deviceEntities) {

        List<ListDeviceItem> devices = new ArrayList<>();
        for (DeviceEntity deviceEntity : CollectionUtil.safeFor(deviceEntities)) {
            if (deviceEntity != null) {
                devices.add(new ListDeviceItem(convertBindDevice(deviceEntity)));
            }
        }
        return devices;
    }

    public static BindDevice convertBindDevice(DeviceEntity deviceEntity) {
        BindDevice bindDevice = new BindDevice();
        if (deviceEntity != null) {
            bindDevice.setId(deviceEntity.getBindId());
            bindDevice.setBind_type(deviceEntity.getBindType());
            bindDevice.setAccount(deviceEntity.getAccount());
            bindDevice.setNickname(deviceEntity.getNickname());
            bindDevice.setUid(deviceEntity.getUid());
            bindDevice.setName(deviceEntity.getName());
            bindDevice.setUuid(deviceEntity.getDeviceId());
            bindDevice.setSn(deviceEntity.getSn());
            bindDevice.setMac(deviceEntity.getMac());
            bindDevice.setVersion(deviceEntity.getVersion());
            bindDevice.setLocal_ip(deviceEntity.getLocalIp());
            bindDevice.setWanip(deviceEntity.getWanIp());
            bindDevice.setType(deviceEntity.getModel());
            bindDevice.setOnline(deviceEntity.getOnline());
            bindDevice.setTime(deviceEntity.getTime());
            bindDevice.setHb_domain(deviceEntity.getHbDomain());
            bindDevice.setHb_server(deviceEntity.getHbServer());
            bindDevice.setHb_port(deviceEntity.getHbPort());
            bindDevice.setOpen_status(deviceEntity.getOpenStatus());
            bindDevice.setZone(deviceEntity.getZone());
            bindDevice.setModel(deviceEntity.getModel());
            bindDevice.setRegion(deviceEntity.getRegion());
            bindDevice.setSort(deviceEntity.getSort());

            bindDevice.setIs_google(deviceEntity.getIs_google());
            bindDevice.setIs_alexa(deviceEntity.getIs_alexa());
            bindDevice.setPuuid(deviceEntity.getPuuid());
            bindDevice.setWifi_level(deviceEntity.getWifi_level());
            bindDevice.setBattery_level(deviceEntity.getBattery_level());
            bindDevice.setP_model(deviceEntity.getPModel());
            bindDevice.setP_version(deviceEntity.getPVersion());
            bindDevice.setModel_type(deviceEntity.getModelType());
            bindDevice.setSecret(deviceEntity.getSecret());
            bindDevice.setIs_theft(deviceEntity.getIsTheft());
            bindDevice.setIs_notice(deviceEntity.getIsNotice());
        }
        return bindDevice;
    }

    public static List<BindDevice> convertBindDevices(List<DeviceEntity> deviceEntities) {
        List<BindDevice> devices = new ArrayList<>();
        for (DeviceEntity deviceEntity : CollectionUtil.safeFor(deviceEntities)) {
            if (deviceEntity != null) {
                devices.add(convertBindDevice(deviceEntity));
            }
        }
        return devices;
    }

    public static List<ListDeviceItem> convertListDeviceItem(List<DeviceInfo> deviceInfos) {
        List<ListDeviceItem> devices = new ArrayList<>();
        for (DeviceInfo device : CollectionUtil.safeFor(deviceInfos)) {
            devices.add(new ListDeviceItem(device));
        }

        return sortDevices(devices);
    }

    public static List<String> getNooieDeviceIds(List<BindDevice> devices) {
        List<String> deviceIds = new ArrayList<>();
        for (BindDevice device : CollectionUtil.safeFor(devices)) {
            if (!TextUtils.isEmpty(device.getUuid())) {
                deviceIds.add(device.getUuid());
            }
        }
        return deviceIds;
    }

    public static List<String> getListDeviceItemIds(List<ListDeviceItem> devices) {
        List<String> deviceIds = new ArrayList<>();
        for (ListDeviceItem device : CollectionUtil.safeFor(devices)) {
            if (!TextUtils.isEmpty(device.getDeviceId())) {
                deviceIds.add(device.getDeviceId());
            }
        }
        return deviceIds;
    }

    public static Map<String, ListDeviceItem> getDevicesWithIds(List<ListDeviceItem> devices) {
        Map<String, ListDeviceItem> deviceMap = new HashMap<>();
        for (ListDeviceItem device : CollectionUtil.safeFor(devices)) {
            if (!TextUtils.isEmpty(device.getDeviceId())) {
                deviceMap.put(device.getDeviceId(), device);
            }
        }
        return deviceMap;
    }

    public static List<ListDeviceItem> filterDevices(List<ListDeviceItem> devices, int platform) {
        List<ListDeviceItem> filterDevices = new ArrayList<>();
        for (ListDeviceItem device : devices) {
            if (device.getDevicePlatform() == platform) {
                filterDevices.add(device);
            }
        }
        return filterDevices;
    }

    public static void updateDevices(List<ListDeviceItem> targetDevices, List<ListDeviceItem> devices) {
        Map<String, ListDeviceItem> deviceMap = getDevicesWithIds(devices);
        for (ListDeviceItem targetDevice : targetDevices) {
            if (deviceMap.containsKey(targetDevice.getDeviceId())) {
                targetDevice.updateItem(deviceMap.get(targetDevice.getDeviceId()));
            }
        }
    }

    public static List<ListDeviceItem> updateListDeviceItems(List<ListDeviceItem> currentDevices, List<ListDeviceItem> newDevices) {
        List<String> currentDeviceIds = getListDeviceItemIds(currentDevices);
        for (ListDeviceItem device : CollectionUtil.safeFor(newDevices)) {
            if (device != null && currentDeviceIds.contains(device.getDeviceId())) {
                for (int i = 0; i < currentDeviceIds.size(); i++) {
                    if (currentDevices.get(i) != null && !TextUtils.isEmpty(currentDevices.get(i).getDeviceId()) && currentDevices.get(i).getDeviceId().equalsIgnoreCase(device.getDeviceId())) {
                        currentDevices.get(i).replaceItem(device);
                    }
                }
            } else {
                currentDevices.add(device);
            }
        }
        return currentDevices;
    }

    public static List<ListDeviceItem> sortDevices(List<ListDeviceItem> deviceItems) {
        Collections.sort(deviceItems, new Comparator<ListDeviceItem>() {
            @Override
            public int compare(ListDeviceItem item1, ListDeviceItem item2) {
                int sort1 = item1 != null && item1.getBindDevice() != null ? item1.getBindDevice().getSort() : 0;
                int sort2 = item2 != null && item2.getBindDevice() != null ? item2.getBindDevice().getSort() : 0;
                return sort1 > sort2 ? 1 : -1;
            }
        });

        return deviceItems;
    }

    public static List<DeviceInfo> sortDeviceInfo(List<DeviceInfo> deviceItems) {
        Collections.sort(deviceItems, new Comparator<DeviceInfo>() {
            @Override
            public int compare(DeviceInfo item1, DeviceInfo item2) {
                int sort1 = item1 != null && item1.getNooieDevice() != null ? item1.getNooieDevice().getSort() : 0;
                int sort2 = item2 != null && item2.getNooieDevice() != null ? item2.getNooieDevice().getSort() : 0;
                return sort1 > sort2 ? 1 : -1;
            }
        });

        return deviceItems;
    }

    public static List<BleApDeviceEntity> sortBleApDevices(List<BleApDeviceEntity> bleApDeviceEntityList) {
        Collections.sort(bleApDeviceEntityList, new Comparator<BleApDeviceEntity>() {
            @Override
            public int compare(BleApDeviceEntity item1, BleApDeviceEntity item2) {
                int sort1 = item1 != null ? item1.getSort() : 0;
                int sort2 = item2 != null ? item2.getSort() : 0;
                return sort1 > sort2 ? 1 : -1;
            }
        });
        return bleApDeviceEntityList;
    }

    public static List<ListDeviceItem> sortListDeviceItem(List<ListDeviceItem> deviceItems) {
        Collections.sort(deviceItems, new Comparator<ListDeviceItem>() {
            @Override
            public int compare(ListDeviceItem item1, ListDeviceItem item2) {
                /*置顶功能逻辑,暂时关闭,赋予空值,实现再打开
                String lDeviceArea = item1.getOrder();
                String rDeviceArea = item2.getOrder();
                */
                String lDeviceArea = null;
                String rDeviceArea = null;
                if (!TextUtils.isEmpty(lDeviceArea) && TextUtils.isEmpty(rDeviceArea)) {
                    return 1;
                } else if (TextUtils.isEmpty(lDeviceArea) && !TextUtils.isEmpty(rDeviceArea)) {
                    return -1;
                } else if (!TextUtils.isEmpty(lDeviceArea) && !TextUtils.isEmpty(rDeviceArea)) {
                    return lDeviceArea.compareToIgnoreCase(rDeviceArea);
                } else {
                    int lhsOnlineType = item1.getOnline();
                    int rhsOnlineType = item2.getOnline();
                    boolean lhsOnline = lhsOnlineType == ApiConstant.ONLINE_STATUS_ON ? true : false;
                    boolean rhsOnline = rhsOnlineType == ApiConstant.ONLINE_STATUS_ON ? true : false;
                    boolean lhsIsMyDev = item1.getBindType() == ApiConstant.BIND_TYPE_OWNER ? true : false;
                    boolean rhsIsMyDev = item2.getBindType() == ApiConstant.BIND_TYPE_OWNER ? true : false;
                    String ldeviceId = item1.getDeviceId();
                    String rdeviceId = item2.getDeviceId();
                    if (lhsOnline && !rhsOnline) {
                        return -1;
                    } else if (!lhsOnline && rhsOnline) {
                        return 1;
                    } else if (lhsIsMyDev && !rhsIsMyDev) {
                        return -1;
                    } else {
                        return !lhsIsMyDev && rhsIsMyDev ? 1 : ldeviceId.compareToIgnoreCase(rdeviceId);
                    }
                }
            }
        });

        return deviceItems;
    }

    public static void updateDeviceOpenStatus(final String deviceId, final int openStatus) {
        Observable.just("")
                .flatMap(new Func1<String, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(String s) {
                        DeviceInfo deviceInfo = NooieDeviceHelper.getDeviceInfoById(deviceId);
                        if (deviceInfo != null && deviceInfo.getNooieDevice() != null) {
                            deviceInfo.getNooieDevice().setOpen_status(openStatus);
                            DeviceInfoCache.getInstance().updateCache(deviceInfo);
                        }
                        ListDeviceItem deviceItem = DeviceListCache.getInstance().getCacheById(deviceId);
                        if (deviceItem != null) {
                            deviceItem.updateOpenCameraDe(openStatus == ApiConstant.OPEN_STATUS_ON);
                            deviceItem.setOpenStatus(deviceItem.isopenCamera() ? ApiConstant.OPEN_STATUS_ON : ApiConstant.OPEN_STATUS_OFF);
                            DeviceListCache.getInstance().updateDevice(deviceItem);
                        }
                        //DeviceCacheService.getInstance().updateOpenStatus(GlobalData.getInstance().getAccount(), deviceId, openStatus);
                        DeviceApi.getInstance().updateDeviceOpenStatus(false, GlobalData.getInstance().getAccount(), deviceId, ListDeviceItem.DEVICE_PLATFORM_NOOIE, openStatus);
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

    public static void updateDeviceSort(String deviceId, int sort) {
        DeviceInfo deviceInfo = getDeviceInfoById(deviceId);
        if (deviceInfo != null && deviceInfo.getNooieDevice() != null) {
            deviceInfo.getNooieDevice().setSort(sort);
            DeviceInfoCache.getInstance().updateCache(deviceInfo);
        }
        ListDeviceItem deviceItem = DeviceListCache.getInstance().getCacheById(deviceId);
        if (deviceItem != null && deviceItem.getBindDevice() != null) {
            deviceItem.getBindDevice().setSort(sort);
            DeviceListCache.getInstance().updateDevice(deviceItem);
        }
    }

    public static void updateDeviceName(String deviceId, String name) {
        DeviceInfo deviceInfo = NooieDeviceHelper.getDeviceInfoById(deviceId);
        if (deviceInfo != null && deviceInfo.getNooieDevice() != null) {
            deviceInfo.getNooieDevice().setName(name);
            DeviceInfoCache.getInstance().updateCache(deviceInfo);
        }

        ListDeviceItem deviceItem = DeviceListCache.getInstance().getCacheById(deviceId);
        if (deviceItem != null && deviceItem.getBindDevice() != null) {
            deviceItem.setName(name);
            deviceItem.getBindDevice().setName(name);
            DeviceListCache.getInstance().updateDevice(deviceItem);
        }
    }

    public static List<TwoAuthDevice> sortTwoAuthDevices(List<TwoAuthDevice> devices) {
        if (CollectionUtil.isEmpty(devices)) {
            return devices;
        }
        Collections.sort(devices, new Comparator<TwoAuthDevice>() {
            @Override
            public int compare(TwoAuthDevice item1, TwoAuthDevice item2) {
                long sort1 = item1 != null ? item1.getLast_login_time() : 0;
                long sort2 = item2 != null ? item2.getLast_login_time() : 0;
                return sort1 > sort2 ? -1 : 1;
            }
        });
        return devices;
    }

    public static boolean isOwnerDevice(String deviceId) {
        DeviceInfo deviceInfo = NooieDeviceHelper.getDeviceInfoById(deviceId);
        if (deviceInfo == null) {
            return false;
        }
        return deviceInfo.getNooieDevice() != null && deviceInfo.getNooieDevice().getBind_type() == ApiConstant.BIND_TYPE_OWNER;
    }

    public static boolean isUseJPush(String region) {
        //return ConstantValue.FORCE_USE_JPUSH || ConstantValue.NOOIE_AREA_CN.equalsIgnoreCase(region) || !(ServiceUtils.isGooglePlayServicesAvailable() && !TextUtils.isEmpty(FirebaseInstanceId.getInstance().getToken()));
        return ConstantValue.FORCE_USE_JPUSH || !(ServiceUtils.isGooglePlayServicesAvailable() && !TextUtils.isEmpty(FirebaseInstanceId.getInstance().getToken()));
    }

    public static DeviceInfo getDeviceInfoById(String deviceId) {
        ListDeviceItem deviceItem = DeviceListCache.getInstance().getCacheById(deviceId);
        DeviceInfo deviceInfo = null;
        if (deviceItem != null) {
            deviceInfo = convertDeviceInfo(deviceItem);
        }
        return deviceInfo;
    }

    public static BindDevice getDeviceById(String deviceId) {
        ListDeviceItem deviceItem = DeviceListCache.getInstance().getCacheById(deviceId);
        if (deviceItem != null && deviceItem.getBindDevice() != null) {
            return deviceItem.getBindDevice();
        }
        return null;
    }

    public static List<DeviceInfo> getAllDeviceInfo() {
        List<DeviceInfo> deviceInfos = new ArrayList<>();
        List<ListDeviceItem> deviceItems = DeviceListCache.getInstance().getAllCache();
        for (ListDeviceItem deviceItem : deviceItems) {
            DeviceInfo deviceInfo = null;
            if (deviceItem != null) {
                deviceInfo = convertDeviceInfo(deviceItem);
            }
            if (deviceInfo != null) {
                deviceInfos.add(deviceInfo);
            }
        }

        return deviceInfos;
    }

    public static List<BindDevice> getAllBindDevice() {
        List<BindDevice> bindDevices = new ArrayList<>();
        for (ListDeviceItem deviceItem : CollectionUtil.safeFor(DeviceListCache.getInstance().getAllCache())) {
            if (deviceItem != null && deviceItem.getBindDevice() != null) {
                bindDevices.add(deviceItem.getBindDevice());
            }
        }
        return bindDevices;
    }

    public static BindDevice getDeviceByConnectionMode(int connectionMode, String deviceId, String defaultModel) {
        if (connectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            BleApDeviceInfo bleApDeviceInfo = BleApDeviceInfoCache.getInstance().getCacheById(deviceId);
            BleApDeviceEntity bleApDeviceEntity = bleApDeviceInfo != null ? bleApDeviceInfo.getBleApDeviceEntity() : null;
            BindDevice device = bleApDeviceInfo != null ? bleApDeviceInfo.getBindDevice() : null;
            if (device == null) {
                String model = bleApDeviceEntity != null ? bleApDeviceEntity.getModel() : new String();
                if (TextUtils.isEmpty(model)) {
                    model = defaultModel;
                }
                device = ApHelper.getInstance().getDevice(deviceId, model);
            }
            ApDeviceInfo apDeviceInfo = ApHelper.getInstance().getCurrentApDeviceInfo();
            if (apDeviceInfo != null && apDeviceInfo.getBindDevice() != null && IpcType.MC120 == NooieDeviceHelper.mergeIpcType(apDeviceInfo.getBindDevice().getType())) {
                device.setOpen_status(apDeviceInfo.getBindDevice().getOpen_status());
            }
            return device;
        } else {
            return NooieDeviceHelper.getDeviceById(deviceId);
        }
    }

    public static DeviceInfo convertDeviceInfo(ListDeviceItem deviceItem) {
        if (deviceItem == null) {
            return null;
        }

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setNooieDevice(deviceItem.getBindDevice());
        deviceInfo.setLoopRecordStatus(true);
        deviceInfo.setDeviceId(deviceItem.getDeviceId());
        deviceInfo.setVersionCode(deviceItem.getVersion());
        deviceInfo.setModel(deviceItem.getModel());
        deviceInfo.setOpenCamera(deviceItem.isopenCamera());
        deviceInfo.setOpenCloud(deviceItem.isOpenCloud());
        deviceInfo.setCloudTime(deviceItem.getCloudTime());

        return deviceInfo;
    }

    public static List<ApDeviceInfo> filterApDeviceInfoList(List<ApDeviceInfo> apDeviceInfoList) {
        if (CollectionUtil.isEmpty(apDeviceInfoList)) {
            return apDeviceInfoList;
        }
        Iterator<ApDeviceInfo> infoIterator = apDeviceInfoList.iterator();
        while (infoIterator.hasNext()) {
            ApDeviceInfo apDeviceInfo = infoIterator.next();
            if (apDeviceInfo == null || apDeviceInfo.getBindDevice() == null) {
                infoIterator.remove();
            }
        }
        return apDeviceInfoList;
    }

    public static boolean checkBleApDeviceEntityValid(BleApDeviceEntity bleApDeviceEntity) {
        return bleApDeviceEntity != null && !TextUtils.isEmpty(bleApDeviceEntity.getDeviceId()) && !TextUtils.isEmpty(bleApDeviceEntity.getModel()) && !TextUtils.isEmpty(bleApDeviceEntity.getSsid());
    }

    public static boolean checkBleApDeviceValid(String deviceId, String model, String ssid) {
        return !TextUtils.isEmpty(deviceId) && !TextUtils.isEmpty(model) && !TextUtils.isEmpty(ssid);
    }

    public static int convertNooieModel(IpcType type, String originalType) {
        NooieLog.d("-->> NooieDeviceHelper convertNooieModel type=" + type.getType() + " originalType=" + originalType);
        return DeviceHelper.convertNooieModel(type, originalType);
    }

    public static int convertNooieModelForCloudPlayback(IpcType type, String originalType) {
        NooieLog.d("-->> NooieDeviceHelper convertNooieModel type=" + type.getType() + " originalType=" + originalType);
        if (type == IpcType.W2 || type == IpcType.EC810_PLUS) {
            return Constant.MODEL_TYPE_W2;
        } else {
            return DeviceHelper.convertNooieModel(type, originalType);
        }

    }

    public static int convertNooieModelForSdPlayback(IpcType type, String originalType) {
        NooieLog.d("-->> NooieDeviceHelper convertNooieModel type=" + type.getType() + " originalType=" + originalType);
        return DeviceHelper.convertNooieModel(type, originalType);
    }

    public static IpcType mergeIpcType(IpcType ipcType) {
        if (ipcType == IpcType.PC420 || ipcType == IpcType.PC440 || ipcType == IpcType.C1 || ipcType == IpcType.C1PRO || ipcType == IpcType.A1 || ipcType == IpcType.TC100 || ipcType == IpcType.XC100) {
            return IpcType.PC420;
        } else if (ipcType == IpcType.PC530 || ipcType == IpcType.PC540 || ipcType == IpcType.PC650 || ipcType == IpcType.SC210 || ipcType == IpcType.SC220 || ipcType == IpcType.SC100 || ipcType == IpcType.PC530PRO || ipcType == IpcType.PC660 || ipcType == IpcType.PC660PRO
                || ipcType == IpcType.P3 || ipcType == IpcType.PC530PRO || ipcType == IpcType.P1 || ipcType == IpcType.P1PRO || ipcType == IpcType.P2 || ipcType == IpcType.P4 || ipcType == IpcType.K1 || ipcType == IpcType.K1PRO || ipcType == IpcType.K2
                || ipcType == IpcType.TR100 || ipcType == IpcType.TS200) {
            return IpcType.PC530;
        } else if (ipcType == IpcType.PC730 || ipcType == IpcType.PC770 || ipcType == IpcType.Q1 || ipcType == IpcType.T1 || ipcType == IpcType.T1PRO || ipcType == IpcType.T2 || ipcType == IpcType.TS100) {
            return IpcType.PC730;
        } else if (ipcType == IpcType.MC120 || ipcType == IpcType.M1) {
            return IpcType.MC120;
        } else if (ipcType == IpcType.EC810_CAM || ipcType == IpcType.W0_CAM) {
            return IpcType.EC810_CAM;
        } else if (ipcType == IpcType.EC810PRO || ipcType == IpcType.W1) {
            return IpcType.EC810PRO;
        } else if (ipcType == IpcType.EC810_PLUS || ipcType == IpcType.W2) {
            return IpcType.EC810_PLUS;
        } else if (ipcType == IpcType.HC320) {
            return IpcType.HC320;
        } else {
            return ipcType;
        }
    }

    public static IpcType mergeIpcType(String model) {
        return mergeIpcType(IpcType.getIpcType(model));
    }

    public static String convertModelToString(String type) {
        initModelNameMap();
        String model = IpcType.getIpcType(type).getType();
        if (mModelNameMap != null && mModelNameMap.containsKey(model)) {
            return mModelNameMap.get(model);
        }

        if (IpcType.getIpcType(type) == IpcType.PC530 || IpcType.getIpcType(type) == IpcType.PC540) {
            return ConstantValue.MODEL_VALUE_OF_530_540;
        } else if (IpcType.getIpcType(type) != IpcType.IPC_UNKNOWN) {
            return IpcType.getIpcType(type).getType();
        } else {
            return type;
        }
    }

    public static int convertIconByModel(IpcType type) {
        if (type == IpcType.PC420) {
            return R.drawable.device_small_icon;
        } else if (type == IpcType.PC530 || type == IpcType.PC540 || type == IpcType.PC650 || type == IpcType.SC210 || type == IpcType.SC220) {
            return R.drawable.device_small_icon_360;
        } else if (type == IpcType.PC730) {
            return R.drawable.device_small_icon_outdoor;
        } else if (type == IpcType.EC810_CAM) {
            return R.drawable.device_small_icon_lp_810;
        } else {
            return R.drawable.device_small_icon;
        }
    }

    public static int convertProductCategoryByType(int productType) {
        if (productType == ConstantValue.PRODUCT_TYPE_CARD) {
            return R.string.add_camera_product_category_card;
        } else if (productType == ConstantValue.PRODUCT_TYPE_HEAD) {
            return R.string.add_camera_product_category_head;
        } else if (productType == ConstantValue.PRODUCT_TYPE_GUN) {
            return R.string.add_camera_product_category_gun;
        } else if (productType == ConstantValue.PRODUCT_TYPE_LOW_POWER) {
            return R.string.add_camera_product_category_lp;
        } else {
            return R.string.add_camera_product_category_card;
        }
    }

    public static int convertStreamType(IpcType type, String originalType) {
        return Constant.VID_STREAM_MAIN;
    }

    public static int compateSdStatus(int status) {
        if (status == -1 || status == 65535) {
            return ConstantValue.NOOIE_SD_STATUS_NO_SD;
        }
        return status;
    }

    public static int compateHubSdStatus(int status) {
        if (status == -1 || status == 65535) {
            return ConstantValue.HUB_SD_STATUS_NO_SD;
        }
        return status;
    }

    public static boolean isHasSdCard(int status) {
        return status == ConstantValue.NOOIE_SD_STATUS_NORMAL || status == ConstantValue.NOOIE_SD_STATUS_FORMATING;
    }

    public static boolean isHasHubSdCard(int status) {
        return status == ConstantValue.HUB_SD_STATUS_NORMAL || status == ConstantValue.HUB_SD_STATUS_FORMATING;
    }

    public static int getFormatStatus(DeviceConfigureEntity configureEntity) {
        if (configureEntity == null) {
            return ConstantValue.NOOIE_SD_STATUS_NO_SD;
        }
        int formatStatus = configureEntity.getFormatStatus();
        if (formatStatus == ConstantValue.NOOIE_SD_STATUS_NORMAL) {
            formatStatus = configureEntity.getFree() >= 0 && configureEntity.getFree() < configureEntity.getTotal() ? formatStatus : ConstantValue.NOOIE_SD_STATUS_NO_SD;
        }
        return formatStatus;
    }

    public static void logListDeviceItems(List<ListDeviceItem> devices) {
        for (ListDeviceItem device : CollectionUtil.safeFor(devices)) {
            NooieLog.d("-->> NooieDeviceHelper logListDeviceItems device id=" + device.getDeviceId() + " name=" + device.getName());
        }
    }

    /**
     * @param version1
     * @param version2
     * @return version1 > version2 as 1, version1 == version2 as 0, version1 < version2 as -1
     */
    public static int compareVersion(String version1, String version2) {
        if (TextUtils.isEmpty(version1) || TextUtils.isEmpty(version2)) {
            return 0;
        }

        String[] versionCodes1 = version1.split("\\.");
        String[] versionCodes2 = version2.split("\\.");
        if (versionCodes1 != null && versionCodes2 != null && versionCodes1.length == versionCodes2.length && versionCodes1.length == 3) {
            if (DataHelper.toInt(versionCodes1[0]) > DataHelper.toInt(versionCodes2[0])) {
                return 1;
            } else if (DataHelper.toInt(versionCodes1[0]) == DataHelper.toInt(versionCodes2[0])) {
                if (DataHelper.toInt(versionCodes1[1]) > DataHelper.toInt(versionCodes2[1])) {
                    return 1;
                } else if (DataHelper.toInt(versionCodes1[1]) == DataHelper.toInt(versionCodes2[1])) {
                    if (DataHelper.toInt(versionCodes1[2]) > DataHelper.toInt(versionCodes2[2])) {
                        return 1;
                    } else if (DataHelper.toInt(versionCodes1[2]) == DataHelper.toInt(versionCodes2[2])) {
                        return 0;
                    } else {
                        return -1;
                    }
                } else {
                    return -1;
                }
            } else {
                return -1;
            }
        }
        return 0;
    }

    public static boolean isDeviceCompatible(DeviceCompatibleVersion compatibleVersion) {
        if (compatibleVersion == null || TextUtils.isEmpty(compatibleVersion.getModel()) || TextUtils.isEmpty(compatibleVersion.getVersion())) {
            return false;
        }

        String key = !TextUtils.isEmpty(compatibleVersion.getType()) ? compatibleVersion.getType() : compatibleVersion.getModel();
        if (compatibleVersion.getCompatibleMinVersion() == null || compatibleVersion.getCompatibleMinVersion().isEmpty() || !compatibleVersion.getCompatibleMinVersion().containsKey(key)) {
            return true;
        }

        return compareVersion(compatibleVersion.getVersion(), compatibleVersion.getCompatibleMinVersion().get(key)) >= 0;
    }

    public static boolean isSupportDetectionZone(String model, String version) {
        IpcType ipcType = IpcType.getIpcType(model);
        if (ipcType == IpcType.IPC_UNKNOWN || TextUtils.isEmpty(version)) {
            return false;
        }
        if (!(ipcType == IpcType.PC420 || ipcType == IpcType.PC530 || ipcType == IpcType.PC540 || ipcType == IpcType.PC650 || ipcType == IpcType.PC730 || ipcType == IpcType.SC210 || ipcType == IpcType.SC220)) {
            return true;
        }
        DeviceCompatibleVersion compatibleVersion = new DeviceCompatibleVersion();
        compatibleVersion.setModel(IpcType.getIpcType(model).getType());
        if (IpcType.PC530A_TYPE.equalsIgnoreCase(model) || IpcType.PC730F23_TYPE.equalsIgnoreCase(model)) {
            compatibleVersion.setType(model);
        }
        compatibleVersion.setVersion(version);
        Map<String, String> compatibleVersionMap = new HashMap<>();
        compatibleVersionMap.put(IpcType.PC420.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_420);
        compatibleVersionMap.put(IpcType.PC530.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_530);
        compatibleVersionMap.put(IpcType.PC530A_TYPE, ConstantValue.MIN_DEVICE_DETECTION_ZONE_530A);
        compatibleVersionMap.put(IpcType.PC540.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_540);
        compatibleVersionMap.put(IpcType.PC650.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_650);
        compatibleVersionMap.put(IpcType.PC730.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_730);
        compatibleVersionMap.put(IpcType.PC730F23_TYPE, ConstantValue.MIN_DEVICE_DETECTION_ZONE_730_F23);
        //compatibleVersionMap.put(IpcType.MC120.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_120);
        compatibleVersionMap.put(IpcType.SC210.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_210);
        compatibleVersionMap.put(IpcType.SC220.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_220);
        compatibleVersion.setCompatibleMinVersion(compatibleVersionMap);
        return NooieDeviceHelper.isDeviceCompatible(compatibleVersion);
    }

    public static boolean isSupportNightVisionLightCmd(String model, String version) {
        IpcType ipcType = IpcType.getIpcType(model);
        if (ipcType == IpcType.TS200 || ipcType == IpcType.K1 || ipcType == IpcType.K1PRO) {
            return true;
        }
        return false;
        /*
        IpcType ipcType = IpcType.getIpcType(model);
        if (ipcType == IpcType.IPC_UNKNOWN || TextUtils.isEmpty(version)) {
            return false;
        }
        if (ipcType == IpcType.TS200 || ipcType == IpcType.K1) {
            return true;
        }
        DeviceCompatibleVersion compatibleVersion = new DeviceCompatibleVersion();
        compatibleVersion.setModel(IpcType.getIpcType(model).getType());
        if (IpcType.PC530A_TYPE.equalsIgnoreCase(model)) {
            compatibleVersion.setType(model);
        }
        compatibleVersion.setVersion(version);
        Map<String, String> compatibleVersionMap = new HashMap<>();
        compatibleVersionMap.put(IpcType.PC420.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_420);
        compatibleVersionMap.put(IpcType.PC440.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_420);
        compatibleVersionMap.put(IpcType.PC530.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_530);
        compatibleVersionMap.put(IpcType.PC530PRO.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_530);
        compatibleVersionMap.put(IpcType.PC530A_TYPE, ConstantValue.MIN_DEVICE_DETECTION_ZONE_530A);
        compatibleVersionMap.put(IpcType.PC540.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_540);
        compatibleVersionMap.put(IpcType.PC650.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_650);
        compatibleVersionMap.put(IpcType.PC660.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_650);
        compatibleVersionMap.put(IpcType.PC660PRO.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_650);
        compatibleVersionMap.put(IpcType.PC730.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_730);
        compatibleVersionMap.put(IpcType.PC730F23_TYPE, ConstantValue.MIN_DEVICE_DETECTION_ZONE_730_F23);
        compatibleVersionMap.put(IpcType.PC770.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_730_F23);
        compatibleVersionMap.put(IpcType.MC120.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_120);
        compatibleVersionMap.put(IpcType.SC100.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_210);
        compatibleVersionMap.put(IpcType.SC210.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_210);
        compatibleVersionMap.put(IpcType.SC220.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_210);
        compatibleVersionMap.put(IpcType.EC810PRO.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_220);
        compatibleVersionMap.put(IpcType.EC810_PLUS.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_220);
        compatibleVersionMap.put(IpcType.EC810_CAM.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_220);
        compatibleVersionMap.put(IpcType.HC320.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_220);
        compatibleVersionMap.put(IpcType.TC100.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_220);
        compatibleVersionMap.put(IpcType.TR100.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_220);
        compatibleVersionMap.put(IpcType.TS100.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_220);

        compatibleVersionMap.put(IpcType.C1.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_420);
        compatibleVersionMap.put(IpcType.A1.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_420);
        compatibleVersionMap.put(IpcType.P3.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_530);
        compatibleVersionMap.put(IpcType.P3Pro.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_530);
        compatibleVersionMap.put(IpcType.K2.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_650);
        compatibleVersionMap.put(IpcType.Q1.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_730);
        compatibleVersionMap.put(IpcType.T1.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_730_F23);
        compatibleVersionMap.put(IpcType.M1.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_120);
        compatibleVersionMap.put(IpcType.P1.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_210);
        compatibleVersionMap.put(IpcType.P2.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_210);
        compatibleVersionMap.put(IpcType.P4.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_210);
        compatibleVersionMap.put(IpcType.W1.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_220);
        compatibleVersionMap.put(IpcType.W2.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_220);
        compatibleVersionMap.put(IpcType.W0_CAM.getType(), ConstantValue.MIN_DEVICE_DETECTION_ZONE_220);

        compatibleVersion.setCompatibleMinVersion(compatibleVersionMap);
        return NooieDeviceHelper.isDeviceCompatible(compatibleVersion);

         */
    }

    public static boolean isSupport5GConnected(IpcType ipcType) {
        if (ipcType == IpcType.W2) {
            return true;
        }
        return false;
    }

    public static List<String> getGatewayDeviceIds(List<GatewayDevice> devices) {
        List<String> deviceIds = new ArrayList<>();
        for (GatewayDevice device : CollectionUtil.safeFor(devices)) {
            if (!TextUtils.isEmpty(device.getUuid())) {
                deviceIds.add(device.getUuid());
            }
        }
        return deviceIds;
    }

    public static boolean isDeviceUpdating(int type) {
        return type == ApiConstant.DEVICE_UPDATE_TYPE_DOWNLOAD_START || type == ApiConstant.DEVICE_UPDATE_TYPE_DOWNLOAD_FINISH || type == ApiConstant.DEVICE_UPDATE_TYPE_INSTALL_START || type == ApiConstant.DEVICE_UPDATE_TYPE_UPDATE_SUCCESS;
    }

    public static boolean isSubDevice(String pUuid) {
        return !TextUtils.isEmpty(pUuid) && !ConstantValue.NORMAL_DEVICE_PUUID.equalsIgnoreCase(pUuid);
    }

    public static boolean isSubDevice(String pUuid, String model) {
        return (!TextUtils.isEmpty(pUuid) && !ConstantValue.NORMAL_DEVICE_PUUID.equalsIgnoreCase(pUuid)) || (!TextUtils.isEmpty(model) && IpcType.getIpcType(model) == IpcType.EC810_CAM);
    }

    public static boolean isLpDevice(String model) {
        return DeviceHelper.isLpDevice(model);
    }

    public static boolean isLpApDevice(String model) {
        return isLpDevice(model) && (IpcType.getIpcType(model) == IpcType.HC320);
    }

    public static boolean isDeviceSDCardEnable(String model, boolean isSubDevice) {
        return !isSubDevice && IpcType.getIpcType(model) != IpcType.IPC_UNKNOWN && IpcType.getIpcType(model) != IpcType.EC810_CAM && IpcType.getIpcType(model) != IpcType.W0_CAM && IpcType.getIpcType(model) != IpcType.EC810PRO && IpcType.getIpcType(model) != IpcType.W1;
    }

    public static boolean isSupportShootingSetting(String model, int connectionMode) {
        return connectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT && (IpcType.getIpcType(model) == IpcType.HC320);
    }

    public static boolean isSupportBluetooth(String model) {
        return IpcType.getIpcType(model) == IpcType.HC320;
    }

    public static boolean isSupportEnergyMode(String model) {
        return IpcType.getIpcType(model) == IpcType.HC320;
    }

    public static boolean isSupportWatermark(String model) {
        return IpcType.getIpcType(model) == IpcType.HC320;
    }

    public static boolean isSupportBleDistributeNetwork(String model) {
        return IpcType.getIpcType(model) == IpcType.HC320;
    }

    public static boolean isSortLinkDevice(String model, String pUuid, int connectionMode) {
        return isSortLinkDevice(model, isSubDevice(pUuid, model), connectionMode);
    }

    public static boolean isSortLinkDevice(String model, boolean isSubDevice, int connectionMode) {
        return !TextUtils.isEmpty(model) && (IpcType.getIpcType(model) == IpcType.EC810PRO || IpcType.getIpcType(model) == IpcType.HC320 || IpcType.getIpcType(model) == IpcType.EC810_PLUS || IpcType.getIpcType(model) == IpcType.W1 || IpcType.getIpcType(model) == IpcType.W2) && !isSubDevice && connectionMode != ConstantValue.CONNECTION_MODE_AP_DIRECT;
    }

    public static boolean isSortLinkDevice(ShortLinkDeviceParam param) {
        return param != null && isSortLinkDevice(param.getModel(), param.isSubDevice(), param.getConnectionMode());
    }

    public static boolean isBleApLpDevice(String model, int connectionMode) {
        return !TextUtils.isEmpty(model) && IpcType.getIpcType(model) == IpcType.HC320 && connectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT;
    }

    public static boolean isRechargeableDevice(String model) {
        return DeviceHelper.isRechargeableDevice(model);
    }

    public static boolean isSupportPtzControl(IpcType type) {
        return type == IpcType.PC530 || type == IpcType.PC540 || type == IpcType.PC650 || type == IpcType.SC210 || type == IpcType.SC220 || type == IpcType.SC100 || type == IpcType.PC530PRO || type == IpcType.PC660 || type == IpcType.PC660PRO
                || type == IpcType.P3 || type == IpcType.P3Pro || type == IpcType.P1 || type == IpcType.P1PRO || type == IpcType.P2 || type == IpcType.P4 || type == IpcType.K1 || type == IpcType.K1PRO || type == IpcType.K2 || type == IpcType.TR100 || type == IpcType.TS200;
    }

    public static boolean isSupportPresetPoint(String type) {
        return type != null && isSupportPtzControl(IpcType.getIpcType(type)) && !type.equalsIgnoreCase(IpcType.PC530A_TYPE);
    }

    public static boolean isSupportPtzControlHorizontal(IpcType type) {
        return isSupportPtzControl(type);
    }

    public static boolean isSupportPtzControlVertical(IpcType type) {
        if (!isSupportPtzControl(type)) {
            return false;
        }
        return type != IpcType.SC100 && type != IpcType.P1;
    }

    public static boolean isSupportDistributeNetworkForLan(String model) {
        IpcType type = IpcType.getIpcType(model);
        return type == IpcType.PC530 || type == IpcType.P3 || type == IpcType.PC530PRO || type == IpcType.P3Pro
                || type == IpcType.PC540 || type == IpcType.PC660 || type == IpcType.PC660PRO || type == IpcType.K2
                || type == IpcType.SC220 || type == IpcType.P4;
    }

    public static boolean isSupportDistributeNetworkForAp(String model) {
        IpcType type = IpcType.getIpcType(model);
        return type == IpcType.TR100 || type == IpcType.TS200 || type == IpcType.K1 || type == IpcType.K1PRO || type == IpcType.P1PRO;
    }

    public static boolean isSupportFlashLight(String model) {
        return !TextUtils.isEmpty(model) && (IpcType.getIpcType(model) == IpcType.EC810PRO || IpcType.getIpcType(model) == IpcType.EC810_PLUS || IpcType.getIpcType(model) == IpcType.W1 || IpcType.getIpcType(model) == IpcType.W2);
    }

    public static boolean isNotSupportLedLight(String type) {
        IpcType ipcType = IpcType.getIpcType(type);
        return ipcType == IpcType.IPC_UNKNOWN || ipcType == IpcType.EC810_CAM || ipcType == IpcType.W0_CAM || ipcType == IpcType.EC810PRO || ipcType == IpcType.W1 || ipcType == IpcType.EC810_PLUS || ipcType == IpcType.W2;
    }

    public static boolean isSupportFaceDetection(String type) {
        IpcType ipcType = IpcType.getIpcType(type);
        return ipcType == IpcType.IPC_UNKNOWN || ipcType == IpcType.EC810_CAM || ipcType == IpcType.W0_CAM;
    }

    public static boolean isSupportNightVisionLight(String type) {
        IpcType ipcType = IpcType.getIpcType(type);
        return ipcType == IpcType.TS200 || ipcType == IpcType.K1 || ipcType == IpcType.K1PRO;
    }

    public static List<DeviceConnInfo> convertDeviceConnInfos(List<BindDevice> devices, String account) {
        DeviceConnectionCache.getInstance().filterInvalidConnection(devices);
        List<DeviceConnInfo> deviceConnInfos = new ArrayList<>();
        try {
            Map<String, DeviceConnInfo> subDeviceConnectInfoMap = new HashMap<>();
            for (BindDevice device : CollectionUtil.safeFor(devices)) {
                if (device != null && isSubDevice(device.getPuuid(), device.getType())) {
                    if (TextUtils.isEmpty(IPv4IntTransformer.littleNumToIP(device.getHb_server())) || ConstantValue.HB_SERVER_EMPTY.equalsIgnoreCase(IPv4IntTransformer.littleNumToIP(device.getHb_server())) || device.getHb_port() == 0) {
                        break;
                    }
                    if (!TextUtils.isEmpty(device.getPuuid()) && subDeviceConnectInfoMap.containsKey(device.getPuuid()) && subDeviceConnectInfoMap.get(device.getPuuid()) != null) {
                        if (!isSubDeviceInConnInfo(device.getUuid(), subDeviceConnectInfoMap.get(device.getPuuid()))) {
                            if (TextUtils.isEmpty(subDeviceConnectInfoMap.get(device.getPuuid()).getSub1UUID())) {
                                subDeviceConnectInfoMap.get(device.getPuuid()).setSub1UUID(StringHelper.safeString(device.getUuid()));
                            } else if (TextUtils.isEmpty(subDeviceConnectInfoMap.get(device.getPuuid()).getSub2UUID())) {
                                subDeviceConnectInfoMap.get(device.getPuuid()).setSub2UUID(StringHelper.safeString(device.getUuid()));
                            } else if (TextUtils.isEmpty(subDeviceConnectInfoMap.get(device.getPuuid()).getSub3UUID())) {
                                subDeviceConnectInfoMap.get(device.getPuuid()).setSub3UUID(StringHelper.safeString(device.getUuid()));
                            } else if (TextUtils.isEmpty(subDeviceConnectInfoMap.get(device.getPuuid()).getSub4UUID())) {
                                subDeviceConnectInfoMap.get(device.getPuuid()).setSub4UUID(StringHelper.safeString(device.getUuid()));
                            }
                        }
                    } else if (!TextUtils.isEmpty(device.getPuuid())) {
                        DeviceConnInfo info = new DeviceConnInfo();
                        info.setUuid(StringHelper.safeString(device.getPuuid()));
                        info.setHbServer(!TextUtils.isEmpty(device.getHb_domain()) ? device.getHb_domain() : StringHelper.safeString(IPv4IntTransformer.littleNumToIP(device.getHb_server())));
                        info.setHbPort(device.getHb_port());
                        info.setUserName(StringHelper.safeString(account));
                        info.setModeType(NooieDeviceHelper.convertNooieModel(IpcType.getIpcType(device.getType()), device.getType()));
                        info.setConnType(ConnType.CONN_TYPE_HUB);
                        info.setSecret(StringHelper.safeString(device.getSecret()));
                        info.setSub1UUID(StringHelper.safeString(device.getUuid()));
                        subDeviceConnectInfoMap.put(device.getPuuid(), info);
                    }
                } else if (device != null && !TextUtils.isEmpty(device.getUuid()) && !DeviceConnectionCache.getInstance().isConnectionExist(device.getUuid())) {
                    DeviceConnInfo info = new DeviceConnInfo();
                    info.setUuid(StringHelper.safeString(device.getUuid()));
                    info.setHbServer(!TextUtils.isEmpty(device.getHb_domain()) ? device.getHb_domain() : StringHelper.safeString(IPv4IntTransformer.littleNumToIP(device.getHb_server())));
                    info.setHbPort(device.getHb_port());
                    info.setUserName(StringHelper.safeString(account));
                    info.setModeType(NooieDeviceHelper.convertNooieModel(IpcType.getIpcType(device.getType()), device.getType()));
                    info.setConnType(ConnType.CONN_TYPE_IPC);
                    info.setSecret(StringHelper.safeString(device.getSecret()));
                    deviceConnInfos.add(info);
                }
            }
            /*
            if (subDeviceConnectInfoMap != null && !subDeviceConnectInfoMap.isEmpty()) {
                deviceConnInfos.addAll(subDeviceConnectInfoMap.values());
            }
            */
            if (CollectionUtil.isNotEmpty(convertNotReadyDeviceConnInfo(subDeviceConnectInfoMap))) {
                deviceConnInfos.addAll(convertNotReadyDeviceConnInfo(subDeviceConnectInfoMap));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deviceConnInfos;
    }

    public static boolean isSubDeviceInConnInfo(String deviceId, DeviceConnInfo deviceConnInfo) {
        if (TextUtils.isEmpty(deviceId) || deviceConnInfo == null) {
            return false;
        }

        return deviceId.equalsIgnoreCase(deviceConnInfo.getSub1UUID()) || deviceId.equalsIgnoreCase(deviceConnInfo.getSub2UUID()) || deviceId.equalsIgnoreCase(deviceConnInfo.getSub3UUID()) || deviceId.equalsIgnoreCase(deviceConnInfo.getSub4UUID());
    }

    private static List<DeviceConnInfo> convertNotReadyDeviceConnInfo(Map<String, DeviceConnInfo> deviceConnInfoMap) {
        if (deviceConnInfoMap == null || deviceConnInfoMap.isEmpty()) {
            return new ArrayList<>();
        }

        List<DeviceConnInfo> deviceConnInfos = new ArrayList<>(deviceConnInfoMap.values());
        Iterator<DeviceConnInfo> infoIterator = deviceConnInfos.iterator();
        while (infoIterator.hasNext()) {
            DeviceConnInfo info = infoIterator.next();
            if (DeviceConnectionCache.getInstance().isConnectionReady(info)) {
                infoIterator.remove();
            }
        }
        return deviceConnInfos;
    }

    public static List<DeviceConnInfo> convertGatewayDeviceConnInfo(List<GatewayDevice> gatewayDevices, String account) {
        List<DeviceConnInfo> deviceConnInfos = new ArrayList<>();
        if (CollectionUtil.isEmpty(gatewayDevices) || TextUtils.isEmpty(account)) {
            return deviceConnInfos;
        }
        try {
            DeviceConnectionCache.getInstance().filterPDeviceInvalidConnection(gatewayDevices);
            for (GatewayDevice gatewayDevice : CollectionUtil.safeFor(gatewayDevices)) {
                if (gatewayDevice != null && !TextUtils.isEmpty(gatewayDevice.getUuid())) {
                    DeviceConnInfo info = new DeviceConnInfo();
                    info.setUuid(StringHelper.safeString(gatewayDevice.getUuid()));
                    info.setHbServer(!TextUtils.isEmpty(gatewayDevice.getHb_domain()) ? gatewayDevice.getHb_domain() : StringHelper.safeString(IPv4IntTransformer.littleNumToIP(gatewayDevice.getHb_server())));
                    info.setHbPort(gatewayDevice.getHb_port());
                    info.setUserName(StringHelper.safeString(account));
                    info.setModeType(NooieDeviceHelper.convertNooieModel(IpcType.getIpcType(gatewayDevice.getType()), gatewayDevice.getType()));
                    info.setConnType(ConnType.CONN_TYPE_HUB);
                    info.setSecret(StringHelper.safeString(gatewayDevice.getSecret()));
                    for (int i = 0; i < CollectionUtil.size(gatewayDevice.getChild()); i++) {
                        BindDevice device = gatewayDevice.getChild().get(i);
                        if (device != null && !TextUtils.isEmpty(device.getUuid())) {
                            if (i == 0) {
                                info.setSub1UUID(device.getUuid());
                            } else if (i == 1) {
                                info.setSub2UUID(device.getUuid());
                            } else if (i == 2) {
                                info.setSub3UUID(device.getUuid());
                            } else if (i == 3) {
                                info.setSub4UUID(device.getUuid());
                            }
                        }
                    }
                    if (!DeviceConnectionCache.getInstance().isConnectionReady(info)) {
                        deviceConnInfos.add(info);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return deviceConnInfos;
    }

    public static void tryConnectionToDevice(String user, List<BindDevice> devices, boolean isForceConnect) {
        if (ApHelper.getInstance().checkIsApDirectConnectionMode() || TextUtils.isEmpty(user) || CollectionUtil.isEmpty(devices)) {
            return;
        }

        Iterator<BindDevice> deviceIterator = devices.iterator();
        while (deviceIterator.hasNext()) {
            BindDevice device = deviceIterator.next();
            if (device != null && isSortLinkDevice(device.getType(), device.getPuuid(), ConstantValue.CONNECTION_MODE_QC)) {
                deviceIterator.remove();
            }
        }

        NooieLog.d("-->> debug NooieDeviceHelper tryConnectionToDevice: cam user=" + user);
        DeviceCmdApi.getInstance().tryConnectionToDevice(user, devices, isForceConnect);
    }

    public static void tryStartPreConnection(String tag, List<DeviceConnInfo> deviceConnInfos) {
        if (CollectionUtil.isNotEmpty(deviceConnInfos)) {
            DeviceConnectionCache.getInstance().log(tag, deviceConnInfos);
            DeviceCmdService.getInstance(NooieApplication.mCtx).connecNooieDevice(deviceConnInfos);
            DeviceConnectionCache.getInstance().addConnections(deviceConnInfos);
        }
        DeviceConnectionCache.getInstance().log();
    }

    public static void removeOffLineDeviceConn(List<BindDevice> devices) {
        if (CollectionUtil.isEmpty(devices)) {
            return;
        }
        for (BindDevice device : devices) {
            if (device != null && DeviceConnectionCache.getInstance().isExisted(device.getUuid()) && device.getOnline() == ApiConstant.ONLINE_STATUS_OFF) {
                DeviceConnectionCache.getInstance().removeConnection(device.getUuid());
            }
        }
    }

    public static void tryConnectionToGatewayDevice(String user, List<GatewayDevice> devices, boolean isForceConnect) {
        if (ApHelper.getInstance().checkIsApDirectConnectionMode()) {
            return;
        }

        NooieLog.d("-->> debug NooieDeviceHelper tryConnectionToDevice: hub user=" + user);
        DeviceCmdApi.getInstance().tryConnectionToGatewayDevice(user, devices, isForceConnect);
    }

    public static void removeOffLineGatewayDeviceConn(List<GatewayDevice> devices) {
        if (CollectionUtil.isEmpty(devices)) {
            return;
        }
        for (GatewayDevice device : devices) {
            if (device != null && DeviceConnectionCache.getInstance().isExisted(device.getUuid()) && device.getOnline() == ApiConstant.ONLINE_STATUS_OFF) {
                DeviceConnectionCache.getInstance().removeConnection(device.getUuid());
            }
        }
    }

    public static boolean tryConnectToSingleDevice(String user, List<BindDevice> devices, boolean isForceConnect) {
        if (ApHelper.getInstance().checkIsApDirectConnectionMode() || TextUtils.isEmpty(user) || CollectionUtil.isEmpty(devices)) {
            return false;
        }
        NooieLog.d("-->> debug NooieDeviceHelper tryConnectionToDevice: single user=" + user);
        user = GlobalData.getInstance().getUid();
        DeviceCmdApi.getInstance().tryConnectionToDevice(user, devices, isForceConnect);
        return true;
    }

    public static List<String> getPDeviceIdsFromSubDevices(List<ListDeviceItem> devices) {
        List<String> pDeviceIds = new ArrayList<>();
        if (CollectionUtil.isEmpty(devices)) {
            return pDeviceIds;
        }

        for (ListDeviceItem device : CollectionUtil.safeFor(devices)) {
            if (device != null && device.getBindDevice() != null && isSubDevice(device.getBindDevice().getPuuid(), device.getBindDevice().getType()) && !TextUtils.isEmpty(device.getBindDevice().getPuuid()) && !pDeviceIds.contains(device.getBindDevice().getPuuid())) {
                pDeviceIds.add(device.getBindDevice().getPuuid());
            }
        }

        return pDeviceIds;
    }

    public static ICRMode convertICRMode(IRMode irMode) {
        if (irMode == null) {
            return ICRMode.ICR_MODE_DAY;
        }

        if (irMode == IRMode.IR_MODE_AUTO) {
            return ICRMode.ICR_MODE_AUTO;
        } else if (irMode == IRMode.IR_MODE_ON) {
            return ICRMode.ICR_MODE_NIGHT;
        } else {
            return ICRMode.ICR_MODE_DAY;
        }
    }

    public static IRMode convertIRMode(ICRMode icrMode) {
        if (icrMode == null) {
            return IRMode.IR_MODE_OFF;
        }

        if (icrMode == ICRMode.ICR_MODE_AUTO) {
            return IRMode.IR_MODE_AUTO;
        } else if (icrMode == ICRMode.ICR_MODE_NIGHT) {
            return IRMode.IR_MODE_ON;
        } else {
            return IRMode.IR_MODE_OFF;
        }
    }

    public static IRMode convertIRModeByMode(int mode) {
        if (mode == ConstantValue.DEVICE_LIGHT_MODE_IR) {
            return IRMode.IR_MODE_AUTO;
        } else {
            return IRMode.IR_MODE_OFF;
        }
    }

    public static int convertBattery(float batteryLevel) {
        if (LP_POWER_MIN >= LP_POWER_MAX) {
            return 1;
        }
        if (batteryLevel > LP_POWER_RANGE_DOWN && batteryLevel <= LP_POWER_MIN) {
            int batteryPercent = (int) ((batteryLevel / LP_POWER_MIN) * 10) >= 1 ? (int) Math.ceil(((batteryLevel / LP_POWER_MIN) * 10)) : 1;
            return batteryPercent < 1 ? 1 : batteryPercent;
        } else if (batteryLevel > LP_POWER_MIN && batteryLevel < LP_POWER_MAX) {
            int batteryPercent = (int) Math.ceil((((batteryLevel - LP_POWER_MIN) / (LP_POWER_MAX - LP_POWER_MIN - 1)) * 90)) + 10;
            return batteryPercent < 10 ? 10 : batteryPercent;
        } else if (batteryLevel >= LP_POWER_MAX && batteryLevel <= LP_POWER_RANGE_UP) {
            return 100;
        } else if (batteryLevel >= LP_POWER_MAX) {
            return 100;
        } else {
            return 1;
        }
    }

    public static int convertBattery(float battery, int batteryRangeDown, int batterRangeUp) {
        if (batteryRangeDown < 0 || batterRangeUp < 0 || batteryRangeDown >= batterRangeUp) {
            return 0;
        }
        float batteryValue = battery <= batteryRangeDown ? 0 : battery - batteryRangeDown;
        float batteryRange = batterRangeUp - batteryRangeDown;
        int result = batteryRange > 0 ? (int) Math.ceil(((batteryValue / batteryRange) * 100)) : 0;
        return result;
    }

    public static int computeBattery(String model, float battery) {
        IpcType type = IpcType.getIpcType(model);
        int result = 0;
        if (type == IpcType.EC810_CAM || type == IpcType.EC810PRO || type == IpcType.EC810_PLUS || type == IpcType.W0_CAM || type == IpcType.W1) {
            result = convertBattery(battery);
        } else if (mergeIpcType(type) == IpcType.MC120) {
            result = convertBattery(battery, POWER_RANGE_DOWN_MC_120, POWER_RANGE_UP_MC_120);
        } else if (mergeIpcType(type) == IpcType.HC320) {
            result = convertBattery(battery, POWER_RANGE_DOWN_HC_320, POWER_RANGE_UP_HC_320);
        } else if (battery <= 100) {
            result = (int) battery;
        }
        return result;
    }

    public static boolean checkIsExternalPower(String model, float battery) {
        IpcType type = IpcType.getIpcType(model);
        if (mergeIpcType(type) == IpcType.HC320 && battery < EXTERNAL_POWER_HC_320) {
            return true;
        }
        return false;
    }

    public static RecordType convertRecordTypeByMsgType(int msgType) {
        if (msgType == ApiConstant.DEVICE_MSG_TYPE_MOTION_DETECT) {
            return RecordType.MOTION_RECORD;
        } else if (msgType == ApiConstant.DEVICE_MSG_TYPE_SOUND_DETECT) {
            return RecordType.SOUND_RECORD;
        } else if (msgType == ApiConstant.DEVICE_MSG_TYPE_PIR_DETECT) {
            return RecordType.PIR_RECORD;
        } else {
            return RecordType.PLAN_RECORD;
        }
    }

    private static final int ZONE_WIDTH = 1920;
    private static final int ZONE_HEIGHT = 1080;

    public static RectF convertZoneRectF(RectF rectF, int width, int height) {
        if (rectF == null || width <= 0 || height <= 0) {
            return null;
        }
        int left = (int) ((rectF.left / width) * ZONE_WIDTH);
        int right = (int) ((rectF.right / width) * ZONE_WIDTH);
        int top = (int) ((rectF.top / height) * ZONE_HEIGHT);
        int bottom = (int) ((rectF.bottom / height) * ZONE_HEIGHT);
        return new RectF(left, top, right, bottom);
    }

    public static RectF reconvertZoneRectF(RectF rectF, int width, int height) {
        if (rectF == null || width <= 0 || height <= 0) {
            return null;
        }
        int left = (int) ((rectF.left / ZONE_WIDTH) * width);
        int right = (int) ((rectF.right / ZONE_WIDTH) * width);
        int top = (int) ((rectF.top / ZONE_HEIGHT) * height);
        int bottom = (int) ((rectF.bottom / ZONE_HEIGHT) * height);
        return new RectF(left, top, right, bottom);
    }

    public static AreaRect convertAreaRect(int horMaxSteps, int verMaxSteps, RectF selectZoneRectF) {
        if (horMaxSteps < 1 || verMaxSteps < 1 || isSelectZoneInvalid(selectZoneRectF)) {
            return null;
        }
        try {
            AreaRect areaRect = new AreaRect();
            int horPerStep = ZONE_WIDTH % horMaxSteps == 0 ? (int) (ZONE_WIDTH / horMaxSteps) : (int) (ZONE_WIDTH / horMaxSteps) + 1;
            int verPerStep = ZONE_HEIGHT % verMaxSteps == 0 ? (int) (ZONE_HEIGHT / verMaxSteps) : (int) (ZONE_HEIGHT / verMaxSteps) + 1;
            areaRect.ltX = selectZoneRectF.left < ZONE_WIDTH ? (int) (selectZoneRectF.left) / horPerStep : horMaxSteps - 1;
            areaRect.rbX = selectZoneRectF.right < ZONE_WIDTH ? (int) (selectZoneRectF.right) / horPerStep : horMaxSteps - 1;
            areaRect.ltY = selectZoneRectF.top < ZONE_HEIGHT ? (int) (selectZoneRectF.top) / verPerStep : verMaxSteps - 1;
            areaRect.rbY = selectZoneRectF.bottom < ZONE_HEIGHT ? (int) (selectZoneRectF.bottom) / verPerStep : verMaxSteps - 1;
            return areaRect;
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
        return null;
    }

    public static boolean isMtAreaInfoInvalid(MTAreaInfo mtAreaInfo) {
        return mtAreaInfo == null || mtAreaInfo.horMaxSteps < 1 || mtAreaInfo.verMaxSteps < 1 || mtAreaInfo.areaRects == null || mtAreaInfo.areaRects.length < 1;
    }

    public static boolean isAreaRectInValid(AreaRect areaRect) {
        return areaRect == null || (areaRect.ltX == 0 && areaRect.rbX == 0) || (areaRect.ltY == 0 && areaRect.rbY == 0);
    }

    public static boolean isSelectZoneInvalid(RectF selectZoneRectF) {
        return selectZoneRectF == null || selectZoneRectF.left < 0 || selectZoneRectF.right < 0 || selectZoneRectF.top < 0 || selectZoneRectF.bottom < 0 || selectZoneRectF.left >= selectZoneRectF.right || selectZoneRectF.top >= selectZoneRectF.bottom;
    }

    public static RectF convertSelectZoneRect(int horMaxSteps, int verMaxSteps, AreaRect areaRect) {
        if (horMaxSteps < 1 || verMaxSteps < 1 || areaRect == null) {
            return null;
        }
        try {
            RectF rectF = new RectF();
            float horPerStep = ZONE_WIDTH / (float) horMaxSteps;
            float verPerStep = ZONE_HEIGHT / (float) verMaxSteps;
            rectF.left = areaRect.ltX * horPerStep;
            rectF.right = (areaRect.rbX + 1) * horPerStep;
            rectF.top = areaRect.ltY * verPerStep;
            rectF.bottom = (areaRect.rbY + 1) * verPerStep;
            return rectF;
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
        return null;
    }

    public static List<SelectProduct> filterEnableSelectProduct(List<SelectProduct> selectProducts) {
        if (CollectionUtil.isEmpty(selectProducts)) {
            return CollectionUtil.emptyList();
        }

        Iterator<SelectProduct> productIterator = selectProducts.iterator();
        while (productIterator.hasNext()) {
            SelectProduct selectProduct = productIterator.next();
            if (selectProduct == null) {
                continue;
            }
            if (selectProduct.isEnable()) {
                selectProduct.setChildren(filterEnableSelectDevice(selectProduct.getChildren()));
            } else {
                productIterator.remove();
            }
        }

        return selectProducts;
    }

    public static List<SelectDeviceBean> filterEnableSelectDevice(List<SelectDeviceBean> selectDevices) {
        if (CollectionUtil.isEmpty(selectDevices)) {
            return CollectionUtil.emptyList();
        }

        Iterator<SelectDeviceBean> deviceIterator = selectDevices.iterator();
        while (deviceIterator.hasNext()) {
            SelectDeviceBean selectDevice = deviceIterator.next();
            if (selectDevice == null) {
                continue;
            }
            if (!selectDevice.isEnable()) {
                deviceIterator.remove();
            }
        }

        return selectDevices;
    }

    public static void initNativeConnect() {
        DeviceCmdApi.getInstance().initNativeConnect();
        DeviceCmdApi.getInstance().refreshDeviceCmdParam();
        /*
        try {
            String[] ipAndPort = !TextUtils.isEmpty(GlobalData.getInstance().getP2pUrl()) ? GlobalData.getInstance().getP2pUrl().split(":") : null;
            NooieLog.d("-->> NooieDeviceHelper initNativeConnect p2pUrl=" + GlobalData.getInstance().getP2pUrl());
            if (ipAndPort == null || ipAndPort.length < 2) {
                return;
            }
            String ip = ipAndPort[0];
            String port = ipAndPort[1];
            NooieLog.d("-->> NooieDeviceHelper initNativeConnect ip=" + ip + " port=" + port);
            NooieNative.getInstance().initNativeConn(GlobalData.getInstance().getUid(), ip, Integer.valueOf(port));
        } catch (Exception e) {
        }
         */
    }

    public static void releaseCalenderBeanList(List<CalenderBean> calenderBeans) {
        if (CollectionUtil.isEmpty(calenderBeans)) {
            return;
        }

        for (int i = 0; i < calenderBeans.size(); i++) {
            if (calenderBeans.get(i) != null) {
                calenderBeans.get(i).setCalendar(null);
            }
        }
        calenderBeans.clear();
    }

    public static boolean isNetworkOf5G(String ssid) {
        NooieLog.d("-->> NooieDeviceHelper isNetworkOf5G ssid=" + ssid);
        if (ssid == null || ssid.toLowerCase() == null || TextUtils.isEmpty(ssid.toLowerCase()) || !(ssid.toLowerCase().contains(ConstantValue.WIFI_FUTURE_CODE_5) || ssid.toLowerCase().contains(ConstantValue.WIFI_FUTURE_CODE_5G))) {
            return false;
        }

        try {
            String ssidLc = ssid.toLowerCase();
            if (ssidLc.contains(ConstantValue.WIFI_FUTURE_CODE_5G)) {
                NooieLog.d("-->> NooieDeviceHelper isNetworkOf5G 5G ssid=" + ssid.toLowerCase() + " subFormat=" + ssid.toLowerCase().substring(ssid.toLowerCase().lastIndexOf(ConstantValue.WIFI_FUTURE_CODE_5G), ssid.length()) + " is5G=" + ssid.toLowerCase().substring(ssid.toLowerCase().lastIndexOf(ConstantValue.WIFI_FUTURE_CODE_5G), ssid.length()).equalsIgnoreCase(ConstantValue.WIFI_FUTURE_CODE_5G));
                return ssidLc.lastIndexOf(ConstantValue.WIFI_FUTURE_CODE_5G) >= 0 && ssidLc.lastIndexOf(ConstantValue.WIFI_FUTURE_CODE_5G) < ssidLc.length() && ssidLc.substring(ssidLc.lastIndexOf(ConstantValue.WIFI_FUTURE_CODE_5G), ssid.length()).equalsIgnoreCase(ConstantValue.WIFI_FUTURE_CODE_5G);
            } else if (ssidLc.contains(ConstantValue.WIFI_FUTURE_CODE_5)) {
                NooieLog.d("-->> NooieDeviceHelper isNetworkOf5G 5 ssid=" + ssid.toLowerCase() + " subFormat1=" + ssid.toLowerCase().substring(ssid.toLowerCase().lastIndexOf(ConstantValue.WIFI_FUTURE_CODE_5), ssid.length()) + " is5G=" + ssid.toLowerCase().substring(ssid.lastIndexOf(ConstantValue.WIFI_FUTURE_CODE_5), ssid.length()).equalsIgnoreCase(ConstantValue.WIFI_FUTURE_CODE_5));
                return ssidLc.lastIndexOf(ConstantValue.WIFI_FUTURE_CODE_5) >= 0 && ssidLc.lastIndexOf(ConstantValue.WIFI_FUTURE_CODE_5) < ssidLc.length() && ssidLc.substring(ssidLc.lastIndexOf(ConstantValue.WIFI_FUTURE_CODE_5), ssid.length()).equalsIgnoreCase(ConstantValue.WIFI_FUTURE_CODE_5);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean checkApFutureCode(String ssid) {
        return ssid != null && !TextUtils.isEmpty(ssid.toLowerCase()) && (ssid.toLowerCase().startsWith(ConstantValue.AP_FUTURE_CODE_PREFIX_VICTURE) || ssid.toLowerCase().startsWith(ConstantValue.AP_FUTURE_CODE_PREFIX_GNCC) || ssid.toLowerCase().startsWith(ConstantValue.AP_FUTURE_CODE_PREFIX_TECKIN));
    }

    public static boolean checkBluetoothApFutureCode(String ssid, String bleName) {
        return ssid != null && !TextUtils.isEmpty(ssid.toLowerCase()) && (ssid.toLowerCase().startsWith(ConstantValue.AP_FUTURE_PREFIX) || ssid.toLowerCase().startsWith(ConstantValue.AP_FUTURE_PREFIX_GNCC) || ssid.toLowerCase().startsWith(ConstantValue.AP_FUTURE_PREFIX_TECKIN));// && ssid.equals(bleName);
    }

    public static boolean checkBluetoothFutureCode(String bleName) {
        return bleName != null && !TextUtils.isEmpty(bleName.toLowerCase()) && (bleName.toLowerCase().startsWith(ConstantValue.AP_FUTURE_PREFIX) || bleName.toLowerCase().startsWith(ConstantValue.AP_FUTURE_PREFIX_GNCC) || bleName.toLowerCase().startsWith(ConstantValue.AP_FUTURE_PREFIX_TECKIN));
    }

    public static boolean checkBluetoothAddressMatching(String targetAddress, String address) {
        return !TextUtils.isEmpty(targetAddress) && !TextUtils.isEmpty(address) && targetAddress.equalsIgnoreCase(address);
    }

    public static String getFileSettingModeText(Context context, int mode) {
        if (context == null) {
            return new String();
        }
        if (mode == ConstantValue.DEVICE_MEDIA_MODE_VIDEO) {
            return context.getString(R.string.media_label_video);
        } else if (mode == ConstantValue.DEVICE_MEDIA_MODE_IMAGE) {
            return context.getString(R.string.media_label_photo);
        } else if (mode == ConstantValue.DEVICE_MEDIA_MODE_VIDEO_IMAGE) {
            return new StringBuilder().append(context.getString(R.string.media_label_video)).append("+").append(context.getString(R.string.media_label_photo)).toString();
        } else {
            return new String();
        }
    }

    public static long getCurrentTimeMillisForApDevice(String model, float timeZone) {
        if (mergeIpcType(model) == IpcType.MC120) {
            return System.currentTimeMillis();
        } else {
            return System.currentTimeMillis() + (long) (timeZone * DateTimeUtil.MINUTE_COUNT * DateTimeUtil.SECOND_COUNT * 1000L);
        }
    }

    public static void sendRemoveDeviceBroadcast(int type, String deviceId) {
        Intent intent = new Intent(ConstantValue.BROADCAST_KEY_REMOVE_CAMERA);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_TYPE, type);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        NotificationUtil.sendBroadcast(NooieApplication.mCtx, intent);
    }

    public static void sendBroadcast(Context context, String action, Bundle extras) {
        List<String> actions = new ArrayList<>();
        actions.add(action);
        sendBroadcast(context, actions, extras);
    }

    public static void sendBroadcast(Context context, List<String> actions, Bundle extras) {
        Intent pushIntent = new Intent();
        for (String action : CollectionUtil.safeFor(actions)) {
            pushIntent.setAction(action);
        }
        if (extras != null) {
            pushIntent.putExtras(extras);
        }
        NotificationUtil.sendBroadcast(NooieApplication.mCtx, pushIntent);
    }

    public static String getDistributeNetworkId(boolean isNew) {
        if (isNew || TextUtils.isEmpty(mDistributeNetworkId)) {
            return String.valueOf(System.currentTimeMillis());
        } else {
            return mDistributeNetworkId;
        }
    }

    public static String createDistributionNetworkExternal(boolean isNew) {
        return GsonHelper.convertToJson(new DistributionNetworkEventBean(getDistributeNetworkId(isNew)));
    }

    public static String createSelectDeviceTypeDNExternal(String selectDeviceType) {
        return GsonHelper.convertToJson(new SelectDeviceTypeDNEventBean(selectDeviceType));
    }

    public static String createConnectionModeDNExternal(int connectionMode) {
        return GsonHelper.convertToJson(new ConnectionModeDNEventBean(connectionMode));
    }

    public static String createConnectionResultDNExternal(int networkMode, int networkResult, int devOnline, int networkReason) {
        return GsonHelper.convertToJson(new ConnectionResultDNEventBean(networkMode, networkResult, devOnline, networkReason));
    }

    public static String createNameDeviceDNExternal(String deviceName) {
        return GsonHelper.convertToJson(new NameDeviceDNEventBean(deviceName));
    }

    public static String createSendApStateDNExternal(int sendApState) {
        return GsonHelper.convertToJson(new SendApStateDNEventBean(sendApState));
    }

    public static String createUuidRepeatDNExternal(String deviceID, String deviceModel) {
        return GsonHelper.convertToJson(new UuidRepeatDNEventBean(deviceID, deviceModel));
    }

    public static int convertEventConnectionMode(String model, int connectionMode) {
        if (connectionMode == ConstantValue.CONNECTION_MODE_AP) {
            return EventDictionary.DISTRIBUTE_NETWORK_MODE_AP;
        } else if (connectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            return EventDictionary.DISTRIBUTE_NETWORK_MODE_DV;
        } else if (connectionMode == ConstantValue.CONNECTION_MODE_LAN) {
            return EventDictionary.DISTRIBUTE_NETWORK_MODE_LAN;
        } else {
            return EventDictionary.DISTRIBUTE_NETWORK_MODE_QC;
        }
    }

    public static void trackDNEvent(String eventId) {
        EventTrackingApi.getInstance().trackNormalEvent(eventId, createDistributionNetworkExternal(false), null, null);
    }

    public static void trackDNEvent(boolean isNew, String eventId) {
        EventTrackingApi.getInstance().trackNormalEvent(eventId, createDistributionNetworkExternal(isNew), null, null);
    }

    public static void trackDNEvent(String eventId, String external2) {
        EventTrackingApi.getInstance().trackNormalEvent(eventId, createDistributionNetworkExternal(false), external2, null);
    }

    public static void trackDNEvent(boolean isNew, String eventId, String external2) {
        EventTrackingApi.getInstance().trackNormalEvent(eventId, createDistributionNetworkExternal(isNew), external2, null);
    }

    public static void trackDNEvent(String eventId, String external2, String external3) {
        EventTrackingApi.getInstance().trackNormalEvent(eventId, createDistributionNetworkExternal(false), external2, external3);
    }

    public static void trackDNEvent(String eventId, String pageId, String external2, String external3) {
        EventTrackingApi.getInstance().trackNormalEvent(eventId, pageId, 0, createDistributionNetworkExternal(false), external2, external3, new String());
    }

    public static void trackDNEvent(boolean isNew, String eventId, String external2, String external3) {
        EventTrackingApi.getInstance().trackNormalEvent(eventId, createDistributionNetworkExternal(isNew), external2, external3);
    }

    public static void trackDNEvent(boolean isNew, String eventId, String pageId, String external2, String external3) {
        EventTrackingApi.getInstance().trackNormalEvent(eventId, pageId, 0, createDistributionNetworkExternal(isNew), external2, external3, new String());
    }

    private static void initModelNameMap() {
        if (mModelNameMap != null) {
            return;
        }
        mModelNameMap = new HashMap<>();
        mModelNameMap.put(IpcType.PC420.getType(), ConstantValue.IPC_MODEL_C1);
        mModelNameMap.put(IpcType.PC440.getType(), ConstantValue.IPC_MODEL_A1);
        mModelNameMap.put(IpcType.PC530.getType(), ConstantValue.IPC_MODEL_P3);
        mModelNameMap.put(IpcType.PC530PRO.getType(), ConstantValue.IPC_MODEL_P3PRO);
        mModelNameMap.put(IpcType.PC730.getType(), ConstantValue.IPC_MODEL_Q1);
        mModelNameMap.put(IpcType.PC770.getType(), ConstantValue.IPC_MODEL_T1);
        mModelNameMap.put(IpcType.SC100.getType(), ConstantValue.IPC_MODEL_P1);
        mModelNameMap.put(IpcType.SC210.getType(), ConstantValue.IPC_MODEL_P2);
        mModelNameMap.put(IpcType.SC220.getType(), ConstantValue.IPC_MODEL_P4);
        //mModelNameMap.put(IpcType.PC660.getType(), ConstantValue.IPC_MODEL_K1);
        mModelNameMap.put(IpcType.PC660PRO.getType(), ConstantValue.IPC_MODEL_K2);
        mModelNameMap.put(IpcType.SC210.getType(), ConstantValue.IPC_MODEL_P2);
        mModelNameMap.put(IpcType.MC120.getType(), ConstantValue.IPC_MODEL_M1);
        mModelNameMap.put(IpcType.EC810_CAM.getType(), ConstantValue.IPC_MODEL_W0_CAM);
        mModelNameMap.put(IpcType.EC810_HUB.getType(), ConstantValue.IPC_MODEL_W0_HUB);
        mModelNameMap.put(IpcType.EC810PRO.getType(), ConstantValue.IPC_MODEL_W1);
        mModelNameMap.put(IpcType.EC810_PLUS.getType(), ConstantValue.IPC_MODEL_W2);
    }
}
