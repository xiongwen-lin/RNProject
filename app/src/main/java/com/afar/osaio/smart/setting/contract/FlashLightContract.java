package com.afar.osaio.smart.setting.contract;

import androidx.annotation.NonNull;

import com.nooie.sdk.device.bean.PirStateV2;

public interface FlashLightContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onGetPirState(int state, PirStateV2 pirState);

        void onSetFlashLightMode(int state);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void getPirState(String deviceId);

        void setFlashLightMode(String deviceId, int mode);

    }
}
