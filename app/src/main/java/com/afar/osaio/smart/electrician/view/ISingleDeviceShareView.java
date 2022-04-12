package com.afar.osaio.smart.electrician.view;

import com.afar.osaio.base.mvp.IBaseView;

/**
 * IAddMemberView
 *
 * @author Administrator
 * @date 2019/3/13
 */
public interface ISingleDeviceShareView extends IBaseView {

    void notifyGetUidSuccess(String uid);

    void notifyGetUidFailed(String msg);

    void notifyUserNotRegister();

    void notifySharedDeviceState(String msg);
}
