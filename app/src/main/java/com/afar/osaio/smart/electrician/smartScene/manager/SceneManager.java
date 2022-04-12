package com.afar.osaio.smart.electrician.smartScene.manager;

import com.tuya.smart.home.sdk.bean.scene.SceneBean;

import java.util.ArrayList;
import java.util.List;

public class SceneManager {

    List<SceneBean> launchList = new ArrayList<>();
    List<SceneBean> autoList = new ArrayList<>();

    private volatile static SceneManager instance;

    private SceneManager() {
    }

    public static SceneManager getInstance() {
        if (instance == null) {
            synchronized (SceneManager.class) {
                if (instance == null) {
                    instance = new SceneManager();
                }
            }
        }
        return instance;
    }

    public void syncSceneList(List<SceneBean> sceneBeanList) {
        launchList.clear();
        autoList.clear();

        for (SceneBean sceneBean : sceneBeanList) {
            if (sceneBean != null && sceneBean.getConditions() != null) {
                autoList.add(sceneBean);
            }
        }
        if (mAutoSceneCallBack != null) {
            mAutoSceneCallBack.callAutoBackSceneBean(autoList);
        }

        for (SceneBean sceneBean : sceneBeanList) {
            if (sceneBean != null && sceneBean.getConditions() == null) {
                launchList.add(sceneBean);
            }
        }
        if (mLaunchSceneCallBack != null) {
            mLaunchSceneCallBack.callLaunchBackSceneBean(launchList);
        }
    }

    public ILaunchSceneBeanCallBack mLaunchSceneCallBack;

    public void setLaunchSceneBeanCallback(ILaunchSceneBeanCallBack launchSceneCallBack) {
        this.mLaunchSceneCallBack = launchSceneCallBack;
    }

    public interface ILaunchSceneBeanCallBack {
        void callLaunchBackSceneBean(List<SceneBean> sceneBeanList);
    }

    public IAutoSceneBeanCallBack mAutoSceneCallBack;

    public void setAutoSceneBeanCallback(IAutoSceneBeanCallBack autoSceneCallBack) {
        this.mAutoSceneCallBack = autoSceneCallBack;
    }

    public interface IAutoSceneBeanCallBack {
        void callAutoBackSceneBean(List<SceneBean> sceneBeanList);
    }

}
