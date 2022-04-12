package com.afar.osaio.application.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.hybrid.helper.HybridHelper;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.configure.LanguageUtil;
import com.nooie.common.utils.encrypt.NooieEncrypt;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.core.NetConfigure;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by victor on 2018/8/6
 * Email is victor.qiao.0604@gmail.com
 */
public class WebViewActivity extends BaseActivity {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.webView)
    WebView webView;

    private String url;

    public static void toWebViewActivity(Context from, String url, String title) {
        Intent intent = new Intent(from, WebViewActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_URL, url);
        intent.putExtra(ConstantValue.INTENT_KEY_TITLE, title);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        ButterKnife.bind(this);

        if (getCurrentIntent() != null) {
            url = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_URL);

            tvTitle.setText(getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_TITLE));
        }

        if (url == null || url.isEmpty()) {
            finish();
            return;
        }

        initView();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        ivRight.setVisibility(View.GONE);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setBackgroundColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.transparent));

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
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                hideLoading();
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                showLoading();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            try {
                webView.destroy();
                webView = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick(R.id.ivLeft)
    public void onViewClicked() {
        finish();
    }

    public static String getUrl(String baseUrl) {

        Locale locale = LanguageUtil.getLocal(NooieApplication.get());
        String language = locale.getLanguage();
        String key;
        if (language.contains("zh")) {
            key = "zh";
        } else if (language.contains("ru")) {
            key = "ru";
        } else if (language.contains("pl")) {
            key = "pl";
        } else if (language.contains("it")) {
            key = "it";
        } else if (language.contains("fr")) {
            key = "fr";
        } else if (language.contains("es")) {
            key = "es";
        } else if (language.contains("de")) {
            key = "de";
        } else if (language.contains("ja")) {
            key = "ja";
        } else {
            key = "en";
        }
        return String.format(baseUrl, key);
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
}
