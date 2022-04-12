package com.afar.osaio.smart.router;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.afar.osaio.R;
import com.afar.osaio.application.activity.WebViewActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.routerdata.GetRouterDataFromCloud;
import com.afar.osaio.routerdata.SendHttpRequest;
import com.afar.osaio.smart.event.RouterOnLineStateEvent;
import com.afar.osaio.smart.routerlocal.internet.InternetConnectionStatus;
import com.afar.osaio.util.CommonUtil;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.afar.osaio.widget.FButton;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.configure.FontUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 路由器连接结果
 */
public class ConnectionRouterActivity extends RouterBaseActivity implements SendHttpRequest.getRouterReturnInfo {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.connectionStatus)
    TextView connectionStatus;
    @BindView(R.id.protocol)
    CheckBox protocol;
    @BindView(R.id.tvProtocol)
    TextView tvProtocol;
    @BindView(R.id.tvBtn)
    FButton tvBtn;

    private String ssid = "";
    private String ssid5G = "";
    private String mac = "";
    private JSONObject jsonObjectInfo = new JSONObject();

    public static void toConnectionRouterActivity(Context from) {
        Intent intent = new Intent(from, ConnectionRouterActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_router);
        ButterKnife.bind(this);

        setupView();
        initData();
    }

    @SuppressLint("SetTextI18n")
    private void setupView() {
        tvTitle.setText(R.string.router_connect_result_device_title);
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        checkBtnEnable(false);

        protocol.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                checkBtnEnable(b);
            }
        });
    }

    private void initData() {
        String conditionUse = getString(R.string.terms_of_service);
        String privacy = getString(R.string.privacy_policy);
        String urlString = String.format(getString(R.string.sign_up_create_account_tip), conditionUse, privacy);


        SpannableStringBuilder style = new SpannableStringBuilder();
        style.append(urlString);

        //设置部分文字点击事件
        ClickableSpan conditionClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                if (InternetConnectionStatus.isNetSystemUsable()) {
                    WebViewActivity.toWebViewActivity(ConnectionRouterActivity.this, WebViewActivity.getUrl(CommonUtil.getTerms(NooieApplication.mCtx)), getString(R.string.terms_of_service));
                } else {
                    ToastUtil.showToast(ConnectionRouterActivity.this, getString(R.string.router_connect_result_no_network));
                }
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
                ds.setTypeface(FontUtil.loadTypeface(getApplicationContext(), "fonts/manrope-semibold.otf"));
            }
        };
        style.setSpan(conditionClickableSpan, urlString.indexOf(conditionUse), urlString.indexOf(conditionUse) + conditionUse.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvProtocol.setText(style);

        //设置部分文字点击事件
        ClickableSpan privacyClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                if (InternetConnectionStatus.isNetSystemUsable()) {
                    WebViewActivity.toWebViewActivity(ConnectionRouterActivity.this, WebViewActivity.getUrl(CommonUtil.getPrivacyPolicyByCountry(NooieApplication.mCtx, CountryUtil.getCurrentCountry(NooieApplication.mCtx))), getString(R.string.privacy_policy));
                } else {
                    ToastUtil.showToast(ConnectionRouterActivity.this, getString(R.string.router_connect_result_no_network));
                }
            }
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
                ds.setTypeface(FontUtil.loadTypeface(getApplicationContext(), "fonts/manrope-semibold.otf"));
            }
        };
        style.setSpan(privacyClickableSpan, urlString.indexOf(privacy), urlString.indexOf(privacy) + privacy.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvProtocol.setText(style);

        //设置部分文字颜色
        ForegroundColorSpan conditionForegroundColorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.theme_green_subtext_color));
        style.setSpan(conditionForegroundColorSpan, urlString.indexOf(conditionUse), urlString.indexOf(conditionUse) + conditionUse.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ForegroundColorSpan privacyForegroundColorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.theme_green_subtext_color));
        style.setSpan(privacyForegroundColorSpan, urlString.indexOf(privacy), urlString.indexOf(privacy) + privacy.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //配置给TextView
        tvProtocol.setMovementMethod(LinkMovementMethod.getInstance());
        tvProtocol.setText(style);
    }

    public void checkBtnEnable(boolean isCheck) {
        tvBtn.setEnabled(isCheck);
        tvBtn.setTextColor(isCheck ? getResources().getColor(R.color.theme_green_subtext_color) : getResources().getColor(R.color.unable_clickable_color));
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @OnClick({R.id.ivLeft, R.id.tvBtn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.tvBtn:
                isConnectWan();
                break;
        }
    }

    private void isConnectWan() {
        showLoadingDialog();
        GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
        try {
            routerDataFromCloud.getSysStatusCfg();
        } catch (JSONException e) {
            e.printStackTrace();
            hideLoadingDialog();
            EventBus.getDefault().post(new RouterOnLineStateEvent());
            //ToastUtil.showToast(ConnectionRouterActivity.this, getString(R.string.router_connect_result_get_info_fail));
        }
    }

    private void dealwithRouterInfo(JSONObject jsonObject) {
        try {
            hideLoadingDialog();
            if ("connected".equals(jsonObject.getString("wanConnStatus"))) {
                GlobalPrefs prefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
                String isBackup = prefs.getRouterIsBackup();
                if ("true".equals(isBackup)) {
                    ConfigurationRouterActivity.toConfigurationRouterActivity(ConnectionRouterActivity.this, ssid, ssid5G);
                } else {
                    RouterInternetModeSettingActivity.toRouterInternetModeSettingActivity(ConnectionRouterActivity.this);
                }
            } else {
                RouterNotOnlineActivity.toRouterNotOnlineActivity(ConnectionRouterActivity.this, ssid, ssid5G);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void dealwithRouterStatusInfo(JSONObject jsonObject) {
        GlobalPrefs prefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
        try {
            ssid = jsonObject.getString("ssid");
            ssid5G = jsonObject.getString("ssid5g");
            mac = jsonObject.getString("lanMac");
            prefs.setRouterMac(mac);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (jsonObjectInfo != null) {
            dealwithRouterInfo(jsonObjectInfo);
        }
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                dealwithRouterStatusInfo(jsonObjectInfo);
            } else if (msg.what == 1) {
                EventBus.getDefault().post(new RouterOnLineStateEvent());
                //ToastUtil.showToast(ConnectionRouterActivity.this, getString(R.string.router_connect_result_get_info_fail));
            }
        }
    };

    @Override
    public void routerReturnInfo(String info, String topicurlString) {
        hideLoadingDialog();
        try {
            Message message = new Message();
            if ("getSysStatusCfg".equals(topicurlString) && !"error".equals(info) && !TextUtils.isEmpty(info)) {
                jsonObjectInfo = new JSONObject(info);
                message.what = 0;
            } else /*if ("error".equals(info) || "".equals(info)) */ {
                message.what = 1;
            }
            mHandler.sendMessage(message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void showLoadingDialog() {
        showLoading("");
    }

    @Override
    public void hideLoadingDialog() {
        hideLoading();
    }

}
