package com.afar.osaio.smart.electrician.view;

import com.afar.osaio.base.mvp.IBaseView;

/**
 * INameDeviceView
 *
 * @author Administrator
 * @date 2019/3/6
 */
public interface INameDeviceView extends IBaseView {

    /**
     *
     */
    void onAddDevSuccess();

    /**
     *
     * @param error
     */
    void onAddDevFailed(String error);

}
