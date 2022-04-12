package com.afar.osaio.smart.push.firebase.analytics;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Map;

/**
 * FirebaseAnalyticsManager
 *
 * @author Administrator
 * @date 2021/6/8
 */
public class FirebaseAnalyticsManager {

    private FirebaseAnalytics mFirebaseAnalytics;
    private String mUserId;

    private FirebaseAnalyticsManager() {
    }

    private static class FirebaseAnalyticsManagerHolder {
        private static final FirebaseAnalyticsManager INSTANCE = new FirebaseAnalyticsManager();
    }

    public static FirebaseAnalyticsManager getInstance() {
        return FirebaseAnalyticsManagerHolder.INSTANCE;
    }

    public void init(Context context) {
        if (!checkFirebaseAnalyticsInvalid() || context == null) {
            return;
        }
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public FirebaseAnalytics getFirebaseAnalytics() {
        return mFirebaseAnalytics;
    }

    public void setUserId(String userId) {
        if (checkFirebaseAnalyticsInvalid() || checkUserIdValid(userId)) {
            return;
        }
        mUserId = userId;
        mFirebaseAnalytics.setUserId(userId);
    }

    public void setSessionTimeoutDuration(long time) {
        if (checkFirebaseAnalyticsInvalid()) {
            return;
        }
        mFirebaseAnalytics.setSessionTimeoutDuration(time);
    }

    public void setUseProperty(Map<String, String> propertyMap) {
        if (checkFirebaseAnalyticsInvalid() || propertyMap == null || propertyMap.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> entry : propertyMap.entrySet()) {
            if (entry != null && !TextUtils.isEmpty(entry.getKey()) && !TextUtils.isEmpty(entry.getValue())) {
                mFirebaseAnalytics.setUserProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    public void logEvent(String eventName, Bundle param) {
        if (checkFirebaseAnalyticsInvalid() || TextUtils.isEmpty(eventName) || param == null || param.isEmpty()) {
            return;
        }
        mFirebaseAnalytics.logEvent(eventName, param);
    }

    private boolean checkFirebaseAnalyticsInvalid() {
        return mFirebaseAnalytics == null;
    }

    private boolean checkUserIdValid(String userId) {
        boolean userIdValid = !TextUtils.isEmpty(userId) && !userId.equalsIgnoreCase(mUserId);
        return userIdValid;
    }

}