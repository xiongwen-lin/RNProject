package com.afar.osaio.account.contract;

import androidx.annotation.NonNull;

import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.entity.LoginResult;

public interface TwoAuthLoginContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onSendTwoAuthCodeResult(int result);

        void onCheckAndLoginResult(int result, BaseResponse<LoginResult> response);

        void onCodeCounterChange(int state, String result);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void sendTwoAuthCode(String account, String countryCode);

        void stopSendTwoAuthCodeTask();

        void checkAndLogin(String account, String password, String country, String code);

        void startVerifyCodeCounter();

        void stopVerifyCodeCounter();
    }
}
