package com.afar.osaio.smart.electrician.smartScene.presenter;

import com.afar.osaio.smart.electrician.smartScene.view.ISortSceneView;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;
import com.tuya.smart.sdk.api.IResultCallback;

import java.util.List;

public class SortPresenter implements ISortPresenter {

    private ISortSceneView mView;

    public SortPresenter(ISortSceneView view) {
        this.mView = view;
    }

    @Override
    public void deleteScene(final SceneBean sceneBean) {
        if (mView != null) {
            mView.showLoadingDialog();
        }

        TuyaHomeSdk.newSceneInstance(sceneBean.getId()).deleteScene(new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    NooieLog.e("deleteScene errorCode " + code + "  errorMsg  " + error);
                    mView.notifyDeleteSceneFailed(code, error);
                }
            }

            @Override
            public void onSuccess() {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyDeleteSceneSuccess(sceneBean);
                }
            }
        });
    }

    @Override
    public void sortSceneList(long homeId, List<String> sceneIds) {
        if (mView != null) {
            mView.showLoadingDialog();
        }

        TuyaHomeSdk.getSceneManagerInstance().sortSceneList(homeId, sceneIds, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    NooieLog.e("sortSceneList errorCode " + code + "  errorMsg  " + error);
                    mView.notifySortSceneListFailed(code, error);
                }
            }

            @Override
            public void onSuccess() {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifySortSceneListSuccess();
                }
            }
        });
    }
}
