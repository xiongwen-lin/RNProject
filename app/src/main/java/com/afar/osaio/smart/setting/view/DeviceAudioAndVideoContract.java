package com.afar.osaio.smart.setting.view;

import androidx.annotation.NonNull;

import com.nooie.sdk.bean.DeviceComplexSetting;
import com.nooie.sdk.device.bean.DevAllSettingsV2;
import com.nooie.sdk.device.bean.ICRMode;
import com.nooie.sdk.device.bean.hub.CameraInfo;

public interface DeviceAudioAndVideoContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onGetDeviceSetting(int code, DeviceComplexSetting complexSetting);

        void onGetAllSettingResult(String msg, DevAllSettingsV2 settings);

        void notifyGetRecordWidthAudioSuccess(boolean open);

        void notifyGetRecordWidthAudioFailed(String message);

        void notifySetRecordWidthAudioResult(String result);

        void notifyGetRotateImageSuccess(boolean on);

        void notifyGetRotateImageFailed(String msg);

        void notifySetRotateImageResult(String result);

        void notifySetNightVisionResult(String result);

        void notifyGetNightVisionResult(String result, ICRMode mode);

        void onGetCamInfoResult(String result, CameraInfo info);

        void onSetMotionTrackingResult(String result);

        void onGetEnergyMode(int state, boolean open);

        void onSetEnergyMode(int state);

        void onGetWaterMark(int state, boolean open);

        void onSetWaterMark(int state);

    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void getDeviceSetting(String deviceId, String model);

        void getAllSetting(String deviceId);

        void getRecordAudioStatus(String deviceId);

        void setRecordWithAudioStatus(String deviceId, boolean open);

        void getRotateImage(String deviceId, boolean isSubDevice);

        void setRotateImage(String deviceId, boolean open, boolean isSubDevice);

        void getNightVision(String deviceId);

        void setNightVision(String deviceId, String model, int mode, boolean isSubDevice);

        void getCamInfo(String deviceId);

        void setMotionTrackingStatus(String deviceId, boolean open);

        void setEnergyMode(String deviceId, boolean open);

        void getEnergyMode(String deviceId);

        void setWaterMark(String deviceId, boolean open);

        void getWaterMark(String deviceId);
    }

}
