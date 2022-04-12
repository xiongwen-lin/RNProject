package com.afar.osaio.account.presenter;

import com.afar.osaio.account.view.IInputVerifyCodeView;
import com.afar.osaio.smart.brain.model.BrainModel;
import com.afar.osaio.smart.brain.model.IBrainModel;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.account.AccountService;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.brain.BrainUrlResult;
import com.nooie.sdk.processor.user.UserApi;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by victor on 2018/6/28
 * Email is victor.qiao.0604@gmail.com
 */
public class InputVerifyCodePresenterImpl implements IInputVerifyCodePresenter {

    private IInputVerifyCodeView mInputVerifyCodeView;
    private IBrainModel mBrainModel;

    private static final long MAX_VERIFY_CODE_LIMIT_TIME = 180L;
    private long mVerifyCodeLimitTime = MAX_VERIFY_CODE_LIMIT_TIME;
    private Subscription mVerifyCodeCounter = null;

    public InputVerifyCodePresenterImpl(IInputVerifyCodeView view) {
        this.mInputVerifyCodeView = view;
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
        /*
        mBrainModel.getBrainTime()
                .flatMap(new Func1<BaseResponse<BrainTimeResult>, Observable<BaseResponse<BrainUrlResult>>>() {
                    @Override
                    public Observable<BaseResponse<BrainUrlResult>> call(BaseResponse<BrainTimeResult> response) {
                        NooieLog.d("-->> SignInPresenterImpl getBrainTime");
                        int currentTime =  (int)(System.currentTimeMillis()/1000L);
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            int netWorkTime = response.getData().getTime();
                            int gapTime = netWorkTime - currentTime;
                            GlobalPrefs.getPreferences(NooieApplication.mCtx).setGapTime(gapTime);
                            NooieLog.d("-->> SignInPresenterImpl getBrainTime netWorkTime=" + netWorkTime + " currentTime=" + currentTime + " gap=" + gapTime);
                        }
                        return type == ConstantValue.USER_REGISTER_VERIFY ? mBrainModel.getBrainUrlByCountry(country) : mBrainModel.getBrainUrlByAccountOrCountry(account, country);
                    }
                })
                .flatMap(new Func1<BaseResponse<BrainUrlResult>, Observable<BaseResponse>>() {
                    @Override
                    public Observable<BaseResponse> call(BaseResponse<BrainUrlResult> response) {
                        boolean isObtainBrainSuccess = response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null && !TextUtils.isEmpty(response.getData().getWeb()) && !TextUtils.isEmpty(response.getData().getS3()) && !TextUtils.isEmpty(response.getData().getP2p());
                        if (!isObtainBrainSuccess) {
                            return Observable.error(new Throwable(""));
                        }

                        UserRegionService.getInstance().addUserRegion(account, country);
                        GlobalPrefs prefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
                        prefs.saveBrain(response.getData().getWeb(), response.getData().getP2p(), response.getData().getS3(), response.getData().getRegion(), response.getData().getSs());
                        return type == ConstantValue.USER_REGISTER_VERIFY ? com.nooie.sdk.api.network.account.AccountService.getService().sendRegisterVerifyCode(account, country) : com.nooie.sdk.api.network.account.AccountService.getService().sendForgetVerifyCode(account, country);
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
                        if (mInputVerifyCodeView != null) {
                            mInputVerifyCodeView.sendVerifyCodeResult("");
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mInputVerifyCodeView != null) {
                            mVerifyCodeLimitTime = MAX_VERIFY_CODE_LIMIT_TIME;
                            startVerifyCodeCounter();
                            mInputVerifyCodeView.sendVerifyCodeResult(ConstantValue.SUCCESS);
                        } else if (mInputVerifyCodeView != null) {
                            mInputVerifyCodeView.sendVerifyCodeResult("");
                        }
                    }
                });
         */
    }

    @Override
    public void checkVerifyCode(final String account, final String code, final String country, final int type) {
        checkVerifyCodeBySDK(account, country, code, type);
        /*
        mBrainModel.getBrainTime()
                .flatMap(new Func1<BaseResponse<BrainTimeResult>, Observable<BaseResponse<BrainUrlResult>>>() {
                    @Override
                    public Observable<BaseResponse<BrainUrlResult>> call(BaseResponse<BrainTimeResult> response) {
                        NooieLog.d("-->> SignInPresenterImpl getBrainTime");
                        int currentTime =  (int)(System.currentTimeMillis()/1000L);
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            int netWorkTime = response.getData().getTime();
                            int gapTime = netWorkTime - currentTime;
                            GlobalPrefs.getPreferences(NooieApplication.mCtx).setGapTime(gapTime);
                            NooieLog.d("-->> SignInPresenterImpl getBrainTime netWorkTime=" + netWorkTime + " currentTime=" + currentTime + " gap=" + gapTime);
                        }
                        return type == ConstantValue.USER_REGISTER_VERIFY ? mBrainModel.getBrainUrlByCountry(country) : mBrainModel.getBrainUrlByAccountOrCountry(account, country);
                    }
                })
                .flatMap(new Func1<BaseResponse<BrainUrlResult>, Observable<BaseResponse>>() {
                    @Override
                    public Observable<BaseResponse> call(BaseResponse<BrainUrlResult> response) {
                        boolean isObtainBrainSuccess = response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null && !TextUtils.isEmpty(response.getData().getWeb()) && !TextUtils.isEmpty(response.getData().getS3()) && !TextUtils.isEmpty(response.getData().getP2p());
                        if (!isObtainBrainSuccess) {
                            return Observable.error(new Throwable(""));
                        }

                        GlobalPrefs prefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
                        prefs.saveBrain(response.getData().getWeb(), response.getData().getP2p(), response.getData().getS3(), response.getData().getRegion(), response.getData().getSs());
                        return type == ConstantValue.USER_REGISTER_VERIFY ? AccountService.getService().checkRegisterVerifyCode(account, code) : AccountService.getService().checkForgetVerifyCode(account, code);
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
                        if (mInputVerifyCodeView != null) {
                            mInputVerifyCodeView.notifyCheckVerifyCodeResult(ConstantValue.ERROR, StateCode.UNKNOWN.code);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mInputVerifyCodeView != null) {
                            mInputVerifyCodeView.notifyCheckVerifyCodeResult(ConstantValue.SUCCESS, response.getCode());
                        } else if (mInputVerifyCodeView != null) {
                            mInputVerifyCodeView.notifyCheckVerifyCodeResult(ConstantValue.ERROR, response != null ? response.getCode() : StateCode.UNKNOWN.code);
                        }
                    }
                });
         */
    }

    @Override
    public void startVerifyCodeCounter() {
        stopVerifyCodeCounter();
        mVerifyCodeCounter = Observable.interval(1, TimeUnit.SECONDS)
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
                            if (mInputVerifyCodeView != null) {
                                mInputVerifyCodeView.notifyVerifyCodeLimitTime(String.valueOf(mVerifyCodeLimitTime));
                            }
                        } else {
                            stopVerifyCodeCounter();
                            if (mInputVerifyCodeView != null) {
                                mInputVerifyCodeView.notifyVerifyCodeLimitTime(ConstantValue.SUCCESS);
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
    }

    /**
     * 获取验证码
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
                        if (mInputVerifyCodeView != null) {
                            mInputVerifyCodeView.sendVerifyCodeResult("");
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mInputVerifyCodeView != null) {
                            mVerifyCodeLimitTime = MAX_VERIFY_CODE_LIMIT_TIME;
                            startVerifyCodeCounter();
                            mInputVerifyCodeView.sendVerifyCodeResult(ConstantValue.SUCCESS);
                        } else if (mInputVerifyCodeView != null) {
                            mInputVerifyCodeView.sendVerifyCodeResult("");
                        }
                    }
                });
    }

    /**
     * 检查验证码
     * @param account
     * @param country
     * @param code
     * @param type 1，注册获取验证码  2，忘记密码获取验证码
     */
    private void checkVerifyCodeBySDK(final String account, final String country, String code, final int type) {
        boolean isRegistered = type != ConstantValue.USER_REGISTER_VERIFY;
        boolean isSavedCountryCode = type == ConstantValue.USER_REGISTER_VERIFY;
        UserApi.getInstance().getBrainUrlObservable(account, country, isRegistered, isSavedCountryCode)
                .flatMap(new Func1<BaseResponse<BrainUrlResult>, Observable<BaseResponse>>() {
                    @Override
                    public Observable<BaseResponse> call(BaseResponse<BrainUrlResult> response) {
                        if (response == null) {
                            return Observable.error(new Throwable(""));
                        }
                        return type == ConstantValue.USER_REGISTER_VERIFY ? AccountService.getService().checkRegisterVerifyCode(account, code) : AccountService.getService().checkForgetVerifyCode(account, code);
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
                        if (mInputVerifyCodeView != null) {
                            mInputVerifyCodeView.notifyCheckVerifyCodeResult(ConstantValue.ERROR, StateCode.UNKNOWN.code);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mInputVerifyCodeView != null) {
                            mInputVerifyCodeView.notifyCheckVerifyCodeResult(ConstantValue.SUCCESS, response.getCode());
                        } else if (mInputVerifyCodeView != null) {
                            mInputVerifyCodeView.notifyCheckVerifyCodeResult(ConstantValue.ERROR, response != null ? response.getCode() : StateCode.UNKNOWN.code);
                        }
                    }
                });
    }
}

