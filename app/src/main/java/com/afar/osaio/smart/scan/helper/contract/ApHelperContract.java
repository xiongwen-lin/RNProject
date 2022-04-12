package com.afar.osaio.smart.scan.helper.contract;

import android.os.Bundle;

import androidx.annotation.NonNull;

import com.afar.osaio.bean.ApDeviceInfo;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.nooie.sdk.listener.OnActionResultListener;

import rx.Observer;

public interface ApHelperContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void startSendHeartBeat(String deviceId, OnActionResultListener listener);

        void stopSendHeartBeat();
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void updateBleApDevice(boolean isSync, String user, String deviceId, Bundle data, Observer<Boolean> observer);

        void removeBleApDeviceInfoCache(boolean isSync, String user, String deviceId, Observer<Boolean> observer);

        void switchApDirectConnectMode(Bundle data, ApHelper.APDirectListener listener);

        void resetApDirectConnectMode(Bundle data, ApHelper.APDirectListener listener);

        void resetApDirectConnectMode(String model, ApHelper.APDirectListener listener);

        void setupApDeviceTime(String deviceId, String model, long timeStamp, OnActionResultListener listener);

        ApDeviceInfo getCurrentApDeviceInfo();

        void updateCurrentApDeviceInfo(Bundle data);

        void startBleApConnectionFrontKeepingTask(Bundle param);

        void stopBleApConnectionFrontKeepingTask();

        void setBleApDeviceConnectionFrontKeepingListener(ApHelper.BleApConnectionFrontKeepingListener listener);

        boolean checkBleApDeviceConnectingExist();

        void checkBleApDeviceConnectionBackgroundKeepingTask(boolean isBackground, ApHelper.BleApConnectionBackgroundKeepingListener listener);

        void stopBleApDeviceConnectionBackgroundKeepingTask();

        void removeBleApDeviceConnection(Bundle param, ApHelper.APDirectListener listener);

        void disconnectBleApDeviceConnection(Bundle param, ApHelper.APDirectListener listener);

        void setLastApDirectConnectingExist(boolean exist);

        boolean getLastApDirectConnectingExist();

    }
}
