package com.afar.osaio.smart.smartlook.helper;

import android.content.Context;
import android.text.TextUtils;

import com.afar.osaio.R;
import com.afar.osaio.smart.smartlook.bean.BleDevice;
import com.afar.osaio.smart.smartlook.cache.BleDeviceScanCache;
import com.nooie.common.base.GlobalData;
import com.nooie.sdk.db.entity.BleDeviceEntity;
import com.nooie.sdk.db.entity.LockAuthorizationEntity;
import com.nooie.sdk.db.entity.LockRecordEntity;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.data.DataHelper;
import com.nooie.common.utils.log.NooieLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class SmartLookDeviceHelper {

    public static int SMART_LOOK_MANUFACTURER_ID = 0x5847;
    public static int SMART_LOOK_MANUFACTURER_BEFORE_ID = 0x58;
    public static int SMART_LOOK_MANUFACTURER_AFTER_ID = 0x47;

    public static void filterAndSaveDevice(ScanResult result) {

        if (result == null) {
            return;
        }

        if (result.getDevice() == null) {
            return;
        }

        if (result.getDevice().getName() == null || (result.getScanRecord().getManufacturerSpecificData() == null || result.getScanRecord().getManufacturerSpecificData().size() == 0)) {
            return;
        }

        byte[] manufacturerDataBytes = result.getScanRecord().getManufacturerSpecificData().get(SMART_LOOK_MANUFACTURER_ID);

        if (manufacturerDataBytes == null || manufacturerDataBytes.length < 11) {
            return;
        }

        if (manufacturerDataBytes[0] == SMART_LOOK_MANUFACTURER_BEFORE_ID && manufacturerDataBytes[1] == SMART_LOOK_MANUFACTURER_AFTER_ID) {
            NooieLog.d("-->> SmartLookDeviceHelper filterAndSaveDevice result=" + result.toString() + " scanRecord 5=" + result.getScanRecord().getBytes()[5] + " 6=" + result.getScanRecord().getBytes()[6]
            + " initState=" + manufacturerDataBytes[8] + " deviceType=" + manufacturerDataBytes[3]);
            byte[] sec = new byte[] {manufacturerDataBytes[9], manufacturerDataBytes[10]};
            int rssi = result.getRssi();
            byte initState = manufacturerDataBytes[8];
            byte deviceType = manufacturerDataBytes[3];
            NooieLog.d("-->> SmartLookDeviceHelper filterAndSaveDevice s1=" + convertByteToShort(manufacturerDataBytes[9]) + " s2=" + convertByteToShort(manufacturerDataBytes[10]));
            BleDevice bleDevice = new BleDevice(result.getDevice(), rssi, sec, initState, deviceType);
            bleDevice.setUid(GlobalData.getInstance().getUid());
            bleDevice.setAccount(GlobalData.getInstance().getAccount());
            bleDevice.setUpdateTime(System.currentTimeMillis());
            BleDeviceScanCache.getInstance().updateCache(bleDevice);
        }
    }

    public static List<String> getBleDeviceIds(List<BleDeviceEntity> bleDeviceEntities) {
        List<String> deviceIds = new ArrayList<>();
        for (BleDeviceEntity deviceEntity : CollectionUtil.safeFor(bleDeviceEntities)) {
            if (deviceEntity != null && !TextUtils.isEmpty(deviceEntity.getDeviceId())) {
                deviceIds.add(deviceEntity.getDeviceId());
            }
        }
        return deviceIds;
    }

    public static List<BleDevice> filterLocalDevice(List<BleDevice> bleDevices, List<String> localBleDeviceIds) {
        if (CollectionUtil.isEmpty(bleDevices) || CollectionUtil.isEmpty(localBleDeviceIds)) {
            return CollectionUtil.safeFor(bleDevices);
        }

        Iterator<BleDevice> bleDeviceIterator = bleDevices.iterator();
        while (bleDeviceIterator.hasNext()) {
            BleDevice bleDevice = bleDeviceIterator.next();
            boolean isRemove = !(bleDevice != null && bleDevice.getDevice() != null && !localBleDeviceIds.contains(bleDevice.getDevice().getName()));
            if (isRemove) {
                bleDeviceIterator.remove();
            }
        }

        return bleDevices;
    }

    public static boolean isBleDevicesInScanCache(List<String> deviceIds) {
        if (CollectionUtil.isEmpty(deviceIds) || BleDeviceScanCache.getInstance().isEmpty()) {
            return false;
        }
        for (String deviceId : CollectionUtil.safeFor(deviceIds)) {
            if (!BleDeviceScanCache.getInstance().isExisted(deviceId)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isBleDeviceVerify(String phone, String password, boolean isAdmin) {
        if (isAdmin) {
            return !TextUtils.isEmpty(phone) && !TextUtils.isEmpty(password);
        } else {
            return !TextUtils.isEmpty(password);
        }
    }

    public static List<String> getRecordKeys(List<LockRecordEntity> lockRecordEntities) {
        List<String> recordKeys = new ArrayList<>();
        for (LockRecordEntity lockRecordEntity : CollectionUtil.safeFor(lockRecordEntities)) {
            if (lockRecordEntity != null && !TextUtils.isEmpty(lockRecordEntity.getRecordKey())) {
                recordKeys.add(lockRecordEntity.getRecordKey());
            }
        }
        return recordKeys;
    }

    public static List<LockRecordEntity> sortLockRecords(List<LockRecordEntity> lockRecordEntities) {
        if (CollectionUtil.isEmpty(lockRecordEntities)) {
            return lockRecordEntities;
        }

        Collections.sort(lockRecordEntities, new Comparator<LockRecordEntity>() {
            @Override
            public int compare(LockRecordEntity o1, LockRecordEntity o2) {
                int result = 0;
                try {
                    result = (o1 == null || o2 == null) ? 0 : -(int)(o1.getTime() - o2.getTime());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return result;
            }
        });

        return lockRecordEntities;
    }

    public static List<String> getAuthorizationKeys(List<LockAuthorizationEntity> lockAuthorizationEntities) {
        List<String> authorizationKeys = new ArrayList<>();
        for (LockAuthorizationEntity lockAuthorizationEntity : CollectionUtil.safeFor(lockAuthorizationEntities)) {
            if (lockAuthorizationEntity != null && !TextUtils.isEmpty(lockAuthorizationEntity.getCode())) {
                authorizationKeys.add(lockAuthorizationEntity.getCode());
            }
        }
        return authorizationKeys;
    }

    public static List<LockAuthorizationEntity> sortLockAuthorizations(List<LockAuthorizationEntity> lockAuthorizationEntities) {
        if (CollectionUtil.isEmpty(lockAuthorizationEntities)) {
            return lockAuthorizationEntities;
        }

        Collections.sort(lockAuthorizationEntities, new Comparator<LockAuthorizationEntity>() {
            @Override
            public int compare(LockAuthorizationEntity o1, LockAuthorizationEntity o2) {
                int result = 0;
                try {
                    result = (o1 == null || o2 == null) ? 0 : (int)(DataHelper.toInt(o1.getCodeIndex()) - DataHelper.toInt(o2.getCodeIndex()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return result;
            }
        });

        return lockAuthorizationEntities;
    }

    public static long convertRecordTime(byte[] data) {
        long time = 0;
        if (data == null || data.length != 4) {
            return time;
        }

        try {
            StringBuilder timeSb = new StringBuilder();
            for (int i = 0; i < data.length; i++) {
                if (i == 0) {
                    timeSb.append(convertByteToStr(data[i]));
                } else {
                    timeSb.append(parseToHexString(data[i]));
                }
            }
            time = Long.parseLong(timeSb.toString(), 16) * 1000L;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return time;
    }

    public static String convertRecordName(Context context, int type) {
        String result = "";
        switch (type) {
            case ConstantValue.LOCK_RECORD_NAME_FOR_USE_OPEN:
                result = context.getString(R.string.lock_record_name_user_open);
                break;
            case ConstantValue.LOCK_RECORD_NAME_FOR_FINGER_OPEN:
                result = context.getString(R.string.lock_record_name_finger_open);
                break;
            case ConstantValue.LOCK_RECORD_NAME_FOR_PSW_OPEN:
                result = context.getString(R.string.lock_record_name_psw_open);
                break;
            case ConstantValue.LOCK_RECORD_NAME_FOR_APP_OPEN:
                result = context.getString(R.string.lock_record_name_app_open);
                break;
            case ConstantValue.LOCK_RECORD_NAME_FOR_REMOTE_OPEN:
                result = context.getString(R.string.lock_record_name_remote_open);
                break;
        }

        return result;
    }

    public static short convertByteToShort(byte value) {
        return (short)(value & 0xFF);
    }

    public static String convertByteToStr(byte value) {
        String result = "";
        try {
            result = Integer.toHexString(value & 0xFF);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    protected final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String parseToHexString(final byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        try {
            final char[] out = new char[data.length * 2];
            for (int j = 0; j < data.length; j++) {
                int v = data[j] & 0xFF;
                out[j * 2] = HEX_ARRAY[v >>> 4];
                out[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
            }
            result.append(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    public static String parseToHexString(final byte data) {
        StringBuilder result = new StringBuilder();
        try {
            final char[] out = new char[2];
            for (int j = 0; j < out.length; j++) {
                int v = data & 0xFF;
                out[0] = HEX_ARRAY[v >>> 4];
                out[1] = HEX_ARRAY[v & 0x0F];
            }
            result.append(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}
