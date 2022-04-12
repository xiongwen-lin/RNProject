package com.afar.osaio.smart.electrician.view;

import com.afar.osaio.base.mvp.IBaseView;

public interface IConnectApModeView extends IBaseView {
    void onGetTokenSuccess(String token);
    void onGetTokenFailed();
}
