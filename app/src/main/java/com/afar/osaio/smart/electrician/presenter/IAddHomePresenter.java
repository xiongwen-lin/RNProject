package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

import java.util.List;

/**
 * INameHomePresenter
 *
 * @author Administrator
 * @date 2019/3/14
 */
public interface IAddHomePresenter extends IBasePresenter {

    void createHome(String homeName, List<String> roomList);

}
