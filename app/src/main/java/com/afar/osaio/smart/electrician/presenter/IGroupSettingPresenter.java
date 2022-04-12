package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

/**
 * IGroupSettingPresenter
 *
 * @author Administrator
 * @date 2019/3/21
 */
public interface IGroupSettingPresenter extends IBasePresenter {

    void getTimerWithTask(String taskName, String devId);

    void removeGroup(long groupId);

    void onGroupInfoUpdate(long groupId);

}
