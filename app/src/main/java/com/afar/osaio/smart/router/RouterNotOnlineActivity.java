package com.afar.osaio.smart.router;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afar.osaio.R;
import com.afar.osaio.routerdata.GetRouterDataFromCloud;
import com.afar.osaio.routerdata.SendHttpRequest;
import com.afar.osaio.smart.event.RouterOnLineStateEvent;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.FButton;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RouterNotOnlineActivity extends RouterBaseActivity implements SendHttpRequest.getRouterReturnInfo {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.btn_testagain)
    FButton btn_testagain;
    @BindView(R.id.tvTips4)
    TextView tvTips4;

    private String ssid = "";
    private String ssid5g = "";
    private String routerReturnInfo;

    public static void toRouterNotOnlineActivity(Context from, String ssid, String ssid5g) {
        Intent intent = new Intent(from, RouterNotOnlineActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_ROUTER_SSID, ssid);
        intent.putExtra(ConstantValue.INTENT_KEY_ROUTER_SSID_5G, ssid5g);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_not_online);
        ButterKnife.bind(this);

        initView();
        //initData();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.router_offline_help);

        ssid = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_ROUTER_SSID);
        ssid5g = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_ROUTER_SSID_5G);
    }

    private void initData() {
        /*String string1 = getString(R.string.router_not_network_tip);
        String string2 = getString(R.string.router_not_network_connect_us);
        String urlString = string1 + string2;
        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        stringBuilder.append(urlString);

        //设置部分文字点击事件
        ClickableSpan conditionClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                FeedbackActivity.toFeedbackActivity(RouterNotOnlineActivity.this);
            }
        };
        stringBuilder.setSpan(conditionClickableSpan, urlString.indexOf(string2), urlString.indexOf(string2) + string2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvTips4.setText(stringBuilder);

        //设置部分文字颜色
        ForegroundColorSpan conditionForegroundColorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.theme_blue));
        stringBuilder.setSpan(conditionForegroundColorSpan, urlString.indexOf(string2), urlString.indexOf(string2) + string2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //配置给TextView
        tvTips4.setMovementMethod(LinkMovementMethod.getInstance());
        tvTips4.setText(stringBuilder);*/
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void dealwithRouterInfo(JSONObject jsonObject) {
        try {
            if ("connected".equals(jsonObject.getString("wanConnStatus"))) {
                RouterInternetModeSettingActivity.toRouterInternetModeSettingActivity(RouterNotOnlineActivity.this);
            } else {
                ToastUtil.showToast(this, getString(R.string.router_network_exception));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.ivLeft, R.id.btn_testagain, R.id.tv_jumpover})
    public void onViewClicked(View v){
        switch(v.getId()) {
            case R.id.btn_testagain:
                isConnectWan();
                break;
            case R.id.tv_jumpover:
                RouterInternetModeSettingActivity.toRouterInternetModeSettingActivity(RouterNotOnlineActivity.this);
                break;
            case R.id.ivLeft:
                finish();
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
        }
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            try {
                if (msg.what == 0) {
                    dealwithRouterInfo(new JSONObject(routerReturnInfo));
                } else {
                    EventBus.getDefault().post(new RouterOnLineStateEvent());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void routerReturnInfo(String info, String topicurlString) {
        hideLoadingDialog();
        Message message = new Message();
        if (!"error".equals(info) && "getSysStatusCfg".equals(topicurlString)) {
            routerReturnInfo = info;
            message.what = 0;
        } else {
            message.what = 1;
        }
        handler.sendMessage(message);
    }

    @Override
    protected void onPause() {
        super.onPause();
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
