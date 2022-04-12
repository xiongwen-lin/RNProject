package com.afar.osaio.smart.electrician.presenter;


import com.afar.osaio.base.mvp.IBasePresenter;

import java.util.List;

/**
 * ISelectShareDevicePresenter
 *
 * @author Administrator
 * @date 2019/3/23
 */
public interface ISelectShareDevicePresenter extends IBasePresenter {

    void addShareWithHomeId(long homeId, String countryCode, String uid, List<String> deviceIds);

    void addShareWithMemberId(long memberId, List<String> deviceIds);

}
