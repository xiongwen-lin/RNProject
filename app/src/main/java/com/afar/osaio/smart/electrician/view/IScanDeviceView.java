package com.afar.osaio.smart.electrician.view;

import com.afar.osaio.base.mvp.IBaseView;
import com.tuya.smart.sdk.bean.DeviceBean;

/**
 * IScanDeviceView
 *
 * @author Administrator
 * @date 2019/3/5
 */
public interface IScanDeviceView extends IBaseView {

    void onDeviceSearchSuccess(DeviceBean deviceBean);

    void onDeviceSearchFailed();
}
