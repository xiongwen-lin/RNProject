package com.afar.osaio.account.presenter;

import android.text.TextUtils;

import com.afar.osaio.account.helper.MyAccountHelper;
import com.afar.osaio.account.view.IAccountView;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.brain.model.BrainModel;
import com.afar.osaio.smart.brain.model.IBrainModel;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.db.dao.UserRegionService;
import com.nooie.sdk.db.entity.CountryCodeEntity;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.sdk.api.network.account.AccountService;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.brain.BrainUrlResult;
import com.nooie.sdk.processor.user.UserApi;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by victor on 2018/6/28
 * Email is victor.qiao.0604@gmail.com
 */
public class VerifyAccountPresenterImpl implements IVerifyAccountPresenter {
    private IAccountView mAccountView;
    private IBrainModel mBrainModel;

    public VerifyAccountPresenterImpl(IAccountView view) {
        this.mAccountView = view;
        mBrainModel = new BrainModel();
    }

    /**
     * 获取验证码
     * get verification code
     *
     * @param account
     * @param country
     * @param type    1，注册获取验证码  2，忘记密码获取验证码
     */
    @Override
    public void sendVerifyCode(final String account, final String country, final int type) {
        sendVerifyCodeBySDK(account, country, type);
    }

    /**
     * 获取当前国别码
     * get current country code
     */
    @Override
    public void getCurrentCountryCode(String account) {
        Observable.just(account)
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String account) {
                        String country = CountryUtil.getCurrentCountry(NooieApplication.mCtx);
                        CountryCodeEntity countryCodeEntity = UserRegionService.getInstance().getUserRegionByAccount(account);
                        if (countryCodeEntity != null && !TextUtils.isEmpty(countryCodeEntity.getCountryCode())) {
                            country = countryCodeEntity.getCountryCode();
                        }
                        return Observable.just(country);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(String country) {
                        if (mAccountView != null) {
                            mAccountView.notifyCurrentCountryCode(country);
                        }
                    }
                });
    }

    /**
     * 获取验证码
     * get verification code
     *
     * @param account
     * @param country
     * @param type    1，注册获取验证码  2，忘记密码获取验证码
     */
    private void sendVerifyCodeBySDK(final String account, final String country, final int type) {
        boolean isRegistered = type != ConstantValue.USER_REGISTER_VERIFY;
        boolean isSavedCountryCode = type == ConstantValue.USER_REGISTER_VERIFY;
        UserApi.getInstance().getBrainUrlObservable(account, country, isRegistered, isSavedCountryCode)
                .flatMap(new Func1<BaseResponse<BrainUrlResult>, Observable<BaseResponse>>() {
                    @Override
                    public Observable<BaseResponse> call(BaseResponse<BrainUrlResult> response) {
                        if (response == null) {
                            return Observable.error(new Throwable(""));
                        }
                        return type == ConstantValue.USER_REGISTER_VERIFY ? AccountService.getService().sendRegisterVerifyCode(account, country) : AccountService.getService().sendForgetVerifyCode(account, country);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mAccountView != null) {
                            mAccountView.sendVerifyCodeResult("", StateCode.UNKNOWN.code);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mAccountView != null) {
                            mAccountView.sendVerifyCodeResult(ConstantValue.SUCCESS, response.getCode());
                        } else if (mAccountView != null) {
                            mAccountView.sendVerifyCodeResult("", response != null ? response.getCode() : StateCode.UNKNOWN.code);
                        }
                    }
                });
    }

    @Override
    public void checkAccountSourceForRegister(String account) {
        String countryCode = CountryUtil.getCurrentCountry(NooieApplication.mCtx);
        UserApi.getInstance().checkBrainUrlObservable(account, countryCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<BrainUrlResult>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mAccountView != null) {
                            mAccountView.onCheckAccountSource(SDKConstant.ERROR, false, "");
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<BrainUrlResult> response) {
                        if (mAccountView == null) {
                            return;
                        }
                        if (response == null || response.getCode() != StateCode.SUCCESS.code) {
                            mAccountView.onCheckAccountSource(SDKConstant.ERROR, false, "");
                            return;
                        }
                        List<String> brandList = response.getData() != null ? response.getData().getSchema() : new ArrayList<>();
                        boolean isSelf = MyAccountHelper.getInstance().checkAppCountSelf(brandList);
                        if (isSelf) {
                            mAccountView.onCheckAccountSourceForRegister(account);
                        } else {
                            mAccountView.onCheckAccountSource(SDKConstant.SUCCESS, true, MyAccountHelper.getInstance().convertFromBrandList(brandList));
                        }
                    }
                });
    }
}
