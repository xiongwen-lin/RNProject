package com.afar.osaio.smart.electrician.smartScene.view;

import com.afar.osaio.base.mvp.IBaseView;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;

public interface ISortSceneView extends IBaseView {

    void notifyDeleteSceneSuccess(SceneBean sceneBean);

    void notifyDeleteSceneFailed(String errorCode, String errorMsg);

    void notifySortSceneListSuccess();

    void notifySortSceneListFailed(String errorCode, String errorMsg);
}
