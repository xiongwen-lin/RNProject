package com.afar.osaio.smart.push.firebase;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.nooie.common.utils.log.NooieLog;

/**
 * Created by victor on 2018/9/25
 * Email is victor.qiao.0604@gmail.com
 */
public class MyFcmService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {

        String token = FirebaseInstanceId.getInstance().getToken();
        String id = FirebaseInstanceId.getInstance().getId();

        NooieLog.d("-->> MyFcmService onTokenRefresh token=" + token);
        //report to server
        /*
        if (PushUtils.getSupportedPlatform(this,true).contains(PushPlatform.FCM.value()))
            PushUtils.reportPushMetaData("FCM-"+getPackageName(), token);
            */
    }
}
