package com.afar.osaio.smart.home.contract;

import androidx.annotation.NonNull;

import com.afar.osaio.message.bean.MsgUnreadInfo;
import com.nooie.sdk.api.network.base.bean.entity.BannerResult;

import java.util.List;

public interface MessageContract {

    interface View {

        /**
         * set the presenter attaching to the view
         */
        void setPresenter(@NonNull Presenter presenter);

        void notifyGetUnreadMsgFailed(String message);

        void notifyGetUnreadMsgSuccess(MsgUnreadInfo info);

        void onLoadBannerSuccess(List<BannerResult.BannerInfo> bannerList);

        void onLoadBannerFail(String msg);
    }

    interface Presenter {

        /**
         * destroy the presenter and set the view null
         */
        void destroy();

        void loadMsgUnread(List<String> ids);

        void loadBanner(String uid);
    }
}
