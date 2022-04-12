package com.afar.osaio.smart.electrician.presenter;

import android.text.TextUtils;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.cache.UserInfoCache;
import com.afar.osaio.smart.electrician.model.IMyProfileModel;
import com.afar.osaio.smart.electrician.model.MyProfileModel;
import com.afar.osaio.smart.electrician.view.IMyProfileView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.base.GlobalData;
import com.nooie.common.bean.CConstant;
import com.nooie.common.utils.file.FileUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.account.AccountService;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.UserInfoResult;
import com.nooie.sdk.api.network.base.core.NetConfigure;
import com.nooie.sdk.bean.CloudUploadResult;
import com.nooie.sdk.processor.cloud.CloudManager;
import com.nooie.sdk.processor.user.UserApi;
import com.tuya.smart.android.user.api.IBooleanCallback;
import com.tuya.smart.home.sdk.TuyaHomeSdk;

import java.io.File;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * MyProfilePresenter
 *
 * @author Administrator
 * @date 2019/2/27
 */
public class MyProfilePresenter implements IMyProfilePresenter {

    private static final int GET_USER_INFO_FAIL = 0;
    private static final int GET_USER_INFO_SUCCESS = 1;
    private static final int GET_USER_INFO_SUCCESS_WITH_UPDATE_PHONE = 2;

    private IMyProfileView myProfileView;
    private IMyProfileModel myProfileModel;
    private boolean mIsDownloadPortrait = false;

    public MyProfilePresenter(IMyProfileView view) {
        myProfileView = view;
        myProfileModel = new MyProfileModel();
    }

    @Override
    public void loadUserInfo() {
        myProfileModel.getUserInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<UserInfoResult>>() {
                    @Override
                    public void onNext(BaseResponse<UserInfoResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null && myProfileView != null) {
                            UserInfoResult result = response.getData();
                            UserInfoCache.getInstance().setUserInfo(response.getData());
                            myProfileView.notifyLoadUserInfoSucess(result);
                        } else {
                            String errorCode = ConstantValue.ERROR;
                            myProfileView.notifyLoadUserInfoState(errorCode);
                        }
                    }

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (myProfileView != null) {
                            myProfileView.notifyLoadUserInfoState(ConstantValue.ERROR);
                        }
                    }
                });
    }

    @Override
    public void logout() {
        UserApi.getInstance().logout(false, new Observer<BaseResponse>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                if (myProfileView != null) {
                    myProfileView.notifyLogoutState("");
                }
            }

            @Override
            public void onNext(BaseResponse response) {
                GlobalData.getInstance().log("PersonPresenter logout");
                if (response != null && response.getCode() == StateCode.SUCCESS.code && myProfileView != null) {
                    myProfileView.notifyLogoutState(ConstantValue.SUCCESS);
                } else if (myProfileView != null) {
                    myProfileView.notifyLogoutState("");
                }
            }
        });
    }

    @Override
    public void setPortrait(File photo) {
        NooieLog.e("MyProfilePresenter setPortrait " + photo);
        TuyaHomeSdk.getUserInstance().uploadUserAvatar(photo,
                new IBooleanCallback() {
                    @Override
                    public void onSuccess() {
                        NooieLog.e("MyProfilePresenter setPortrait success");
                        myProfileView.notifySetPortraitState(ConstantValue.SUCCESS);
                    }

                    @Override
                    public void onError(String code, String error) {
                        NooieLog.e("MyProfilePresenter setPortrait errorCode " + code + "  error  " + error);
                        myProfileView.notifySetPortraitState(ConstantValue.ERROR);
                    }
                });
    }

    @Override
    public void uploadPictures(String userid, String username, String photoPath) {
        NooieLog.d("-->> debug MyProfilePresenter uploadPictures: 1000 userId=" + userid + " " + username + " " + photoPath);
        /*CloudManager.getInstance().uploadPortrait(userid, username, photoPath, FileUtil.getPersonPortrait(NooieApplication.mCtx, username).getPath(), FileUtil.getPersonPortraitInPrivate(NooieApplication.mCtx, username).getPath(), new Observer<Boolean>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                NooieLog.d("-->> debug MyProfilePresenter uploadPictures: 1004");
                if (myProfileView != null) {
                    myProfileView.notifyRefreshUserPortrait(ConstantValue.ERROR, true);
                }
            }

            @Override
            public void onNext(Boolean result) {
                NooieLog.d("-->> MyProfilePresenter uploadPictures 1001 result=" + result);
                if (result) {
                    setPortrait(new File(FileUtil.getPortraitPhotoPathInPrivate(NooieApplication.mCtx, username, userid)));
                    updatePhotoName();
                } else if (myProfileView != null) {
                    NooieLog.d("-->> debug MyProfilePresenter uploadPictures: 1003");
                    myProfileView.notifyRefreshUserPortrait(ConstantValue.ERROR, true);
                }
            }
        });*/
        CloudManager.getInstance().uploadPortrait(userid, username, photoPath, FileUtil.getPersonPortrait(NooieApplication.mCtx, username).getPath(), FileUtil.getPersonPortraitInPrivate(NooieApplication.mCtx, username).getPath(), "public-read", new Observer<CloudUploadResult>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                NooieLog.d("-->> debug MyProfilePresenter uploadPictures: 1004");
                if (myProfileView != null) {
                    myProfileView.notifyRefreshUserPortrait(ConstantValue.ERROR, true);
                }
            }

            @Override
            public void onNext(CloudUploadResult cloudUploadResult) {
                NooieLog.d("-->> MyProfilePresenter uploadPictures 1001 result=" + cloudUploadResult.isSuccessful());
                NooieLog.d("-->> MyProfilePresenter uploadPictures 1002 url=" + cloudUploadResult.getUrl());
                if (cloudUploadResult.isSuccessful()) {
                    setPortrait(new File(FileUtil.getPortraitPhotoPathInPrivate(NooieApplication.mCtx, username, userid)));
                    String photoUrl = cloudUploadResult.getUrl().contains("?") ? cloudUploadResult.getUrl().substring(0, cloudUploadResult.getUrl().indexOf("?")) : "";
                    NooieLog.d("-->> MyProfilePresenter uploadPictures 1003 url=" + photoUrl);
                    updatePhotoName(photoUrl);
                } else if (myProfileView != null) {
                    NooieLog.d("-->> debug MyProfilePresenter uploadPictures: 1003");
                    myProfileView.notifyRefreshUserPortrait(ConstantValue.ERROR, true);
                }
            }
        });
    }

    @Override
    public void getUserInfo(String userId, String userName, String portraitPath) {
        NooieLog.d("-->> debug MyProfilePresenter getUserInfo: 1001 userId" + userId + " userName=" + userName + " portraitPath=" + portraitPath);
        AccountService.getService().getUserInfo()
                .flatMap(new Func1<BaseResponse<UserInfoResult>, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(BaseResponse<UserInfoResult> response) {
                        NooieLog.d("-->> debug MyProfilePresenter getUserInfo: 1002");
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            NooieLog.d("-->> debug MyProfilePresenter getUserInfo: 1003");
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
                        NooieLog.d("-->> debug MyProfilePresenter getUserInfo: 1007");
                        if (myProfileView != null) {
                            myProfileView.notifyGetUserInfoResult("");
                        }
                    }

                    @Override
                    public void onNext(Integer result) {
                        NooieLog.d("-->> debug MyProfilePresenter getUserInfo: 1004 result=" + result);
                        if (result == GET_USER_INFO_SUCCESS_WITH_UPDATE_PHONE) {
                            NooieLog.d("-->> debug MyProfilePresenter getUserInfo: 1005");
                            downloadPortrait(userId, userName, portraitPath);
                        }
                        if (myProfileView != null) {
                            NooieLog.d("-->> debug MyProfilePresenter getUserInfo: 1006");
                            myProfileView.notifyGetUserInfoResult((result == GET_USER_INFO_SUCCESS || result == GET_USER_INFO_SUCCESS_WITH_UPDATE_PHONE) ? ConstantValue.SUCCESS : "");
                        }
                    }
                });
    }

    @Override
    public void downloadPortrait(String userId, String username, String portraitPath) {
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

    private void updatePhotoName(String photoUrl) {
        NooieLog.d("-->> debug MyProfilePresenter uploadPictures: 1005");
        if (TextUtils.isEmpty(photoUrl)) {
            AccountService.getService().updateUserPhoto(createPhotoName())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<BaseResponse>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            NooieLog.d("-->> debug MyProfilePresenter uploadPictures: 1009");
                            if (myProfileView != null) {
                                myProfileView.notifyRefreshUserPortrait(ConstantValue.ERROR, true);
                            }
                        }

                        @Override
                        public void onNext(BaseResponse response) {
                            NooieLog.d("-->> debug MyProfilePresenter uploadPictures: 1006");
                            if (response != null && response.getCode() == StateCode.SUCCESS.code && myProfileView != null) {
                                NooieLog.d("-->> debug MyProfilePresenter uploadPictures: 1007");
                                myProfileView.notifyRefreshUserPortrait(ConstantValue.SUCCESS, true);
                            } else if (myProfileView != null) {
                                NooieLog.d("-->> debug MyProfilePresenter uploadPictures: 1008");
                                myProfileView.notifyRefreshUserPortrait(ConstantValue.ERROR, true);
                            }
                        }
                    });
        } else {
            AccountService.getService().updateUserPhoto(photoUrl)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<BaseResponse>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            NooieLog.d("-->> debug MyProfilePresenter uploadPictures: 1010");
                            if (myProfileView != null) {
                                myProfileView.notifyRefreshUserPortrait(ConstantValue.ERROR, true);
                            }
                        }

                        @Override
                        public void onNext(BaseResponse response) {
                            NooieLog.d("-->> debug MyProfilePresenter uploadPictures: 1011");
                            if (response != null && response.getCode() == StateCode.SUCCESS.code && myProfileView != null) {
                                NooieLog.d("-->> debug MyProfilePresenter uploadPictures: 1012");
                                myProfileView.notifyRefreshUserPortrait(ConstantValue.SUCCESS, true);
                            } else if (myProfileView != null) {
                                NooieLog.d("-->> debug MyProfilePresenter uploadPictures: 1013");
                                myProfileView.notifyRefreshUserPortrait(ConstantValue.ERROR, true);
                            }
                        }
                    });
        }
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
}
