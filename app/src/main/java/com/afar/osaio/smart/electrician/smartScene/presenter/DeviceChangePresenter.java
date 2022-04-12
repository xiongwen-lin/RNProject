package com.afar.osaio.smart.electrician.smartScene.presenter;

import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.smartScene.view.IDeviceChangeView;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.List;

public class DeviceChangePresenter implements IDeviceChangePresenter {

    private IDeviceChangeView mView;

    public DeviceChangePresenter(IDeviceChangeView view) {
        this.mView = view;
    }

    @Override
    public void getConditionDevList() {
        if (mView != null) {
            mView.showLoadingDialog();
        }
        TuyaHomeSdk.getSceneManagerInstance().getConditionDevList(FamilyManager.getInstance().getCurrentHomeId(), new ITuyaResultCallback<List<DeviceBean>>() {
            @Override
            public void onSuccess(List<DeviceBean> result) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyGetConditionDevListSuccess(result);
                }
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyGetConditionDevListFail(errorMessage);
                }
            }
        });
    }
}
