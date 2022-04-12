package com.afar.osaio.smart.hybrid.module;

import android.text.TextUtils;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;

public class BridgeWebComponent {

    public static final String HANDLE_CALLBACK_SUCCESS = "HANDLE_CALLBACK_SUCCESS";
    public static final String HANDLE_CALLBACK_FAIL = "HANDLE_CALLBACK_FAIL";
    public static final String HANDLE_NAME_LOAD_AMAZON_VIEW = "loadAmazonView";
    public static final String HANDLE_NAME_LOAD_BANNER_VIEW = "loadBannerView";
    public static final String HANDLE_NAME_CHECK_AMAZON_APP_INSTALL = "checkAmazonAppInstall";
    public static final String RESPONSE_KEY_INSTALL = "install";

    private BridgeWebView mWebView;

    public BridgeWebComponent(BridgeWebView webView) {
        mWebView = webView;
    }

    public void release() {
        if (mWebView != null) {
            mWebView = null;
        }
    }

    public void registerHandler(String handlerName, BridgeHandler bridgeHandler) {
        if (mWebView == null || TextUtils.isEmpty(handlerName)) {
            if (bridgeHandler != null) {
                bridgeHandler.handler(HANDLE_CALLBACK_FAIL, null);
            }
            return;
        }
        mWebView.registerHandler(handlerName, bridgeHandler);
    }

    public void unregisterHandler(String handlerName) {
        if (mWebView == null) {
            return;
        }
        mWebView.unregisterHandler(handlerName);
    }

    public void registerLoadAmazonView(BridgeHandler bridgeHandler) {
        registerHandler(HANDLE_NAME_LOAD_AMAZON_VIEW, bridgeHandler);
    }

    public void unregisterLoadAmazonView() {
        unregisterHandler(HANDLE_NAME_LOAD_AMAZON_VIEW);
    }

    public void registerLoadBannerView(BridgeHandler bridgeHandler) {
        registerHandler(HANDLE_NAME_LOAD_BANNER_VIEW, bridgeHandler);
    }

    public void unregisterLoadBannerView() {
        unregisterHandler(HANDLE_NAME_LOAD_BANNER_VIEW);
    }

    public void registerCheckAmazonAppInstall(BridgeHandler bridgeHandler) {
        registerHandler(HANDLE_NAME_CHECK_AMAZON_APP_INSTALL, bridgeHandler);
    }

    public void unregisterCheckAmazonAppInstall() {
        unregisterHandler(HANDLE_NAME_CHECK_AMAZON_APP_INSTALL);
    }
}
