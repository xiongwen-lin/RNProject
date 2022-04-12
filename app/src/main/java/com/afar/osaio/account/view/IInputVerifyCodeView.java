package com.afar.osaio.account.view;

import com.afar.osaio.base.view.IBaseActivityView;

/**
 * Created by victor on 2018/6/28
 * Email is victor.qiao.0604@gmail.com
 */
public interface IInputVerifyCodeView extends IBaseActivityView {

    void sendVerifyCodeResult(String result);

    void notifyVerifyCodeLimitTime(String result);

    void notifyCheckVerifyCodeResult(String result, int code);
}
