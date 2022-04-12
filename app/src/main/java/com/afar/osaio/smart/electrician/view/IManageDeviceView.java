package com.afar.osaio.smart.electrician.view;

import com.afar.osaio.base.mvp.IBaseView;
import com.tuya.smart.sdk.bean.GroupBean;
import com.tuya.smart.sdk.bean.GroupDeviceBean;

import java.util.List;

/**
 * IManageDeviceView
 *
 * @author Administrator
 * @date 2019/3/21
 */
public interface IManageDeviceView extends IBaseView {

    void notifyLoadGroupSuccess(GroupBean groupBean);

    void notifyLoadDevicesSuccess(List<GroupDeviceBean> devices);

    void notifyDevicesFailed(String msg);

    void notifyUpdateDeviceState(String msg);
}
