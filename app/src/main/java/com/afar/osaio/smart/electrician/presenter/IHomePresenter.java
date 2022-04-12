package com.afar.osaio.smart.electrician.presenter;

import com.tuya.smart.android.mvp.presenter.IPresenter;
import com.tuya.smart.home.sdk.bean.DeviceAndGroupInRoomBean;

import java.util.List;
import java.util.Map;

/**
 * IHomePresenter
 *
 * @author Administrator
 * @date 2019/2/28
 */
public interface IHomePresenter extends IPresenter {

    void createDefaultHome(int times);

    void loadHomeDetail(long homeId);

    void loadOnServerConnectHome(long homeId);

    /**
     * 控制智能插座的群组
     *
     * @param groupId
     * @param open
     */
    void controlGroup(long groupId, boolean open);

    /**
     * 控制智能灯的群组
     *
     * @param groupId
     * @param dpId
     * @param open
     */
    void controlLampGroup(long groupId, String dpId, boolean open);

    void controlDevice(String deviceId, boolean open);

    void controlPowerStrip(String devId, String dpId, boolean open);

    /**
     * 控制智能灯
     *
     * @param devId
     * @param dpId
     * @param open
     */
    void controlLamp(String devId, String dpId, boolean open);

    void release();

    void loadHomes();

    void changeCurrentHome(long homeId);

    void sortDevice(long roomId, List<DeviceAndGroupInRoomBean> list);

    void loadDeviceBean(String deviceId);

    void loadGroupBean(long groupId);

    void controlBrightness(String deviceId, String dpId, int brightnessValue);

    void updateMessageStatus(String id, int type);

    void controlStrip(String devId, Map<String, Object> dpsMap);

    void getHomeList();

    void checkNetworkStatus();

    void setWeather(double lon,double lat);

    void loadBanner();

}
