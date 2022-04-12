package com.afar.osaio.account.contract;

import androidx.annotation.NonNull;

import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.entity.TwoAuthDevice;
import com.nooie.sdk.api.network.base.bean.entity.UserInfoResult;

import java.util.List;

public interface TwoAuthDetailContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void onGetUserInfo(UserInfoResult result);

        void onOpenTwoAuth(BaseResponse response);

        void onCloseTwoAuth(BaseResponse response);

        void onGetTwoAuthDevice(List<TwoAuthDevice> devices);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void getUserInfo();

        void openTwoAuth();

        void closeTwoAuth();

        void getTwoAuthDevice();
    }
}
