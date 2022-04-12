package com.afar.osaio.smart.event;

import android.os.Bundle;

/**
 * TabSelectedEvent
 *
 * @author Administrator
 * @date 2019/11/4
 */
public class HomeActionEvent {

    public static final int HOME_ACTION_SHOW_PHOTO_PICKER = 1;
    public static final int HOME_ACTION_ALBUM_STORAGE_PERMISSION = 2;
    public static final int HOME_ACTION_LOCATION_PERMISSION = 3;
    public static final int HOME_ACTION_WEATHER_LOCATION_PERMISSION = 4;
    public int action;
    public Bundle data;

    public HomeActionEvent(int action) {
        this.action = action;
    }

    public HomeActionEvent(int action, Bundle data) {
        this.action = action;
        this.data = data;
    }
}
