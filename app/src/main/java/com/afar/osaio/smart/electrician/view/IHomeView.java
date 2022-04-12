package com.afar.osaio.smart.electrician.view;

import com.nooie.sdk.api.network.base.bean.entity.BannerResult;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.bean.WeatherBean;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.GroupBean;

import java.util.List;

/**
 * IHomeView
 *
 * @author Administrator
 * @date 2019/2/25
 */
public interface IHomeView {
    /**
     * 加载用户信息回调
     *
     * @param result
     */
    void notifyLoadUserInfoState(String result);

    void loadHomeDetailSuccess(HomeBean homeBean);

    void loadHomeDetailFailed(String error);

    void notifyControlGroupState(String result);

    void notifyGroupDpUpdate(long groupId, String dps);

    void notifyControlDeviceState(String result, String msg);

    void notifyControlDeviceSuccess(String result);

    void notifyDeviceDpUpdate(String deviceId, String dps);

    void notifyLoadHomesSuccess(List<HomeBean> homes);

    void notifyLoadHomesFailed(String msg);

    void notifyChangeHomeState(String msg);

    void notifyLoadDeviceSuccess(String devId, DeviceBean deviceBean);

    void notifyLoadGroupSuccess(long groupId, GroupBean groupBean);

    void notifyLoadHomeListSuccess(String code, List<HomeBean> list);

    void onCheckNetworkStatus(String result, boolean isNetworkUsable);

    void onGetWeatherSuccess(WeatherBean weatherBean);

    void onGetWeatherFail(String errorCode, String errorMsg);

    void onLoadBannerSuccess(List<BannerResult.BannerInfo> bannerList);

    void onLoadBannerFail(String msg);
}
