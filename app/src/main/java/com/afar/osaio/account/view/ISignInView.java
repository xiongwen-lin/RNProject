package com.afar.osaio.account.view;

import com.afar.osaio.base.mvp.IBaseView;
import com.nooie.sdk.db.entity.UserInfoEntity;

import java.util.List;

/**
 * Created by victor on 2018/6/26
 * Email is victor.qiao.0604@gmail.com
 */
public interface ISignInView extends IBaseView {
    void notifySignInResult(String msg, int code);

    void showLoadingDialog();

    void hideLoadingDialog();

    void onLoadAccountHistorySuccess(List<UserInfoEntity> result);

    void notifySendRegisterVerifyCode(int sendCodeType, String result, int code, String account);

    void notifyRegisterVerifyCodeLimitTime(String result);

    void notifyCheckVerifyCodeResult(String result, int code);

    void onCheckAccountSourceForSignIn(String account, String password);

    void onCheckAccountSourceForRegister(String account);

    void onCheckAccountSource(int state, boolean isOtherBrand, String brand, boolean isSignIn);

}
