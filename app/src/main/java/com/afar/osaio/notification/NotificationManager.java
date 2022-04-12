package com.afar.osaio.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import android.text.TextUtils;

import com.afar.osaio.R;
import com.afar.osaio.account.activity.SplashActivity;
import com.afar.osaio.account.helper.MyAccountHelper;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.bean.ListDeviceItem;
import com.afar.osaio.smart.push.bean.PushDetectMessageExtras;
import com.afar.osaio.smart.push.bean.PushFeedbackMessageExtras;
import com.afar.osaio.smart.push.bean.PushMessageBaseExtras;
import com.afar.osaio.smart.push.bean.PushNormalMessageExtras;
import com.afar.osaio.smart.push.bean.PushOrderMessageExtras;
import com.afar.osaio.smart.push.bean.PushOtherLoginMessageExtras;
import com.afar.osaio.smart.push.bean.PushShareMessageExtras;
import com.afar.osaio.smart.push.bean.PushSubscribeMessageExtras;
import com.afar.osaio.smart.push.bean.PushSysMessageExtras;
import com.afar.osaio.smart.push.bean.PushUpdateMessageExtras;
import com.afar.osaio.smart.push.bean.PushActiveMessageExtras;
import com.afar.osaio.smart.push.bean.PushDeviceBoundMessageExtras;
import com.afar.osaio.smart.push.helper.NooiePushMsgHelper;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.PushCode;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
/**
 * Created by victor on 2018/6/27
 * Email is victor.qiao.0604@gmail.com
 */
public class NotificationManager {

    private NotificationManager() {
    }

    private static class NotificationManagerHolder {
        private static final NotificationManager INSTANCE = new NotificationManager();
    }

    public static NotificationManager getInstance() {
        return NotificationManagerHolder.INSTANCE;
    }

    public void showJPushNotification(Context context, PushMessageBaseExtras extras, String title, String content) {

        if (context == null || extras == null) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(NooieApplication.get());
        builder.setAutoCancel(true);

        Intent msgIntent = new Intent();
        msgIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int type = extras != null ? extras.getCode() : PushCode.NORMAL_SYSTEM_MSG.code;
        String deviceId = "";
        String deviceName = "";
        if (extras instanceof PushFeedbackMessageExtras) {
//            PushFeedbackMessageExtras feedbackExtra = (PushFeedbackMessageExtras) extras;
//            title = context.getString(R.string.home_person_feedback);
//            content = !TextUtils.isEmpty(feedbackExtra.getMsg()) ? feedbackExtra.getMsg() : NooiePushMsgHelper.getFeedbackContent(context, feedbackExtra.getFeedback_status(), feedbackExtra.getContent());
        } else if (extras instanceof PushDetectMessageExtras && extras != null) {
            PushDetectMessageExtras detectExtra = (PushDetectMessageExtras) extras;
            if (extras.getCode() == PushCode.DEVICE_SD_LEAK.code) {
                title = context.getString(R.string.application_name);
                content = NooiePushMsgHelper.getDetectPushContent(context, type, detectExtra.getDevice(), detectExtra.getTime());
            }
            deviceId = detectExtra.getDevice_id();
            deviceName = detectExtra.getDevice();
        } else if (extras instanceof PushShareMessageExtras) {
//            PushShareMessageExtras shareExtra = (PushShareMessageExtras) extras;
//            type = shareExtra.getCode();
//            title = context.getString(R.string.share_dialog_title);
//            content = showNooieShareMessage(context, shareExtra);
        } else if (extras instanceof PushUpdateMessageExtras) {
//            PushUpdateMessageExtras updateExtra = (PushUpdateMessageExtras) extras;
//            content = NooiePushMsgHelper.getDeviceUpdateContent(context, updateExtra.getCode(), updateExtra.getMsg());
        } else if (extras instanceof PushOrderMessageExtras) {
            PushOrderMessageExtras orderExtra = (PushOrderMessageExtras) extras;
            title = context.getString(R.string.application_name);
            content = String.format(context.getString(R.string.system_message_pay_nooie_subscribe_successfully_with_trial), orderExtra.getDevice(), orderExtra.getOrder(), orderExtra.getPack());
        } else if (extras instanceof PushSubscribeMessageExtras) {
//            PushSubscribeMessageExtras subscribeExtra = (PushSubscribeMessageExtras) extras;
//            type = subscribeExtra.getCode();
//            if (type == PushCode.CLOUD_SUBSCRIBE_CANCEL.code) {
//                content = String.format(context.getString(R.string.system_message_pay_nooie_subscribe_cancel_with_trial), subscribeExtra.getDevice());
//            } else if (type == PushCode.CLOUD_SUBSCRIBE_RENEWAL.code) {
//                content = String.format(context.getString(R.string.system_message_pay_nooie_subscribe_subscribe_with_trial), subscribeExtra.getDevice());
//            } else if (type == PushCode.CLOUD_SUBSCRIBE_EXPIRED.code) {
//                content = String.format(context.getString(R.string.system_message_pay_nooie_subscribe_expired_with_trial), subscribeExtra.getDevice());
//            }
        } else if (extras instanceof PushSysMessageExtras) {
            PushSysMessageExtras sysExtra = (PushSysMessageExtras) extras;
//            title = context.getString(R.string.system_message);
//            content = sysExtra.getMsg();
        } else if (extras instanceof PushOtherLoginMessageExtras) {
            PushOtherLoginMessageExtras otherLoginExtra = (PushOtherLoginMessageExtras) extras;
            title = context.getString(R.string.application_name);
            content = context.getString(R.string.kick_out_info);
        } else if (extras instanceof PushNormalMessageExtras) {
            PushNormalMessageExtras normalMessageExtra = (PushNormalMessageExtras) extras;
            title = context.getString(R.string.application_name);
            content = normalMessageExtra.getMsg();
        } else if (extras instanceof PushActiveMessageExtras) {
//            PushActiveMessageExtras pushActiveExtra = (PushActiveMessageExtras) extras;
//            title = context.getString(R.string.application_name);
//            content = pushActiveExtra.getTitle();
        } else if (extras instanceof PushDeviceBoundMessageExtras) {
            PushDeviceBoundMessageExtras pushDeviceBoundExtra = (PushDeviceBoundMessageExtras) extras;
            title = context.getString(R.string.application_name);
            content = String.format(context.getString(R.string.push_msg_device_bound_by_other), pushDeviceBoundExtra.getAccount());
        }

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
            return;
        }

        if (type == PushCode.MOTION_DETECT.code || type == PushCode.SOUND_DETECT.code  || type == PushCode.PIR_DETECT.code || type == PushCode.DEVICE_SD_LEAK.code) {
            if (MyAccountHelper.getInstance().isAppBeKilled(context)) {
                msgIntent.setAction(Intent.ACTION_MAIN);
                msgIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                msgIntent.setClass(NooieApplication.get(), SplashActivity.class);
                msgIntent.putExtra(ConstantValue.INTENT_KEY_DATA_TYPE, ConstantValue.NOOIE_MSG_TYPE_DEVICE);
                msgIntent.putExtra(ConstantValue.INTENT_KEY_DEVICE_PLATFORM, ListDeviceItem.DEVICE_PLATFORM_NOOIE);
                msgIntent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
                msgIntent.putExtra(ConstantValue.INTENT_KEY_DEVICE_NAME, deviceName);
            } else if (!TextUtils.isEmpty(deviceId) && !TextUtils.isEmpty(deviceName)) {
                msgIntent.setAction(ConstantValue.INTENT_FILTER_NOOIE_DEVICE);
                msgIntent.putExtra(ConstantValue.INTENT_KEY_DEVICE_PLATFORM, ListDeviceItem.DEVICE_PLATFORM_NOOIE);
                msgIntent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
                msgIntent.putExtra(ConstantValue.INTENT_KEY_DEVICE_NAME, deviceName);
            }
        } else if (type == PushCode.NORMAL_SYSTEM_MSG.code || type == PushCode.BACKGROUND_DEAL_FEEDBACK_STATUS.code || type == PushCode.ORDER_FINISH.code
                || type == PushCode.CLOUD_SUBSCRIBE_CANCEL.code || type == PushCode.CLOUD_SUBSCRIBE_RENEWAL.code || type == PushCode.CLOUD_SUBSCRIBE_EXPIRED.code
                || type == PushCode.PUSH_SHARE_MSG_TO_SHARER.code || type == PushCode.PUSH_SHARE_STATUS_TO_OWNER.code || type == PushCode.OWNER_REMOVE_SHARE_NOTIFY_SHARER.code || type == PushCode.SHARER_REMOVER_SHARE_NOTIFY_OWNER.code
                || type == PushCode.FREE_TRIAL_STORAGE.code) {
            if (MyAccountHelper.getInstance().isAppBeKilled(context)) {
                msgIntent.setAction(Intent.ACTION_MAIN);
                msgIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                msgIntent.setClass(NooieApplication.get(), SplashActivity.class);
                msgIntent.putExtra(ConstantValue.INTENT_KEY_DATA_TYPE, ConstantValue.NOOIE_MSG_TYPE_SYS);
            } else {
                msgIntent.setAction(ConstantValue.INTENT_FILTER_NOOIE_SYS);
            }
        } else if (type == PushCode.NORMAL.code) {
        } else if (type == PushCode.PUSH_ACTIVE.code) {
            if (MyAccountHelper.getInstance().isAppBeKilled(context)) {
                msgIntent.setAction(Intent.ACTION_MAIN);
                msgIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                msgIntent.setClass(NooieApplication.get(), SplashActivity.class);
                msgIntent.putExtra(ConstantValue.INTENT_KEY_DATA_TYPE, ConstantValue.NOOIE_MSG_TYPE_SYS);
            } else {
                msgIntent.setAction(ConstantValue.INTENT_FILTER_NOOIE_HOME);
            }
        } else if (type == PushCode.PUSH_DEVICE_BOUND.code) {
            if (MyAccountHelper.getInstance().isAppBeKilled(context)) {
                msgIntent.setAction(Intent.ACTION_MAIN);
                msgIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                msgIntent.setClass(NooieApplication.get(), SplashActivity.class);
                msgIntent.putExtra(ConstantValue.INTENT_KEY_DATA_TYPE, ConstantValue.NOOIE_MSG_TYPE_SYS);
            } else {
                msgIntent.setAction(ConstantValue.INTENT_FILTER_NOOIE_SYS);
            }
        }

        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setTicker(title);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setChannelId(NooieApplication.PUSH_CHANNEL_ID);
        builder.setContentIntent(PendingIntent.getActivity(NooieApplication.get(), type, msgIntent, PendingIntent.FLAG_CANCEL_CURRENT));
        //NooieLog.d("-->> NotificationManager showJPushNotification requestCode=" + type);

        android.app.NotificationManager nm = (android.app.NotificationManager) NooieApplication.get().getSystemService(Context.NOTIFICATION_SERVICE);
        Notification nf = builder.build();
        nf.defaults = Notification.DEFAULT_ALL;
        nm.notify(type, nf);
    }

    public void showJPushNotification(Context context, PushMessageBaseExtras extras) {

        if (context == null || extras == null) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(NooieApplication.get());
        builder.setAutoCancel(true);

        Intent msgIntent = new Intent();
        msgIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        String title = context.getString(R.string.application_name);
        String content = context.getString(R.string.unknown);
        int type = PushCode.NORMAL_SYSTEM_MSG.code;
        String deviceId = "";
        String deviceName = "";
        if (extras instanceof PushFeedbackMessageExtras) {
            PushFeedbackMessageExtras feedbackExtra = (PushFeedbackMessageExtras) extras;
            type = feedbackExtra.getCode();
            title = context.getString(R.string.home_person_feedback);
            content = !TextUtils.isEmpty(feedbackExtra.getMsg()) ? feedbackExtra.getMsg() : NooiePushMsgHelper.getFeedbackContent(context, feedbackExtra.getFeedback_status(), feedbackExtra.getContent());
            //msgIntent.setAction(ConstantValue.INTENT_FILTER_NOOIE_SYS);
        } else if (extras instanceof PushDetectMessageExtras) {
            PushDetectMessageExtras detectExtra = (PushDetectMessageExtras) extras;
            type = detectExtra.getCode();
            //title = context.getString(NooieJPushMsgHelper.getDetectPushTitleId(type));
            content = NooiePushMsgHelper.getDetectPushContent(context, type, detectExtra.getDevice(), detectExtra.getTime());
            deviceId = detectExtra.getDevice_id();
            deviceName = detectExtra.getDevice();
        } else if (extras instanceof PushShareMessageExtras) {
            PushShareMessageExtras shareExtra = (PushShareMessageExtras) extras;
            type = shareExtra.getCode();
            title = context.getString(R.string.share_dialog_title);
            content = showNooieShareMessage(context, shareExtra);
        } else if (extras instanceof PushUpdateMessageExtras) {
            PushUpdateMessageExtras updateExtra = (PushUpdateMessageExtras) extras;
            type = updateExtra.getCode();
            content = NooiePushMsgHelper.getDeviceUpdateContent(context, updateExtra.getCode(), updateExtra.getMsg());
        } else if (extras instanceof PushOrderMessageExtras) {
            PushOrderMessageExtras orderExtra = (PushOrderMessageExtras) extras;
            type = orderExtra.getCode();
            content = String.format(context.getString(R.string.system_message_pay_nooie_subscribe_successfully_with_trial), orderExtra.getDevice(), orderExtra.getOrder(), orderExtra.getPack());
            //msgIntent.setAction(ConstantValue.INTENT_FILTER_NOOIE_SYS);
        } else if (extras instanceof PushSubscribeMessageExtras) {
            PushSubscribeMessageExtras subscribeExtra = (PushSubscribeMessageExtras) extras;
            type = subscribeExtra.getCode();
            if (type == PushCode.CLOUD_SUBSCRIBE_CANCEL.code) {
                content = String.format(context.getString(R.string.system_message_pay_nooie_subscribe_cancel_with_trial), subscribeExtra.getDevice());
            } else if (type == PushCode.CLOUD_SUBSCRIBE_RENEWAL.code) {
                content = String.format(context.getString(R.string.system_message_pay_nooie_subscribe_subscribe_with_trial), subscribeExtra.getDevice());
            } else if (type == PushCode.CLOUD_SUBSCRIBE_EXPIRED.code) {
                content = String.format(context.getString(R.string.system_message_pay_nooie_subscribe_expired_with_trial), subscribeExtra.getDevice());
            }
            //msgIntent.setAction(ConstantValue.INTENT_FILTER_NOOIE_SYS);
        } else if (extras instanceof PushSysMessageExtras) {
            PushSysMessageExtras sysExtra = (PushSysMessageExtras) extras;
            type = sysExtra.getCode();
            title = context.getString(R.string.system_message);
            content = sysExtra.getMsg();
            //msgIntent.setAction(ConstantValue.INTENT_FILTER_NOOIE_SYS);
        } else if (extras instanceof PushOtherLoginMessageExtras) {
            PushOtherLoginMessageExtras otherLoginExtra = (PushOtherLoginMessageExtras) extras;
            type = otherLoginExtra.getCode();
            content = context.getString(R.string.kick_out_info);
        } else if (extras instanceof PushNormalMessageExtras) {
            PushNormalMessageExtras normalMessageExtra = (PushNormalMessageExtras) extras;
            type = normalMessageExtra.getCode();
            content = normalMessageExtra.getMsg();
        } else if (extras instanceof PushActiveMessageExtras) {
            PushActiveMessageExtras pushActiveExtra = (PushActiveMessageExtras) extras;
            type = pushActiveExtra.getCode();
            content = pushActiveExtra.getTitle();
        } else if (extras instanceof PushDeviceBoundMessageExtras) {
            PushDeviceBoundMessageExtras pushDeviceBoundExtra = (PushDeviceBoundMessageExtras) extras;
            type = pushDeviceBoundExtra.getCode();
            content = String.format(context.getString(R.string.push_msg_device_bound_by_other), pushDeviceBoundExtra.getAccount());
        }

        if (type == PushCode.MOTION_DETECT.code || type == PushCode.SOUND_DETECT.code || type == PushCode.PIR_DETECT.code || type == PushCode.DEVICE_SD_LEAK.code) {
            if (MyAccountHelper.getInstance().isAppBeKilled(context)) {
                msgIntent.setAction(Intent.ACTION_MAIN);
                msgIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                msgIntent.setClass(NooieApplication.get(), SplashActivity.class);
                msgIntent.putExtra(ConstantValue.INTENT_KEY_DATA_TYPE, ConstantValue.NOOIE_MSG_TYPE_DEVICE);
                msgIntent.putExtra(ConstantValue.INTENT_KEY_DEVICE_PLATFORM, ListDeviceItem.DEVICE_PLATFORM_NOOIE);
                msgIntent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
                msgIntent.putExtra(ConstantValue.INTENT_KEY_DEVICE_NAME, deviceName);
            } else if (!TextUtils.isEmpty(deviceId) && !TextUtils.isEmpty(deviceName)) {
                msgIntent.setAction(ConstantValue.INTENT_FILTER_NOOIE_DEVICE);
                msgIntent.putExtra(ConstantValue.INTENT_KEY_DEVICE_PLATFORM, ListDeviceItem.DEVICE_PLATFORM_NOOIE);
                msgIntent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
                msgIntent.putExtra(ConstantValue.INTENT_KEY_DEVICE_NAME, deviceName);
            }
        } else if (type == PushCode.NORMAL_SYSTEM_MSG.code || type == PushCode.BACKGROUND_DEAL_FEEDBACK_STATUS.code || type == PushCode.ORDER_FINISH.code
                || type == PushCode.CLOUD_SUBSCRIBE_CANCEL.code || type == PushCode.CLOUD_SUBSCRIBE_RENEWAL.code || type == PushCode.CLOUD_SUBSCRIBE_EXPIRED.code
                || type == PushCode.PUSH_SHARE_MSG_TO_SHARER.code || type == PushCode.PUSH_SHARE_STATUS_TO_OWNER.code || type == PushCode.OWNER_REMOVE_SHARE_NOTIFY_SHARER.code || type == PushCode.SHARER_REMOVER_SHARE_NOTIFY_OWNER.code) {
            if (MyAccountHelper.getInstance().isAppBeKilled(context)) {
                msgIntent.setAction(Intent.ACTION_MAIN);
                msgIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                msgIntent.setClass(NooieApplication.get(), SplashActivity.class);
                msgIntent.putExtra(ConstantValue.INTENT_KEY_DATA_TYPE, ConstantValue.NOOIE_MSG_TYPE_SYS);
            } else {
                msgIntent.setAction(ConstantValue.INTENT_FILTER_NOOIE_SYS);
            }
        } else if (type == PushCode.NORMAL.code) {
        } else if (type == PushCode.PUSH_ACTIVE.code) {
            if (MyAccountHelper.getInstance().isAppBeKilled(context)) {
                msgIntent.setAction(Intent.ACTION_MAIN);
                msgIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                msgIntent.setClass(NooieApplication.get(), SplashActivity.class);
                msgIntent.putExtra(ConstantValue.INTENT_KEY_DATA_TYPE, ConstantValue.NOOIE_MSG_TYPE_SYS);
            } else {
                msgIntent.setAction(ConstantValue.INTENT_FILTER_NOOIE_HOME);
            }
        } else if (type == PushCode.PUSH_DEVICE_BOUND.code) {
            if (MyAccountHelper.getInstance().isAppBeKilled(context)) {
                msgIntent.setAction(Intent.ACTION_MAIN);
                msgIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                msgIntent.setClass(NooieApplication.get(), SplashActivity.class);
                msgIntent.putExtra(ConstantValue.INTENT_KEY_DATA_TYPE, ConstantValue.NOOIE_MSG_TYPE_SYS);
            } else {
                msgIntent.setAction(ConstantValue.INTENT_FILTER_NOOIE_SYS);
            }
        }

        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setTicker(title);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setChannelId(NooieApplication.PUSH_CHANNEL_ID);
        builder.setContentIntent(PendingIntent.getActivity(NooieApplication.get(), type, msgIntent, PendingIntent.FLAG_CANCEL_CURRENT));
        //NooieLog.d("-->> NotificationManager showJPushNotification requestCode=" + type);

        android.app.NotificationManager nm = (android.app.NotificationManager) NooieApplication.get().getSystemService(Context.NOTIFICATION_SERVICE);
        Notification nf = builder.build();
        nf.defaults = Notification.DEFAULT_ALL;
        nm.notify(type, nf);
    }

    private String showNooieShareMessage(Context context, PushShareMessageExtras shareMessage) {
        String message = null;
        if (shareMessage != null) {
            String title = context.getString(R.string.share_dialog_title);
            String subMessage = null;

            if (shareMessage.getCode() == PushCode.PUSH_SHARE_MSG_TO_SHARER.code) {
                //%1$s shares %2$s to me
                message = String.format(context.getString(R.string.share_dialog_content), shareMessage.getAccount(), shareMessage.getDevice());
                subMessage = String.format(context.getString(R.string.share_device_sub_message), shareMessage.getDevice());
            } else if (shareMessage.getCode() == PushCode.PUSH_SHARE_STATUS_TO_OWNER.code && shareMessage.getStatus() == ApiConstant.SYS_MSG_SHARE_STATUS_ACCEPT) {
                //%1$s agreed with my device %2$s
                message = String.format(context.getString(R.string.share_dialog_content_agree), shareMessage.getAccount(), shareMessage.getDevice());
            } else if (shareMessage.getCode() == PushCode.PUSH_SHARE_STATUS_TO_OWNER.code && shareMessage.getStatus() == ApiConstant.SYS_MSG_SHARE_STATUS_REJECT) {
                //%1$s rejected my shared %2$s
                message = String.format(context.getString(R.string.share_dialog_content_reject), shareMessage.getAccount(), shareMessage.getDevice());
            } else if (shareMessage.getCode() == PushCode.OWNER_REMOVE_SHARE_NOTIFY_SHARER.code) {
                //%1$s canceled sharing %2$s to me
                message = String.format(context.getString(R.string.share_dialog_content_cancel), shareMessage.getAccount(), shareMessage.getDevice());
            } else if (shareMessage.getCode() == PushCode.SHARER_REMOVER_SHARE_NOTIFY_OWNER.code) {
                //%1$s deleted my shared %2$s
                message = String.format(context.getString(R.string.share_dialog_content_remove), shareMessage.getAccount(), shareMessage.getDevice());
            }
        }

        return message;
    }

    public void cancelAllNotifications() {
        try {
            android.app.NotificationManager nm = (android.app.NotificationManager) NooieApplication.get().getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancelAll();
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
    }

    public void cancelNotificationById(int id) {
        try {
            android.app.NotificationManager nm = (android.app.NotificationManager) NooieApplication.get().getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(id);
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
    }
}