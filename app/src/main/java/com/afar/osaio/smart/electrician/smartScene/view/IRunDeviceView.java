package com.afar.osaio.smart.electrician.smartScene.view;


import com.afar.osaio.base.mvp.IBaseView;
import com.afar.osaio.smart.electrician.bean.MixDeviceBean;

import java.util.List;

public interface IRunDeviceView extends IBaseView {

    void notifyGetTaskDevAndGoupListSuccess(List<MixDeviceBean> mixDeviceBeanList);
}
