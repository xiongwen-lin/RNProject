package com.afar.osaio.smart.device.view;

import com.afar.osaio.base.mvp.IBaseView;
import com.nooie.sdk.api.network.base.bean.entity.DeviceRelationResult;

/**
 * Created by victor on 2018/7/3
 * Email is victor.qiao.0604@gmail.com
 */
public interface INooieShareDeviceView extends IBaseView {
    void onShowShareToUsers(DeviceRelationResult result);

    void onGetShareToUsersError(String message);

    void onShowUserIsYourself(String userAccount);

    void onUserIsYourSharer(String userAccount);

    void onShareDevSuccess(String message);

    void onShareDevFailed(int code, String shareAccount, int num);

    void notifyDeleteSharedResult(String result);

    void showLoadingDialog();

    void hideLoadingDialog();

}
