package com.afar.osaio.smart.device.helper;

import android.text.TextUtils;

import com.afar.osaio.base.NooieApplication;
import com.nooie.sdk.watcher.DogWatcher;

public class EventTrackingHelper {

    private EventTrackingHelper() {
    }

    private static class EventTrackingHelperHolder {
        private static final EventTrackingHelper INSTANCE = new EventTrackingHelper();
    }

    public static EventTrackingHelper getInstance() {
        return EventTrackingHelperHolder.INSTANCE;
    }

    public void appStart(String uid, String appid, String region, String server, int port) {
        if (TextUtils.isEmpty(uid) || TextUtils.isEmpty(appid)) {
            return;
        }
        DogWatcher.getInstance(NooieApplication.mCtx).appStart(uid, appid, region, server, port);
    }

    public void appKill() {
        DogWatcher.getInstance(NooieApplication.mCtx).appKill();
    }
}
