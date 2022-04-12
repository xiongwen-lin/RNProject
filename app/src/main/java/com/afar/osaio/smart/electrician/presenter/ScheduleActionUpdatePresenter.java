package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.smart.electrician.model.IScheduleModel;
import com.afar.osaio.smart.electrician.model.ScheduleModel;
import com.afar.osaio.smart.electrician.view.ISheduleActionUpdateView;
import com.tuya.smart.sdk.api.IResultStatusCallback;

public class ScheduleActionUpdatePresenter implements IScheduleActionUpdatePresenter {

    private IScheduleModel mScheduleModel;
    private ISheduleActionUpdateView mSheduleActionUpdateView;

    public ScheduleActionUpdatePresenter(ISheduleActionUpdateView view) {
        mSheduleActionUpdateView = view;
        mScheduleModel = new ScheduleModel();
    }

    @Override
    public void updateTimerWithTask(String taskName, String loops, String devId, String timerId, String dpId, String time, boolean isOpen) {
        mScheduleModel.updateTimerWithTask(taskName, loops, devId, timerId, dpId, time, isOpen, new IResultStatusCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(String errorCode, String errorMessage) {

            }
        });
    }

    @Override
    public void updateTimerWithTask(String taskName, String loops, String devId, String timerId, String instruct) {
        mScheduleModel.updateTimerWithTask(taskName, loops, devId, timerId, instruct, new IResultStatusCallback() {
            @Override
            public void onSuccess() {
                if (mSheduleActionUpdateView != null){
                    mSheduleActionUpdateView.notifyUpdateTimerWithTaskSuccess();
                }
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                if (mSheduleActionUpdateView != null){
                    mSheduleActionUpdateView.notifyUpdateTimerWithTaskFail(errorCode,errorMessage);
                }
            }
        });
    }

}
