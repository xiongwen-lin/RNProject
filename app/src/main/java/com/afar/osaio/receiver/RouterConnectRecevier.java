package com.afar.osaio.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.afar.osaio.widget.WiFiDialog;

/**
 * Create by Raymond  on 2021/6/17
 * Description: 监听路由器连接状态
 */
public class RouterConnectRecevier extends BroadcastReceiver {

    private WiFiDialog wiFiDialog;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (null == wiFiDialog){
            wiFiDialog = new WiFiDialog(context,true);
        }

        if (!wiFiDialog.isShowing()){
            wiFiDialog .show();
        }

    }

}
