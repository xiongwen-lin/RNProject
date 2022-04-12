package com.afar.osaio.account.presenter;

import com.nooie.sdk.api.network.base.bean.StateCode;
import com.afar.osaio.account.view.IChangePasswordView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.processor.user.UserApi;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by victor on 2018/7/10
 * Email is victor.qiao.0604@gmail.com
 */
public class ChangedPasswordPresenterImpl implements IChangedPasswordPresenter {
    private IChangePasswordView mChangePsdView;

    public ChangedPasswordPresenterImpl(IChangePasswordView changePsdView) {
        this.mChangePsdView = changePsdView;
    }


    /**
     * 修改密码 modify password
     *
     * @param newPsd 新密码 new password
     */

    @Override
    public void changePassword(String account, final String oldPsd, final String newPsd) {

        changePasswordBySDK(account, oldPsd, newPsd);
        /*
        AccountService.getService().updatePassword(MD5Util.MD5Hash(newPsd), MD5Util.MD5Hash(oldPsd))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mChangePsdView != null){
                            mChangePsdView.notifyChangePsdState("");
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mChangePsdView != null) {
                            GlobalData.getInstance().updatePassword(newPsd);
                            UserInfoService.getInstance().updatePassword(GlobalData.getInstance().getAccount(), newPsd);
                            mChangePsdView.notifyChangePsdState(ConstantValue.SUCCESS);
                        } else if (mChangePsdView != null) {
                            mChangePsdView.notifyChangePsdState("");
                        }
                    }
                });
         */
    }

    public void changePasswordBySDK(String account, final String oldPsd, final String newPsd) {
        UserApi.getInstance().changePassword(account, oldPsd, newPsd)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mChangePsdView != null){
                            mChangePsdView.notifyChangePsdState("");
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mChangePsdView != null) {
                            mChangePsdView.notifyChangePsdState(ConstantValue.SUCCESS);
                        } else if (mChangePsdView != null) {
                            mChangePsdView.notifyChangePsdState("");
                        }
                    }
                });
    }
}
