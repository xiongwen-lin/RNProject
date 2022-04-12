package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.model.DeviceShareUsersMode;
import com.afar.osaio.smart.electrician.model.IDeviceShareUsersMode;
import com.afar.osaio.smart.electrician.view.IDeviceShareUsersView;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.bean.SharedUserInfoBean;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;

import java.util.List;

/**
 * DeviceShareUsersPresenter
 *
 * @author Administrator
 * @date 2019/5/14
 */
public class DeviceShareUsersPresenter implements IDeviceShareUsersPresenter {

    private IDeviceShareUsersView mDeviceShareUsersView;
    private IDeviceShareUsersMode mDeviceShareUsersMode;

    public DeviceShareUsersPresenter(IDeviceShareUsersView view) {
        mDeviceShareUsersView = view;
        mDeviceShareUsersMode = new DeviceShareUsersMode();
    }

    @Override
    public void getHomeDetail(long homeId) {
        if (homeId == FamilyManager.DEFAULT_HOME_ID) {
            return;
        }
        TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                if (mDeviceShareUsersView != null) {
                    mDeviceShareUsersView.notifyGetHomeDetailSuccess(homeBean);
                }
            }

            @Override
            public void onError(String code, String msg) {
                if (mDeviceShareUsersView != null) {
                    mDeviceShareUsersView.notifyGetHomeDetailFailed(msg);
                }
            }
        });
    }

    @Override
    public void queryDevShareUserList(String deviceId) {
        mDeviceShareUsersMode.queryDevShareUserList(deviceId, new ITuyaResultCallback<List<SharedUserInfoBean>>() {
            @Override
            public void onError(String code, String errorMsg) {
                if (mDeviceShareUsersView != null) {
                    NooieLog.e("------>error queryDevShareUserList code " + code + " msg " + errorMsg);
                    mDeviceShareUsersView. queryDevShareUserListError(errorMsg);
                }
            }

            @Override
            public void onSuccess(List<SharedUserInfoBean> list) {
                if (mDeviceShareUsersView != null){
                    mDeviceShareUsersView.queryDevShareUserListSuccess(list);
                }
            }
        });
    }

}
