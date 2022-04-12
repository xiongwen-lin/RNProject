package com.afar.osaio.message.view;

import com.afar.osaio.base.mvp.IBaseView;
import com.afar.osaio.message.bean.MsgUnreadInfo;

/**
 * Created by victor on 2018/7/9
 * Email is victor.qiao.0604@gmail.com
 */
public interface IMessageView extends IBaseView {
    /**
     * load unread message information
     *
     * @param message
     */
    void notifyGetUnreadMsgFailed(String message);

    void notifyGetUnreadMsgSuccess(MsgUnreadInfo info);

    void showLoadingDialog();

    void hideLoadingDialog();

    void notifyDeleteMsgSuccess();
    void notifyDeleteMsgFailed(String msg);
}
