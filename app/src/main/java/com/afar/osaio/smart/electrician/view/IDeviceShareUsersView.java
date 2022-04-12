package com.afar.osaio.smart.electrician.view;

import com.afar.osaio.base.mvp.IBaseView;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.bean.SharedUserInfoBean;

import java.util.List;

public interface IDeviceShareUsersView extends IBaseView {

    void notifyGetHomeDetailSuccess(HomeBean homeBean);

    void notifyGetHomeDetailFailed(String msg);

    void queryDevShareUserListSuccess(List<SharedUserInfoBean> sharedUserInfoBeanList);

    void queryDevShareUserListError(String error);

}
