package com.afar.osaio.smart.setting.contract;

import androidx.annotation.NonNull;

import com.nooie.sdk.device.bean.NooieMediaMode;

public interface FileSettingContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onGetFileSettingMode(int state, NooieMediaMode mode);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void getFileSettingMode(String deviceId);
    }
}
