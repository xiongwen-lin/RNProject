package com.afar.osaio.smart.bridge.utils;

import static com.apemans.platformbridge.constant.BridgeConstant.YR_PLATFORM_EVENT_CLICK_FOR_IPC;
import static com.apemans.platformbridge.constant.BridgeConstant.YR_PLATFORM_EVENT_SWITCH_FOR_IPC;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.helper.SmartDeviceHelper;
import com.afar.osaio.smart.home.bean.SmartCameraDevice;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.apemans.platformbridge.bean.DeviceInfoModel;
import com.apemans.platformbridge.bean.YRPlatformDevice;
import com.apemans.platformbridge.constant.BridgeConstant;
import com.nooie.common.base.GlobalData;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***********************************************************
 * 作者: zhengruidong@apemans.com
 * 日期: 2022/2/17 10:57 上午
 * 说明:
 *
 * 备注:
 *
 ***********************************************************/
public class YRBridgeUtil {

    public static List<YRPlatformDevice> convertPlatformDeviceList(List<SmartCameraDevice> devices) {
        List<YRPlatformDevice> result = new ArrayList<>();
        if (CollectionUtil.isEmpty(devices)) {
            return result;
        }
        for(SmartCameraDevice device : devices) {
            result.add(convertPlatformDevice(device));
        }
        return result;
    }

    public static YRPlatformDevice convertPlatformDevice(SmartCameraDevice device) {
        YRPlatformDevice platformDevice = new YRPlatformDevice();
        if (device == null) {
            return platformDevice;
        }
        platformDevice.setUuid(device.deviceId);
        platformDevice.setName(device.deviceName);
        platformDevice.setOnline(SmartDeviceHelper.checkIsDeviceStateOn(device.deviceState));
        platformDevice.setVersion("");
        platformDevice.setIconUrl(getDevicePreviewFile(device.deviceId));
        platformDevice.setProductId("");
        platformDevice.setPlatform(BridgeConstant.YR_PLATFORM_OF_YRCX);
        platformDevice.setCategory(BridgeConstant.YR_PLATFORM_CATEGORY_OF_IPC);

        Map<String, Object> extra = new HashMap<>();
        extra.put(BridgeConstant.YR_PLATFORM_KEY_DEVICE_LINK_TYPE, device.deviceInfoType);
        extra.put(BridgeConstant.YR_PLATFORM_KEY_MODEL, device.model);
        extra.put(BridgeConstant.YR_PLATFORM_KEY_CLOUD_STATE, device.cloudState);
        platformDevice.setExtra(extra);
        platformDevice.setEventSchema(createEventSchema(platformDevice.getEventSchema(), platformDevice.getUuid(), platformDevice.getOnline(), SmartDeviceHelper.checkIsDeviceSwitchStateOn(device.deviceSwitchState)));

        return platformDevice;
    }

    public static YRPlatformDevice convertPlatformDeviceForNetSpot(SmartCameraDevice device) {
        YRPlatformDevice platformDevice = new YRPlatformDevice();
        if (device == null) {
            return platformDevice;
        }
        platformDevice.setUuid(device.deviceId);
        platformDevice.setName(device.deviceName);
        platformDevice.setOnline(SmartDeviceHelper.checkIsDeviceStateOn(device.deviceState));
        platformDevice.setVersion("");
        platformDevice.setIconUrl(getDevicePreviewFile(device.deviceId));
        platformDevice.setProductId("");
        platformDevice.setPlatform(BridgeConstant.YR_PLATFORM_OF_YRCX);
        platformDevice.setCategory(BridgeConstant.YR_PLATFORM_CATEGORY_OF_IPC);

        Map<String, Object> extra = new HashMap<>();
        extra.put(BridgeConstant.YR_PLATFORM_KEY_DEVICE_LINK_TYPE, device.deviceInfoType);
        extra.put(BridgeConstant.YR_PLATFORM_KEY_MODEL, device.model);
        extra.put(BridgeConstant.YR_PLATFORM_KEY_SSID, device.deviceSsid);
        extra.put(BridgeConstant.YR_PLATFORM_KEY_BLE_DEVICE_ID, device.bleDeviceId);
        platformDevice.setExtra(extra);
        platformDevice.setEventSchema(createEventSchema(platformDevice.getEventSchema(), platformDevice.getUuid(), platformDevice.getOnline(), SmartDeviceHelper.checkIsDeviceSwitchStateOn(device.deviceSwitchState)));

        return platformDevice;
    }

    public static List<DeviceInfoModel> convertDeviceInfoModelList(List<BindDevice> devices) {
        List<DeviceInfoModel> result = new ArrayList<>();
        if (CollectionUtil.isEmpty(devices)) {
            return result;
        }
        for (BindDevice device: devices) {
            result.add(convertDeviceInfoModel(device));
        }
        return result;
    }

    public static DeviceInfoModel convertDeviceInfoModel(BindDevice device) {
        DeviceInfoModel info = new DeviceInfoModel();
        if (device == null) {
            return info;
        }
        String bindType = device.getBind_type() == ApiConstant.BIND_TYPE_OWNER ? BridgeConstant.DEVICE_BIND_TYPE_OWNER : BridgeConstant.DEVICE_BIND_TYPE_SHARER;
        info.setDeviceId(device.getUuid());
        info.setModel(device.getType());
        info.setName(device.getName());
        info.setModel(bindType);
        return info;
    }

    public static String getDevicePreviewFile(String deviceId) {
        String account = GlobalData.getInstance().getAccount();
        String key = String.format("%s_%s", GlobalPrefs.KEY_DEVICE_PREVIEW, deviceId);
        return GlobalPrefs.getString(NooieApplication.mCtx, account, key, "");
    }

    private static Map<String, Map<String, Object>> createEventSchema(Map<String, Map<String, Object>> eventSchema, String uuid, Boolean online, Boolean on) {
        Map<String, Map<String, Object>> result = new HashMap<>();
        if (eventSchema != null && !eventSchema.isEmpty()) {
            result.putAll(eventSchema);
        }
        if (online) {
            Map<String, Object> switchEvent = new HashMap<>();

            Map<String, Object> param = new HashMap<>();
            param.put(BridgeConstant.YR_PLATFORM_KEY_UUID, uuid);
            param.put(BridgeConstant.YR_PLATFORM_KEY_STATE, false);

            switchEvent.put(BridgeConstant.YR_PLATFORM_KEY_EVENT_ID, BridgeConstant.YR_PLATFORM_EVENT_ID_SWITCH);
            switchEvent.put(BridgeConstant.YR_PLATFORM_KEY_EVENT_URL, YR_PLATFORM_EVENT_SWITCH_FOR_IPC);
            switchEvent.put(BridgeConstant.YR_PLATFORM_KEY_EVENT_PARAM, param);
            switchEvent.put(BridgeConstant.YR_PLATFORM_KEY_EVENT_VALUE, on);
            result.put(BridgeConstant.YR_PLATFORM_EVENT_ID_SWITCH, switchEvent);
        }

        Map<String, Object> clickEvent = new HashMap<>();

        Map<String, Object> param = new HashMap<>();
        param.put(BridgeConstant.YR_PLATFORM_KEY_UUID, uuid);

        clickEvent.put(BridgeConstant.YR_PLATFORM_KEY_EVENT_ID, BridgeConstant.YR_PLATFORM_EVENT_ID_CLICK);
        clickEvent.put(BridgeConstant.YR_PLATFORM_KEY_EVENT_URL, YR_PLATFORM_EVENT_CLICK_FOR_IPC);
        clickEvent.put(BridgeConstant.YR_PLATFORM_KEY_EVENT_PARAM, param);
        clickEvent.put(BridgeConstant.YR_PLATFORM_KEY_EVENT_VALUE, "");
        result.put(BridgeConstant.YR_PLATFORM_EVENT_ID_CLICK, clickEvent);

        return result;
    }
}
