package com.afar.osaio.smart.smartlook.contract;

import androidx.annotation.NonNull;

import com.nooie.sdk.db.entity.BleDeviceEntity;
import com.afar.osaio.smart.smartlook.profile.manager.SmartLookManager;

public interface LockAccountContract {

    interface View extends BaseBleContract.View{

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void notifyBleDeviceState(int connectState);

        void notifyLoadDataResult(String result, BleDeviceEntity bleDeviceEntity);

        void notifyDeleteAccountResult(String result, String deviceId);
    }

    interface Presenter extends BaseBleContract.Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        SmartLookManager getSmartLookManager();

        void loadData(String account, String deviceId);

        void deleteAccount(BleDeviceEntity deviceEntity);
    }
}
