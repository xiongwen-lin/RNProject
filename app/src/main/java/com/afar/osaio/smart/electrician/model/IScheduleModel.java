package com.afar.osaio.smart.electrician.model;

import com.afar.osaio.base.mvp.IBaseModel;
import com.tuya.smart.sdk.api.IGetTimerWithTaskCallback;
import com.tuya.smart.sdk.api.IResultStatusCallback;

import java.util.Map;

public interface IScheduleModel extends IBaseModel {

    /**
     * 增加定时器
     */
    void setScheduleAtion(String taskName, String devId, String loops, Map<String, Object> dps, String time, IResultStatusCallback callback);

    /**
     *  获取定时任务下所有定时器
     */
    void getTimerWithTask(String taskName, String devId,IGetTimerWithTaskCallback callback);

    /**
     *  控制某个定时器的开关状态
     */
    void updateTimerStatusWithTask(String taskName, String devId, String timerId, boolean isOpen, IResultStatusCallback callback);

    /**
     * 移除某个定时器
     */
    void removeTimerWithTask(String taskName, String devId, String timerId, IResultStatusCallback callback);

    /**
     * 更新定时器的状态 该接口可以修改一个定时器的所有属性
     */
    void updateTimerWithTask(String taskName, String loops, String devId, String timerId, String dpId, String time, boolean isOpen, final IResultStatusCallback callback);

    /**
     * 更新定时器的状态 该接口可以修改一个定时器的所有属性
     */
    void updateTimerWithTask(String taskName, String loops, String devId, String timerId, String instruct, final IResultStatusCallback callback);


    void release();

}
