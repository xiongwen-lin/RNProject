package com.afar.osaio.smart.electrician.presenter;


import com.afar.osaio.base.mvp.IBasePresenter;

/**
 * IDeviceShareUsersPresenter
 *
 * @author Administrator
 * @date 2019/5/14
 */
public interface IDeviceShareUsersPresenter extends IBasePresenter {

    void getHomeDetail(long homeId);

    /**
     * 查询指定设备的分享用户列表
     * @param deviceId
     */
    void queryDevShareUserList(String deviceId);

}
