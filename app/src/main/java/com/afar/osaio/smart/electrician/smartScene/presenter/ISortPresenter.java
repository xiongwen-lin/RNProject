package com.afar.osaio.smart.electrician.smartScene.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;

import java.util.List;

public interface ISortPresenter extends IBasePresenter {
    //用于删除场景
    void deleteScene(SceneBean sceneBean);

    //场景排序
    void sortSceneList(long homeId, List<String> sceneIds);
}
