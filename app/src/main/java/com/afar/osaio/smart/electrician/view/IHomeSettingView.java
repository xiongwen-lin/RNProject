package com.afar.osaio.smart.electrician.view;


import com.afar.osaio.base.mvp.IBaseView;

/**
 * IHomeSettingView
 *
 * @author Administrator
 * @date 2019/3/14
 */
public interface IHomeSettingView extends IBaseView {

    void notifyUpdateHomeState(String msg);

    void notifyRemoveHomeState(String msg);

    void notifyRefreshHomeState(String msg);

}
