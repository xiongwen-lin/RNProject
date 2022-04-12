package com.afar.osaio.base.contract;

import androidx.annotation.NonNull;

public interface BaseSupportContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onUpdateShareMsgState(String result);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void updateShareMsgState(int msgId, int shareId, int status);

        void changeDeviceUpgradeState(String user, String deviceId, int platform, int upgradeState);

        void startNetworkDetector();

        void stopNetworkDetector();
    }
}
