package com.afar.osaio.account.presenter;

import androidx.annotation.NonNull;

import android.text.TextUtils;

import com.afar.osaio.account.helper.MyAccountHelper;
import com.afar.osaio.account.view.ISetPasswordView;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.brain.model.BrainModel;
import com.afar.osaio.smart.brain.model.IBrainModel;
import com.nooie.common.base.GlobalData;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.push.helper.NooiePushMsgHelper;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.RegisterResult;
import com.nooie.sdk.encrypt.NooieEncryptService;
import com.nooie.sdk.processor.user.UserApi;
import com.tuya.smart.android.user.api.ILoginCallback;
import com.tuya.smart.android.user.bean.User;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.enums.TempUnitEnum;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by victor on 2018/6/28
 * Email is victor.qiao.0604@gmail.com
 */
public class SetPasswordPresenterImpl implements ISetPasswordPresenter {
    private ISetPasswordView mSetPasswordView;
    private IBrainModel mBrainModel;

    public SetPasswordPresenterImpl(@NonNull ISetPasswordView view) {
        this.mSetPasswordView = view;
        mBrainModel = new BrainModel();
    }

    @Override
    public void manageAccount(String account, String psd, String verifyCode, String country, int verifyType) {
        if (verifyType == ConstantValue.USER_REGISTER_VERIFY) {
            signUpBySDK(account, psd, country, verifyCode);
        } else {
            resetPassword(account, psd, verifyCode, country);
        }
    }

    @Override
    public void resetPassword(final String account, final String psd, final String code, final String country) {
        resetPasswordBySDK(account, psd, code, country);
    }

    /**
     * 注册接口
     *
     * @param account
     * @param password
     * @param countryCode 必须是用户选定的国家码
     * @param verifyCode
     */
    private void signUpBySDK(String account, String password, String countryCode, String verifyCode) {
        boolean isUseJPush = NooieDeviceHelper.isUseJPush(GlobalData.getInstance().getRegion());
        NooieApplication.get().setIsUseJPush(isUseJPush);
        UserApi.getInstance().register(account, password, countryCode, verifyCode, MyAccountHelper.getInstance().createReportUserRequest())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<RegisterResult>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        if (mSetPasswordView != null) {
                            mSetPasswordView.notifySignUpResult("");
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<RegisterResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mSetPasswordView != null) {
                            GlobalPrefs.setPrivacyIsReadByAccount(account, true);
                            NooieEncryptService service = NooieEncryptService.getInstance();
                            String uid = service.getTuyaPsd(response.getData().getUid());
                            String country = response.getData() != null && !TextUtils.isEmpty(response.getData().getRegister_country()) ? response.getData().getRegister_country() : countryCode;
                            loginTuyaAccount(country, response.getData().getUid(), uid);

                        } else if (mSetPasswordView != null) {
                            mSetPasswordView.notifySignUpResult("");
                        }
                    }
                });
    }

    private void resetPasswordBySDK(final String account, final String password, final String code, final String countryCode) {
        UserApi.getInstance().resetPassword(account, password, code, countryCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mSetPasswordView != null) {
                            mSetPasswordView.notifyResetPsdResult("");
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mSetPasswordView != null) {
                            mSetPasswordView.notifyResetPsdResult(ConstantValue.SUCCESS);
                        } else if (mSetPasswordView != null) {
                            mSetPasswordView.notifyResetPsdResult("");
                        }
                    }
                });
    }

    private void loginTuyaAccount(String countryCode, final String uid, String passwd) {
        TuyaHomeSdk.getUserInstance().loginWithUid(countryCode, uid, passwd, new ILoginCallback() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    setupTuyaPush();
                    setupTuyaTempUnit(countryCode);
                    GlobalPrefs globalPrefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
                    globalPrefs.setTuyaPhoto(TextUtils.isEmpty(user.getHeadPic()) ? "" : user.getHeadPic());
                    if (mSetPasswordView != null) {
                        mSetPasswordView.hideLoadingDialog();
                        mSetPasswordView.notifySignUpResult(ConstantValue.SUCCESS);
                    }
                }
            }

            @Override
            public void onError(String s, String s1) {
                if (mSetPasswordView != null) {
                    mSetPasswordView.hideLoadingDialog();
                    mSetPasswordView.notifySignUpResult(ConstantValue.ERROR);
                }
            }
        });
    }

    //USA、Bahamas、Cayman Islands、Liberia、Palau、Micronesia 、Marshall Island7个国家/地区默认为华氏度
    private  void setupTuyaTempUnit(String countryCode){
        TempUnitEnum type = TempUnitEnum.Celsius;
        if ("1".equals(countryCode)||"231".equals(countryCode)||"1242".equals(countryCode)||"1345".equals(countryCode)){
            type = TempUnitEnum.Fahrenheit;
        }
        NooieLog.d("-->> SetPasswordPresenterImpl setTempUnit()-----type="+type.getType()+",countryCode="+countryCode);
        TuyaHomeSdk.getUserInstance().setTempUnit(type, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                NooieLog.d("-->> SignInPresenterImpl setTempUnit code="+code+",error="+error);
            }

            @Override
            public void onSuccess() {
                NooieLog.d("-->> SignInPresenterImpl setTempUnit onSuccess");
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
}
