package com.afar.osaio.smart.router;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.receiver.SysWifiStateReceiver;
import com.afar.osaio.smart.event.RouterOnLineStateEvent;
import com.afar.osaio.widget.WiFiDialog;
import com.nooie.common.utils.configure.CountryUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 路由器基类
 */
public class RouterBaseActivity extends BaseActivity {

    private WiFiDialog wiFiDialog;
    private SysWifiStateReceiver sysWifiStateReceiver; // wifi 监听
    private boolean hasShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listenWifiState();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void routerOnLineState(RouterOnLineStateEvent event) {
        if (!hasShow) { // 展示过一次就不给展示了
            showOnlineStateDialog();
        }
        hasShow = true;
    }

    /**
     * 监听wifi状态
     */
    private void listenWifiState() {
        sysWifiStateReceiver = new SysWifiStateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        if (null != sysWifiStateReceiver) {
            registerReceiver(sysWifiStateReceiver, filter);
        }
    }

    /**
     * 显示路由器在线状态
     */
    private void showOnlineStateDialog() {
        if (null == wiFiDialog) {
            wiFiDialog = new WiFiDialog(this, true);
        }
        if (!wiFiDialog.isShowing()) {
            wiFiDialog.show();
        }
    }

    /**
     * 路由器有关时区tz设置的问题：时区需要反着来,比如UTC+08:00 需要转换为 UTC-8(否则设置会有问题)
     * @return
     */
    public String timeZoneConversion() {
        String timeZone = CountryUtil.getTimeZone();
        StringBuilder stringBuilder = new StringBuilder();
        if (timeZone.contains("+")) {
            stringBuilder.append("-");
        } else {
            stringBuilder.append("+");
        }

        if (Integer.parseInt(timeZone.substring(1,2)) == 0) {
            stringBuilder.append(timeZone.substring(2,3));
        } else {
            stringBuilder.append(timeZone.substring(1,3));
        }
        return stringBuilder.toString();
    }

    /**
     * 挑选当前路由器在线设备
     * @throws JSONException
     */
    public JSONArray dealwithOnlineDevice(String routerReturnOnlineMsg) throws JSONException {
        JSONObject jsonObject = new JSONObject(routerReturnOnlineMsg);
        JSONArray jsonArrayWhite = new JSONArray();
        JSONArray jsonArrayBlack = new JSONArray();
        JSONArray jsonArrayOnlineDevice = new JSONArray();

        jsonArrayWhite = jsonObject.getJSONArray("white");
        jsonArrayBlack = jsonObject.getJSONArray("black");
        for (int i = 0; i < jsonArrayWhite.length(); i++) {
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("type", 1); // white
            jsonObject1.put("mac", jsonArrayWhite.getJSONObject(i).get("mac"));
            jsonObject1.put("ip", jsonArrayWhite.getJSONObject(i).get("ip"));
            jsonObject1.put("name", jsonArrayWhite.getJSONObject(i).get("name"));
            jsonObject1.put("linkType", jsonArrayWhite.getJSONObject(i).get("linkType"));
            jsonArrayOnlineDevice.put(jsonObject1);
        }

        for (int i = 0; i < jsonArrayBlack.length(); i++) {
            JSONObject jsonObject1 = new JSONObject();
            jsonObject1.put("type", 0); // black
            jsonObject1.put("mac", jsonArrayBlack.getJSONObject(i).get("mac"));
            jsonObject1.put("ip", jsonArrayBlack.getJSONObject(i).get("ip"));
            jsonObject1.put("name", jsonArrayBlack.getJSONObject(i).get("name"));
            jsonObject1.put("linkType", jsonArrayBlack.getJSONObject(i).get("linkType"));
            jsonArrayOnlineDevice.put(jsonObject1);
        }
        return jsonArrayOnlineDevice;
    }


    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != sysWifiStateReceiver) {
            unregisterReceiver(sysWifiStateReceiver);
        }
    }
}