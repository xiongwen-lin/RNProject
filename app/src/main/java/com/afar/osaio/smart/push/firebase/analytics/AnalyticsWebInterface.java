package com.afar.osaio.smart.push.firebase.analytics;

import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;

import com.afar.osaio.util.DateConvertUtil;
import com.google.gson.reflect.TypeToken;
import com.nooie.common.utils.json.GsonHelper;
import com.nooie.common.utils.log.NooieLog;

import java.util.HashMap;
import java.util.Map;

public class AnalyticsWebInterface {

    public static final String TAG = "AnalyticsWebInterface";

    public AnalyticsWebInterface() {
    }

    @JavascriptInterface
    public void logEvent(String name, String jsonParams) {
        LOGD("logEvent:" + name);
        Bundle param = bundleFromJson(jsonParams);
        FirebaseAnalyticsManager.getInstance().logEvent(name, param);
    }

    @JavascriptInterface
    public void setUserProperty(String name, String value) {
        LOGD("setUserProperty:" + name);
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(value)) {
            return;
        }
        Map<String, String> userProperty = new HashMap<>();
        userProperty.put(name, value);
        FirebaseAnalyticsManager.getInstance().setUseProperty(userProperty);
    }

    private void LOGD(String message) {
        // Only log on debug builds, for privacy
        NooieLog.d("-->> debug AnalyticsWebInterface LOGD: message " + message);
    }

    private Bundle bundleFromJson(String json) {
        Map<String, Object> dataMap = GsonHelper.convertJsonForCollection(json, new TypeToken<Map<String, Object>>(){});
        return DateConvertUtil.bundleFromMap(dataMap);
    }

}
