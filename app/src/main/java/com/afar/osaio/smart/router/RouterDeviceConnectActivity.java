package com.afar.osaio.smart.router;

import android.annotation.SuppressLint;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afar.osaio.R;
import com.afar.osaio.routerdata.GetRouterDataFromCloud;
import com.afar.osaio.routerdata.SendHttpRequest;
import com.afar.osaio.smart.device.bean.RouterDeviceConnectInfo;
import com.afar.osaio.smart.event.RouterOnLineStateEvent;
import com.afar.osaio.smart.home.adapter.OnlineDeviceAdapter;
import com.afar.osaio.util.ConstantValue;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RouterDeviceConnectActivity extends RouterBaseActivity implements OnlineDeviceAdapter.OnRouterDevicesClickListener, SendHttpRequest.getRouterReturnInfo {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.deviceRecy)
    RecyclerView deviceRecy;

    private OnlineDeviceAdapter onlineDeviceAdapter;
    private JSONArray onlineMsgJson;
    private String routerReturnOnlineMsg = "";
    private int mPosition = 0;
    private String mDeviceName = "";
    private String isWhite = "1";
    private List<RouterDeviceConnectInfo> routerOnlineDeviceList = new ArrayList<>();

    public static void toRouterDeviceConnectActivity(Context from, String onlineMsgString) {
        Intent intent = new Intent(from, RouterDeviceConnectActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_ONLINE_MSG, onlineMsgString);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_router_device_connect);
        ButterKnife.bind(this);

        initView();
        initData();
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.router_devices_connected);

        deviceRecy.setLayoutManager(new GridLayoutManager(this, 2));
    }

    private void initData() {
        routerReturnOnlineMsg = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_ONLINE_MSG);
        onlineDeviceAdapter = new OnlineDeviceAdapter();
        onlineDeviceAdapter.setRouterDevicesClickListener(this);
        deviceRecy.setAdapter(onlineDeviceAdapter);

        setRouterOnlineDevice();
    }

    private void setRouterOnlineDevice() {
        routerOnlineDeviceList.clear();
        routerOnlineDeviceList.addAll(setOnlineInfo());
        onlineDeviceAdapter.setData(routerOnlineDeviceList);
    }


    private List<RouterDeviceConnectInfo> setOnlineInfo() {
        List<RouterDeviceConnectInfo> routerOnlineDeviceList = new ArrayList<>();

        RouterDeviceConnectInfo deviceConnectInfo2 = new RouterDeviceConnectInfo("Online");
        routerOnlineDeviceList.add(deviceConnectInfo2);

        List<RouterDeviceConnectInfo> onlineDeviceList = setDeviceListInfo();
        if (onlineDeviceList != null && onlineDeviceList.size() > 0) {
            routerOnlineDeviceList.addAll(onlineDeviceList);
        }
        return routerOnlineDeviceList;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getAccessDeviceCfg();
    }

    private void getAccessDeviceCfg() {
        showLoadingDialog();
        try {
            GetRouterDataFromCloud routerDataFromCloud = new GetRouterDataFromCloud(this);
            routerDataFromCloud.getAccessDeviceCfg();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void routerDeviceClick(int position, RouterDeviceConnectInfo device) {
        mPosition = position;
        RouterDeviceConnectSetActivity.toRouterDeviceConnectSetActivity(RouterDeviceConnectActivity.this, device.getDeviceName(), device.getId_address(), device.getDevice_mac(), device.getConnectWifiType(), device.getIsWhite());
    }

    @OnClick({R.id.ivLeft})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            mDeviceName = data.getStringExtra(ConstantValue.INTENT_KEY_DEVICE_NAME);
            isWhite = data.getStringExtra(ConstantValue.INTENT_KEY_DEVICE_IS_WHITE);
            onlineDeviceAdapter.updata(mPosition, mDeviceName, isWhite);
        }
    }


    private List<RouterDeviceConnectInfo> setDeviceListInfo() {
        List<RouterDeviceConnectInfo> deviceList = new ArrayList<>();
        List<String> saveDays = new ArrayList<>();
        try {
            if (routerReturnOnlineMsg != null && !"".equals(routerReturnOnlineMsg)) {
                onlineMsgJson = new JSONArray(routerReturnOnlineMsg);
            } else {
                return null;
            }

            for (int i = 0; i < onlineMsgJson.length(); i++) {
                saveDays.add("" + (i + 1));
                RouterDeviceConnectInfo routerDeviceConnectInfo = null;
                routerDeviceConnectInfo = new RouterDeviceConnectInfo(
                        onlineMsgJson.getJSONObject(i).getString("name"),
                        onlineMsgJson.getJSONObject(i).getString("linkType"),
                        "0.5", "0.6", "6h",
                        true, onlineMsgJson.getJSONObject(i).getString("mac"),
                        onlineMsgJson.getJSONObject(i).getString("ip"),
                        onlineMsgJson.getJSONObject(i).getString("type"));
                deviceList.add(routerDeviceConnectInfo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return deviceList;
    }

    private void routerOnlineDevice() throws JSONException {
        JSONArray jsonArrayOnlineDevice = new JSONArray();
        jsonArrayOnlineDevice = dealwithOnlineDevice(routerReturnOnlineMsg);
        routerReturnOnlineMsg = "";
        routerReturnOnlineMsg = jsonArrayOnlineDevice.toString();
        setRouterOnlineDevice();
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            try {
                if (msg.what == 0) {
                    routerOnlineDevice();
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
        if ("getAccessDeviceCfg".equals(topicurlString) && !"error".equals(info)) {
            routerReturnOnlineMsg = info;
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
