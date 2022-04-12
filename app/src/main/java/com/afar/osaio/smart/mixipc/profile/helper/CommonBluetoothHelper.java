package com.afar.osaio.smart.mixipc.profile.helper;

import android.text.TextUtils;

import com.afar.osaio.smart.mixipc.profile.bean.BleDevice;

public class CommonBluetoothHelper {

    public static boolean checkBleDeviceValid(BleDevice bleDevice) {
        return !(bleDevice == null || bleDevice.getDevice() == null || TextUtils.isEmpty(bleDevice.getDevice().getAddress()));
    }
}
