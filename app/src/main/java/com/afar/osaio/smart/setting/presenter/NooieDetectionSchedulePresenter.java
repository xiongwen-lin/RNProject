package com.afar.osaio.smart.setting.presenter;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.DetectionSchedule;
import com.afar.osaio.smart.device.helper.DeviceSettingHelper;
import com.afar.osaio.smart.setting.view.INooieDetectionScheduleView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.json.GsonHelper;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.api.network.base.bean.entity.DeviceConfig;
import com.nooie.sdk.api.network.base.bean.entity.PIRPlanConfig;
import com.nooie.sdk.api.network.device.DeviceService;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.device.DeviceCmdService;
import com.nooie.sdk.device.bean.AlertPlanItem;
import com.nooie.sdk.device.bean.hub.PirPlan;
import com.nooie.sdk.device.listener.OnMotionDetectPlanListener;
import com.nooie.sdk.device.listener.OnSoundDetectPlanListener;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.listener.OnGetPirPlanListener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class NooieDetectionSchedulePresenter implements INooieDetectionSchedulePresenter {

    private static final int PIR_PLAN_DURATION_NUM = 48;
    private static final int PIR_PLAN_DURATION = 30;
    private static final int WEEK_DAY_LEN = 7;
    private static final int PIR_PLAN_MAX_SIZE = 3;

    private INooieDetectionScheduleView mDetectionScheduleView;
    public NooieDetectionSchedulePresenter(INooieDetectionScheduleView view) {
        mDetectionScheduleView = view;
    }

    @Override
    public void detachView() {
        if (mDetectionScheduleView != null) {
            mDetectionScheduleView = null;
        }
    }

    @Override
    public void getDetectionSchedules(final int detectType, String deviceId, boolean isHideLoading) {
        if (detectType == ConstantValue.NOOIE_DETECT_TYPE_MOTION) {
            DeviceCmdApi.getInstance().getMotionDetectPlan(deviceId, new OnMotionDetectPlanListener() {
                @Override
                public void onMotionDetectPlanInfo(int code, List<AlertPlanItem> plans) {
                    if (code == SDKConstant.CODE_CACHE) {
                        return;
                    }
                    if (code == Constant.OK && mDetectionScheduleView != null) {
                        List<DetectionSchedule> schedules = convertDetectionSchedule(plans);
                        mDetectionScheduleView.notifyGetDetectionSchedulesSuccess(detectType, schedules, isHideLoading);
                    } else if (mDetectionScheduleView != null) {
                        mDetectionScheduleView.notifyGetDetectionSchedulesFailed(detectType, isHideLoading);
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
                    if (code == Constant.OK && mDetectionScheduleView != null) {
                        List<DetectionSchedule> schedules = convertDetectionSchedule(plans);
                        mDetectionScheduleView.notifyGetDetectionSchedulesSuccess(detectType, schedules, isHideLoading);
                    } else if (mDetectionScheduleView != null) {
                        mDetectionScheduleView.notifyGetDetectionSchedulesFailed(detectType, isHideLoading);
                    }
                }
            });
        } else if (detectType == ConstantValue.DETECT_TYPE_PIR) {
            getPIRPlanConfig(deviceId, true);
        }
    }

    @Override
    public void setDetectionSchedules(final int detectType, final String deviceId, final DetectionSchedule detectionSchedule) {
        if (mDetectionScheduleView != null) {
            mDetectionScheduleView.displayLoading(true);
        }
        if (detectType == ConstantValue.NOOIE_DETECT_TYPE_MOTION) {
            DeviceCmdApi.getInstance().getMotionDetectPlan(deviceId, new OnMotionDetectPlanListener() {
                @Override
                public void onMotionDetectPlanInfo(int code, List<AlertPlanItem> plans) {
                    if (code == SDKConstant.CODE_CACHE) {
                        return;
                    }
                    if (code == Constant.OK) {
                        List<AlertPlanItem> planItems = convertAlertPlans(plans, detectionSchedule);
                        setDetectionAlartPlan(detectType, deviceId, planItems);
                    } else if (mDetectionScheduleView != null) {
                        mDetectionScheduleView.displayLoading(false);
                        mDetectionScheduleView.notifySetDetectionSchedulesResult(ConstantValue.ERROR);
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
                    if (code == Constant.OK) {
                        List<AlertPlanItem> planItems = convertAlertPlans(plans, detectionSchedule);
                        setDetectionAlartPlan(detectType, deviceId, planItems);
                    } else if (mDetectionScheduleView != null) {
                        mDetectionScheduleView.displayLoading(false);
                        mDetectionScheduleView.notifySetDetectionSchedulesResult(ConstantValue.ERROR);
                    }
                }
            });
        } else if (detectType == ConstantValue.DETECT_TYPE_PIR) {
            updatePirPlanConfig(deviceId, detectionSchedule);
        } else if (mDetectionScheduleView != null) {
            mDetectionScheduleView.displayLoading(true);
        }
    }

    public void setDetectionAlartPlan(final int detectType, final String deviceId, List<AlertPlanItem> planItems) {
        if (CollectionUtil.isEmpty(planItems) || planItems.size() != 21) {
            return;
        }
        if (detectType == ConstantValue.NOOIE_DETECT_TYPE_MOTION) {
            DeviceCmdApi.getInstance().setMotionDetectPlan(deviceId, planItems, new OnActionResultListener() {
                @Override
                public void onResult(int code) {
                    getDetectionSchedules(detectType, deviceId, false);
                    if (mDetectionScheduleView != null) {
                        mDetectionScheduleView.displayLoading(false);
                        mDetectionScheduleView.notifySetDetectionSchedulesResult(code == Constant.OK ? ConstantValue.SUCCESS : ConstantValue.ERROR);
                    }
                }
            });
        } else if (detectType == ConstantValue.NOOIE_DETECT_TYPE_SOUND) {
            DeviceCmdApi.getInstance().setSoundDetectPlan(deviceId, planItems, new OnActionResultListener() {
                @Override
                public void onResult(int code) {
                    getDetectionSchedules(detectType, deviceId, false);
                    if (mDetectionScheduleView != null) {
                        mDetectionScheduleView.displayLoading(false);
                        mDetectionScheduleView.notifySetDetectionSchedulesResult(code == Constant.OK ? ConstantValue.SUCCESS : ConstantValue.ERROR);
                    }
                }
            });
        }
    }

    public static List<DetectionSchedule> convertDetectionSchedule(List<AlertPlanItem> alertPlanItems) {
        List<DetectionSchedule> schedules = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(alertPlanItems) && alertPlanItems.size() == 21) {
            for (int i = 0; i < 3; i++) {
                DetectionSchedule detectionSchedule = null;
                List<Integer> weekDays = new ArrayList<>();
                boolean isEffective = true;
                int firstStart = 0;
                int firstEnd = 0;
                for (int j = 0; j < 7; j++) {
                    int index = j + 7 * i;
                    if (index < 0 || index >= alertPlanItems.size()) {
                        continue;
                    }
                    AlertPlanItem alertPlanItem = alertPlanItems.get(index);
                    if (alertPlanItem != null) {
                        if (j == 0) {
                            detectionSchedule = new DetectionSchedule(alertPlanItem.getStartTime(), alertPlanItem.getEndTime(), alertPlanItem.getStatus() == 1);
                            detectionSchedule.setId(i);
                            firstStart = detectionSchedule.getStart();
                            firstEnd = detectionSchedule.getEnd();
                        }

                        int weekDay = convertWeekDay(j);
                        if (detectionSchedule != null && alertPlanItem.getStatus() == 1 && weekDay != -1) {
                            weekDays.add(weekDay);
                        }

                        if (firstStart != detectionSchedule.getStart() || firstEnd != detectionSchedule.getEnd()) {
                            isEffective = false;
                        }
                    }
                }

                if (detectionSchedule != null) {
                    detectionSchedule.setWeekDays(weekDays);
                    detectionSchedule.setOpen(CollectionUtil.isNotEmpty(weekDays));
                    detectionSchedule.setEffective(isEffective);
                    schedules.add(detectionSchedule);
                }
            }
        }

        return schedules;
    }

    public static int convertWeekDay(int i) {
        int weekDay = -1;
        switch (i) {
            case 0:
                weekDay = Calendar.MONDAY;
                break;
            case 1:
                weekDay = Calendar.TUESDAY;
                break;
            case 2:
                weekDay = Calendar.WEDNESDAY;
                break;
            case 3:
                weekDay = Calendar.THURSDAY;
                break;
            case 4:
                weekDay = Calendar.FRIDAY;
                break;
            case 5:
                weekDay = Calendar.SATURDAY;
                break;
            case 6:
                weekDay = Calendar.SUNDAY;
                break;
        }

        return weekDay;
    }

    public List<AlertPlanItem> convertAlertPlans(List<AlertPlanItem> planItems, DetectionSchedule detectionSchedule) {
        List<AlertPlanItem> convertPlanItems = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(planItems) && planItems.size() == 21 && detectionSchedule != null) {
            int startIndex = detectionSchedule.getId() * 7;
            int endIndex = startIndex + 7;
            for (int i = 0; i < planItems.size(); i++) {
                if (i >= startIndex && i < endIndex) {
                    planItems.get(i).setStartTime(detectionSchedule.getStart());
                    planItems.get(i).setEndTime(detectionSchedule.getEnd());
                    if (detectionSchedule.isOpen()) {
                        int status = CollectionUtil.isNotEmpty(detectionSchedule.getWeekDays()) && detectionSchedule.getWeekDays().contains(convertWeekDay(i%7)) ? 1 : 0;
                        planItems.get(i).setStatus(status);
                    } else {
                        planItems.get(i).setStatus(0);
                    }
                    convertPlanItems.add(planItems.get(i));
                } else {
                    convertPlanItems.add(planItems.get(i));
                }
            }
        }
        return convertPlanItems;
    }

    //这部分于pir 侦测设置

    public void updatePirPlanConfig(String deviceId, DetectionSchedule schedule) {
        DeviceService.getService().getDeviceInfo(deviceId)
                .flatMap(new Func1<BaseResponse<BindDevice>, Observable<BaseResponse>>() {
                    @Override
                    public Observable<BaseResponse> call(BaseResponse<BindDevice> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            DeviceConfig deviceConfig = GsonHelper.convertJson(response.getData().getApp_timing_config(), DeviceConfig.class);
                            if (deviceConfig != null && CollectionUtil.isNotEmpty(deviceConfig.getPIRPlanList())) {
                                int planListSize = CollectionUtil.size(deviceConfig.getPIRPlanList());
                                int planId = -1;
                                for (int i = 0; i < planListSize; i++) {
                                    if (deviceConfig.getPIRPlanList().get(i) != null && deviceConfig.getPIRPlanList().get(i).getId() == schedule.getId()) {
                                        planId = schedule.getId();
                                        deviceConfig.getPIRPlanList().get(i).setStartTime(schedule.getStart());
                                        deviceConfig.getPIRPlanList().get(i).setEndTime(schedule.getEnd());
                                        deviceConfig.getPIRPlanList().get(i).setWeekArr(DeviceSettingHelper.convertPIRPlanWeekDayArr(schedule.isOpen(), DateTimeUtil.convertWeekDayKeys(schedule.getWeekDays())));
                                    }
                                }

                                if (planId == -1) {
                                    if (planListSize < PIR_PLAN_MAX_SIZE) {
                                        PIRPlanConfig pirPlanConfig = new PIRPlanConfig();
                                        pirPlanConfig.setId(deviceConfig.getPIRPlanList().size() + 1);
                                        pirPlanConfig.setStartTime(schedule.getStart());
                                        pirPlanConfig.setEndTime(schedule.getEnd());
                                        pirPlanConfig.setWeekArr(DeviceSettingHelper.convertPIRPlanWeekDayArr(schedule.isOpen(), DateTimeUtil.convertWeekDayKeys(schedule.getWeekDays())));
                                        deviceConfig.getPIRPlanList().add(pirPlanConfig);
                                    } else {
                                        deviceConfig.getPIRPlanList().get(planListSize - 1).setStartTime(schedule.getStart());
                                        deviceConfig.getPIRPlanList().get(planListSize - 1).setEndTime(schedule.getEnd());
                                        deviceConfig.getPIRPlanList().get(planListSize - 1).setWeekArr(DeviceSettingHelper.convertPIRPlanWeekDayArr(schedule.isOpen(), DateTimeUtil.convertWeekDayKeys(schedule.getWeekDays())));
                                    }
                                }
                            } else {
                                if (deviceConfig == null) {
                                    deviceConfig = new DeviceConfig();
                                }
                                List<PIRPlanConfig> pirPlanConfigs = new ArrayList<>();
                                PIRPlanConfig pirPlanConfig = new PIRPlanConfig();
                                pirPlanConfig.setId(1);
                                pirPlanConfig.setStartTime(schedule.getStart());
                                pirPlanConfig.setEndTime(schedule.getEnd());
                                pirPlanConfig.setWeekArr(DeviceSettingHelper.convertPIRPlanWeekDayArr(schedule.isOpen(), DateTimeUtil.convertWeekDayKeys(schedule.getWeekDays())));
                                pirPlanConfigs.add(pirPlanConfig);
                                deviceConfig.setPIRPlanList(pirPlanConfigs);
                            }

                            String configStr = GsonHelper.convertToJson(deviceConfig);
                            return DeviceService.getService().updateTimingConfig(deviceId, configStr);
                        }

                        return Observable.just(null);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mDetectionScheduleView != null) {
                            mDetectionScheduleView.notifySetDetectionSchedulesResult(ConstantValue.ERROR);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code) {
                            getPIRPlanConfig(deviceId,false);
                            setDeviceDetectionSchedule(deviceId, schedule);
                        } else if (mDetectionScheduleView != null) {
                            mDetectionScheduleView.notifySetDetectionSchedulesResult(ConstantValue.ERROR);
                        }
                    }
                });
    }

    public void setDeviceDetectionSchedule(String deviceId, DetectionSchedule schedule) {
        DeviceService.getService().getDeviceInfo(deviceId)
                .flatMap(new Func1<BaseResponse<BindDevice>, Observable<DeviceConfig>>() {
                    @Override
                    public Observable<DeviceConfig> call(BaseResponse<BindDevice> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            DeviceConfig deviceConfig = GsonHelper.convertJson(response.getData().getApp_timing_config(), DeviceConfig.class);
                            if (deviceConfig == null) {
                                deviceConfig = new DeviceConfig();
                            }
                            return Observable.just(deviceConfig);
                        }
                        return Observable.just(null);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<DeviceConfig>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mDetectionScheduleView != null) {
                            mDetectionScheduleView.notifySetDetectionSchedulesResult(ConstantValue.ERROR);
                        }
                    }

                    @Override
                    public void onNext(DeviceConfig deviceConfig) {
                        PirPlan pirPlan = DeviceSettingHelper.convertPirPlanFromConfig(deviceConfig);
                        setDevicePIRPlan(deviceId, pirPlan);
                    }
                });
    }

    public void setDevicePIRPlan(String deviceId, PirPlan plan) {
        DeviceCmdApi.getInstance().setPirPlan(deviceId, plan, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                if (mDetectionScheduleView != null) {
                    mDetectionScheduleView.notifySetDetectionSchedulesResult(code == Constant.OK ? ConstantValue.SUCCESS : ConstantValue.ERROR);
                }
            }
        });
    }

    @Override
    public void getPIRPlanConfig(String deviceId, boolean isHideLoading) {
        DeviceService.getService().getDeviceInfo(deviceId)
                .flatMap(new Func1<BaseResponse<BindDevice>, Observable<DeviceConfig>>() {
                    @Override
                    public Observable<DeviceConfig> call(BaseResponse<BindDevice> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            DeviceConfig deviceConfig = GsonHelper.convertJson(response.getData().getApp_timing_config(), DeviceConfig.class);
                            if (deviceConfig == null) {
                                deviceConfig = new DeviceConfig();
                            }
                            return Observable.just(deviceConfig);
                        }
                        return Observable.just(null);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<DeviceConfig>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mDetectionScheduleView != null) {
                        }
                    }

                    @Override
                    public void onNext(DeviceConfig deviceConfig) {
                        if (deviceConfig != null && mDetectionScheduleView != null) {
                            mDetectionScheduleView.notifyGetDetectionSchedulesSuccess(ConstantValue.DETECT_TYPE_PIR, DeviceSettingHelper.convertDetectionSchedulesFromConfig(deviceConfig), isHideLoading);
                        } else if (mDetectionScheduleView != null) {
                            mDetectionScheduleView.notifyGetDetectionSchedulesFailed(ConstantValue.DETECT_TYPE_PIR, isHideLoading);
                        }
                    }
                });
    }

    private PirPlan convertPirPlan(DetectionSchedule schedule) {
        PirPlan pirPlan = new PirPlan();
        pirPlan.plan = new boolean[WEEK_DAY_LEN * PIR_PLAN_DURATION_NUM];

        if (schedule == null || CollectionUtil.isEmpty(schedule.getWeekDays())) {
            return pirPlan;
        }

        boolean[] pirPlanTimes = convertPirPlanTimes(schedule.getStart(), schedule.getEnd());
        logPirPlanForBoolean("PirPlan valid day list convert schedule from " + schedule.getStart() + " to " + schedule.getEnd(), pirPlanTimes);
        for (int i = 0; i < WEEK_DAY_LEN; i++) {
            boolean isDaySelected = CollectionUtil.isNotEmpty(schedule.getWeekDays()) && schedule.getWeekDays().contains(DateTimeUtil.convertWeekDay(i));
            if (!isDaySelected) {
                continue;
            }

            for (int j = PIR_PLAN_DURATION_NUM * i; j < PIR_PLAN_DURATION_NUM * (i + 1); j++) {
                pirPlan.plan[j] = pirPlanTimes[j % PIR_PLAN_DURATION_NUM];
            }
        }

        return pirPlan;

    }

    private boolean[] convertPirPlanTimes(int start, int end) {
        int startIndex = start / PIR_PLAN_DURATION;
        int endIndex = end / PIR_PLAN_DURATION;
        boolean[] pirPlanTimes = new boolean[PIR_PLAN_DURATION_NUM];
        if (startIndex > endIndex) {
            return pirPlanTimes;
        }

        if (startIndex == endIndex) {
            pirPlanTimes[startIndex] = true;
            return pirPlanTimes;
        }

        for (int i = 0; i < pirPlanTimes.length; i++) {
            pirPlanTimes[i] = (i >= startIndex && i < endIndex);
        }

        return pirPlanTimes;
    }

    public void getDeviceDetectionSchedule(String deviceId) {
        getDevicePIRPLAN(deviceId);
    }

    public void getDevicePIRPLAN(String deviceId) {
        DeviceCmdService.getInstance(NooieApplication.mCtx).camGetPIRPlan(deviceId, new OnGetPirPlanListener() {
            @Override
            public void onGetPirPlan(int result, boolean[] list) {
                if (result == Constant.OK && mDetectionScheduleView != null) {
                    ArrayList<DetectionSchedule> schedules = new ArrayList<>();
                    logPirPlanForBoolean("PirPlan week list", list);
                    boolean[] pirPlanTimes = cutPirPlanTimes(list);
                    logPirPlanForBoolean("PirPlan valid day list", pirPlanTimes);
                    DetectionSchedule schedule = convertDetectionSchedule(pirPlanTimes);
                    List<Integer> weekDays = getPirPlanWeekDays(list);
                    if (schedule != null) {
                        schedule.setId(0);
                        schedule.setWeekDays(weekDays);
                        schedule.setOpen(CollectionUtil.isNotEmpty(weekDays));
                        schedules.add(schedule);
                    }
                    mDetectionScheduleView.notifyGetDetectionSchedulesSuccess(ConstantValue.DETECT_TYPE_PIR, schedules, true);
                } else if (mDetectionScheduleView != null) {
                    mDetectionScheduleView.notifyGetDetectionSchedulesFailed(ConstantValue.DETECT_TYPE_PIR, true);
                }
            }
        });
    }

    private boolean[] cutPirPlanTimes(boolean[] list) {
        boolean[] pirPlanTimes = new boolean[PIR_PLAN_DURATION_NUM];
        if (list == null || list.length < PIR_PLAN_DURATION_NUM * WEEK_DAY_LEN) {
            return pirPlanTimes;
        }

        for (int i = 0; i < WEEK_DAY_LEN; i++) {
            int startIndex = i * PIR_PLAN_DURATION_NUM;
            int endIndex = (i + 1) * PIR_PLAN_DURATION_NUM;
            boolean isPirPlanValid = false;
            for (int j = startIndex; j < endIndex; j++) {
                if (isPirPlanValid && !list[j]) {
                    break;
                }

                if (list[j]) {
                    isPirPlanValid = true;
                    pirPlanTimes[j % PIR_PLAN_DURATION_NUM] = list[j];
                }
            }

            if (isPirPlanValid) {
                break;
            }
        }

        return pirPlanTimes;
    }

    private List<Integer> getPirPlanWeekDays(boolean[] list) {
        List<Integer> weekDays =  new ArrayList<>();
        if (list == null || list.length < PIR_PLAN_DURATION_NUM * WEEK_DAY_LEN) {
            return weekDays;
        }

        for (int i = 0; i < WEEK_DAY_LEN; i++) {
            int startIndex = i * PIR_PLAN_DURATION_NUM;
            int endIndex = (i + 1) * PIR_PLAN_DURATION_NUM;
            for (int j =startIndex; j < endIndex; j++) {
                if (list[j]) {
                    weekDays.add(DateTimeUtil.convertWeekDay(i));
                    break;
                }
            }
        }

        return weekDays;
    }
    private DetectionSchedule convertDetectionSchedule(boolean[] pirPlanTimes) {
        DetectionSchedule schedule = new DetectionSchedule(0, (24 * 60) - 1, false);
        schedule.setEffective(true);
        if (pirPlanTimes == null || pirPlanTimes.length != PIR_PLAN_DURATION_NUM) {
            return schedule;
        }

        int startIndex = -1;
        int endIndex = -1;
        for (int i = 0; i < PIR_PLAN_DURATION_NUM; i++) {
            if (pirPlanTimes[i] && startIndex == -1) {
                startIndex = i;
                break;
            }
        }

        if (startIndex == -1) {
            return schedule;
        } else if (startIndex == PIR_PLAN_DURATION_NUM - 1) {
            int startTime = PIR_PLAN_DURATION * startIndex;
            int endTime = PIR_PLAN_DURATION * (startIndex + 1) - 1;
            schedule.setStart(startTime);
            schedule.setEnd(endTime);
            //schedule.setOpen(true);
            return schedule;
        }

        for (int i = startIndex + 1; i < PIR_PLAN_DURATION_NUM; i++) {
            if (!pirPlanTimes[i] && endIndex == -1) {
                endIndex = i - 1;
                break;
            } else {
                endIndex = i;
            }
        }

        int startTime = PIR_PLAN_DURATION * startIndex;
        int endTime = PIR_PLAN_DURATION * (endIndex + 1) - 1;

        schedule.setStart(startTime);
        schedule.setEnd(endTime);
        //schedule.setOpen(true);

        return schedule;
    }

    public void logPirPlanForBoolean(String logTag, boolean[] list) {
        StringBuilder listSb =  new StringBuilder();
        for (int i = 0; i < list.length; i++) {
            listSb.append(list[i] ? "1" : "0");
        }
        NooieLog.d("-->> NooieDetectionSchedulePresenter logPirPlanForBoolean tag " + logTag + " {r list=" + listSb.toString());
    }

    public List<PIRPlanConfig> convertPIRPlanConfig(List<DetectionSchedule> schedules) {
        List<PIRPlanConfig> pirPlanConfigs = new ArrayList<>();
        if (CollectionUtil.isEmpty(schedules)) {
            return pirPlanConfigs;
        }

        for (DetectionSchedule schedule : CollectionUtil.safeFor(schedules)) {
            if (schedule != null) {
                PIRPlanConfig pirPlanConfig = new PIRPlanConfig();
                pirPlanConfig.setStartTime(schedule.getStart());
                pirPlanConfig.setEndTime(schedule.getEnd());
                pirPlanConfig.setWeekArr(DeviceSettingHelper.convertPIRPlanWeekDayArr(schedule.isOpen(), DateTimeUtil.convertWeekDayKeys(schedule.getWeekDays())));
                pirPlanConfigs.add(pirPlanConfig);
            }
        }

        return pirPlanConfigs;
    }

    //这部分于pir 侦测设置
}
