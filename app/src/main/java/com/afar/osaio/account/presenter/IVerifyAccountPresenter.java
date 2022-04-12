package com.afar.osaio.account.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

/**
 * Created by victor on 2018/6/28
 * Email is victor.qiao.0604@gmail.com
 */
public interface IVerifyAccountPresenter extends IBasePresenter {

    void sendVerifyCode(String account, String country, int type);

    void getCurrentCountryCode(String account);

    void checkAccountSourceForRegister(String account);
}
