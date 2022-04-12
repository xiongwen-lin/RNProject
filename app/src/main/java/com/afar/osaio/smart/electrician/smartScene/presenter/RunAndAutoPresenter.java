package com.afar.osaio.smart.electrician.smartScene.presenter;

import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.smartScene.view.IRunAndAutoView;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;
import com.tuya.smart.sdk.api.IResultCallback;

import java.util.List;

public class RunAndAutoPresenter implements IRunAndAutoPresenter {

    private IRunAndAutoView mView;

    public RunAndAutoPresenter(IRunAndAutoView view) {
        this.mView = view;
    }

    @Override
    public void getSceneList() {
        if (mView != null) {
            mView.showLoadingDialog();
        }
        TuyaHomeSdk.getSceneManagerInstance().getSceneList(FamilyManager.getInstance().getCurrentHomeId(), new ITuyaResultCallback<List<SceneBean>>() {
            @Override
            public void onSuccess(List<SceneBean> result) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyGetSceneListSuccess(result);
                }
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyGetSceneListFail(errorMessage);
                    NooieLog.e("getSceneList error " + errorCode + "  errorMsg  " + errorMessage);
                }
            }
        });
    }

    @Override
    public void executeScene(String sceneId) {
        if (mView != null) {
            mView.showLoadingDialog();
        }
        TuyaHomeSdk.newSceneInstance(sceneId).executeScene(new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyExecuteSceneFail(error);
                }
                NooieLog.e("executeScene error " + code + "  errorMsg  " + error);
            }

            @Override
            public void onSuccess() {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyExecuteSceneSuccess();
                }
            }
        });
    }

    @Override
    public void enableScene(final String sceneId) {
        TuyaHomeSdk.newSceneInstance(sceneId).enableScene(sceneId, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                NooieLog.e("executeScene error " + code + "  errorMsg  " + error);
            }

            @Override
            public void onSuccess() {
                NooieLog.e("----------sceneID " + sceneId + " enableScene Success");
            }
        });
    }

    @Override
    public void disableScene(final String sceneId) {
        TuyaHomeSdk.newSceneInstance(sceneId).disableScene(sceneId, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                NooieLog.e("executeScene error " + code + "  errorMsg  " + error);
            }

            @Override
            public void onSuccess() {
                NooieLog.e("----------sceneID " + sceneId + " disableScene Success");
            }
        });
    }
}
