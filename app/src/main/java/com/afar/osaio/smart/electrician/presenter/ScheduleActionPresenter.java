package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.smart.electrician.model.IScheduleModel;
import com.afar.osaio.smart.electrician.model.ScheduleModel;
import com.afar.osaio.smart.electrician.view.ISheduleActionView;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.sdk.api.IResultStatusCallback;

import java.util.Map;

public class ScheduleActionPresenter implements IScheduleActionPresenter {

    private IScheduleModel mScheduleModel;
    private ISheduleActionView mSheduleActionView;

    public ScheduleActionPresenter(ISheduleActionView view) {
        mSheduleActionView = view;
        mScheduleModel = new ScheduleModel();
    }

    @Override
    public void setScheduleAtion(String taskName, String devId, String loops, Map<String, Object> dps, String time) {
        NooieLog.e("---->>. dps "+dps.toString());
        mScheduleModel.setScheduleAtion(taskName, devId, loops, dps, time, new IResultStatusCallback() {
            @Override
            public void onSuccess() {
                if (mSheduleActionView != null){
                    mSheduleActionView.notifyScheduleAtionSuccess();
                }
            }

            @Override
            public void onError(String code, String s1) {
                if (mSheduleActionView != null){
                    mSheduleActionView.notifyScheduleActionFail(code);
                }
            }
        });
    }


}
