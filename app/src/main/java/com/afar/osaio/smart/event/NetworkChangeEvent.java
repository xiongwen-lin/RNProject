package com.afar.osaio.smart.event;

import android.os.Bundle;

/**
 * TabSelectedEvent
 *
 * @author Administrator
 * @date 2019/11/4
 */
public class NetworkChangeEvent {

    public static final int NETWORK_CHANGE_CONNECTED = 1;
    public static final int NETWORK_CHANGE_DISCONNECTED = 2;
    public int state;
    public Bundle data;

    public NetworkChangeEvent(int state) {
        this.state = state;
    }

    public NetworkChangeEvent(int state, Bundle data) {
        this.state = state;
        this.data = data;
    }
}
