package com.afar.osaio.smart.home.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.base.NooieBaseMainFragment;
import com.afar.osaio.bean.StoreGoods;
import com.afar.osaio.smart.event.TabSelectedEvent;
import com.afar.osaio.smart.hybrid.module.BridgeWebComponent;
import com.afar.osaio.smart.push.firebase.analytics.AnalyticsWebInterface;
import com.afar.osaio.widget.SitePopupWindows;
import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.BridgeWebViewClient;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.nooie.common.base.GlobalData;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.nooie.common.utils.json.GsonHelper;
import com.nooie.common.utils.log.NooieLog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class HomeStoreFragment extends NooieBaseMainFragment {

    private volatile static HomeStoreFragment mStoreFragment;

    @BindView(R.id.webView)
    BridgeWebView webView;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.tvRight)
    TextView tvRight;
    @BindView(R.id.tvTitle)
    TextView tvTitle;

    private View mContentView;
    private Unbinder unbinder;
    private boolean isCurrentFragment = false;
    private String urlRegion;
    private String mCurrentRegion = "US";
    private SitePopupWindows sitePopupWindows;
    /**
     * 1 banner页面
     * 2 亚马逊商城页面
     */
    private int H5_CURRENT_PAGE = 1;
    /**
     * 亚马逊商城页面 h5返回的回调结果保存起来 点击返回的时候需要使用
     */
    private StoreGoods mStoreGoods;
    private BridgeWebComponent mBridgeWebComponent;

    public static HomeStoreFragment newInstance() {
        if (mStoreFragment == null) {
            synchronized (HomeStoreFragment.class) {
                if (mStoreFragment == null) {
                    mStoreFragment = new HomeStoreFragment();
                }
            }
        }
        return mStoreFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_store, container, false);
        unbinder = ButterKnife.bind(this, mContentView);
        initData();
        initView();
        return mContentView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        unbinder.unbind();
        mBridgeWebComponent.unregisterLoadAmazonView();
        mBridgeWebComponent.release();
    }

    private void initData() {
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.home_tab_label_store);
        ivRight.setVisibility(View.INVISIBLE);
        Drawable drawable = getResources().getDrawable(R.drawable.store_drop);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        tvRight.setCompoundDrawables(null, null, drawable, null);
        tvRight.setTextSize(15);
        tvRight.setTextColor(getResources().getColor(R.color.black));
        EventBus.getDefault().register(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setBackgroundColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.transparent));

        webView.setWebViewClient(new BridgeWebViewClient(webView) {

            @Override
            public boolean onCustomShouldOverrideUrlLoading(String url) {
                if (checkActivityIsDestroy() || checkNull(webView)) {
                    return true;
                }
                webView.loadUrl(url);
                return true;
            }

            @Override
            public void onCustomPageFinishd(WebView view, String url){
                if (checkActivityIsDestroy() || checkNull(ivLeft, tvRight)) {
                    return;
                }
                NooieLog.d("-->> debug HomeStoreFragment onPageFinished: url" + url);
                if (url.contains("/mall/index")) {
                    ivLeft.setVisibility(View.INVISIBLE);
                    tvRight.setVisibility(View.VISIBLE);
                } else {
                    ivLeft.setVisibility(View.VISIBLE);
                    tvRight.setVisibility(View.INVISIBLE);
                }
                hideLoading();
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (checkActivityIsDestroy() || checkNull(ivLeft, tvRight)) {
                    return;
                }
                NooieLog.d("-->> debug HomeStoreFragment onPageStarted: url " + url);
                if (isCurrentFragment) {
                    showLoading(true);
                }
                if (url.contains("/mall/index")) {
                    ivLeft.setVisibility(View.INVISIBLE);
                    tvRight.setVisibility(View.VISIBLE);
                } else {
                    ivLeft.setVisibility(View.VISIBLE);
                    tvRight.setVisibility(View.INVISIBLE);
                }
            }
        });
        mBridgeWebComponent = new BridgeWebComponent(webView);
        mBridgeWebComponent.registerLoadBannerView(new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                if (BridgeWebComponent.HANDLE_CALLBACK_FAIL.equalsIgnoreCase(data)) {
                    return;
                }
                H5_CURRENT_PAGE = 1;
            }
        });
        mBridgeWebComponent.registerLoadAmazonView(new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                if (BridgeWebComponent.HANDLE_CALLBACK_FAIL.equalsIgnoreCase(data)) {
                    sendOpenAmazonAppResult(function, false);
                    return;
                }
                NooieLog.d("-->> debug HomeStoreFragment handler: registerLoadAmazonView data=" + data);
                H5_CURRENT_PAGE = 2;
                mStoreGoods = GsonHelper.convertJson(data, StoreGoods.class);
                boolean openResult = tryOpenProductInAmazonApp(mStoreGoods);
                sendOpenAmazonAppResult(function, openResult);
            }
        });

//        webView.registerHandler(loadBannerView, new BridgeHandler() {
//            @Override
//            public void handler(String data, CallBackFunction function) {
//                H5_CURRENT_PAGE = 1;
//                //storeReport(FirebaseConstant.H5_STORE_BANNER_LOADING, getCountryTxt(tvRight));
//            }
//        });
//        webView.registerHandler(loadAmazonView, new BridgeHandler() {
//            @Override
//            public void handler(String data, CallBackFunction function) {
//                H5_CURRENT_PAGE = 2;
//                mStoreGoods = GsonHelper.convertJson(data, StoreGoods.class);
//                //amazonReport(FirebaseConstant.H5_STORE_AMAZON_LOADING);
//            }
//        });

        //storeReport(FirebaseConstant.H5_STORE_LOADING, getCountryTxt(tvRight));
        webView.setOnKeyListener(new View.OnKeyListener() {

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (checkActivityIsDestroy() || checkNull(webView, event)) {
                    return false;
                }
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == MotionEvent.ACTION_UP
                        && webView.canGoBack()) {
                    handler.sendEmptyMessage(1);
                    return true;
                }
                return false;
            }

        });
        addAnalyticsWebInterface(webView);

        reloadStorePage(true);
    }

    @OnClick({R.id.ivLeft, R.id.tvRight})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                backReport();
                webView.goBack();
                break;
            }
            case (R.id.tvRight): {
                showCountryPopMenu();
                break;
            }
        }
    }

    private void showCountryPopMenu() {
        if (sitePopupWindows != null) {
            sitePopupWindows.dismiss();
        }

        sitePopupWindows = new SitePopupWindows(getActivity());

        sitePopupWindows.setListener(new SitePopupWindows.SiteListener() {
            @Override
            public void onSiteItemClick(int position, String region) {
                //storeReport(FirebaseConstant.H5_STORE_SELECT_COUNTRY, getCountryTxt(tvRight));
                if (checkActivityIsDestroy() || checkNull(sitePopupWindows) || TextUtils.isEmpty(region)) {
                    return;
                }
                mCurrentRegion = region;
                tryToLoadRegionUrl(region, mUid, true);
                sitePopupWindows.dismiss();
            }
        });

        sitePopupWindows.setWidth(DisplayUtil.dpToPx(NooieApplication.mCtx, 100));
        sitePopupWindows.showAsDropDown(tvRight, -DisplayUtil.dpToPx(NooieApplication.mCtx, 55), -DisplayUtil.dpToPx(NooieApplication.mCtx, 10));
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1: {
                    webViewGoBack();
                }
                break;
            }
        }
    };

    private void webViewGoBack() {
        webView.goBack();
    }

    @Subscribe
    public void onTabSelectedEvent(TabSelectedEvent event) {
        NooieLog.d("-->> debug HomeStoreFragment onTabSelectedEvent: position=" + (event != null ? event.position : -1));
        if (event == null || event.position != HomeFragment.THIRD) {
            isCurrentFragment = false;
            return;
        }
        isCurrentFragment = true;
        reloadStorePage(false);
    }

    private void backReport() {
        if (H5_CURRENT_PAGE == 1) {
            //storeReport(FirebaseConstant.H5_STORE_BANNER_BACK, getCountryTxt(tvRight));
        } else if (H5_CURRENT_PAGE == 2) {
            //amazonReport(FirebaseConstant.H5_STORE_AMAZON_BACK);
        }
    }

    private String getCountryTxt(TextView tvRight) {
        String country = null;
        if (tvRight != null) {
            country = tvRight.getText().toString();
        }
        if (!TextUtils.isEmpty(country)) {
            return country;
        }
        return null;
    }

    /**
     * @param key
     * @param country
     */
    private void storeReport(String key, String country) {
        /*
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseConstant.H5_STORE_COUNTRY, country);
        FirebaseAnalyticsUtils.getInstance().reportWithParas(key, bundle);

         */
    }

    /**
     * @param key
     */
    private void amazonReport(String key) {
        /*
        Bundle bundle = new Bundle();
        if (mStoreGoods != null) {
            bundle.putString(FirebaseConstant.DISCOUNT_CODE, mStoreGoods.code);
            bundle.putString(FirebaseConstant.H5_STORE_COUNTRY, mStoreGoods.station_code);
            bundle.putString(FirebaseConstant.GOODS_NAME, mStoreGoods.name);
            bundle.putString(FirebaseConstant.GOODS_ID, mStoreGoods.id);
            bundle.putString(FirebaseConstant.DISCOUNT_PRICE, mStoreGoods.price);
            bundle.putString(FirebaseConstant.GOODS_ORIGIN_PRICE, mStoreGoods.original_price);
        }
        FirebaseAnalyticsUtils.getInstance().reportWithParas(key, bundle);

         */
    }

    private void reloadStorePage(boolean isResetUrlRegion) {
        if (isResetUrlRegion) {
            mCurrentRegion = convertStoreRegion(CountryUtil.getCurrentCountryKey(NooieApplication.mCtx));
        }
        tryToLoadRegionUrl(mCurrentRegion, mUid, isResetUrlRegion);
    }

    private String createStoreRegionUrl(String region, String uid, boolean isReset) {
        if (!isReset && !TextUtils.isEmpty(urlRegion)) {
            return urlRegion;
        }
        String webUrl = GlobalData.getInstance().getWebUrl();
        if (TextUtils.isEmpty(webUrl) || TextUtils.isEmpty(uid)) {
            urlRegion = "";
            return "";
        }
        String web = "";
        if (webUrl.contains("/v")) {
            String[] urlSplitArray = webUrl.split("/v");
            if (urlSplitArray == null || urlSplitArray.length < 1 || TextUtils.isEmpty(urlSplitArray[0])) {
                urlRegion = null;
                return "";
            }
            web = urlSplitArray[0];
        }
        //urlRegion = web + "/mall/index/v2?station=" + region + "&uid=" + mUid;
        urlRegion = "http://3n023324f9.imdo.co:14519/mall/index/victure?station=US" + "&uid=" + mUid;
        //urlRegion = "file:///android_asset/mall/index/Store.html";
        return urlRegion;
    }

    private String convertStoreRegion(String countryCode) {
        NooieLog.d("-->> debug HomeStoreFragment convertStoreRegion: countryCode  " + countryCode);
        if (TextUtils.isEmpty(countryCode)){
            countryCode = "1";
        }
        if (countryCode.equals("49") || countryCode.equals("48") || countryCode.equals("36") || countryCode.equals("45") || countryCode.equals("380") || countryCode.equals("420") || countryCode.equals("370") ||
                countryCode.equals("371") || countryCode.equals("358") || countryCode.equals("46") || countryCode.equals("47") || countryCode.equals("31") || countryCode.equals("32") ||
                countryCode.equals("40") || countryCode.equals("359") || countryCode.equals("90")) {
            return "DE";
        } else if (countryCode.equals("39") || countryCode.equals("30") || countryCode.equals("216")) {
            return "IT";
        } else if (countryCode.equals("44") || countryCode.equals("353") || countryCode.equals("354")) {
            return "UK";
        } else if (countryCode.equals("34") || countryCode.equals("351") || countryCode.equals("212") || countryCode.equals("213")) {
            return "ES";
        } else if (countryCode.equals("33")) {
            return "FR";
        } else if (countryCode.equals("81")) {
            return "JP";
        } else {
            return "US";
        }
    }

    private String getStoreRegionOrDefault(String region) {
        return TextUtils.isEmpty(region) ? "US" : region;
    }

    private void tryToLoadRegionUrl(String region, String uid, boolean isReset) {
        region = getStoreRegionOrDefault(region);
        String regionUrl = createStoreRegionUrl(region, uid, isReset);
        if (TextUtils.isEmpty(regionUrl) || checkNull(tvRight, webView)) {
            return;
        }
        tvRight.setText(region);
        webView.loadUrl(regionUrl);
    }

    private boolean tryOpenProductInAmazonApp(StoreGoods storeGoods) {
        if (storeGoods == null || !checkAmazonAppInstall()) {
            return false;
        }
        boolean result = openProductInAmazonApp(storeGoods.amazon_url);
        return result;
    }

    private boolean openProductInAmazonApp(String productUri) {
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(productUri));
            //intent.setData(Uri.parse("com.amazon.mobile.shopping.web://www.amazon.it/gp/product/B07ZQFVDVB"));
            //intent.setData(Uri.parse("com.amazon.mobile.shopping://www.amazon.de/products/B07QPY7XRT"));
            //intent.addCategory(Intent.CATEGORY_DEFAULT);
            //intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
        return false;
    }

    private void sendOpenAmazonAppResult(CallBackFunction function, boolean openResult) {
        if (function == null) {
            return;
        }
        Map<String, Integer> result = new HashMap<>();
        int installValue = openResult ? 1 : 0;
        result.put(BridgeWebComponent.RESPONSE_KEY_INSTALL, installValue);
        function.onCallBack(GsonHelper.convertToJson(result));
    }

    private boolean checkAmazonAppInstall() {
        return checkAppInstalled(NooieApplication.mCtx, "com.amazon.mShop.android.shopping");
    }

    private boolean checkAppInstalled(Context context, String pkgName) {
        if (context == null || TextUtils.isEmpty(pkgName)) {
            return false;
        }
        try {
            final PackageManager packageManager = context.getPackageManager();
            // 获取所有已安装程序的包信息
            List<PackageInfo> info = packageManager.getInstalledPackages(0);
            if(info == null || info.isEmpty())
                return false;
            for ( int i = 0; i < info.size(); i++ ) {
                if(pkgName.equals(info.get(i).packageName)) {
                    return true;
                }
            }
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
        return false;
    }

    private void addAnalyticsWebInterface(BridgeWebView webView) {
        if (checkNull(webView)) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webView.addJavascriptInterface(new AnalyticsWebInterface(), AnalyticsWebInterface.TAG);
        } else {
            NooieLog.d("-->> debug HomeStoreFragment addFirebaseEventInterface Not adding JavaScriptInterface, API Version:" + Build.VERSION.SDK_INT);
        }
    }

}

