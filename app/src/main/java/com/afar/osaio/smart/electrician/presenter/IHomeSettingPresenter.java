package com.afar.osaio.smart.electrician.presenter;

import com.tuya.smart.home.sdk.bean.HomeBean;

/**
 * IHomeSettingPresenter
 *
 * @author Administrator
 * @date 2019/3/14
 */
public interface IHomeSettingPresenter {

    void updateHome(HomeBean homeBean, String homeName);

    void removeHome(long homeId);

    void refreshHome();

}
