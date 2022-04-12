package com.afar.osaio.smart.device.bean;

import com.nooie.common.bean.DataEffect;
import com.nooie.common.bean.DataEffectCache;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;

public class ListDeviceItem {
    private boolean mountedSDCard;
    private boolean openCamera;
    private boolean openCloud;
    private String order;
    private boolean recordLoop;
    private long cloudTime;

    public boolean isMountedSDCard() {
        return mountedSDCard;
    }

    public void setMountedSDCard(boolean mountedSDCard) {
        this.mountedSDCard = mountedSDCard;
    }

    public boolean isopenCamera() {
        return openCamera;
    }

    public void setOpenCamera(boolean openCamera) {
        this.openCamera = openCamera;
    }

    public boolean isOpenCloud() {
        return openCloud;
    }

    public void setOpenCloud(boolean openCloud) {
        this.openCloud = openCloud;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public boolean isRecordLoop() {
        return recordLoop;
    }

    public void setRecordLoop(boolean recordLoop) {
        this.recordLoop = recordLoop;
    }

    public long getCloudTime() {
        return cloudTime;
    }

    public void setCloudTime(long cloudTime) {
        this.cloudTime = cloudTime;
    }

    //nooie smart 新平台设备属性
    public final static int DEVICE_PLATFORM_DANA = 0;
    public final static int DEVICE_PLATFORM_NOOIE = 1;
    private int devicePlatform;
    private int bindType;
    private String deviceId;
    private String name;
    private String version;
    private String model;
    private int online;
    private int openStatus;
    private BindDevice bindDevice;
    private String deviceMac;
    private String isBind;

    public ListDeviceItem(String name, String deviceMac, int bindType, int online, String isBind) {
        this.name = name;
        this.deviceMac = deviceMac;
        this.bindType = bindType;
        this.online = online;
        this.isBind = isBind;
    }

    public ListDeviceItem(BindDevice device) {
        setDevicePlatform(DEVICE_PLATFORM_NOOIE);
        setBindType(device.getBind_type());
        setDeviceId(device.getUuid());
        setName(device.getName());
        setVersion(device.getVersion());
        setModel(device.getType());
        setOnline(device.getOnline());
        setOpenStatus(device.getOpen_status());
        setBindDevice(device);

        setDataEffects();
    }

    public ListDeviceItem(DeviceInfo info) {
        if (info == null) {
            return;
        }

        setBindDevice(info.getNooieDevice());
        setBindType(info.getNooieDevice().getBind_type());
        setName(info.getNooieDevice().getName());
        setOnline(info.getNooieDevice().getOnline());
        setDevicePlatform(info.getDevicePlatform());
        setDeviceId(info.getDeviceId());
        setVersion(info.getVersionCode());
        setModel(info.getModel());
        setOpenCloud(info.isOpenCloud());
        setOpenCamera(info.isOpenCamera());
        setOpenStatus(info.isOpenCamera() ? ApiConstant.OPEN_STATUS_ON : ApiConstant.OPEN_STATUS_OFF);
        setCloudTime(info.getCloudTime());
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public String getIsBind() {
        return isBind;
    }

    public void setIsBind(String isBind) {
        this.isBind = isBind;
    }

    public int getDevicePlatform() {
        return devicePlatform;
    }

    public void setDevicePlatform(int devicePlatform) {
        this.devicePlatform = devicePlatform;
    }

    public int getBindType() {
        return bindType;
    }

    public void setBindType(int bindType) {
        this.bindType = bindType;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getOnline() {
        return online;
    }

    public void setOnline(int online) {
        this.online = online;
    }

    public int getOpenStatus() {
        return openStatus;
    }

    public void setOpenStatus(int openStatus) {
        this.openStatus = openStatus;
    }

    public BindDevice getBindDevice() {
        return bindDevice;
    }

    public void setBindDevice(BindDevice bindDevice) {
        this.bindDevice = bindDevice;
    }

    /**
     * nooie smart update the item
     * @param item
     */
    public void updateItem(ListDeviceItem item) {
        if (item == null) {
            return;
        }

        setBindDevice(item.getBindDevice());
        setOpenStatus(item.getOpenStatus());
        setDevicePlatform(item.getDevicePlatform());
        setBindType(item.getBindType());
        setDeviceId(item.getDeviceId());
        setName(item.getName());
        setVersion(item.getVersion());
        setModel(item.getModel());
        setOnline(item.getOnline());
        setOpenCloud(item.isOpenCloud());
        setCloudTime(item.getCloudTime());
    }

    public void replaceItem(ListDeviceItem item) {
        if (item == null) {
            return;
        }

        setMountedSDCard(item.isMountedSDCard());
        setOpenCamera(item.isopenCamera());
        setOpenCloud(item.isOpenCloud());
        setOrder(item.getOrder());
        setDevicePlatform(item.getDevicePlatform());
        setBindType(item.getBindType());
        setDeviceId(item.getDeviceId());
        setName(item.getName());
        setVersion(item.getVersion());
        setModel(item.getModel());
        setOnline(item.getOnline());
        setOpenStatus(item.getOpenStatus());
        setBindDevice(item.getBindDevice());
        setCloudTime(item.getCloudTime());
    }

    private DataEffectCache mDataEffectCache = new DataEffectCache();
    private final static String DE_KEY_OPEN_CAMERA = "open_camera";
    public final static String DE_KEY_OPEN_CLOUD = "open_cloud";
    public final static String DE_KEY_MODEL = "model";
    public final static String DE_KEY_VERSION = "version";
    private void setDataEffects() {
        DataEffect<Boolean> mOpenCameraDe = new DataEffect<>();
        mOpenCameraDe.setKey(DE_KEY_OPEN_CAMERA);
        mOpenCameraDe.setValue(false);
        mOpenCameraDe.setEffective(false);

        DataEffect<Boolean> mOpenCloudDe = new DataEffect<>();
        mOpenCloudDe.setKey(DE_KEY_OPEN_CLOUD);
        mOpenCloudDe.setValue(false);
        mOpenCloudDe.setEffective(false);

        DataEffect<String> mModelDe = new DataEffect<>();
        mModelDe.setKey(DE_KEY_MODEL);
        mModelDe.setValue("");
        mModelDe.setEffective(false);

        DataEffect<String> mVersionDe = new DataEffect<>();
        mVersionDe.setKey(DE_KEY_VERSION);
        mVersionDe.setValue("");
        mVersionDe.setEffective(false);

        mDataEffectCache.put(mOpenCameraDe.getKey(), mOpenCameraDe);
        mDataEffectCache.put(mOpenCloudDe.getKey(), mOpenCloudDe);
        mDataEffectCache.put(mModelDe.getKey(), mModelDe);
        mDataEffectCache.put(mVersionDe.getKey(), mVersionDe);
    }

    public DataEffectCache getDataEffectCache() {
        return mDataEffectCache;
    }
    public DataEffect getDataEffectByKey(String key) {
        return mDataEffectCache != null ? mDataEffectCache.get(key) : null;
    }

    public boolean isDataEffectByKey(String key) {
        return mDataEffectCache != null ? mDataEffectCache.isDataEffective(key) : false;
    }

    public void updateOpenCameraDe(boolean open) {
        if (getDevicePlatform() == DEVICE_PLATFORM_DANA) {
            DataEffect<Boolean> mOpenCameraDe = new DataEffect<>();
            mOpenCameraDe.setKey(DE_KEY_OPEN_CAMERA);
            mOpenCameraDe.setValue(open);
            mOpenCameraDe.setEffective(true);
            if (mDataEffectCache != null) {
                mDataEffectCache.put(mOpenCameraDe.getKey(), mOpenCameraDe);
            }
        }
    }

    public void updateOpenCloudDe(boolean open) {
        DataEffect<Boolean> mOpenCloudDe = new DataEffect<>();
        mOpenCloudDe.setKey(DE_KEY_OPEN_CLOUD);
        mOpenCloudDe.setValue(open);
        mOpenCloudDe.setEffective(true);
        if (mDataEffectCache != null) {
            mDataEffectCache.put(mOpenCloudDe.getKey(), mOpenCloudDe);
        }
    }

    public void updateItemByEffect(final ListDeviceItem item) {
        if (item == null) {
            return;
        }

        DataEffectCache dataEffectCache = item.getDataEffectCache();
        if (dataEffectCache != null) {
            dataEffectCache.checkDataEffective(DE_KEY_OPEN_CLOUD, new DataEffectCache.CheckDataCallback2<Boolean>() {
                @Override
                public void onResult(DataEffect<Boolean> dataEffect) {
                    if (dataEffect != null && dataEffect.isEffective()) {
                        setOpenCloud(dataEffect.getValue());
                        updateOpenCloudDe(dataEffect.getValue());
                    }
                }
            });
        }
        setBindDevice(item.getBindDevice());
        setOpenStatus(item.getOpenStatus());
        setVersion(item.getVersion());
        setModel(item.getModel());
        setDevicePlatform(item.getDevicePlatform());
        setBindType(item.getBindType());
        setDeviceId(item.getDeviceId());
        setName(item.getName());
        setOnline(item.getOnline());
    }
}
