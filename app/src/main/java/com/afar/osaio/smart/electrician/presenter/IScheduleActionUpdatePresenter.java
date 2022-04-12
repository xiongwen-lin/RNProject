package com.afar.osaio.smart.electrician.presenter;

public interface IScheduleActionUpdatePresenter {

   void updateTimerWithTask(String taskName, String loops, String devId, String timerId, String dpId, String time, boolean isOpen);

   void updateTimerWithTask(String taskName, String loops, String devId, String timerId, String instruct);

}
