package com.afar.osaio.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.afar.osaio.widget.WiFiDialog;

/**
 * Create by Raymond  on 2021/6/9
 * Description:  系统路由器连接状态监听
 */
public class SysWifiStateReceiver extends BroadcastReceiver {


    private WiFiDialog wiFiDialog;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {

            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {

            } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            }
        }

        //wifi打开与否
        if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
            int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);
            if (wifistate == WifiManager.WIFI_STATE_DISABLED) {

            } else if (wifistate == WifiManager.WIFI_STATE_ENABLED) {

            } else if (wifistate == WifiManager.WIFI_STATE_DISABLING) {
                showWiFiDialog(context);
            } else if (wifistate == WifiManager.WIFI_STATE_ENABLING) {
                hideWifiDialog();
            }
        }
    }

    /**
     * 显示对话
     */
    private void showWiFiDialog(Context context) {
        if (null == wiFiDialog) {
            wiFiDialog = new WiFiDialog(context,false);
        }
        wiFiDialog.show();
    }


    /**
     * 隐藏对话框
     */
    private void hideWifiDialog() {
        if (null != wiFiDialog) {
            wiFiDialog.dismiss();
        }
    }


}
