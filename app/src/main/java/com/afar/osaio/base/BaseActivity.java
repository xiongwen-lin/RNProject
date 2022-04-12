package com.afar.osaio.base;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.alibaba.android.arouter.launcher.ARouter;
import com.android.nordicbluetooth.SmartBleManager;
import com.afar.osaio.BuildConfig;
import com.afar.osaio.R;
import com.afar.osaio.account.activity.SignInActivity;
import com.afar.osaio.account.activity.SplashActivity;
import com.afar.osaio.account.helper.MyAccountHelper;
import com.afar.osaio.base.presenter.BaseActivityPresenterImpl;
import com.afar.osaio.base.presenter.IBaseActivityPresenter;
import com.afar.osaio.base.view.IBaseActivityView;
import com.afar.osaio.bean.CurrentDeviceParam;
import com.afar.osaio.bean.ShortLinkDeviceParam;
import com.afar.osaio.receiver.listener.MyAccountReceiverListener;
import com.afar.osaio.smart.device.bean.ListDeviceItem;
import com.afar.osaio.receiver.ForceLogoutBroadcastReceiver;
import com.afar.osaio.smart.device.helper.DeviceConnectionHelper;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.device.listener.ConnectShortLinkDeviceListener;
import com.afar.osaio.smart.device.listener.DeviceConnectionListener;
import com.afar.osaio.smart.device.listener.ShortLinkKeepListener;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.smart.push.bean.PushBatteryMessageExtras;
import com.afar.osaio.smart.push.bean.PushMessageBaseExtras;
import com.afar.osaio.smart.push.bean.PushShareMessageExtras;
import com.afar.osaio.smart.push.bean.PushUpdateMessageExtras;
import com.afar.osaio.smart.push.bean.PushActiveMessageExtras;
import com.afar.osaio.smart.push.helper.NooiePushMsgHelper;
import com.afar.osaio.util.SoftHideKeyBoardUtil;
import com.nooie.common.base.GlobalData;
import com.nooie.common.bean.CConstant;
import com.nooie.common.utils.encrypt.MD5Util;
import com.nooie.common.utils.notify.NotificationUtil;
import com.nooie.data.EventDictionary;
import com.nooie.eventtracking.EventTrackingApi;
import com.nooie.eventtracking.IAutoScreenTracker;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.cache.DeviceConnectionCache;
import com.nooie.sdk.receiver.AccountReceiver;
import com.nooie.sdk.receiver.NetworkWatcher;
import com.nooie.sdk.receiver.UpdateShareDataReceiver;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;
import com.gyf.immersionbar.ImmersionBar;
import com.megabox.android.slide.SlideBackActivity;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.nooie.common.hardware.bluetooth.BluetoothHelper;
import com.nooie.common.utils.json.GsonHelper;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.PushCode;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.device.DeviceCmdService;
import com.umeng.message.PushAgent;

import java.io.File;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

import static com.afar.osaio.util.preference.GlobalPrefs.KEY_DEVICE_PREVIEW;

public class BaseActivity extends SlideBackActivity implements
        EasyPermissions.PermissionCallbacks, IBaseActivityView, IAutoScreenTracker {
    protected static final int DEFAULT_SETTINGS_REQ_CODE = 0x10;

    private Intent mCurrentIntent;
    private volatile boolean isPause;
    private volatile boolean isDestroyed;
    private volatile boolean isFirstLaunch;
    private volatile boolean isCheckForceLogout = true;
    private IBaseActivityPresenter baseActivityPresenter;

    private volatile int requestPermissionSize = 0;

    private AlertDialog mLoadingDialog;
    private AlertDialog mNooieShareDialog;
    protected String mUid;
    protected String mToken;
    protected String mUserAccount;
    protected boolean isHomeActivity = false;

    private boolean mIsCheckLocationEnable = false;
    private boolean mIsCheckLocationPerm = false;
    private AlertDialog mCheckLocationEnableDialog = null;
    private AlertDialog mCheckBluetoothDialog = null;
    private AlertDialog mCheckLocalPermForBluetoothDialog = null;
    private MyAccountReceiverListener mAccountReceiverListener = null;
    private DeviceApHelperListener mDeviceApHelperListener;
    private CustomBleApConnectionFrontKeepingListener mCustomBleApConnectionFrontKeepingListener = null;
    private Dialog mApDirectConnectionErrorDialog = null;
    private boolean mIsShowSingleLoading = false;
    private String mQuickConnectShortLinkDeviceTaskId = null;
    private boolean mIsGotoOtherPage = false;
    private boolean mIsForceDisconnectShortLinkDevice = false;
    private Dialog mLpDeviceLimitTimeDialog;
    private Dialog mBleApConnectionKeepingDialog;
    private boolean mInputIsVisible = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NooieLog.d("-->> debug BaseActivity onCreate liveFlag=" + GlobalData.liveFlag + " saveInstanceState null " + (savedInstanceState == null));
        MyAccountHelper.getInstance().log("BaseActivity onCreate");

        /*
        if (NooieApplication.liveFlag == -1) {
            NooieLog.d("-->> BaseActivity onCreate liveFlag=" + NooieApplication.liveFlag);
            //todo open auto restart when liveFlag equals -1
            protectApp();
        }
         */

        if (GlobalData.liveFlag == 0) {
            NooieLog.d("-->> BaseActivity onCreate 1 liveFlag=" + GlobalData.liveFlag);
            //open auto restart when liveFlag equals 0
            protectApp();
        }

        NooieLog.d("-->> debug BaseActivity onCreate: eventId=" + getEventId(EventDictionary.EVENT_TRACK_TYPE_START) + " trackType=" + getTrackType());
        EventTrackingApi.getInstance().trackScreenEvent(this, System.currentTimeMillis(), EventDictionary.EVENT_TRACK_TYPE_START);

        ActivityStack.instance().add(this);

        this.mCurrentIntent = getIntent();
        isDestroyed = false;
        isFirstLaunch = true;
        baseActivityPresenter = new BaseActivityPresenterImpl(this);

        // hide title, fullscreen
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initGlobalData();
        PushAgent.getInstance(NooieApplication.mCtx).onAppStart();
        registerUpdateShareDataReceiver();
        setupStatusBar();
    }

    //设置低版本状态栏透明色
    protected void setPreMVersionStatusBar() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            View decorView = getWindow().getDecorView();
            decorView.setFitsSystemWindows(true);
        }
    }

    public void initGlobalData() {
        mUid = GlobalData.getInstance().getUid();
        mToken = GlobalData.getInstance().getToken();
        mUserAccount = GlobalData.getInstance().getAccount();
    }

    public void unInitGlobalData() {
        mUid = "";
        mToken = "";
        mUserAccount = "";
    }

    public void protectApp() {
        Intent intent = new Intent(BaseActivity.this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        NooieLog.d("-->> debug BaseActivity onStart: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyAccountHelper.getInstance().log("BaseActivity onResume");
        isPause = false;
        registerJPushReceiver();
        //registerForceLogoutBroadCast();
        registerAccountReceiverListener();
        registerDeviceApHelperListener();
        registerBleApConnectionFrontKeepingListener();
        setIsGotoOtherPage(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyAccountHelper.getInstance().log("BaseActivity onPause");
        isPause = true;
        isFirstLaunch = false;
        unregisterJPushReceiver();
        //unRegisterForceLogoutBroadCast();
        unRegisterAccountReceiverListener();
        unregisterDeviceApHelperListener();
        unregisterBleApConnectionFrontKeepingListener();
        hideInputMethod();
        hideNooieShareDialog();
        hideLpDeviceLimitTimeDialog();
        //checkIsNeedToDisconnectShortLinkDevice();
    }

    @Override
    protected void onStop() {
        super.onStop();
        NooieLog.d("-->> debug BaseActivity onStop: ");
    }

    @Override
    protected void onDestroy() {
        NooieLog.d("-->> debug BaseActivity onDestroy: ");
        NooieLog.d("-->> debug BaseActivity onDestroy: eventId=" + getEventId(EventDictionary.EVENT_TRACK_TYPE_END) + " trackType=" + getTrackType());
        EventTrackingApi.getInstance().trackScreenEvent(this, System.currentTimeMillis(), EventDictionary.EVENT_TRACK_TYPE_END);
        hideLoading();
        hideCheckLocationEnableDialog();
        hideCheckBluetoothDialog();
        hideCheckLocalPermForBluetoothDialog();
        hideNooieShareDialog();
        hideLowBatteryDialog();
        hideApDirectConnectionErrorDialog();
        super.onDestroy();
        unRegisterUpdateShareDataReceiver();
        mAccountReceiverListener = null;
        destroyDeviceApHelperListener();
        isDestroyed = true;
        ActivityStack.instance().remove(this);
    }

    /**
     * 禁止app字体大小跟随系统字体大小调节
     *
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

    /**
     * 根据背景模式适配状态栏
     */
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
        } else {
            ImmersionBar.with(this).statusBarDarkFont(false).fitsSystemWindows(true).statusBarColor(R.color.colorPrimary).keyboardEnable(true).init();
        }
    }

    /**
     * 当前页面状态栏背景模式，重写该方法即可采用不同模式
     *
     * @return
     */
    public int getStatusBarMode() {
        return ConstantValue.STATUS_BAR_DARK_MODE;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        NooieLog.d("-->> debug BaseActivity onNewIntent: ");
        //this.mCurrentIntent = getIntent();
        this.mCurrentIntent = intent != null ? intent : getIntent();
    }

    public Intent getCurrentIntent() {
        return mCurrentIntent;
    }

    public boolean isCurrentIntentNull() {
        return getCurrentIntent() == null;
    }

    public boolean isPause() {
        return isPause;
    }

    public boolean isFirstLaunch() {
        return isFirstLaunch;
    }

    protected void releaseRes() {
    }

    protected void requestPermission(String[] perms) {
        if (EasyPermissions.hasPermissions(this, perms)) {
            permissionsGranted();
        } else {
            // request for one permission
            //EasyPermissions.requestPermissions(this, getString(R.string.request_title), 100, perms);
            EasyPermissions.requestPermissions(new PermissionRequest.Builder(this, 100, perms)
                    .setRationale(R.string.request_title)
                    .setTheme(R.style.EasyPermissionsDialogStyle)
                    .build());
        }
    }

    protected void requestPermission(String[] perms, int requestCode) {
        if (EasyPermissions.hasPermissions(this, perms)) {
            permissionsGranted();
        } else {
            // request for one permission
            //EasyPermissions.requestPermissions(this, getString(R.string.request_title), 100, perms);
            EasyPermissions.requestPermissions(new PermissionRequest.Builder(this, requestCode, perms)
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
            permissionsGranted();
            permissionsGranted(requestCode);
        }
    }

    protected void permissionsGranted() {
    }

    protected void permissionsGranted(int requestcode) {
    }

    /**
     * 点击授权取消按钮时调用
     */
    public void cancelPermission() {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            showRationaleAskAgainDialog();
        } else {
            showRationaleAskDialog(requestCode, perms);
        }
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
                cancelPermission();
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
                    ActivityCompat.requestPermissions(BaseActivity.this, perms.toArray(new String[perms.size()]), requestCode);
                }
            }

            @Override
            public void onClickLeft() {
                cancelPermission();
            }
        });
    }

    public void requestPermissions(String[] perms) {
        if (EasyPermissions.hasPermissions(this, perms)) {
            NooieLog.e("-----------homeActivity 无权限  permissionsGranted");
            permissionsGranted();
        } else {
            NooieLog.e("-----------homeActivity 无权限  系统弹窗EasyPermission");
            EasyPermissions.requestPermissions(this, getResources().getString(R.string.permissions_required), 100, perms);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DEFAULT_SETTINGS_REQ_CODE) {
            // Do something after user returned from app settings screen, like showing a Toast.
            onReturnFromAppSettingActivity();
        }
    }

    protected void onReturnFromAppSettingActivity() {
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setIsGotoOtherPage(true);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        //savedInstanceState.putString("message", text.getText().toString());
        NooieLog.d("-->> debug BaseActivity onSaveInstanceState: isLogin=" + MyAccountHelper.getInstance().isLogin());
    }

    public Bundle getStartParam() {
        if (getCurrentIntent() == null) {
            return null;
        }
        return getCurrentIntent().getBundleExtra(ConstantValue.INTENT_KEY_START_PARAM);
    }

    /**
     * 返回页面事件Id
     *
     * @param trackType
     * @return
     */
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
     *
     * @return 默认不记录进入事件
     */
    @Override
    public int getTrackType() {
        return EventDictionary.EVENT_TRACK_TYPE_NONE;
    }

    /**
     * 返回页面埋点的页码,空值使用默认
     *
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

    /**
     * 返回拓展字段1
     *
     * @return
     */
    @Override
    public String getExternal() {
        return null;
    }

    /**
     * 返回拓展字段2
     *
     * @return
     */
    @Override
    public String getExternal2() {
        return null;
    }

    /**
     * 返回拓展字段3
     *
     * @return
     */
    @Override
    public String getExternal3() {
        return null;
    }

    /**
     * 返回设备id
     *
     * @return
     */
    @Override
    public String getUuid() {
        return null;
    }

    /**
     * keep screen light
     */
    public void keepScreenLongLight() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * close keep screen light
     */
    public void clearKeepScreenLongLight() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    protected void hideStatusBar() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);   // hide
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); // show
    }

    public void showVirtualBar() {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = getWindow().getDecorView();
            v.setSystemUiVisibility(View.VISIBLE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            View docorView = getWindow().getDecorView();
            int uiOption = View.SYSTEM_UI_FLAG_VISIBLE;//.SYSTEM_UI_FLAG_FULLSCREEN;
            docorView.setSystemUiVisibility(uiOption);
        }
    }

    public void hideVirtualBar() {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            View docorView = getWindow().getDecorView();
            int uiOption = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_FULLSCREEN;
            docorView.setSystemUiVisibility(uiOption);
        }
    }

    /**
     * hide input method
     */
    protected void hideInputMethod() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    protected void hideInputMethod(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * show input method
     */
    protected void showInputMethod() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(getWindow().getDecorView(), 0);
        }
    }

    public void registerInputListener() {
        SoftHideKeyBoardUtil.getInstance().register(this, new SoftHideKeyBoardUtil.SoftHideKeyBoardListener() {
            @Override
            public void onKeyBoardChange(boolean isVisible) {
                mInputIsVisible = isVisible;
            }
        });
    }

    public void unRegisterInputListener() {
        SoftHideKeyBoardUtil.getInstance().unRegister();
    }

    public void checkIsNeedToRequestLayout() {
        try {
            if (mInputIsVisible && findViewById(android.R.id.content) != null) {
                findViewById(android.R.id.content).requestLayout();
            }
        } catch (Exception e) {
        }
    }

    public void setCheckForceLogout(boolean checkForceLogout) {
        isCheckForceLogout = checkForceLogout;
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

    private void registerForceLogoutBroadCast() {
        if (!isCheckForceLogout) {
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

    private void unRegisterForceLogoutBroadCast() {
        if (mForceLogoutReceiver != null) {
            mForceLogoutReceiver.dismissAllDialog();
            NotificationUtil.unregisterLocalReceiver(NooieApplication.mCtx, mForceLogoutReceiver);
            mForceLogoutReceiver = null;
        }
    }

    @Override
    public void notifyHandleShareDeviceSuccess() {
        ToastUtil.showToast(this, R.string.success);
        Intent intent = new Intent(ConstantValue.BROADCAST_KEY_UPDATE_CAMERA);
        NotificationUtil.sendBroadcast(NooieApplication.mCtx, intent);
    }

    @Override
    public void notifyHandleShareDeviceFailed(int code) {
        if (isDestroyed()) {
            return;
        }
        if (code == StateCode.DEVICE_UNBINED.code || code == StateCode.UUID_NOT_EXISTED.code) {
            ToastUtil.showToast(this, R.string.api_code_1109);
        } else if (code == StateCode.SHARE_ACCOUNT_BOND_BY_DEVICE.code) {
            ToastUtil.showLongToast(this, R.string.share_send_invitation_wait);
        } else if (code == StateCode.SHARE_DEVICE_COUNT_OVER.code) {
            ToastUtil.showToast(this, NooieApplication.get().getString(R.string.share_device_count_over));
        } else {
            ToastUtil.showToast(this, NooieApplication.get().getString(R.string.get_fail));
        }
    }

    public void changeDeviceUpgradeState(String deviceId, int platform, int upgradeState) {
        if (baseActivityPresenter != null) {
            baseActivityPresenter.changeDeviceUpgradeState(mUserAccount, deviceId, platform, upgradeState);
        }
    }

    private void showNooieShareDialog(final int msgId, final int shareId, String title, String message, String subMessage) {
        hideNooieShareDialog();
        mNooieShareDialog = DialogUtils.showConfirmWithSubMsgDialog(this, title, message, R.string.ignore, R.string.accept, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                if (isPause) {
                    return;
                }
                baseActivityPresenter.handleNooieSharedDevice(msgId, shareId, true);
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
                //DialogUtils.showInformationDialog(BaseActivity.this, title, message);
                //ToastUtil.showToast(this, message);
                sendReceiveShareAgreeBroadcast();
            } else if (shareMessage.getCode() == PushCode.PUSH_SHARE_STATUS_TO_OWNER.code && shareMessage.getStatus() == ApiConstant.SYS_MSG_SHARE_STATUS_REJECT) {
                //%1$s rejected my shared %2$s
                message = String.format(getString(R.string.share_dialog_content_reject), shareMessage.getAccount(), shareMessage.getDevice());
                //DialogUtils.showInformationDialog(BaseActivity.this, title, message);
                //ToastUtil.showToast(BaseActivity.this, message);
            } else if (shareMessage.getCode() == PushCode.OWNER_REMOVE_SHARE_NOTIFY_SHARER.code) {
                //%1$s canceled sharing %2$s to me
                message = String.format(getString(R.string.share_dialog_content_cancel), shareMessage.getAccount(), shareMessage.getDevice());
                //DialogUtils.showInformationDialog(BaseActivity.this, title, message);
                //ToastUtil.showToast(BaseActivity.this, message);
                sendRemoveCameraBroadcast();
                gotoHomeActivity(shareMessage.getUuid());
            } else if (shareMessage.getCode() == PushCode.SHARER_REMOVER_SHARE_NOTIFY_OWNER.code) {
                //%1$s deleted my shared %2$s
                message = String.format(getString(R.string.share_dialog_content_remove), shareMessage.getAccount(), shareMessage.getDevice());
                //DialogUtils.showInformationDialog(BaseActivity.this, title, message);
                //ToastUtil.showToast(BaseActivity.this, message);
                sendReceiveShareAgreeBroadcast();
            }
        }
    }

    private void gotoHomeActivity(String deviceId) {
        if (TextUtils.isEmpty(deviceId) || TextUtils.isEmpty(getCurDeviceId()) || !deviceId.equalsIgnoreCase(getCurDeviceId())) {
            return;
        }
        redirectGotoHomePage();
    }

    public String getCurDeviceId() {
        return null;
    }

    public void showPushActiveMessage(PushActiveMessageExtras pushActiveExtras) {
    }

    private void sendRemoveCameraBroadcast() {
        Intent intent = new Intent(ConstantValue.BROADCAST_KEY_REMOVE_CAMERA);
        NotificationUtil.sendBroadcast(NooieApplication.mCtx, intent);
    }

    private void sendReceiveShareAgreeBroadcast() {
        Intent intent = new Intent(ConstantValue.BROADCAST_KEY_REMOVE_CAMERA);
        NotificationUtil.sendBroadcast(NooieApplication.mCtx, intent);
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

    class JPushBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            NooieLog.d("-->> JPushBroadcastReceiver onReceive");
            convertJPushCustomMessage(intent);
        }

        private void convertJPushCustomMessage(Intent intent) {
            Bundle pushData;
            if (intent == null || (pushData = intent.getBundleExtra(ConstantValue.INTENT_KEY_RECEIVE_JG_PUSH)) == null) {
                return;
            }
            //String message = pushData.getString(JPushInterface.EXTRA_MESSAGE);
            String extras = pushData.getString(NooiePushMsgHelper.NOOIE_PUSH_MSG_EXTRA);//pushData.getString(JPushInterface.EXTRA_EXTRA);
            if (!checkCurrentAccount(extras)) {
                return;
            }
            try {
                int code = NooiePushMsgHelper.getJPushMsgGode(extras);
                //NooieLog.d("-->> BaseActivity JPushBroadcastReceiver convertJPushCustomMessage code=" + NooieJPushMsgHelper.getJPushMsgGode(extras));
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

                /* notification 放到NooieService中接收
                if (code == PushCode.NORMAL_SYSTEM_MSG.code) {
                    JPushSysMessageExtras sysMessageExtras = gson.fromJson(extras, JPushSysMessageExtras.class);
                    NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, sysMessageExtras);
                } else if (code == PushCode.MOTION_DETECT.code || code == PushCode.SOUND_DETECT.code || code == PushCode.DEVICE_SD_LEAK.code) {
                    JPushDetectMessageExtras detectExtras = gson.fromJson(extras, JPushDetectMessageExtras.class);
                    NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, detectExtras);
                } else if (code == PushCode.PUSH_SHARE_MSG_TO_SHARER.code || code == PushCode.PUSH_SHARE_STATUS_TO_OWNER.code || code == PushCode.OWNER_REMOVE_SHARE_NOTIFY_SHARER.code || code == PushCode.SHARER_REMOVER_SHARE_NOTIFY_OWNER.code) {
                    JPushShareMessageExtras shareExtras = gson.fromJson(extras, JPushShareMessageExtras.class);
                    showNooieShareMessage(shareExtras);
                }  else if (code == PushCode.ORDER_FINISH.code) {
                    JPushOrderMessageExtras orderExtras = gson.fromJson(extras, JPushOrderMessageExtras.class);
                    NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, orderExtras);
                }  else if (code == PushCode.BACKGROUND_DEAL_FEEDBACK_STATUS.code) {
                    JPushFeedbackMessageExtras feedbackExtras = gson.fromJson(extras, JPushFeedbackMessageExtras.class);
                    NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, feedbackExtras);
                }  else if (code == PushCode.DEVICE_UPDATE_START.code || code == PushCode.DEVICE_UPDATE_SUCCESS.code || code == PushCode.DEVICE_UPDATE_FAILED.code) {
                    JPushUpdateMessageExtras updateExtras = gson.fromJson(extras, JPushUpdateMessageExtras.class);
                    NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, updateExtras);
                } else if (code == PushCode.CLOUD_SUBSCRIBE_CANCEL.code || code == PushCode.CLOUD_SUBSCRIBE_RENEWAL.code || code == PushCode.CLOUD_SUBSCRIBE_EXPIRED.code) {
                    JPushSubscribeMessageExtras subscribeExtras = gson.fromJson(extras, JPushSubscribeMessageExtras.class);
                    NotificationManager.getInstance().showJPushNotification(NooieApplication.mCtx, subscribeExtras);
                }
                */
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private boolean checkCurrentAccount(String extras) {
            PushMessageBaseExtras jExtras = GsonHelper.convertJson(extras, PushMessageBaseExtras.class);
            return jExtras != null && !TextUtils.isEmpty(jExtras.getUser_account()) && jExtras.getUser_account().equalsIgnoreCase(mUserAccount);
        }
    }

    public void sendRefreshPicture(String path) {
        if (TextUtils.isEmpty(path) || !new File(path).exists()) {
            return;
        }
        try {
            File file = new File(path);
            Uri uri = Uri.fromFile(file);
            NooieApplication.mCtx.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        } catch (Exception e) {
        }
    }

    public static String getDevicePreviewFile(String deviceId) {
        String account = GlobalData.getInstance().getAccount();
        if (TextUtils.isEmpty(account) || TextUtils.isEmpty(deviceId)) {
            return null;
        }
        String key = String.format("%s_%s", KEY_DEVICE_PREVIEW, deviceId);
        return GlobalPrefs.getString(NooieApplication.mCtx, account, key, "");
    }

    public String createSortLinkDeviceTaskId(String account, String deviceId) {
        return MD5Util.MD5Hash(new StringBuilder().append(account).append(deviceId).append(System.currentTimeMillis()).toString());
    }

    public void setIsGotoOtherPage(boolean isGotoOtherPage) {
        mIsGotoOtherPage = isGotoOtherPage;
    }

    public void setIsForceDisconnectShortLinkDevice(boolean isForce) {
        mIsForceDisconnectShortLinkDevice = isForce;
    }

    public boolean getIsForceDisconnectShortLinkDevice() {
        return mIsForceDisconnectShortLinkDevice;
    }

    public void setIsDestroyShortLink(boolean isDestroyShortLink) {
        DeviceConnectionHelper.getInstance().setIsDestroyShortLink(isDestroyShortLink);
    }

    public boolean checkIsDestroyShortLink() {
        return DeviceConnectionHelper.getInstance().checkIsDestroyShortLink();
    }

    public void tryConnectShortLinkDevice() {
        if (getShortLinkDeviceParam() == null) {
            return;
        }
        tryConnectShortLinkDevice(getShortLinkDeviceParam().getAccount(), getShortLinkDeviceParam().getDeviceId(), getShortLinkDeviceParam().getModel(), getShortLinkDeviceParam().isSubDevice(), getShortLinkDeviceParam().getConnectionMode());
    }

    public void tryConnectShortLinkDevice(String account, String deviceId, String model, boolean isSubDevice, int connectionMode) {
        NooieLog.d("-->> debug NooieDeviceSettingActivity tryConnectShortLinkDevice: 1000 sortLinkDevice account=" + account + "deviceId=" + " model=" + model + " isSubDevice=" + isSubDevice);
        if (!NooieDeviceHelper.isSortLinkDevice(model, isSubDevice, connectionMode) || !checkIsDestroyShortLink() || DeviceConnectionCache.getInstance().isConnectionExist(deviceId)) {
            if (NooieDeviceHelper.isSortLinkDevice(model, isSubDevice, connectionMode) && DeviceConnectionCache.getInstance().isConnectionExist(deviceId)) {
                dealAfterDeviceShortLink();
            }
            return;
        }
        setIsDestroyShortLink(false);
        tryStopConnectShortLinkDevice();
        showSingleLoading(true);
        mQuickConnectShortLinkDeviceTaskId = createSortLinkDeviceTaskId(account, deviceId);
        NooieLog.d("-->> debug NooieDeviceSettingActivity tryConnectShortLinkDevice: 1001 sortLinkDevice deviceId=" + deviceId + " taskId=" + mQuickConnectShortLinkDeviceTaskId);
        DeviceConnectionHelper.getInstance().startQuickConnectShortLinkDevice(mQuickConnectShortLinkDeviceTaskId, account, deviceId, model, isSubDevice, connectionMode, new ConnectShortLinkDeviceListener() {
            @Override
            public void onResult(int code, String taskId, String account, String deviceId) {
                if (isDestroyed()) {
                    return;
                }
                NooieLog.d("-->> debug NooieDeviceSettingActivity tryConnectShortLinkDevice: 1002 sortLinkDevice deviceId" + deviceId + " taskId=" + taskId + " code=" + code + " mTaskId=" + mQuickConnectShortLinkDeviceTaskId);
                hideSingleLoading();
                if (TextUtils.isEmpty(mQuickConnectShortLinkDeviceTaskId) || !mQuickConnectShortLinkDeviceTaskId.equals(taskId)) {
                    tryStopConnectShortLinkDevice();
                    DeviceConnectionHelper.getInstance().stopShortLinkKeepTask();
                    return;
                }
                boolean isIgnoreCallback = code == DeviceConnectionHelper.CONNECT_SHORT_LINK_DEVICE_RECEIVE_FAIL;
                if (isIgnoreCallback) {
                    return;
                }
                tryStopConnectShortLinkDevice();
                boolean isConnectShortLinkDeviceFinish = code == DeviceConnectionHelper.CONNECT_SHORT_LINK_DEVICE_SUCCESS || code == DeviceConnectionHelper.CONNECT_SHORT_LINK_DEVICE_COUNTDOWN_SUCCESS
                        || code == DeviceConnectionHelper.CONNECT_SHORT_LINK_DEVICE_COUNTDOWN_ERROR || code == DeviceConnectionHelper.CONNECT_SHORT_LINK_DEVICE_EXIST;
                if (isConnectShortLinkDeviceFinish) {
//                    DeviceConnectionHelper.getInstance().startSendHeartBeat(deviceId);
                    dealAfterDeviceShortLink();
                } else {
                    DeviceConnectionHelper.getInstance().stopShortLinkKeepTask();
                }
            }
        });
    }

    public void tryStopConnectShortLinkDevice() {
        DeviceConnectionHelper.getInstance().stopQuickConnectShortLinkDevice();
        mQuickConnectShortLinkDeviceTaskId = null;
    }

    public void tryDisconnectShortLinkDevice(String deviceId, String model, boolean isSubDevice, boolean isForceStop, int connectionMode) {
        NooieLog.d("-->> debug NooieDeviceSettingActivity tryDisconnectShortLinkDevice: 1000 sortLinkDevice deviceId=" + deviceId + " model=" + model + " isSubDevice=" + isSubDevice);
        if (!NooieDeviceHelper.isSortLinkDevice(model, isSubDevice, connectionMode)) {
            setIsDestroyShortLink(true);
            return;
        }
        NooieLog.d("-->> debug NooieDeviceSettingActivity tryDisconnectShortLinkDevice: 1001 sortLinkDevice deviceId=" + deviceId);
        hideLoading();
        disconnectSortLinkDevice(deviceId);
        mQuickConnectShortLinkDeviceTaskId = null;
        setIsDestroyShortLink(true);
    }

    public void disconnectSortLinkDevice(String deviceId) {
        DeviceConnectionHelper.getInstance().stopQuickConnectShortLinkDevice();
        DeviceConnectionHelper.getInstance().stopShortLinkKeepTask();
        DeviceConnectionCache.getInstance().removeConnection(deviceId);
    }

    public void checkIsNeedToDisconnectShortLinkDevice() {
        if (getShortLinkDeviceParam() == null) {
            return;
        }
        tryDisconnectShortLinkDevice(getShortLinkDeviceParam().getDeviceId(), getShortLinkDeviceParam().getModel(), getShortLinkDeviceParam().isSubDevice(), false, getShortLinkDeviceParam().getConnectionMode());
    }

    public boolean checkIsGotoOtherPage() {
        return mIsGotoOtherPage;
    }

    public void dealAfterDeviceShortLink() {
    }

    public ShortLinkDeviceParam getShortLinkDeviceParam() {
        return null;
    }

    public void dealForLpLimitWarn(boolean isReconnect) {
        if (isDestroyed() || getShortLinkDeviceParam() == null) {
            return;
        }
        if (!isReconnect) {
            tryDisconnectShortLinkDevice(getShortLinkDeviceParam().getDeviceId(), getShortLinkDeviceParam().getModel(), getShortLinkDeviceParam().isSubDevice(), false, getShortLinkDeviceParam().getConnectionMode());
            redirectGotoHomePage();
        }
    }

    private void sendBroadcast(String action) {
        Intent pushIntent = new Intent();
        pushIntent.setAction(action);
        NotificationUtil.sendBroadcast(NooieApplication.mCtx, pushIntent);
    }

    @Override
    public void showLoadingDialog() {
        showLoading();
    }

    @Override
    public void hideLoadingDialog() {
        hideLoading();
    }

    protected void showLoading() {
        if (mIsShowSingleLoading) {
            return;
        }
        if (!isPause) {
            hideLoading();
            mLoadingDialog = DialogUtils.showLoadingDialog(this);
        }
    }

    protected void showLoading(String test) {
        hideLoading();
        mLoadingDialog = DialogUtils.showLoadingDialog(this);
        mLoadingDialog.setCanceledOnTouchOutside(false);
    }

    protected void showLoading(boolean cancel) {
        if (mIsShowSingleLoading) {
            return;
        }
        if (!isPause) {
            hideLoading();
            mLoadingDialog = DialogUtils.showLoadingDialog(this);
            mLoadingDialog.setCanceledOnTouchOutside(cancel);
        }
    }

    protected void showNormalLoading(boolean cancel) {
        if (mIsShowSingleLoading) {
            return;
        }
        if (!isPause) {
            hideLoading();
            mLoadingDialog = DialogUtils.showLoadingDialog(this, cancel);
        }
    }

    protected void showSingleLoading(boolean cancel) {
        if (!isPause) {
            hideLoading();
            mIsShowSingleLoading = true;
            mLoadingDialog = DialogUtils.showLoadingDialog(this, cancel);
        }
    }

    protected void hideLoading() {
        if (mIsShowSingleLoading) {
            return;
        }
        if (!isDestroyed && mLoadingDialog != null) {
            mLoadingDialog.dismiss();
            mLoadingDialog = null;
        }
    }

    protected void hideSingleLoading() {
        if (!isDestroyed && mLoadingDialog != null) {
            mLoadingDialog.dismiss();
            mLoadingDialog = null;
        }
        mIsShowSingleLoading = false;
    }

    public boolean checkNull(Object... args) {
        for (Object object : args) {
            if (object == null) {
                return true;
            }
        }

        return false;
    }

    public void displayViewAll(boolean show, View... views) {
        if (views == null || views.length == 0) {
            return;
        }
        for (View view : views) {
            if (view != null) {
                view.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        }
    }

    public void finishForResult(Bundle bundle) {
        if (bundle != null) {
            Intent intent = new Intent();
            intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM, bundle);
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    public void sendUpdateCameraBroadcast() {
        Intent intent = new Intent(ConstantValue.BROADCAST_KEY_UPDATE_CAMERA);
        NotificationUtil.sendBroadcast(NooieApplication.mCtx, intent);
    }

    public boolean isLocationPermEnable() {
        return BluetoothHelper.isLocationEnabled(NooieApplication.mCtx) && EasyPermissions.hasPermissions(NooieApplication.mCtx, ConstantValue.PERM_GROUP_LOCATION);
    }

    public boolean checkUseLocationEnable() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || (BluetoothHelper.isLocationEnabled(NooieApplication.mCtx) && EasyPermissions.hasPermissions(NooieApplication.mCtx, ConstantValue.PERM_GROUP_LOCATION));
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

    public boolean requestLocationPerm(String title, String tipContent, String leftBtn, String rightBtn, DialogUtils.OnClickConfirmButtonListener listener, boolean isForceCheck) {
        if (!BluetoothHelper.isLocationEnabled(NooieApplication.mCtx)) {
            if (isForceCheck || !mIsCheckLocationEnable) {
                mIsCheckLocationEnable = true;
                showCheckLocationEnableDialog(title, tipContent, leftBtn, rightBtn, listener);
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

    public void checkLocalPerm(String tipContent, boolean isCheckPerm) {

        if (!BluetoothHelper.isLocationEnabled(NooieApplication.mCtx)) {
            if (!mIsCheckLocationEnable) {
                mIsCheckLocationEnable = true;
                showCheckLocationEnableDialog(tipContent);
            }
            return;
        }

        if (isCheckPerm && !mIsCheckLocationPerm) {
            mIsCheckLocationPerm = true;
            showCheckLocationPermDialog();
        }
    }

    public boolean isBluetoothReady() {
        return BluetoothHelper.isBluetoothOn() && BluetoothHelper.isLocationEnabled(NooieApplication.mCtx) && EasyPermissions.hasPermissions(NooieApplication.mCtx, ConstantValue.PERM_GROUP_LOCATION);
    }

    public void checkBluetooth(String tipContent, boolean isForceCheck) {

        if (!BluetoothHelper.isBluetoothOn()) {
            showCheckBluetoothDialog(getString(R.string.add_bluetooth_device_check_bluetooth_title), getString(R.string.add_bluetooth_device_check_bluetooth_content), getString(R.string.cancel), getString(R.string.settings), null);
            return;
        }

        if (!BluetoothHelper.isLocationEnabled(NooieApplication.mCtx)) {
            if (isForceCheck || !mIsCheckLocationEnable) {
                mIsCheckLocationEnable = true;
                showCheckLocationEnableDialog(tipContent);
            }
            return;
        }

        if (!EasyPermissions.hasPermissions(NooieApplication.mCtx, ConstantValue.PERM_GROUP_LOCATION)) {
            if (isForceCheck || !mIsCheckLocationPerm) {
                mIsCheckLocationPerm = true;
                showCheckLocationPermDialog();
            }
        }
    }

    public void checkBluetoothIsOn(String tipTitle, String tipContent, String leftBtnTxt, String rightBtnTxt, DialogUtils.OnClickConfirmButtonListener listener) {
        if (!BluetoothHelper.isBluetoothOn()) {
            showCheckBluetoothDialog(tipTitle, tipContent, leftBtnTxt, rightBtnTxt, listener);
            return;
        }
    }

    public void showCheckLocationEnableDialog(String tipContent) {
        showCheckLocationEnableDialog(getString(R.string.dialog_tip_title), tipContent, getString(R.string.cancel), getString(R.string.settings), null);
    }

    public void showCheckLocationEnableDialog(String title, String tipContent, String leftBtn, String rightBtn, DialogUtils.OnClickConfirmButtonListener listener) {
        hideCheckLocationEnableDialog();
        mCheckLocationEnableDialog = DialogUtils.showConfirmWithSubMsgDialog(this, title, tipContent, leftBtn, rightBtn, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                if (listener != null) {
                    listener.onClickRight();
                }
            }

            @Override
            public void onClickLeft() {
                if (listener != null) {
                    listener.onClickLeft();
                }
            }
        });
    }

    public void hideCheckLocationEnableDialog() {
        if (mCheckLocationEnableDialog != null) {
            mCheckLocationEnableDialog.dismiss();
            mCheckLocationEnableDialog = null;
        }
    }

    public void showCheckBluetoothDialog(String tipTitle, String tipContent, String leftBtnTxt, String rightBtnTxt, DialogUtils.OnClickConfirmButtonListener listener) {
        hideCheckBluetoothDialog();
        mCheckBluetoothDialog = DialogUtils.showConfirmWithSubMsgDialog(this, tipTitle, tipContent, leftBtnTxt, rightBtnTxt, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                BluetoothHelper.startBluetooth(BaseActivity.this, ConstantValue.REQUEST_CODE_FOR_ENABLE_BLUE);
                if (listener != null) {
                    listener.onClickRight();
                }
            }

            @Override
            public void onClickLeft() {
                if (listener != null) {
                    listener.onClickLeft();
                }
            }
        });
    }

    public void hideCheckBluetoothDialog() {
        if (mCheckBluetoothDialog != null) {
            mCheckBluetoothDialog.dismiss();
            mCheckBluetoothDialog = null;
        }
    }

    public void showCheckLocalPermForBluetoothDialog(DialogUtils.OnClickConfirmButtonListener listener) {
        hideCheckLocalPermForBluetoothDialog();
        mCheckLocalPermForBluetoothDialog = DialogUtils.showConfirmWithSubMsgDialog(this, R.string.dialog_tip_title, R.string.bluetooth_location_permission_request_tip, R.string.cancel, R.string.settings, listener);
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
            mLowBatteryDialog = null;
        }
    }

    private void showApDirectConnectionErrorDialog() {
        hideApDirectConnectionErrorDialog();
        if (mApDirectConnectionErrorDialog == null) {
            mApDirectConnectionErrorDialog = DialogUtils.showInformationDialog(this, getString(R.string.dialog_tip_title), getString(R.string.switch_connection_mode_ap_direct_connection_error), getString(R.string.confirm_upper), true, false, new DialogUtils.OnClickInformationDialogLisenter() {
                @Override
                public void onConfirmClick() {
                    ApHelper.getInstance().updateApDirectConnectionErrorCount(true);
                }
            });
        }
    }

    private void hideApDirectConnectionErrorDialog() {
        if (mApDirectConnectionErrorDialog != null) {
            mApDirectConnectionErrorDialog.dismiss();
            mApDirectConnectionErrorDialog = null;
        }
    }

    private void showLpDeviceLimitTimeDialog() {
        hideLpDeviceLimitTimeDialog();
        mLpDeviceLimitTimeDialog = DialogUtils.showConfirmWithSubMsgDialog(this, R.string.nooie_play_limit_time_title, R.string.nooie_play_limit_time_content_short_link, R.string.cancel, R.string.confirm_upper, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                dealForLpLimitWarn(true);
            }

            @Override
            public void onClickLeft() {
                dealForLpLimitWarn(false);
            }
        });
    }

    private void hideLpDeviceLimitTimeDialog() {
        if (mLpDeviceLimitTimeDialog != null) {
            mLpDeviceLimitTimeDialog.dismiss();
        }
    }

    private boolean checkApDirectConnectionErrorDialogShowing() {
        return !(isDestroyed() || isPause()) && mApDirectConnectionErrorDialog != null && mApDirectConnectionErrorDialog.isShowing();
    }

    public void switchCheckDeviceConnection(boolean isPause) {
        DeviceConnectionHelper.getInstance().switchCheckDeviceConnection(isPause);
    }

    private NetworkWatcher.OnNetworkChangedListener mNetworkWatcherListener = null;

    public void addNetworkWatcherListener(NetworkWatcher.OnNetworkChangedListener listener) {
        if (listener == null) {
            return;
        }
        mNetworkWatcherListener = listener;
        NetworkWatcher.registerNetworkWatcher(mNetworkWatcherListener);
    }

    public void removeNetworkWatcherListener() {
        NetworkWatcher.unregisterNetworkWatcher(mNetworkWatcherListener);
    }

    private DeviceConnectionListener mDeviceConnectionListener = null;

    public void registerDeviceConnectionListener() {
        if (mDeviceConnectionListener == null) {
            mDeviceConnectionListener = new DeviceConnectionListener() {
                @Override
                public void onReInitDeviceConn() {
                    if (isDestroyed || !BuildConfig.DEBUG) {
                        return;
                    }
                    ToastUtil.showToast(BaseActivity.this, "The device is reconnecting, please return to the homepage and wait a while and try again.");
                }
            };
        }
        DeviceConnectionHelper.getInstance().addListener(mDeviceConnectionListener);
    }

    public void unRegisterDeviceConnectionListener() {
        if (mDeviceConnectionListener != null) {
            DeviceConnectionHelper.getInstance().removeListener(mDeviceConnectionListener);
            mDeviceConnectionListener = null;
        }
    }

    public void registerShortLinkKeepListener() {
        DeviceConnectionHelper.getInstance().setShortLinkKeepListener(new ShortLinkKeepListener() {
            @Override
            public void onShortLinkKeep(String taskId, int type) {
                NooieLog.d("-->> debug BaseActivity onShortLinkKeep: taskId=" + taskId + " type=" + type);
                if (type == DeviceConnectionHelper.SHORT_LINK_KEEP_TYPE_TIME_END) {
                    showLpDeviceLimitTimeDialog();
                } else if (type == DeviceConnectionHelper.SHORT_LINK_KEEP_TYPE_ERROR) {
                }
            }
        });
    }

    public void unRegisterShortLinkKeepListener() {
        DeviceConnectionHelper.getInstance().setShortLinkKeepListener(null);
    }

    class DevicesChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            onReceiveDeviceChange(intent);
        }
    }

    public void onReceiveDeviceChange(Intent intent) {
    }

    private DevicesChangeReceiver mDevicesChangeReceiver;

    public void registerDevicesChangeReceiver() {
        if (mDevicesChangeReceiver == null) {
            mDevicesChangeReceiver = new DevicesChangeReceiver();
            IntentFilter intentFilter = new IntentFilter(ConstantValue.BROADCAST_KEY_REMOVE_CAMERA);
            intentFilter.addAction(ConstantValue.BROADCAST_KEY_UPDATE_CAMERA);
            NotificationUtil.registerLocalReceiver(NooieApplication.mCtx, mDevicesChangeReceiver, intentFilter);
        }
    }

    public void unRegisterDevicesChangeReceiver() {
        if (mDevicesChangeReceiver != null) {
            NotificationUtil.unregisterLocalReceiver(NooieApplication.mCtx, mDevicesChangeReceiver);
            mDevicesChangeReceiver = null;
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

    public void initDeviceCmdReceiver() {
        DeviceCmdService.getInstance(NooieApplication.mCtx).registerConnectBroadCast(NooieApplication.get());
    }

    public void unInitDeviceCmdReceiver() {
        DeviceCmdService.getInstance(NooieApplication.mCtx).unregisterConnectBroadCast(null);
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

    public void onReceiveDeviceCmdConnect(String deviceId) {
    }

    public void onReceiveDeviceCmdDisconnect(String deviceId) {
    }

    class DeviceCmdReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                String deviceId = intent.getStringExtra(DeviceCmdService.CONNECT_UUID_KEY);
                NooieLog.d("-->> DeviceCmdReceiver onReceive B action=" + action + " deviceId=" + deviceId);
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

    private void registerDeviceApHelperListener() {
        if (isDestroyed() || !checkIsAddDeviceApHelperListener()) {
            return;
        }
        if (mDeviceApHelperListener == null) {
            mDeviceApHelperListener = new DeviceApHelperListener();
        }
        ApHelper.getInstance().addListener(mDeviceApHelperListener);
    }

    private void unregisterDeviceApHelperListener() {
        if (!checkIsAddDeviceApHelperListener()) {
            return;
        }
        if (mDeviceApHelperListener != null) {
            ApHelper.getInstance().removeListener(mDeviceApHelperListener);
        }
    }

    private void destroyDeviceApHelperListener() {
        if (!checkIsAddDeviceApHelperListener()) {
            return;
        }
        if (mDeviceApHelperListener != null) {
            unregisterDeviceApHelperListener();
            mDeviceApHelperListener = null;
        }
    }

    public boolean checkIsAddDeviceApHelperListener() {
        return false;
    }

    public void dealOnApHeartBeatResponse(int code) {
        NooieLog.d("-->> debug BaseActivity dealOnApHeartBeatResponse: code=" + code);
        if (isDestroyed() || isPause() || checkApDirectConnectionErrorDialogShowing() || !ApHelper.getInstance().checkApDirectConnectionIsError()) {
            return;
        }
        showApDirectConnectionErrorDialog();
    }

    public CurrentDeviceParam getCurrentDeviceParam() {
        return null;
    }

    public boolean checkIsBleApConnectionFrontKeepingEnable() {
        return getCurrentDeviceParam() != null && NooieDeviceHelper.isBleApLpDevice(getCurrentDeviceParam().getModel(), getCurrentDeviceParam().getConnectionMode());
    }

    public void checkBleApDirectIsDestroy(int connectionMode) {
        if (connectionMode != ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            return;
        }
        if (!ApHelper.getInstance().checkBleApDeviceConnectingExist()) {
            redirectGotoHomePage();
        }
    }

    public void tryDisconnectBluetooth() {
        NooieLog.d("-->> debug BaseActivity tryDisconnectBluetooth 1001");
        if (SmartBleManager.core != null && SmartBleManager.core.isBleConnect()) {
            NooieLog.d("-->> debug BaseActivity tryDisconnectBluetooth 1002 disconnectBle");
            SmartBleManager.core.disconnectBle();
        }
    }

    public void redirectGotoHomePage() {
        if (isDestroyed()) {
            return;
        }
        HomeActivity.toHomeActivity(this);
        finish();
    }

    private void registerBleApConnectionFrontKeepingListener() {
        if (!checkIsBleApConnectionFrontKeepingEnable()) {
            return;
        }
        if (mCustomBleApConnectionFrontKeepingListener == null) {
            mCustomBleApConnectionFrontKeepingListener = new CustomBleApConnectionFrontKeepingListener();
        }
        ApHelper.getInstance().setBleApDeviceConnectionFrontKeepingTask(mCustomBleApConnectionFrontKeepingListener);
    }

    private void unregisterBleApConnectionFrontKeepingListener() {
        if (!checkIsBleApConnectionFrontKeepingEnable()) {
            return;
        }
        mCustomBleApConnectionFrontKeepingListener = null;
        ApHelper.getInstance().setBleApDeviceConnectionFrontKeepingTask(null);
    }

    private void dealForBleApConnectionFrontKeeping(boolean isReconnect, Bundle param) {
        if (isDestroyed() || !checkIsBleApConnectionFrontKeepingEnable()) {
            return;
        }
        NooieLog.d("-->> debug BaseActivity dealForBleApConnectionFrontKeeping: isReconnect=" + isReconnect);
        if (!isReconnect) {
            showLoading();
            NooieLog.d("-->> debug turn off hot spot 设置页面使用超过3分钟断开直连");
            ApHelper.getInstance().disconnectBleApDeviceConnection(param, new ApHelper.APDirectListener() {
                @Override
                public void onSwitchConnectionMode(boolean result, int connectionMode, String deviceId) {
                    hideLoading();
                    redirectGotoHomePage();
                }
            });
        }
    }

    private void dealOnBleApConnectionFrontKeepingResponse(int state, Bundle param) {
        if (isDestroyed() || isPause() || !checkIsBleApConnectionFrontKeepingEnable()) {
            return;
        }
        if (state == ApHelper.BLE_AP_CONNECTION_KEEPING_FRONT_STATE_TIME_OUT) {
            showBleApConnectionFrontKeepingDialog(param);
        }
    }

    private void showBleApConnectionFrontKeepingDialog(Bundle param) {
        hideBleApConnectionFrontKeepingDialog();
        mBleApConnectionKeepingDialog = DialogUtils.showConfirmWithSubMsgDialog(this, R.string.nooie_play_limit_time_title, R.string.nooie_play_limit_time_content_short_link, R.string.cancel, R.string.confirm_upper, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                dealForBleApConnectionFrontKeeping(true, param);
            }

            @Override
            public void onClickLeft() {
                dealForBleApConnectionFrontKeeping(false, param);
            }
        });
    }

    private void hideBleApConnectionFrontKeepingDialog() {
        if (mBleApConnectionKeepingDialog != null) {
            mBleApConnectionKeepingDialog.dismiss();
        }
    }

    private class DeviceApHelperListener implements ApHelper.ApHelperListener {

        @Override
        public void onApHeartBeatResponse(int code) {
            dealOnApHeartBeatResponse(code);
        }

        @Override
        public void onNetworkChange() {
        }
    }

    private class CustomBleApConnectionFrontKeepingListener implements ApHelper.BleApConnectionFrontKeepingListener {

        @Override
        public void onResult(int state, Bundle param) {
            dealOnBleApConnectionFrontKeepingResponse(state, param);
        }
    }
}
