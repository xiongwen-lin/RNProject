package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;
import com.tuya.smart.sdk.bean.Timer;

/**
 * IGroupPresenter
 *
 * @author Administrator
 * @date 2019/3/20
 */
public interface IGroupPresenter extends IBasePresenter {

    void setGroupSchedule(String timeType, String schedule);

    void onGroupInfoUpdate(long groupId);

    void getTimerWithTask(String taskName, String devId);

    void updateTimerStatusWithTask(String taskName, String devId, String timerId, boolean isOpen, int position, Timer timer);

    void removeTimerWithTask(String taskName, String devId, String timerId,int position);

}
