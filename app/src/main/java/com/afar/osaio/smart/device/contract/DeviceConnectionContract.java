package com.afar.osaio.smart.device.contract;

import android.content.Context;
import androidx.annotation.NonNull;

import com.afar.osaio.smart.device.listener.ConnectShortLinkDeviceListener;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;

public interface DeviceConnectionContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onCheckDeviceConnectResult(boolean isReconnect, String user, String deviceId, String pDeviceId);

        void onShortLinkKeepResult(String taskId, int code);

        void onSendHeartBeatResult(int code);

    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void startAllDeviceConnectionTask(String user, boolean isForceConnect);

        void stopAllDeviceConnectionTask();

        void startHubDeviceConnectionTask(String user);

        void stopHubDeviceConnectionTask();

        void tryToReconnectWhenWifiChanged(Context context, String user);

        void stopReconnectDeviceTask();

        void setIsPauseCheckConn(boolean isPauseCheckConn);

        void checkDeviceConnection(Context context, String user);

        void stopCheckDeviceConnection();

        void startDeviceConnectTask(String user, String deviceId);

        void stopDeviceConnectTask();

        void checkDevicesConnection(Context context, String user);

        void stopCheckDevicesConnection();

        void startConnectShortLinkDevice(String taskId, String account, BindDevice device, ConnectShortLinkDeviceListener listener);

        void stopConnectShortLinkDevice();

        void startQuickConnectShortLinkDevice(String taskId, String account, BindDevice device, ConnectShortLinkDeviceListener listener);

        void stopQuickConnectShortLinkDevice();

        void connectShortLinkDevice(String taskId, String account, String deviceId, ConnectShortLinkDeviceListener listener);

        void startShortLinkKeepTask(String taskId);

        void stopShortLinkKeepTask();

        void startSendHeartBeat(String deviceId);

        void stopSendHeartBeat();

        boolean checkDirectConnectionIsError();

    }
}
