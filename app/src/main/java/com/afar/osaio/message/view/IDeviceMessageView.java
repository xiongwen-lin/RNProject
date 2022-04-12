package com.afar.osaio.message.view;

import androidx.annotation.NonNull;

import com.nooie.sdk.api.network.base.bean.entity.DeviceMessage;
import com.afar.osaio.base.mvp.IBaseView;
import com.nooie.common.bean.DataEffect;

import java.util.List;

/**
 * Created by victor on 2018/7/3
 * Email is victor.qiao.0604@gmail.com
 */
public interface IDeviceMessageView extends IBaseView {
    void onLoadWarningMessage(String deviceId, @NonNull List<DeviceMessage> messages, boolean openCloud);

    void onHandleFailed(String deviceId, String message);

    void notifyHaveSDCardResult(int code, String deviceId, String result, boolean haveSDCard);

    void notifyDeleteMsgResult(String result);

    void notifyDeleteAllMsgResult(String deviceId, String result);

    void notifyCheckDataEffectResult(String deviceId, String key, DataEffect dataEffect);

    void onCheckPackInfo(String deviceId, boolean isOpenCloud, int status, boolean isEvent, int storageDayNum);
}
