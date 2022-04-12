package com.afar.osaio.smart.hybrid.helper;

import android.net.Uri;
import android.text.TextUtils;

import com.nooie.data.EventDictionary;

/**
 * HybridHelper
 *
 * @author Administrator
 * @date 2020/9/23
 */
public class HybridHelper {

    private static final String URL_PRIVACY_POLICY = "https://osaio.net/privacy-policy";
    private static final String URL_TERMS = "https://osaio.net/terms";
    private static final String URL_CAM_CONNECT_FAIL = "Cam_ConnectionFailed";
    private static final String URL_CLOUD_PACK = "pack/list";

    public static String getEventIdByUrl(String url, int trackType) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        if (url.contains(URL_PRIVACY_POLICY)) {
            return trackType == EventDictionary.EVENT_TRACK_TYPE_START ? EventDictionary.EVENT_ID_CLICK_PRIVACY : EventDictionary.EVENT_ID_ABORT_PRIVACY_PAGE;
        } else if (url.contains(URL_TERMS)) {
            return trackType == EventDictionary.EVENT_TRACK_TYPE_START ? EventDictionary.EVENT_ID_CLICK_TERMS_OF_SERVICE : EventDictionary.EVENT_ID_ABORT_TERMS_OF_SERVICE_PAGE;
        } else if (url.contains(URL_CAM_CONNECT_FAIL)) {
            return EventDictionary.EVENT_ID_ACCESS_DEVICE_SCAN_FAIL_PAGE;
        } else if (url.contains(URL_CLOUD_PACK)) {
            return EventDictionary.EVENT_ID_ACCESS_CLOUD_PACK_PAGE;
        } else {
            return null;
        }
    }

    public static int getTrackTypeByUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return EventDictionary.EVENT_TRACK_TYPE_NONE;
        }
        if (url.contains(URL_PRIVACY_POLICY)) {
            return EventDictionary.EVENT_TRACK_TYPE_END;
        } else if (url.contains(URL_TERMS)) {
            return EventDictionary.EVENT_TRACK_TYPE_END;
        } else if (url.contains(URL_CAM_CONNECT_FAIL)) {
            return EventDictionary.EVENT_TRACK_TYPE_START;
        } else if (url.contains(URL_CLOUD_PACK)) {
            return EventDictionary.EVENT_TRACK_TYPE_START;
        } else {
            return EventDictionary.EVENT_TRACK_TYPE_NONE;
        }
    }

    public static String getUrlParam(String url, String paramKey) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        try {
            Uri uri = Uri.parse(url);
            String param = uri != null ? uri.getQueryParameter(paramKey) : "";
            return param;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}