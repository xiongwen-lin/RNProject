package com.afar.osaio.account.presenter;

import com.afar.osaio.account.view.ISplashView;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.brain.model.BrainModel;
import com.afar.osaio.smart.brain.model.IBrainModel;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.db.dao.UserInfoService;
import com.nooie.sdk.db.entity.UserInfoEntity;
import com.nooie.sdk.processor.user.UserApi;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by victor on 2018/8/30
 * Email is victor.qiao.0604@gmail.com
 */
public class SplashPresenterImpl implements ISplashPresenter {

    public static final String ASSETS_ENCRYPT_FOLDER_NAME = "encrypt";
    private IBrainModel mBrainModel;
    private ISplashView mSplashView;

    private int mSignInCount = 2;

    public SplashPresenterImpl(ISplashView splashView) {
        this.mSplashView = splashView;
        mBrainModel = new BrainModel();
    }

    public void destroy() {
        mBrainModel = null;
        mSplashView = null;
    }

    @Override
    public void initGlobalData() {
        UserApi.getInstance().getInitGlobalDataObservable(NooieApplication.mCtx, ASSETS_ENCRYPT_FOLDER_NAME)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mSplashView != null) {
                            mSplashView.onInitGlobalDataResult(ConstantValue.ERROR, false);
                        }
                    }

                    @Override
                    public void onNext(Boolean result) {
                        if (mSplashView != null) {
                            mSplashView.onInitGlobalDataResult(ConstantValue.SUCCESS, result);
                        }
                    }
                });
    }

    @Override
    public void autoLogin(boolean isRetryLogin) {
        UserApi.getInstance().autoLogin(isRetryLogin)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        NooieLog.d("-->> SplashPresenterImpl autoLogin error");
                        if (mSplashView != null) {
                            mSplashView.hideLoadingDialog();
                            mSplashView.notifySignInResult(mSignInCount == 0 ? ConstantValue.ERROR : "");
                        }
                    }

                    @Override
                    public void onNext(Boolean result) {
                        NooieLog.d("-->> SplashPresenterImpl autoLogin success result=" + result);
                        mSplashView.hideLoadingDialog();
                        mSplashView.notifySignInResult(ConstantValue.SUCCESS);
                    }
                });
    }

    @Override
    public void checkAppIsStarted() {
        Observable.just(1)
                .flatMap(new Func1<Integer, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Integer integer) {
                        List<UserInfoEntity> userInfoEntities = UserInfoService.getInstance().getAllUserInfo();
                        boolean isAppStarted = GlobalPrefs.getAppIsStarted() || CollectionUtil.isNotEmpty(userInfoEntities);
                        if (isAppStarted) {
                            return Observable.just(true).delay(1000, TimeUnit.MILLISECONDS);
                        }
                        return Observable.just(false).delay(500, TimeUnit.MILLISECONDS);
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
                        if (mSplashView != null) {
                            mSplashView.onCheckAppIsStarted(SDKConstant.SUCCESS, true);
                        }
                    }

                    @Override
                    public void onNext(Boolean isStarted) {
                        if (mSplashView != null) {
                            mSplashView.onCheckAppIsStarted(SDKConstant.SUCCESS, isStarted);
                        }
                    }
                });
    }
}
