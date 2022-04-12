package com.afar.osaio.smart.player.activity;

import android.text.TextUtils;

import com.afar.osaio.base.NooieApplication;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.bean.DeviceComplexSetting;
import com.nooie.sdk.bean.IpcType;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.device.DeviceCmdService;
import com.nooie.sdk.device.bean.APPairStatus;
import com.nooie.sdk.device.bean.AlertPlanItem;
import com.nooie.sdk.device.bean.DevInfo;
import com.nooie.sdk.device.bean.FormatInfo;
import com.nooie.sdk.device.bean.ImgItem;
import com.nooie.sdk.device.bean.MTAreaInfo;
import com.nooie.sdk.device.bean.MotionDetectLevel;
import com.nooie.sdk.device.bean.NooieHotspot;
import com.nooie.sdk.device.bean.NooieMediaMode;
import com.nooie.sdk.device.bean.PirStateV2;
import com.nooie.sdk.device.bean.RecordFragment;
import com.nooie.sdk.device.bean.SensitivityLevel;
import com.nooie.sdk.device.bean.SoundDetectLevel;
import com.nooie.sdk.device.bean.SpeakerInfo;
import com.nooie.sdk.device.bean.hub.IRMode;
import com.nooie.sdk.device.bean.hub.PirState;
import com.nooie.sdk.device.bean.hub.ZoneRect;
import com.nooie.sdk.device.listener.OnIRModeListener;
import com.nooie.sdk.device.listener.OnMotionDetectLevelListener;
import com.nooie.sdk.device.listener.OnMotionDetectPlanListener;
import com.nooie.sdk.device.listener.OnSoundDetectLevelListener;
import com.nooie.sdk.device.listener.OnSoundDetectPlanListener;
import com.nooie.sdk.device.listener.OnSwitchStateListener;
import com.nooie.sdk.listener.OnAPPairStatusResultListener;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.listener.OnGetDevInfoListener;
import com.nooie.sdk.listener.OnGetFormatInfoListener;
import com.nooie.sdk.listener.OnGetHotspotListener;
import com.nooie.sdk.listener.OnGetImgListListener;
import com.nooie.sdk.listener.OnGetMTAreaListener;
import com.nooie.sdk.listener.OnGetMediaModeListener;
import com.nooie.sdk.listener.OnGetPirPlanListener;
import com.nooie.sdk.listener.OnGetPirStateListener;
import com.nooie.sdk.listener.OnGetPirStateV2Listener;
import com.nooie.sdk.listener.OnGetQuickReplyMsgListResultListener;
import com.nooie.sdk.listener.OnGetRecDatesListener;
import com.nooie.sdk.listener.OnGetSdcardRecordListener;
import com.nooie.sdk.listener.OnGetSpeakerInfoListener;
import com.nooie.sdk.listener.OnGetStateListener;
import com.nooie.sdk.listener.OnGetTimeListener;
import com.nooie.sdk.listener.OnGetZoneInfoListener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;
import com.nooie.sdk.processor.cmd.listener.OnGetDeviceSetting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeviceCmdApiTest
 *
 * @author Administrator
 * @date 2020/8/21
 */
public class DeviceCmdApiTest {

    public static final String KEY_GET_SD_RECORD_LIST = "KEY_GET_SD_RECORD_LIST";
    public static final String KEY_GET_SETTING = "KEY_GET_SETTING";
    public static final String KEY_GET_AUDIO_RECORD = "KEY_GET_AUDIO_RECORD";
    public static final String KEY_GET_LED = "KEY_GET_LED";
    public static final String KEY_GET_ROTATE = "KEY_GET_ROTATE";
    public static final String KEY_GET_LOOP_RECORD = "KEY_GET_LOOP_RECORD";
    public static final String KEY_GET_SLEEP = "KEY_GET_SLEEP";
    public static final String KEY_GET_ICR = "KEY_GET_ICR";
    public static final String KEY_GET_SYNC_TIME = "KEY_GET_SYNC_TIME";
    public static final String KEY_GET_MOTION_TRACK = "KEY_GET_MOTION_TRACK";
    public static final String KEY_GET_MOTION_DETECT_LEVEL = "KEY_GET_MOTION_DETECT_LEVEL";
    public static final String KEY_GET_SOUND_DETECT_LEVEL = "KEY_GET_SOUND_DETECT_LEVEL";
    public static final String KEY_GET_MOTION_DETECT_PLAN = "KEY_GET_MOTION_DETECT_PLAN";
    public static final String KEY_GET_SOUND_DETECT_PLAN = "KEY_GET_SOUND_DETECT_PLAN";
    public static final String KEY_GET_SPEAKER_INFO = "KEY_GET_SPEAKER_INFO";
    public static final String KEY_GET_FORMAT_INFO = "KEY_GET_FORMAT_INFO";
    public static final String KEY_GET_WIFI_SINGLE_INFO = "KEY_GET_WIFI_SINGLE_INFO";
    public static final String KEY_GET_SD_CARD_REC_DAY = "KEY_GET_SD_CARD_REC_DAY";
    public static final String KEY_GET_MT_AREA_INFO = "KEY_GET_MT_AREA_INFO";
    public static final String KEY_GET_RINGTONE_INDEX = "KEY_GET_RINGTONE_INDEX";
    public static final String KEY_GET_RINGTONE_VOLUME = "KEY_GET_RINGTONE_VOLUME";
    public static final String KEY_GET_PIR_STATE = "KEY_GET_PIR_STATE";
    public static final String KEY_GET_PIR_PLAN = "KEY_GET_PIR_PLAN";
    public static final String KEY_GET_PIR_AI = "KEY_GET_PIR_AI";
    public static final String KEY_GET_PRESS_AUDIO = "KEY_GET_PRESS_AUDIO";
    public static final String KEY_GET_QUICK_REPLY_LIST = "KEY_GET_QUICK_REPLY_LIST";
    public static final String KEY_GET_PIR_DISTANCE = "KEY_GET_PIR_DISTANCE";
    public static final String KEY_GET_TAMPER_STATE = "KEY_GET_TAMPER_STATE";
    public static final String KEY_GET_PD_ZONE = "KEY_GET_PD_ZONE";
    public static final String KEY_GET_POWER_FREQ = "KEY_GET_POWER_FREQ";
    public static final String KEY_GET_AP_PAIR_STATUS = "KEY_GET_AP_PAIR_STATUS";
    public static final String KEY_GET_AP_HTTP_PAIR_STATUS = "KEY_GET_AP_HTTP_PAIR_STATUS";

    public static final String KEY_SET_AUDIO_RECORD = "KEY_SET_AUDIO_RECORD";
    public static final String KEY_SET_LED = "KEY_SET_LED";
    public static final String KEY_SET_ROTATE = "KEY_SET_ROTATE";
    public static final String KEY_SET_LOOP_RECORD = "KEY_SET_LOOP_RECORD";
    public static final String KEY_SET_SLEEP = "KEY_SET_SLEEP";
    public static final String KEY_SET_ICR = "KEY_SET_ICR";
    public static final String KEY_SET_SYNC_TIME = "KEY_SET_SYNC_TIME";
    public static final String KEY_SET_MOTION_TRACK = "KEY_SET_MOTION_TRACK";
    public static final String KEY_SET_MOTION_DETECT_LEVEL = "KEY_SET_MOTION_DETECT_LEVEL";
    public static final String KEY_SET_SOUND_DETECT_LEVEL = "KEY_SET_SOUND_DETECT_LEVEL";
    public static final String KEY_SET_MOTION_DETECT_PLAN = "KEY_SET_MOTION_DETECT_PLAN";
    public static final String KEY_SET_SOUND_DETECT_PLAN = "KEY_SET_SOUND_DETECT_PLAN";
    public static final String KEY_SET_ALARM_SOUND = "KEY_SET_ALARM_SOUND";
    public static final String KEY_FORMAT_SD_CARD = "KEY_FORMAT_SD_CARD";
    public static final String KEY_PTZ_CONTROL = "KEY_PTZ_CONTROL";
    public static final String KEY_UPGRADE = "KEY_UPGRADE";
    public static final String KEY_COMBO_UPGRADE = "KEY_COMBO_UPGRADE";
    public static final String KEY_RESET_DEVICE = "KEY_RESET_DEVICE";
    public static final String KEY_REBOOT = "KEY_REBOOT";
    public static final String KEY_START_AUTH = "KEY_START_AUTH";
    public static final String KEY_STOP_AUTH = "KEY_STOP_AUTH";
    public static final String KEY_SET_RINGTONE_INDEX = "KEY_SET_RINGTONE_INDEX";
    public static final String KEY_SET_RINGTONE_VOLUME = "KEY_SET_RINGTONE_VOLUME";
    public static final String KEY_DOWNLOAD_RINGTONE = "KEY_DOWNLOAD_RINGTONE";
    public static final String KEY_SET_PIR_STATE = "KEY_SET_PIR_STATE";
    public static final String KEY_SET_PIR_PLAN = "KEY_SET_PIR_PLAN";
    public static final String KEY_SET_PIR_AI = "KEY_SET_PIR_AI";
    public static final String KEY_SET_PRESS_AUDIO = "KEY_SET_PRESS_AUDIO";
    public static final String KEY_SEND_QUICK_REPLY_MSG = "KEY_SEND_QUICK_REPLY_MSG";
    public static final String KEY_SET_TAMPER_STATE = "KEY_SET_TAMPER_STATE";
    public static final String KEY_SET_PD_ZONE = "KEY_SET_PD_ZONE";
    public static final String KEY_SET_POWER_FREQ = "KEY_SET_POWER_FREQ";
    public static final String KEY_START_AP_PAIR= "KEY_START_AP_PAIR";
    public static final String KEY_START_AP_HTTP_PAIR= "KEY_START_AP_HTTP_PAIR";
    public static final String KEY_REMOVE_DEVICE= "KEY_REMOVE_DEVICE";


    private Map<String, DeviceCmdTestResult> mTestResult;
    private int mTodayTimeStamp = 0;
//    private DeviceCmdApiTestListener mListener;

    private DeviceCmdApiTest() {
        init();
    }

    public static DeviceCmdApiTest getInstance() {
        return DeviceCmdApiTestHolder.INSTANCE;
    }

    private void init() {
        mTestResult = new HashMap<>();
        String[] cmdKeys = {KEY_GET_SD_RECORD_LIST, KEY_GET_SETTING, KEY_GET_AUDIO_RECORD, KEY_GET_LED, KEY_GET_ROTATE};
        for (String cmdKey : cmdKeys) {
            mTestResult.put(cmdKey, new DeviceCmdTestResult(cmdKey));
        }
        mTodayTimeStamp = (int)(DateTimeUtil.getUtcTodayStartTimeStamp() / 1000L);
    }

    private <T> void addTestResult(DeviceCmdTestResult<T> testResult) {
        if (mTestResult == null) {
            mTestResult = new HashMap<>();
        }
        if (testResult == null || TextUtils.isEmpty(testResult.getKey())) {
            return;
        }
        mTestResult.put(testResult.getKey(), testResult);
    }

    public void runGetCmdTest(String deviceId) {

        int testType = 1;//1 hc 320 ap_p2p
        if (testType == 1) {
            getDeviceSetting(deviceId, IpcType.HC320.getType());
            getSDRecordList(deviceId);
//            getDevInfo(deviceId);//fail
//            getFormatInfo(deviceId);
            getSDCardRecDay(deviceId);
//            getSyncTime(deviceId);
//            getIcr(deviceId);
//            getLoopRecord(deviceId);
//            getRotate(deviceId);
            getImgLists(deviceId);
            //camInfo
//            getMediaMode(deviceId);
            getHotspot(deviceId);
//            getWaterMark(deviceId);
//            getDeviceLed(deviceId);
            getInfraredSavePower(deviceId);
            getLight(deviceId);
            getPir(deviceId);
        }
//        getSDRecordList(deviceId);
//        getDeviceSetting(deviceId);
//        getAudioRecord(deviceId);
//        getDeviceLed(deviceId);
//        getDevInfo(deviceId);
//        getRotate(deviceId);
//        getLoopRecord(deviceId);
//        getSleep(deviceId);
//        getIcr(deviceId);
//        getSyncTime(deviceId);
//        getMotionTrack(deviceId);
//        getMotionDetectLevel(deviceId);
//        getSoundDetectLevel(deviceId);
//        getMotionDetectPlan(deviceId);
//        getSoundDetectPlan(deviceId);
//        getSpeakInfo(deviceId);
//        getFormatInfo(deviceId);
//        formatsd(deviceId);
//        getWifiSingleInfo(deviceId);
//        getSDCardRecDay(deviceId);
//        getMtAreaInfo(deviceId);
//        getRingtoneIndex(deviceId);
//        getRingtoneVolume(deviceId);
//        getPirState(deviceId);
//        getPirAi(deviceId);
//        getPressAudio(deviceId);
//        getQuickReplyList(deviceId);
//        getPirDistance(deviceId);
//        getTamperState(deviceId);
//        getPDZone(deviceId);
//        getPowerFreq(deviceId);
    }

    private void getLight(String deviceId) {
        DeviceCmdApi.getInstance().getLight(deviceId, new OnSwitchStateListener() {
            @Override
            public void onStateInfo(int code, boolean open) {
                NooieLog.d("-->> debug DeviceCmdApiTest getLight code=" + code);
            }
        });
    }

    private void getPir(String deviceId) {
        DeviceCmdApi.getInstance().getPir(deviceId, new OnGetPirStateV2Listener() {
            @Override
            public void onGetPirStateV2(int code, PirStateV2 pirStateV2) {
                NooieLog.d("-->> debug DeviceCmdApiTest getPir code=" + code);
            }
        });
    }

    private void getInfraredSavePower(String deviceId) {
        DeviceCmdApi.getInstance().getInfraredSavePower(deviceId, new OnSwitchStateListener() {
            @Override
            public void onStateInfo(int i, boolean b) {
                NooieLog.d("-->> debug DeviceCmdApiTest getInfraredSavePower code=" + i);
            }
        });
    }

    private void getWaterMark(String deviceId) {
        DeviceCmdApi.getInstance().getWaterMark(deviceId, new OnSwitchStateListener() {
            @Override
            public void onStateInfo(int i, boolean b) {
                NooieLog.d("-->> debug DeviceCmdApiTest onResult: getWaterMark");
            }
        });
    }

    private void getHotspot(String deviceId) {
        DeviceCmdApi.getInstance().getHotspot(deviceId, new OnGetHotspotListener() {
            @Override
            public void onResult(int i, NooieHotspot nooieHotspot) {
                NooieLog.d("-->> debug DeviceCmdApiTest onResult: getHotspot");
            }
        });
    }

    private void getMediaMode(String deviceId) {
        DeviceCmdApi.getInstance().getMediaMode(deviceId, new OnGetMediaModeListener() {
            @Override
            public void onResult(int i, NooieMediaMode nooieMediaMode) {
                NooieLog.d("-->> debug DeviceCmdApiTest onResult: getMediaMode");
            }
        });
    }

    private void getImgLists(String deviceId) {
        DeviceCmdApi.getInstance().getImgLists(deviceId, (int) (DateTimeUtil.getUtcTodayStartTimeStamp() / 1000L), 0, 40, new OnGetImgListListener() {
            @Override
            public void onGetImgs(int i, ImgItem[] imgItems) {
                NooieLog.d("-->> debug DeviceCmdApiTest onResult: getImgLists");
            }
        });
    }

    private DeviceCmdTestResult getDeviceCmdTestResult(String cmdKey) {
        if (mTestResult == null) {
            mTestResult = new HashMap<>();
            mTestResult.put(cmdKey, new DeviceCmdTestResult(cmdKey));
        }

        if (!mTestResult.containsKey(cmdKey) || mTestResult.get(cmdKey) == null) {
            mTestResult.put(cmdKey, new DeviceCmdTestResult(cmdKey));
        }
        return mTestResult.get(cmdKey);
    }

    private void getSDRecordList(String deviceId) {
        DeviceCmdApi.getInstance().getSDCardRecordList(deviceId, mTodayTimeStamp, new OnGetSdcardRecordListener() {
            @Override
            public void onGetSdcardRecordInfo(int code, RecordFragment[] records) {
                NooieLog.d("-->> debug DeviceCmdApiTest getSDRecordList code=" + code);
                dealCmdCallback(KEY_GET_SD_RECORD_LIST, code, records, records);
            }
        });
    }

    private void getDevInfo(String deviceId) {
        DeviceCmdApi.getInstance().getDevInfo(deviceId, new OnGetDevInfoListener() {
            @Override
            public void onDevInfo(int code, DevInfo devInfo) {
                NooieLog.d("-->> debug DeviceCmdApiTest getDevInfo code=" + code);
            }
        });
    }

    private void getDeviceSetting(String deviceId, String model) {
        DeviceCmdApi.getInstance().getDeviceSetting(deviceId, model, new OnGetDeviceSetting() {
            @Override
            public void onGetDeviceSetting(int code, DeviceComplexSetting setting) {
                NooieLog.d("-->> debug DeviceCmdApiTest  getDeviceSetting code=" + code);
                dealCmdCallback(KEY_GET_SETTING, code, setting, setting);
            }
        });
    }

    private void getAudioRecord(String deviceId) {
        DeviceCmdApi.getInstance().getRecordAudio(deviceId, new OnSwitchStateListener() {
            @Override
            public void onStateInfo(int code, boolean on) {
                NooieLog.d("-->> debug DeviceCmdApiTest getDeviceSetting code=" + code);
                dealCmdCallback(KEY_GET_AUDIO_RECORD, code, on, on);
            }
        });
    }

    private void getDeviceLed(String deviceId) {
        DeviceCmdApi.getInstance().getLed(deviceId, new OnSwitchStateListener() {
            @Override
            public void onStateInfo(int code, boolean on) {
                NooieLog.d("-->> debug DeviceCmdApiTest getDeviceLed code=" + code);
                dealCmdCallback(KEY_GET_LED, code, on, on);
            }
        });
    }

    private void getRotate(String deviceId) {
        DeviceCmdApi.getInstance().getRotate(deviceId, new OnSwitchStateListener() {
            @Override
            public void onStateInfo(int code, boolean on) {
                NooieLog.d("-->> debug DeviceCmdApiTest getRotate code=" + code);
                dealCmdCallback(KEY_GET_ROTATE, code, on, on);
            }
        });
    }

    private void getLoopRecord(String deviceId) {
        DeviceCmdApi.getInstance().getLoopRecord(deviceId, new OnSwitchStateListener() {
            @Override
            public void onStateInfo(int code, boolean on) {
                NooieLog.d("-->> debug DeviceCmdApiTest getLoopRecord code=" + code);
                dealCmdCallback(KEY_GET_LOOP_RECORD, code, on, on);
            }
        });
    }

    private void getSleep(String deviceId) {
        DeviceCmdApi.getInstance().getSleep(deviceId, new OnSwitchStateListener() {
            @Override
            public void onStateInfo(int code, boolean on) {
                NooieLog.d("-->> debug DeviceCmdApiTest getSleep code=" + code);
                dealCmdCallback(KEY_GET_SLEEP, code, on, on);
            }
        });
    }

    private void getIcr(String deviceId) {
        DeviceCmdApi.getInstance().getIcr(deviceId, IpcType.HC320.getType(), new OnIRModeListener() {
            @Override
            public void onIR(int code, IRMode mode) {
                NooieLog.d("-->> debug DeviceCmdApiTest getIcr code=" + code);
                dealCmdCallback(KEY_GET_ICR, code, mode, mode);
            }
        });
    }

    private void getSyncTime(String deviceId) {
        DeviceCmdApi.getInstance().getSyncTime(deviceId, new OnGetTimeListener() {
            @Override
            public void onGetTime(int result, int mode, float timeZone, int timeOffset) {
                NooieLog.d("-->> debug DeviceCmdApiTest getSyncTime code=" + result);
                dealCmdCallback(KEY_GET_SYNC_TIME, result, timeOffset, timeOffset);
            }
        });
    }

    private void getMotionTrack(String deviceId) {
        DeviceCmdApi.getInstance().getMotionTrack(deviceId, new OnSwitchStateListener() {
            @Override
            public void onStateInfo(int code, boolean on) {
                NooieLog.d("-->> debug DeviceCmdApiTest getMotionTrack code=" + code);
                dealCmdCallback(KEY_GET_MOTION_TRACK, code, on, on);
            }
        });
    }

    private void getMotionDetectLevel(String deviceId) {
        DeviceCmdApi.getInstance().getMotionDetectLevel(deviceId, new OnMotionDetectLevelListener() {
            @Override
            public void onMotionDetectInfo(int code, MotionDetectLevel level) {
                NooieLog.d("-->> debug DeviceCmdApiTest getMotionDetectLevel code=" + code);
                dealCmdCallback(KEY_GET_MOTION_DETECT_LEVEL, code, level, level);
            }
        });
    }

    private void getSoundDetectLevel(String deviceId) {
        DeviceCmdApi.getInstance().getSoundDetectLevel(deviceId, new OnSoundDetectLevelListener() {
            @Override
            public void OnSoundDetectLevelListener(int code, SoundDetectLevel level) {
                NooieLog.d("-->> debug DeviceCmdApiTest getMotionDetectLevel code=" + code);
                dealCmdCallback(KEY_GET_SOUND_DETECT_LEVEL, code, level, level);
            }
        });
    }

    private void getMotionDetectPlan(String deviceId) {
        DeviceCmdApi.getInstance().getMotionDetectPlan(deviceId, new OnMotionDetectPlanListener() {
            @Override
            public void onMotionDetectPlanInfo(int code, List<AlertPlanItem> plans) {
                NooieLog.d("-->> debug DeviceCmdApiTest getMotionDetectPlan code=" + code);
                dealCmdCallback(KEY_GET_MOTION_DETECT_PLAN, code, plans, plans);
            }
        });
    }

    private void getSoundDetectPlan(String deviceId) {
        DeviceCmdApi.getInstance().getSoundDetectPlan(deviceId, new OnSoundDetectPlanListener() {
            @Override
            public void onSoundDetectPlanInfo(int code, List<AlertPlanItem> plans) {
                NooieLog.d("-->> debug DeviceCmdApiTest getSoundDetectPlan code=" + code);
                dealCmdCallback(KEY_GET_SOUND_DETECT_PLAN, code, plans, plans);
            }
        });
    }

    private void getSpeakInfo(String deviceId) {
        DeviceCmdApi.getInstance().getSpeakerInfo(deviceId, new OnGetSpeakerInfoListener() {
            @Override
            public void onSpeakerInfo(int code, SpeakerInfo info) {
                NooieLog.d("-->> debug DeviceCmdApiTest getSpeakInfo code=" + code);
                dealCmdCallback(KEY_GET_SPEAKER_INFO, code, info, info);
            }
        });
    }

    private void getFormatInfo(String deviceId) {
        DeviceCmdApi.getInstance().getFormatInfo(deviceId, new OnGetFormatInfoListener() {
            @Override
            public void onGetFormatInfo(int code, FormatInfo info) {
                NooieLog.d("-->> debug DeviceCmdApiTest getFormatInfo code=" + code);
                dealCmdCallback(KEY_GET_FORMAT_INFO, code, info, info);
            }
        });
    }

    private void getWifiSingleInfo(String deviceId) {
//        DeviceCmdApi.getInstance().getWifiSingleInfo(deviceId, new OnGetWifiSingleInfoListener() {
//            @Override
//            public void onGetSingleInfo(int code, WifiSingleInfo info) {
//                dealCmdCallback(KEY_GET_WIFI_SINGLE_INFO, code, info, info);
//            }
//        });
    }

    private void getSDCardRecDay(String deviceId) {
        DeviceCmdApi.getInstance().getSDCardRecDay(deviceId, new OnGetRecDatesListener() {
            @Override
            public void onRecDates(int code, int[] list, int today) {
                NooieLog.d("-->> debug DeviceCmdApiTest getSDCardRecDay code=" + code);
                dealCmdCallback(KEY_GET_SD_CARD_REC_DAY, code, list, list);
            }
        });
    }

    private void getMtAreaInfo(String deviceId) {
        DeviceCmdApi.getInstance().getMTAreaInfo(deviceId, new OnGetMTAreaListener() {
            @Override
            public void onGetAreaInfoResult(int result, MTAreaInfo info) {
                NooieLog.d("-->> debug DeviceCmdApiTest getMtAreaInfo code=" + result);
                dealCmdCallback(KEY_GET_MT_AREA_INFO, result, info, info);
            }
        });
    }

    private void getRingtoneIndex(String deviceId) {
        DeviceCmdApi.getInstance().getRingtoneIndex(deviceId, new OnGetStateListener() {
            @Override
            public void onGetStateResult(int result, int state) {
                NooieLog.d("-->> debug DeviceCmdApiTest getMtAreaInfo code=" + result);
                dealCmdCallback(KEY_GET_RINGTONE_INDEX, result, state, state);
            }
        });
    }

    private void getRingtoneVolume(String deviceId) {
        DeviceCmdApi.getInstance().getRingtoneVolume(deviceId, new OnGetStateListener() {
            @Override
            public void onGetStateResult(int result, int state) {
                NooieLog.d("-->> debug DeviceCmdApiTest getRingtoneVolume code=" + result);
                dealCmdCallback(KEY_GET_RINGTONE_VOLUME, result, state, state);
            }
        });
    }

    private void getPirState(String deviceId) {
        DeviceCmdApi.getInstance().getPirState(deviceId, new OnGetPirStateListener() {
            @Override
            public void onGetPirState(int result, PirState state) {
                NooieLog.d("-->> debug DeviceCmdApiTest getPirState code=" + result);
                dealCmdCallback(KEY_GET_PIR_STATE, result, state, state);
            }
        });
    }

    private void getPirPlan(String deviceId) {
        DeviceCmdApi.getInstance().getPirPlan(deviceId, new OnGetPirPlanListener() {
            @Override
            public void onGetPirPlan(int result, boolean[] list) {
                NooieLog.d("-->> debug DeviceCmdApiTest getPirPlan code=" + result);
                dealCmdCallback(KEY_GET_PIR_PLAN, result, list, list);
            }
        });
    }

    private void getPirAi(String deviceId) {
        DeviceCmdApi.getInstance().getPirAi(deviceId, new OnSwitchStateListener() {
            @Override
            public void onStateInfo(int code, boolean on) {
                NooieLog.d("-->> debug DeviceCmdApiTest getPirAi code=" + code);
                dealCmdCallback(KEY_GET_PIR_AI, code, on, on);
            }
        });
    }

    private void getPressAudio(String deviceId) {
        DeviceCmdApi.getInstance().getPressAudio(deviceId, new OnSwitchStateListener() {
            @Override
            public void onStateInfo(int code, boolean on) {
                NooieLog.d("-->> debug DeviceCmdApiTest getPirAi code=" + code);
                dealCmdCallback(KEY_GET_PRESS_AUDIO, code, on, on);
            }
        });
    }

    private void getQuickReplyList(String deviceId) {
        DeviceCmdApi.getInstance().getQuickReplyList(deviceId, new OnGetQuickReplyMsgListResultListener() {
            @Override
            public void onResult(int code, String[] list) {
                NooieLog.d("-->> debug DeviceCmdApiTest getQuickReplyList code=" + code);
                dealCmdCallback(KEY_GET_QUICK_REPLY_LIST, code, list, list);
            }
        });
    }

    private void getPirDistance(String deviceId) {
        DeviceCmdApi.getInstance().getPirDistance(deviceId, new OnGetStateListener() {
            @Override
            public void onGetStateResult(int result, int state) {
                NooieLog.d("-->> debug DeviceCmdApiTest getPirDistance code=" + result);
                dealCmdCallback(KEY_GET_PIR_DISTANCE, result, state, state);
            }
        });
    }

    private void getTamperState(String deviceId) {
        DeviceCmdApi.getInstance().getTamperState(deviceId, new OnSwitchStateListener() {
            @Override
            public void onStateInfo(int code, boolean on) {
                NooieLog.d("-->> debug DeviceCmdApiTest getPirDistance code=" + code);
                dealCmdCallback(KEY_GET_TAMPER_STATE, code, on, on);
            }
        });
    }

    private void getPDZone(String deviceId) {
        DeviceCmdApi.getInstance().getPDZone(deviceId, new OnGetZoneInfoListener() {
            @Override
            public void onGetZoneResult(int result, ZoneRect[] rects) {
                NooieLog.d("-->> debug DeviceCmdApiTest getPDZone code=" + rects);
                dealCmdCallback(KEY_GET_PD_ZONE, result, rects, rects);
            }
        });
    }

    private void getPowerFreq(String deviceId) {
        DeviceCmdApi.getInstance().getPowerFreq(deviceId, new OnGetStateListener() {
            @Override
            public void onGetStateResult(int result, int state) {
                NooieLog.d("-->> debug DeviceCmdApiTest getPowerFreq code=" + result);
                dealCmdCallback(KEY_GET_POWER_FREQ, result, state, state);
            }
        });
    }

    private void getApPairStatus() {
        DeviceCmdApi.getInstance().getAPPairStatus(new OnAPPairStatusResultListener() {
            @Override
            public void onResult(int code, APPairStatus status, String uuid) {
                NooieLog.d("-->> debug DeviceCmdApiTest getApPairStatus code=" + code);
                dealCmdCallback(KEY_GET_AP_PAIR_STATUS, code, status, status);
            }
        });
    }

    private void getApHttpPairStatus() {
        DeviceCmdApi.getInstance().getAPHttpPairStatus(true, new OnAPPairStatusResultListener() {
            @Override
            public void onResult(int code, APPairStatus status, String uuid) {
                NooieLog.d("-->> debug DeviceCmdApiTest getApPairStatus code=" + code);
                dealCmdCallback(KEY_GET_AP_HTTP_PAIR_STATUS, code, status, status);
            }
        });
    }

    public void runSetCmdTest(String deviceId) {

        int testType = 1;//1 hc 320 ap_p2p
        if (testType == 1) {
//            formatsd(deviceId);
            setUTCTimeStamp(deviceId);
//            setSyncTime(deviceId);
//            setIcr(deviceId);
//            setLoopRecord(deviceId);
//            setRotate(deviceId);
//            setMediaMode(deviceId);
//            setHotspot(deviceId);
//            setWaterMark(deviceId);
//            setLed(deviceId);
            setInfraredSavePower(deviceId);
            setLight(deviceId);
            setPir(deviceId);
            //set utc time
            //resetFactory(deviceId);
            //unbindDevice(deviceId);
        }
    }

    private void setLight(String deviceId) {
        DeviceCmdApi.getInstance().setLight(deviceId, true, new OnActionResultListener() {
            @Override
            public void onResult(int i) {
                NooieLog.d("-->> debug DeviceCmdApiTest setLight code=" + i);
            }
        });
    }

    private void setPir(String deviceId) {
        PirStateV2 pirStateV2 = new PirStateV2();
        pirStateV2.enable = true;
        pirStateV2.sensitivityLevel = SensitivityLevel.SENSITIVITY_LEVEL_MIDDLE;
        pirStateV2.duration = 15;
        DeviceCmdApi.getInstance().setPir(deviceId, pirStateV2, new OnActionResultListener() {
            @Override
            public void onResult(int i) {
                NooieLog.d("-->> debug DeviceCmdApiTest setPir code=" + i);
            }
        });
    }

    private void setUTCTimeStamp(String deviceId) {
        long deviceTime = System.currentTimeMillis() / 1000L;
        DeviceCmdService.getInstance(NooieApplication.mCtx).setUTCTimeStamp(deviceId, deviceTime, new OnActionResultListener() {
            @Override
            public void onResult(int i) {
                NooieLog.d("-->> debug DeviceCmdApiTest setUTCTimeStamp code=" + i);
            }
        });
    }

    private void setInfraredSavePower(String deviceId) {
        DeviceCmdApi.getInstance().setInfraredSavePower(deviceId, true, new OnActionResultListener() {
            @Override
            public void onResult(int i) {
                NooieLog.d("-->> debug DeviceCmdApiTest setInfraredSavePower code=" + i);
            }
        });
    }

    private void resetFactory(String deviceId) {
        DeviceCmdApi.getInstance().resetDevice(deviceId, new OnActionResultListener() {
            @Override
            public void onResult(int i) {
                NooieLog.d("-->> debug DeviceCmdApiTest resetFactory code=" + i);
            }
        });
    }

    private void unbindDevice(String deviceId) {
        DeviceCmdService.getInstance(NooieApplication.mCtx).unbindDevice(deviceId, "", new OnActionResultListener() {
            @Override
            public void onResult(int i) {
                NooieLog.d("-->> debug DeviceCmdApiTest unbindDevice code=" + i);
            }
        });
    }

    private void formatsd(String deviceId) {
        DeviceCmdApi.getInstance().formatSDCard(deviceId, new OnActionResultListener() {
            @Override
            public void onResult(int i) {
                NooieLog.d("-->> debug DeviceCmdApiTest formatsd code=" + i);
            }
        });
    }

    private void setSyncTime(String deviceId) {
        DeviceCmdApi.getInstance().setSyncTime(deviceId, new OnActionResultListener() {
            @Override
            public void onResult(int i) {
                NooieLog.d("-->> debug DeviceCmdApiTest setSyncTime code=" + i);
            }
        });
    }

    private void setIcr(String deviceId) {
        DeviceCmdApi.getInstance().setIcr(deviceId, IpcType.HC320.getType(), IRMode.IR_MODE_AUTO, new OnActionResultListener() {
            @Override
            public void onResult(int i) {
                NooieLog.d("-->> debug DeviceCmdApiTest setIcr code=" + i);
            }
        });
    }

    private void setLoopRecord(String deviceId) {
        DeviceCmdApi.getInstance().setLoopRecord(deviceId, true, new OnActionResultListener() {
            @Override
            public void onResult(int i) {
                NooieLog.d("-->> debug DeviceCmdApiTest setLoopRecord code=" + i);
            }
        });
    }

    private void setRotate(String deviceId) {
        DeviceCmdApi.getInstance().setRotate(deviceId, true, new OnActionResultListener() {
            @Override
            public void onResult(int i) {
                NooieLog.d("-->> debug DeviceCmdApiTest setRotate code=" + i);
            }
        });
    }

    private void setWaterMark(String deviceId) {
        DeviceCmdApi.getInstance().setWaterMark(deviceId, true, new OnActionResultListener() {
            @Override
            public void onResult(int i) {
                NooieLog.d("-->> debug DeviceCmdApiTest setWaterMark code=" + i);
            }
        });
    }

    private void setMediaMode(String deviceId) {
        NooieMediaMode nooieMediaMode = new NooieMediaMode(1, 3, 20);
        DeviceCmdApi.getInstance().setMediaMode(deviceId, nooieMediaMode, new OnActionResultListener() {
            @Override
            public void onResult(int i) {
                NooieLog.d("-->> debug DeviceCmdApiTest setMediaMode code=" + i);
            }
        });
    }


    private void setHotspot(String deviceId) {
        NooieHotspot nooieHotspot = new NooieHotspot("12345678","HC320-001017");
        DeviceCmdApi.getInstance().setHotspot(deviceId, nooieHotspot, new OnActionResultListener() {
            @Override
            public void onResult(int i) {
                NooieLog.d("-->> debug DeviceCmdApiTest setHotspot code=" + i);
            }
        });
    }

    private void setLed(String deviceId) {
        DeviceCmdApi.getInstance().setLed(deviceId, true, new OnActionResultListener() {
            @Override
            public void onResult(int i) {
                NooieLog.d("-->> debug DeviceCmdApiTest setLed code=" + i);
            }
        });
    }

    private  <T> void dealCmdCallback(String cmdKey, int code, T result, T cacheResult) {
        if (getDeviceCmdTestResult(cmdKey) != null) {
            getDeviceCmdTestResult(cmdKey).setCode(code);
        }
        if (code == SDKConstant.CODE_CACHE) {
            //code为SDKConstant.CODE_CACHE时，代表命令获取到是该命令前一次获取成功时，返回结果的缓存
            if (getDeviceCmdTestResult(cmdKey) != null) {
                getDeviceCmdTestResult(cmdKey).setCacheResult(cacheResult);
            }
        } else if (code == Constant.OK) {
            //code为Constant.OK时，代表该次命令的请求成功
            if (getDeviceCmdTestResult(cmdKey) != null) {
                getDeviceCmdTestResult(cmdKey).setResult(result);
            }
        } else {
            //如果不是上述两种情况，代表该次命令发送失败
        }
        notifyDeviceCmdTestResultChange();
    }

    private void notifyDeviceCmdTestResultChange() {
//        if (mListener != null) {
//            mListener.onDeviceCmdTestNotify(mTestResult);
//        }
    }

//    public void addListener(DeviceCmdApiTestListener listener) {
//        this.mListener = listener;
//    }

    private static final class DeviceCmdApiTestHolder {
        private static final DeviceCmdApiTest INSTANCE = new DeviceCmdApiTest();
    }
}