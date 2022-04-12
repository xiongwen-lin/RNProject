package com.afar.osaio.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.nooie.common.utils.log.NooieLog;

public class WebViewDialogView extends LinearLayout {

    View vWebViewDialogOutsider;
    View vWebViewDialogContentContainer;
    BridgeWebView wvWebViewDialogLoader;
    FButton btnCancel;
    FButton btnConfirm;

    private WebViewDialogListener mListener;

    public WebViewDialogView(Context context) {
        this(context, null);
    }

    public WebViewDialogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public int getLayoutId() {
        return R.layout.mine_layout_webview_dialog;
    }

    public void loadContent(String url) {
        if (wvWebViewDialogLoader == null) {
            return;
        }
        wvWebViewDialogLoader.loadUrl(url);
    }

    public void setCacheEnable(boolean enable) {
        if (wvWebViewDialogLoader == null) {
            return;
        }
        wvWebViewDialogLoader.getSettings().setAppCacheEnabled(enable);
        if (!enable) {
            wvWebViewDialogLoader.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        }
    }

    public void setListener(WebViewDialogListener listener) {
        mListener = listener;
    }

    public void release() {
        if (wvWebViewDialogLoader != null) {
            wvWebViewDialogLoader.setWebViewClient(null);
            wvWebViewDialogLoader.setWebChromeClient(null);
        }
        setListener(null);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(getLayoutId(), this, false);
        addView(view);
        vWebViewDialogOutsider = view.findViewById(R.id.vWebViewDialogOutsider);
        vWebViewDialogContentContainer = view.findViewById(R.id.vWebViewDialogContentContainer);
        wvWebViewDialogLoader = view.findViewById(R.id.wvWebViewDialogLoader);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnConfirm = view.findViewById(R.id.btnConfirm);
        setWebViewSetting(wvWebViewDialogLoader);
        setupWebViewClient(wvWebViewDialogLoader);
        setOnViewClick();
    }

    private void setWebViewSetting(WebView webView) {
        if (webView == null) {
            return;
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setUserAgentString(NooieApplication.getUserAgent());
        webView.setBackgroundColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.transparent));
        setCacheEnable(false);
    }

    private void setupWebViewClient(WebView webView) {
        if (webView == null) {
            return;
        }
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(webView, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (mListener != null) {
                    mListener.onPageFinished();
                }
                //changeBackground(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (mListener != null) {
                    mListener.onPageStarted();
                }
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                NooieLog.d("-->> WebViewDialogView onReceivedTitle title=" + title);
            }
        });
    }

    private void setOnViewClick() {
        if (vWebViewDialogOutsider == null || btnConfirm == null || btnCancel == null) {
            return;
        }
        vWebViewDialogOutsider.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onOutSideClick();
                }
            }
        });

        btnConfirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onConfirm();
                }
            }
        });

        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onCancel();
                }
            }
        });
    }

    private void changeBackground(WebView webView, String url) {
        try {
            if (url.isEmpty()) {
                return;
            }
            if (url.contains("privacy-policy")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript("document.querySelector('.box').style.backgroundColor = '#ffffff';", new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                        }
                    });
                }
            }
        } catch (Exception e) {
        }
    }

    public interface WebViewDialogListener {

        void onCancel();

        void onConfirm();

        void onOutSideClick();

        void onPageStarted();

        void onPageFinished();

    }
}
