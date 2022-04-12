package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.view.IManageDeviceView;
import com.afar.osaio.util.ConstantValue;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.bean.GroupBean;
import com.tuya.smart.sdk.bean.GroupDeviceBean;

import java.util.List;

/**
 * ManageDevicePresenter
 *
 * @author Administrator
 * @date 2019/3/21
 */
public class ManageDevicePresenter implements IManageDevicePresenter {

    private IManageDeviceView mManageDeviceView;

    public ManageDevicePresenter(IManageDeviceView view) {
        mManageDeviceView = view;
    }

    @Override
    public void loadGroup(long groupId) {
        GroupBean groupBean = TuyaHomeSdk.getDataInstance().getGroupBean(groupId);
        if (mManageDeviceView != null) {
            mManageDeviceView.notifyLoadGroupSuccess(groupBean);
        }
    }


    @Override
    public void loadSelectDevices(long groupId, String productId) {
        if (mManageDeviceView != null) {
            mManageDeviceView.showLoadingDialog();
        }
        TuyaHomeSdk.newHomeInstance(FamilyManager.getInstance().getCurrentHomeId()).queryDeviceListToAddGroup(groupId, productId, new ITuyaResultCallback<List<GroupDeviceBean>>() {
            @Override
            public void onSuccess(List<GroupDeviceBean> groupDeviceBeans) {
                if (mManageDeviceView != null) {
                    mManageDeviceView.hideLoadingDialog();
                    mManageDeviceView.notifyLoadDevicesSuccess(groupDeviceBeans);
                }
            }

            @Override
            public void onError(String code, String msg) {
                if (mManageDeviceView != null) {
                    mManageDeviceView.hideLoadingDialog();
                    mManageDeviceView.notifyDevicesFailed(code);
                }
            }
        });
    }

    @Override
    public void updateDeviceList(long groupId, List<String> devIds) {
        TuyaHomeSdk.newGroupInstance(groupId).updateDeviceList(devIds, new IResultCallback() {
            @Override
            public void onError(String code, String msg) {
                if (mManageDeviceView != null) {
                    mManageDeviceView.notifyUpdateDeviceState(code);
                }
            }

            @Override
            public void onSuccess() {
                if (mManageDeviceView != null) {
                    mManageDeviceView.notifyUpdateDeviceState(ConstantValue.SUCCESS);
                }
            }
        });
    }
}
