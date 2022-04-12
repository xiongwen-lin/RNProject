package com.afar.osaio.account.view;

import com.afar.osaio.base.mvp.IBaseView;

/**
 * Created by victor on 2018/6/28
 * Email is victor.qiao.0604@gmail.com
 */
public interface IAccountView extends IBaseView {
    void sendVerifyCodeResult(String result, int code);

    void notifyCurrentCountryCode(String code);

    void onCheckAccountSourceForRegister(String account);

    void onCheckAccountSource(int state, boolean isOtherBrand, String brand);
}
