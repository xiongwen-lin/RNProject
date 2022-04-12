package com.afar.osaio.account.presenter;

import android.text.TextUtils;

import com.afar.osaio.account.contract.TwoAuthLoginContract;
import com.afar.osaio.account.helper.MyAccountHelper;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.push.helper.NooiePushMsgHelper;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.LoginResult;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.encrypt.NooieEncryptService;
import com.nooie.sdk.processor.user.UserApi;
import com.tuya.smart.android.user.api.ILoginCallback;
import com.tuya.smart.android.user.bean.User;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IResultCallback;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class TwoAuthLoginPresenter implements TwoAuthLoginContract.Presenter {

    public static final long MAX_VERIFY_CODE_LIMIT_TIME = 180L;
    public static final int CODE_NONE = 0;
    public static final int CODE_COUNTING = 1;
    public static final int CODE_COUNT_FINISH = 2;

    private TwoAuthLoginContract.View mTaskView;
    private long mVerifyCodeLimitTime = MAX_VERIFY_CODE_LIMIT_TIME;
    private Subscription mVerifyCodeCounter = null;
    private Subscription mSendCodeTask = null;
    private boolean mIsRetryLoginTuya = false;
    private Subscription mRetryLoginTuyaTask = null;
    private static final int RETRY_LOGIN_TUYA_TIME_LEN = 3 * 1000;

    public TwoAuthLoginPresenter(TwoAuthLoginContract.View view) {
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
    public void sendTwoAuthCode(String account, String countryCode) {
        stopSendTwoAuthCodeTask();
        mSendCodeTask = UserApi.getInstance().sendTwoAuthCode(account, countryCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onSendTwoAuthCodeResult(SDKConstant.ERROR);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mTaskView != null) {
                            mTaskView.onSendTwoAuthCodeResult(SDKConstant.SUCCESS);
                        } else if (mTaskView != null) {
                            mTaskView.onSendTwoAuthCodeResult(SDKConstant.ERROR);
                        }
                    }
                });
    }

    @Override
    public void stopSendTwoAuthCodeTask() {
        if (mSendCodeTask != null && !mSendCodeTask.isUnsubscribed()) {
            mSendCodeTask.unsubscribe();
            mSendCodeTask = null;
        }
    }

    @Override
    public void checkAndLogin(String account, String password, String country, String code) {
        UserApi.getInstance().twoAuthLogin(account, country, code, password, MyAccountHelper.getInstance().createReportUserRequest())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<LoginResult>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onCheckAndLoginResult(SDKConstant.ERROR, null);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<LoginResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mTaskView != null) {
                            NooieEncryptService service = NooieEncryptService.getInstance();
                            String uid = service.getTuyaPsd(response.getData().getUid());
                            String country = response.getData() != null && !TextUtils.isEmpty(response.getData().getRegister_country()) ? response.getData().getRegister_country() : CountryUtil.getCurrentCountry(NooieApplication.mCtx);
                            mIsRetryLoginTuya = true;
                            loginTuyaAccount(country, response.getData().getUid(), uid, response);
                        } else {
                            mTaskView.onCheckAndLoginResult(SDKConstant.ERROR, null);
                        }
                    }
                });
    }

    private void loginTuyaAccount(String countryCode, final String uid, String passwd, BaseResponse<LoginResult> response) {
        NooieLog.d("-->> TwoAuthLoginImpl loginTuyaAccount 1001");
        TuyaHomeSdk.getUserInstance().loginWithUid(countryCode, uid, passwd, new ILoginCallback() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    setupTuyaPush();
                    if (mTaskView != null) {
                        mTaskView.onCheckAndLoginResult(SDKConstant.SUCCESS, response);
                    }
                }
            }

            @Override
            public void onError(String s, String s1) {
                NooieLog.d("-->> TwoAuthLoginImpl loginTuyaAccount 1002 s=" + s + " s1=" + s1);
                if (mIsRetryLoginTuya) {
                    mIsRetryLoginTuya = false;
                    startRetryLoginTuyaTask(countryCode, uid, passwd, response);
                    return;
                }
                if (mTaskView != null) {
                    mTaskView.onCheckAndLoginResult(SDKConstant.ERROR, null);
                }
            }
        });
    }

    private void startRetryLoginTuyaTask(String countryCode, final String uid, String passwd, BaseResponse<LoginResult> response) {
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
                        loginTuyaAccount(countryCode, uid, passwd, response);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        NooieLog.d("-->> SignInPresenterImpl startRetryLoginTuyaTask 1003");
                        loginTuyaAccount(countryCode, uid, passwd, response);
                    }
                });
    }

    private void stopRetryLoginTuyaTask() {
        if (mRetryLoginTuyaTask != null && !mRetryLoginTuyaTask.isUnsubscribed()) {
            mRetryLoginTuyaTask.unsubscribe();
            mRetryLoginTuyaTask = null;
        }
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

    @Override
    public void startVerifyCodeCounter() {
        stopVerifyCodeCounter();
        mVerifyCodeLimitTime = MAX_VERIFY_CODE_LIMIT_TIME;
        mVerifyCodeCounter = Observable.interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onCodeCounterChange(CODE_NONE, null);
                        }
                    }

                    @Override
                    public void onNext(Long total) {
                        NooieLog.d("-->> SignInPresenterImpl startVerifyCodeCounter onNext total=" + total);
                        if (mVerifyCodeLimitTime > 0) {
                            mVerifyCodeLimitTime--;
                            if (mTaskView != null) {
                                mTaskView.onCodeCounterChange(CODE_COUNTING, String.valueOf(mVerifyCodeLimitTime));
                            }
                        } else {
                            stopVerifyCodeCounter();
                            if (mTaskView != null) {
                                mTaskView.onCodeCounterChange(CODE_COUNT_FINISH, null);
                            }
                        }
                    }
                });
    }

    @Override
    public void stopVerifyCodeCounter() {
        if (mVerifyCodeCounter != null && !mVerifyCodeCounter.isUnsubscribed()) {
            mVerifyCodeCounter.unsubscribe();
        }
        mVerifyCodeLimitTime = 0;
    }
}
