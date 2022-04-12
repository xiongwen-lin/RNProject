package com.afar.osaio.smart.setting.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

/**
 * Created by victor on 2018/11/14
 * Email is victor.qiao.0604@gmail.com
 */
public interface INooieStoragePresenter extends IBasePresenter {

    void destroy();

    /**
     * 查询卡的信息,作用与getSDCardCapacity（旧版本的逻辑）一致
     * @param deviceId
     * @return
     */
    void loadSDCardInfo(String user, String deviceId, boolean isShortLinkDevice);

    /**
     * Loop record
     *
     * @param deviceId
     */
    void getLoopRecordStatus(String deviceId);

    void setLoopRecordStatus(String deviceId, boolean open);

    /**
     * Format SD card
     *
     * @param deviceId
     */
    void formatSDCard(String deviceId);

    /**
     * check cloud states of the device list;
     * @param user
     * @param deviceId device to check.
     */
    void getCloudState(String user, String deviceId, int bindType);

    void startQuerySDCardFormatState(String deviceId);

    void stopQuerySDCardFormatState();

    void unsubscribePack(String account, String deviceId);

    void getDeviceOfOrder(String deviceId);
}
