package com.afar.osaio.smart.electrician.view;


import com.afar.osaio.base.mvp.IBaseView;

/**
 * IManageDeviceView
 *
 * @author Administrator
 * @date 2019/3/21
 */
public interface ISelectShareDeviceView extends IBaseView {

    void notifySharedDeviceState(String msg);

    void addShareWithMemberIdSucccess(String msg);

    void addShareWithMemberIdFail(String error);

}
