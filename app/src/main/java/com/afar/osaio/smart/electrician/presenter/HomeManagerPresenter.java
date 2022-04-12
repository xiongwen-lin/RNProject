package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.view.IHomeManagerView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.bean.MemberBean;
import com.tuya.smart.home.sdk.bean.SharedUserInfoBean;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaGetMemberListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.List;

/**
 * HomeManagerPresenter
 *
 * @author Administrator
 * @date 2019/3/13
 */
public class HomeManagerPresenter implements IHomeManagerPresenter {

    private IHomeManagerView mHomeManagerView;

    public HomeManagerPresenter(IHomeManagerView view) {
        mHomeManagerView = view;
    }

    @Override
    public void getHomeDetail(long homeId) {
        if (homeId == FamilyManager.DEFAULT_HOME_ID) {
            return;
        }
        TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                if (mHomeManagerView != null) {
                    mHomeManagerView.notifyGetHomeDetailSuccess(homeBean);
                }
            }

            @Override
            public void onError(String code, String msg) {
                if (mHomeManagerView != null) {
                    mHomeManagerView.notifyGetHomeDetailFailed(msg);
                }
            }
        });
    }

    @Override
    public void loadHomeDevices(long homeId) {
        List<DeviceBean> devices = TuyaHomeSdk.getDataInstance().getHomeDeviceList(homeId);
        if (mHomeManagerView != null) {
            mHomeManagerView.notifyHomeDevices(devices);
        }
    }

    @Override
    public void loadHomeMembers(long homeId) {
        TuyaHomeSdk.getMemberInstance().queryMemberList(homeId, new ITuyaGetMemberListCallback() {
            @Override
            public void onSuccess(List<MemberBean> members) {
                if (members != null && mHomeManagerView != null){
                    mHomeManagerView.notifyHomeMemberSuccess(members);
                }
            }

            @Override
            public void onError(String code, String msg) {
                if (mHomeManagerView != null) {
                    mHomeManagerView.notifyHomeMemberFailed(msg);
                }
            }
        });
    }

    @Override
    public void loadHomes(final boolean isUpdate) {
        FamilyManager.getInstance().getHomeList(new ITuyaGetHomeListCallback() {
            @Override
            public void onSuccess(List<HomeBean> homes) {
                if (mHomeManagerView != null) {
                    mHomeManagerView.notifyLoadHomesSuccess(homes,isUpdate);
                }
            }

            @Override
            public void onError(String error, String msg) {
                if (mHomeManagerView != null) {
                    mHomeManagerView.notifyLoadHomesFailed(msg);
                }
            }
        });
    }

    @Override
    public void resetHome() {
        FamilyManager.getInstance().resetHome(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                if (mHomeManagerView != null) {
                    mHomeManagerView.notifyResetHomeState(ConstantValue.SUCCESS);
                }
            }

            @Override
            public void onError(String code, String msg) {
                if (mHomeManagerView != null) {
                    mHomeManagerView.notifyResetHomeState(msg);
                }
            }
        });
    }

    @Override
    public void changeCurrentHome(long homeId) {
        NooieLog.e("--------------> changeCurrentHome homeID "+homeId);
        FamilyManager.getInstance().updateCurrentHomeById(homeId, new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                if (mHomeManagerView != null) {
                    mHomeManagerView.notifyChangeHomeState(ConstantValue.SUCCESS);
                }
            }

            @Override
            public void onError(String code, String msg) {
                if (mHomeManagerView != null) {
                    mHomeManagerView.notifyChangeHomeState(msg);
                }
            }
        });
    }

    @Override
    public void loadUserShareList(long homeId) {
        TuyaHomeSdk.getDeviceShareInstance().queryUserShareList(homeId, new ITuyaResultCallback<List<SharedUserInfoBean>>() {
            @Override
            public void onSuccess(List<SharedUserInfoBean> sharedUserInfoBeanList) {
                if (mHomeManagerView != null){
                    mHomeManagerView.notifyUserShareListSuccess(sharedUserInfoBeanList);
                }
            }

            @Override
            public void onError(String errorMsg, String errorCode) {
                if (mHomeManagerView != null){
                    mHomeManagerView.notifyUserShareListFail(errorMsg);
                }
            }
        });
    }

}
