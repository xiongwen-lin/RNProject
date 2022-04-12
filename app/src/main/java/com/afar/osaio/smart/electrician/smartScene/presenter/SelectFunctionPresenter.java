package com.afar.osaio.smart.electrician.smartScene.presenter;

import com.afar.osaio.smart.electrician.smartScene.view.ISelectFunctionView;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.scene.dev.TaskListBean;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;

import java.util.List;

public class SelectFunctionPresenter implements ISelectFunctionPresenter {

    private ISelectFunctionView mView;

    public SelectFunctionPresenter(ISelectFunctionView view) {
        this.mView = view;
    }

    @Override
    public void getDevTask(String devId) {
        if (mView != null) {
            mView.showLoadingDialog();
        }
        TuyaHomeSdk.getSceneManagerInstance().getDeviceTaskOperationList(devId, new ITuyaResultCallback<List<TaskListBean>>() {
            @Override
            public void onSuccess(List<TaskListBean> result) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyGetDeviceTaskListSuccess(result);
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
    public void getGroupTask(String goupId) {
        if (mView != null) {
            mView.showLoadingDialog();
        }
        TuyaHomeSdk.getSceneManagerInstance().getDeviceTaskOperationListByGroup(goupId, new ITuyaResultCallback<List<TaskListBean>>() {
            @Override
            public void onSuccess(List<TaskListBean> result) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyGetDeviceTaskListSuccess(result);
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
    public void getDevCondition(String devId) {
        if (mView != null) {
            mView.showLoadingDialog();
        }
        TuyaHomeSdk.getSceneManagerInstance().getDeviceConditionOperationList(devId, new ITuyaResultCallback<List<TaskListBean>>() {
            @Override
            public void onSuccess(List<TaskListBean> result) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyGetDeviceTaskListSuccess(result);
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
}
