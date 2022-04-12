package com.afar.osaio.account.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

/**
 * Created by victor on 2018/7/10
 * Email is victor.qiao.0604@gmail.com
 */
public interface IChangedPasswordPresenter extends IBasePresenter {
    void changePassword(String account, String oldPsd, String newPsd);
}
