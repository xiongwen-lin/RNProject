package com.afar.osaio.smart.smartlook.contract;

import androidx.annotation.NonNull;

import com.nooie.sdk.db.entity.LockRecordEntity;
import com.afar.osaio.smart.smartlook.profile.manager.SmartLookManager;

import java.util.List;

public interface LockRecordContract {

    interface View extends BaseBleContract.View{

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void notifyBleDeviceState(int connectState);

        void notifyGetLockRecord(String result, List<LockRecordEntity> lockRecords);
    }

    interface Presenter extends BaseBleContract.Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        SmartLookManager getSmartLookManager();

        void getLockRecords(String user, String deviceId);

        void getLockRecordsFromDevice(String user, String deviceId);

        void setBaseInfo(String userAccount, String deviceId);
    }
}
