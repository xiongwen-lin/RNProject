package com.afar.osaio.message.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

import java.util.List;

/**
 * Created by victor on 2018/6/27
 * Email is victor.qiao.0604@gmail.com
 */
public interface ISystemMessagePresenter extends IBasePresenter {

    void deleteSystemMessages(boolean deleteAll, List<String> willDeleteSysMsgIds);

    /**
     * nooie smart load sys msg together
     * @param page
     * @param endTime
     * @param size
     */
    void loadSystemMessage(int page, long endTime, int size);

    /**
     * nooie smart update share msg status
     * @param msgId
     * @param shareId
     * @param status
     */
    void updateShareMsgState(int msgId, int shareId, int status);

    void updateMsgReadState(int msgId, int type);

    void setSystemMsgReadState();
}
