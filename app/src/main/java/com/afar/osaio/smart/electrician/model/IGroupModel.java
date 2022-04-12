package com.afar.osaio.smart.electrician.model;

import com.afar.osaio.base.mvp.IBaseModel;
import com.tuya.smart.sdk.api.IGroupListener;
import com.tuya.smart.sdk.api.IResultCallback;

import java.util.Map;

/**
 * IGroupModel
 *
 * @author Administrator
 * @date 2019/3/29
 */
public interface IGroupModel extends IBaseModel {

    void sendCommand(String key, Object dps, IResultCallback callback);

    void sendCommands(Map<String, Object> dpsMap, IResultCallback callback);

    void registerListener(IGroupListener listener);

    void unRegisterListener();

    void release();

}
