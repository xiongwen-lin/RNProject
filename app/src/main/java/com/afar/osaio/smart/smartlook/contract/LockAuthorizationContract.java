package com.afar.osaio.smart.smartlook.contract;

import androidx.annotation.NonNull;

import com.nooie.sdk.db.entity.LockAuthorizationEntity;
import com.afar.osaio.smart.smartlook.profile.manager.SmartLookManager;

import java.util.List;

public interface LockAuthorizationContract {

    interface View extends BaseBleContract.View{

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void notifyBleDeviceState(int connectState);

        void notifyGetLockAuthorization(String result, List<LockAuthorizationEntity> lockAuthorizationEntities);

        void notifyCreateAuthorization(String result, LockAuthorizationEntity lockAuthorizationEntity);

        void notifyDeleteAuthorization(String result, LockAuthorizationEntity lockAuthorizationEntity);
    }

    interface Presenter extends BaseBleContract.Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        SmartLookManager getSmartLookManager();

        void getLockAuthorizations(String user, String deviceId);

        void getLockAuthorizationsFromDevice(String user, String deviceId);

        void updateAuthorization(LockAuthorizationEntity lockAuthorizationEntity);

        void deleteAuthorization(LockAuthorizationEntity lockAuthorizationEntity);

        void setBaseInfo(String userAccount, String deviceId);
    }
}
