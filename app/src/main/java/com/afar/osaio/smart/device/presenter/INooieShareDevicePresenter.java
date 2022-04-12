package com.afar.osaio.smart.device.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

import java.util.List;

/**
 * Created by victor on 2018/7/3
 * Email is victor.qiao.0604@gmail.com
 */
public interface INooieShareDevicePresenter extends IBasePresenter {

    void destroy();

    void getDeviceSharedUserList(String deviceId, int page, int perPage);

    boolean checkUser(String owner, String newSharer, List<String> oldSharers);

    void shareDevice(String deviceId, String userAccount);

    void deleteDeviceShared(int sharedId);
}
