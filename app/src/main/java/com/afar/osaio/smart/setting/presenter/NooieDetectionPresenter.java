package com.afar.osaio.smart.setting.presenter;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.AlarmLevel;
import com.afar.osaio.bean.DetectionSchedule;
import com.afar.osaio.smart.setting.view.INooieDetectionView;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.device.bean.AlertPlanItem;
import com.nooie.sdk.device.bean.MTAreaInfo;
import com.nooie.sdk.device.bean.MotionDetectLevel;
import com.nooie.sdk.device.bean.SoundDetectLevel;
import com.nooie.sdk.device.listener.OnMotionDetectLevelListener;
import com.nooie.sdk.device.listener.OnMotionDetectPlanListener;
import com.nooie.sdk.device.listener.OnSoundDetectLevelListener;
import com.nooie.sdk.device.listener.OnSoundDetectPlanListener;
import com.nooie.sdk.device.listener.OnSwitchStateListener;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.listener.OnGetMTAreaListener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;

import java.util.List;

import static com.afar.osaio.util.preference.GlobalPrefs.KEY_MOTION_DETECT_STATE;
import static com.afar.osaio.util.preference.GlobalPrefs.KEY_NOOIE_MOTION_DETECT_STATE;
import static com.afar.osaio.util.preference.GlobalPrefs.KEY_NOOIE_SOUND_DETECT_STATE;
import static com.afar.osaio.util.preference.GlobalPrefs.KEY_SOUND_DETECT_STATE;
import static com.afar.osaio.util.preference.GlobalPrefs.MOTION_DETECT_STATE_CLOSE;
import static com.afar.osaio.util.preference.GlobalPrefs.MOTION_DETECT_STATE_HIGH;
import static com.afar.osaio.util.preference.GlobalPrefs.MOTION_DETECT_STATE_IGNORE;
import static com.afar.osaio.util.preference.GlobalPrefs.MOTION_DETECT_STATE_LOW;
import static com.afar.osaio.util.preference.GlobalPrefs.MOTION_DETECT_STATE_MEDIUM;

/**
 * Created by victor on 2018/7/5
 * Email is victor.qiao.0604@gmail.com
 */
public class NooieDetectionPresenter implements INooieDetectionPresenter {
    INooieDetectionView mDetectionView;

    public NooieDetectionPresenter(INooieDetectionView view) {
        this.mDetectionView = view;
    }

    @Override
    public void destroy() {
        if (mDetectionView != null) {
            mDetectionView = null;
        }
    }

    /**
     * 获得设备告警状态 Get the device alarm status
     */
    @Override
    public void getDetectionLevel(int detectType, String deviceId, boolean openCamera) {
        NooieLog.d("-->> NooieDetectionPresenter getDetectionLevel detectType=" + detectType + " deviceId=" + deviceId + " openCamera=" + openCamera);
        if (detectType == ConstantValue.NOOIE_DETECT_TYPE_MOTION) {
            getMotionDetectionLevelFromCache(deviceId, openCamera);
        } else {
            getSoundDetectionLevelFromCache(deviceId, openCamera);
        }
    }

    /**
     * 设置告警级别高低 Set the device motion alarm level
     *
     * @param deviceId
     * @param alarmLevel 告警级别高低 the device alarm level
     * @param openCamera
     */
    @Override
    public void setDetectionLevel(int detectType, String deviceId, final AlarmLevel alarmLevel, boolean openCamera) {
        NooieLog.d("-->> NooieDetectionPresenter setDetectionLevel detectType=" + detectType + " deviceId=" + deviceId + " openCamera=" + openCamera);
        if (detectType == ConstantValue.NOOIE_DETECT_TYPE_MOTION) {
            setMotionDetectionLevelToCache(deviceId, alarmLevel, openCamera);
        } else {
            setSoundDetectionLevelToCache(deviceId, alarmLevel, openCamera);
        }
    }

    private void getMotionDetectionLevelFromCache(String deviceId, boolean openCamera) {
        if (openCamera) {
            getMotionDetectionLevelFromNetWork(deviceId);
            return;
        }

        if (mDetectionView == null) {
            return;
        }

        String key = String.format("%s-%s", deviceId, KEY_MOTION_DETECT_STATE);
        GlobalPrefs globalPrefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
        int motionDetectState = globalPrefs.getInt(key, -1);
        if (motionDetectState == MOTION_DETECT_STATE_CLOSE) {
            mDetectionView.onGetSoundDetection(AlarmLevel.Close);
        } else if (motionDetectState == MOTION_DETECT_STATE_LOW) {
            mDetectionView.onGetSoundDetection(AlarmLevel.Low);
        } else if (motionDetectState == MOTION_DETECT_STATE_MEDIUM) {
            mDetectionView.onGetSoundDetection(AlarmLevel.Medium);
        } else if (motionDetectState == MOTION_DETECT_STATE_HIGH) {
            mDetectionView.onGetSoundDetection(AlarmLevel.High);
        } else if (motionDetectState == MOTION_DETECT_STATE_IGNORE) {
            getMotionDetectionLevelFromNetWork(deviceId);
        } else {
            getMotionDetectionLevelFromNetWork(deviceId);
        }
    }

    private void getMotionDetectionLevelFromNetWork(final String deviceId) {
        DeviceCmdApi.getInstance().getMotionDetectLevel(deviceId, new OnMotionDetectLevelListener() {
            @Override
            public void onMotionDetectInfo(int code, MotionDetectLevel level) {
                if (code == SDKConstant.CODE_CACHE) {
                    return;
                }
                if (mDetectionView != null && code == Constant.OK && level != null) {
                    AlarmLevel soundDetectLevel = AlarmLevel.getAlarmLevel(level.getIntValue());
                    AlarmLevel motionDetectLevel = AlarmLevel.getAlarmLevel(level.getIntValue());
                    mDetectionView.onGetSoundDetection(motionDetectLevel);
                    //nooie smart todo change sound level
                    saveDetectLevel(deviceId, motionDetectLevel, soundDetectLevel);
                } else if (mDetectionView != null) {
                    mDetectionView.onGetSoundDetection(AlarmLevel.Close);
                }
            }
        });
    }

    private void getSoundDetectionLevelFromCache(String deviceId, boolean openCamera) {
        if (openCamera) {
            getSoundDetectionLevelFromNetWork(deviceId);
            return;
        }

        if (mDetectionView == null) {
            return;
        }

        String key = String.format("%s-%s", deviceId, KEY_SOUND_DETECT_STATE);
        GlobalPrefs globalPrefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
        int motionDetectState = globalPrefs.getInt(key, -1);
        if (motionDetectState == MOTION_DETECT_STATE_CLOSE) {
            mDetectionView.onGetSoundDetection(AlarmLevel.Close);
        } else if (motionDetectState == MOTION_DETECT_STATE_LOW) {
            mDetectionView.onGetSoundDetection(AlarmLevel.Low);
        } else if (motionDetectState == MOTION_DETECT_STATE_MEDIUM) {
            mDetectionView.onGetSoundDetection(AlarmLevel.Medium);
        } else if (motionDetectState == MOTION_DETECT_STATE_HIGH) {
            mDetectionView.onGetSoundDetection(AlarmLevel.High);
        } else if (motionDetectState == MOTION_DETECT_STATE_IGNORE) {
            getSoundDetectionLevelFromNetWork(deviceId);
        } else {
            getSoundDetectionLevelFromNetWork(deviceId);
        }
    }

    private void getSoundDetectionLevelFromNetWork(final String deviceId) {
        DeviceCmdApi.getInstance().getSoundDetectLevel(deviceId, new OnSoundDetectLevelListener() {
            @Override
            public void OnSoundDetectLevelListener(int code, SoundDetectLevel level) {
                if (code == SDKConstant.CODE_CACHE) {
                    return;
                }
                if (mDetectionView != null && code == Constant.OK && level != null) {
                    AlarmLevel soundDetectLevel = AlarmLevel.getAlarmLevel(level.getIntValue());
                    AlarmLevel motionDetectLevel = AlarmLevel.getAlarmLevel(level.getIntValue());
                    mDetectionView.onGetSoundDetection(soundDetectLevel);
                    //nooie smart change motion detect
                    saveDetectLevel(deviceId, motionDetectLevel, soundDetectLevel);
                } else if (mDetectionView != null) {
                    mDetectionView.onGetSoundDetection(AlarmLevel.Close);
                }
            }
        });
    }

    private void setMotionDetectionLevelToCache(String deviceId, final AlarmLevel alarmLevel, boolean openCamera) {
        if (openCamera) {
            setMotionAlarm(deviceId, alarmLevel);
            return;
        }
        String key = String.format("%s-%s", deviceId, KEY_NOOIE_MOTION_DETECT_STATE);
        GlobalPrefs globalPrefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
        if (alarmLevel == AlarmLevel.Close) {
            globalPrefs.putInt(key, MOTION_DETECT_STATE_CLOSE);
        } else if (alarmLevel == AlarmLevel.Low) {
            globalPrefs.putInt(key, MOTION_DETECT_STATE_LOW);
        } else if (alarmLevel == AlarmLevel.Medium) {
            globalPrefs.putInt(key, MOTION_DETECT_STATE_MEDIUM);
        } else if (alarmLevel == AlarmLevel.High) {
            globalPrefs.putInt(key, MOTION_DETECT_STATE_HIGH);
        }

        if (mDetectionView != null) {
            mDetectionView.onGetSoundDetection(alarmLevel);
        }
    }

    private void setSoundDetectionLevelToCache(String deviceId, final AlarmLevel alarmLevel, boolean openCamera) {
        if (openCamera) {
            setSoundAlarm(deviceId, alarmLevel);
            return;
        }
        String key = String.format("%s-%s", deviceId, KEY_NOOIE_SOUND_DETECT_STATE);
        GlobalPrefs globalPrefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
        if (alarmLevel == AlarmLevel.Close) {
            globalPrefs.putInt(key, MOTION_DETECT_STATE_CLOSE);
        } else if (alarmLevel == AlarmLevel.Low) {
            globalPrefs.putInt(key, MOTION_DETECT_STATE_LOW);
        } else if (alarmLevel == AlarmLevel.Medium) {
            globalPrefs.putInt(key, MOTION_DETECT_STATE_MEDIUM);
        } else if (alarmLevel == AlarmLevel.High) {
            globalPrefs.putInt(key, MOTION_DETECT_STATE_HIGH);
        }

        if (mDetectionView != null) {
            mDetectionView.onGetSoundDetection(alarmLevel);
        }
    }

    private void setMotionAlarm(final String deviceId, final AlarmLevel alarmLevel) {
        MotionDetectLevel motionDetectLevel = MotionDetectLevel.getMotionDetectLevel(alarmLevel.getIntVal());
        DeviceCmdApi.getInstance().setMotionDetectLevel(deviceId, motionDetectLevel, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                if (mDetectionView != null && code == Constant.OK) {
                    mDetectionView.onGetSoundDetection(alarmLevel);
                    AlarmLevel motionAlarmLevel = alarmLevel;
                    AlarmLevel soundAlarLevel = alarmLevel;
                    saveDetectLevel(deviceId, motionAlarmLevel, soundAlarLevel);
                }

                if (mDetectionView != null) {
                    mDetectionView.notifySetDetectionResult(code == Constant.OK ? ConstantValue.SUCCESS : ConstantValue.ERROR);
                }
            }
        });
    }

    private void setSoundAlarm(final String deviceId, final AlarmLevel alarmLevel) {
        SoundDetectLevel soundDetectLevel = SoundDetectLevel.getSoundDetectLevel(alarmLevel.getIntVal());
        DeviceCmdApi.getInstance().setSoundDetectLevel(deviceId, soundDetectLevel, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                if (mDetectionView != null && code == Constant.OK) {
                    mDetectionView.onGetSoundDetection(alarmLevel);
                    AlarmLevel motionAlarmLevel = alarmLevel;
                    AlarmLevel soundAlarLevel = alarmLevel;
                    saveDetectLevel(deviceId, motionAlarmLevel, soundAlarLevel);
                }

                if (mDetectionView != null) {
                    mDetectionView.notifySetDetectionResult(code == Constant.OK ? ConstantValue.SUCCESS : ConstantValue.ERROR);
                }
            }
        });
    }

    /**
     * 保存移动侦测和声音侦测的Level
     *
     * @param deviceId
     * @param motionLevel
     * @param soundLevel
     */
    private void saveDetectLevel(String deviceId, AlarmLevel motionLevel, AlarmLevel soundLevel) {
        GlobalPrefs globalPrefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
        String motionKey = String.format("%s-%s", deviceId, KEY_NOOIE_MOTION_DETECT_STATE);
        String soundKey = String.format("%s-%s", deviceId, KEY_NOOIE_SOUND_DETECT_STATE);
        if (motionLevel == AlarmLevel.Close) {
            globalPrefs.putInt(motionKey, MOTION_DETECT_STATE_CLOSE);
        } else if (motionLevel == AlarmLevel.Low) {
            globalPrefs.putInt(motionKey, MOTION_DETECT_STATE_LOW);
        } else if (motionLevel == AlarmLevel.Medium) {
            globalPrefs.putInt(motionKey, MOTION_DETECT_STATE_MEDIUM);
        } else if (motionLevel == AlarmLevel.High) {
            globalPrefs.putInt(motionKey, MOTION_DETECT_STATE_HIGH);
        }

        if (soundLevel == AlarmLevel.Close) {
            globalPrefs.putInt(soundKey, MOTION_DETECT_STATE_CLOSE);
        } else if (soundLevel == AlarmLevel.Low) {
            globalPrefs.putInt(soundKey, MOTION_DETECT_STATE_LOW);
        } else if (soundLevel == AlarmLevel.Medium) {
            globalPrefs.putInt(soundKey, MOTION_DETECT_STATE_MEDIUM);
        } else if (soundLevel == AlarmLevel.High) {
            globalPrefs.putInt(soundKey, MOTION_DETECT_STATE_HIGH);
        }
    }

    @Override
    public void getSleepStatus(String deviceId) {
        NooieLog.d("-->> NooieDetectionPresenter setDetectionLevel deviceId=" + deviceId);
        DeviceCmdApi.getInstance().getSleep(deviceId, new OnSwitchStateListener() {
            @Override
            public void onStateInfo(int code, boolean on) {
                if (code == SDKConstant.CODE_CACHE) {
                    return;
                }
                if (mDetectionView != null && code == Constant.OK) {
                    mDetectionView.notifyGetSleepStateSuccess(!on);
                } else if (mDetectionView != null) {
                    //mDetectionView.notifyGetSleepStateFailed(NooieApplication.get().getString(R.string.unable_to_parse_response));
                }
            }
        });
    }

    @Override
    public void getDetectionSchedules(final int detectType, String deviceId) {
        if (detectType == ConstantValue.NOOIE_DETECT_TYPE_MOTION) {
            DeviceCmdApi.getInstance().getMotionDetectPlan(deviceId, new OnMotionDetectPlanListener() {
                @Override
                public void onMotionDetectPlanInfo(int code, List<AlertPlanItem> plans) {
                    if (code == SDKConstant.CODE_CACHE) {
                        return;
                    }
                    if (code == Constant.OK && mDetectionView != null) {
                        List<DetectionSchedule> schedules = NooieDetectionSchedulePresenter.convertDetectionSchedule(plans);
                        mDetectionView.notifyGetDetectionSchedulesSuccess(detectType, schedules);
                    }
                }
            });
        } else if (detectType == ConstantValue.NOOIE_DETECT_TYPE_SOUND) {
            DeviceCmdApi.getInstance().getSoundDetectPlan(deviceId, new OnSoundDetectPlanListener() {
                @Override
                public void onSoundDetectPlanInfo(int code, List<AlertPlanItem> plans) {
                    if (code == SDKConstant.CODE_CACHE) {
                        return;
                    }
                    if (code == Constant.OK && mDetectionView != null) {
                        List<DetectionSchedule> schedules = NooieDetectionSchedulePresenter.convertDetectionSchedule(plans);
                        mDetectionView.notifyGetDetectionSchedulesSuccess(detectType, schedules);
                    }
                }
            });
        }
    }

    @Override
    public void getMtAreaInfo(String deviceId) {
        DeviceCmdApi.getInstance().getMTAreaInfo(deviceId, new OnGetMTAreaListener() {
            @Override
            public void onGetAreaInfoResult(int result, MTAreaInfo info) {
                if (result == SDKConstant.CODE_CACHE) {
                    return;
                }
                if (mDetectionView != null) {
                    mDetectionView.onGetMtAreaInfo((result == Constant.OK ? ConstantValue.SUCCESS : ConstantValue.ERROR), info);
                }
            }
        });
    }
}
