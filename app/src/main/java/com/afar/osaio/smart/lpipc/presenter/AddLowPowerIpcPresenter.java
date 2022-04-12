package com.afar.osaio.smart.lpipc.presenter;

import com.afar.osaio.smart.lpipc.contract.AddLowPowerIpcContract;

public class AddLowPowerIpcPresenter implements AddLowPowerIpcContract.Presenter {

    private AddLowPowerIpcContract.View mTaskView;

    public AddLowPowerIpcPresenter(AddLowPowerIpcContract.View view) {
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
