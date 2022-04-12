package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.view.ICreateGroupView;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;
import com.tuya.smart.sdk.bean.GroupDeviceBean;

import java.util.List;

/**
 * CreateGroupPresenter
 *
 * @author Administrator
 * @date 2019/3/20
 */
public class CreateGroupPresenter implements ICreateGroupPresenter {

    private ICreateGroupView mCreateGroupView;

    public CreateGroupPresenter(ICreateGroupView view) {
        mCreateGroupView = view;
    }

    @Override
    public void loadDevices(String productId) {
        TuyaHomeSdk.newHomeInstance(FamilyManager.getInstance().getCurrentHomeId()).queryDeviceListToAddGroup(-1, productId, new ITuyaResultCallback<List<GroupDeviceBean>>() {
            @Override
            public void onSuccess(List<GroupDeviceBean> groupDeviceBeans) {
                if (mCreateGroupView != null) {
                    mCreateGroupView.notifyLoadDevicesSuccess(groupDeviceBeans);
                }
            }

            @Override
            public void onError(String code, String msg) {
                if (mCreateGroupView != null) {
                    mCreateGroupView.notifyDevicesFailed(code);
                }
            }
        });
    }
}
