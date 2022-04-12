package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;
import com.tuya.smart.home.sdk.bean.HomeBean;

import java.util.List;

/**
 * INameHomePresenter
 *
 * @author Administrator
 * @date 2019/3/14
 */
public interface INameHomePresenter extends IBasePresenter {

    void updateHome(HomeBean homeBean, String homeName);

    void createHome(String homeName, List<String> roomList);

}
