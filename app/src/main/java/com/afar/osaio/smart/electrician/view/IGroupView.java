package com.afar.osaio.smart.electrician.view;

import com.afar.osaio.base.mvp.IBaseView;
import com.tuya.smart.sdk.bean.GroupBean;
import com.tuya.smart.sdk.bean.Timer;
import com.tuya.smart.sdk.bean.TimerTask;

/**
 * IGroupView
 *
 * @author Administrator
 * @date 2019/3/20
 */
public interface IGroupView extends IBaseView {

    void notifyCreateGroupScheduleState(String msg);

    void notifyCleanGroupScheduleState(String msg);

    void notifyOnGroupInfoUpdate(GroupBean groupBean );

    void notifyGetTimerWithTaskSuccess(TimerTask timerTask);

    void notifyGetTimerWithTaskFail(String errorCode, String errorMsg);

    void notifyUpdateTimerStatusWithTaskSuccess(int position, Timer timer, boolean isOpen);

    void notifyUpdateTimerStatusWithTaskFail(String errorCode, String errorMsg,int position, Timer timer );

    void notifyRemoveTimerWithTaskSuccess(int position);

    void notifyRemoveTimerWithTaskFail(String errorCode, String errorMsg);


}
