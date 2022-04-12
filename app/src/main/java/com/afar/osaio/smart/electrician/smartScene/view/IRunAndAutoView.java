package com.afar.osaio.smart.electrician.smartScene.view;

import com.afar.osaio.base.mvp.IBaseView;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;

import java.util.List;

public interface IRunAndAutoView extends IBaseView {

    void notifyGetSceneListSuccess(List<SceneBean> sceneBeanList);

    void notifyGetSceneListFail(String errorMsg);

    void notifyExecuteSceneSuccess();

    void notifyExecuteSceneFail(String errorMsg);
}
