package com.afar.osaio.smart.lpipc.contract;

import androidx.annotation.NonNull;

import com.afar.osaio.smart.device.bean.NooieDevice;
import com.nooie.sdk.device.bean.FormatInfo;
import com.nooie.sdk.device.bean.hub.HubInfo;

public interface GatewayInfoContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void notifyDeviceInfoResult(String result, NooieDevice deviceInfo);

        void onGetGatewayInfoResult(String result, HubInfo hubInfo);

        void onDeleteGatewayDeviceResult(String result);

        void onRestartGatewayDeviceResult(String result);

        void onClearDeviceUserSpaceResult(String result);

        void onQuerySDStatusSuccess(FormatInfo formatInfo, int status, String freeGB, String totalGB, int progress);

        void onSetSyncTimeResult(String result);

        void onSetLedResult(String result);

        void onCheckDeviceUpdateStatus(String result, int type);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void loadDeviceInfo(String account, String deviceId);

        void getGatewayInfo(String deviceId);

        void unbindDevice(String deviceId, String uid, String account);

        void unbindDevice(String deviceId, String uid, String account, boolean isOnline);

        void restartDevice(String deviceId);

        void clearDeviceUseSpace(String deviceId);

        void startQuerySDCardFormatState(String deviceId);

        void stopQuerySDCardFormatState();

        void setSyncTime(String uuid, final int mode, float timeZone, int timeOffset);

        void setLed(String deviceid, boolean open);

        void checkDeviceUpdateStatus(String deviceId);
    }
}
