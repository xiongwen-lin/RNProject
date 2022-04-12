package com.afar.osaio.smart.push;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.afar.osaio.account.helper.MyAccountHelper;
import com.nooie.common.utils.notify.NotificationUtil;
import com.nooie.common.utils.tool.RxUtil;
import com.nooie.sdk.api.network.account.AccountService;
import com.nooie.sdk.cache.DeviceConnectionCache;
import com.afar.osaio.smart.push.bean.PushActiveMessageExtras;
import com.afar.osaio.smart.push.bean.PushDetectMessageExtras;
import com.afar.osaio.smart.push.bean.PushDeviceBoundMessageExtras;
import com.afar.osaio.smart.push.bean.PushFeedbackMessageExtras;
import com.afar.osaio.smart.push.bean.PushFreeTrialStorageMessageExtras;
import com.afar.osaio.smart.push.bean.PushMessageBaseExtras;
import com.afar.osaio.smart.push.bean.PushSubscribeMessageExtras;
import com.afar.osaio.smart.push.bean.PushUpdateMessageExtras;
import com.afar.osaio.smart.push.helper.NooiePushMsgHelper;
import com.google.firebase.messaging.RemoteMessage;
import com.nooie.common.base.GlobalData;
import com.nooie.common.utils.json.GsonHelper;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.PushCode;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.cache.DeviceInfoCache;
import com.afar.osaio.notification.NotificationManager;
import com.afar.osaio.smart.cache.DeviceListCache;
import com.afar.osaio.smart.push.bean.PushOrderMessageExtras;
import com.afar.osaio.smart.push.bean.PushShareMessageExtras;
import com.afar.osaio.smart.push.bean.PushSysMessageExtras;
import com.afar.osaio.util.ConstantValue;
import com.umeng.message.entity.UMessage;

import java.util.concurrent.TimeUnit;

public class PushMsgManager {

    private static final int DELAY_TO_CHECK_LOGIN_TIME_LEN = 3000;

    private PushMsgManager() {
    }

    private static class PushMsgManagerHolder {
        public static final PushMsgManager INSTANCE = new PushMsgManager();
    }

    public static PushMsgManager getInstance() {
        return PushMsgManagerHolder.INSTANCE;
    }

    public void convertCustomMessage(String extras, RemoteMessage.Notification notification) {
        if (!checkCurrentAccount(extras) || notification == null) {
            return;
        }
        convertCustomMessage(extras, notification.getTitle(), notification.getBody());
    }

    public void convertCustomMessage(String extras, UMessage msg) {
        if (!checkCurrentAccount(extras) || msg == null) {
            return;
        }
        convertCustomMessage(extras, msg.title, msg.text);
    }

    public void convertCustomMessage(String extras, String title, String content) {
        if (!checkCurrentAccount(extras)) {
            return;
        }

        try {
            int code = NooiePushMsgHelper.getJPushMsgGode(extras);
            NooieLog.d("-->> PushMsgManager convertJPushCustomMessage code=" + NooiePushMsgHelper.getJPushMsgGode(extras));
            if (code == PushCode.NORMAL_SYSTEM_MSG.code) {
                PushSysMessageExtras sysMessageExtras = GsonHelper.convertJson(extras, PushSysMessageExtras.class);
                NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, sysMessageExtras, title, content);
            } else if (code == PushCode.MOTION_DETECT.code || code == PushCode.SOUND_DETECT.code || code == PushCode.PIR_DETECT.code || code == PushCode.DEVICE_SD_LEAK.code) {
                PushDetectMessageExtras detectExtras = GsonHelper.convertJson(extras, PushDetectMessageExtras.class);
                NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, detectExtras, title, content);
            } else if (code == PushCode.PUSH_SHARE_MSG_TO_SHARER.code || code == PushCode.PUSH_SHARE_STATUS_TO_OWNER.code || code == PushCode.OWNER_REMOVE_SHARE_NOTIFY_SHARER.code || code == PushCode.SHARER_REMOVER_SHARE_NOTIFY_OWNER.code) {
                PushShareMessageExtras shareExtras = GsonHelper.convertJson(extras, PushShareMessageExtras.class);
                NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, shareExtras, title, content);
                if (code == PushCode.OWNER_REMOVE_SHARE_NOTIFY_SHARER.code) {
                    DeviceListCache.getInstance().removeCacheById(shareExtras.getUuid());
                    DeviceInfoCache.getInstance().removeCacheById(shareExtras.getUuid());
                    DeviceConnectionCache.getInstance().removeConnection(shareExtras.getUuid());
                    sendBroadcast(ConstantValue.BROADCAST_KEY_REMOVE_CAMERA, null);
                }
            } else if (code == PushCode.ORDER_FINISH.code) {
                PushOrderMessageExtras orderExtras = GsonHelper.convertJson(extras, PushOrderMessageExtras.class);
                NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, orderExtras, title, content);
            } else if (code == PushCode.BACKGROUND_DEAL_FEEDBACK_STATUS.code) {
                PushFeedbackMessageExtras feedbackExtras = GsonHelper.convertJson(extras, PushFeedbackMessageExtras.class);
                NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, feedbackExtras, title, content);
            } else if (code == PushCode.DEVICE_UPDATE_START.code || code == PushCode.DEVICE_UPDATE_SUCCESS.code || code == PushCode.DEVICE_UPDATE_FAILED.code) {
                PushUpdateMessageExtras updateExtras = GsonHelper.convertJson(extras, PushUpdateMessageExtras.class);
                NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, updateExtras, title, content);
            } else if (code == PushCode.CLOUD_SUBSCRIBE_CANCEL.code || code == PushCode.CLOUD_SUBSCRIBE_RENEWAL.code || code == PushCode.CLOUD_SUBSCRIBE_EXPIRED.code) {
                PushSubscribeMessageExtras subscribeExtras = GsonHelper.convertJson(extras, PushSubscribeMessageExtras.class);
                NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, subscribeExtras, title, content);
            } else if (code == PushCode.USER_OTHER_PLACE_UPDAET.code) {
                //JPushOtherLoginMessageExtras otherLoginExtras = GsonHelper.convertJson(extras, JPushOtherLoginMessageExtras.class);
                //NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, otherLoginExtras);
                checkLoginValid();
            } else if (code == PushCode.PUSH_ACTIVE.code) {
                PushActiveMessageExtras pushActiveExtras = GsonHelper.convertJson(extras, PushActiveMessageExtras.class);
                NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, pushActiveExtras, title, content);
            } else if (code == PushCode.PUSH_DEVICE_BOUND.code) {
                PushDeviceBoundMessageExtras pushDeviceBoundMessageExtras = GsonHelper.convertJson(extras, PushDeviceBoundMessageExtras.class);
                NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, pushDeviceBoundMessageExtras, title, content);
            } else if (code == PushCode.FREE_TRIAL_STORAGE.code) {
                PushFreeTrialStorageMessageExtras pushFreeTrialStorageMessageExtras = GsonHelper.convertJson(extras, PushFreeTrialStorageMessageExtras.class);
                NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, pushFreeTrialStorageMessageExtras, title, content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void convertCustomMessage(String extras) {
        if (!checkCurrentAccount(extras)) {
            return;
        }

        try {
            int code = NooiePushMsgHelper.getJPushMsgGode(extras);
            NooieLog.d("-->> PushMsgManager convertJPushCustomMessage code=" + NooiePushMsgHelper.getJPushMsgGode(extras));
            if (code == PushCode.NORMAL_SYSTEM_MSG.code) {
                PushSysMessageExtras sysMessageExtras = GsonHelper.convertJson(extras, PushSysMessageExtras.class);
                NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, sysMessageExtras);
            } else if (code == PushCode.MOTION_DETECT.code || code == PushCode.SOUND_DETECT.code || code == PushCode.PIR_DETECT.code || code == PushCode.DEVICE_SD_LEAK.code) {
                PushDetectMessageExtras detectExtras = GsonHelper.convertJson(extras, PushDetectMessageExtras.class);
                NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, detectExtras);
            } else if (code == PushCode.PUSH_SHARE_MSG_TO_SHARER.code || code == PushCode.PUSH_SHARE_STATUS_TO_OWNER.code || code == PushCode.OWNER_REMOVE_SHARE_NOTIFY_SHARER.code || code == PushCode.SHARER_REMOVER_SHARE_NOTIFY_OWNER.code) {
                PushShareMessageExtras shareExtras = GsonHelper.convertJson(extras, PushShareMessageExtras.class);
                NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, shareExtras);
                if (code == PushCode.OWNER_REMOVE_SHARE_NOTIFY_SHARER.code) {
                    DeviceListCache.getInstance().removeCacheById(shareExtras.getUuid());
                    DeviceInfoCache.getInstance().removeCacheById(shareExtras.getUuid());
                    DeviceConnectionCache.getInstance().removeConnection(shareExtras.getUuid());
                    sendBroadcast(ConstantValue.BROADCAST_KEY_REMOVE_CAMERA, null);
                }
            } else if (code == PushCode.ORDER_FINISH.code) {
                PushOrderMessageExtras orderExtras = GsonHelper.convertJson(extras, PushOrderMessageExtras.class);
                NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, orderExtras);
            } else if (code == PushCode.BACKGROUND_DEAL_FEEDBACK_STATUS.code) {
                PushFeedbackMessageExtras feedbackExtras = GsonHelper.convertJson(extras, PushFeedbackMessageExtras.class);
                NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, feedbackExtras);
            } else if (code == PushCode.DEVICE_UPDATE_START.code || code == PushCode.DEVICE_UPDATE_SUCCESS.code || code == PushCode.DEVICE_UPDATE_FAILED.code) {
                PushUpdateMessageExtras updateExtras = GsonHelper.convertJson(extras, PushUpdateMessageExtras.class);
                NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, updateExtras);
            } else if (code == PushCode.CLOUD_SUBSCRIBE_CANCEL.code || code == PushCode.CLOUD_SUBSCRIBE_RENEWAL.code || code == PushCode.CLOUD_SUBSCRIBE_EXPIRED.code) {
                PushSubscribeMessageExtras subscribeExtras = GsonHelper.convertJson(extras, PushSubscribeMessageExtras.class);
                NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, subscribeExtras);
            } else if (code == PushCode.USER_OTHER_PLACE_UPDAET.code) {
                //JPushOtherLoginMessageExtras otherLoginExtras = GsonHelper.convertJson(extras, JPushOtherLoginMessageExtras.class);
                //NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, otherLoginExtras);
            } else if (code == PushCode.PUSH_ACTIVE.code) {
                PushActiveMessageExtras pushActiveExtras = GsonHelper.convertJson(extras, PushActiveMessageExtras.class);
                NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, pushActiveExtras);
            } else if (code == PushCode.PUSH_DEVICE_BOUND.code) {
                PushDeviceBoundMessageExtras pushDeviceBoundMessageExtras = GsonHelper.convertJson(extras, PushDeviceBoundMessageExtras.class);
                NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, pushDeviceBoundMessageExtras);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkCurrentAccount(String extras) {
        PushMessageBaseExtras jExtras = GsonHelper.convertJson(extras, PushMessageBaseExtras.class);
        logPushExtra(GlobalData.getInstance().getAccount(), jExtras);
        return jExtras != null && !TextUtils.isEmpty(jExtras.getUser_account()) && jExtras.getUser_account().equalsIgnoreCase(GlobalData.getInstance().getAccount());
    }

    private void sendBroadcast(String action, Bundle extras) {
        Intent pushIntent = new Intent();
        pushIntent.setAction(action);
        if (extras != null) {
            pushIntent.putExtras(extras);
        }
        NotificationUtil.sendBroadcast(NooieApplication.mCtx, pushIntent);
    }

    private void logPushExtra(String user, PushMessageBaseExtras extras) {
        if (extras == null) {
            return;
        }
        NooieLog.d("-->> PushMsgManager logPushExtra code=" + extras.getCode() + " account=" + extras.getUser_account() + " user" + user);
    }

    private void checkLoginValid() {
        NooieLog.d("-->> debug PushMsgManager checkLoginValid: 1");
        if (!MyAccountHelper.getInstance().isLogin()) {
            return;
        }
        RxUtil.wrapperObservable("-->> debug PushMsgManager checkLoginValid: 3", AccountService.getService().getUserInfo().delay(DELAY_TO_CHECK_LOGIN_TIME_LEN, TimeUnit.MILLISECONDS), null);
        NooieLog.d("-->> debug PushMsgManager checkLoginValid: 2");
    }

}
