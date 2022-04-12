package com.afar.osaio.base.view;

import com.afar.osaio.base.mvp.IBaseView;

/**
 * Created by victor on 2018/8/21
 * Email is victor.qiao.0604@gmail.com
 */
public interface IBaseActivityView extends IBaseView {
    void showLoadingDialog();

    void hideLoadingDialog();

    void notifyHandleShareDeviceSuccess();

    void notifyHandleShareDeviceFailed(int code);
}
