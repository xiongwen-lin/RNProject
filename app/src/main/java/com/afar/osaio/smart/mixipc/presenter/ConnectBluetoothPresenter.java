package com.afar.osaio.smart.mixipc.presenter;

import com.afar.osaio.smart.mixipc.contract.ConnectBluetoothContract;

public class ConnectBluetoothPresenter implements ConnectBluetoothContract.Presenter {

    private ConnectBluetoothContract.View mTaskView;

    public ConnectBluetoothPresenter(ConnectBluetoothContract.View view) {
        mTaskView = view;
        mTaskView.setPresenter(this);
    }

    @Override
    public void destroy() {
        if (mTaskView != null) {
            mTaskView.setPresenter(null);
            mTaskView = null;
        }
    }
}
