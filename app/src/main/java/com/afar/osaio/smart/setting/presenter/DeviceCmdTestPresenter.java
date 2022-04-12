package com.afar.osaio.smart.setting.presenter;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.util.Util;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.device.DeviceCmdService;
import com.nooie.sdk.device.bean.hub.PirPlan;
import com.nooie.sdk.listener.OnActionResultListener;

public class DeviceCmdTestPresenter {

    private String uuid;
    private String hubUUID;
    private boolean enable;

    public DeviceCmdTestPresenter(String uuid, String hubUUID, boolean enable) {
        this.uuid = uuid;
        this.hubUUID = hubUUID;
        this.enable = enable;
    }

    public void startTest() {
        setToDefaultSettings();
        Util.delayTask(20 * 1000, new Util.OnDelayTaskFinishListener() {
            @Override
            public void onFinish() {
                //getAllSettings();
            }
        });
    }
    public void getAllSettings() {
//        DeviceCmdService service = DeviceCmdService.getInstance();
//
//        service.camGetPIRPlan(this.uuid, new OnGetPirPlanListener() {
//            @Override
//            public void onGetPirPlan(int result, boolean[] list) {
//            }
//        });
//
//        service.hubGetInfo(this.hubUUID, new OnGetHubInfoListener() {
//            @Override
//            public void onGetHubInfo(int result, HubInfo info) {
//            }
//        });
//
//        service.hubGetLed(this.hubUUID, new OnSwitchStateListener() {
//            @Override
//            public void onStateInfo(int code, boolean on) {
//            }
//        });
//
//        service.hubGetTime(this.hubUUID, new OnGetTimeListener() {
//            @Override
//            public void onGetTime(int result, int mode, float timeZone, int timeOffset) {
//            }
//        });
//
//        service.hubSDcardStatus(this.hubUUID, new OnGetFormatInfoListener() {
//            @Override
//            public void onGetFormatInfo(int code, FormatInfo info) {
//            }
//        });
//
//        service.camGetPIRPlan(this.uuid, new OnGetPirPlanListener() {
//            @Override
//            public void onGetPirPlan(int result, boolean[] list) {
//            }
//        });
//
//        service.camGetFDMode(this.uuid, new OnSwitchStateListener() {
//            @Override
//            public void onStateInfo(int code, boolean on) {
//            }
//        });
//
//        service.camGetPDMode(this.uuid, new OnSwitchStateListener() {
//            @Override
//            public void onStateInfo(int code, boolean on) {
//            }
//        });
//
//        service.camGetIR(this.uuid, new OnIRModeListener() {
//            @Override
//            public void onIR(int code, IRMode mode) {
//            }
//        });
//
//        service.camGetPIRMode(this.uuid, new OnGetPirStateListener() {
//            @Override
//            public void onGetPirState(int result, PirState state) {
//            }
//        });
//
//        service.camGetRecDates(this.uuid, new OnGetRecDatesListener() {
//            @Override
//            public void onRecDates(int code, int[] list, int today) {
//            }
//        });
//
//        service.camGetRecList(this.uuid, (int)(System.currentTimeMillis()/1000), new OnGetSdcardRecordListener(){
//            @Override
//            public void onGetSdcardRecordInfo(int code, RecordFragment[] records) {
//            }
//        });
//
//        service.camGetVidRotate(this.uuid, new OnSwitchStateListener() {
//            @Override
//            public void onStateInfo(int code, boolean on) {
//            }
//        });
//
//        service.camGetInfo(this.uuid, new OnGetSubCamInfoListener() {
//            @Override
//            public void onSubCamInfo(int result, CameraInfo info) {
//            }
//        });
    }

    public void setToDefaultSettings() {
        DeviceCmdService service = DeviceCmdService.getInstance(NooieApplication.mCtx);

//        service.hubSetLed(this.hubUUID, enable, new OnActionResultListener() {
//            @Override
//            public void onResult(int code) {
//            }
//        });
//
//        service.hubSetTime(this.hubUUID, 0, 5.0f, 0, new OnActionResultListener() {
//            @Override
//            public void onResult(int code) {
//            }
//        });
//
//        service.camSetFDMode(this.uuid, enable, new OnActionResultListener() {
//            @Override
//            public void onResult(int code) {
//            }
//        });
//
//        IRMode irMode = enable ? IRMode.IR_MODE_AUTO : IRMode.IR_MODE_OFF;
//        service.camSetIR(this.uuid, irMode, new OnActionResultListener() {
//            @Override
//            public void onResult(int code) {
//            }
//        });
//
//        service.camSetPDMode(this.uuid, enable, new OnActionResultListener() {
//            @Override
//            public void onResult(int code) {
//            }
//        });
//
//        PirState pir = new PirState();
//        pir.enable = enable;
//        pir.delay = 0;
//        pir.duration = 10;
//        pir.pd = true;
//        pir.sensitivityLevel = SensitivityLevel.SENSITIVITY_LEVEL_MIDDLE;
//        pir.siren = true;
//        service.camSetPIRMode(this.uuid, pir, new OnActionResultListener() {
//            @Override
//            public void onResult(int code) {
//
//            }
//        });
//
//        service.camSetVidRotate(this.uuid, enable, new OnActionResultListener() {
//            @Override
//            public void onResult(int code) {
//
//            }
//        });

        PirPlan plan = new PirPlan();
        plan.plan = new boolean[7 * 48];
        for (int i = 4; i < 8; i++) {
            plan.plan[i] = true;
        }
        StringBuilder planSb = new StringBuilder();
        for (int i = 0; i < plan.plan.length; i++) {
            planSb.append(plan.plan[i] ? "1" : "0");
        }
        NooieLog.d("{r-->> DeviceCmdTestPresenter setToDefaultSettings plan=" + planSb.toString());
        service.camSetPIRPlan(this.uuid, plan, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
            }
        });

        if (enable) {
            service.hubStartAuth(uuid, new OnActionResultListener() {
                @Override
                public void onResult(int code) {
                }
            });
        } else {
            service.hubStopAuth(uuid, new OnActionResultListener() {
                @Override
                public void onResult(int code) {
                }
            });
        }
    }
}
