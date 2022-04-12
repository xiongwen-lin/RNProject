package com.afar.osaio.receiver.listener;

import android.app.Activity;
import android.app.AlertDialog;
import androidx.annotation.NonNull;

import com.afar.osaio.R;
import com.afar.osaio.account.activity.SignInActivity;
import com.afar.osaio.account.helper.MyAccountHelper;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.notification.NotificationManager;
import com.afar.osaio.receiver.contract.MyAccountReceiverContract;
import com.afar.osaio.receiver.presenter.MyAccountReceiverPresenter;
import com.afar.osaio.smart.push.bean.PushNormalMessageExtras;
import com.afar.osaio.util.DialogUtils;
import com.alibaba.android.arouter.launcher.ARouter;
import com.apemans.platformbridge.helper.YRUserPlatformHelper;
import com.nooie.common.base.GlobalData;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.PushCode;
import com.nooie.sdk.receiver.listener.AccountReceiverListener;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class MyAccountReceiverListener extends AccountReceiverListener implements MyAccountReceiverContract.View {

    private MyAccountReceiverContract.Presenter mPresenter;
    private WeakReference<Activity> mActivityWf = null;
    private AlertDialog mForceLogoutDialog = null;
    private AlertDialog mAccountMoveDialog = null;

    public MyAccountReceiverListener(Activity activity) {
        init(activity);
    }

    private void init(Activity activity) {
        new MyAccountReceiverPresenter(this);
        mActivityWf = new WeakReference<>(activity);
    }

    @Override
    public void onForceLogout() {
        NooieLog.d("-->> debug MyAccountReceiverListener onForceLogout: checkActivityInvalid=" + checkActivityInvalid() + " isLogin=" + MyAccountHelper.getInstance().isLogin());
        if (checkActivityInvalid() && !MyAccountHelper.getInstance().isLogin()) {
            return;
        }
        dealOnForceLogout();
    }

    @Override
    public void onLoginExpire() {
        if (checkActivityInvalid() && !MyAccountHelper.getInstance().isLogin()) {
            return;
        }
        deaLoginExpire();
    }

    @Override
    public void onAccountMove() {
        if (checkActivityInvalid()) {
            return;
        }
        dealAccountMove();
    }

    private void dealOnForceLogout() {
        NooieLog.d("-->> debug MyAccountReceiverListener dealOnForceLogout: 1");
        showForceLogoutDialog();
        NotificationManager.getInstance().cancelAllNotifications();
        NooieLog.d("-->> debug MyAccountReceiverListener dealOnForceLogout: 2");
    }

    private void deaLoginExpire() {
        NooieLog.d("-->> debug MyAccountReceiverListener deaLoginExpire: 1");
        if (mActivityWf == null) {
            return;
        }
        NotificationManager.getInstance().cancelAllNotifications();
        clearLoginInfo(mActivityWf.get(), true);
        sendLoginExpiredNotification();
        NooieLog.d("-->> debug MyAccountReceiverListener deaLoginExpire: 2");
    }

    private void dealAccountMove() {
        NooieLog.d("-->> debug MyAccountReceiverListener dealAccountMove: 1");
        showAccountMoveDialog();
        NotificationManager.getInstance().cancelAllNotifications();
        NooieLog.d("-->> debug MyAccountReceiverListener dealAccountMove: 2");
    }

    private void clearLoginInfo(Activity activity, boolean isClearPassword) {
        NooieLog.d("-->> debug MyAccountReceiverListener clearLoginInfo: 1");
        if (activity == null) {
            return;
        }
        String password = isClearPassword ? "" : GlobalData.getInstance().getPassword();
        MyAccountHelper.getInstance().logout();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("logout", "");
        YRUserPlatformHelper.INSTANCE.updataUserInfo(hashMap);
        // 发送跳转到登录页命令
        ARouter.getInstance().build("/user/login")
                .withString("userAccount", NooieApplication.get().getUsername())
                .withString("password", password)
                .withBoolean("isClearTask", true)
                .navigation();
//        SignInActivity.toSignInActivity(activity, NooieApplication.get().getUsername(), password, true);
        activity.finish();
        NooieLog.d("-->> debug MyAccountReceiverListener clearLoginInfo: 2");
    }

    @Override
    public void setPresenter(@NonNull MyAccountReceiverContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onLogout() {
        if (checkActivityInvalid()) {
            return;
        }
        NooieLog.d("-->> debug MyAccountReceiverListener onLogout: ");
        clearLoginInfo(mActivityWf.get(), true);
    }

    private boolean checkActivityInvalid() {
        return mActivityWf == null || mActivityWf.get() == null || mActivityWf.get().isDestroyed();
    }

    private void showForceLogoutDialog() {
        if (checkActivityInvalid()) {
            return;
        }
        if (mForceLogoutDialog == null) {
            mForceLogoutDialog = DialogUtils.showForceLogoutDialog(mActivityWf.get(), new DialogUtils.OnClickConfirmButtonListener() {
                @Override
                public void onClickRight() {
                    if (mPresenter != null) {
                        NooieLog.d("-->> debug MyAccountReceiverListener showForceLogoutDialog onClickRight: 1");
                        mPresenter.logout();
                    }
                }

                @Override
                public void onClickLeft() {
                }
            });
        }
    }

    public void hideForceLogoutDialog() {
        if (checkActivityInvalid() || mForceLogoutDialog == null) {
            return;
        }
        mForceLogoutDialog.dismiss();
        mForceLogoutDialog = null;
    }

    private void showAccountMoveDialog() {
        if (checkActivityInvalid()) {
            return;
        }
        if (mForceLogoutDialog == null) {
            mAccountMoveDialog = DialogUtils.showInformationNormalDialog(mActivityWf.get(), NooieApplication.mCtx.getResources().getString(R.string.dialog_tip_title), NooieApplication.mCtx.getResources().getString(R.string.account_move_info), false, false, new DialogUtils.OnClickInformationDialogLisenter() {
                @Override
                public void onConfirmClick() {
                    if (mActivityWf != null) {
                        NooieLog.d("-->> debug MyAccountReceiverListener showAccountMoveDialog onConfirmClick:");
                        clearLoginInfo(mActivityWf.get(), false);
                    }
                }
            });
        }
    }

    private void hideAccountMoveDialog() {
        if (checkActivityInvalid() || mAccountMoveDialog == null) {
            return;
        }
        mAccountMoveDialog.dismiss();
        mAccountMoveDialog = null;
    }

    /**
     * 在注销监听器之前调用
     */
    public void hideAllDialog() {
        hideForceLogoutDialog();
        hideAccountMoveDialog();
    }

    private void sendLoginExpiredNotification() {
        PushNormalMessageExtras normalMessageExtras = new PushNormalMessageExtras();
        normalMessageExtras.setCode(PushCode.NORMAL.code);
        normalMessageExtras.setMsg(NooieApplication.mCtx.getResources().getString(R.string.login_expire_info));
        NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, normalMessageExtras, "", NooieApplication.mCtx.getResources().getString(R.string.login_expire_info));
    }
}
