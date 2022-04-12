package com.afar.osaio.smart.electrician.view;
;
import com.afar.osaio.base.mvp.IBaseView;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.bean.MemberBean;
import com.tuya.smart.home.sdk.bean.SharedUserInfoBean;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.List;

/**
 * IHomeManagerView
 *
 * @author Administrator
 * @date 2019/3/13
 */
public interface IHomeManagerView extends IBaseView {

    void notifyGetHomeDetailSuccess(HomeBean homeBean);

    void notifyGetHomeDetailFailed(String msg);

    void notifyHomeDevices(List<DeviceBean> devices);

    void notifyHomeMemberSuccess( List<MemberBean> members);

    void notifyHomeMemberFailed(String msg);

    void notifyLoadHomesSuccess(List<HomeBean> homes,boolean isUpdate);

    void notifyLoadHomesFailed(String msg);

    void notifyResetHomeState(String msg);

    void notifyChangeHomeState(String msg);

    void notifyUserShareListSuccess(List<SharedUserInfoBean> sharedUserInfoBeanList);

    void notifyUserShareListFail(String msg);
}
