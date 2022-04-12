package com.afar.osaio.smart.setting.view;

import androidx.annotation.NonNull;

import com.afar.osaio.bean.DetectionSchedule;
import com.nooie.sdk.device.bean.PirStateV2;

import java.util.List;

public interface DevicePIRContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onSetPIRModeResult(String result, PirStateV2 pirState, int operationType);

        void onGetPIRModeResult(String result, PirStateV2 pirState, int operationType);

        void onGetDeviceDetectionScheduleResult(String result, List<DetectionSchedule> schedules);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void setDevicePIRMode(String deviceId, PirStateV2 pirState, int operationType);

        void getDevicePIRMode(String deviceId);

        void getDeviceDetectionSchedule(String deviceId, boolean isSyncPirPlan);

        void setApDevicePirPlan(String deviceId, boolean pirEnable);

        void test(String deviceId, String pDeviceId, String account);
    }
}
