package com.afar.osaio.smart.electrician.model;

import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IGetTimerWithTaskCallback;
import com.tuya.smart.sdk.api.IResultStatusCallback;
import com.tuya.smart.sdk.api.ITuyaTimer;

import java.util.Map;

public class ScheduleModel implements IScheduleModel {

    private ITuyaTimer mITuyaTime;

    public ScheduleModel(){
        mITuyaTime = TuyaHomeSdk.getTimerManagerInstance();
    }

    @Override
    public void setScheduleAtion(String taskName, String devId, String loops, Map<String, Object> dpsMap, String time, IResultStatusCallback callback) {
        mITuyaTime.addTimerWithTask(taskName,devId,loops,dpsMap,time,callback);
    }

    @Override
    public void updateTimerStatusWithTask(String taskName, String devId, String timerId, boolean isOpen, IResultStatusCallback callback) {
        mITuyaTime.updateTimerStatusWithTask(taskName, devId, timerId, isOpen, callback);
    }

    @Override
    public void getTimerWithTask(String taskName, String devId, IGetTimerWithTaskCallback callback) {
        mITuyaTime.getTimerWithTask(taskName,devId,callback);
    }

    @Override
    public void removeTimerWithTask(String taskName, String devId, String timerId, IResultStatusCallback callback) {
        mITuyaTime.removeTimerWithTask(taskName,devId,timerId,callback);
    }

    @Override
    public void updateTimerWithTask(String taskName, String loops, String devId, String timerId, String dpId, String time, boolean isOpen, IResultStatusCallback callback) {
        mITuyaTime.updateTimerWithTask(taskName,loops,devId,timerId,dpId,time,isOpen,callback);
    }

    @Override
    public void updateTimerWithTask(String taskName, String loops, String devId, String timerId, String instruct, IResultStatusCallback callback) {
        mITuyaTime.updateTimerWithTask(taskName,loops,devId,timerId,instruct,callback);
    }

    @Override
    public void release() {

    }


}
