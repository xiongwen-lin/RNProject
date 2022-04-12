package com.afar.osaio.smart.home.presenter;

import android.os.Bundle;
import android.text.TextUtils;

import com.afar.osaio.account.model.PlatformUpgradeModel;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.message.bean.MsgUnreadInfo;
import com.afar.osaio.message.model.IMessageModel;
import com.afar.osaio.message.model.MessageModelImpl;
import com.afar.osaio.smart.push.helper.NooiePushMsgHelper;
import com.afar.osaio.util.CommonUtil;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.base.GlobalData;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.configure.PhoneUtil;
import com.nooie.common.utils.file.FileUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.network.NetworkUtil;
import com.nooie.common.utils.tool.ShellUtil;
import com.nooie.sdk.api.network.account.AccountService;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.afar.osaio.smart.home.contract.HomeContract;
import com.nooie.sdk.api.network.base.bean.entity.AppVersionResult;
import com.nooie.sdk.api.network.base.bean.entity.MsgActiveInfo;
import com.nooie.sdk.api.network.base.bean.entity.UserInfoResult;
import com.nooie.sdk.api.network.message.MessageService;
import com.nooie.sdk.api.network.setting.SettingService;
import com.nooie.sdk.bean.IpcType;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.db.dao.DeviceHardVersionService;
import com.nooie.sdk.db.dao.UserRegionService;
import com.nooie.sdk.db.entity.CountryCodeEntity;
import com.nooie.sdk.processor.user.UserApi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class HomePresenter implements HomeContract.Presenter {

    private HomeContract.View mTasksView;

    private boolean mIsUploadCrashLog = true;
    private PlatformUpgradeModel mPlatformUpgradeModel;
    private Subscription mStartGetAllApDeviceHardVersionTask = null;
    private IMessageModel mMessageModel;

    public HomePresenter(HomeContract.View tasksView) {
        this.mTasksView = tasksView;
        this.mTasksView.setPresenter(this);
        mPlatformUpgradeModel = new PlatformUpgradeModel();
        mMessageModel = new MessageModelImpl();
    }

    @Override
    public void destroy() {
        if (mTasksView != null) {
            mTasksView.setPresenter(null);
            mTasksView = null;
            mPlatformUpgradeModel = null;
        }
    }

    /**
     *nooie start 上报用户信息
     */
    @Override
    public void reportUserInfo(String account, String password, float zone, String country, int type, String nickname, String photo, String phoneCode, int deviceType, int pushType, String pushToken, String appVersion, String appVersionCode, String phoneModel, String phoneBrand, String phoneVersion, String phoneScreen, String language, String packageName) {
        Observable.just(1)
                .flatMap(new Func1<Integer, Observable<BaseResponse>>() {
                    @Override
                    public Observable<BaseResponse> call(Integer integer) {
                        CountryCodeEntity countryCodeEntity = UserRegionService.getInstance().getUserRegionByAccount(account);
                        String countryCode = countryCodeEntity != null && !TextUtils.isEmpty(countryCodeEntity.getCountryCode()) ? countryCodeEntity.getCountryCode() : country;
                        return AccountService.getService().putUserInfo(pushType, deviceType, pushToken, phoneCode, countryCode, zone, nickname, photo, appVersion, appVersionCode, phoneModel, phoneBrand, phoneVersion, phoneScreen, language, packageName, PhoneUtil.getPhoneName(NooieApplication.mCtx));
                    }
                })
//        AccountService.getService().putUserInfo(pushType, deviceType, pushToken, phoneCode, country, zone, nickname, photo, appVersion, appVersionCode, phoneModel, phoneBrand, phoneVersion, phoneScreen, language, packageName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTasksView != null) {
                            mTasksView.notifyReportUserInfoResult(ConstantValue.ERROR);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && NooiePushMsgHelper.isPushTokenValid(pushToken)) {
//                            GlobalPrefs.getPreferences(NooieApplication.mCtx).setPushToken(pushToken);
                            GlobalData.getInstance().updatePushToken(UserApi.getInstance().createPushTokenData(pushToken));
                        }
                        if (mTasksView != null) {
                            mTasksView.notifyReportUserInfoResult(response != null && response.getCode() == StateCode.SUCCESS.code ? ConstantValue.SUCCESS : ConstantValue.ERROR);
                        }
                        //上报信息后再加载确保uid和token有效
                    }
                });
    }

    @Override
    public void getLastActiveMsg() {
        MessageService.getService().getLastActiveMsg()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<MsgActiveInfo>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTasksView != null) {
                            mTasksView.onGetLastActiveMsgResult(ConstantValue.ERROR, null);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<MsgActiveInfo> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mTasksView != null) {
                            mTasksView.onGetLastActiveMsgResult(ConstantValue.SUCCESS, response.getData());
                        } else if (mTasksView != null) {
                            mTasksView.onGetLastActiveMsgResult(ConstantValue.ERROR, null);
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
    public void checkNetworkStatus() {
        Observable.just(1)
                .flatMap(new Func1<Integer, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Integer value) {
                        CommonUtil.isNetworkValid(NooieApplication.mCtx);
                        boolean isNetworkUsable = false;
                        boolean isConnected = NetworkUtil.isConnected(NooieApplication.mCtx);
                        if (isConnected) {
                            List<String> addresses = new ArrayList<>();
                            addresses.add("www.baidu.com");
                            addresses.add("www.google.com");
                            try {
                                for (String address : CollectionUtil.safeFor(addresses)) {

                                        ShellUtil.CommandResult result = NetworkUtil.pingAddressResult(address, 2);
                                        if (result != null && result.result == 0) {
                                            isNetworkUsable = true;
                                            break;
                                        }
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                        }
                        NooieLog.d("-->> HomePresenter checkNetworkStatus isConnected=" + isConnected + " isNetworkUsable=" + isNetworkUsable);
                        return Observable.just(isNetworkUsable);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTasksView != null) {
                            mTasksView.onCheckNetworkStatus(ConstantValue.ERROR, false);
                        }
                    }

                    @Override
                    public void onNext(Boolean isNetworkUsable) {
                        if (mTasksView != null) {
                            mTasksView.onCheckNetworkStatus(ConstantValue.SUCCESS, isNetworkUsable);
                        }
                    }
                });
    }

    @Override
    public void getUserInfo(final String uid, final String account) {
        AccountService.getService().getUserInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .delay(5000, TimeUnit.MILLISECONDS)
                .flatMap(new Func1<BaseResponse<UserInfoResult>, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(BaseResponse<UserInfoResult> response) {
                        boolean isClearLogFile = !(response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null && response.getData().getIsdebug() != ConstantValue.NOOIE_DEVELOP_MODE_DEFAULT);
                        try {
                            List<String> logFilesForClear = mPlatformUpgradeModel.getCrashLogForClearing(isClearLogFile);
                            for (String logFile : CollectionUtil.safeFor(logFilesForClear)) {
                                FileUtil.deleteFile(logFile);
                            }
                        } catch (Exception e) {
                            NooieLog.printStackTrace(e);
                        }
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            GlobalData.getInstance().updateDebugMode(response.getData().getIsdebug());
                            boolean uploadEnable = mIsUploadCrashLog && response.getData().getIsdebug() != ConstantValue.NOOIE_DEVELOP_MODE_DEFAULT && NetworkUtil.isWifiConnected(NooieApplication.mCtx);
                            mIsUploadCrashLog = false;
                            return uploadEnable ? mPlatformUpgradeModel.reportNooieLog(uid, account) : Observable.just(false);
                        }
                        return Observable.just(false);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Boolean result) {
                    }
                });
    }

    @Override
    public void clearLogFile() {
        Observable.just(1)
                .flatMap(new Func1<Integer, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Integer integer) {
                        try {
                            List<String> logFilesForClear = mPlatformUpgradeModel.getCrashLogForClearing(true);
                            for (String logFile : CollectionUtil.safeFor(logFilesForClear)) {
                                FileUtil.deleteFile(logFile);
                            }
                        } catch (Exception e) {
                            NooieLog.printStackTrace(e);
                        }
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Boolean result) {
                    }
                });
    }

    public void getAllApDeviceHardVersion() {
        stopGetAllApDeviceHardVersion();
        List<String> apDeviceModelList = new ArrayList<>();
        apDeviceModelList.add(IpcType.HC320_TYPE);
        apDeviceModelList.add(IpcType.MC120_TYPE);
        mStartGetAllApDeviceHardVersionTask = Observable.from(apDeviceModelList)
                .flatMap(new Func1<String, Observable<BaseResponse<AppVersionResult>>>() {
                    @Override
                    public Observable<BaseResponse<AppVersionResult>> call(String model) {
                        if (TextUtils.isEmpty(model)) {
                            return Observable.just(null);
                        }
                        return getDeviceHardVersion(model);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<AppVersionResult>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(BaseResponse<AppVersionResult> response) {
                    }
                });
    }

    public void stopGetAllApDeviceHardVersion() {
        if (mStartGetAllApDeviceHardVersionTask != null && !mStartGetAllApDeviceHardVersionTask.isUnsubscribed()) {
            mStartGetAllApDeviceHardVersionTask.unsubscribe();
            mStartGetAllApDeviceHardVersionTask = null;
        }
    }

    @Override
    public void loadMsgUnread(List<String> ids, boolean isFirstLaunch) {
        mMessageModel.getMsgUnreadObservable(ids)
                .delay((isFirstLaunch ? 1000 : 0), TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<MsgUnreadInfo>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTasksView != null) {
                            mTasksView.onGetUnreadMsgSuccess(SDKConstant.ERROR, null);
                        }
                    }

                    @Override
                    public void onNext(MsgUnreadInfo msgUnreadInfo) {
                        if (mTasksView != null) {
                            mTasksView.onGetUnreadMsgSuccess(SDKConstant.SUCCESS, msgUnreadInfo);
                        }
                    }
                });
    }

    private Observable<BaseResponse<AppVersionResult>> getDeviceHardVersion(String model) {
        return SettingService.getService().getHardVersion(model)
                .flatMap(new Func1<BaseResponse<AppVersionResult>, Observable<BaseResponse<AppVersionResult>>>() {
                    @Override
                    public Observable<BaseResponse<AppVersionResult>> call(BaseResponse<AppVersionResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            NooieLog.d("-->> debug HomePresenter mode=" + response.getData().getModel() + " version=" + response.getData().getVersion_code());
                            updateDeviceHardVersion(response.getData());
                        }
                        return Observable.just(response);
                    }
                })
                .onErrorReturn(new Func1<Throwable, BaseResponse<AppVersionResult>>() {
                    @Override
                    public BaseResponse<AppVersionResult> call(Throwable throwable) {
                        return null;
                    }
                });
    }

    private void updateDeviceHardVersion(AppVersionResult versionResult) {
        if (versionResult == null || TextUtils.isEmpty(versionResult.getModel()) || TextUtils.isEmpty(versionResult.getVersion_code())) {
            return;
        }
        Bundle data = new Bundle();
        data.putString(DeviceHardVersionService.KEY_TYPE, versionResult.getType());
        data.putString(DeviceHardVersionService.KEY_MODEL, versionResult.getModel());
        data.putString(DeviceHardVersionService.KEY_VERSION_CODE, versionResult.getVersion_code());
        data.putString(DeviceHardVersionService.KEY_KEY, versionResult.getKey());
        data.putString(DeviceHardVersionService.KEY_MD5, versionResult.getMd5());
        data.putString(DeviceHardVersionService.KEY_LOG, versionResult.getLog());
        DeviceHardVersionService.getInstance().addDeviceHardVersion(data);
    }
}
