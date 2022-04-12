package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

import java.util.List;

/**
 * ISingleDeviceSharePresenter
 *
 * @author Administrator
 * @date 2019/4/4
 */
public interface ISingleDeviceSharePresenter extends IBasePresenter {

    void shareDevices(long homeId, String countryCode, String uid, List<String> deviceIds);

    void getUidByAccount(String account);
}
