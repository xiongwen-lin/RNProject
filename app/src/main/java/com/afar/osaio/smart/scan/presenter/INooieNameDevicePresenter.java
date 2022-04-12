package com.afar.osaio.smart.scan.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

/**
 * INooieNameDevicePresenter
 *
 * @author Administrator
 * @date 2019/4/22
 */
public interface INooieNameDevicePresenter extends IBasePresenter {

    void destroy();

    void renameDevice(int connectionMode, String user, String deviceId, String name);

    void updateDeviceName(String user, String deviceId, String name);

}
