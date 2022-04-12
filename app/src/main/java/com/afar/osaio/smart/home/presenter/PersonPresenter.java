package com.afar.osaio.smart.home.presenter;

import android.text.TextUtils;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.message.bean.MsgUnreadInfo;
import com.afar.osaio.message.model.IMessageModel;
import com.afar.osaio.message.model.MessageModelImpl;
import com.nooie.common.base.GlobalData;
import com.nooie.common.bean.CConstant;
import com.nooie.common.utils.file.FileUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.account.AccountService;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.GatewayDevice;
import com.nooie.sdk.api.network.base.bean.entity.UserInfoResult;
import com.afar.osaio.smart.cache.UserInfoCache;
import com.afar.osaio.smart.home.contract.PersonContract;
import com.afar.osaio.util.ConstantValue;
import com.nooie.sdk.api.network.base.core.NetConfigure;
import com.nooie.sdk.api.network.device.DeviceService;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.processor.cloud.CloudManager;
import com.nooie.sdk.processor.user.UserApi;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class PersonPresenter implements PersonContract.Presenter {

    private static final int GET_USER_INFO_FAIL = 0;
    private static final int GET_USER_INFO_SUCCESS = 1;
    private static final int GET_USER_INFO_SUCCESS_WITH_UPDATE_PHONE = 2;

    private PersonContract.View mTasksView;
    private IMessageModel mMessageModel;
    private boolean mIsDownloadPortrait = false;

    public PersonPresenter(PersonContract.View view) {
        this.mTasksView = view;
        this.mTasksView.setPresenter(this);
        mMessageModel = new MessageModelImpl();
    }

    @Override
    public void destroy() {
        if (mTasksView != null) {
            mTasksView.setPresenter(null);
            mTasksView = null;
        }
    }

    @Override
    public void logout() {
        UserApi.getInstance().logout(false, new Observer<BaseResponse>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                if (mTasksView != null) {
                    mTasksView.notifyLogoutResult("");
                }
            }

            @Override
            public void onNext(BaseResponse response) {
                GlobalData.getInstance().log("PersonPresenter logout");
                if (response != null && response.getCode() == StateCode.SUCCESS.code && mTasksView != null) {
                    mTasksView.notifyLogoutResult(ConstantValue.SUCCESS);
                } else if (mTasksView != null) {
                    mTasksView.notifyLogoutResult("");
                }
            }
        });
    }

    private void tryLogout() {
        UserApi.getInstance().logout(false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTasksView != null) {
                            mTasksView.notifyLogoutResult("");
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        GlobalData.getInstance().log("PersonPresenter tryLogout");
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mTasksView != null) {
                            mTasksView.notifyLogoutResult(ConstantValue.SUCCESS);
                        } else if (mTasksView != null) {
                            mTasksView.notifyLogoutResult("");
                        }
                    }
                });
    }

    @Override
    public void getUserInfo(String userId, final String userName, String portraitPath) {
        NooieLog.d("-->> debug PersonPresenter getUserInfo: 1001 userId" + userId + " userName=" + userName + " portraitPath=" + portraitPath);
        AccountService.getService().getUserInfo()
                .flatMap(new Func1<BaseResponse<UserInfoResult>, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(BaseResponse<UserInfoResult> response) {
                        NooieLog.d("-->> debug PersonPresenter getUserInfo: 1002");
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            NooieLog.d("-->> debug PersonPresenter getUserInfo: 1003");
                            int result = UserInfoCache.getInstance().getUserInfo() == null || TextUtils.isEmpty(UserInfoCache.getInstance().getUserInfo().getPhoto()) || !UserInfoCache.getInstance().getUserInfo().getPhoto().equalsIgnoreCase(response.getData().getPhoto()) ? GET_USER_INFO_SUCCESS_WITH_UPDATE_PHONE : GET_USER_INFO_SUCCESS;
                            UserInfoCache.getInstance().setUserInfo(response.getData());
                            return Observable.just(result);
                        }
                        return Observable.just(GET_USER_INFO_FAIL);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        NooieLog.d("-->> debug PersonPresenter getUserInfo: 1007");
                        if (mTasksView != null) {
                            mTasksView.notifyGetUserInfoResult("");
                        }
                    }

                    @Override
                    public void onNext(Integer result) {
                        NooieLog.d("-->> debug PersonPresenter getUserInfo: 1004 result=" + result);
                        if (result == GET_USER_INFO_SUCCESS_WITH_UPDATE_PHONE) {
                            NooieLog.d("-->> debug PersonPresenter getUserInfo: 1005");
                            downloadPortrait(userId, userName, portraitPath);
                        }
                        if (mTasksView != null) {
                            NooieLog.d("-->> debug PersonPresenter getUserInfo: 1006");
                            mTasksView.notifyGetUserInfoResult((result == GET_USER_INFO_SUCCESS || result == GET_USER_INFO_SUCCESS_WITH_UPDATE_PHONE) ? ConstantValue.SUCCESS : "");
                        }
                    }
                });
    }

    @Override
    public void changeUserName(final String name) {
        AccountService.getService().updateNickname(name)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTasksView != null) {
                            mTasksView.notifyChangeUserNameResult(ConstantValue.ERROR);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mTasksView != null) {
                            UserInfoCache.getInstance().updateNickName(name);
                            mTasksView.notifyChangeUserNameResult(ConstantValue.SUCCESS);
                        } else if (mTasksView != null) {
                            mTasksView.notifyChangeUserNameResult(ConstantValue.ERROR);
                        }
                    }
                });
    }

    @Override
    public void uploadPictures(final String userid, final String username, final String photoPath) {
        NooieLog.d("-->> debug PersonPresenter uploadPictures: 1000 userId=" + userid + " " + username + " " + photoPath);
        CloudManager.getInstance().uploadPortrait(userid, username, photoPath, FileUtil.getPersonPortrait(NooieApplication.mCtx, username).getPath(), FileUtil.getPersonPortraitInPrivate(NooieApplication.mCtx, username).getPath(), new Observer<Boolean>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                NooieLog.d("-->> debug PersonPresenter uploadPictures: 1004");
                if (mTasksView != null) {
                    mTasksView.notifyRefreshUserPortrait(ConstantValue.ERROR, true);
                }
            }

            @Override
            public void onNext(Boolean result) {
                NooieLog.d("-->> PersonPresenter uploadPictures 1001 result=" + result);
                if (result) {
                    updatePhotoName();
                } else if (mTasksView != null) {
                    NooieLog.d("-->> debug PersonPresenter uploadPictures: 1003");
                    mTasksView.notifyRefreshUserPortrait(ConstantValue.ERROR, true);
                }
            }
        });
    }

    private void updatePhotoName() {
        NooieLog.d("-->> debug PersonPresenter uploadPictures: 1005");
        AccountService.getService().updateUserPhoto(createPhotoName())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        NooieLog.d("-->> debug PersonPresenter uploadPictures: 1009");
                        if (mTasksView != null) {
                            mTasksView.notifyRefreshUserPortrait(ConstantValue.ERROR, true);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        NooieLog.d("-->> debug PersonPresenter uploadPictures: 1006");
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mTasksView != null) {
                            NooieLog.d("-->> debug PersonPresenter uploadPictures: 1007");
                            mTasksView.notifyRefreshUserPortrait(ConstantValue.SUCCESS, true);
                        } else if (mTasksView != null) {
                            NooieLog.d("-->> debug PersonPresenter uploadPictures: 1008");
                            mTasksView.notifyRefreshUserPortrait(ConstantValue.ERROR, true);
                        }
                    }
                });
    }

    private String createPhotoName() {
        StringBuilder photoNameSb = new StringBuilder();
        photoNameSb.append(NetConfigure.getInstance().getAppId());
        photoNameSb.append("/");
        photoNameSb.append(GlobalData.getInstance().getUid());
        photoNameSb.append(CConstant.UNDER_LINE);
        photoNameSb.append(System.currentTimeMillis());
        photoNameSb.append(CConstant.PERIOD);
        photoNameSb.append(CConstant.MEDIA_TYPE_JPEG);
        return photoNameSb.toString();
    }

    @Override
    public void downloadPortrait(final String userId, final String username, String portraitPath) {
        if (mIsDownloadPortrait) {
            return;
        }
        setDownloadPortraitState(true);
        CloudManager.getInstance().downloadPortrait(userId, username, portraitPath);
    }

    @Override
    public void setDownloadPortraitState(boolean isDownloadPortrait) {
        mIsDownloadPortrait = isDownloadPortrait;
    }

    @Override
    public void loadGatewayDevices() {
        DeviceService.getService().getGatewayDevices()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<List<GatewayDevice>>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTasksView != null) {
                            mTasksView.onLoadGatewayDevicesResult(ConstantValue.ERROR, null);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<List<GatewayDevice>> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mTasksView != null) {
                            mTasksView.onLoadGatewayDevicesResult(ConstantValue.SUCCESS, response.getData());
                        } else if (mTasksView != null) {
                            mTasksView.onLoadGatewayDevicesResult(ConstantValue.ERROR, null);
                        }
                    }
                });
    }

    @Override
    public void loadMsgUnread(List<String> ids) {
        mMessageModel.getMsgUnreadObservable(ids)
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

}

