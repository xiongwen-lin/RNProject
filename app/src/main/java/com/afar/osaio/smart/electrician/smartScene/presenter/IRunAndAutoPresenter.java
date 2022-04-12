package com.afar.osaio.smart.electrician.smartScene.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

public interface IRunAndAutoPresenter extends IBasePresenter {
    //获取场景列表数据
    void getSceneList();

    //执行场景
    void executeScene(String sceneId);

    //开启或关闭自动化场景
    void enableScene(String sceneId);

    void disableScene(String sceneId);
}
