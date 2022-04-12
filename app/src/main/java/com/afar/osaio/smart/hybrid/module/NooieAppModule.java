package com.afar.osaio.smart.hybrid.module;

import android.net.Uri;

public class NooieAppModule {

    private static final String HYBRID_SCHEMA_NOOIE_APP = "nooieapp";
    private static final String HYBRID_SCHEMA_APEMANS = "apemans";
    public static final String HYBRID_ACTION_GO_HOME = "gohome";
    public static final String HYBRID_ACTION_GET_USER_INFO = "getuid";
    public static final String HYBRID_ACTION_GET_REGION = "getregion";
    public static final String HYBRID_ACTION_UPDATE_TITLE= "update_title";
    public static final String HYBRID_ACTION_PAGE_GO_BACK = "page_goback";

    public static final String HYBRID_KEY_PAGE_GO_BACK_HOME = "home";
    public static final String HYBRID_KEY_PAGE_TITLE = "title";

    public static final String HYBRID_PAGE_GO_BACK_APP = "1";

    private NooieAppModuleCallback mCallback;

    public NooieAppModule(NooieAppModuleCallback callback) {
        mCallback = callback;
    }

    public boolean parseRequestUrl(String url) {
        try {
            Uri uri = Uri.parse(url);
            if (uri != null && (uri.getScheme().equals(HYBRID_SCHEMA_NOOIE_APP) || uri.getScheme().equals(HYBRID_SCHEMA_APEMANS))) {
                if (mCallback != null) {
                    mCallback.onActionCallback(uri.getAuthority(), url);
                }
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public interface NooieAppModuleCallback {
        void onActionCallback(String action, String url);
    }
}
