package com.afar.osaio.smart.electrician.model;

import com.afar.osaio.base.mvp.IBaseModel;
import com.tuya.smart.sdk.api.IResultCallback;

/**
 * INameDeviceModel
 *
 * @author Administrator
 * @date 2019/3/6
 */
public interface INameDeviceModel extends IBaseModel {

    void renameDevice(String name, IResultCallback callback);

    void removeDevice(IResultCallback callback);

    void release();

}
