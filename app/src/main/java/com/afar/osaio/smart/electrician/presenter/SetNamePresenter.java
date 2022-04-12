package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.smart.cache.UserInfoCache;
import com.afar.osaio.smart.electrician.model.IMyProfileModel;
import com.afar.osaio.smart.electrician.model.MyProfileModel;
import com.afar.osaio.smart.electrician.view.ISetNameView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.tuya.smart.android.user.api.IReNickNameCallback;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IResultCallback;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class SetNamePresenter implements ISetNamePresenter {

    private ISetNameView setNameView;
    private IMyProfileModel myProfileModel;

    public SetNamePresenter(ISetNameView view) {
        setNameView = view;
        myProfileModel = new MyProfileModel();
    }

    @Override
    public void modifyUserNickname(final String nickName) {
        if (setNameView != null) setNameView.showLoadingDialog();

        myProfileModel.updateNickname(nickName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && setNameView != null) {
                            UserInfoCache.getInstance().updateNickName(nickName);

                            TuyaHomeSdk.getUserInstance().reRickName(nickName, new IReNickNameCallback() {
                                @Override
                                public void onSuccess() {
                                    if (setNameView != null) {
                                        setNameView.notifySetSelfNameResult(ConstantValue.SUCCESS);
                                        setNameView.hideLoadingDialog();
                                    }
                                }

                                @Override
                                public void onError(String code, String error) {
                                    if (setNameView != null) {
                                        setNameView.notifySetSelfNameResult(error);
                                        setNameView.hideLoadingDialog();
                                    }
                                }
                            });
                        } else if (response != null && setNameView != null) {
                            String errorCode = ConstantValue.ERROR;
                            if (setNameView != null) {
                                setNameView.notifySetSelfNameResult(errorCode);
                                setNameView.hideLoadingDialog();
                            }
                        }
                    }

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (setNameView != null) {
                            setNameView.notifySetSelfNameResult(ConstantValue.ERROR);
                            setNameView.hideLoadingDialog();
                        }
                    }
                });
    }

    @Override
    public void modifyMemberNickname(long memberId, String remarkName) {
        if (setNameView != null) setNameView.showLoadingDialog();
        TuyaHomeSdk.getDeviceShareInstance().renameShareNickname(memberId, remarkName, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                if (setNameView != null) {
                    setNameView.notifySetMemberNameResult(code);
                    setNameView.hideLoadingDialog();
                }
            }

            @Override
            public void onSuccess() {
                if (setNameView != null) {
                    setNameView.notifySetMemberNameResult(ConstantValue.SUCCESS);
                    setNameView.hideLoadingDialog();
                }
            }
        });
    }
}
