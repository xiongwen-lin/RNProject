package com.afar.osaio.smart.home.presenter;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.message.bean.MsgUnreadInfo;
import com.afar.osaio.message.model.IMessageModel;
import com.afar.osaio.message.model.MessageModelImpl;
import com.afar.osaio.smart.home.contract.MessageContract;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.base.GlobalData;
import com.nooie.common.utils.configure.LanguageUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.BannerResult;
import com.nooie.sdk.api.network.base.core.NetConfigure;
import com.nooie.sdk.api.network.message.MessageService;

import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MessagePresenter implements MessageContract.Presenter {

    private MessageContract.View mTasksView;
    private IMessageModel mMessageModel;

    public MessagePresenter(MessageContract.View view) {
        this.mTasksView = view;
        this.mTasksView.setPresenter(this);
        mMessageModel = new MessageModelImpl();
    }

    @Override
    public void destroy() {
        if (mTasksView != null) {
            mTasksView.setPresenter(null);
            mTasksView = null;
        }
    }

    @Override
    public void loadMsgUnread(List<String> ids) {
        mMessageModel.getMsgUnreadObservable(ids)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<MsgUnreadInfo>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTasksView != null) {
                            mTasksView.notifyGetUnreadMsgFailed("");
                        }
                    }

                    @Override
                    public void onNext(MsgUnreadInfo msgUnreadInfo) {
                        if (mTasksView != null) {
                            mTasksView.notifyGetUnreadMsgSuccess(msgUnreadInfo);
                        }
                    }
                });
    }

    @Override
    public void loadBanner(String uid) {
        MessageService.getService().getBannerList(ConstantValue.BANNER_PARAM_CODE, LanguageUtil.getLocal(NooieApplication.mCtx).getLanguage(), uid, NetConfigure.getInstance().getAppId())
                .subscribeOn(rx.schedulers.Schedulers.io())
                .observeOn(rx.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(new rx.Observer<BaseResponse<BannerResult>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        NooieLog.d("-->> test banner e=" + e.toString());
                    }

                    @Override
                    public void onNext(BaseResponse<BannerResult> bannerResultBaseResponse) {
                        if (mTasksView != null && bannerResultBaseResponse.getCode() == StateCode.SUCCESS.code && bannerResultBaseResponse.getData() != null) {
                            mTasksView.onLoadBannerSuccess(bannerResultBaseResponse.getData().getContent_page_list());
                        } else {
                            NooieLog.d("-->> test banner bannerResultBaseResponse getCode " + bannerResultBaseResponse.getCode());
                        }
                    }
                });
    }
}
