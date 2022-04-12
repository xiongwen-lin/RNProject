package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.view.IHomeListView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;

import java.util.List;


/**
 * HomeListPresenter
 *
 * @author Administrator
 * @date 2019/3/15
 */
public class HomeListPresenter implements IHomeListPresenter {

    IHomeListView mView;

    public HomeListPresenter(IHomeListView view) {
        mView = view;
    }

    @Override
    public void loadHomes() {
        if (mView != null) {
            mView.showLoadingDialog();
        }
        FamilyManager.getInstance().getHomeList(new ITuyaGetHomeListCallback() {
            @Override
            public void onSuccess(List<HomeBean> homes) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyLoadHomesSuccess(homes);
                }
            }

            @Override
            public void onError(String code, String msg) {
                NooieLog.e("-------------> code " + code + " msg " + msg);
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyLoadHomesFailed(code, msg);
                }
            }
        });
    }

    @Override
    public void changeCurrentHome(long homeId) {
        NooieLog.e("---------> changeCurrentHome  homeId " + homeId);
        if (mView != null) {
            mView.showLoadingDialog();
        }
        FamilyManager.getInstance().updateCurrentHomeById(homeId, new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                NooieLog.e("---------> changeCurrentHome onSuccess homeId " + homeBean.getHomeId());
                if (mView != null) {
                    mView.notifyChangeHomeState(ConstantValue.SUCCESS);
                }
            }

            @Override
            public void onError(String code, String msg) {
                NooieLog.e("---------> changeCurrentHome onError code " + code + " msg " + msg);
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyChangeHomeState(msg);
                }
            }
        });

    }
}

