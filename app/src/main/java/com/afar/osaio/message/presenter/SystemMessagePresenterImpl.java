package com.afar.osaio.message.presenter;

import android.content.res.Resources;
import android.text.TextUtils;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.message.bean.HandleMessageType;
import com.afar.osaio.message.bean.SystemMessage;
import com.afar.osaio.message.bean.SystemMessageType;
import com.afar.osaio.message.view.ISystemMessageView;
import com.afar.osaio.smart.device.bean.ListDeviceItem;
import com.afar.osaio.smart.push.helper.NooiePushMsgHelper;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.collection.ConvertUtil;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.Message;
import com.nooie.sdk.api.network.base.bean.entity.MsgActiveInfo;
import com.nooie.sdk.api.network.base.bean.entity.MsgBatteryInfo;
import com.nooie.sdk.api.network.base.bean.entity.MsgDeviceBoundInfo;
import com.nooie.sdk.api.network.base.bean.entity.MsgFeedbackInfo;
import com.nooie.sdk.api.network.base.bean.entity.MsgFreeTrialStorageInfo;
import com.nooie.sdk.api.network.base.bean.entity.MsgInfo;
import com.nooie.sdk.api.network.base.bean.entity.MsgOrderInfo;
import com.nooie.sdk.api.network.base.bean.entity.MsgOtherLogin;
import com.nooie.sdk.api.network.base.bean.entity.MsgSubscribeInfo;
import com.nooie.sdk.api.network.base.bean.entity.MsgSysInfo;
import com.nooie.sdk.api.network.device.DeviceService;
import com.nooie.sdk.api.network.message.MessageService;
import com.tuya.smart.android.user.api.IBooleanCallback;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.ITuyaDataCallback;
import com.tuya.smart.sdk.bean.message.MessageBean;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by victor on 2018/11/12
 * Email is victor.qiao.0604@gmail.com
 */
public class SystemMessagePresenterImpl implements ISystemMessagePresenter {

    private WeakReference<ISystemMessageView> mSystemMessageView;
    private int mNextPage = 0;
    private List<String> mTuyaMsgIdList = new ArrayList<>();

    public SystemMessagePresenterImpl(ISystemMessageView view) {
        mSystemMessageView = new WeakReference<>((ISystemMessageView) view);
    }

    @Override
    public void loadSystemMessage(int page, long endTime, int size) {
        if (page == 0) {
            clearTuyaMsgIds();
            TuyaHomeSdk.getMessageInstance().getMessageList(new ITuyaDataCallback<List<MessageBean>>() {
                @Override
                public void onSuccess(List<MessageBean> result) {
                    /*
                    for (MessageBean messageBean : CollectionUtil.safeFor(result)) {
                        NooieLog.d("-->> debug System message getMsgType=" + messageBean.getMsgType() + " getMsgTypeContent=" + messageBean.getMsgTypeContent() + " getMsgContent=" + messageBean.getMsgContent() + " date=" + messageBean.getDateTime() + " time=" +messageBean.getTime());
                    }

                     */
                    addTuyaMsgIds(result);
                    getSystemMessage(page, size, convertTuyaMsgList(result));
                }

                @Override
                public void onError(String errorCode, String errorMessage) {
                    getSystemMessage(page, size, null);
                }
            });
        } else {
            getSystemMessage(page, size, null);
        }
    }

    public void getSystemMessage(int page, int size, List<SystemMessage> messageList) {
        getPlatformSystemMessage(page, size)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<SystemMessage>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(List<SystemMessage> messages) {
                        if (CollectionUtil.isNotEmpty(messageList)) {
                            messageList.addAll(CollectionUtil.safeFor(messages));
                            messages = messageList;
                        }
                        if (mSystemMessageView != null) {
                            mSystemMessageView.get().onLoadSystemMessage(messages);
                        }
                    }
                });
    }

    @Override
    public void deleteSystemMessages(boolean deleteAll, List<String> willDeleteSysMsgIds) {

        MessageService.getService().deleteAllSystemMsg()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mSystemMessageView.get() != null){
                            mSystemMessageView.get().notifyDeleteSystemMsgResult("");
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        deleteTuyaAllMessage(response != null && response.getCode() == StateCode.SUCCESS.code);
                    }
                });

    }

    @Override
    public void updateShareMsgState(int msgId, int shareId, int status) {
        DeviceService.getService().feedbackShare(msgId, shareId, status)
                .flatMap(new Func1<BaseResponse, Observable<BaseResponse>>() {
                    @Override
                    public Observable<BaseResponse> call(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.DEVICE_UNBINED.code) {
                            List<String> msgIds = new ArrayList<>();
                            msgIds.add(String.valueOf(msgId));
                            String deleteMsgIds = ConvertUtil.convertListToString(msgIds);
                            return MessageService.getService()
                                    .deleteMsgById(deleteMsgIds, ApiConstant.MSG_TYPE_SYS)
                                    .flatMap(new Func1<BaseResponse, Observable<BaseResponse>>() {
                                        @Override
                                        public Observable<BaseResponse> call(BaseResponse delResponse) {
                                            return Observable.just(response);
                                        }
                                    });
                        }
                        return Observable.just(response);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mSystemMessageView.get() != null) {
                            mSystemMessageView.get().onHandleFailed(ConstantValue.ERROR, StateCode.UNKNOWN.code);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mSystemMessageView.get() != null) {
                            mSystemMessageView.get().onHandleSuccess(HandleMessageType.TYPE_SYSTEM_SHARED);
                        } else if (response != null && mSystemMessageView.get() != null) {
                            mSystemMessageView.get().onHandleFailed(ConstantValue.ERROR, response.getCode());
                        } else if (mSystemMessageView.get() != null) {
                            mSystemMessageView.get().onHandleFailed(ConstantValue.ERROR, StateCode.UNKNOWN.code);
                        }
                    }
                });
    }

    @Override
    public void updateMsgReadState(int msgId, int type) {
        MessageService.getService().updateMsgStatus(String.valueOf(msgId), type)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                    }
                });
    }

    @Override
    public void setSystemMsgReadState() {
        MessageService.getService().setSystemAllMsgRead()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                    }
                });
    }

    private SystemMessage nooieMsgToSysMsg(Message message) {
        Resources res = NooieApplication.get().getResources();
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setDevicePlatform(ListDeviceItem.DEVICE_PLATFORM_NOOIE);
        systemMessage.setId(String.valueOf(message.getId()));
        systemMessage.setUtcTime(message.getTime() * 1000L);
        systemMessage.setDeviceId(message.getUuid());

        switch (message.getType()) {
            case ApiConstant.SYS_MSG_TYPE_SYS_NOTIFY: {
                MsgSysInfo msgSysInfo = message.getMsg() != null ? message.getMsg().obtainMsgSysInfo() : null;
                if (msgSysInfo == null) {
                    systemMessage = null;
                    break;
                }

                systemMessage.setTitle(res.getString(R.string.system_message));
                systemMessage.setContent(msgSysInfo.getContent());
                break;
            }
            case ApiConstant.SYS_MSG_TYPE_SYS_SHARE_TO_ME: {
                systemMessage.setMessageType(SystemMessageType.OTHER_SHARE_DEVICE_TO_ME);
                systemMessage.setTitle(res.getString(R.string.system_message_device_sharing));

                MsgInfo shareMsgInfo = message.getMsg() != null ? message.getMsg().obtainMsgInfo() : null;
                if (shareMsgInfo == null) {
                    systemMessage = null;
                    break;
                }
                systemMessage.setShareId(shareMsgInfo.getShare_id());
                String sender = shareMsgInfo.getAccount();
                String deviceAlias = shareMsgInfo.getDevice();
                String content = "";

                if (String.valueOf(ApiConstant.SYS_MSG_SHARE_STATUS_NORMAL).equalsIgnoreCase(shareMsgInfo.getShare_status())) {
                    content = sender + " " + res.getString(R.string.system_message_share) + " " + deviceAlias + " " + res.getString(R.string.system_message_to_me);
                    systemMessage.setShowAgreeReject(true);
                } else if (String.valueOf(ApiConstant.SYS_MSG_SHARE_STATUS_REJECT).equalsIgnoreCase(shareMsgInfo.getShare_status())) {
                    content = res.getString(R.string.system_message_i_reject) + " " + sender + " " + res.getString(R.string.system_message_shared) + " " + deviceAlias;
                    systemMessage.setShowAgreeReject(false);
                    systemMessage.setState(res.getString(R.string.system_message_rejected));
                } else if (String.valueOf(ApiConstant.SYS_MSG_SHARE_STATUS_ACCEPT).equalsIgnoreCase(shareMsgInfo.getShare_status())) {
                    content = res.getString(R.string.system_message_i_agree) + " " + sender + " " + res.getString(R.string.system_message_shared) + " " + deviceAlias;
                    systemMessage.setShowAgreeReject(false);
                    systemMessage.setState(res.getString(R.string.system_message_accepted));
                }

                systemMessage.setContent(content);
                break;
            }
            case ApiConstant.SYS_MSG_TYPE_SYS_SHARE_TO_OWNER: {
                systemMessage.setTitle(res.getString(R.string.system_message_device_sharing));

                MsgInfo shareMsgInfo = message.getMsg() != null ? message.getMsg().obtainMsgInfo() : null;
                if (shareMsgInfo == null) {
                    systemMessage = null;
                    break;
                }
                String sender = shareMsgInfo.getAccount();
                String deviceAlias = shareMsgInfo.getDevice();
                String content = "";

                if (String.valueOf(ApiConstant.SYS_MSG_SHARE_STATUS_NORMAL).equalsIgnoreCase(shareMsgInfo.getShare_status())) {
                } else if (String.valueOf(ApiConstant.SYS_MSG_SHARE_STATUS_REJECT).equalsIgnoreCase(shareMsgInfo.getShare_status())) {
                    systemMessage.setMessageType(SystemMessageType.OTHER_REJECT_MY_SHARE);
                    content = sender + " " + res.getString(R.string.system_message_reject_share) + " " + deviceAlias;
                    systemMessage.setShowAgreeReject(false);
                    systemMessage.setState(res.getString(R.string.system_message_rejected));
                } else if (String.valueOf(ApiConstant.SYS_MSG_SHARE_STATUS_ACCEPT).equalsIgnoreCase(shareMsgInfo.getShare_status())) {
                    systemMessage.setMessageType(SystemMessageType.OTHER_ACCEPT_MY_SHARE);
                    content = sender + " " + res.getString(R.string.system_message_agree_share) + " " + deviceAlias;
                    systemMessage.setShowAgreeReject(false);
                    systemMessage.setState(res.getString(R.string.system_message_accepted));
                }

                systemMessage.setContent(content);
                break;
            }
            case ApiConstant.SYS_MSG_TYPE_SYS_SHARE_DELETE: {
                systemMessage.setTitle(res.getString(R.string.system_message_device_sharing));
                systemMessage.setMessageType(SystemMessageType.OTHER_CANCEL_SHARE_TO_ME);

                MsgInfo shareMsgInfo = message.getMsg() != null ? message.getMsg().obtainMsgInfo() : null;
                if (shareMsgInfo == null) {
                    systemMessage = null;
                    break;
                }
                String sender = shareMsgInfo.getAccount();
                String deviceAlias = shareMsgInfo.getDevice();
                String content = "";

                content = sender + " " + res.getString(R.string.system_message_cancel_share) + " " + deviceAlias + " " + res.getString(R.string.system_message_to_me);
                systemMessage.setShowAgreeReject(false);
                systemMessage.setState(res.getString(R.string.system_message_canceled));

                systemMessage.setContent(content);
                break;
            }
            case ApiConstant.SYS_MSG_TYPE_SYS_SHARE_DEVICE_REMOVE: {
                systemMessage.setTitle(res.getString(R.string.system_message_device_sharing));
                systemMessage.setMessageType(SystemMessageType.OTHER_ACCEPT_THEN_CANCEL);

                MsgInfo shareMsgInfo = message.getMsg() != null ? message.getMsg().obtainMsgInfo() : null;
                if (shareMsgInfo == null) {
                    systemMessage = null;
                    break;
                }
                String sender = shareMsgInfo.getAccount();
                String deviceAlias = shareMsgInfo.getDevice();
                String content = "";

                content = sender + " " + res.getString(R.string.system_message_delete_share) + " " + deviceAlias;
                systemMessage.setShowAgreeReject(false);
                systemMessage.setState(res.getString(R.string.system_message_canceled));

                systemMessage.setContent(content);
                break;
            }
            case ApiConstant.SYS_MSG_TYPE_SYS_ORDER_FINISH: {
                systemMessage.setTitle(res.getString(R.string.system_message_pay_subscribe_complete_title));
                systemMessage.setShowAgreeReject(false);
                systemMessage.setState(null);

                MsgOrderInfo msgOrderInfo = message.getMsg() != null ? message.getMsg().obtainMsgOrderInfo() : null;
                if (msgOrderInfo == null) {
                    systemMessage = null;
                    break;
                }

                String deviceAlias = msgOrderInfo.getDevice();
                String orderId = msgOrderInfo.getOrder_id();
                String packName = msgOrderInfo.getPack_name();
                String content = String.format(res.getString(R.string.system_message_pay_nooie_subscribe_successfully_with_trial), deviceAlias, orderId, packName);

                systemMessage.setContent(content);
                break;
            }
            case ApiConstant.SYS_MSG_TYPE_SYS_FEEDBACK: {
                MsgFeedbackInfo msgFeedbackInfo = message.getMsg() != null ? message.getMsg().obtainMsgFeedbackInfo() : null;
                if (msgFeedbackInfo == null) {
                    systemMessage = null;
                    break;
                }
                String typeName = msgFeedbackInfo.getFeed_type_name();
                int status = msgFeedbackInfo.getFeedback_status();
                String content = !TextUtils.isEmpty(msgFeedbackInfo.getMsg()) ? msgFeedbackInfo.getMsg() : NooiePushMsgHelper.getFeedbackContent(NooieApplication.mCtx, status, msgFeedbackInfo.getContent());

                systemMessage.setTitle(res.getString(R.string.home_person_feedback));
                systemMessage.setContent(content);
                break;
            }
            case ApiConstant.SYS_MSG_TYPE_SYS_SUBSCRIBE_RENEWAL: {
                MsgSubscribeInfo msgSubscribeInfo = message.getMsg() != null ? message.getMsg().obtainMsgSubscribeInfo() : null;
                if (msgSubscribeInfo == null) {
                    systemMessage = null;
                    break;
                }
                systemMessage.setTitle(res.getString(R.string.system_message_pay_subscribe_charge_title));
                String content = String.format(res.getString(R.string.system_message_pay_nooie_subscribe_subscribe_with_trial), msgSubscribeInfo.getDevice());
                systemMessage.setContent(content);
                break;
            }
            case ApiConstant.SYS_MSG_TYPE_SYS_SUBSCRIBE_CANCEL: {
                MsgSubscribeInfo msgSubscribeInfo = message.getMsg() != null ? message.getMsg().obtainMsgSubscribeInfo() : null;
                if (msgSubscribeInfo == null) {
                    systemMessage = null;
                    break;
                }
                systemMessage.setTitle(res.getString(R.string.system_message_pay_subscribe_cancel_title));
                String content = String.format(res.getString(R.string.system_message_pay_nooie_subscribe_cancel_with_trial), msgSubscribeInfo.getDevice());
                systemMessage.setContent(content);
                break;
            }
            case ApiConstant.SYS_MSG_TYPE_SYS_SUBSCRIBE_EXPIRED: {
                MsgSubscribeInfo msgSubscribeInfo = message.getMsg() != null ? message.getMsg().obtainMsgSubscribeInfo() : null;
                if (msgSubscribeInfo == null) {
                    systemMessage = null;
                    break;
                }
                systemMessage.setTitle(res.getString(R.string.system_message_pay_subscribe_arrears_title));
                String content = String.format(res.getString(R.string.system_message_pay_nooie_subscribe_expired_with_trial), msgSubscribeInfo.getDevice());
                systemMessage.setContent(content);
                break;
            }
            case ApiConstant.SYS_MSG_TYPE_SYS_ACTIVE: {
                MsgActiveInfo msgActiveInfo = message.getMsg() != null ? message.getMsg().obtainMsgActiveInfo() : null;
                if (msgActiveInfo == null) {
                    systemMessage = null;
                    break;
                }
                systemMessage.setTitle(res.getString(R.string.home_ad_dialog_title));
                systemMessage.setContent(msgActiveInfo.getMsg());
                systemMessage.setMessage(message);
                break;
            }
            case ApiConstant.SYS_MSG_TYPE_SYS_DEVICE_BOUND: {
                MsgDeviceBoundInfo msgDeviceBoundInfo = message.getMsg() != null ? message.getMsg().obtainMsgDeviceBoundInfo() : null;
                if (msgDeviceBoundInfo == null) {
                    systemMessage = null;
                    break;
                }
                String content = String.format(res.getString(R.string.system_message_device_bound_by_other), msgDeviceBoundInfo.getUuid(), msgDeviceBoundInfo.getPuuid());//String.format(res.getString(R.string.system_message_device_bound_by_other), msgDeviceBoundInfo.getUuid(), msgDeviceBoundInfo.getPuuid(), msgDeviceBoundInfo.getAccount());
                systemMessage.setTitle(res.getString(R.string.system_message));
                systemMessage.setContent(content);
                systemMessage.setMessage(message);
                break;
            }
            case ApiConstant.SYS_MSG_TYPE_SYS_LOW_BATTERY: {
                MsgBatteryInfo msgBatteryInfo = message.getMsg() != null ? message.getMsg().obtainMsgBatteryInfo() : null;
                if (msgBatteryInfo == null) {
                    systemMessage = null;
                    break;
                }
                String content = String.format(res.getString(R.string.nooie_play_low_battery_content), msgBatteryInfo.getName());
                systemMessage.setTitle(res.getString(R.string.nooie_play_low_battery_title));
                systemMessage.setContent(content);
                systemMessage.setMessage(message);
                break;
            }
            case ApiConstant.SYS_MSG_TYPE_SYS_FREE_TRIAL_STORAGE: {
                MsgFreeTrialStorageInfo msgFreeTrialStorageInfo = message.getMsg() != null ? message.getMsg().obtainMsgFreeTrialStorageInfo() : null;
                if (msgFreeTrialStorageInfo == null) {
                    systemMessage = null;
                    break;
                }
                String content = String.format(res.getString(R.string.system_message_free_trial_storage), DateTimeUtil.getUtcTimeString(msgFreeTrialStorageInfo.getExpire_date() * 1000L, DateTimeUtil.PATTERN_YMD));
                systemMessage.setTitle(res.getString(R.string.system_message));
                systemMessage.setContent(content);
                systemMessage.setMessage(message);
                break;
            }
            case ApiConstant.SYS_MSG_TYPE_SYS_OTHER_LOGIN: {
                MsgOtherLogin msgOtherLogin = message.getMsg() != null ? message.getMsg().obtainMsgOtherLogin() : null;
                if (msgOtherLogin == null || TextUtils.isEmpty(msgOtherLogin.getPhone_brand())) {
                    systemMessage = null;
                    break;
                }
                String content = res.getString(R.string.system_message_other_place_login);
                systemMessage.setTitle(res.getString(R.string.system_message));
                systemMessage.setContent(content);
                systemMessage.setMessage(message);
                break;
            }
            default: {
                systemMessage = null;
            }
        }

        return systemMessage;
    }

    private SystemMessage convertTuyaMsg(MessageBean message) {
        Resources res = NooieApplication.get().getResources();
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setDevicePlatform(ListDeviceItem.DEVICE_PLATFORM_NOOIE);
        systemMessage.setId(String.valueOf(message.getId()));
        systemMessage.setUtcTime(message.getTime() * 1000L);
        systemMessage.setTitle(res.getString(R.string.system_message));
        systemMessage.setContent(message.getMsgContent());
        return systemMessage;
    }

    private List<SystemMessage> convertTuyaMsgList(List<MessageBean> messageList) {
        List<SystemMessage> result = new ArrayList<>();
        if (CollectionUtil.isEmpty(messageList)) {
            return result;
        }
        for (MessageBean message : CollectionUtil.safeFor(messageList)) {
            result.add(convertTuyaMsg(message));
        }
        return result;
    }

    private Observable<List<SystemMessage>> getPlatformSystemMessage(int page, int size) {

        if (page == 0) {
            mNextPage = 0;
        }

        Observable<List<SystemMessage>> sysMsgObservable = MessageService.getService().getSystemMsg(mNextPage, size)
                .flatMap(new Func1<BaseResponse<List<Message>>, Observable<List<SystemMessage>>>() {
                    @Override
                    public Observable<List<SystemMessage>> call(BaseResponse<List<Message>> response) {
                        List<SystemMessage> nooieSystemMsgs = new ArrayList<>();
                        if (response != null && response.getCode() == StateCode.SUCCESS.code) {

                            if (response.getData() == null || response.getData().size() == 0) {
                                return Observable.just(nooieSystemMsgs);
                            }

                            int lastMessageIndex = response.getData().size() - 1;
                            mNextPage = response.getData().get(lastMessageIndex).getTime();

                            for(Message msg : CollectionUtil.safeFor(response.getData())) {
                                if (msg != null && nooieMsgToSysMsg(msg) != null) {
                                    nooieSystemMsgs.add(nooieMsgToSysMsg(msg));
                                }
                            }
                        }
                        return Observable.just(nooieSystemMsgs);
                    }
                });

        return sysMsgObservable;

    }

    private void addTuyaMsgIds(List<MessageBean> messageBeanList) {
        if (CollectionUtil.isEmpty(messageBeanList)) {
            return;
        }
        if (mTuyaMsgIdList == null) {
            mTuyaMsgIdList = new ArrayList<>();
        }
        mTuyaMsgIdList.clear();
        for (MessageBean messageBean : messageBeanList) {
            if (messageBean != null && !TextUtils.isEmpty(messageBean.getId())) {
                mTuyaMsgIdList.add(messageBean.getId());
            }
        }
    }

    private void clearTuyaMsgIds() {
        if (mTuyaMsgIdList != null) {
            mTuyaMsgIdList.clear();
        }
    }

    private void deleteTuyaAllMessage(boolean isSysMsgDeleteSuccess) {
        if (CollectionUtil.isEmpty(mTuyaMsgIdList)) {
            if (mSystemMessageView.get() != null) {
                mSystemMessageView.get().notifyDeleteSystemMsgResult(isSysMsgDeleteSuccess ? ConstantValue.SUCCESS : ConstantValue.ERROR);
            }
            return;
        }
        TuyaHomeSdk.getMessageInstance().deleteMessages(mTuyaMsgIdList, new IBooleanCallback() {
            @Override
            public void onSuccess() {
                if (mSystemMessageView.get() != null) {
                    mSystemMessageView.get().notifyDeleteSystemMsgResult(ConstantValue.SUCCESS);
                }
            }

            @Override
            public void onError(String code, String error) {
                if (mSystemMessageView.get() != null) {
                    mSystemMessageView.get().notifyDeleteSystemMsgResult(isSysMsgDeleteSuccess ? ConstantValue.SUCCESS : ConstantValue.ERROR);
                }
            }
        });
    }
}
