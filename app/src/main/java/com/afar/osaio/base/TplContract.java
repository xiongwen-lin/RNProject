package com.afar.osaio.base;

import androidx.annotation.NonNull;

public interface TplContract {

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
