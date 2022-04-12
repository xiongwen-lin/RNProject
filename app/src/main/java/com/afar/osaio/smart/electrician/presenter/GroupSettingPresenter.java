package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.smart.electrician.model.IScheduleModel;
import com.afar.osaio.smart.electrician.model.ScheduleModel;
import com.afar.osaio.smart.electrician.view.IGroupSettingView;
import com.afar.osaio.util.ConstantValue;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IGetTimerWithTaskCallback;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.bean.GroupBean;
import com.tuya.smart.sdk.bean.TimerTask;

/**
 * GroupSettingPresenter
 *
 * @author Administrator
 * @date 2019/3/21
 */
public class GroupSettingPresenter implements IGroupSettingPresenter {

    private IGroupSettingView mGroupSettingView;
    private IScheduleModel mScheduleModel;

    public GroupSettingPresenter(IGroupSettingView view) {
        mGroupSettingView = view;
        mScheduleModel = new ScheduleModel();
    }

    @Override
    public void getTimerWithTask(String taskName, String devId) {
        mScheduleModel.getTimerWithTask(taskName, devId, new IGetTimerWithTaskCallback() {
            @Override
            public void onSuccess(TimerTask timerTask) {
                if (mGroupSettingView != null){
                    mGroupSettingView.notifyGetTimerWithTaskSuccess(timerTask);
                }
            }

            @Override
            public void onError(String s, String s1) {
                if (mGroupSettingView != null){
                    mGroupSettingView.notifyGetTimerWithTaskFail(s,s1);
                }
            }
        });
    }

    @Override
    public void removeGroup(long groupId) {
        TuyaHomeSdk.newGroupInstance(groupId).dismissGroup(new IResultCallback() {
            @Override
            public void onError(String code, String msg) {
                if (mGroupSettingView != null) {
                    mGroupSettingView.notifyRemoveGroupState(code);
                }
            }

            @Override
            public void onSuccess() {
                if (mGroupSettingView != null) {
                    mGroupSettingView.notifyRemoveGroupState(ConstantValue.SUCCESS);
                }
            }
        });
    }

    @Override
    public void onGroupInfoUpdate(long groupId) {
        GroupBean groupBean = TuyaHomeSdk.getDataInstance().getGroupBean(groupId);
        if (mGroupSettingView != null){
            mGroupSettingView.notifyOnGroupInfoUpdate(groupBean);
        }
    }
}
