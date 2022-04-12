package com.afar.osaio.smart.electrician.view;

import com.afar.osaio.base.mvp.IBaseView;
import com.tuya.smart.home.sdk.bean.ShareSentUserDetailBean;

/**
 * IMemberView
 *
 * @author Administrator
 * @date 2019/3/15
 */
public interface IMemberView extends IBaseView {

    void notifyRemoveMemberState(String msg);

    void notifyLoadUserShareInfoSuccess(ShareSentUserDetailBean detailBean);

    void notifyLoadUserShareInfoFailed(String msg);

    void notifyGetAccountSuccess(String account);

    void notifyGetAccountFailed(String msg);

    void notifyRemoveUserShareSuccess(String msg);

    void notifyRemoveUserShareFail(String error);

}
