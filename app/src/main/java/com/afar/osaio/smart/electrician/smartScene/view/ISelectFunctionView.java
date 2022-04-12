package com.afar.osaio.smart.electrician.smartScene.view;

import com.afar.osaio.base.mvp.IBaseView;
import com.tuya.smart.home.sdk.bean.scene.dev.TaskListBean;

import java.util.List;

public interface ISelectFunctionView extends IBaseView {
    void notifyGetDeviceTaskListSuccess(List<TaskListBean> result);

    void notifyGetDeviceTaskListFail(String errorMsg);
}
