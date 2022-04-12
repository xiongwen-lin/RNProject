package com.afar.osaio.smart.electrician.view;

import com.afar.osaio.base.mvp.IBaseView;
import com.tuya.smart.home.sdk.bean.MemberBean;

/**
 * IAddOwnerView
 *
 * @author Administrator
 * @date 2019/3/13
 */
public interface IAddOwnerView extends IBaseView {

    void notifyAddMemberSuccess(MemberBean member);

    void notifyAddMemberFailed(String msg, boolean isTuyaError);

    void notifyGetUidSuccess(String uid);

    void notifyUserNotRegister();
}

