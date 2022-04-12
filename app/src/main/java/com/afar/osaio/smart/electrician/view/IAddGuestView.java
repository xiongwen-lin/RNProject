package com.afar.osaio.smart.electrician.view;

import com.afar.osaio.base.mvp.IBaseView;
import com.tuya.smart.home.sdk.bean.SharedUserInfoBean;

import java.util.List;

/**
 * IAddGuestView
 *
 * @author Administrator
 * @date 2019/3/13
 */
public interface IAddGuestView extends IBaseView {

    void notifyAddMemberFailed(String msg, boolean isTuyaError);

    void notifyGetUidSuccess(String uid);

    void notifyHomeGuestSuccess(List<SharedUserInfoBean> sharedUserInfoBeanList);

    void notifyHomeGuestFailed(String msg);

    void notifyUserNotRegister();
}

