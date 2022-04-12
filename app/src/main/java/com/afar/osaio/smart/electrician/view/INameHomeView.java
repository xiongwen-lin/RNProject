package com.afar.osaio.smart.electrician.view;

import com.afar.osaio.base.mvp.IBaseView;
import com.tuya.smart.home.sdk.bean.HomeBean;

/**
 * INameHomeView
 *
 * @author Administrator
 * @date 2019/3/14
 */
public interface INameHomeView extends IBaseView {

    void notifyUpdateHomeState(String msg);

    void notifyCreateHomeSuccess(HomeBean homeBean);

    void notifyCreateHomeFailed(String msg);

}
