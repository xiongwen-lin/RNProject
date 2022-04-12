package com.afar.osaio.account.presenter;

import android.os.Build;

import com.afar.osaio.account.contract.TwoAuthDetailContract;
import com.afar.osaio.base.NooieApplication;
import com.nooie.common.base.GlobalData;
import com.nooie.common.utils.configure.PhoneUtil;
import com.nooie.sdk.api.network.account.AccountService;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.TwoAuthDevice;
import com.nooie.sdk.api.network.base.bean.entity.UserInfoResult;

import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class TwoAuthDetailPresenter implements TwoAuthDetailContract.Presenter {

    private TwoAuthDetailContract.View mTaskView;

    public TwoAuthDetailPresenter(TwoAuthDetailContract.View view) {
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
    public void getUserInfo() {
        AccountService.getService().getUserInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<UserInfoResult>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onGetUserInfo(null);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<UserInfoResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mTaskView != null) {
                            mTaskView.onGetUserInfo(response.getData());
                        } else if (mTaskView != null) {
                            mTaskView.onGetUserInfo(null);
                        }
                    }
                });
    }

    @Override
    public void openTwoAuth() {
        String phoneCode = GlobalData.getInstance().getPhoneId();
        String phoneBrand = Build.BRAND;
        String phoneModel = Build.MODEL;
        String phoneName= PhoneUtil.getPhoneName(NooieApplication.mCtx);
        AccountService.getService().twoAuthOpen(phoneCode, phoneModel, phoneBrand, phoneName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onOpenTwoAuth(null);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (mTaskView != null) {
                            mTaskView.onOpenTwoAuth(response);
                        }
                    }
                });
    }

    @Override
    public void closeTwoAuth() {
        AccountService.getService().twoAuthClose()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onCloseTwoAuth(null);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (mTaskView != null) {
                            mTaskView.onCloseTwoAuth(response);
                        }
                    }
                });
    }

    @Override
    public void getTwoAuthDevice() {
        AccountService.getService().twoAuthDeviceList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<List<TwoAuthDevice>>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(BaseResponse<List<TwoAuthDevice>> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mTaskView != null) {
                            mTaskView.onGetTwoAuthDevice(response.getData());
                        }
                    }
                });
    }
}
