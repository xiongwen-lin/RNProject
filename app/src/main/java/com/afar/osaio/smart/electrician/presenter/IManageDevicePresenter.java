package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

import java.util.List;

/**
 * IManageDevicePresenter
 *
 * @author Administrator
 * @date 2019/3/21
 */
public interface IManageDevicePresenter extends IBasePresenter {

    //获取群组里的设备
    void loadGroup(long groupId);

    //获取群组里可添加的设备
    void loadSelectDevices(long groupId, String productId);

    //更新群组里的设备
    void updateDeviceList(long groupId, List<String> devIds);


}
