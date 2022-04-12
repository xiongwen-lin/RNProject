package com.afar.osaio.smart.event;

import android.os.Bundle;

/**
 * TabSelectedEvent
 *
 * @author Administrator
 * @date 2019/11/4
 */
public class DeviceChangeEvent {

    public static final int DEVICE_CHANGE_ACTION_UPDATE = 1;
    public static final int DEVICE_CHANGE_ACTION_FIND_GRAND_LOCATION_PERMISSION = 2;
    public int action;
    public Bundle data;

    public DeviceChangeEvent(int action) {
        this.action = action;
    }

    public DeviceChangeEvent(int action, Bundle data) {
        this.action = action;
        this.data = data;
    }
}
