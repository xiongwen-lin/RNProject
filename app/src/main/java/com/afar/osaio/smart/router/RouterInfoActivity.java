package com.afar.osaio.smart.router;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
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

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RouterInfoActivity extends RouterBaseActivity implements SendHttpRequest.getRouterReturnInfo {

    public static void toRouterInfoActivity(Context from, String routerName, String routerMac) {
        Intent intent = new Intent(from, RouterInfoActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_NAME, routerName);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_MAC, routerMac);
        from.startActivity(intent);
    }

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.routerName)
    TextView routerName;
    @BindView(R.id.routerModel)
    TextView routerModel;
    @BindView(R.id.routerMac)
    TextView routerMac;
    @BindView(R.id.routerSerial)
    TextView routerSerial;
    @BindView(R.id.routerFirstIp)
    TextView routerFirstIp;
    @BindView(R.id.routerSecIp)
    TextView routerSecIp;
    @BindView(R.id.routerPer)
    TextView routerPer;
    @BindView(R.id.routerAlt)
    TextView routerAlt;
    @BindView(R.id.routerOnlineTime)
    TextView routerOnlineTime;

    private String routerNameString;
    private String routerMacString;
    private String sysStatusInfo;
    private String routerUUid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_info);
        ButterKnife.bind(this);

        initView();
        initData();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.router_detail_setting_device_information);
    }

    private void initData() {
        routerNameString = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_NAME);
        routerMacString = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_MAC);

        routerName.setText(routerNameString);
        routerMac.setText(routerMacString);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSysStatusCfg();
        getRouterUUid();
    }

    @OnClick({R.id.ivLeft})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
        }
    }

    private void dealwithViewShow() {
        try {
            JSONObject jsonObject = new JSONObject(sysStatusInfo);
            routerModel.setText(jsonObject.getString("model").equals("R2") ? "R2" : jsonObject.getString("model"));
            routerFirstIp.setText(jsonObject.getString("lanIp"));
            routerPer.setText(jsonObject.getString("priDns"));
            String[] times = jsonObject.getString("wanConnTime").split(";");
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(times[0]).append("d ").append(times[1]).append("h ").append(times[2]).append("m ").append(times[3]).append("s");
            routerOnlineTime.setText(stringBuilder.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getSysStatusCfg() {
        showLoadingDialog();
        GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
        try {
            routerDataFromCloud.getSysStatusCfg();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getRouterUUid() {
        GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
        try {
            routerDataFromCloud.getRouterUUid();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                dealwithViewShow();
            } else if (msg.what == 2) {
                formatDeviceId();
            } else {
                EventBus.getDefault().post(new RouterOnLineStateEvent());
            }
        }
    };

    @Override
    public void routerReturnInfo(String info, String topicurlString) {
        hideLoadingDialog();
        try {
            Message message = new Message();
            if (!"error".equals(info) && "getSysStatusCfg".equals(topicurlString)) {
                sysStatusInfo = info;
                message.what = 1;
            } else if (!"error".equals(info) && "getUuidInfo".equals(topicurlString)) {
                routerUUid = new JSONObject(info).getString("device_uuid");
                message.what = 2;
            } else {
                message.what = 0;
            }
            handler.sendMessage(message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 截取 设备ID 对齐
     */
    private void formatDeviceId() {
        if (null != routerUUid && !TextUtils.isEmpty(routerUUid)) {
            int size = routerUUid.length();
            String deviceID = routerUUid.substring(0, size / 2) + "\n" + routerUUid.substring(size / 2, size);
            routerSerial.setText(deviceID);
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

    @Override
    public void showLoadingDialog() {
        showLoading("");
    }

    @Override
    public void hideLoadingDialog() {
        hideLoading();
    }
}
