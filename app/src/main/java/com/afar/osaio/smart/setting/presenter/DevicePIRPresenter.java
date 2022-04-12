package com.afar.osaio.smart.setting.presenter;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.DetectionSchedule;
import com.afar.osaio.smart.device.helper.DeviceSettingHelper;
import com.afar.osaio.smart.setting.view.DevicePIRContract;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.json.GsonHelper;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.api.network.base.bean.entity.DeviceConfig;
import com.nooie.sdk.api.network.device.DeviceService;
import com.nooie.sdk.base.Constant;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.device.DeviceCmdService;
import com.nooie.sdk.device.bean.PirStateV2;
import com.nooie.sdk.device.bean.SensitivityLevel;
import com.nooie.sdk.device.bean.hub.PirPlan;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.listener.OnGetPirPlanListener;
import com.nooie.sdk.listener.OnGetPirStateV2Listener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class DevicePIRPresenter implements DevicePIRContract.Presenter {

    private static final int PIR_PLAN_DURATION_NUM = 48;
    private static final int PIR_PLAN_DURATION = 30;
    private static final int WEEK_DAY_LEN = 7;

    private DevicePIRContract.View mTaskView;

    public DevicePIRPresenter(DevicePIRContract.View view) {
        mTaskView = view;
        mTaskView.setPresenter(this);
    }

    @Override
    public void destroy() {
        if (mTaskView != null) {
            mTaskView.setPresenter(null);
            mTaskView = null;
        }
    }

    @Override
    public void setDevicePIRMode(String deviceId, PirStateV2 pirState, int operationType) {
        if (pirState != null) {
            NooieLog.d("{r-->> DevicePIRPresenter setDevicePIRMode deviceId=" + deviceId + " state enable=" + pirState.enable + " sensitivityLevel=" + (pirState.sensitivityLevel != null ? pirState.sensitivityLevel.getIntValue() : -1) + " operationType=" + operationType);
        }
        DeviceCmdApi.getInstance().getPir(deviceId, new OnGetPirStateV2Listener() {
            @Override
            public void onGetPirStateV2(int result, PirStateV2 state) {
                if (result == SDKConstant.CODE_CACHE) {
                    return;
                }
                if (result == Constant.OK && state != null) {
                    if (operationType == 1) {
                        state.enable = pirState != null ? pirState.enable : true;
                    } else if (operationType == 2) {
                        state.sensitivityLevel = pirState != null ?  pirState.sensitivityLevel : SensitivityLevel.SENSITIVITY_LEVEL_LOW;
                    }
                    NooieLog.d("{r-->> DevicePIRPresenter setDevicePIRMode onGetPirState + deviceId=" + deviceId + " state enable=" + state.enable + " duration=" + state.duration + " sensitivityLevel=" + state.sensitivityLevel.getIntValue() + " delay=" + state.delay + " pd=" + state.pd + " siren=" + state.siren);
                    DeviceCmdApi.getInstance().setPir(deviceId, state, new OnActionResultListener() {
                        @Override
                        public void onResult(int code) {
                            NooieLog.d("-->> DevicePIRPresenter setDevicePIRMode onSetResult code=" + code);
                            if (mTaskView !=  null) {
                                mTaskView.onSetPIRModeResult(code == Constant.OK ? ConstantValue.SUCCESS : ConstantValue.ERROR, state, operationType);
                            }
                        }
                    });
                } else if (mTaskView != null) {
                    mTaskView.onSetPIRModeResult(ConstantValue.ERROR, null, operationType);
                }
            }
        });

        /*
        if (pirState != null) {
            NooieLog.d("{r-->> DevicePIRPresenter setDevicePIRMode deviceId=" + deviceId + " state enable=" + pirState.enable + " sensitivityLevel=" + (pirState.sensitivityLevel != null ? pirState.sensitivityLevel.getIntValue() : -1) + " operationType=" + operationType);
        }
        DeviceCmdApi.getInstance().getPirState(deviceId, new OnGetPirStateListener() {
            @Override
            public void onGetPirState(int result, PirState state) {
                if (result == SDKConstant.CODE_CACHE) {
                    return;
                }
                if (result == Constant.OK && state != null) {
                    PirState tmpState = state;
                    if (operationType == 1) {
                        state.enable = pirState != null ? pirState.enable : true;
                    } else if (operationType == 2) {
                        state.sensitivityLevel = pirState != null ?  pirState.sensitivityLevel : SensitivityLevel.SENSITIVITY_LEVEL_LOW;
                    }
                    NooieLog.d("{r-->> DevicePIRPresenter setDevicePIRMode onGetPirState + deviceId=" + deviceId + " state enable=" + state.enable + " duration=" + state.duration + " sensitivityLevel=" + state.sensitivityLevel.getIntValue() + " delay=" + state.delay + " pd=" + state.pd + " siren=" + state.siren);
                    DeviceCmdApi.getInstance().setPirState(deviceId, state, new OnActionResultListener() {
                        @Override
                        public void onResult(int code) {
                            NooieLog.d("-->> DevicePIRPresenter setDevicePIRMode onSetResult code=" + code);
                            if (mTaskView !=  null) {
                                mTaskView.onSetPIRModeResult(code == Constant.OK ? ConstantValue.SUCCESS : ConstantValue.ERROR, state, operationType);
                            }
                        }
                    });
                } else if (mTaskView != null) {
                    mTaskView.onSetPIRModeResult(ConstantValue.ERROR, null, operationType);
                }
            }
        });

         */
    }

    @Override
    public void getDevicePIRMode(String deviceId) {
        DeviceCmdApi.getInstance().getPir(deviceId, new OnGetPirStateV2Listener() {
            @Override
            public void onGetPirStateV2(int result, PirStateV2 state) {
                if (result == SDKConstant.CODE_CACHE) {
                    return;
                }
                if (mTaskView != null) {
                    mTaskView.onGetPIRModeResult(result == Constant.OK ? ConstantValue.SUCCESS : ConstantValue.ERROR, state, 0);
                }
            }
        });
        /*
        DeviceCmdApi.getInstance().getPirState(deviceId, new OnGetPirStateListener() {
            @Override
            public void onGetPirState(int result, PirState state) {
                if (result == SDKConstant.CODE_CACHE) {
                    return;
                }
                if (mTaskView != null) {
                    mTaskView.onGetPIRModeReuslt(result == Constant.OK ? ConstantValue.SUCCESS : ConstantValue.ERROR, state, 0);
                }
            }
        });

         */
    }

    @Override
    public void getDeviceDetectionSchedule(String deviceId, boolean isSyncPirPlan) {
        getPIRPlanConfig(deviceId, isSyncPirPlan);
    }

    public void getPIRPlanConfig(String deviceId, boolean isSyncPirPlan) {
        DeviceService.getService().getDeviceInfo(deviceId)
                .flatMap(new Func1<BaseResponse<BindDevice>, Observable<BaseResponse<DeviceConfig>>>() {
                    @Override
                    public Observable<BaseResponse<DeviceConfig>> call(BaseResponse<BindDevice> response) {
                        BaseResponse<DeviceConfig> configResponse = new BaseResponse<>();
                        configResponse.setCode(response != null ? response.getCode() : StateCode.FAILED.code);
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            DeviceConfig deviceConfig = GsonHelper.convertJson(response.getData().getApp_timing_config(), DeviceConfig.class);
                            if (deviceConfig == null || CollectionUtil.isEmpty(deviceConfig.getPIRPlanList())) {
                                String configStr = GsonHelper.convertToJson(DeviceSettingHelper.createDeviceConfig());
                                return DeviceService.getService().updateTimingConfig(deviceId, configStr).flatMap(new Func1<BaseResponse, Observable<BaseResponse<DeviceConfig>>>() {
                                    @Override
                                    public Observable<BaseResponse<DeviceConfig>> call(BaseResponse response) {
                                        BaseResponse<DeviceConfig> configResponse = new BaseResponse<>();
                                        if (response != null && response.getCode() == StateCode.SUCCESS.code) {
                                            configResponse.setCode(StateCode.SUCCESS.code);
                                            configResponse.setData(DeviceSettingHelper.createDeviceConfig());
                                        } else {
                                            configResponse.setCode(StateCode.FAILED.code);
                                        }
                                        return Observable.just(configResponse);
                                    }
                                });
                            }
                            configResponse.setData(deviceConfig);
                        }
                        return Observable.just(configResponse);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<DeviceConfig>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onGetDeviceDetectionScheduleResult(ConstantValue.ERROR, null);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<DeviceConfig> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mTaskView != null) {
                            mTaskView.onGetDeviceDetectionScheduleResult(ConstantValue.SUCCESS, DeviceSettingHelper.convertDetectionSchedulesFromConfig(response.getData()));
                            if (!isSyncPirPlan || response.getData() == null || CollectionUtil.isEmpty(response.getData().getPIRPlanList())) {
                                return;
                            }
                            PirPlan pirPlan = DeviceSettingHelper.convertPirPlanFromConfig(response.getData());
                            if (pirPlan != null) {
                                syncPirPlan(deviceId, pirPlan.plan);
                            }
                        } else if (mTaskView != null) {
                            mTaskView.onGetDeviceDetectionScheduleResult(ConstantValue.ERROR, null);
                        }
                    }
                });
    }

    public void syncPirPlan(String deviceId, boolean[] pirPlan) {
        if (pirPlan == null || pirPlan.length != PIR_PLAN_DURATION_NUM * WEEK_DAY_LEN) {
            return;
        }
        DeviceCmdApi.getInstance().getPirPlan(deviceId, new OnGetPirPlanListener() {
            @Override
            public void onGetPirPlan(int result, boolean[] list) {
                NooieLog.d("-->> DevicePIRPresenter syncPirPlan {r onGetPirPlan code=" + result);
                if (result == SDKConstant.CODE_CACHE) {
                    return;
                }
                if (result == Constant.OK && (list != null && list.length == PIR_PLAN_DURATION_NUM * WEEK_DAY_LEN)) {
                    DeviceSettingHelper.logPirPlanForBoolean("-->> DevicePIRPresenter pirPlan ", pirPlan);
                    DeviceSettingHelper.logPirPlanForBoolean("-->> DevicePIRPresenter pirList ", list);
                    boolean isPirPlanSame = true;
                    int pirPlanLen = pirPlan.length;
                    for (int i = 0; i < pirPlanLen; i++) {
                        if (pirPlan[i] != list[i]) {
                            isPirPlanSame = false;
                            break;
                        }
                    }

                    NooieLog.d("-->> DevicePIRPresenter syncPirPlan {r onGetPirPlan isPirPlanSame=" + isPirPlanSame);
                    if (!isPirPlanSame) {
                        setPirPlan(deviceId, pirPlan);
                    }
                }
            }
        });
    }

    public void setPirPlan(String deviceId, boolean[] pirPlanList) {
        if (pirPlanList == null || pirPlanList.length != PIR_PLAN_DURATION_NUM * WEEK_DAY_LEN) {
            return;
        }

        DeviceSettingHelper.logPirPlanForBoolean("-->> DevicePIRPresenter setPirPlan ", pirPlanList);
        PirPlan pirPlan = new PirPlan();
        pirPlan.plan = pirPlanList;
        DeviceCmdApi.getInstance().setPirPlan(deviceId, pirPlan, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
                /*
                Util.delayTask(10 * 1000, new Util.OnDelayTaskFinishListener() {
                    @Override
                    public void onFinish() {
                        DeviceCmdService.getInstance().camGetPIRPlan(deviceId, new OnGetPirPlanListener() {
                            @Override
                            public void onGetPirPlan(int result, boolean[] list) {
                                DeviceSettingHelper.logPirPlanForBoolean("-->> DevicePIRPresenter setPirPlan after set list", list);
                            }
                        });
                    }
                });
                */
            }
        });
    }

    @Override
    public void setApDevicePirPlan(String deviceId, boolean pirEnable) {
        if (!pirEnable) {
            return;
        }
        DeviceCmdApi.getInstance().getPirPlan(deviceId, new OnGetPirPlanListener() {
            @Override
            public void onGetPirPlan(int code, boolean[] plan) {
                if (code == SDKConstant.CODE_CACHE) {
                    return;
                }
                boolean[] pirPlan = new boolean[PIR_PLAN_DURATION_NUM * WEEK_DAY_LEN];
                for (int i = 0; i < pirPlan.length; i++) {
                    pirPlan[i] = true;
                }
                setPirPlan(deviceId, pirPlan);
            }
        });
    }

    public void getDevicePIRPLAN(String deviceId) {
        DeviceCmdService.getInstance(NooieApplication.mCtx).camGetPIRPlan(deviceId, new OnGetPirPlanListener() {
            @Override
            public void onGetPirPlan(int result, boolean[] list) {
                if (result == Constant.OK && mTaskView != null) {
                    ArrayList<DetectionSchedule> schedules = new ArrayList<>();
                    boolean[] pirPlanTimes = cutPirPlanTimes(list, 0);
                    DetectionSchedule schedule = convertDetectionSchedule(pirPlanTimes);
                    List<Integer> weekDays = getPirPlanWeekDays(list);
                    if (schedule != null) {
                        schedule.setId(0);
                        schedule.setWeekDays(weekDays);
                        schedule.setOpen(CollectionUtil.isNotEmpty(weekDays));
                        schedules.add(schedule);
                    }
                    mTaskView.onGetDeviceDetectionScheduleResult(ConstantValue.SUCCESS, schedules);
                } else if (mTaskView != null) {
                    mTaskView.onGetDeviceDetectionScheduleResult(ConstantValue.ERROR, null);
                }
            }
        });
    }

    private boolean[] cutPirPlanTimes(boolean[] list, int startDay) {
        boolean[] pirPlanTimes = new boolean[PIR_PLAN_DURATION_NUM];
        if (list == null || list.length < PIR_PLAN_DURATION_NUM * (startDay + 1)) {
            return pirPlanTimes;
        }

        for (int i = 0; i < PIR_PLAN_DURATION_NUM; i++) {
            int startIndex = startDay * PIR_PLAN_DURATION_NUM + i;
            pirPlanTimes[i] = list[startIndex];
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

    @Override
    public void test(String deviceId, String pDeviceId, String account) {
        DeviceCmdTestPresenter testPresenter = new DeviceCmdTestPresenter(deviceId, pDeviceId, true);
        testPresenter.startTest();
    }

}
