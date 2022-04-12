package com.afar.osaio.smart.electrician.smartScene.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

public interface ISelectFunctionPresenter extends IBasePresenter {

    //根据设备 id 获取设备任务
    void getDevTask(String devId);

    //根据群组 id 获取可执行的动作
    void getGroupTask(String goupId);

    //根据设备 id 获取设备条件列表
    void getDevCondition(String devId);
}
