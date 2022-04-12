package com.afar.osaio.smart.electrician.view;


import com.afar.osaio.base.mvp.IBaseView;

/**
 * IRenameDeviceView
 *
 * @author Administrator
 * @date 2019/3/18
 */
public interface IPowerStripRenameView extends IBaseView {

    void notifyRenameDeviceState(String msg);
}
