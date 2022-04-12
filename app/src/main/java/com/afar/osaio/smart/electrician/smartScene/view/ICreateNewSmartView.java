package com.afar.osaio.smart.electrician.smartScene.view;

import com.afar.osaio.base.mvp.IBaseView;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;
import com.tuya.smart.home.sdk.bean.scene.SceneCondition;
import com.tuya.smart.home.sdk.bean.scene.SceneTask;
import com.tuya.smart.home.sdk.bean.scene.condition.ConditionListBean;
import com.tuya.smart.home.sdk.bean.scene.dev.TaskListBean;

import java.util.List;

public interface ICreateNewSmartView extends IBaseView {
    void notifyCreateSceneSuccess(SceneBean sceneBean);

    void notifyCreateSceneFailed(String errorCode, String errorMsg);

    void notifyDeleteSceneSuccess();

    void notifyDeleteSceneFailed(String errorCode, String errorMsg);

    void notifyModifySceneSuccess(SceneBean sceneBean);

    void notifyModifySceneFailed(String errorCode, String errorMsg);

    void notifyGetDeviceTaskListSuccess(List<TaskListBean> result, SceneTask sceneTask,int position);

    void notifyGetDeviceTaskListSuccess(List<TaskListBean> result,SceneCondition sceneCondition,int position);

    void notifyGetDeviceTaskListFail(String errorMsg);

    void notifyGetConditionListSuccess(List<ConditionListBean> conditionList, SceneCondition sceneCondition,int position);

    void notifyGetConditionListFail(String errorMsg);

    void notifyEnableScene();
}
