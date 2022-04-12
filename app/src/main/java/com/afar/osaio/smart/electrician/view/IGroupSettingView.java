package com.afar.osaio.smart.electrician.view;

import com.afar.osaio.base.mvp.IBaseView;
import com.tuya.smart.sdk.bean.GroupBean;
import com.tuya.smart.sdk.bean.TimerTask;

/**
 * IGroupSettingView
 *
 * @author Administrator
 * @date 2019/3/21
 */
public interface IGroupSettingView extends IBaseView {

    void notifyRemoveGroupState(String msg);

    void notifyOnGroupInfoUpdate(GroupBean groupBean );

    void notifyGetTimerWithTaskSuccess(TimerTask timerTask);

    void notifyGetTimerWithTaskFail(String errorCode, String errorMsg);

}
