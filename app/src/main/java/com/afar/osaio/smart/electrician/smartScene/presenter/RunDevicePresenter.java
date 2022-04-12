package com.afar.osaio.smart.electrician.smartScene.presenter;

import com.afar.osaio.smart.electrician.bean.MixDeviceBean;
import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.smartScene.view.IRunDeviceView;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.scene.SceneTaskGroupDevice;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;

import java.util.ArrayList;
import java.util.List;

public class RunDevicePresenter implements IRunDevicePresenter {

    private IRunDeviceView mView;

    public RunDevicePresenter(IRunDeviceView view) {
        this.mView = view;
    }

    @Override
    public void getDevAndGroupTask() {
        if (mView != null) {
            mView.showLoadingDialog();
        }

        TuyaHomeSdk.getSceneManagerInstance().getTaskDevAndGoupList(FamilyManager.getInstance().getCurrentHomeId(), new ITuyaResultCallback<SceneTaskGroupDevice>() {
            @Override
            public void onSuccess(SceneTaskGroupDevice result) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    List<DeviceBean> deviceBeanList = result.getDevices();
                    List<GroupBean> groupBeanList = result.getGoups();
                    List<MixDeviceBean> devices = new ArrayList<>();

                    for (DeviceBean deviceBean : deviceBeanList) {
                        MixDeviceBean mixDeviceBean = new MixDeviceBean();
                        mixDeviceBean.setDeviceBean(deviceBean);
                        devices.add(mixDeviceBean);
                    }

                  /*  for (GroupBean groupBean : groupBeanList) {
                        MixDeviceBean mixDeviceBean = new MixDeviceBean();
                        mixDeviceBean.setGroupBean(groupBean);
                        devices.add(mixDeviceBean);
                    }*/
                    mView.notifyGetTaskDevAndGoupListSuccess(devices);
                    NooieLog.e("--------->>deviceList  " + deviceBeanList.size() + "  groupList  " + groupBeanList.size() + " mixBeanList  " + devices.size());
                }
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    NooieLog.e("getTaskDevAndGoupList error " + errorCode + "  errorMsg  " + errorMessage);
                }
            }
        });
    }
}
