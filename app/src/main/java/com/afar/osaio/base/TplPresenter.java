package com.afar.osaio.base;

public class TplPresenter implements TplContract.Presenter {

    private TplContract.View mTaskView;

    public TplPresenter(TplContract.View view) {
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
