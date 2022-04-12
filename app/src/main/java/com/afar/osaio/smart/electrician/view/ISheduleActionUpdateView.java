package com.afar.osaio.smart.electrician.view;

public interface ISheduleActionUpdateView {

    void notifyUpdateTimerWithTaskSuccess();

    void notifyUpdateTimerWithTaskFail(String errorCode, String errorMessage);

}
