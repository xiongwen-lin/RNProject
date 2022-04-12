package com.afar.osaio.smart.electrician.presenter;


import com.afar.osaio.base.mvp.IBasePresenter;

/**
 * IRenameDevicePresenter
 *
 * @author Administrator
 * @date 2019/3/18
 */
public interface IRenameDevicePresenter extends IBasePresenter {

    void renameDevice(String deviceId, String name);
}
