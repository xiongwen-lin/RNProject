package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

/**
 * IHomeManagerPresenter
 *
 * @author Administrator
 * @date 2019/3/13
 */
public interface IHomeManagerPresenter extends IBasePresenter {

    void getHomeDetail(long homeId);

    void loadHomeDevices(long homeId);

    void loadHomeMembers(long homeId);

    void loadHomes(boolean isUpdate);

    void resetHome();

    void changeCurrentHome(long homeId);

    void loadUserShareList(long homeId);
}
