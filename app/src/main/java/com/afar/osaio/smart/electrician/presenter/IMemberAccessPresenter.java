package com.afar.osaio.smart.electrician.presenter;


import com.afar.osaio.base.mvp.IBasePresenter;

/**
 * IMemberPresenter
 *
 * @author jiangzt
 * @date 2019/4/25
 */
public interface IMemberAccessPresenter extends IBasePresenter {

    void loadUserShareInfo(long memberId);

    void removeDevice(long memberId, String deviceId);

}
