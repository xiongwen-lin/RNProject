package com.afar.osaio.smart.electrician.smartScene.presenter;

import com.tuya.smart.home.sdk.bean.scene.PreCondition;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;
import com.tuya.smart.home.sdk.bean.scene.SceneCondition;
import com.tuya.smart.home.sdk.bean.scene.SceneTask;

import java.util.List;

public interface ICreateNewSmartPresenter {

    void modifyScene(String sceneId,SceneBean sceneReqBean);

    void deleteScene(String sceneId);

    void createScene(long homeId, String name, String background, List<SceneCondition> conditions, List<SceneTask> tasks, int matchType);

    void createScene(long homeId, String name, String displayColor, List<SceneCondition> conditions, List<SceneTask> tasks, List<PreCondition> preConditions, int matchType);

    //根据设备 id 获取设备任务
    void getDevTask(SceneTask sceneTask,int position);

    //根据群组 id 获取可执行的动作
    void getGroupTask(SceneTask sceneTask,int position);

    //根据设备 id 获取设备条件列表
    void getDevCondition(SceneCondition sceneCondition,int position);

    void getWeatherCondition(SceneCondition sceneCondition,int position);

    //开启或关闭自动化场景
    void enableScene(String sceneId);

    void disableScene(String sceneId);
}
