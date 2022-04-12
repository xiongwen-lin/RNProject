package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

/**
 * IAddOwnerPresenter
 *
 * @author Administrator
 * @date 2019/3/13
 */
public interface IAddOwnerPresenter extends IBasePresenter {

    void addMember(long homeId, String countryCode, String userAccount, String name, boolean isAdmin);

    void getUidByAccount(String account);

}

