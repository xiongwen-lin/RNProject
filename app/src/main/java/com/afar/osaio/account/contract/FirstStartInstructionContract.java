package com.afar.osaio.account.contract;

import androidx.annotation.NonNull;

public interface FirstStartInstructionContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();
    }
}
