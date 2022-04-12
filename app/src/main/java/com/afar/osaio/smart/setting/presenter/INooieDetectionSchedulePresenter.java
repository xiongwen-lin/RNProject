package com.afar.osaio.smart.setting.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;
import com.afar.osaio.bean.DetectionSchedule;

public interface INooieDetectionSchedulePresenter extends IBasePresenter {

    void detachView();

    void getDetectionSchedules(int detectType, String deviceId, boolean isHideLoading);

    void setDetectionSchedules(int detectType, String deviceId, DetectionSchedule detectionSchedule);

    void getPIRPlanConfig(String deviceId, boolean isHideLoading);
}
