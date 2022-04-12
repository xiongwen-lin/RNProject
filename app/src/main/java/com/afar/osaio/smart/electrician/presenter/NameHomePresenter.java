package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.view.INameHomeView;
import com.afar.osaio.util.ConstantValue;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.api.IResultCallback;

import java.util.List;

/**
 * NameHomePresenter
 *
 * @author Administrator
 * @date 2019/3/14
 */
public class NameHomePresenter implements INameHomePresenter {

    INameHomeView mNameHomeView;

    public NameHomePresenter(INameHomeView view) {
        mNameHomeView = view;
    }

    @Override
    public void createHome(String homeName, List<String> roomList) {
        FamilyManager.getInstance().createHome(homeName, roomList, new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                if (mNameHomeView != null) {
                    mNameHomeView.notifyCreateHomeSuccess(homeBean);
                }
            }

            @Override
            public void onError(String code, String msg) {
                if (mNameHomeView != null) {
                    mNameHomeView.notifyCreateHomeFailed(code);
                }
            }
        });
    }

    @Override
    public void updateHome(HomeBean homeBean, String homeName) {
        TuyaHomeSdk.newHomeInstance(homeBean.getHomeId()).updateHome(homeName, homeBean.getLon(), homeBean.getLat(), homeBean.getGeoName(), new IResultCallback() {
            @Override
            public void onError(String code, String msg) {
                if (mNameHomeView != null) {
                    mNameHomeView.notifyUpdateHomeState(code);
                }
            }

            @Override
            public void onSuccess() {
                if (mNameHomeView != null) {
                    mNameHomeView.notifyUpdateHomeState(ConstantValue.SUCCESS);
                }
            }
        });
    }
}
