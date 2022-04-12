package com.afar.osaio.receiver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.afar.osaio.R;
import com.afar.osaio.account.activity.SignInActivity;
import com.afar.osaio.account.helper.MyAccountHelper;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.notification.NotificationManager;
import com.alibaba.android.arouter.launcher.ARouter;
import com.apemans.platformbridge.helper.YRUserPlatformHelper;
import com.nooie.common.base.GlobalData;
import com.nooie.common.bean.CConstant;
import com.nooie.sdk.db.dao.UserRegionService;
import com.afar.osaio.smart.push.bean.PushNormalMessageExtras;
import com.afar.osaio.util.DialogUtils;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.PushCode;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.device.DeviceCmdService;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.processor.user.UserApi;

import java.util.HashMap;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by victor on 2018/6/27
 * Email is victor.qiao.0604@gmail.com
 */
public class ForceLogoutBroadcastReceiver extends BroadcastReceiver {
    private Activity mActivity;
    private AlertDialog mDialog = null;
    private AlertDialog mExpireDialog = null;
    private AlertDialog mAccountMoveDialog = null;

    public ForceLogoutBroadcastReceiver(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if (CConstant.ACTION_FORCE_LOGOUT.equals(intent.getAction()) && MyAccountHelper.getInstance().isLogin()) {
                NooieLog.d("-->> ForceLogoutBroadcastReceiver onReceive force logout");

                //GlobalPrefs globalPrefs = GlobalPrefs.getPreferences(context);
                //globalPrefs.logout();

                if (mDialog == null) {
                    mDialog = DialogUtils.showForceLogoutDialog(mActivity, onClickReLoginListener);
                }
                NotificationManager.getInstance().cancelAllNotifications();
            } else if (CConstant.ACTION_LOGIN_EXPIRE.equals(intent.getAction()) && MyAccountHelper.getInstance().isLogin()) {
                /*
                if (mExpireDialog == null) {
                    mExpireDialog = DialogUtils.showInformationNormalDialog(mActivity, NooieApplication.mCtx.getResources().getString(R.string.login_expire), NooieApplication.mCtx.getResources().getString(R.string.login_expire_info), false, onClickLoginExpireListener);
                }
                */
                NotificationManager.getInstance().cancelAllNotifications();
                clearLoginInfo(true);
                sendLoginExpiredNotification();
            } else if (CConstant.ACTION_ACCOUNT_MOVE.equalsIgnoreCase(intent.getAction())) {
                final String account = intent.getStringExtra(ApiConstant.DATA_KEY_ACCOUNT);
                final String countryCode = intent.getStringExtra(ApiConstant.DATA_KEY_COUNTRY_CODE);
                NooieLog.d("-->> ForceLogoutBroadcastReceiver onReceive ACTION_ACCOUNT_MOVE account=" + account + " countryCode=" + countryCode);
                updateCountryCode(account, countryCode);
                if (mAccountMoveDialog == null) {
                    mAccountMoveDialog = DialogUtils.showInformationNormalDialog(mActivity, NooieApplication.mCtx.getResources().getString(R.string.dialog_tip_title), NooieApplication.mCtx.getResources().getString(R.string.account_move_info), false, false, new DialogUtils.OnClickInformationDialogLisenter() {
                        @Override
                        public void onConfirmClick() {
                            dealAccountMoved(account, countryCode);
                        }
                    });
                }
                NotificationManager.getInstance().cancelAllNotifications();
            }
        }
    }

    private DialogUtils.OnClickConfirmButtonListener onClickReLoginListener = new DialogUtils.OnClickConfirmButtonListener() {
        @Override
        public void onClickRight() {
            DeviceCmdService.getInstance(NooieApplication.mCtx).destroyAllConn(new OnActionResultListener() {
                @Override
                public void onResult(int code) {
                    UserApi.getInstance().logout(true)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<BaseResponse>() {
                                @Override
                                public void onCompleted() {
                                }

                                @Override
                                public void onError(Throwable e) {
                                    clearLoginInfo(true);
                                }

                                @Override
                                public void onNext(BaseResponse response) {
                                    clearLoginInfo(true);
                                }
                            });
                }
            });
        }

        @Override
        public void onClickLeft() {
        }
    };

    private DialogUtils.OnClickInformationDialogLisenter onClickLoginExpireListener = new DialogUtils.OnClickInformationDialogLisenter() {
        @Override
        public void onConfirmClick() {
            clearLoginInfo(false);
        }
    };

    private void sendLoginExpiredNotification() {
        PushNormalMessageExtras normalMessageExtras = new PushNormalMessageExtras();
        normalMessageExtras.setCode(PushCode.NORMAL.code);
        normalMessageExtras.setMsg(NooieApplication.mCtx.getResources().getString(R.string.login_expire_info));
        NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, normalMessageExtras);
    }

    private void updateCountryCode(String account, final String countryCode) {
        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(countryCode)) {
            Observable.just(account)
                    .flatMap(new Func1<String, Observable<String>>() {
                        @Override
                        public Observable<String> call(String account) {
                            UserRegionService.getInstance().addUserRegion(account, countryCode);
                            return Observable.just(account);
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                        }

                        @Override
                        public void onNext(String result) {
                            NooieLog.d("-->> ForceLogoutBroadcastReceiver updateCountryCode onNext");
                        }
                    });
        }
    }

    private void dealAccountMoved(final String account, final String countryCode) {
        if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(countryCode)) {
            clearLoginInfo(false);
        }
    }

    private void clearLoginInfo(boolean isClearPassword) {
        String passwrod = isClearPassword ? "" : GlobalData.getInstance().getPassword();
        MyAccountHelper.getInstance().logout();
        // 发送中间件信息（清空YRCXSDK 和 YRBusiness 模块中的缓存信息）
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("logout", "");
        YRUserPlatformHelper.INSTANCE.updataUserInfo(hashMap);
        // 发送跳转到登录页命令
        ARouter.getInstance().build("/user/login")
                .withString("userAccount", NooieApplication.get().getUsername())
                .withString("password", passwrod)
                .withBoolean("isClearTask", true)
                .navigation();
//        SignInActivity.toSignInActivity(mActivity, NooieApplication.get().getUsername(), passwrod, true);
        mDialog = null;
        if (mActivity != null) {
            mActivity.finish();
        }
    }

    public void dismissAllDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
        }

        if (mExpireDialog != null) {
            mExpireDialog.dismiss();
        }

        if (mAccountMoveDialog != null) {
            mAccountMoveDialog.dismiss();
        }
    }

    private ForceLogoutCallback mCallback;
    public void setCallback(ForceLogoutCallback callback) {
        mCallback = callback;
    }
    public interface ForceLogoutCallback {
        void onReceive(Intent intent);
    }
}

