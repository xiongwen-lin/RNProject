package com.afar.osaio.smart.mixipc.presenter;

import com.afar.osaio.smart.mixipc.contract.ModifyCameraPasswordContract;

public class ModifyCameraPasswordPresenter implements ModifyCameraPasswordContract.Presenter {

    private ModifyCameraPasswordContract.View mTaskView;

    public ModifyCameraPasswordPresenter(ModifyCameraPasswordContract.View view) {
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
