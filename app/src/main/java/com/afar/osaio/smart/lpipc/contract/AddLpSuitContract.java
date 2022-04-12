package com.afar.osaio.smart.lpipc.contract;

import androidx.annotation.NonNull;

public interface AddLpSuitContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onGetGatewayNumResult(String result, int deviceNum);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void getGatewayNum();
    }
}
