package com.afar.osaio.protocol;

import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.afar.osaio.protocol.bean.Constant;
import com.afar.osaio.protocol.bean.DayNotificationsPlanPeriod;
import com.afar.osaio.protocol.bean.Week;
import com.afar.osaio.protocol.request.CAlarmSetCommonRequest;
import com.afar.osaio.protocol.request.CGetCommonRequest;
import com.afar.osaio.protocol.request.CSetCommonRequest;
import com.afar.osaio.protocol.request.CSetDayPlanRequest;
import com.afar.osaio.protocol.request.CSetRequest;
import com.afar.osaio.protocol.request.CSetSleepRequest;
import com.afar.osaio.protocol.request.CSetWeekPlanRequest;
import com.afar.osaio.protocol.request.CUpdateNooieRequest;
import com.afar.osaio.protocol.response.CCommonResponse;
import com.afar.osaio.protocol.response.CGetAlarmResponse;
import com.afar.osaio.protocol.response.CGetDayPlanResponse;
import com.afar.osaio.protocol.response.CGetModelResponse;
import com.afar.osaio.protocol.response.CGetWeekPlanResponse;
import com.afar.osaio.protocol.response.CRecentSDRecordsResponse;
import com.afar.osaio.protocol.response.CResponseImpl;
import com.afar.osaio.protocol.response.CUnknownResponse;

import java.util.List;

/**
 * Created by victor on 2018/7/31
 * Email is victor.qiao.0604@gmail.com
 */
public class ProtocolHelper {
    private static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * build get led status cmd
     *
     * @return
     */
    public static String buildGetLedCmd() {
        CGetCommonRequest request = new CGetCommonRequest(Constant.KEY_LED);
        boolean result = request.buildCmd();
        return bytesToHex(request.data());
    }

    /**
     * build set led status cmd
     *
     * @return
     */
    public static String buildSetLedCmd(boolean on) {
        CSetCommonRequest request = new CSetCommonRequest(Constant.KEY_LED, on);
        boolean result = request.buildCmd();
        return bytesToHex(request.data());
    }


    /**
     * build set motion tracking cmd
     *
     * @return
     */
    public static String buildSetMotionTrackingCmd(boolean on) {
        CSetCommonRequest request = new CSetCommonRequest(Constant.KEY_MOTION_TRACKING, on);
        boolean result = request.buildCmd();
        return bytesToHex(request.data());
    }

    /**
     * build set sleep status cmd
     *
     * @return
     */
    public static String buildSetSleepCmd(int value) {
        CSetSleepRequest request = new CSetSleepRequest(Constant.KEY_SLEEP_STATE, value);
        boolean result = request.buildCmd();
        return bytesToHex(request.data());
    }

    /**
     * build set day notification status cmd
     *
     * @return
     */
    public static String buildSetDayNotificationSwitchCmd(Week week, boolean on) {
        CSetCommonRequest request = new CSetCommonRequest(ProtocolHelper.notificationDaySwitchKeyByWeek(week), on);
        boolean result = request.buildCmd();
        return bytesToHex(request.data());
    }

    /**
     * build get day notification status cmd
     *
     * @return
     */
    public static String buildGetDayNotificationSwitchCmd(Week week) {
        CGetCommonRequest request = new CGetCommonRequest(ProtocolHelper.notificationDaySwitchKeyByWeek(week));
        boolean result = request.buildCmd();
        return bytesToHex(request.data());
    }

    /**
     * build set loop record status cmd
     *
     * @return
     */
    public static String buildSetLoopRecordCmd(boolean on) {
        CSetCommonRequest request = new CSetCommonRequest(Constant.KEY_LOOP_RECORD, on);
        boolean result = request.buildCmd();
        return bytesToHex(request.data());
    }

    /**
     * build get loop record status cmd
     *
     * @return
     */
    public static String buildGetLoopRecordCmd() {
        CGetCommonRequest request = new CGetCommonRequest(Constant.KEY_LOOP_RECORD);
        boolean result = request.buildCmd();
        return bytesToHex(request.data());
    }

    /**
     * build get record audio cmd
     *
     * @return
     */
    public static String buildGetRecordAudioCmd() {
        CGetCommonRequest request = new CGetCommonRequest(Constant.KEY_RECORD_WITH_AUDIO);
        boolean result = request.buildCmd();
        return bytesToHex(request.data());
    }

    /**
     * build set record audio cmd
     *
     * @return
     */
    public static String buildSetRecordAudioCmd(boolean on) {
        CSetCommonRequest request = new CSetCommonRequest(Constant.KEY_RECORD_WITH_AUDIO, on);
        boolean result = request.buildCmd();
        return bytesToHex(request.data());
    }

    /**
     * build get model cmd
     *
     * @return
     */
    public static String buildGetModelCmd() {
        CGetCommonRequest request = new CGetCommonRequest(Constant.KEY_MODEL);
        boolean result = request.buildCmd();
        return bytesToHex(request.data());
    }

    /**
     * build get one day plan
     *
     * @param week
     * @return
     */
    public static String buildGetDayNotificationsPlanCmd(Week week) {
        int key = 0;
        switch (week) {
            case Mon:
                key = Constant.KEY_NOTIFICATION_MON_PLAN;
                break;
            case Tues:
                key = Constant.KEY_NOTIFICATION_TUES_PLAN;
                break;
            case Wed:
                key = Constant.KEY_NOTIFICATION_WED_PLAN;
                break;
            case Thur:
                key = Constant.KEY_NOTIFICATION_THUR_PLAN;
                break;
            case Fri:
                key = Constant.KEY_NOTIFICATION_FRI_PLAN;
                break;
            case Sat:
                key = Constant.KEY_NOTIFICATION_SAT_PLAN;
                break;
            case Sun:
                key = Constant.KEY_NOTIFICATION_SUN_PLAN;
                break;
        }
        CGetCommonRequest request = new CGetCommonRequest(key);
        boolean result = request.buildCmd();
        return bytesToHex(request.data());
    }


    /**
     * build set one day plan
     *
     * @param week
     * @return
     */
    public static String buildSetDayNotificationsPlanCmd(Week week, @NonNull DayNotificationsPlanPeriod period) {
        int key = notificationDayPlanKeyByWeek(week);
        CSetDayPlanRequest request = new CSetDayPlanRequest(key, period);
        boolean result = request.buildCmd();
        return bytesToHex(request.data());
    }

    /**
     * build get week plan
     *
     * @return
     */
    public static String buildGetWeekNotificationsPlanCmd() {
        CGetCommonRequest request = new CGetCommonRequest(Constant.KEY_NOTIFICATION_WEEK_PLAN);
        boolean result = request.buildCmd();
        return bytesToHex(request.data());
    }

    /**
     * build set week plan
     *
     * @param plans
     * @return
     */
    public static String buildSetWeekNotificationsPlanCmd(@NonNull List<DayNotificationsPlanPeriod> plans) {
        CSetWeekPlanRequest request = new CSetWeekPlanRequest(plans);
        boolean result = request.buildCmd();
        return bytesToHex(request.data());
    }

    /**
     * build set week plan
     *
     * @param plans
     * @return
     */
    public static String buildSetWeekSoundPlanCmd(@NonNull List<DayNotificationsPlanPeriod> plans) {
        CSetWeekPlanRequest request = new CSetWeekPlanRequest(plans, Constant.KEY_SUD_DETECT_WEEK_PLAN);
        boolean result = request.buildCmd();
        return bytesToHex(request.data());
    }

    /**
     * build set one day plan
     *
     * @param week
     * @return
     */
    public static String buildSetDaySoundPlanCmd(Week week, @NonNull DayNotificationsPlanPeriod period) {
        int key = soundDayPlanKeyByWeek(week);
        CSetDayPlanRequest request = new CSetDayPlanRequest(key, period);
        boolean result = request.buildCmd();
        return bytesToHex(request.data());
    }

    /**
     * build set day notification status cmd
     *
     * @return
     */
    public static String buildSetDaySoundSwitchCmd(Week week, boolean on) {
        CSetCommonRequest request = new CSetCommonRequest(ProtocolHelper.soundDaySwitchKeyByWeek(week), on);
        boolean result = request.buildCmd();
        return bytesToHex(request.data());
    }

    /**
     * build set factory reset
     *
     * @return
     */
    public static String buildSetCmd(int key) {
        CSetRequest request = new CSetRequest(key);
        boolean result = request.buildCmd();
        return bytesToHex(request.data());
    }

    /**
     * build update dana to nooie
     * @param uid
     * @param timezone
     * @return
     */
    public static String buildUpdateNooieCmd(String uid, String timezone, String area) {
        CUpdateNooieRequest request = new CUpdateNooieRequest(uid, timezone, area);
        boolean result = request.buildCmd();
        if (!result) {
            return "";
        }
        return bytesToHex(request.data());
    }

    public static String buildSetAlarmAudioCmd(boolean on, int id, int time, int num) {
        CAlarmSetCommonRequest request = new CAlarmSetCommonRequest(on, id, time, num);
        boolean result = request.buildCmd();
        if (!result) {
            return "";
        }
        return bytesToHex(request.data());
    }

    /**
     * parse response
     *
     * @param data
     * @return
     */
    public static CResponseImpl parse(byte data[]) {
        int action = parseAction(data);
        int key = parseKey(data);

        CResponseImpl response = createResponse(action, key);

        if (response.parse(data)) {
            return response;
        } else {
            return null;
        }
    }

    /**
     * parse response
     *
     * @param hexString
     * @return
     */
    public static CResponseImpl parse(String hexString) {
        return parse(toByteArray(hexString));
    }

    public static CResponseImpl createResponse(int action, int key) {
        CResponseImpl response = null;
        if (action == Constant.CMD_GET) {
            switch (key) {
                case Constant.KEY_LED:
                case Constant.KEY_RECORD_WITH_AUDIO:
                case Constant.KEY_LOOP_RECORD:
                case Constant.KEY_NOTIFICATION_MON_SWITCH:
                case Constant.KEY_NOTIFICATION_TUES_SWITCH:
                case Constant.KEY_NOTIFICATION_WED_SWITCH:
                case Constant.KEY_NOTIFICATION_THUR_SWITCH:
                case Constant.KEY_NOTIFICATION_FRI_SWITCH:
                case Constant.KEY_NOTIFICATION_SAT_SWITCH:
                case Constant.KEY_NOTIFICATION_SUN_SWITCH:
                case Constant.KEY_SLEEP_STATE:
                case Constant.KEY_MOTION_TRACKING:
                case Constant.KEY_SUD_DETECT_PLAN_MON_SWITCH:
                case Constant.KEY_SUD_DETECT_PLAN_TUES_SWITCH:
                case Constant.KEY_SUD_DETECT_PLAN_WED_SWITCH:
                case Constant.KEY_SUD_DETECT_PLAN_THUR_SWITCH:
                case Constant.KEY_SUD_DETECT_PLAN_FRI_SWITCH:
                case Constant.KEY_SUD_DETECT_PLAN_SAT_SWITCH:
                case Constant.KEY_SUD_DETECT_PLAN_SUN_SWITCH:
                    response = new CCommonResponse();
                    break;
                case Constant.KEY_MODEL:
                    response = new CGetModelResponse();
                    break;
                case Constant.KEY_NOTIFICATION_WEEK_PLAN:
                case Constant.KEY_SUD_DETECT_WEEK_PLAN:
                    response = new CGetWeekPlanResponse();
                    break;
                case Constant.KEY_NOTIFICATION_MON_PLAN:
                case Constant.KEY_NOTIFICATION_TUES_PLAN:
                case Constant.KEY_NOTIFICATION_WED_PLAN:
                case Constant.KEY_NOTIFICATION_THUR_PLAN:
                case Constant.KEY_NOTIFICATION_FRI_PLAN:
                case Constant.KEY_NOTIFICATION_SAT_PLAN:
                case Constant.KEY_NOTIFICATION_SUN_PLAN:
                case Constant.KEY_SUD_DETECT_PLAN_MON:
                case Constant.KEY_SUD_DETECT_PLAN_TUES:
                case Constant.KEY_SUD_DETECT_PLAN_WED:
                case Constant.KEY_SUD_DETECT_PLAN_THUR:
                case Constant.KEY_SUD_DETECT_PLAN_FRI:
                case Constant.KEY_SUD_DETECT_PLAN_SAT:
                case Constant.KEY_SUD_DETECT_PLAN_SUN:
                    response = new CGetDayPlanResponse();
                    break;
                case Constant.KEY_RECENT_SDCARD_RECORDS:
                    response = new CRecentSDRecordsResponse();
                    break;
                case Constant.KEY_CAMERA_ALRAM_AUDIO:
                    response = new CGetAlarmResponse();
                    break;
                default:
                    response = new CUnknownResponse();
                    break;
            }
        } else if (action == Constant.CMD_SET) {
            response = new CCommonResponse();
        } else {
            response = new CUnknownResponse();
        }
        return response;
    }

    private static int parseAction(@NonNull byte data[]) {
        if (data.length < (Constant.LEN_BYTES_LEN + Constant.LEN_BYTES_ACTION + Constant.LEN_BYTES_KEY + 1)) {
            return -1;
        }
        return (data[1] & 0xFF);
    }

    private static int parseKey(@NonNull byte data[]) {
        if (data.length < (Constant.LEN_BYTES_LEN + Constant.LEN_BYTES_ACTION + Constant.LEN_BYTES_KEY + 1)) {
            return -1;
        }
        return (data[2] & 0xFF);
    }

    // BigEndian
    public static byte[] intToBytes(int value) {
        byte[] data = new byte[4];
        data[0] = (byte) ((value & 0xFF000000) >> 24);
        data[1] = (byte) ((value & 0x00FF0000) >> 16);
        data[2] = (byte) ((value & 0x0000FF00) >> 8);
        data[3] = (byte) ((value & 0x000000FF));
        return data;
    }

    // BigEndian
    public static int bytesToInt(byte[] data, int offset, int len) {
        int value = 0;
        if (len == 2) {
            value = ((data[offset] << 8) & 0xFF00)
                    | (data[offset + 1] & 0xFF)
                    & 0x0000FFFF;
        } else if (len == 4) {
            value = (data[offset + 3] & 0xFF)
                    | ((data[offset + 2] << 8) & 0xFF00)
                    | ((data[offset + 1] << 16) & 0xFF0000)
                    | ((data[offset] << 24) & 0xFF000000);
        }
        return value;
    }

    public static String formatProtocol(byte[] data) {
        StringBuilder builder = new StringBuilder();
        int len = data.length;
        if (len >= Constant.LEN_BYTES_LEN + Constant.LEN_BYTES_ACTION + Constant.LEN_BYTES_KEY) {
            builder.append("LEN:" + String.valueOf(data[0]));
            builder.append(" ACTION:" + formatAction(data[1]));
            builder.append(" KEY:" + formatKey(data[2]));

            if (data.length - 3 > 0) {
                byte payload[] = new byte[data.length - 3];
                System.arraycopy(data, 3, payload, 0, data.length - 3);
                builder.append(" PAYLOAD:" + bytesToHex(payload));
            }
        }

        return new String(builder);
    }

    public static String bytesToHex(byte[] bytes) {
        char[] buf = new char[bytes.length * 2];
        int index = 0;
        for (byte b : bytes) {
            buf[index++] = HEX_CHAR[b >>> 4 & 0xf];
            buf[index++] = HEX_CHAR[b & 0xf];
        }

        return new String(buf);
    }

    public static byte[] toByteArray(String hexString) {
        if (TextUtils.isEmpty(hexString))
            return null;

        hexString = hexString.toLowerCase();
        final byte[] byteArray = new byte[hexString.length() >> 1];
        int index = 0;
        for (int i = 0; i < hexString.length(); i++) {
            if (index > hexString.length() - 1)
                return byteArray;
            byte highDit = (byte) (Character.digit(hexString.charAt(index), 16) & 0xFF);
            byte lowDit = (byte) (Character.digit(hexString.charAt(index + 1), 16) & 0xFF);
            byteArray[i] = (byte) (highDit << 4 | lowDit);
            index += 2;
        }
        return byteArray;
    }

    public static String formatAction(int action) {
        switch (action) {
            case Constant.CMD_GET:
                return "GET";
            case Constant.CMD_SET:
                return "SET";
            default:
                return "UNKNOWN";
        }
    }

    public static String formatKey(int key) {
        switch (key) {
            case Constant.KEY_LED:
                return "KEY_LED";
            case Constant.KEY_MODEL:
                return "KEY_MODEL";
            case Constant.KEY_RECORD_WITH_AUDIO:
                return "KEY_RECORD_WITH_AUDIO";
            case Constant.KEY_NOTIFICATION_WEEK_PLAN:
                return "KEY_NOTIFICATION_WEEK_PLAN";
            case Constant.KEY_NOTIFICATION_MON_PLAN:
                return "KEY_NOTIFICATION_MON_PLAN";
            case Constant.KEY_NOTIFICATION_TUES_PLAN:
                return "KEY_NOTIFICATION_TUES_PLAN";
            case Constant.KEY_NOTIFICATION_WED_PLAN:
                return "KEY_NOTIFICATION_WED_PLAN";
            case Constant.KEY_NOTIFICATION_THUR_PLAN:
                return "KEY_NOTIFICATION_THUR_PLAN";
            case Constant.KEY_NOTIFICATION_FRI_PLAN:
                return "KEY_NOTIFICATION_FRI_PLAN";
            case Constant.KEY_NOTIFICATION_SAT_PLAN:
                return "KEY_NOTIFICATION_SAT_PLAN";
            case Constant.KEY_NOTIFICATION_SUN_PLAN:
                return "KEY_NOTIFICATION_SUN_PLAN";
            case Constant.KEY_NOTIFICATION_MON_SWITCH:
                return "KEY_NOTIFICATION_MON_SWITCH";
            case Constant.KEY_NOTIFICATION_TUES_SWITCH:
                return "KEY_NOTIFICATION_TUES_SWITCH";
            case Constant.KEY_NOTIFICATION_WED_SWITCH:
                return "KEY_NOTIFICATION_WED_SWITCH";
            case Constant.KEY_NOTIFICATION_THUR_SWITCH:
                return "KEY_NOTIFICATION_THUR_SWITCH";
            case Constant.KEY_NOTIFICATION_FRI_SWITCH:
                return "KEY_NOTIFICATION_FRI_SWITCH";
            case Constant.KEY_NOTIFICATION_SAT_SWITCH:
                return "KEY_NOTIFICATION_SAT_SWITCH";
            case Constant.KEY_NOTIFICATION_SUN_SWITCH:
                return "KEY_NOTIFICATION_SUN_SWITCH";
            default:
                return "UNKNOWN";
        }
    }

    public static String formatCode(int code) {
        switch (code) {
            case Constant.CODE_OK:
                return "OK";
            case Constant.CODE_ERROR:
                return "ERROR";
            default:
                return "UNKNOWN";
        }
    }

    public static String formatValue(int value) {
        switch (value) {
            case Constant.VALUE_ON:
                return "ON";
            case Constant.VALUE_OFF:
                return "OFF";
            default:
                return "UNKNOWN";
        }
    }

    public static int notificationDayPlanKeyByWeek(Week week) {
        switch (week) {
            case Mon:
                return Constant.KEY_NOTIFICATION_MON_PLAN;
            case Tues:
                return Constant.KEY_NOTIFICATION_TUES_PLAN;
            case Wed:
                return Constant.KEY_NOTIFICATION_WED_PLAN;
            case Thur:
                return Constant.KEY_NOTIFICATION_THUR_PLAN;
            case Fri:
                return Constant.KEY_NOTIFICATION_FRI_PLAN;
            case Sat:
                return Constant.KEY_NOTIFICATION_SAT_PLAN;
            case Sun:
                return Constant.KEY_NOTIFICATION_SUN_PLAN;
            default:
                return Constant.KEY_NOTIFICATION_MON_PLAN;
        }
    }

    public static Week weekByNotificationDayPlanKey(int key) {
        switch (key) {
            case Constant.KEY_NOTIFICATION_MON_PLAN:
            case Constant.KEY_SUD_DETECT_PLAN_MON:
                return Week.Mon;
            case Constant.KEY_NOTIFICATION_TUES_PLAN:
            case Constant.KEY_SUD_DETECT_PLAN_TUES:
                return Week.Tues;
            case Constant.KEY_NOTIFICATION_WED_PLAN:
            case Constant.KEY_SUD_DETECT_PLAN_WED:
                return Week.Wed;
            case Constant.KEY_NOTIFICATION_THUR_PLAN:
            case Constant.KEY_SUD_DETECT_PLAN_THUR:
                return Week.Thur;
            case Constant.KEY_NOTIFICATION_FRI_PLAN:
            case Constant.KEY_SUD_DETECT_PLAN_FRI:
                return Week.Fri;
            case Constant.KEY_NOTIFICATION_SAT_PLAN:
            case Constant.KEY_SUD_DETECT_PLAN_SAT:
                return Week.Sat;
            case Constant.KEY_NOTIFICATION_SUN_PLAN:
            case Constant.KEY_SUD_DETECT_PLAN_SUN:
                return Week.Sun;
            default:
                return Week.Mon;
        }
    }

    public static int notificationDaySwitchKeyByWeek(Week week) {
        switch (week) {
            case Mon:
                return Constant.KEY_NOTIFICATION_MON_SWITCH;
            case Tues:
                return Constant.KEY_NOTIFICATION_TUES_SWITCH;
            case Wed:
                return Constant.KEY_NOTIFICATION_WED_SWITCH;
            case Thur:
                return Constant.KEY_NOTIFICATION_THUR_SWITCH;
            case Fri:
                return Constant.KEY_NOTIFICATION_FRI_SWITCH;
            case Sat:
                return Constant.KEY_NOTIFICATION_SAT_SWITCH;
            case Sun:
                return Constant.KEY_NOTIFICATION_SUN_SWITCH;
            default:
                return Constant.KEY_NOTIFICATION_MON_SWITCH;
        }
    }

    public static Week weekByNotificationDaySwitchKey(int key) {
        switch (key) {
            case Constant.KEY_NOTIFICATION_MON_SWITCH:
                return Week.Mon;
            case Constant.KEY_NOTIFICATION_TUES_SWITCH:
                return Week.Tues;
            case Constant.KEY_NOTIFICATION_WED_SWITCH:
                return Week.Wed;
            case Constant.KEY_NOTIFICATION_THUR_SWITCH:
                return Week.Thur;
            case Constant.KEY_NOTIFICATION_FRI_SWITCH:
                return Week.Fri;
            case Constant.KEY_NOTIFICATION_SAT_SWITCH:
                return Week.Sat;
            case Constant.KEY_NOTIFICATION_SUN_SWITCH:
                return Week.Sun;
            default:
                return Week.Mon;
        }
    }

    public static int soundDayPlanKeyByWeek(Week week) {
        switch (week) {
            case Mon:
                return Constant.KEY_SUD_DETECT_PLAN_MON;
            case Tues:
                return Constant.KEY_SUD_DETECT_PLAN_TUES;
            case Wed:
                return Constant.KEY_SUD_DETECT_PLAN_WED;
            case Thur:
                return Constant.KEY_SUD_DETECT_PLAN_THUR;
            case Fri:
                return Constant.KEY_SUD_DETECT_PLAN_FRI;
            case Sat:
                return Constant.KEY_SUD_DETECT_PLAN_SAT;
            case Sun:
                return Constant.KEY_SUD_DETECT_PLAN_SUN;
            default:
                return Constant.KEY_SUD_DETECT_PLAN_MON;
        }
    }

    public static int soundDaySwitchKeyByWeek(Week week) {
        switch (week) {
            case Mon:
                return Constant.KEY_SUD_DETECT_PLAN_MON_SWITCH;
            case Tues:
                return Constant.KEY_SUD_DETECT_PLAN_TUES_SWITCH;
            case Wed:
                return Constant.KEY_SUD_DETECT_PLAN_WED_SWITCH;
            case Thur:
                return Constant.KEY_SUD_DETECT_PLAN_THUR_SWITCH;
            case Fri:
                return Constant.KEY_SUD_DETECT_PLAN_FRI_SWITCH;
            case Sat:
                return Constant.KEY_SUD_DETECT_PLAN_SAT_SWITCH;
            case Sun:
                return Constant.KEY_SUD_DETECT_PLAN_SUN_SWITCH;
            default:
                return Constant.KEY_SUD_DETECT_PLAN_MON_SWITCH;
        }
    }

}
