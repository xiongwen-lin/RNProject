package com.afar.osaio.message.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;
import com.nooie.common.bean.DataEffect;

import java.util.List;

/**
 * Created by victor on 2018/7/3
 * Email is victor.qiao.0604@gmail.com
 */
public interface IDeviceMsgPresenter extends IBasePresenter {

    void destroy();

    void deleteDeviceMessages(List<String> msgIds);

    void deleteAllMessages(String deviceId);

    void deleteNooieMessageByIds(String deviceId, List<String> msgIds);

    /**
     * nooie smart merge data
     * @param page
     * @param deviceId
     * @param size
     */
    void loadWarningMessage(int page, String deviceId, int size);

    void getNooieDeviceSdCardSate(String user, String deviceId, boolean isMounted);

    /**
     * nooie smart
     * @param uid
     * @param deviceId
     * @param bindType
     */
    void checkNooieDeviceIsOpenCloud(String account, String uid, String deviceId, int bindType);

    void updateMsgReadState(int msgId, int type);

    void setDeviceMsgReadState(String deviceId);

    void checkIsOwnerDevice(String deviceId, DataEffect<Boolean> dataEffect);
}
