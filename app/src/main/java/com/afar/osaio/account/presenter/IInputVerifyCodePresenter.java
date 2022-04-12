package com.afar.osaio.account.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

/**
 * Created by victor on 2018/6/28
 * Email is victor.qiao.0604@gmail.com
 */
public interface IInputVerifyCodePresenter extends IBasePresenter {
    void sendVerifyCode(String account, String country, int type);

    void checkVerifyCode(String account, String code, String country, int type);

    void startVerifyCodeCounter();

    void stopVerifyCodeCounter();
}
