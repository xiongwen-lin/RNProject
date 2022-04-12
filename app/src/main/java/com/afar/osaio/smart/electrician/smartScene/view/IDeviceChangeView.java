package com.afar.osaio.smart.electrician.smartScene.view;

import com.afar.osaio.base.mvp.IBaseView;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.List;

public interface IDeviceChangeView extends IBaseView {

    void notifyGetConditionDevListSuccess(List<DeviceBean> deviceBeanList);

    void notifyGetConditionDevListFail(String errorMsg);
}
