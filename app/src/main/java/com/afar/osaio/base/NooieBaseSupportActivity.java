package com.afar.osaio.base;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import android.text.TextUtils;

import com.afar.osaio.smart.home.activity.HomeActivity;
import com.alibaba.android.arouter.launcher.ARouter;
import com.android.nordicbluetooth.SmartBleManager;
import com.afar.osaio.account.activity.SignInActivity;
import com.afar.osaio.account.helper.MyAccountHelper;
import com.afar.osaio.base.contract.BaseSupportContract;
import com.afar.osaio.base.presenter.BaseSupportPresenter;
import com.afar.osaio.receiver.listener.MyAccountReceiverListener;
import com.afar.osaio.smart.push.bean.PushActiveMessageExtras;
import com.afar.osaio.smart.push.bean.PushBatteryMessageExtras;
import com.afar.osaio.smart.push.bean.PushShareMessageExtras;
import com.afar.osaio.smart.push.bean.PushUpdateMessageExtras;
import com.afar.osaio.smart.push.helper.NooiePushMsgHelper;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.nooie.common.base.GlobalData;
import com.nooie.common.bean.CConstant;
import com.nooie.common.hardware.bluetooth.BluetoothHelper;
import com.nooie.common.utils.notify.NotificationUtil;
import com.nooie.data.EventDictionary;
import com.nooie.eventtracking.EventTrackingApi;
import com.nooie.eventtracking.IAutoScreenTracker;
import com.nooie.sdk.base.AppStateManager;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.processor.device.DeviceApi;
import com.nooie.sdk.receiver.AccountReceiver;
import com.nooie.sdk.receiver.NetworkManagerReceiver;
import com.nooie.sdk.receiver.UpdateShareDataReceiver;
import com.gyf.immersionbar.ImmersionBar;
import com.nooie.common.utils.json.GsonHelper;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.PushCode;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.afar.osaio.R;
import com.afar.osaio.account.activity.SplashActivity;
import com.afar.osaio.smart.device.bean.ListDeviceItem;
import com.afar.osaio.receiver.ForceLogoutBroadcastReceiver;
import com.afar.osaio.smart.push.bean.PushMessageBaseExtras;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;
import com.nooie.sdk.device.DeviceCmdService;
import com.umeng.message.PushAgent;

import java.util.List;

import me.yokeyword.fragmentation.base.SupportActivity;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public class NooieBaseSupportActivity extends SupportActivity implements EasyPermissions.PermissionCallbacks, BaseSupportContract.View, IAutoScreenTracker {

    protected static final int DEFAULT_SETTINGS_REQ_CODE = 0x10;

    protected String mUid;
    protected String mToken;
    protected String mUserAccount;
    private volatile int requestPermissionSize = 0;
    private volatile boolean isFirstLaunch;

    private AlertDialog mNooieShareDialog;
    private AlertDialog mLoadingDialog;
    BaseSupportContract.Presenter mPresenter;
    private MyAccountReceiverListener mAccountReceiverListener = null;
    private boolean mIsCheckLocationEnable = false;
    private boolean mIsCheckLocationPerm = false;
    private AlertDialog mCheckLocationEnableDialog = null;
    private AlertDialog mCheckLocalPermForBluetoothDialog = null;
    private CustomAppStateManagerListener mAppStateManagerListener = null;
    private Intent mCurrentIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        if (NooieApplication.liveFlag == -1) {
            //todo open auto restart when liveFlag equals -1
            protectApp();
        }
         */
        NooieLog.d("-->> debug NooieBaseSupportActivity onCreate: liveFlag=" + GlobalData.liveFlag);
        MyAccountHelper.getInstance().log("NooieBaseSupportActivity onCreate");
        if (GlobalData.liveFlag == 0) {
            //open auto restart when liveFlag equals 0
            NooieLog.d("-->> debug NooieBaseSupportActivity onCreate: 1 liveFlag=" + GlobalData.liveFlag);
            protectApp();
        }

        NooieLog.d("-->> debug NooieBaseSupportActivity onCreate: eventId=" + getEventId(EventDictionary.EVENT_TRACK_TYPE_START) + " trackType=" + getTrackType());
        EventTrackingApi.getInstance().trackScreenEvent(this, System.currentTimeMillis(), EventDictionary.EVENT_TRACK_TYPE_START);

        ActivityStack.instance().add(this);
        isFirstLaunch = true;
        initGlobalData();
        PushAgent.getInstance(NooieApplication.mCtx).onAppStart();
        new BaseSupportPresenter(this);
        registerUpdateShareDataReceiver();
        registerNetworkManagerReceiver();
        setupStatusBar();
    }

    public void initGlobalData() {
        mUid = GlobalData.getInstance().getUid();
        mToken = GlobalData.getInstance().getToken();
        mUserAccount = GlobalData.getInstance().getAccount();
        DeviceApi.getInstance().initDeviceConfigureCache(mUserAccount);
    }

    public void unInitGlobalData() {
        mUid = "";
        mToken = "";
        mUserAccount = "";
    }

    public void protectApp() {
        Intent intent = new Intent(NooieBaseSupportActivity.this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        MyAccountHelper.getInstance().log("NooieBaseSupportActivity onResume");
        registerJPushReceiver();
        //registerForceLogoutBroadcast();
        registerAccountReceiverListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        MyAccountHelper.getInstance().log("NooieBaseSupportActivity onPause");
        isFirstLaunch = false;
        unregisterJPushReceiver();
        //unRegisterForceLogoutBroadcast();
        unRegisterAccountReceiverListener();
        hideNooieShareDialog();
    }

    @Override
    protected void  onStop() {
        super.onStop();
        NooieLog.d("-->> debug NooieBaseSupportActivity onStop: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            NooieLog.d("-->> debug NooieBaseSupportActivity onDestroy: eventId=" + getEventId(EventDictionary.EVENT_TRACK_TYPE_END) + " trackType=" + getTrackType());
            EventTrackingApi.getInstance().trackScreenEvent(this, System.currentTimeMillis(), EventDictionary.EVENT_TRACK_TYPE_END);
            unRegisterUpdateShareDataReceiver();
            unRegisterNetworkManagerReceiver();
            mAccountReceiverListener = null;
            ActivityStack.instance().remove(this);
        }catch (Exception e){
            NooieLog.e("NooieBaseSupportActivity---onDestroy()--error");
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        NooieLog.d("-->> debug NooieBaseSupportActivity onNewIntent: ");
        this.mCurrentIntent = intent != null ? intent : getIntent();
    }

    /**
     * 禁止app字体大小跟随系统字体大小调节
     * @return
     */
    @Override
    public Resources getResources() {
        Resources resources = super.getResources();
        if (resources != null && resources.getConfiguration().fontScale != 1.0f) {
            android.content.res.Configuration configuration = resources.getConfiguration();
            if (configuration != null) {
                configuration.fontScale = 1.0f;
                resources.updateConfiguration(configuration, resources.getDisplayMetrics());
            }
        }
        return resources;
    }

    public void setupStatusBar() {
        if (getStatusBarMode() == ConstantValue.STATUS_BAR_DARK_MODE) {
            if (ImmersionBar.isSupportStatusBarDarkFont()) {
                ImmersionBar.with(this).statusBarColor(R.color.colorPrimary).statusBarDarkFont(true).fitsSystemWindows(true).keyboardEnable(true).init();
            } else {
                //当前设备不支持状态栏字体变色
                ImmersionBar.with(this).statusBarColor(R.color.colorPrimary).statusBarDarkFont(true, 0.2f).fitsSystemWindows(true).keyboardEnable(true).init();
            }
        } else if (getStatusBarMode() == ConstantValue.STATUS_BAR_LIGHT_BLUE_MODE) {
            if (ImmersionBar.isSupportStatusBarDarkFont()) {
                ImmersionBar.with(this).statusBarColor(R.color.background_login_and_register).statusBarDarkFont(true).fitsSystemWindows(true).keyboardEnable(true).init();
            } else {
                //当前设备不支持状态栏字体变色
                ImmersionBar.with(this).statusBarColor(R.color.background_login_and_register).statusBarDarkFont(true, 0.2f).fitsSystemWindows(true).keyboardEnable(true).init();
            }
        } else if (getStatusBarMode() == ConstantValue.STATUS_BAR_SPLASH_MODE) {
            if (ImmersionBar.isSupportStatusBarDarkFont()) {
                ImmersionBar.with(this).statusBarColor(R.color.theme_splash_primary).statusBarDarkFont(true).fitsSystemWindows(true).keyboardEnable(true).init();
            } else {
                //当前设备不支持状态栏字体变色
                ImmersionBar.with(this).statusBarColor(R.color.theme_splash_primary).statusBarDarkFont(true, 0.2f).fitsSystemWindows(true).keyboardEnable(true).init();
            }
        } else if (getStatusBarMode() == ConstantValue.STATUS_BAR_DARK_MODE_2) {
            if (ImmersionBar.isSupportStatusBarDarkFont()) {
                ImmersionBar.with(this).statusBarColor(R.color.background_home_page_1).statusBarDarkFont(true).fitsSystemWindows(true).keyboardEnable(true).init();
            } else {
                //当前设备不支持状态栏字体变色
                ImmersionBar.with(this).statusBarColor(R.color.background_home_page_1).statusBarDarkFont(true, 0.2f).fitsSystemWindows(true).keyboardEnable(true).init();
            }
        } else {
            ImmersionBar.with(this).statusBarDarkFont(false).fitsSystemWindows(true).statusBarColor(R.color.colorPrimary).keyboardEnable(true).init();
        }
    }

    public int getStatusBarMode() {
        return ConstantValue.STATUS_BAR_DARK_MODE;
    }

    @Override
    public void setPresenter(@NonNull BaseSupportContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onUpdateShareMsgState(String result) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            ToastUtil.showToast(this, R.string.success);
            Intent intent = new Intent(ConstantValue.BROADCAST_KEY_UPDATE_CAMERA);
            NotificationUtil.sendBroadcast(NooieApplication.mCtx, intent);
        }
    }

    public boolean isFirstLaunch() {
        return isFirstLaunch;
    }

    protected void requestPermission(String[] perms, int requestcode) {
        if (EasyPermissions.hasPermissions(this, perms)) {
            permissionsGranted(requestcode);
        } else {
            // request for one permission
            //EasyPermissions.requestPermissions(this, getString(R.string.request_title), requestcode, perms);
            EasyPermissions.requestPermissions(new PermissionRequest.Builder(this, requestcode, perms)
                    .setRationale(R.string.request_title)
                    .setTheme(R.style.EasyPermissionsDialogStyle)
                    .build());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        this.requestPermissionSize = permissions == null ? 0 : permissions.length;
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (perms.size() == requestPermissionSize) {
            permissionsGranted(requestCode);
        }
    }

    protected void permissionsGranted(int requestcode) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            showRationaleAskAgainDialog();
        } else {
            showRationaleAskDialog(requestCode, perms);
        }
    }

    public boolean requestLocationPerm(String tipContent, boolean isForceCheck) {
        if (!BluetoothHelper.isLocationEnabled(NooieApplication.mCtx)) {
            if (isForceCheck || !mIsCheckLocationEnable) {
                mIsCheckLocationEnable = true;
                showCheckLocationEnableDialog(tipContent);
                return false;
            }
        }

        if (!EasyPermissions.hasPermissions(NooieApplication.mCtx, ConstantValue.PERM_GROUP_LOCATION)) {
            if (isForceCheck || !mIsCheckLocationPerm) {
                mIsCheckLocationPerm = true;
                showCheckLocationPermDialog();
                return false;
            }
        }

        return true;
    }

    public void showCheckLocationEnableDialog(String tipContent) {
        hideCheckLocationEnableDialog();
        mCheckLocationEnableDialog = DialogUtils.showConfirmWithSubMsgDialog(this, getString(R.string.enable_location), tipContent, R.string.cancel, R.string.settings, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }

            @Override
            public void onClickLeft() {
            }
        });
    }

    public void hideCheckLocationEnableDialog() {
        if (mCheckLocationEnableDialog != null) {
            mCheckLocationEnableDialog.dismiss();
            mCheckLocationEnableDialog = null;
        }
    }

    public void showCheckLocalPermDialog(String tipContent, DialogUtils.OnClickConfirmButtonListener listener) {
        hideCheckLocalPermForBluetoothDialog();
        mCheckLocalPermForBluetoothDialog = DialogUtils.showConfirmWithSubMsgDialog(this, getString(R.string.dialog_tip_title), tipContent, R.string.cancel, R.string.settings, listener);
    }

    public void hideCheckLocalPermForBluetoothDialog() {
        if (mCheckLocalPermForBluetoothDialog != null) {
            mCheckLocalPermForBluetoothDialog.dismiss();
            mCheckLocalPermForBluetoothDialog = null;
        }
    }

    public void showCheckLocationPermDialog() {
    }

    public boolean checkUseLocationEnable() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || (BluetoothHelper.isLocationEnabled(NooieApplication.mCtx) && EasyPermissions.hasPermissions(NooieApplication.mCtx, ConstantValue.PERM_GROUP_LOCATION));
    }

    private void showRationaleAskAgainDialog() {
        DialogUtils.showConfirmDialog(this, R.string.request_rationale_ask_again, R.string.cancel, R.string.settings, false, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.fromParts("package", getPackageName(), null));
                startActivityForResult(intent, DEFAULT_SETTINGS_REQ_CODE);
            }

            @Override
            public void onClickLeft() {
            }
        });
    }

    private void showRationaleAskDialog(final int requestCode, final List<String> perms) {
        if (perms.size() == 0) {
            return;
        }
        DialogUtils.showConfirmDialog(this, R.string.request_rationale_ask, R.string.cancel, R.string.request, false, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    return;
                } else {
                    ActivityCompat.requestPermissions(NooieBaseSupportActivity.this, perms.toArray(new String[perms.size()]), requestCode);
                }
            }

            @Override
            public void onClickLeft() {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DEFAULT_SETTINGS_REQ_CODE) {
            // Do something after user returned from app settings screen, like showing a Toast.
            onReturnFromAppSettingActivity();
        } else if (requestCode == ConstantValue.REQUEST_CODE_SIGN_IN) {
            onReturnFromSignInActivity();
        }
    }

    protected void onReturnFromAppSettingActivity() {
    }

    public void onReturnFromSignInActivity() {}

    public Intent getCurrentIntent() {
        return mCurrentIntent;
    }

    public boolean isCurrentIntentNull() {
        return getCurrentIntent() == null;
    }

    @Override
    public String getEventId(int trackType) {
        return null;
    }

    /**
     * 返回页面埋点上传类型
     * trackType类型如下：
     * EventDictionary.EVENT_TRACK_TYPE_START表示只记录进入页面事件
     * EVENT_TRACK_TYPE_END表示只记录退出页面事件
     * EVENT_TRACK_TYPE_START_END表示记录进入和退出页面事件
     * @return 默认不记录进入事件
     */
    @Override
    public int getTrackType() {
        return EventDictionary.EVENT_TRACK_TYPE_NONE;
    }

    /**
     * 返回页面埋点的页码,空值使用默认
     * @return
     */
    @Override
    public String getPageId() {
        return null;
    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public String getExternal() {
        return null;
    }








    @Override
    public String getExternal2() {
        return null;
    }

    @Override
    public String getExternal3() {
        return  null;
    }

    /**
     * 返回设备id
     * @return
     */
    @Override
    public String getUuid() {
        return null;
    }

    public boolean checkLogin(String account, String password) {
        if (MyAccountHelper.getInstance().isLogin()) {
            return true;
        } else {
            // 发送跳转到登录页命令
            ARouter.getInstance().build("/user/login")
                    .withString("userAccount", account)
                    .withString("password", password)
                    .withBoolean("isClearTask", false)
                    .navigation();
//            SignInActivity.toSignInActivity(this, account, password, false);
            return false;
        }
    }

    public Bundle getStartParam() {
        if (getCurrentIntent() == null) {
            return null;
        }
        return getCurrentIntent().getBundleExtra(ConstantValue.INTENT_KEY_START_PARAM);
    }

    private void registerAccountReceiverListener() {
        if (!MyAccountHelper.getInstance().isLogin()) {
            return;
        }
        if (mAccountReceiverListener == null) {
            mAccountReceiverListener = new MyAccountReceiverListener(this);
        }
        AccountReceiver.getInstance().addListener(mAccountReceiverListener);
    }

    private void unRegisterAccountReceiverListener() {
        if (mAccountReceiverListener != null) {
            mAccountReceiverListener.hideAllDialog();
            AccountReceiver.getInstance().removeListener(mAccountReceiverListener);
        }
    }

    private ForceLogoutBroadcastReceiver mForceLogoutReceiver = null;

    private void registerForceLogoutBroadcast() {
        if (!MyAccountHelper.getInstance().isLogin()) {
            return;
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CConstant.ACTION_FORCE_LOGOUT);
        intentFilter.addAction(CConstant.ACTION_LOGIN_EXPIRE);
        intentFilter.addAction(CConstant.ACTION_ACCOUNT_MOVE);
        mForceLogoutReceiver = new ForceLogoutBroadcastReceiver(this);
        mForceLogoutReceiver.setCallback(new ForceLogoutBroadcastReceiver.ForceLogoutCallback() {
            @Override
            public void onReceive(Intent intent) {
            }
        });
        NotificationUtil.registerLocalReceiver(NooieApplication.mCtx, mForceLogoutReceiver, intentFilter);
    }

    private void unRegisterForceLogoutBroadcast() {
        if (mForceLogoutReceiver != null) {
            mForceLogoutReceiver.dismissAllDialog();
            NotificationUtil.unregisterLocalReceiver(NooieApplication.mCtx, mForceLogoutReceiver);
            mForceLogoutReceiver = null;
        }
    }

    JPushBroadcastReceiver mJPushBroadcastReceiver;

    private void registerJPushReceiver() {
        if (mJPushBroadcastReceiver == null) {
            //setNotifyBuilder();
            mJPushBroadcastReceiver = new JPushBroadcastReceiver();
            IntentFilter intentFilter = new IntentFilter(ConstantValue.BROADCAST_KEY_RECEIVE_JG_PUSH);
            NotificationUtil.registerLocalReceiver(NooieApplication.mCtx, mJPushBroadcastReceiver, intentFilter);
        }
    }

    private void unregisterJPushReceiver() {
        if (mJPushBroadcastReceiver != null) {
            NotificationUtil.unregisterLocalReceiver(NooieApplication.mCtx, mJPushBroadcastReceiver);
            mJPushBroadcastReceiver = null;
        }
    }

    private void showNooieShareMessage(PushShareMessageExtras shareMessage) {
        if (shareMessage != null) {
            String title = getString(R.string.share_dialog_title);
            String message = null;
            String subMessage = null;

            if (shareMessage.getCode() == PushCode.PUSH_SHARE_MSG_TO_SHARER.code) {
                //%1$s shares %2$s to me
                message = String.format(getString(R.string.share_dialog_content), shareMessage.getAccount(), shareMessage.getDevice());
                subMessage = String.format(getString(R.string.share_device_sub_message), shareMessage.getDevice());
                showNooieShareDialog(shareMessage.getMsg_id(), shareMessage.getShare_id(), title, message, subMessage);
            } else if (shareMessage.getCode() == PushCode.PUSH_SHARE_STATUS_TO_OWNER.code && shareMessage.getStatus() == ApiConstant.SYS_MSG_SHARE_STATUS_ACCEPT) {
                //%1$s agreed with my device %2$s
                message = String.format(getString(R.string.share_dialog_content_agree), shareMessage.getAccount(), shareMessage.getDevice());
                //ToastUtil.showToast(this, message);
                sendReceiveShareAgreeBroadcast();
            } else if (shareMessage.getCode() == PushCode.PUSH_SHARE_STATUS_TO_OWNER.code && shareMessage.getStatus() == ApiConstant.SYS_MSG_SHARE_STATUS_REJECT) {
                //%1$s rejected my shared %2$s
                message = String.format(getString(R.string.share_dialog_content_reject), shareMessage.getAccount(), shareMessage.getDevice());
                //ToastUtil.showToast(this, message);
            } else if (shareMessage.getCode() == PushCode.OWNER_REMOVE_SHARE_NOTIFY_SHARER.code) {
                //%1$s canceled sharing %2$s to me
                message = String.format(getString(R.string.share_dialog_content_cancel), shareMessage.getAccount(), shareMessage.getDevice());
                //ToastUtil.showToast(this, message);
                sendRemoveCameraBroadcast();
            } else if (shareMessage.getCode() == PushCode.SHARER_REMOVER_SHARE_NOTIFY_OWNER.code) {
                //%1$s deleted my shared %2$s
                message = String.format(getString(R.string.share_dialog_content_remove), shareMessage.getAccount(), shareMessage.getDevice());
                //ToastUtil.showToast(this, message);
                sendReceiveShareAgreeBroadcast();
            }
        }
    }

    private void showNooieShareDialog(final int msgId, final int shareId, String title, String message, String subMessage) {
        hideNooieShareDialog();
        mNooieShareDialog = DialogUtils.showConfirmWithSubMsgDialog(this, title, message, R.string.ignore, R.string.accept, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                if (mPresenter != null) {
                    mPresenter.updateShareMsgState(msgId, shareId, ApiConstant.SYS_MSG_SHARE_STATUS_ACCEPT);
                }
            }

            @Override
            public void onClickLeft() {
            }
        });
    }

    private void hideNooieShareDialog() {
        if (mNooieShareDialog != null) {
            mNooieShareDialog.dismiss();
            mNooieShareDialog = null;
        }
    }

    public void showPushActiveMessage(PushActiveMessageExtras pushActiveExtras) {}

    private void sendRemoveCameraBroadcast() {
        Intent intent = new Intent(ConstantValue.BROADCAST_KEY_REMOVE_CAMERA);
        NotificationUtil.sendBroadcast(NooieApplication.mCtx, intent);
    }

    private void sendReceiveShareAgreeBroadcast() {
        Intent intent = new Intent(ConstantValue.BROADCAST_KEY_REMOVE_CAMERA);
        NotificationUtil.sendBroadcast(NooieApplication.mCtx, intent);
    }

    class JPushBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            convertJPushCustomMessage(intent);
        }

        private void convertJPushCustomMessage(Intent intent) {
            Bundle pushData;
            if (intent == null || (pushData = intent.getBundleExtra(ConstantValue.INTENT_KEY_RECEIVE_JG_PUSH)) == null) {
                return;
            }
            String extras = pushData.getString(NooiePushMsgHelper.NOOIE_PUSH_MSG_EXTRA);
            if (!checkCurrentAccount(extras)) {
                return;
            }
            try {
                int code = NooiePushMsgHelper.getJPushMsgGode(extras);
                if (code == PushCode.PUSH_SHARE_MSG_TO_SHARER.code || code == PushCode.PUSH_SHARE_STATUS_TO_OWNER.code || code == PushCode.OWNER_REMOVE_SHARE_NOTIFY_SHARER.code || code == PushCode.SHARER_REMOVER_SHARE_NOTIFY_OWNER.code) {
                    PushShareMessageExtras shareExtras = GsonHelper.convertJson(extras, PushShareMessageExtras.class);
                    showNooieShareMessage(shareExtras);
                } else if (code == PushCode.DEVICE_UPDATE_START.code || code == PushCode.DEVICE_UPDATE_SUCCESS.code || code == PushCode.DEVICE_UPDATE_FAILED.code) {
                    PushUpdateMessageExtras updateExtras = GsonHelper.convertJson(extras, PushUpdateMessageExtras.class);
                    if (updateExtras != null && !TextUtils.isEmpty(updateExtras.getUuid())) {
                        changeDeviceUpgradeState(updateExtras.getUuid(), ListDeviceItem.DEVICE_PLATFORM_NOOIE, code == PushCode.DEVICE_UPDATE_START.code ? ApiConstant.DEVICE_UPDATE_TYPE_DOWNLOAD_START : ApiConstant.DEVICE_UPDATE_TYPE_NORMAL);
                    }
                } else if (code == PushCode.USER_OTHER_PLACE_UPDAET.code) {
                    //sendBroadcast(ForceLogoutBroadcastReceiver.ACTION_FORCE_LOGOUT);
                } else if (code == PushCode.PUSH_ACTIVE.code) {
                    PushActiveMessageExtras pushActiveExtras = GsonHelper.convertJson(extras, PushActiveMessageExtras.class);
                    showPushActiveMessage(pushActiveExtras);
                } else if (code == PushCode.PUSH_LOW_BATTERY.code) {
                    PushBatteryMessageExtras pushBatteryExtras = GsonHelper.convertJson(extras, PushBatteryMessageExtras.class);
                    String name = pushBatteryExtras != null ? (!TextUtils.isEmpty(pushBatteryExtras.getName()) ? pushBatteryExtras.getName() : pushBatteryExtras.getUuid()) : "";
                    showLowBatteryDialog(name);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private boolean checkCurrentAccount(String extras) {
            PushMessageBaseExtras jExtras = GsonHelper.convertJson(extras, PushMessageBaseExtras.class);
            return jExtras != null && !TextUtils.isEmpty(jExtras.getUser_account()) && jExtras.getUser_account().equalsIgnoreCase(mUserAccount);
        }
    }

    public void changeDeviceUpgradeState(String deviceId, int platform, int upgradeState) {
        if (mPresenter != null) {
            mPresenter.changeDeviceUpgradeState(mUserAccount, deviceId, platform, upgradeState);
        }
    }

    public void startNetworkDetect() {
        if (mPresenter != null) {
            mPresenter.startNetworkDetector();
        }
    }

    public void stopNetworkDetect() {
        if (mPresenter != null) {
            mPresenter.stopNetworkDetector();
        }
    }

    private void sendBroadcast(String action) {
        Intent pushIntent = new Intent();
        pushIntent.setAction(action);
        NotificationUtil.sendBroadcast(NooieApplication.mCtx, pushIntent);
    }

    public void finishForResult(Bundle bundle) {
        if (bundle != null) {
            Intent intent = new Intent();
            intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM, bundle);
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    public void tryDisconnectBluetooth() {
        NooieLog.d("-->> debug NooieBaseSupportActivity tryDisconnectBluetooth 1001");
        if (SmartBleManager.core.isBleConnect()) {
            NooieLog.d("-->> debug NooieBaseSupportActivity tryDisconnectBluetooth 1002 disconnectBle");
            SmartBleManager.core.disconnectBle();
        }
    }

    public void registerAppStateListener() {
        if (mAppStateManagerListener == null) {
            mAppStateManagerListener = new CustomAppStateManagerListener();
        }
        AppStateManager.getInstance().addListener(mAppStateManagerListener);
    }

    public void unRegisterAppStateListener() {
        if (mAppStateManagerListener != null) {
            AppStateManager.getInstance().removeListener(mAppStateManagerListener);
        }
    }

    public void showLoading(boolean cancel) {
        if (!isDestroyed()) {
            hideLoading();
            mLoadingDialog = DialogUtils.showLoadingDialog(this);
            mLoadingDialog.setCanceledOnTouchOutside(cancel);
        }
    }

    public void hideLoading() {
        if (!isDestroyed() && mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    private AlertDialog mLowBatteryDialog;
    private void showLowBatteryDialog(String name) {
        hideLowBatteryDialog();
        mLowBatteryDialog = DialogUtils.showInformationDialog(this, getString(R.string.nooie_play_low_battery_title), String.format(getString(R.string.nooie_play_low_battery_content), name), getString(R.string.confirm_upper), true, false, new DialogUtils.OnClickInformationDialogLisenter() {
            @Override
            public void onConfirmClick() {
            }
        });
    }

    private void hideLowBatteryDialog() {
        if (mLowBatteryDialog != null) {
            mLowBatteryDialog.dismiss();
        }
    }

    private UpdateShareDataReceiver mUpdateShareDataReceiver;

    private void registerUpdateShareDataReceiver() {
        if (mUpdateShareDataReceiver == null) {
            mUpdateShareDataReceiver = new UpdateShareDataReceiver() {
                @Override
                public void onUpdateGlobalData(String action) {
                    if (SDKConstant.ACTION_UPDATE_GLOBAL_DATA_FOR_LOGIN.equalsIgnoreCase(action)) {
                        initGlobalData();
                    } else if (SDKConstant.ACTION_UPDATE_GLOBAL_DATA_FOR_LOGOUT.equalsIgnoreCase(action)) {
                        unInitGlobalData();
                    }
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SDKConstant.ACTION_UPDATE_GLOBAL_DATA_FOR_LOGIN);
            intentFilter.addAction(SDKConstant.ACTION_UPDATE_GLOBAL_DATA_FOR_LOGOUT);
            NotificationUtil.registerLocalReceiver(NooieApplication.mCtx, mUpdateShareDataReceiver, intentFilter);
        }
    }

    private void unRegisterUpdateShareDataReceiver() {
        if (mUpdateShareDataReceiver != null) {
            NotificationUtil.unregisterLocalReceiver(NooieApplication.mCtx, mUpdateShareDataReceiver);
            mUpdateShareDataReceiver = null;
        }
    }

    private NetworkManagerReceiver mNetworkManagerReceiver;

    private void registerNetworkManagerReceiver() {
        if (mNetworkManagerReceiver == null) {
            mNetworkManagerReceiver = new NetworkManagerReceiver() {

                @Override
                public void onNetworkChanged() {
                    doOnNetworkChanged();
                }

                @Override
                public void onNetworkDetected(Bundle data) {
                    doOnNetworkDetected(data);
                }

                @Override
                public void onNetworkOperated(Bundle data) {
                    doOnNetworkOperated(data);
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SDKConstant.ACTION_NETWORK_MANAGER_ON_CHANGED);
            intentFilter.addAction(SDKConstant.ACTION_NETWORK_MANAGER_ON_DETECTED);
            intentFilter.addAction(SDKConstant.ACTION_NETWORK_MANAGER_ON_OPERATED);
            NotificationUtil.registerLocalReceiver(NooieApplication.mCtx, mNetworkManagerReceiver, intentFilter);
        }
    }

    private void unRegisterNetworkManagerReceiver() {
        if (mNetworkManagerReceiver != null) {
            NotificationUtil.unregisterLocalReceiver(NooieApplication.mCtx, mNetworkManagerReceiver);
            mNetworkManagerReceiver = null;
        }
    }

    public void doOnNetworkChanged() {
    }

    public void doOnNetworkDetected(Bundle data) {
    }

    public void doOnNetworkOperated(Bundle data) {
    }

    public void initDeviceCmdReceiver() {
        try {
            DeviceCmdService.getInstance(NooieApplication.mCtx).registerConnectBroadCast(NooieApplication.get());
        } catch (Exception e) {
        }
    }

    public void unInitDeviceCmdReceiver() {
        try {
            DeviceCmdService.getInstance(NooieApplication.mCtx).unregisterConnectBroadCast(null);
        } catch (Exception e) {
        }
    }

    private DeviceCmdReceiver mDeviceCmdReceiver;
    public void registerDeviceCmdReceiver() {
        mDeviceCmdReceiver = new DeviceCmdReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DeviceCmdService.CONNECT_CREATE);
        intentFilter.addAction(DeviceCmdService.CONNECT_BROKE);
        NotificationUtil.registerLocalReceiver(NooieApplication.mCtx, mDeviceCmdReceiver, intentFilter);
    }

    public void unregisterDeviceCmdReceiver() {
        if (mDeviceCmdReceiver != null) {
            NotificationUtil.unregisterLocalReceiver(NooieApplication.mCtx, mDeviceCmdReceiver);
            mDeviceCmdReceiver = null;
        }
    }

    public void onReceiveDeviceCmdConnect(String deviceId) {}

    public void onReceiveDeviceCmdDisconnect(String deviceId) {}

    class DeviceCmdReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                String deviceId = intent.getStringExtra(DeviceCmdService.CONNECT_UUID_KEY);
                NooieLog.d("-->> DeviceCmdReceiver onReceive action=" + action + " deviceId=" + deviceId);
                if (TextUtils.isEmpty(action) || TextUtils.isEmpty(deviceId)) {
                    return;
                }
                if (DeviceCmdService.CONNECT_CREATE.equalsIgnoreCase(action)) {
                    onReceiveDeviceCmdConnect(deviceId);
                } else if (DeviceCmdService.CONNECT_BROKE.equalsIgnoreCase(action)) {
                    onReceiveDeviceCmdDisconnect(deviceId);
                }
            }
        }
    }

    private class CustomAppStateManagerListener implements AppStateManager.AppStateManagerListener {

        @Override
        public void onAppBackground() {
            NooieLog.d("-->> debug BaseActivity onAppBackground");
            ApHelper.getInstance().checkBleApDeviceConnectionBackgroundKeepingTask(true, null);
        }

        @Override
        public void onAppForeground() {
            NooieLog.d("-->> debug BaseActivity onAppForeground");
            ApHelper.getInstance().checkBleApDeviceConnectionBackgroundKeepingTask(false, new ApHelper.BleApConnectionBackgroundKeepingListener() {
                @Override
                public void onResult(int state, Bundle param) {
                    if (isDestroyed()) {
                        return;
                    }
                    NooieLog.d("-->> debug BaseActivity onAppForeground 1002 state=" + state + " isEnterApDevicePage=" +  ApHelper.getInstance().getIsEnterApDevicePage());
                    if (state == ApHelper.BLE_AP_CONNECTION_KEEPING_BACKGROUND_STATE_DISCONNECTED && ApHelper.getInstance().getIsEnterApDevicePage()) {
                        NooieLog.d("-->> debug BaseActivity onAppForeground 1003");
                        HomeActivity.toHomeActivity(NooieBaseSupportActivity.this);
                    }
                }
            });
        }
    }

}
