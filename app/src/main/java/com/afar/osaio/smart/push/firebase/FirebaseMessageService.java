package com.afar.osaio.smart.push.firebase;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.afar.osaio.base.NooieApplication;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.afar.osaio.smart.push.PushMsgManager;
import com.afar.osaio.smart.push.firebase.helper.FirebaseHelper;
import com.afar.osaio.smart.push.helper.NooiePushMsgHelper;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.notify.NotificationUtil;

import java.util.Map;

/**
 * Created by victor on 2018/9/25
 * Email is victor.qiao.0604@gmail.com
 */
public class FirebaseMessageService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        NooieLog.d("-->> FirebaseMessageService onMessageReceived id: " + remoteMessage.getMessageId() + " from: " + remoteMessage.getFrom());
        RemoteMessage.Notification notification = remoteMessage.getNotification();

        Map<String, String> data = remoteMessage.getData();
        if (remoteMessage != null && data != null) {
            String extra = FirebaseHelper.convertFcmToJPush(data);
            if (!TextUtils.isEmpty(extra)) {
                //PushMsgManager.getInstance().convertCustomMessage(extra);
                PushMsgManager.getInstance().convertCustomMessage(extra, notification);
                Bundle pushBundle = new Bundle();
                pushBundle.putString(NooiePushMsgHelper.NOOIE_PUSH_MSG_EXTRA, extra);
                sendReceiveBroadcast(this, pushBundle);
            }
        }
    }

    @Override
    public void onNewToken(String token) {
        Log.d("FirebaseMessageService", "-->> onNewToken token:" + token);
    }

    private void sendReceiveBroadcast(Context context, Bundle bundle) {
        Intent pushIntent = new Intent();
        pushIntent.setAction(ConstantValue.BROADCAST_KEY_RECEIVE_JG_PUSH);
        pushIntent.putExtra(ConstantValue.INTENT_KEY_RECEIVE_JG_PUSH, bundle);
        NotificationUtil.sendBroadcast(NooieApplication.mCtx, pushIntent);
    }
}