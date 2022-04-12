package com.afar.osaio.receiver.contract;

import androidx.annotation.NonNull;

public interface MyAccountReceiverContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onLogout();
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void logout();
    }
}
