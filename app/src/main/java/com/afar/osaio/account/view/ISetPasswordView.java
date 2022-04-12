package com.afar.osaio.account.view;

import com.afar.osaio.base.mvp.IBaseView;

/**
 * Created by victor on 2018/6/28
 * Email is victor.qiao.0604@gmail.com
 */
public interface ISetPasswordView extends IBaseView {

    void showLoadingDialog();

    void hideLoadingDialog();

    void notifySignUpResult(String result);

    void notifyResetPsdResult(String result);
}
