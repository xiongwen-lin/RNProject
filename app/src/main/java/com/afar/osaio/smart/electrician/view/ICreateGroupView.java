package com.afar.osaio.smart.electrician.view;

import com.afar.osaio.base.mvp.IBaseView;
import com.tuya.smart.sdk.bean.GroupDeviceBean;

import java.util.List;

/**
 * ICreateGroupView
 *
 * @author Administrator
 * @date 2019/3/20
 */
public interface ICreateGroupView extends IBaseView {

    void notifyLoadDevicesSuccess(List<GroupDeviceBean> devices);

    void notifyDevicesFailed(String msg);
}
