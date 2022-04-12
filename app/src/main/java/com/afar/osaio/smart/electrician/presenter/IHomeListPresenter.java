package com.afar.osaio.smart.electrician.presenter;

/**
 * IHomeListPresenter
 *
 * @author Administrator
 * @date 2020/2/1
 */
public interface IHomeListPresenter {

    void loadHomes();

    void changeCurrentHome(long homeId);
}

