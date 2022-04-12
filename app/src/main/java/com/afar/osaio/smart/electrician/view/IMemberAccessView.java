package com.afar.osaio.smart.electrician.view;

import com.afar.osaio.base.mvp.IBaseView;
import com.tuya.smart.home.sdk.bean.ShareSentUserDetailBean;

/**
 * IMemberView
 *
 * @author jiangzt
 * @date 2019/4/25
 */
public interface IMemberAccessView extends IBaseView {

    void notifyRemoveDeviceState(String msg);

    void notifyLoadUserShareInfoSuccess(ShareSentUserDetailBean detailBean);

    void notifyLoadUserShareInfoFailed(String msg);

}
