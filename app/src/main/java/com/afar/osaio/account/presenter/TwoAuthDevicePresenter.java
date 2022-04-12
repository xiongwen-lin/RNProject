package com.afar.osaio.account.presenter;

import com.afar.osaio.account.contract.TwoAuthDeviceContract;
import com.nooie.sdk.api.network.account.AccountService;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.bean.SDKConstant;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class TwoAuthDevicePresenter implements TwoAuthDeviceContract.Presenter {

    private TwoAuthDeviceContract.View mTaskView;

    public TwoAuthDevicePresenter(TwoAuthDeviceContract.View view) {
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
    public void removeTwoAuthDevice(String phoneCode) {
        AccountService.getService().twoAuthDel(phoneCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onRemoveTwoAuthDevice(SDKConstant.ERROR);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mTaskView != null) {
                            mTaskView.onRemoveTwoAuthDevice(SDKConstant.SUCCESS);
                        } else if (mTaskView != null) {
                             mTaskView.onRemoveTwoAuthDevice(SDKConstant.ERROR);
                        }
                    }
                });
    }
}
