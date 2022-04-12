package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

/**
 * ICreateGroupPresenter
 *
 * @author Administrator
 * @date 2019/3/20
 */
public interface ICreateGroupPresenter extends IBasePresenter {

    void loadDevices(String productId);
}
