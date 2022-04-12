package com.afar.osaio.smart.electrician.smartScene.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

public interface IRunDevicePresenter extends IBasePresenter {

    //获取执行动作中包含群组设备的设备列表
    void getDevAndGroupTask();
}
