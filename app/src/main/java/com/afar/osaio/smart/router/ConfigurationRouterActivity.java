package com.afar.osaio.smart.router;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.preference.GlobalPrefs;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 配置路由器
 */
public class ConfigurationRouterActivity extends RouterBaseActivity {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.wanType)
    TextView wanType;
    @BindView(R.id.wifiName2_4G)
    TextView wifiName2_4G;
    @BindView(R.id.wifiName5G)
    TextView wifiName5G;

    private String ssid = "";
    private String ssid5g = "";

    public static void toConfigurationRouterActivity(Context from, String ssid, String ssid5g) {
        Intent intent = new Intent(from, ConfigurationRouterActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_ROUTER_SSID, ssid);
        intent.putExtra(ConstantValue.INTENT_KEY_ROUTER_SSID_5G, ssid5g);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration_router);
        ButterKnife.bind(this);

        setupView();
        initData();
    }

    @SuppressLint("SetTextI18n")
    private void setupView() {
        tvTitle.setText(R.string.router_config_title);
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);

        ssid = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_ROUTER_SSID);
        ssid5g = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_ROUTER_SSID_5G);

        wifiName2_4G.setText(ssid);
        wifiName5G.setText(ssid5g);
    }

    @SuppressLint("SetTextI18n")
    private void initData() {
        GlobalPrefs prefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
        List<String> internetModeInfo = prefs.getRouterInternetMode();
        if ("0".equals(internetModeInfo.get(0))) {
            wanType.setText(R.string.router_config_static_ip);
        } else if ("1".equals(internetModeInfo.get(0))) {
            wanType.setText(R.string.router_config_dhcp);
        } else if ("3".equals(internetModeInfo.get(0))) {
            wanType.setText(R.string.router_config_pppoe);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @OnClick({R.id.ivLeft, R.id.btnBackup, R.id.tvNewConfiguration})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnBackup:
                RouterInternetModeSettingActivity.toRouterInternetModeSettingActivity(ConfigurationRouterActivity.this, "backUp");
                break;
            case R.id.tvNewConfiguration:
                RouterInternetModeSettingActivity.toRouterInternetModeSettingActivity(ConfigurationRouterActivity.this);
                break;
        }
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

}
