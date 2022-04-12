package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.view.IAddHomeView;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;

import java.util.List;

/**
 * NameHomePresenter
 *
 * @author Administrator
 * @date 2019/3/14
 */
public class AddHomePresenter implements IAddHomePresenter {

    IAddHomeView mAddHomeView;

    public AddHomePresenter(IAddHomeView view) {
        mAddHomeView = view;
    }

    @Override
    public void createHome(String homeName, List<String> roomList) {
        FamilyManager.getInstance().createHome(homeName, roomList, new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                if (mAddHomeView != null) {
                    mAddHomeView.notifyCreateHomeSuccess(homeBean);
                }
            }

            @Override
            public void onError(String code, String msg) {
                if (mAddHomeView != null) {
                    mAddHomeView.notifyCreateHomeFailed(code);
                }
            }
        });
    }


}
