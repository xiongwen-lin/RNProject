package com.afar.osaio.account.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

/**
 * Created by victor on 2018/6/26
 * Email is victor.qiao.0604@gmail.com
 */
public interface ISignInPresenter extends IBasePresenter {

    void destroy();

    void signIn(String account, String psd);

    void loadAccountHistory();

    void removeAccountFromHistory(String account);

    void sendRegisterVerifyCode(int sendCodeType, String account, String country);

    void checkRegisterVerifyCode(String account, String code, String country);

    void stopVerifyCodeCounter();

    void checkAccountSourceForSignIn(String account, String password);

    void checkAccountSourceForRegister(String account);

}
