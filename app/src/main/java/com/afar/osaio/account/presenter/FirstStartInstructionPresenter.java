package com.afar.osaio.account.presenter;

import com.afar.osaio.account.contract.FirstStartInstructionContract;

public class FirstStartInstructionPresenter implements FirstStartInstructionContract.Presenter {

    private FirstStartInstructionContract.View mTaskView;

    public FirstStartInstructionPresenter(FirstStartInstructionContract.View view) {
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
