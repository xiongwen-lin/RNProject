package com.afar.osaio.smart.electrician.presenter;
import com.afar.osaio.smart.electrician.model.GroupModel;
import com.afar.osaio.smart.electrician.model.IGroupModel;
import com.afar.osaio.smart.electrician.model.IScheduleModel;
import com.afar.osaio.smart.electrician.model.ScheduleModel;
import com.afar.osaio.smart.electrician.view.IGroupView;
import com.afar.osaio.util.ConstantValue;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IGetTimerWithTaskCallback;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.IResultStatusCallback;
import com.tuya.smart.sdk.bean.GroupBean;
import com.tuya.smart.sdk.bean.Timer;
import com.tuya.smart.sdk.bean.TimerTask;

/**
 * GroupPresenter
 *
 * @author Administrator
 * @date 2019/3/21
 */
public class GroupPresenter implements IGroupPresenter {

    private IGroupView mGroupView;
    private IGroupModel mGroupModel;
    private IScheduleModel mScheduleModel;

    public GroupPresenter(IGroupView view, long gruopId) {
        mGroupView = view;
        mGroupModel = new GroupModel(gruopId);
        mScheduleModel = new ScheduleModel();
    }

    @Override
    public void setGroupSchedule(String timeType, String schedule) {
        mGroupModel.sendCommand(timeType, schedule, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                if (mGroupView != null) {
                    mGroupView.notifyCreateGroupScheduleState(code);
                }
            }

            @Override
            public void onSuccess() {
                if (mGroupView != null){
                    mGroupView.notifyCreateGroupScheduleState(ConstantValue.SUCCESS);
                }
            }
        });
    }

    @Override
    public void onGroupInfoUpdate(long groupId) {
        GroupBean group = TuyaHomeSdk.getDataInstance().getGroupBean(groupId);
        if (mGroupView != null){
            mGroupView.notifyOnGroupInfoUpdate(group);
        }
    }

    @Override
    public void getTimerWithTask(String taskName, String devId) {
        mScheduleModel.getTimerWithTask(taskName, devId, new IGetTimerWithTaskCallback() {
            @Override
            public void onSuccess(TimerTask timerTask) {
                if (mGroupView != null){
                    mGroupView.notifyGetTimerWithTaskSuccess(timerTask);
                }
            }

            @Override
            public void onError(String s, String s1) {
                if (mGroupView != null){
                    mGroupView.notifyGetTimerWithTaskFail(s,s1);
                }
            }
        });

    }

    @Override
    public void updateTimerStatusWithTask(String taskName, String devId, String timerId, final boolean isOpen, final int position, final Timer timer) {
        mScheduleModel.updateTimerStatusWithTask(taskName, devId, timerId, isOpen, new IResultStatusCallback() {
            @Override
            public void onSuccess() {
                if (mGroupView != null)
                    mGroupView.notifyUpdateTimerStatusWithTaskSuccess(position, timer, isOpen);
            }

            @Override
            public void onError(String errorCode, String errorMsg) {
                if (mGroupView != null){
                    mGroupView.notifyUpdateTimerStatusWithTaskFail(errorCode,errorMsg,position,timer);
                }
            }
        });
    }

    @Override
    public void removeTimerWithTask(String taskName, String devId, String timerId, final int position) {
        mScheduleModel.removeTimerWithTask(taskName, devId, timerId, new IResultStatusCallback() {
            @Override
            public void onSuccess() {
                if (mGroupView != null){
                    mGroupView.notifyRemoveTimerWithTaskSuccess(position);
                }
            }

            @Override
            public void onError(String errorCode, String errorMsg) {
                if (mGroupView != null){
                    mGroupView.notifyRemoveTimerWithTaskFail(errorCode,errorMsg);
                }
            }
        });
    }

}
