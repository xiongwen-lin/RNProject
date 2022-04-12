package com.afar.osaio.smart.smartlook.contract;

import androidx.annotation.NonNull;

import com.afar.osaio.smart.smartlook.bean.BleDevice;

public interface AddAuthorizationContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void notifyAddAuthorizationCodeResult(String result);
    }

    interface Presenter {

        void addAuthorizationCode(String user, String uid, String phone, String code, BleDevice bleDevice);

        /**
         * destroy the presenter and set the view null
         */
        void destroy();
    }
}
