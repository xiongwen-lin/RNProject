package com.afar.osaio.smart.push.helper;

import android.content.Context;
import android.text.TextUtils;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.push.bean.PushMessageBaseExtras;
import com.google.firebase.iid.FirebaseInstanceId;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.common.utils.json.GsonHelper;
import com.nooie.sdk.api.network.base.bean.PushCode;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.umeng.message.PushAgent;

/**
 * NooieJPushMsgHelper
 *
 * @author Administrator
 * @date 2019/5/14
 */
public class NooiePushMsgHelper {

    public static String NOOIE_PUSH_MSG_EXTRA= "NOOIE_PUSH_MSG_EXTRA";
    public static int JPUSH_SEQUENCE_SET_ALIAS = 0;
    public static int JPUSH_SEQUENCE_DELETE_ALIAS = 1;

    public static String getDetectPushContent(Context context, int type, String deviceName, int time) {
        String detectTime = DateTimeUtil.localToUtc(time * 1000L, DateTimeUtil.PATTERN_HM);
        StringBuilder sb = new StringBuilder();

        if (type == PushCode.MOTION_DETECT.code) {
            sb.append(String.format(context.getString(R.string.nooie_push_msg_detect_of_motion), deviceName));
        } else if (type == PushCode.SOUND_DETECT.code) {
            sb.append(String.format(context.getString(R.string.nooie_push_msg_detect_of_sound), deviceName));
        } else if (type == PushCode.PIR_DETECT.code) {
            sb.append(String.format(context.getString(R.string.nooie_push_msg_detect_of_pir), deviceName));
        } else if (type == PushCode.DEVICE_SD_LEAK.code) {
            sb.append(context.getString(R.string.message_sd_leak_info));
        } else {
            sb.append(context.getString(R.string.nooie_push_msg_normal_content));
        }

        return sb.toString();
    }

    public static String getFeedbackContent(Context context, int type, String detail) {
        if (type == ApiConstant.SYS_MSG_FEEDBACK_STATUS_NORMAL || type == ApiConstant.SYS_MSG_FEEDBACK_STATUS_WAIT) {
            return context.getString(R.string.system_message_feedback_wait);
        } else if (type == ApiConstant.SYS_MSG_FEEDBACK_STATUS_FINISH) {
            return  context.getString(R.string.system_message_feedback_finish);
        } else {
            return detail;
        }
    }

    public static String getDeviceUpdateContent(Context context, int type, String detail) {
        StringBuilder sb = new StringBuilder();
        if (type == PushCode.DEVICE_UPDATE_SUCCESS.code) {
            sb.append(context.getString(R.string.message_device_update_success));
        } else if (type == PushCode.DEVICE_UPDATE_FAILED.code) {
            sb.append(context.getString(R.string.message_device_update_fail));
        } else if (type == PushCode.DEVICE_UPDATE_START.code) {
            sb.append(context.getString(R.string.message_device_update_start));
        } else {
            sb.append(detail);
        }
        return sb.toString();
    }

    public static int getJPushMsgGode(String extras) {
        int code = PushCode.UNKNOWN.code;
        PushMessageBaseExtras jExtras = GsonHelper.convertJson(extras, PushMessageBaseExtras.class);
        if (jExtras != null) {
            code = jExtras.getCode();
        }
        return code;
    }

    public static String getPushToken() {
        String token = "-1";
        try {
            token = NooieApplication.get().getIsUseJPush() ? PushAgent.getInstance(NooieApplication.mCtx).getRegistrationId() : FirebaseInstanceId.getInstance().getToken();
            token = TextUtils.isEmpty(token) ? "-1" : token;
        } catch (Exception e) {
        }
        return token;
    }

    public static int getPushType() {
        return NooieApplication.get().getIsUseJPush() ? ApiConstant.PUSH_TYPE_UMENG : ApiConstant.PUSH_TYPE_FCM;
    }

    public static boolean isPushTokenValid(String token) {
        return !TextUtils.isEmpty(token) && !token.equalsIgnoreCase("-1");
    }

    public static boolean isPushTokenChange(String currentToken, String newToken) {
        return isPushTokenValid(currentToken) && isPushTokenValid(newToken) && !currentToken.equalsIgnoreCase(newToken);
    }
}
