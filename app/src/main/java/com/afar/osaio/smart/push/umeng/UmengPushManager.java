package com.afar.osaio.smart.push.umeng;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.push.PushMsgManager;
import com.afar.osaio.smart.push.firebase.helper.FirebaseHelper;
import com.afar.osaio.smart.push.helper.NooiePushMsgHelper;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.notify.NotificationUtil;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.MsgConstant;
import com.umeng.message.PushAgent;
import com.umeng.message.UTrack;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;

public class UmengPushManager {

    private UmengPushManager() {
    }

    private static class UmengPushManagerHolder  {
        public static final UmengPushManager INSTANCE = new UmengPushManager();
    }

    public static UmengPushManager getInstance() {
        return UmengPushManagerHolder.INSTANCE;
    }

    public void initUpush(Context context, boolean isDebug, final IUmengRegisterCallback callback) {
        UMConfigure.setLogEnabled(isDebug);
        UMConfigure.init(context, ConstantValue.UMENG_APP_KEY, NooieApplication.PUSH_CHANNEL_NAME, UMConfigure.DEVICE_TYPE_PHONE, ConstantValue.UMENG_APP_SECRET);

        PushAgent mPushAgent = PushAgent.getInstance(context);

        //sdk开启通知声音
        mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SDK_ENABLE);
        // sdk关闭通知声音
        // mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SDK_DISABLE);
        // 通知声音由服务端控制
        // mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SERVER);

        // mPushAgent.setNotificationPlayLights(MsgConstant.NOTIFICATION_PLAY_SDK_DISABLE);
        // mPushAgent.setNotificationPlayVibrate(MsgConstant.NOTIFICATION_PLAY_SDK_DISABLE);

        UmengMessageHandler messageHandler = new UmengMessageHandler() {

            /**
             * 通知的回调方法（通知送达时会回调）
             */
            @Override
            public void dealWithNotificationMessage(Context context, UMessage msg) {
                //调用super，会展示通知，不调用super，则不展示通知。
                super.dealWithNotificationMessage(context, msg);
                NooieLog.d("-->> umeng dealWithNotificationMessage id=" + msg.message_id + " build_id=" + msg.builder_id + " title=" + msg.title + " text=" + msg.text + " custom=" + msg.custom);
            }

            /**
             * 自定义消息的回调方法
             */
            @Override
            public void dealWithCustomMessage(final Context context, final UMessage msg) {
                if (msg == null) {
                    return;
                }
                NooieLog.d("-->> umeng getNotification id=" + msg.message_id + " build_id=" + msg.builder_id + " title=" + msg.title + " text=" + msg.text + " custom=" + msg.custom);
                String extra = FirebaseHelper.convertFcmToJPush(msg.extra);
                if (!TextUtils.isEmpty(extra)) {
                    //PushMsgManager.getInstance().convertCustomMessage(extra);
                    PushMsgManager.getInstance().convertCustomMessage(extra, msg);
                    Bundle pushBundle = new Bundle();
                    pushBundle.putString(NooiePushMsgHelper.NOOIE_PUSH_MSG_EXTRA, extra);
                    sendReceiveBroadcast(NooieApplication.mCtx, pushBundle);
                }
            }

            /**
             * 自定义通知栏样式的回调方法
             */
            @Override
            public Notification getNotification(Context context, UMessage msg) {
                /*
                if (msg == null) {
                    return super.getNotification(context, msg);
                }
                NooieLog.d("-->> umeng getNotification id=" + msg.message_id + " build_id=" + msg.builder_id + " title=" + msg.title + " text=" + msg.text);
                String extra = FirebaseHelper.convertFcmToJPush(msg.extra);
                if (!TextUtils.isEmpty(extra)) {
                    PushMsgManager.getInstance().convertCustomMessage(extra);
                    Bundle pushBundle = new Bundle();
                    pushBundle.putString(NooieJPushMsgHelper.NOOIE_PUSH_MSG_EXTRA, extra);
                    sendReceiveBroadcast(NooieApplication.mCtx, pushBundle);
                }
                */
                //默认为0，若填写的builder_id并不存在，也使用默认。
                return super.getNotification(context, msg);
            }
        };
        mPushAgent.setMessageHandler(messageHandler);

        /**
         * 自定义行为的回调处理，参考文档：高级功能-通知的展示及提醒-自定义通知打开动作
         * UmengNotificationClickHandler是在BroadcastReceiver中被调用，故
         * 如果需启动Activity，需添加Intent.FLAG_ACTIVITY_NEW_TASK
         * */
        UmengNotificationClickHandler notificationClickHandler = new UmengNotificationClickHandler() {

            @Override
            public void launchApp(Context context, UMessage msg) {
                super.launchApp(context, msg);
            }

            @Override
            public void openUrl(Context context, UMessage msg) {
                super.openUrl(context, msg);
            }

            @Override
            public void openActivity(Context context, UMessage msg) {
                super.openActivity(context, msg);
            }

            @Override
            public void dealWithCustomAction(Context context, UMessage msg) {
            }
        };
        //使用自定义的NotificationHandler
        mPushAgent.setNotificationClickHandler(notificationClickHandler);

        //注册推送服务 每次调用register都会回调该接口
        mPushAgent.register(new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String deviceToken) {
                NooieLog.d("-->> umeng register onSuccess token=" + deviceToken);
                if (callback != null) {
                    callback.onSuccess(deviceToken);
                }
            }

            @Override
            public void onFailure(String s, String s1) {
                NooieLog.d("-->> umeng register onFailure s=" + s + " s1=" +s1);
                if (callback != null) {
                    callback.onFailure(s, s1);
                }
            }
        });

        mPushAgent.setAlias(PushAgent.getInstance(NooieApplication.mCtx).getRegistrationId(), "TUYA_SMART", new UTrack.ICallBack() {
            @Override
            public void onMessage(boolean isSuccess, String message) {
                NooieLog.e("------>Umeng message="+message);
            }
        });

        //使用完全自定义处理
        //mPushAgent.setPushIntentServiceClass(UmengNotificationService.class);

        //小米通道
        //MiPushRegistar.register(this, XIAOMI_ID, XIAOMI_KEY);
        //华为通道
        //HuaWeiRegister.register(this);
        //魅族通道
        //MeizuRegister.register(this, MEIZU_APPID, MEIZU_APPKEY);
    }

    private void sendReceiveBroadcast(Context context, Bundle bundle) {
        if (context == null || bundle == null) {
            return;
        }
        Intent pushIntent = new Intent();
        pushIntent.setAction(ConstantValue.BROADCAST_KEY_RECEIVE_JG_PUSH);
        pushIntent.putExtra(ConstantValue.INTENT_KEY_RECEIVE_JG_PUSH, bundle);
        NotificationUtil.sendBroadcast(context, pushIntent);
    }

}
