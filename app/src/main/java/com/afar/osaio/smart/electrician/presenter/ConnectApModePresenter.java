package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.view.IConnectApModeView;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.ITuyaActivatorGetToken;

public class ConnectApModePresenter implements IConnectApModePresenter {

    private IConnectApModeView connectApModeView;

    public ConnectApModePresenter(IConnectApModeView view) {
        connectApModeView = view;
    }

    @Override
    public void getToken() {
        long homeId = FamilyManager.getInstance().getCurrentHomeId();

        if (homeId == 0 || homeId == FamilyManager.DEFAULT_HOME_ID) {
            return;
        }

        TuyaHomeSdk.getActivatorInstance().getActivatorToken(homeId, new ITuyaActivatorGetToken() {
            @Override
            public void onSuccess(String token) {
                if (connectApModeView != null) {
                    connectApModeView.onGetTokenSuccess(token);
                }
            }

            @Override
            public void onFailure(String s, String s1) {
            }
        });
    }
}
