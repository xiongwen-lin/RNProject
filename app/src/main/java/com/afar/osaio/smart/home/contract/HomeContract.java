package com.afar.osaio.smart.home.contract;

import androidx.annotation.NonNull;

import com.afar.osaio.message.bean.MsgUnreadInfo;
import com.nooie.sdk.api.network.base.bean.entity.MsgActiveInfo;

import java.util.List;

public interface HomeContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void notifyReportUserInfoResult(String result);

        void onGetLastActiveMsgResult(String result, MsgActiveInfo msgActiveInfo);

        void onCheckNetworkStatus(String result, boolean isNetworkUsable);

        void onGetUnreadMsgSuccess(int state, MsgUnreadInfo info);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void reportUserInfo(String account, String password, float zone, String country, int type, String nickname, String photo, String phoneCode, int deviceType, int pushType, String pushToken, String appVersion, String appVersionCode, String phoneModel, String phoneBrand, String phoneVersion, String phoneScreen, String language, String packageName);

        void getLastActiveMsg();

        void updateMsgReadState(int msgId, int type);

        void checkNetworkStatus();

        void getUserInfo(String uid, String account);

        void clearLogFile();

        void getAllApDeviceHardVersion();

        void stopGetAllApDeviceHardVersion();

        void loadMsgUnread(List<String> ids, boolean isFirstLaunch);
    }
}
