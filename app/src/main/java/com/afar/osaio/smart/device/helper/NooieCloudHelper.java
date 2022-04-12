package com.afar.osaio.smart.device.helper;

import android.text.TextUtils;

import com.afar.osaio.util.ConstantValue;
import com.nooie.common.base.GlobalData;
import com.nooie.common.bean.CConstant;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.PackInfoResult;
import com.nooie.sdk.db.entity.DeviceConfigureEntity;

public class NooieCloudHelper {

    public static boolean isSubscribeCloud(int status) {
        return  status != ApiConstant.CLOUD_STATE_UNSUBSCRIBE;
    }

    public static boolean isOpenCloud(PackInfoResult info) {
        return info != null && (isSubscribeCloud(info.getStatus()) || info.getEnd_time() - System.currentTimeMillis()/1000 > 0);
    }

    public static boolean isOpenCloud(long endTime) {
        long t = System.currentTimeMillis() / 1000;
        NooieLog.d("end time:" + endTime + " cur:" + t);
        return endTime - t > 0;
    }

    public static boolean isOpenCloud(long endTime, float deviceTimeZone) {
        long t = (System.currentTimeMillis() + GlobalData.getInstance().getGapTime() * 1000L + (long)(deviceTimeZone * 3600 * 1000L)) / 1000;
        NooieLog.d("end time:" + endTime + " cur:" + t + " gapTime=:" + GlobalData.getInstance().getGapTime() + " timezone=" + deviceTimeZone);
        return endTime - t > 0;
    }

    public static boolean isOpenCloud(int existCloud) {
        return existCloud == ApiConstant.CLOUD_EXIST_CLOUD_Y;
    }

    public static boolean isEventCloud(int isEvent) {
        return isEvent == ApiConstant.CLOUD_IS_EVENT_Y;
    }

    public static boolean isFreeCloud(int isFree) {
        return isFree == ApiConstant.CLOUD_IS_FREE_Y;
    }

    public static boolean isDetectionAvailable(int time) {
        return time != -1;
    }

    public static boolean isCloudEventMsgInvalid(int storageDayNum, long msgTime) {
        if (storageDayNum < 1) {
            return false;
        }
        return msgTime < (DateTimeUtil.getUtcTodayStartTimeStamp() - (storageDayNum * DateTimeUtil.DAY_SECOND_COUNT * 1000L));
    }

    public static PackInfoResult createPackInfoResult() {
        PackInfoResult packInfoResult = new PackInfoResult();
        packInfoResult.setEnd_time(0);
        packInfoResult.setTotal_time(0);
        packInfoResult.setStatus(ApiConstant.CLOUD_STATE_UNSUBSCRIBE);
        return packInfoResult;
    }

    public static PackInfoResult createPackInfoResult(DeviceConfigureEntity configureEntity) {
        if (configureEntity != null) {
            PackInfoResult result = new PackInfoResult();
            result.setUuid(configureEntity.getDeviceId());
            result.setStart_time(configureEntity.getStartTime());
            result.setEnd_time(configureEntity.getEndTime());
            result.setFile_time(configureEntity.getFileTime());
            result.setTotal_time(configureEntity.getTotalTime());
            result.setIs_event(configureEntity.getIsEvent());
            result.setStatus(configureEntity.getStatus());
            result.setIs_free(configureEntity.getIsFree());
            return result;
        }
        return null;
    }

    public static String createEnterMark(String uid) {
        return new StringBuilder().append(uid).append(CConstant.UNDER_LINE).append(System.currentTimeMillis()).toString();
    }

    public static String createCloudPackUrl(String deviceId, String model, String enterMark, String origin) {
        if (TextUtils.isEmpty(GlobalData.getInstance().getWebUrl())) {
            return "";
        }
        StringBuilder urlSb = new StringBuilder();
        urlSb.append(GlobalData.getInstance().getWebUrl())
                .append("/pack/list")
                .append("?")
                .append(ConstantValue.CLOUD_PACK_PARAM_KEY_UUID)
                .append("=")
                .append(deviceId)
                .append("&")
                .append(ConstantValue.CLOUD_PACK_PARAM_KEY_MODEL)
                .append("=")
                .append(model)
                .append("&")
                .append(ConstantValue.CLOUD_PACK_PARAM_KEY_ENTER_MARK)
                .append("=")
                .append(enterMark)
                .append("&")
                .append(ConstantValue.CLOUD_PACK_PARAM_KEY_ORIGIN)
                .append("=")
                .append(origin);
        return urlSb.toString();
    }
}
