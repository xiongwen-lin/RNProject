package com.afar.osaio.smart.electrician.smartScene.presenter;

import android.text.TextUtils;

import com.afar.osaio.smart.electrician.smartScene.view.ICreateNewSmartView;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.scene.PreCondition;
import com.tuya.smart.home.sdk.bean.scene.SceneBean;
import com.tuya.smart.home.sdk.bean.scene.SceneCondition;
import com.tuya.smart.home.sdk.bean.scene.SceneTask;
import com.tuya.smart.home.sdk.bean.scene.condition.ConditionListBean;
import com.tuya.smart.home.sdk.bean.scene.dev.TaskListBean;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;
import com.tuya.smart.sdk.api.IResultCallback;

import java.util.List;

public class CreateNewSmartPresenter implements ICreateNewSmartPresenter {

    private ICreateNewSmartView mView;

    public CreateNewSmartPresenter(ICreateNewSmartView view) {
        this.mView = view;
    }

    @Override
    public void modifyScene(String sceneId, SceneBean sceneReqBean) {
        if (mView != null) {
            mView.showLoadingDialog();
        }
        TuyaHomeSdk.newSceneInstance(sceneId).modifyScene(sceneReqBean, new ITuyaResultCallback<SceneBean>() {
            @Override
            public void onSuccess(SceneBean result) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyModifySceneSuccess(result);
                }
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyModifySceneFailed(errorCode, errorMessage);
                }
            }
        });
    }

    @Override
    public void deleteScene(String sceneId) {
        if (mView != null) {
            mView.showLoadingDialog();
        }
        TuyaHomeSdk.newSceneInstance(sceneId).deleteScene(new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyDeleteSceneFailed(code, error);
                }
            }

            @Override
            public void onSuccess() {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyDeleteSceneSuccess();
                }
            }
        });
    }

    @Override
    public void createScene(long homeId, String name, String background, List<SceneCondition> conditions, List<SceneTask> tasks, int matchType) {
        if (mView != null) {
            mView.showLoadingDialog();
        }

        TuyaHomeSdk.getSceneManagerInstance().createScene(homeId, name, "", conditions, tasks, matchType, new ITuyaResultCallback<SceneBean>() {
            @Override
            public void onSuccess(SceneBean result) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyCreateSceneSuccess(result);
                }
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyCreateSceneFailed(errorCode, errorMessage);
                }
            }
        });
    }

    @Override
    public void createScene(long homeId, String name, String displayColor, List<SceneCondition> conditions, List<SceneTask> tasks, List<PreCondition> preConditions, int matchType) {
        if (mView != null) {
            mView.showLoadingDialog();
        }

        TuyaHomeSdk.getSceneManagerInstance().createScene(homeId, name, false, "", displayColor, "", conditions, tasks, preConditions, matchType, new ITuyaResultCallback<SceneBean>() {
            @Override
            public void onSuccess(SceneBean result) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    enableScene(result.getId());
                    mView.notifyCreateSceneSuccess(result);
                }
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyCreateSceneFailed(errorCode, errorMessage);
                }
            }
        });
    }

    @Override
    public void getDevTask(final SceneTask sceneTask, final int position) {
        if (TextUtils.isEmpty(sceneTask.getEntityId())) {
            return;
        }
        if (mView != null) {
            mView.showLoadingDialog();
        }
        TuyaHomeSdk.getSceneManagerInstance().getDeviceTaskOperationList(sceneTask.getEntityId(), new ITuyaResultCallback<List<TaskListBean>>() {
            @Override
            public void onSuccess(List<TaskListBean> result) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyGetDeviceTaskListSuccess(result, sceneTask, position);
                }
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyGetDeviceTaskListFail(errorMessage);
                    NooieLog.e("getDeviceTaskOperationList error " + errorCode + "  errorMsg  " + errorMessage);
                }
            }
        });
    }

    @Override
    public void getGroupTask(final SceneTask sceneTask, final int position) {
        if (TextUtils.isEmpty(sceneTask.getEntityId())) {
            return;
        }
        if (mView != null) {
            mView.showLoadingDialog();
        }
        TuyaHomeSdk.getSceneManagerInstance().getDeviceTaskOperationListByGroup(sceneTask.getEntityId(), new ITuyaResultCallback<List<TaskListBean>>() {
            @Override
            public void onSuccess(List<TaskListBean> result) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyGetDeviceTaskListSuccess(result, sceneTask, position);
                }
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyGetDeviceTaskListFail(errorMessage);
                    NooieLog.e("getDeviceTaskOperationListByGroup error " + errorCode + "  errorMsg  " + errorMessage);
                }
            }
        });
    }

    @Override
    public void getDevCondition(final SceneCondition sceneCondition, final int position) {
        if (TextUtils.isEmpty(sceneCondition.getEntityId())) {
            return;
        }
        if (mView != null) {
            mView.showLoadingDialog();
        }
        TuyaHomeSdk.getSceneManagerInstance().getDeviceConditionOperationList(sceneCondition.getEntityId(), new ITuyaResultCallback<List<TaskListBean>>() {
            @Override
            public void onSuccess(List<TaskListBean> result) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyGetDeviceTaskListSuccess(result, sceneCondition, position);
                }
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyGetDeviceTaskListFail(errorMessage);
                    NooieLog.e("getDeviceConditionOperationList error " + errorCode + "  errorMsg  " + errorMessage);
                }
            }
        });
    }

    @Override
    public void getWeatherCondition(final SceneCondition sceneCondition, final int position) {
        if (mView != null) {
            mView.showLoadingDialog();
        }
        TuyaHomeSdk.getSceneManagerInstance().getConditionList(false, new ITuyaResultCallback<List<ConditionListBean>>() {
            @Override
            public void onSuccess(List<ConditionListBean> result) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyGetConditionListSuccess(result, sceneCondition, position);
                }
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyGetConditionListFail(errorMessage);
                    NooieLog.e("getConditionList error " + errorCode + "  errorMsg  " + errorMessage);
                }
            }
        });
    }

    @Override
    public void enableScene(String sceneId) {
        TuyaHomeSdk.newSceneInstance(sceneId).enableScene(sceneId, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                NooieLog.e("executeScene error " + code + "  errorMsg  " + error);
                if (mView != null) {
                    mView.notifyEnableScene();
                }
            }

            @Override
            public void onSuccess() {
                NooieLog.e("----------sceneID " + sceneId + " enableScene Success");
                if (mView != null) {
                    mView.notifyEnableScene();
                }
            }
        });
    }

    @Override
    public void disableScene(String sceneId) {
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
