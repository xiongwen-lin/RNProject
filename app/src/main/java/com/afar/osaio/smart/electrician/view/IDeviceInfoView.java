package com.afar.osaio.smart.electrician.view;

import com.afar.osaio.base.mvp.IBaseView;
import com.afar.osaio.smart.electrician.bean.DeviceInfoBean;
import com.tuya.smart.sdk.bean.DeviceBean;

/**
 * IDeviceInfoPresenter
 *
 * @author Administrator
 * @date 2019/3/18
 */
public interface IDeviceInfoView extends IBaseView {

    void notifyLoadDeviceInfo(DeviceBean device);

    void getDeviceIpSuccess(DeviceInfoBean deviceInfoBean);

    void getDeviceIpFail(String error);
}
