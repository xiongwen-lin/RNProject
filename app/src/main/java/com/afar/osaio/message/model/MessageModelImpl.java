package com.afar.osaio.message.model;

import android.text.TextUtils;

import com.nooie.sdk.api.network.message.MessageService;
import com.afar.osaio.message.bean.DevMsgUnreadInfo;
import com.afar.osaio.message.bean.MsgUnreadInfo;
import com.nooie.common.utils.collection.ConvertUtil;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.MsgUnreadResult;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by victor on 2018/7/9
 * Email is victor.qiao.0604@gmail.com
 */
public class MessageModelImpl implements IMessageModel {

    @Override
    public Observable<MsgUnreadInfo> getMsgUnreadObservable(List<String> ids) {

        Observable<BaseResponse<MsgUnreadResult>> systemMsgUnreadObservable = MessageService.getService().getMsgUnread(ApiConstant.MSG_TYPE_SYS, "")
                .onErrorReturn(new Func1<Throwable, com.nooie.sdk.api.network.base.bean.BaseResponse<MsgUnreadResult>>() {
                    @Override
                    public com.nooie.sdk.api.network.base.bean.BaseResponse<MsgUnreadResult> call(Throwable throwable) {
                        return null;
                    }
                });

        Observable<BaseResponse<MsgUnreadResult>> devicesMsgUnreadObservable = MessageService.getService().getMsgUnread(ApiConstant.MSG_TYPE_DEVICE, ConvertUtil.convertListToString(ids))
                .onErrorReturn(new Func1<Throwable, com.nooie.sdk.api.network.base.bean.BaseResponse<MsgUnreadResult>>() {
                    @Override
                    public com.nooie.sdk.api.network.base.bean.BaseResponse<MsgUnreadResult> call(Throwable throwable) {
                        return null;
                    }
                });

        return Observable.zip(systemMsgUnreadObservable, devicesMsgUnreadObservable, new Func2<BaseResponse<MsgUnreadResult>, BaseResponse<MsgUnreadResult>, MsgUnreadInfo>() {
            @Override
            public MsgUnreadInfo call(BaseResponse<MsgUnreadResult> nooieResponse, BaseResponse<MsgUnreadResult> nooieDeviceResponse) {
                int nooieSysMsgCount = 0;
                if (nooieResponse != null && nooieResponse.getCode() == StateCode.SUCCESS.code && nooieResponse.getData() != null) {
                    nooieSysMsgCount = nooieResponse.getData().getNum();
                }

                List<MsgUnreadResult.MsgUnreadItem> msgUnreadItems = new ArrayList<>();
                if (nooieDeviceResponse != null && nooieDeviceResponse.getCode() == StateCode.SUCCESS.code && nooieResponse.getData() != null && nooieDeviceResponse.getData().getData() != null) {
                    msgUnreadItems.addAll(nooieDeviceResponse.getData().getData());
                }

                MsgUnreadInfo msgUnreadInfo = null;
                if (msgUnreadInfo == null) {
                    msgUnreadInfo = new MsgUnreadInfo(0, new ArrayList<DevMsgUnreadInfo>());
                } else if (msgUnreadInfo.getDevMsgUnreadInfos() == null) {
                    msgUnreadInfo.setDevMsgUnreadInfos(new ArrayList<DevMsgUnreadInfo>());
                }

                msgUnreadInfo.setSystemUnreadCount(msgUnreadInfo.getSystemUnreadCount() + nooieSysMsgCount);

                for (MsgUnreadResult.MsgUnreadItem msgUnreadItem : msgUnreadItems) {
                    if (!TextUtils.isEmpty(msgUnreadItem.getUuid())) {
                        DevMsgUnreadInfo devMsgUnreadInfo = new DevMsgUnreadInfo(msgUnreadItem.getUuid(), msgUnreadItem.getCount());
                        msgUnreadInfo.getDevMsgUnreadInfos().add(devMsgUnreadInfo);
                    }
                }

                return msgUnreadInfo;
            }
        });
    }
}
