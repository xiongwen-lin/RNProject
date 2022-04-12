package com.afar.osaio.smart.setting.view;

import com.afar.osaio.base.mvp.IBaseView;
import com.afar.osaio.bean.AlarmLevel;
import com.afar.osaio.bean.DetectionSchedule;
import com.nooie.sdk.device.bean.MTAreaInfo;

import java.util.List;

/**
 * Created by victor on 2018/7/5
 * Email is victor.qiao.0604@gmail.com
 */
public interface INooieDetectionView extends IBaseView {
    void showLoadingDialog();

    void hideLoadingDialog();

    void showError(String err);

    void onGetSoundDetection(AlarmLevel soundDetection);

    void notifySetDetectionResult(String result);

    /**
     * Sleep state
     *
     * @param message
     */
    void notifyGetSleepStateFailed(String message);

    void notifyGetSleepStateSuccess(boolean openCamera);

    void notifySetSleepStateResult(String result);

    void notifyGetDetectionSchedulesSuccess(int detectType, List<DetectionSchedule> schedules);

    void onGetMtAreaInfo(String result, MTAreaInfo info);

}
