package com.afar.osaio.account.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

/**
 * Created by victor on 2018/6/28
 * Email is victor.qiao.0604@gmail.com
 */
public interface ISetPasswordPresenter extends IBasePresenter {
    void manageAccount(String account, String psd, String verifyCode, String country, int verifyType);

    void resetPassword(String account, String psd, String code, String country);
}
