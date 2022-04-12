package com.afar.osaio.util;

import android.os.Looper;

import com.afar.osaio.base.NooieApplication;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Created by victor on 2018/9/25
 * Email is victor.qiao.0604@gmail.com
 */
public class ServiceUtils {
    public static boolean isGooglePlayServicesAvailable() {
        int googlePlayServicesAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(NooieApplication.get());
        return googlePlayServicesAvailable == ConnectionResult.SUCCESS || googlePlayServicesAvailable == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED;
    }

    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }
}
