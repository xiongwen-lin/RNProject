package com.afar.osaio.message.model;

import com.afar.osaio.base.mvp.IBaseModel;
import com.afar.osaio.message.bean.MsgUnreadInfo;

import java.util.List;

import rx.Observable;

/**
 * Created by victor on 2018/7/9
 * Email is victor.qiao.0604@gmail.com
 */
public interface IMessageModel extends IBaseModel {

    Observable<MsgUnreadInfo> getMsgUnreadObservable(List<String> ids);

}
