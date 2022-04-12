package com.afar.osaio.account.contract;

import androidx.annotation.NonNull;

public interface TwoAuthDeviceContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onRemoveTwoAuthDevice(int state);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void removeTwoAuthDevice(String phoneCode);
    }
}
