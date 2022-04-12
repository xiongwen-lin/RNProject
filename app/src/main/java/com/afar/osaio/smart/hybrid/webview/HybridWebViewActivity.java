package com.afar.osaio.smart.hybrid.webview;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.hybrid.helper.HybridHelper;
import com.afar.osaio.smart.push.firebase.analytics.FirebaseConstant;
import com.afar.osaio.smart.push.firebase.analytics.FirebaseEventManager;
import com.google.gson.Gson;
import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.smart.hybrid.entity.UserInfo;
import com.afar.osaio.smart.hybrid.module.NooieAppModule;
import com.afar.osaio.smart.hybrid.wx.WXH5PayHandler;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.configure.LanguageUtil;
import com.nooie.common.utils.encrypt.NooieEncrypt;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.afar.osaio.util.DialogUtils;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.api.network.base.core.NetConfigure;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by victor on 2018/8/6
 * Email is victor.qiao.0604@gmail.com
 */
public class HybridWebViewActivity extends BaseActivity {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.webView)
    WebView webView;

    private String url;
    private WXH5PayHandler mWXH5PayHandler;
    private boolean mIsCache = false;
    private String mCurrentPageGoBack = NooieAppModule.HYBRID_PAGE_GO_BACK_APP;

    public static void toHybridWebViewActivity(Context from, Bundle param) {
        Intent intent = new Intent(from, HybridWebViewActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_START_PARAM, param);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        ButterKnife.bind(this);

        url = getLoadUrl();
        mIsCache = getIsCache();

        //todo remove below url param
        //url = "https://chn.nooie.com/v1/pack/failure";
        //url = "file:///android_asset/html/test_api.html";
        if (url == null || url.isEmpty()) {
            finish();
            return;
        }
        mWXH5PayHandler = new WXH5PayHandler();
        initView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideWXUninstallDialog();
        if (webView != null) {
            try {
                webView.destroy();
                webView = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mWXH5PayHandler != null) {
            mWXH5PayHandler = null;
        }
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        ivRight.setVisibility(View.GONE);
        updateTitle(getPageTitle());
        setupWebView();
    }

    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAppCacheEnabled(mIsCache);
        if (!mIsCache) {
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        }
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setUserAgentString(NooieApplication.getUserAgent());
        webView.setBackgroundColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.transparent));
        setupWebViewClient();

        long timestamp = System.currentTimeMillis()/1000L;
        /*
        String sign = TextUtils.isEmpty(mUid) || TextUtils.isEmpty(mToken) ? NooieEncrypt.signWithoutToken(ApiConstant.API_SECRET, ApiConstant.APP_ID, timestamp) :
                NooieEncrypt.signWithToken(ApiConstant.API_SECRET, ApiConstant.APP_ID, timestamp, mUid, mToken);
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put(ApiConstant.API_KEY_APP_ID, ApiConstant.APP_ID);
         */
        String sign = TextUtils.isEmpty(mUid) || TextUtils.isEmpty(mToken) ? NooieEncrypt.signWithoutToken(NetConfigure.getInstance().getAppSecret(), NetConfigure.getInstance().getAppId(), timestamp) :
                NooieEncrypt.signWithToken(NetConfigure.getInstance().getAppSecret(), NetConfigure.getInstance().getAppId(), timestamp, mUid, mToken);
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put(ApiConstant.API_KEY_APP_ID, NetConfigure.getInstance().getAppId());
        headerMap.put(ApiConstant.API_KEY_TIMESTAMP, String.valueOf(timestamp));
        headerMap.put(ApiConstant.API_KEY_UID, mUid);
        headerMap.put(ApiConstant.API_KEY_TOKEN, mToken);
        headerMap.put(ApiConstant.API_KEY_SIGN, sign);
        webView.loadUrl(url, headerMap);
    }

    private void setupWebViewClient() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleWxUrl(view, url, super.shouldOverrideUrlLoading(webView, url));
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (isDestroyed()) {
                    return;
                }
                hideLoading();
                eventInterceptorOnPageFinished(url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (isDestroyed()) {
                    return;
                }
                showLoading();
                eventInterceptorOnPageStarted(url);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                NooieLog.d("-->> HybridWebViewActivity onReceivedTitle title=" + title);
                updateTitle(title);
            }
        });
    }

    @OnClick({R.id.ivLeft})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                tryGoBackPage();
                break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        tryGoBackPage();
    }

    public boolean handleWxUrl(WebView view, String url, boolean superResult) {
        if (isDestroyed() || TextUtils.isEmpty(url)) {
            return true;
        }

        Uri uri = null;
        try {
            uri = Uri.parse(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (uri == null) {
            return true;
        }

        if (mWXH5PayHandler == null) {
            mWXH5PayHandler = new WXH5PayHandler();
        }

        NooieAppModule nooieAppModule = new NooieAppModule(new NooieAppModule.NooieAppModuleCallback() {
            @Override
            public void onActionCallback(String action, String url) {
                if (isDestroyed() || webView == null) {
                    return;
                }
                if (!TextUtils.isEmpty(action)) {
                    if (action.equals(NooieAppModule.HYBRID_ACTION_GET_USER_INFO)) {
                        Gson gson = new Gson();
                        UserInfo userInfo = new UserInfo();
                        userInfo.setUid(mUid);
                        //userInfo.setAppId(ApiConstant.APP_ID);
                        userInfo.setAppId(NetConfigure.getInstance().getAppId());
                        String result = gson.toJson(userInfo);
                        //NooieLog.d("-->> HybridWebViewActivity onActionCallback returnUserInfo result=" + result);
                        webView.loadUrl("javascript:returnUserInfo(" + result + ")");
                    } else if (action.equals(NooieAppModule.HYBRID_ACTION_GO_HOME)) {
                        HomeActivity.toHomeActivity(HybridWebViewActivity.this);
                        finish();
                    } else if (action.equals(NooieAppModule.HYBRID_ACTION_GET_REGION)) {
                        String region = LanguageUtil.getLocal(NooieApplication.mCtx).getLanguage();
                        //NooieLog.d("-->> HybridWebViewActivity onActionCallback returnRegion region=" + region);
                        webView.loadUrl("javascript:returnRegion(" + region + ")");
                    } else if (action.equalsIgnoreCase(NooieAppModule.HYBRID_ACTION_UPDATE_TITLE)) {
                        //nooieapp://update_title?title=value
                        if (TextUtils.isEmpty(url)) {
                            return;
                        }
//                        try {
//                            NooieLog.d("-->> HybridWebViewActivity onActionCallback update title url=" + url);
//                            Uri uri = Uri.parse(url);
//                            /*
//                            if (uri.getQueryParameterNames() != null) {
//                                for (String paramName : uri.getQueryParameterNames()) {
//                                    NooieLog.d("-->> HybridWebViewActivity onActionCallback update title paramName=" + paramName);
//                                }
//                            }
//                            */
//                            String title = uri != null ? uri.getQueryParameter("title") : "";
//                            NooieLog.d("-->> HybridWebViewActivity onActionCallback update title title=" + title);
//                            updateTitle(title);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                        String title = HybridHelper.getUrlParam(url, NooieAppModule.HYBRID_KEY_PAGE_TITLE);
                        NooieLog.d("-->> HybridWebViewActivity onActionCallback update title title=" + title);
                        updateTitle(title);
                    } else if (action.equalsIgnoreCase(NooieAppModule.HYBRID_ACTION_PAGE_GO_BACK)) {
                        if (TextUtils.isEmpty(url)) {
                            return;
                        }
//                        try {
//                            NooieLog.d("-->> HybridWebViewActivity onActionCallback page go back url=" + url);
//                            Uri uri = Uri.parse(url);
//                            mCurrentPageGoBack = uri != null ? uri.getQueryParameter(NooieAppModule.HYBRID_KEY_PAGE_GO_BACK_HOME) : NooieAppModule.HYBRID_PAGE_GO_BACK_APP;
//                            NooieLog.d("-->> HybridWebViewActivity onActionCallback page go back home=" + mCurrentPageGoBack);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                        String currentPageGoBack = HybridHelper.getUrlParam(url, NooieAppModule.HYBRID_KEY_PAGE_GO_BACK_HOME);
                        mCurrentPageGoBack = !TextUtils.isEmpty(currentPageGoBack) ? currentPageGoBack : NooieAppModule.HYBRID_PAGE_GO_BACK_APP;
                        NooieLog.d("-->> HybridWebViewActivity onActionCallback page go back home=" + mCurrentPageGoBack);
                    }
                }
            }
        });

        if (!URLUtil.isNetworkUrl(url)) {
            if (nooieAppModule.parseRequestUrl(url)) {
            } else if (mWXH5PayHandler != null && mWXH5PayHandler.isWXLaunchUrl(url)) {
                //  处理微信h5支付2
                boolean launchResult = mWXH5PayHandler.launchWX(view, url);
                if (!launchResult && view != null) {
                    view.goBack();
                    showWXUninstallDialog();
                }
            } else {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    view.getContext().startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return true;
        }

        if (WXH5PayHandler.isWXH5Pay(url)) {
            // 处理微信h5支付1
            //mWXH5PayHandler = new WXH5PayHandler();
            return mWXH5PayHandler.pay(url);
        } else if (mWXH5PayHandler != null) {
            // 处理微信h5支付3
            if (mWXH5PayHandler.isRedirectUrl(url)) {
                boolean result = mWXH5PayHandler.redirect();
                //mWXH5PayHandler = null;
                return result;
            }
            //mWXH5PayHandler = null;
        }

        return superResult;
    }

    private Dialog mWXUninstallDialog;

    private void showWXUninstallDialog() {
        hideWXUninstallDialog();
        mWXUninstallDialog = DialogUtils.showInformationDialog(this, getString(R.string.dialog_tip_title), getString(R.string.hybrid_wx_uninstall_content), getString(R.string.hybrid_wx_uninstall_install), true, false, new DialogUtils.OnClickInformationDialogLisenter() {
            @Override
            public void onConfirmClick() {
            }
        });
    }

    private void hideWXUninstallDialog() {
        if (mWXUninstallDialog != null) {
            mWXUninstallDialog.dismiss();
        }
    }

    private void tryGoBackPage() {
        if (!NooieAppModule.HYBRID_PAGE_GO_BACK_APP.equalsIgnoreCase(mCurrentPageGoBack) && canGoBack()) {
            pageGoBack();
        } else {
            finish();
        }
    }

    private boolean canGoBack() {
        return webView != null && webView.canGoBack();
    }

    private void pageGoBack() {
        if (webView != null) {
            webView.goBack();
        }
    }

    private void updateTitle(String title) {
        if (isDestroyed() || checkNull(tvTitle)) {
            return;
        }
        if (getIsShowTitle()) {
            tvTitle.setText(title);
        } else {
            tvTitle.setText("");
        }
    }

    private void eventInterceptorOnPageStarted(String url) {
        if (isDestroyed() || TextUtils.isEmpty(url)) {
            return;
        }
        if (url.contains(FirebaseConstant.EVENT_URL_CLOUD_PACK_HOME)) {
            String deviceId = HybridHelper.getUrlParam(url, ConstantValue.CLOUD_PACK_PARAM_KEY_UUID);
            BindDevice device = NooieDeviceHelper.getDeviceById(deviceId);
            if (device == null) {
                return;
            }
            String origin = HybridHelper.getUrlParam(url, ConstantValue.CLOUD_PACK_PARAM_KEY_ORIGIN);
            FirebaseEventManager.getInstance().sendCloudPackPageStartLoading(getDeviceId(), device.getName(), device.getType(), origin);
        }
    }

    private void eventInterceptorOnPageFinished(String url) {
        if (isDestroyed() || TextUtils.isEmpty(url)) {
            return;
        }
        if (url.contains(FirebaseConstant.EVENT_URL_CLOUD_PACK_HOME)) {
            String deviceId = HybridHelper.getUrlParam(url, ConstantValue.CLOUD_PACK_PARAM_KEY_UUID);
            BindDevice device = NooieDeviceHelper.getDeviceById(deviceId);
            if (device == null) {
                return;
            }
            String language = LanguageUtil.getLocal(NooieApplication.mCtx).getLanguage();
            String enterMark = HybridHelper.getUrlParam(url, ConstantValue.CLOUD_PACK_PARAM_KEY_ENTER_MARK);
            String origin = HybridHelper.getUrlParam(url, ConstantValue.CLOUD_PACK_PARAM_KEY_ORIGIN);
            FirebaseEventManager.getInstance().sendCloudPackPageFinishLoading(getDeviceId(), device.getName(), device.getType(), origin, language , enterMark, FirebaseConstant.RESULT_SUCCESS);
        }
    }

    @Override
    public String getEventId(int trackType) {
        String url = getIntent() != null ? getIntent().getStringExtra(ConstantValue.INTENT_KEY_URL) : null;
        return HybridHelper.getEventIdByUrl(url, trackType);
    }

    @Override
    public int getTrackType() {
        String url = getIntent() != null ? getIntent().getStringExtra(ConstantValue.INTENT_KEY_URL) : null;
        return HybridHelper.getTrackTypeByUrl(url);
    }

    private String getLoadUrl() {
        return getStartParam() != null ? getStartParam().getString(ConstantValue.INTENT_KEY_URL) : "";
    }

    private String getPageTitle() {
        return getStartParam() != null ? getStartParam().getString(ConstantValue.INTENT_KEY_TITLE) : "";
    }

    private boolean getIsCache() {
        return getStartParam() != null ? getStartParam().getBoolean(ConstantValue.INTENT_KEY_DATA_PARAM, false) : false;
    }

    private boolean getIsShowTitle() {
        return getStartParam() != null ? getStartParam().getBoolean(ConstantValue.INTENT_KEY_DATA_PARAM_1, false) : false;
    }

    private String getDeviceId() {
        return getStartParam() != null ? getStartParam().getString(ConstantValue.INTENT_KEY_DEVICE_ID) : "";
    }
}
