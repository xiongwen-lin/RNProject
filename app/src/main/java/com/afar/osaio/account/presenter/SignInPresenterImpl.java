package com.afar.osaio.account.presenter;

import android.text.TextUtils;

import com.afar.osaio.R;
import com.afar.osaio.account.helper.MyAccountHelper;
import com.afar.osaio.account.view.ISignInView;
import com.afar.osaio.base.NooieApplication;
import com.nooie.common.base.GlobalData;
import com.nooie.data.EventDictionary;
import com.nooie.eventtracking.EventTrackingApi;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.db.dao.UserInfoService;
import com.nooie.sdk.db.entity.UserInfoEntity;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.push.helper.NooiePushMsgHelper;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.account.AccountService;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.LoginResult;
import com.nooie.sdk.api.network.base.bean.entity.brain.BrainUrlResult;
import com.nooie.sdk.encrypt.NooieEncryptService;
import com.nooie.sdk.processor.user.UserApi;
import com.tuya.smart.android.user.api.ILoginCallback;
import com.tuya.smart.android.user.bean.User;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IResultCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by victor on 2018/6/26
 * Email is victor.qiao.0604@gmail.com
 */
public class SignInPresenterImpl implements ISignInPresenter {
    private ISignInView mSignInView;

    private static final int RETRY_LOGIN_TUYA_TIME_LEN = 3 * 1000;
    private static final long MAX_VERIFY_CODE_LIMIT_TIME = 180L;
    private long mVerifyCodeLimitTime = MAX_VERIFY_CODE_LIMIT_TIME;
    private Subscription mVerifyCodeCounter = null;
    private boolean mIsRetryLoginTuya = false;
    private Subscription mRetryLoginTuyaTask = null;

    public SignInPresenterImpl(ISignInView signInView) {
        this.mSignInView = signInView;
    }

    @Override
    public void destroy() {
        mSignInView = null;
    }

    @Override
    public void signIn(final String account, final String psd) {
        signInAppBySDK(account, psd);
    }

    @Override
    public void loadAccountHistory() {
        Observable.create(new Observable.OnSubscribe<List<UserInfoEntity>>() {
            @Override
            public void call(Subscriber<? super List<UserInfoEntity>> subscriber) {
                subscriber.onNext(UserInfoService.getInstance().getAllUserInfo());
                subscriber.onCompleted();
                return;
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<UserInfoEntity>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(List<UserInfoEntity> userInfoEntities) {
                        if (mSignInView != null) {
                            mSignInView.onLoadAccountHistorySuccess(userInfoEntities);
                        }
                    }
                });
    }

    @Override
    public void removeAccountFromHistory(String account) {
        if (!TextUtils.isEmpty(account)) {
            Observable.just(account)
                    .flatMap(new Func1<String, Observable<Boolean>>() {
                        @Override
                        public Observable<Boolean> call(String account) {
                            UserInfoService.getInstance().deleteLoginUserInfo(account);
                            return Observable.just(true);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Boolean>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                        }

                        @Override
                        public void onNext(Boolean aBoolean) {
                        }
                    });
        }
    }

    @Override
    public void sendRegisterVerifyCode(int sendCodeType, final String account, final String country) {
        sendRegisterVerifyCodeBySDK(sendCodeType, account, country);
    }

    @Override
    public void checkRegisterVerifyCode(final String account, final String code, final String country) {
        checkRegisterVerifyCodeBySDK(account, code, country);
    }

    @Override
    public void stopVerifyCodeCounter() {
        if (mVerifyCodeCounter != null && !mVerifyCodeCounter.isUnsubscribed()) {
            mVerifyCodeCounter.unsubscribe();
        }
    }

    @Override
    public void checkAccountSourceForSignIn(String account, String password) {
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
                        if (mSignInView != null) {
                            mSignInView.onCheckAccountSource(SDKConstant.ERROR, false, "", true);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<BrainUrlResult> response) {
                        if (mSignInView == null) {
                            return;
                        }
                        if (response == null || response.getCode() != StateCode.SUCCESS.code) {
                            mSignInView.onCheckAccountSource(SDKConstant.ERROR, false, "", true);
                            return;
                        }
                        List<String> brandList = response.getData() != null ? response.getData().getSchema() : new ArrayList<>();
                        boolean isSelf = MyAccountHelper.getInstance().checkAppCountSelf(brandList);
                        if (isSelf) {
                            mSignInView.onCheckAccountSourceForSignIn(account, password);
                        } else {
                            mSignInView.onCheckAccountSource(SDKConstant.SUCCESS, true, MyAccountHelper.getInstance().convertFromBrandList(brandList), true);
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
                        if (mSignInView != null) {
                            mSignInView.onCheckAccountSource(SDKConstant.ERROR, false, "", false);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<BrainUrlResult> response) {
                        if (mSignInView == null) {
                            return;
                        }
                        if (response == null || response.getCode() != StateCode.SUCCESS.code) {
                            mSignInView.onCheckAccountSource(SDKConstant.ERROR, false, "", false);
                            return;
                        }
                        List<String> brandList = response.getData() != null ? response.getData().getSchema() : new ArrayList<>();
                        boolean isSelf = MyAccountHelper.getInstance().checkAppCountSelf(brandList);
                        if (isSelf) {
                            mSignInView.onCheckAccountSourceForRegister(account);
                        } else {
                            mSignInView.onCheckAccountSource(SDKConstant.SUCCESS, true, MyAccountHelper.getInstance().convertFromBrandList(brandList), false);
                        }
                    }
                });
    }

    private void startVerifyCodeCounter() {
        stopVerifyCodeCounter();
        mVerifyCodeCounter = Observable.interval(0, 1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Long total) {
                        NooieLog.d("-->> SignInPresenterImpl startVerifyCodeCounter onNext total=" + total);
                        if (mVerifyCodeLimitTime > 0) {
                            mVerifyCodeLimitTime--;
                            if (mSignInView != null) {
                                mSignInView.notifyRegisterVerifyCodeLimitTime(String.valueOf(mVerifyCodeLimitTime));
                            }
                        } else {
                            stopVerifyCodeCounter();
                            if (mSignInView != null) {
                                mSignInView.notifyRegisterVerifyCodeLimitTime(ConstantValue.SUCCESS);
                            }
                        }
                    }
                });
    }

    /**
     * 使用sdk接口登录
     *
     * @param account
     * @param psd
     */
    private void signInAppBySDK(String account, String psd) {
        boolean isUseJPush = NooieDeviceHelper.isUseJPush(GlobalData.getInstance().getRegion());
        NooieApplication.get().setIsUseJPush(isUseJPush);
        String countryCode = CountryUtil.getCurrentCountry(NooieApplication.mCtx);
        UserApi.getInstance().login(account, psd, countryCode, MyAccountHelper.getInstance().createReportUserRequest())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<LoginResult>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        NooieLog.d("-->> SignInPresenterImpl signInAppBySDK onError e=" + (throwable != null ? throwable.toString() : "throwable null"));
                        if (mSignInView != null) {
                            String msg = NooieApplication.mCtx.getResources().getString(R.string.network_error0);
                            mSignInView.notifySignInResult(msg, StateCode.UNKNOWN.code);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<LoginResult> response) {
                        GlobalData.getInstance().log("SignInPresenterImpl signInAppBySDK");
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mSignInView != null) {
                            NooieLog.d("-->> SignInPresenterImpl signInAppBySDK success");
                            String country = response.getData() != null && !TextUtils.isEmpty(response.getData().getRegister_country()) ? response.getData().getRegister_country() : countryCode;
                            NooieEncryptService service = NooieEncryptService.getInstance();
                            String uid = service.getTuyaPsd(response.getData().getUid());
                            mIsRetryLoginTuya = true;
                            loginTuyaAccount(country, response.getData().getUid(), uid);
                        } else if (mSignInView != null) {
                            NooieLog.d("-->> SignInPresenterImpl signInAppBySDK fail");
                            int code = response != null ? response.getCode() : StateCode.UNKNOWN.code;
                            if (code == StateCode.TWO_AUTH_LOGIN.code) {
                                mSignInView.notifySignInResult(ConstantValue.ERROR, code);
                                return;
                            }
                            String msg = NooieApplication.mCtx.getResources().getString(R.string.network_error0);
                            if (code == StateCode.ACCOUNT_NOT_EXIST.code) {
                                msg = NooieApplication.mCtx.getResources().getString(R.string.sign_in_account_not_exist);
                            } else if (code == StateCode.PASSWORD_ERROR.code) {
                                msg = NooieApplication.mCtx.getResources().getString(R.string.sign_in_password_incorrect);
                            } else if (code == StateCode.ACCOUNT_FORMAT_ERROR.code) {
                                msg = NooieApplication.mCtx.getResources().getString(R.string.camera_share_account_invalid);
                            }
                            mSignInView.notifySignInResult(msg, code);
                        }
                    }
                });
    }

    private void loginTuyaAccount(String countryCode, final String uid, String passwd) {
        NooieLog.d("-->> SignInPresenterImpl loginTuyaAccount 1001");
        TuyaHomeSdk.getUserInstance().loginWithUid(countryCode, uid, passwd, new ILoginCallback() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    setupTuyaPush();
                    GlobalPrefs globalPrefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
                    globalPrefs.setTuyaPhoto(TextUtils.isEmpty(user.getHeadPic()) ? "" : user.getHeadPic());
                    if (mSignInView != null) {
                        mSignInView.hideLoadingDialog();
                        mSignInView.notifySignInResult(ConstantValue.SUCCESS, StateCode.SUCCESS.code);
                    }
                }
            }

            @Override
            public void onError(String s, String s1) {
                NooieLog.d("-->> SignInPresenterImpl loginTuyaAccount 1002 s=" + s + " s1 " + s1 + " mIsRetryLoginTuya=" + mIsRetryLoginTuya);
                if (mIsRetryLoginTuya) {
                    mIsRetryLoginTuya = false;
                    startRetryLoginTuyaTask(countryCode, uid, passwd);
                    return;
                }
                stopRetryLoginTuyaTask();
                NooieLog.d("-->> SignInPresenterImpl loginTuyaAccount 1003");
                if (mSignInView != null) {
                    String msg = NooieApplication.mCtx.getResources().getString(R.string.network_error0);
                    mSignInView.hideLoadingDialog();
                    mSignInView.notifySignInResult(msg, StateCode.UNKNOWN.code);
                }
            }
        });
    }

    private void setupTuyaPush() {
        if (NooieApplication.get().getIsUseJPush()) {
            registerToTuya(NooiePushMsgHelper.getPushToken(), ConstantValue.PUSH_UMENG_TUYA_PUSHPROVIDER);
        } else {
            registerToTuya(NooiePushMsgHelper.getPushToken(), ConstantValue.PUSH_FCM_TUYA_PUSHPROVIDER);
        }
    }

    private void registerToTuya(final String alias, final String pushProvider) {
        TuyaHomeSdk.getPushInstance().registerDevice(alias, pushProvider, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                NooieLog.e("---------->>registerToTuya " + error);
            }

            @Override
            public void onSuccess() {
                NooieLog.e("---------->>registerToTuya success");
            }
        });
    }

    /**
     * 使用sdk发送注册码
     *
     * @param account
     * @param country
     */
    private void sendRegisterVerifyCodeBySDK(int sendCodeType, final String account, final String country) {
        UserApi.getInstance().getBrainUrlObservable(account, country, false, true)
                .flatMap(new Func1<BaseResponse<BrainUrlResult>, Observable<BaseResponse>>() {
                    @Override
                    public Observable<BaseResponse> call(BaseResponse<BrainUrlResult> response) {
                        if (response == null) {
                            return Observable.error(new Throwable(""));
                        }
                        return AccountService.getService().sendRegisterVerifyCode(account, country);
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
                        if (mSignInView != null) {
                            mSignInView.notifySendRegisterVerifyCode(sendCodeType, ConstantValue.ERROR, StateCode.UNKNOWN.code, account);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mSignInView != null) {
                            mSignInView.notifySendRegisterVerifyCode(sendCodeType, ConstantValue.SUCCESS, response.getCode(), account);
                            mVerifyCodeLimitTime = MAX_VERIFY_CODE_LIMIT_TIME;
                            startVerifyCodeCounter();
                        } else if (mSignInView != null) {
                            mSignInView.notifySendRegisterVerifyCode(sendCodeType, ConstantValue.ERROR, response != null ? response.getCode() : StateCode.UNKNOWN.code, account);
                        }
                    }
                });
    }

    /**
     * 使用sdk检测注册码
     *
     * @param account
     * @param code
     * @param country
     */
    private void checkRegisterVerifyCodeBySDK(final String account, final String code, final String country) {
        UserApi.getInstance().getBrainUrlObservable(account, country, false, false)
                .flatMap(new Func1<BaseResponse<BrainUrlResult>, Observable<BaseResponse>>() {
                    @Override
                    public Observable<BaseResponse> call(BaseResponse<BrainUrlResult> response) {
                        if (response == null) {
                            return Observable.error(new Throwable(""));
                        }
                        return AccountService.getService().checkRegisterVerifyCode(account, code);
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
                        if (mSignInView != null) {
                            mSignInView.notifyCheckVerifyCodeResult(ConstantValue.ERROR, StateCode.UNKNOWN.code);
                            EventTrackingApi.getInstance().trackNormalEvent(EventDictionary.EVENT_ID_CHECK_VERIFY_CODE, "", EventDictionary.EVENT_CODE_FAIL, null, "");
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mSignInView != null) {
                            EventTrackingApi.getInstance().trackNormalEvent(EventDictionary.EVENT_ID_CHECK_VERIFY_CODE, "", EventDictionary.EVENT_CODE_SUCCESS, null, "");
                            mSignInView.notifyCheckVerifyCodeResult(ConstantValue.SUCCESS, response.getCode());
                        } else if (mSignInView != null) {
                            EventTrackingApi.getInstance().trackNormalEvent(EventDictionary.EVENT_ID_CHECK_VERIFY_CODE, "", EventDictionary.EVENT_CODE_FAIL, null, "");
                            mSignInView.notifyCheckVerifyCodeResult(ConstantValue.ERROR, response != null ? response.getCode() : StateCode.UNKNOWN.code);
                        }
                    }
                });
    }

    private void startRetryLoginTuyaTask(String countryCode, final String uid, String passwd) {
        NooieLog.d("-->> SignInPresenterImpl startRetryLoginTuyaTask 1001");
        stopRetryLoginTuyaTask();
        mRetryLoginTuyaTask = Observable.just(1)
                .delay(RETRY_LOGIN_TUYA_TIME_LEN, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        NooieLog.d("-->> SignInPresenterImpl startRetryLoginTuyaTask 1002");
                        loginTuyaAccount(countryCode, uid, passwd);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        NooieLog.d("-->> SignInPresenterImpl startRetryLoginTuyaTask 1003");
                        loginTuyaAccount(countryCode, uid, passwd);
                    }
                });
    }

    private void stopRetryLoginTuyaTask() {
        if (mRetryLoginTuyaTask != null && !mRetryLoginTuyaTask.isUnsubscribed()) {
            mRetryLoginTuyaTask.unsubscribe();
            mRetryLoginTuyaTask = null;
        }
    }
}
