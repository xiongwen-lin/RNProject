package com.afar.osaio.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.afar.osaio.R;
import com.afar.osaio.base.presenter.BaseActivityPresenterImpl;
import com.afar.osaio.base.presenter.IBaseActivityPresenter;
import com.afar.osaio.base.view.IBaseActivityView;
import com.afar.osaio.receiver.ForceLogoutBroadcastReceiver;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;
import com.megabox.android.slide.SlideBackFragmentActivity;
import com.nooie.common.bean.CConstant;
import com.nooie.common.utils.notify.NotificationUtil;
import com.nooie.sdk.api.network.base.bean.StateCode;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by victor on 2018/7/2
 * Email is victor.qiao.0604@gmail.com
 */
public class BaseFragmentActivity extends SlideBackFragmentActivity implements
        EasyPermissions.PermissionCallbacks, IBaseActivityView {
    protected static final int DEFAULT_SETTINGS_REQ_CODE = 0x10;

    private Intent mCurrentIntent;
    private volatile boolean isPause;
    private volatile boolean isDestroyed;
    private volatile boolean isFirstLaunch;
    private volatile Activity mCtx;
    private volatile IBaseActivityPresenter baseActivityPresenter;

    private AlertDialog mLoadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityStack.instance().add(this);

        this.mCurrentIntent = getIntent();
        isDestroyed = false;
        isFirstLaunch = true;
        baseActivityPresenter = new BaseActivityPresenterImpl(this);

        // hide title, fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.mCurrentIntent = getIntent();
    }

    public Intent getCurrentIntent() {
        return mCurrentIntent;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPause = false;
        registerForceLogoutBroadCast();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPause = true;
        isFirstLaunch = false;
        unRegisterForceLogoutBroadCast();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
        ActivityStack.instance().remove(this);
    }

    public boolean isPause() {
        return isPause;
    }

    protected void showLoading() {
        if (!isPause) {
            hideLoading();
            mLoadingDialog = DialogUtils.showLoadingDialog(this);
        }//LoadingDialog.createDefault(this).show();
    }

    protected void showLoading(boolean cancel) {
        if (!isPause) {
            hideLoading();
            mLoadingDialog = DialogUtils.showLoadingDialog(this);
            mLoadingDialog.setCanceledOnTouchOutside(cancel);
            /*
            LoadingDialog dialog = LoadingDialog.createDefault(this);
            dialog.setCanceledOnTouchOutside(cancel);
            dialog.show();
            */
        }
    }

    protected void hideLoading() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    protected void requestPermission(String[] perms) {
        if (EasyPermissions.hasPermissions(this, perms)) {
            permissionsGranted();
        } else {
            // request for one permission
            EasyPermissions.requestPermissions(this, getString(R.string.request_title), 100, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        permissionsGranted();
    }

    protected void permissionsGranted() {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
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
            }
        });
    }

    private void showRationaleAskDialog(final int requestCode, final List<String> perms) {
        if (perms.size() == 0) return;
        DialogUtils.showConfirmDialog(this, R.string.request_rationale_ask, R.string.cancel, R.string.request, false, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    return;
                } else {
                    ActivityCompat.requestPermissions(BaseFragmentActivity.this, perms.toArray(new String[perms.size()]), requestCode);
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
        }
    }

    protected void onReturnFromAppSettingActivity() {

    }

    //*****************************************Screen light**********************************************//

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


    //******************************************Input method**********************************************//

    /**
     * hide input method
     */
    protected void hideInputMethod() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
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

    //******************************************Force logout**********************************************//
    private ForceLogoutBroadcastReceiver mForceLogoutReceiver;

    private void registerForceLogoutBroadCast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(CConstant.ACTION_FORCE_LOGOUT);
        mForceLogoutReceiver = new ForceLogoutBroadcastReceiver(mCtx);
        NotificationUtil.registerLocalReceiver(NooieApplication.mCtx, mForceLogoutReceiver, intentFilter);
    }

    private void unRegisterForceLogoutBroadCast() {
        if (mForceLogoutReceiver != null) {
            NotificationUtil.unregisterLocalReceiver(NooieApplication.mCtx, mForceLogoutReceiver);
            mForceLogoutReceiver = null;
        }
    }

    //***************************************Share device dialog handle***********************************//
    @Override
    public void showLoadingDialog() {
        showLoading();
    }

    @Override
    public void hideLoadingDialog() {
        hideLoading();
    }

    @Override
    public void notifyHandleShareDeviceSuccess() {
        ToastUtil.showToast(mCtx, R.string.success);
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
}
