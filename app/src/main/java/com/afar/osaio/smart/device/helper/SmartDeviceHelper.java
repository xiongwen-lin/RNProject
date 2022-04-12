package com.afar.osaio.smart.device.helper;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.afar.osaio.R;
import com.afar.osaio.bean.ApDeviceInfo;
import com.afar.osaio.smart.device.bean.ListDeviceItem;
import com.afar.osaio.smart.electrician.manager.PowerStripHelper;
import com.afar.osaio.smart.home.bean.SmartBaseDevice;
import com.afar.osaio.smart.home.bean.SmartCameraDevice;
import com.afar.osaio.smart.home.bean.SmartDeviceConstant;
import com.afar.osaio.smart.home.bean.SmartRouterDevice;
import com.afar.osaio.smart.home.bean.SmartTyDevice;
import com.afar.osaio.smart.player.activity.BasePlayerActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.bean.TabItemBean;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.db.entity.BleApDeviceEntity;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2021/11/29 6:15 下午
 * 说明:
 *
 * 备注:
 *
 ***********************************************************/
public class SmartDeviceHelper {

    public static <T extends SmartBaseDevice> List<SmartBaseDevice> convertSmartDeviceList(List<T> deviceList) {
        List<SmartBaseDevice> result = new ArrayList<>();
        if (deviceList == null || deviceList.isEmpty()) {
            return result;
        }
        result.addAll(deviceList);
        return result;
    }

    public static List<SmartTyDevice> convertSmartTyDeviceList(List<DeviceBean> deviceBeanList) {
        if (deviceBeanList == null || deviceBeanList.isEmpty()) {
            return null;
        }
        List<SmartTyDevice> result = new ArrayList<>();
        for (DeviceBean deviceBean : deviceBeanList) {
            SmartTyDevice device = convertSmartTyDevice(deviceBean);
            if (device != null) {
                result.add(device);
            }
        }
        return result;
    }

    public static SmartTyDevice convertSmartTyDevice(DeviceBean deviceBean) {

        if (deviceBean == null) {
            return null;
        }

        SmartTyDevice device = new SmartTyDevice();
        device.deviceId = deviceBean.getDevId();
        device.model = "";
        device.deviceCategory = SmartDeviceConstant.DEVICE_CATEGORY_TUYA;
        device.deviceSubCategory = SmartDeviceConstant.DEVICE_SUB_CATEGORY_NORMAL;
        device.deviceName = deviceBean.getName();
        device.deviceState = convertDeviceState(deviceBean.getIsOnline());
        device.deviceSwitchState = convertDeviceSwitchState(PowerStripHelper.getInstance().isDeviceOpen(deviceBean));
        device.bindType = convertDeviceBindType(!deviceBean.getIsShare());
        device.deviceIconUrl = deviceBean.getIconUrl();
        device.productId = deviceBean.getProductId();

        return device;
    }

    public static List<SmartCameraDevice> convertSmartCameraDeviceList(List<ListDeviceItem> deviceItemList) {
        if (deviceItemList == null || deviceItemList.isEmpty()) {
            return null;
        }
        List<SmartCameraDevice> result = new ArrayList<>();
        for (ListDeviceItem deviceItem : deviceItemList) {
            SmartCameraDevice device = convertSmartCameraDevice(deviceItem);
            if (device != null) {
                result.add(device);
            }
        }
        sortSmartBaseDeviceByBindTime(result);
        return result;
    }

    public static SmartCameraDevice convertSmartCameraDevice(ListDeviceItem deviceItem) {

        if (deviceItem == null) {
            return null;
        }

        if (deviceItem.getBindDevice() != null) {
            NooieLog.d("--> debug device id=" + deviceItem.getBindDevice().getUuid() + " time=" + deviceItem.getBindDevice().getTime());
        }
        SmartCameraDevice device = new SmartCameraDevice();
        device.deviceId = deviceItem.getDeviceId();
        device.model = deviceItem.getModel();
        device.deviceCategory = SmartDeviceConstant.DEVICE_CATEGORY_CAMERA;
        device.deviceSubCategory = SmartDeviceConstant.DEVICE_SUB_CATEGORY_NORMAL;
        device.deviceName = deviceItem.getName();
        device.deviceState = convertDeviceState(deviceItem.getOnline() == ApiConstant.ONLINE_STATUS_ON);
        device.deviceSwitchState = convertDeviceSwitchState(deviceItem.getOpenStatus() == ApiConstant.OPEN_STATUS_ON);
        device.bindType = convertDeviceBindType(deviceItem.getBindType());
        device.deviceIconUrl = BasePlayerActivity.getDevicePreviewFile(deviceItem.getDeviceId());
        device.cloudState = convertCloudState(deviceItem.isOpenCloud());

        if (deviceItem.getBindDevice() != null) {
            device.parentDeviceId = deviceItem.getBindDevice().getPuuid();
            device.deviceBindTime = deviceItem.getBindDevice().getTime();
        }

        return device;
    }

    public static List<SmartCameraDevice> convertSmartCameraDeviceListOfNetSpot(List<ApDeviceInfo> deviceItemList) {
        if (deviceItemList == null || deviceItemList.isEmpty()) {
            return null;
        }
        List<SmartCameraDevice> result = new ArrayList<>();
        for (ApDeviceInfo deviceItem : deviceItemList) {
            SmartCameraDevice device = convertSmartCameraDeviceOfNetSpot(deviceItem);
            if (device != null) {
                result.add(device);
            }
        }
        return result;
    }

    public static SmartCameraDevice convertSmartCameraDeviceOfNetSpot(ApDeviceInfo deviceItem) {

        if (deviceItem == null || deviceItem.getBindDevice() == null) {
            return null;
        }

        BindDevice bindDevice = deviceItem.getBindDevice();

        SmartCameraDevice device = new SmartCameraDevice();
        device.deviceId = bindDevice.getUuid();
        device.model = bindDevice.getModel();
        device.deviceCategory = SmartDeviceConstant.DEVICE_CATEGORY_CAMERA;
        device.deviceSubCategory = SmartDeviceConstant.DEVICE_SUB_CATEGORY_NORMAL;
        device.deviceName = bindDevice.getName();
        device.deviceState = convertDeviceState(bindDevice.getOnline() == ApiConstant.ONLINE_STATUS_ON);
        device.deviceSwitchState = convertDeviceSwitchState(bindDevice.getOpen_status() == ApiConstant.OPEN_STATUS_ON);
        device.bindType = convertDeviceBindType(bindDevice.getBind_type());
        device.deviceIconUrl = BasePlayerActivity.getDevicePreviewFile(deviceItem.getDeviceId());
        device.cloudState = "";
        device.deviceSsid = deviceItem.getDeviceSsid();
        device.bleDeviceId = deviceItem.getBleDeviceId();
        device.deviceInfoType = SmartDeviceConstant.SMART_CAMERA_DEVICE_INFO_TYPE_DIRECT_LINK;

        return device;
    }

    public static List<SmartCameraDevice> convertSmartCameraDeviceListOfBleAp(List<BleApDeviceEntity> deviceItemList) {
        if (deviceItemList == null || deviceItemList.isEmpty()) {
            return null;
        }
        List<SmartCameraDevice> result = new ArrayList<>();
        for (BleApDeviceEntity deviceItem : deviceItemList) {
            SmartCameraDevice device = convertSmartCameraDeviceOfBleAp(deviceItem);
            if (device != null) {
                result.add(device);
            }
        }
        sortSmartBaseDeviceByBindTime(result);
        return result;
    }

    public static SmartCameraDevice convertSmartCameraDeviceOfBleAp(BleApDeviceEntity deviceItem) {

        if (deviceItem == null) {
            return null;
        }

        if (deviceItem != null) {
            NooieLog.d("--> debug device ble id=" + deviceItem.getDeviceId() + " time=" + deviceItem.getTime());
        }
        SmartCameraDevice device = new SmartCameraDevice();
        device.deviceId = deviceItem.getDeviceId();
        device.model = deviceItem.getModel();
        device.deviceCategory = SmartDeviceConstant.DEVICE_CATEGORY_CAMERA;
        device.deviceSubCategory = SmartDeviceConstant.DEVICE_SUB_CATEGORY_NORMAL;
        device.deviceName = deviceItem.getName();
        device.deviceState = SmartDeviceConstant.DEVICE_STATE_ONLINE;
        device.deviceSwitchState = SmartDeviceConstant.DEVICE_SWITCH_STATE_ON;
        device.bindType = SmartDeviceConstant.DEVICE_BIND_TYPE_OWNER;
        device.deviceIconUrl = BasePlayerActivity.getDevicePreviewFile(deviceItem.getDeviceId());
        device.cloudState = "";
        device.deviceSsid = deviceItem.getSsid();
        device.bleDeviceId = deviceItem.getBleDeviceId();
        device.deviceInfoType = SmartDeviceConstant.SMART_CAMERA_DEVICE_INFO_TYPE_BLE_NET_SPOT;
        device.deviceBindTime = deviceItem.getTime();

        return device;
    }

    public static String convertDeviceState(boolean on) {
        return on ? SmartDeviceConstant.DEVICE_STATE_ONLINE : SmartDeviceConstant.DEVICE_STATE_OFFLINE;
    }

    public static String convertDeviceSwitchState(boolean switchOn) {
        return switchOn ? SmartDeviceConstant.DEVICE_SWITCH_STATE_ON : SmartDeviceConstant.DEVICE_SWITCH_STATE_OFF;
    }

    public static String convertDeviceBindType(int bindType) {
        return bindType == ApiConstant.BIND_TYPE_OWNER ? SmartDeviceConstant.DEVICE_BIND_TYPE_OWNER : SmartDeviceConstant.DEVICE_BIND_TYPE_SHARER;
    }

    public static String convertDeviceBindType(boolean isOwner) {
        return isOwner ? SmartDeviceConstant.DEVICE_BIND_TYPE_OWNER : SmartDeviceConstant.DEVICE_BIND_TYPE_SHARER;
    }

    public static String convertCloudState(boolean isOpen) {
        return isOpen ? SmartDeviceConstant.CLOUD_STATE_ACTIVE : SmartDeviceConstant.CLOUD_STATE_NONE;
    }

    public static String convertTyDeviceCategory(String categoryCode) {
        if (ConstantValue.TUYA_CATEGORY_CODE_APPLIANCES.equalsIgnoreCase(categoryCode)) {
            return SmartDeviceConstant.DEVICE_CATEGORY_APPLIANCES;
        } else if (ConstantValue.TUYA_CATEGORY_CODE_LIGHT.equalsIgnoreCase(categoryCode)) {
            return SmartDeviceConstant.DEVICE_CATEGORY_LIGHT;
        } else {
            return SmartDeviceConstant.DEVICE_CATEGORY_ELECTRICIAN;
        }
    }

    public static String convertTabDeviceCategory(String deviceCategory) {
        if (SmartDeviceConstant.DEVICE_CATEGORY_CAMERA.equalsIgnoreCase(deviceCategory)) {
            return ConstantValue.TAB_DEVICE_CATEGORY_CAMERA;
        } else if (SmartDeviceConstant.DEVICE_CATEGORY_TUYA.equalsIgnoreCase(deviceCategory)) {
            return ConstantValue.TAB_DEVICE_CATEGORY_TUYA;
        } else if (SmartDeviceConstant.DEVICE_CATEGORY_ROUTER.equalsIgnoreCase(deviceCategory)) {
            return ConstantValue.TAB_DEVICE_CATEGORY_ROUTER;
        } else {
            return "";
        }
    }

    public static List<SmartBaseDevice> mergeSmartDevice(List<SmartCameraDevice> cameras, List<SmartTyDevice> tyDevices, List<SmartRouterDevice> routers) {
        List<SmartBaseDevice> result = new ArrayList<>();
        result.addAll(CollectionUtil.safeFor(cameras));
        result.addAll(CollectionUtil.safeFor(routers));
        result.addAll(CollectionUtil.safeFor(tyDevices));
        return result;
    }

    public static List<SmartBaseDevice> filterSmartDevice(List<SmartBaseDevice> smartDevices, String tabDeviceCategory) {
        if (TextUtils.isEmpty(tabDeviceCategory)) {
            return CollectionUtil.emptyList();
        }
        if (CollectionUtil.isEmpty(smartDevices) || ConstantValue.TAB_DEVICE_CATEGORY_ALL.equalsIgnoreCase(tabDeviceCategory)) {
            return smartDevices;
        }
        List<SmartBaseDevice> result = new ArrayList<>();
        for (SmartBaseDevice device : smartDevices) {
            if (tabDeviceCategory.equalsIgnoreCase(convertTabDeviceCategory(device.deviceCategory))) {
                result.add(device);
            }
        }
        return result;
    }

    public static List<SmartCameraDevice> sortSmartCameraDevice(List<SmartCameraDevice> cameraDevices) {
        List<SmartCameraDevice> result = new ArrayList<>();
        if (CollectionUtil.isEmpty(cameraDevices)) {
            return result;
        }
        List<SmartCameraDevice> offlineDevices = new ArrayList<>();
        for (SmartCameraDevice device : cameraDevices) {
            if (device != null && checkIsDeviceStateOn(device.deviceState)) {
                result.add(device);
            } else if (device != null && !checkIsDeviceStateOn(device.deviceState)) {
                offlineDevices.add(device);
            }
        }
        result.addAll(offlineDevices);
        return result;
    }

    public static List<SmartTyDevice> sortSmartTyDevice(List<SmartTyDevice> smartDevices) {
        List<SmartTyDevice> result = new ArrayList<>();
        if (CollectionUtil.isEmpty(smartDevices)) {
            return result;
        }
        List<SmartTyDevice> offlineDevices = new ArrayList<>();
        for (SmartTyDevice device : smartDevices) {
            if (device != null && checkIsDeviceStateOn(device.deviceState)) {
                result.add(device);
            } else if (device != null && !checkIsDeviceStateOn(device.deviceState)) {
                offlineDevices.add(device);
            }
        }
        result.addAll(offlineDevices);
        return result;
    }

    public static List<SmartRouterDevice> sortSmartRouterDevice(List<SmartRouterDevice> smartDevices) {
        List<SmartRouterDevice> result = new ArrayList<>();
        if (CollectionUtil.isEmpty(smartDevices)) {
            return result;
        }
        List<SmartRouterDevice> offlineDevices = new ArrayList<>();
        for (SmartRouterDevice device : smartDevices) {
            if (device != null && checkIsDeviceStateOn(device.deviceState)) {
                result.add(device);
            } else if (device != null && !checkIsDeviceStateOn(device.deviceState)) {
                offlineDevices.add(device);
            }
        }
        result.addAll(offlineDevices);
        return result;
    }

    public static List<? extends SmartBaseDevice> sortSmartBaseDeviceByBindTime(List<? extends SmartBaseDevice> deviceItems) {
        Collections.sort(deviceItems, new Comparator<SmartBaseDevice>() {
            @Override
            public int compare(SmartBaseDevice item1, SmartBaseDevice item2) {
                long sort1 = item1 != null ? item1.deviceBindTime : 0;
                long sort2 = item2 != null ? item2.deviceBindTime : 0;
                return sort2 > sort1 ? 1 : -1;
            }
        });
        return deviceItems;
    }

    public static TabItemBean createTabItemBean(String title, String tag, int num, int sort) {
        TabItemBean itemBean = new TabItemBean();
        itemBean.title = title;
        itemBean.tag = tag;
        itemBean.num = num;
        itemBean.sort = sort;
        return itemBean;
    }

    public static String convertTabItemTitle(Context context, String tag) {
        String title = "";
        if (context == null) {
            return title;
        }
        if (ConstantValue.TAB_DEVICE_CATEGORY_ALL.equalsIgnoreCase(tag)) {
            title = context.getString(R.string.all);
        } else if (ConstantValue.TAB_DEVICE_CATEGORY_CAMERA.equalsIgnoreCase(tag)) {
            title = context.getString(R.string.camera);
        } else if (ConstantValue.TAB_DEVICE_CATEGORY_TUYA.equalsIgnoreCase(tag)) {
            title = context.getString(R.string.smart_home);
        } else if (ConstantValue.TAB_DEVICE_CATEGORY_ROUTER.equalsIgnoreCase(tag)) {
            title = context.getString(R.string.router_detail_router);
        } else {
            title = context.getString(R.string.warn_message_other);
        }
        return title;
    }

    public static boolean checkIsDeviceStateOn(String state) {
        return SmartDeviceConstant.DEVICE_STATE_ONLINE.equalsIgnoreCase(state);
    }

    public static boolean checkIsDeviceSwitchStateOn(String state) {
        return SmartDeviceConstant.DEVICE_SWITCH_STATE_ON.equalsIgnoreCase(state);
    }

    public static boolean checkIsOwnerDevice(String bindType) {
        return SmartDeviceConstant.DEVICE_BIND_TYPE_OWNER.equalsIgnoreCase(bindType);
    }

    public static boolean checkIsCloudStateActive(String state) {
        return SmartDeviceConstant.CLOUD_STATE_ACTIVE.equalsIgnoreCase(state);
    }

    public static boolean checkDeviceInfoTypeIsBleDirectLink(String type) {
        return SmartDeviceConstant.SMART_CAMERA_DEVICE_INFO_TYPE_DIRECT_LINK.equalsIgnoreCase(type);
    }

    public static boolean checkDeviceInfoTypeIsBleNetSpot(String type) {
        return SmartDeviceConstant.SMART_CAMERA_DEVICE_INFO_TYPE_BLE_NET_SPOT.equalsIgnoreCase(type);
    }

    public static boolean checkDeviceInfoTypeIsP2P(String type) {
        return SmartDeviceConstant.SMART_CAMERA_DEVICE_INFO_TYPE_P2P.equalsIgnoreCase(type);
    }

    public static boolean checkTabCategoryExist(String tabCategory, List<TabItemBean> tabItemBeans) {
        if (CollectionUtil.isEmpty(tabItemBeans)) {
            return false;
        }
        List<String> tabTags = new ArrayList<>();
        for (TabItemBean tabItemBean : CollectionUtil.safeFor(tabItemBeans)) {
            if (tabItemBean != null && !TextUtils.isEmpty(tabItemBean.tag)) {
                tabTags.add(tabItemBean.tag);
            }
        }
        return tabTags.contains(tabCategory);
    }

}
