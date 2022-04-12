package com.afar.osaio.smart.home.contract;

import androidx.annotation.NonNull;

import com.afar.osaio.message.bean.MsgUnreadInfo;
import com.nooie.sdk.api.network.base.bean.entity.GatewayDevice;

import java.util.List;

public interface PersonContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void notifyLogoutResult(String result);

        void notifyGetUserInfoResult(String result);

        void notifyChangeUserNameResult(String result);

        void notifyRefreshUserPortrait(String result, boolean isUploadPortrait);

        void onLoadGatewayDevicesResult(String result, List<GatewayDevice> gatewayDevices);

        void onGetUnreadMsgSuccess(int state, MsgUnreadInfo info);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void logout();

        void getUserInfo(String userId, final String userName, String portraitPath);

        void changeUserName(String name);

        void uploadPictures(String userid, String username, String photoPath);

        void downloadPortrait(String userId, final String username, String portraitPath);

        void setDownloadPortraitState(boolean isDownloadPortrait);

        void loadGatewayDevices();

        void loadMsgUnread(List<String> ids);

    }
}
