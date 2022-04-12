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
import com.afar.osaio.util.ConstantValue;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ConnectionDeviceDetailsActivity extends RouterBaseActivity {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.deviceName)
    TextView deviceName;
    @BindView(R.id.deviceType)
    TextView deviceType;
    @BindView(R.id.deviceIpAddress)
    TextView deviceIpAddress;
    @BindView(R.id.deviceMacAddress)
    TextView deviceMacAddress;

    private String deviceNameString = "";
    private String ip = "";
    private String mac = "";

    public static void toConnectionDeviceDetailsActivity(Context from, String deviceName, String ip, String mac) {
        Intent intent = new Intent(from, ConnectionDeviceDetailsActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_NAME, deviceName);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_IP, ip);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_MAC, mac);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_device_details);
        ButterKnife.bind(this);

        initView();
        initData();
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.router_device_detail_title);
    }

    private void initData() {
        deviceNameString = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_NAME);
        ip = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_IP);
        mac = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_MAC);
        if (deviceNameString != null && !"".equals(deviceNameString)) {
            deviceName.setText(deviceNameString);
        }

        if (ip != null && !"".equals(ip)) {
            deviceIpAddress.setText(ip);
        }

        if (mac != null && !"".equals(mac)) {
            deviceMacAddress.setText(mac);
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

    @OnClick({R.id.ivLeft})
    public void onViewClicked(View view) {
        switch(view.getId()) {
            case R.id.ivLeft:
                finish();
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
