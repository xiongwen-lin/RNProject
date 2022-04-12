package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.view.IHomeSettingView;
import com.afar.osaio.util.ConstantValue;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.api.IResultCallback;

/**
 * HomeSettingPresenter
 *
 * @author Administrator
 * @date 2019/3/14
 */
public class HomeSettingPresenter implements IHomeSettingPresenter {

    IHomeSettingView mHomeSettingView;

    public HomeSettingPresenter(IHomeSettingView view) {
        mHomeSettingView = view;
    }

    @Override
    public void updateHome(HomeBean homeBean, String homeName) {
        TuyaHomeSdk.newHomeInstance(homeBean.getHomeId()).updateHome(homeName, homeBean.getLon(), homeBean.getLat(), homeBean.getGeoName(), new IResultCallback() {
            @Override
            public void onError(String code, String msg) {
                if (mHomeSettingView != null) {
                    mHomeSettingView.notifyUpdateHomeState(code);
                }
            }

            @Override
            public void onSuccess() {
                if (mHomeSettingView != null) {
                    mHomeSettingView.notifyUpdateHomeState(ConstantValue.SUCCESS);
                }
            }
        });
    }

    @Override
    public void removeHome(long homeId) {
        TuyaHomeSdk.newHomeInstance(homeId).dismissHome(new IResultCallback() {
            @Override
            public void onError(String code, String msg) {
                if (mHomeSettingView != null) {
                    mHomeSettingView.notifyRemoveHomeState(msg);
                }
            }

            @Override
            public void onSuccess() {
                if (mHomeSettingView != null) {
                    mHomeSettingView.notifyRemoveHomeState(ConstantValue.SUCCESS);
                }
            }
        });
    }

    @Override
    public void refreshHome() {
        FamilyManager.getInstance().updateCurrentHome(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                if (mHomeSettingView != null) {
                    mHomeSettingView.notifyRefreshHomeState(ConstantValue.SUCCESS);
                }
            }

            @Override
            public void onError(String code, String msg) {
                if (mHomeSettingView != null) {
                    mHomeSettingView.notifyRefreshHomeState(code);
                }
            }
        });
    }

}
