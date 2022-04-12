package com.afar.osaio.smart.setting.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;
import com.afar.osaio.bean.AlarmLevel;

/**
 * Created by victor on 2018/7/5
 * Email is victor.qiao.0604@gmail.com
 */
public interface INooieDetectionPresenter extends IBasePresenter {

    void destroy();

    void getDetectionLevel(int detectType, String deviceId, boolean openCamera);

    void setDetectionLevel(int detectType, String deviceId, final AlarmLevel alarmLevel, boolean openCamera);

    /**
     * Sleep state
     *
     * @param deviceId
     */
    void getSleepStatus(String deviceId);

    void getDetectionSchedules(int detectType, String deviceId);

    void getMtAreaInfo(String deviceId);
}
