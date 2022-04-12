package com.afar.osaio.smart.setting.view;

import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.afar.osaio.base.mvp.IBaseView;
import com.nooie.sdk.api.network.base.bean.entity.DeviceOfOrderResult;
import com.nooie.sdk.api.network.base.bean.entity.PackInfoResult;

import java.util.List;

/**
 * INooieStorageView
 *
 * @author Administrator
 * @date 2019/4/18
 */
public interface INooieStorageView extends IBaseView {
    void showLoadingDialog();

    void hideLoadingDialog();

    /**
     * Loop recording
     *
     * @param message
     */
    void notifyGetLoopRecordingFailed(String message);

    void notifyGetLoopRecordingSuccess(boolean open);

    void notifySetLoopRecordingResult(String result);

    /**
     * Cloud information
     *
     * @param message
     */
    void notifyGetCloudInfoFailed(String message);

    void notifyGetCloudInfoSuccess(BaseResponse<PackInfoResult> response);

    /**
     * Format card
     *
     * @param result
     */
    void notifyFormatCardResult(String result);

    void notifyQuerySDStatusSuccess(int status, String freeGB, String totalGB, int progress);

    void notifyQuerySDStatusFailed(String msg);

    void notifyUnsubscribePackResult(String result);

    void notifyNoStorage();

    void onLoadDeviceOfOrder(String result, String deviceId, List<DeviceOfOrderResult> orderResult);

}
