package com.afar.osaio.smart.electrician.presenter;


import com.afar.osaio.base.mvp.IBasePresenter;

/**
 * IAddGuestPresenter
 *
 * @author Administrator
 * @date 2019/3/13
 */
public interface IAddGuestPresenter extends IBasePresenter {

    void getUidByAccount(String account);

    void loadHomeGuest(long homeId);
}

