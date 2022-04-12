package com.afar.osaio.smart.push.firebase.helper;

import com.afar.osaio.smart.push.bean.PushBatteryMessageExtras;
import com.afar.osaio.smart.push.bean.PushDetectMessageExtras;
import com.afar.osaio.smart.push.bean.PushFeedbackMessageExtras;
import com.afar.osaio.smart.push.bean.PushFreeTrialStorageMessageExtras;
import com.afar.osaio.smart.push.bean.PushOrderMessageExtras;
import com.afar.osaio.smart.push.bean.PushOtherLoginMessageExtras;
import com.afar.osaio.smart.push.bean.PushShareMessageExtras;
import com.afar.osaio.smart.push.bean.PushSubscribeMessageExtras;
import com.afar.osaio.smart.push.bean.PushSysMessageExtras;
import com.afar.osaio.smart.push.bean.PushUpdateMessageExtras;
import com.afar.osaio.smart.push.bean.PushActiveMessageExtras;
import com.afar.osaio.smart.push.bean.PushDeviceBoundMessageExtras;
import com.nooie.common.utils.data.DataHelper;
import com.nooie.common.utils.json.GsonHelper;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.PushCode;

import java.util.Map;

/**
 * FirebaseHelper
 *
 * @author Administrator
 * @date 2019/6/15
 */
public class FirebaseHelper {

    public static String convertFcmToJPush(Map<String,String> data) {
        String extras = "";
        if (data == null || !data.containsKey("code")) {
            return extras;
        }
        for (Map.Entry<String, String> entry : data.entrySet()) {
            NooieLog.d("-->> FirebaseMessageService onMessageReceived key=" + entry.getKey() + " value=" + entry.getValue());
        }

        if (String.valueOf(PushCode.BACKGROUND_DEAL_FEEDBACK_STATUS.code).equals(data.get("code"))) {
            //message = data.get("code");
            PushFeedbackMessageExtras jPushFeedbackMessageExtras = new PushFeedbackMessageExtras();
            jPushFeedbackMessageExtras.setCode(DataHelper.toInt(data.get("code")));
            jPushFeedbackMessageExtras.setUser_account(data.get("user_account"));
            jPushFeedbackMessageExtras.setContent(data.get("content"));
            jPushFeedbackMessageExtras.setFeed_type_name(data.get("feed_type_name"));
            jPushFeedbackMessageExtras.setFeedback_status(DataHelper.toInt(data.get("feedback_status")));
            jPushFeedbackMessageExtras.setPro_model(data.get("pro_model"));
            jPushFeedbackMessageExtras.setMsg(data.get("msg"));
            extras = GsonHelper.convertToJson(jPushFeedbackMessageExtras);
        } else if (String.valueOf(PushCode.ORDER_FINISH.code).equals(data.get("code"))) {
            //message = data.get("code");
            PushOrderMessageExtras jExtras = new PushOrderMessageExtras();
            jExtras.setCode(DataHelper.toInt(data.get("code")));
            jExtras.setUser_account(data.get("user_account"));
            jExtras.setOrder(data.get("order"));
            jExtras.setPack(data.get("pack"));
            jExtras.setTime(DataHelper.toLong(data.get("time")));
            jExtras.setType(DataHelper.toInt(data.get("type")));
            jExtras.setDevice(data.get("device"));
            extras = GsonHelper.convertToJson(jExtras);
        } else if (String.valueOf(PushCode.ORDER_FINISH.code).equals(data.get("code"))) {
            //message = data.get("code");
            PushOrderMessageExtras jExtras = new PushOrderMessageExtras();
            jExtras.setCode(DataHelper.toInt(data.get("code")));
            jExtras.setUser_account(data.get("user_account"));
            jExtras.setOrder(data.get("order"));
            jExtras.setPack(data.get("pack"));
            jExtras.setTime(DataHelper.toLong(data.get("time")));
            jExtras.setType(DataHelper.toInt(data.get("type")));
            jExtras.setDevice(data.get("device"));
            extras = GsonHelper.convertToJson(jExtras);
        } else if (String.valueOf(PushCode.MOTION_DETECT.code).equals(data.get("code")) || String.valueOf(PushCode.SOUND_DETECT.code).equals(data.get("code")) || String.valueOf(PushCode.DEVICE_SD_LEAK.code).equals(data.get("code")) || String.valueOf(PushCode.PIR_DETECT.code).equals(data.get("code"))) {
            //message = data.get("code");
            PushDetectMessageExtras jExtras = new PushDetectMessageExtras();
            jExtras.setCode(DataHelper.toInt(data.get("code")));
            jExtras.setUser_account(data.get("user_account"));
            jExtras.setTime(DataHelper.toInt(data.get("time")));
            jExtras.setType(DataHelper.toInt(data.get("type")));
            jExtras.setDevice(data.get("device"));
            jExtras.setDevice_id(data.get("device_id"));
            extras = GsonHelper.convertToJson(jExtras);
        } else if (String.valueOf(PushCode.PUSH_SHARE_MSG_TO_SHARER.code).equals(data.get("code")) || String.valueOf(PushCode.PUSH_SHARE_STATUS_TO_OWNER.code).equals(data.get("code")) || String.valueOf(PushCode.OWNER_REMOVE_SHARE_NOTIFY_SHARER.code).equals(data.get("code")) || String.valueOf(PushCode.SHARER_REMOVER_SHARE_NOTIFY_OWNER.code).equals(data.get("code"))) {
            //message = data.get("code");
            PushShareMessageExtras jExtras = new PushShareMessageExtras();
            jExtras.setCode(DataHelper.toInt(data.get("code")));
            jExtras.setUser_account(data.get("user_account"));
            jExtras.setAccount(data.get("account"));
            jExtras.setDevice(data.get("device"));
            jExtras.setMsg_id(DataHelper.toInt(data.get("msg_id")));
            jExtras.setNickname(data.get("nickname"));
            jExtras.setStatus(DataHelper.toInt(data.get("status")));
            jExtras.setShare_id(DataHelper.toInt(data.get("share_id")));
            jExtras.setUuid(data.get("uuid"));
            extras = GsonHelper.convertToJson(jExtras);
        } else if (String.valueOf(PushCode.DEVICE_UPDATE_START.code).equals(data.get("code")) || String.valueOf(PushCode.DEVICE_UPDATE_SUCCESS.code).equals(data.get("code")) || String.valueOf(PushCode.DEVICE_UPDATE_FAILED.code).equals(data.get("code"))) {
            //message = data.get("code");
            PushUpdateMessageExtras jExtras = new PushUpdateMessageExtras();
            jExtras.setCode(DataHelper.toInt(data.get("code")));
            jExtras.setUser_account(data.get("user_account"));
            jExtras.setUuid(data.get("uuid"));
            jExtras.setMsg(data.get("msg"));
            extras = GsonHelper.convertToJson(jExtras);
        } else if (String.valueOf(PushCode.USER_OTHER_PLACE_UPDAET.code).equals(data.get("code"))) {
            //message = data.get("code");
            PushOtherLoginMessageExtras jExtras = new PushOtherLoginMessageExtras();
            jExtras.setCode(DataHelper.toInt(data.get("code")));
            jExtras.setUser_account(data.get("user_account"));
            jExtras.setAccount(data.get("account"));
            extras = GsonHelper.convertToJson(jExtras);
        } else if (String.valueOf(PushCode.CLOUD_SUBSCRIBE_CANCEL.code).equals(data.get("code")) || String.valueOf(PushCode.CLOUD_SUBSCRIBE_RENEWAL.code).equals(data.get("code")) || String.valueOf(PushCode.CLOUD_SUBSCRIBE_EXPIRED.code).equals(data.get("code"))) {
            //message = data.get("code");
            PushSubscribeMessageExtras jExtras = new PushSubscribeMessageExtras();
            jExtras.setCode(DataHelper.toInt(data.get("code")));
            jExtras.setUser_account(data.get("user_account"));
            jExtras.setDevice(data.get("device"));
            extras = GsonHelper.convertToJson(jExtras);
        } else if (String.valueOf(PushCode.NORMAL_SYSTEM_MSG.code).equalsIgnoreCase(data.get("code"))) {
            //message = data.get("code");
            PushSysMessageExtras jExtras = new PushSysMessageExtras();
            jExtras.setCode(DataHelper.toInt(data.get("code")));
            jExtras.setUser_account(data.get("user_account"));
            jExtras.setMsg(data.get("msg"));
            extras = GsonHelper.convertToJson(jExtras);
        } else if (String.valueOf(PushCode.PUSH_ACTIVE.code).equalsIgnoreCase(data.get("code"))) {
            PushActiveMessageExtras jExtras = new PushActiveMessageExtras();
            jExtras.setCode(DataHelper.toInt(data.get("code")));
            jExtras.setUser_account(data.get("user_account"));
            jExtras.setMsg(data.get("msg"));
            jExtras.setUrl(data.get("url"));
            jExtras.setTitle(data.get("title"));
            extras = GsonHelper.convertToJson(jExtras);
        } else if (String.valueOf(PushCode.PUSH_DEVICE_BOUND.code).equalsIgnoreCase(data.get("code"))) {
            PushDeviceBoundMessageExtras jExtras = new PushDeviceBoundMessageExtras();
            jExtras.setCode(DataHelper.toInt(data.get("code")));
            jExtras.setUser_account(data.get("user_account"));
            jExtras.setUuid(data.get("uuid"));
            jExtras.setPuuid(data.get("puuid"));
            jExtras.setAccount(data.get("account"));
            jExtras.setNickname(data.get("nickname"));
            jExtras.setRegion(data.get("region"));
            extras = GsonHelper.convertToJson(jExtras);
        } else if (String.valueOf(PushCode.PUSH_LOW_BATTERY.code).equalsIgnoreCase(data.get("code"))) {
            PushBatteryMessageExtras jExtras = new PushBatteryMessageExtras();
            jExtras.setCode(DataHelper.toInt(data.get("code")));
            jExtras.setUser_account(data.get("user_account"));
            jExtras.setUuid(data.get("uuid"));
            jExtras.setName(data.get("name"));
            jExtras.setBattery_level(DataHelper.toInt(data.get("battery_level")));
            extras = GsonHelper.convertToJson(jExtras);
        } else if (String.valueOf(PushCode.FREE_TRIAL_STORAGE.code).equalsIgnoreCase(data.get("code"))) {
            PushFreeTrialStorageMessageExtras jExtras = new PushFreeTrialStorageMessageExtras();
            jExtras.setCode(DataHelper.toInt(data.get("code")));
            jExtras.setUser_account(data.get("user_account"));
            jExtras.setDevice_name(data.get("device_name"));
            jExtras.setUuid(data.get("uuid"));
            jExtras.setUid(DataHelper.toInt(data.get("uid")));
            jExtras.setExpire_date(DataHelper.toLong(data.get("expire_date")));
            jExtras.setFile_time(DataHelper.toInt(data.get("file_time")));
            jExtras.setDuration(DataHelper.toInt(data.get("duration")));
            jExtras.setTime_unit(data.get("time_unit"));
            extras = GsonHelper.convertToJson(jExtras);
        }
        return extras;
    }
}
