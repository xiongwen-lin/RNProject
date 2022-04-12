package com.afar.osaio.receiver.presenter;

import com.afar.osaio.receiver.contract.MyAccountReceiverContract;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.processor.user.UserApi;

import rx.Observer;

public class MyAccountReceiverPresenter implements MyAccountReceiverContract.Presenter {

    private MyAccountReceiverContract.View mTaskView;

    public MyAccountReceiverPresenter(MyAccountReceiverContract.View view) {
        mTaskView = view;
        mTaskView.setPresenter(this);
    }

    @Override
    public void destroy() {
        if (mTaskView != null) {
            mTaskView.setPresenter(null);
            mTaskView = null;
        }
    }

    @Override
    public void logout() {
        UserApi.getInstance().logout(true, new Observer<BaseResponse>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                if (mTaskView != null) {
                    mTaskView.onLogout();
                }
            }

            @Override
            public void onNext(BaseResponse response) {
                if (mTaskView != null) {
                    mTaskView.onLogout();
                }
            }
        });
    }
}
