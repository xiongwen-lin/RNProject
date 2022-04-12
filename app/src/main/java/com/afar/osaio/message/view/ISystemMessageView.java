package com.afar.osaio.message.view;

import androidx.annotation.NonNull;

import com.afar.osaio.base.mvp.IBaseView;
import com.afar.osaio.message.bean.HandleMessageType;
import com.afar.osaio.message.bean.SystemMessage;

import java.util.List;

/**
 * Created by victor on 2018/7/9
 * Email is victor.qiao.0604@gmail.com
 */
public interface ISystemMessageView extends IBaseView {
    void onLoadSystemMessage(@NonNull List<SystemMessage> messages);

    void notifyDeleteSystemMsgResult(String result);

    void onHandleSuccess(HandleMessageType type);

    void onHandleFailed(String message, int code);
}
