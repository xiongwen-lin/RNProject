package com.afar.osaio.smart.smartlook.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.smart.smartlook.bean.BleConnectState;
import com.afar.osaio.smart.smartlook.bean.BleDevice;
import com.afar.osaio.smart.smartlook.cache.BleDeviceScanCache;
import com.afar.osaio.smart.smartlook.contract.LookDeviceContract;
import com.afar.osaio.smart.smartlook.helper.SmartLookDeviceHelper;
import com.afar.osaio.smart.smartlook.presenter.LookDevicePresenter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.NormalTextIconView;
import com.nooie.common.utils.encrypt.SmartLookEncrypt;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LookDeviceActivity extends BaseActivity implements LookDeviceContract.View {

    private static final int LOCK_SWITCH_STATE_OPEN = 1;
    private static final int LOCK_SWITCH_STATE_CLOSE = 2;

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.btnLookSwitch)
    ImageView btnLookSwitch;
    @BindView(R.id.tivPassword)
    NormalTextIconView tivPassword;
    @BindView(R.id.tivRecording)
    NormalTextIconView tivRecording;
    @BindView(R.id.tivAuthorization)
    NormalTextIconView tivAuthorization;
    @BindView(R.id.tivRename)
    NormalTextIconView tivRename;
    @BindView(R.id.tivUser)
    NormalTextIconView tivUser;
    @BindView(R.id.tivBluetooth)
    NormalTextIconView tivBluetooth;

    LookDeviceContract.Presenter mPresenter;
    boolean mIsBleDeviceVerify = false;
    String mDeviceId;
    String mPhone;
    String mPassword;
    boolean mIsAdmin;
    byte[] mSec;

    public static void toLookDeviceActivity(Context from, String deviceId, int routeSource, String phone, String password, boolean isAdmin, String name, byte[] sec) {
        Intent intent = new Intent(from, LookDeviceActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_ROUTE_SOURCE, routeSource);
        intent.putExtra(ConstantValue.INTENT_KEY_PHONE_CODE, phone);
        intent.putExtra(ConstantValue.INTENT_KEY_PSD, password);
        intent.putExtra(ConstantValue.INTENT_KEY_IS_ADMIN, isAdmin);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_NAME, name);
        intent.putExtra(ConstantValue.INTENT_KEY_BLE_SEC, sec);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_look_device);
        ButterKnife.bind(this);
        initData();
        initView();
        tryConnectDevice();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
            return;
        }
        mDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
        mPhone = getIntent().getStringExtra(ConstantValue.INTENT_KEY_PHONE_CODE);
        mPassword = getIntent().getStringExtra(ConstantValue.INTENT_KEY_PSD);
        mIsAdmin = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_IS_ADMIN, false);
        String name = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_NAME);
        mSec = getCurrentIntent().getByteArrayExtra(ConstantValue.INTENT_KEY_BLE_SEC);
        if (mSec != null && mSec.length == 2) {
            SmartLookEncrypt.sec[0] = SmartLookDeviceHelper.convertByteToShort(mSec[0]);
            SmartLookEncrypt.sec[1] = SmartLookDeviceHelper.convertByteToShort(mSec[1]);
        }
        SmartLookEncrypt.passwordPhone = Long.parseLong(mPhone);
        SmartLookEncrypt.passwordNumber = Long.parseLong(mPassword, 16);
        mIsBleDeviceVerify = SmartLookDeviceHelper.isBleDeviceVerify(mPhone, mPassword, mIsAdmin);
        new LookDevicePresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.look_device_title);
        setupIconView();
        btnLookSwitch.setTag(LOCK_SWITCH_STATE_CLOSE);
        displayLockSwitch(false);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mPresenter != null) {
            mPresenter.disconnect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setupIconView() {
        tivPassword.setTextIcon(R.drawable.ble_password_icon);
        tivPassword.setTextTitle(getString(R.string.look_device_password_label));
        tivRecording.setTextIcon(R.drawable.ble_recording_icon);
        tivRecording.setTextTitle(getString(R.string.look_device_recording_label));
        tivAuthorization.setTextIcon(R.drawable.ble_authorization_icon);
        tivAuthorization.setTextTitle(getString(R.string.look_device_authorization_label));
        tivRename.setTextIcon(R.drawable.ble_rename_icon);
        tivRename.setTextTitle(getString(R.string.look_device_rename_label));
        tivUser.setTextIcon(R.drawable.ble_user_icon);
        tivUser.setTextTitle(getString(R.string.look_device_user_label));
        tivBluetooth.setTextIcon(R.drawable.ble_bluetooth_icon);
        tivBluetooth.setTextTitle(getString(R.string.look_device_bluetooth_label));
    }

    private void toggleLock() {
        if (checkNull(btnLookSwitch, mPresenter)) {
            return;
        }
        if (!mPresenter.getSmartLookManager().isBleDeviceConnected()) {
            tryConnectDevice();
            return;
        }
        boolean isSwitchOpen = btnLookSwitch.getTag() != null && (Integer)btnLookSwitch.getTag() == LOCK_SWITCH_STATE_CLOSE;
        btnLookSwitch.setTag(isSwitchOpen ? LOCK_SWITCH_STATE_OPEN : LOCK_SWITCH_STATE_CLOSE);
        displayLockSwitch(isSwitchOpen);
        if (mPresenter.getSmartLookManager() != null) {
            mPresenter.getSmartLookManager().openAndCloseLock(isSwitchOpen);
        }
    }

    private void closeLock() {
        if (checkNull(btnLookSwitch)) {
            return;
        }
        btnLookSwitch.setTag(LOCK_SWITCH_STATE_CLOSE);
        displayLockSwitch(false);
    }

    private void getTemporaryPassword() {
        if (checkNull(btnLookSwitch, mPresenter)) {
            return;
        }
        if (!mPresenter.getSmartLookManager().isBleDeviceConnected()) {
            tryConnectDevice();
            return;
        }
        if (mPresenter.getSmartLookManager() != null) {
            showLoading();
            mPresenter.getSmartLookManager().getTemporaryPassword();
        }
    }

    private void displayLockSwitch(boolean isOpen) {
        if (checkNull(btnLookSwitch)) {
            return;
        }
        btnLookSwitch.setImageResource(isOpen ? R.drawable.ble_lock_open : R.drawable.ble_lock_close);
    }

    @OnClick({R.id.ivLeft, R.id.btnLookSwitch, R.id.tivPassword, R.id.tivRecording, R.id.tivAuthorization, R.id.tivRename, R.id.tivUser, R.id.tivBluetooth})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                if (getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_ROUTE_SOURCE, ConstantValue.ROUTE_SOURCE_NORMAL) == ConstantValue.ROUTE_SOURCE_ADD_DEVICE) {
                    HomeActivity.toHomeActivity(this);
                }
                finish();
                break;
            case R.id.btnLookSwitch:
                toggleLock();
                break;
            case R.id.tivPassword:
                getTemporaryPassword();
                break;
            case R.id.tivRecording:
                LockRecordActivity.toLookDeviceActivity(this, mDeviceId, mPhone, mPassword, mSec, mIsAdmin);
                break;
            case R.id.tivAuthorization:
                LockAuthorizationActivity.toLockAuthorizationActivity(this, mDeviceId, mPhone, mPassword, mSec, mIsAdmin);
                break;
            case R.id.tivRename:
                showRenameDeviceDialog();
                break;
            case R.id.tivUser:
                LockAccountActivity.toLockAccountActivity(this, mDeviceId, mPhone, mPassword, mSec, mIsAdmin);
                break;
            case R.id.tivBluetooth:
                AddBluetoothDeviceActivity.toAddBluetoothDeviceActivity(this);
                break;
        }
    }

    public void tryConnectDevice() {
        String deviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
        if (mPresenter == null && TextUtils.isEmpty(deviceId)) {
            return;
        }

        BleDevice bleDevice = BleDeviceScanCache.getInstance().getCacheById(deviceId);
        if (bleDevice != null) {
            mPresenter.connect(NooieApplication.mCtx, bleDevice);
        } else {
            showLoading();
            List<String> filterDeviceIds = new ArrayList<>();
            filterDeviceIds.add(deviceId);
            mPresenter.startScanDeviceByTask(mUserAccount, filterDeviceIds);
        }
    }

    @Override
    public void setPresenter(@NonNull LookDeviceContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void notifyBleDeviceState(int connectState) {
        switch (connectState) {
            case BleConnectState.CONNECTING:
                showLoading();
                break;
            case BleConnectState.CONNECTED:
                hideLoading();
                break;
            case BleConnectState.DEVICE_READY:
                if (mPresenter != null && mPresenter.getSmartLookManager() != null) {
                    mPresenter.getSmartLookManager().getBattery();
                }
                break;
        }
    }

    @Override
    public void onScanDeviceFinish(String result) {
        if (isDestroyed() || mPresenter == null) {
            return;
        }

        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            String deviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
            BleDevice bleDevice = BleDeviceScanCache.getInstance().getCacheById(deviceId);
            if (bleDevice != null) {
                mPresenter.connect(NooieApplication.mCtx, bleDevice);
            }
        }
    }

    private AlertDialog mTempPasswordDialog = null;
    private AlertDialog mRenameDeviceDialog = null;
    @Override
    public void notifyGetTemporaryPassword(String result, String id, String password) {
        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            hideTempPasswordDialog();
            mTempPasswordDialog = DialogUtils.showBleAuthorizationCodeDialog(this, getString(R.string.dialog_ble_authorization_title), formatTempPassword(password), getString(R.string.dialog_ble_authorization_code_hint),
                    getString(R.string.cancel), getString(R.string.confirm_upper), true, DialogUtils.INPUT_DIALOG_TYPE_TEXT, new DialogUtils.OnClickBleAuthorizationCodeDialogListener() {
                        @Override
                        public void onClickCancel() {
                        }

                        @Override
                        public void onClickConfirm(int inputType, String text) {
                        }
                    });
        }
    }

    private void hideTempPasswordDialog() {
        if (mTempPasswordDialog != null) {
            mTempPasswordDialog.dismiss();
        }
    }

    @Override
    public void notifyGetBattery(String result, int battery) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            if (battery != 0 && mPresenter != null) {
                mPresenter.updateLockBattery(mUserAccount, mDeviceId, battery);
            }
        }
    }

    public static final int MAX_DEVICE_NAME_BYTE_LENGTH = 11;
    private void showRenameDeviceDialog() {
        hideRenameDeviceDialog();
        mRenameDeviceDialog = DialogUtils.showBleAuthorizationCodeDialog(this, getString(R.string.dialog_ble_rename_device_title), "", getString(R.string.dialog_ble_rename_device_title),
                getString(R.string.cancel), getString(R.string.confirm_upper), true, DialogUtils.INPUT_DIALOG_TYPE_EDIT, new DialogUtils.OnClickBleAuthorizationCodeDialogListener() {
                    @Override
                    public void onClickCancel() {
                    }

                    @Override
                    public void onClickConfirm(int inputType, String text) {
                        if (TextUtils.isEmpty(text)) {
                            ToastUtil.showToast(LookDeviceActivity.this, R.string.rename_empty);
                            return;
                        }

                        if (text != null && text.getBytes() != null && text.getBytes().length > MAX_DEVICE_NAME_BYTE_LENGTH) {
                            ToastUtil.showToast(LookDeviceActivity.this, R.string.look_device_rename_to_long);
                            return;
                        }

                        if (mPresenter != null) {
                            mPresenter.getSmartLookManager().renameLock(text);
                        }
                    }
                });
    }

    private void hideRenameDeviceDialog() {
        if (mRenameDeviceDialog != null) {
            mRenameDeviceDialog.dismiss();
        }
    }

    private String formatTempPassword(String password) {
        StringBuilder resultSb = new StringBuilder();
        if (!TextUtils.isEmpty(password) && password.length() == 8) {
            resultSb.append(password.substring(0, 4));
            resultSb.append(" ");
            resultSb.append(password.substring(4));
        } else {
            return password;
        }
        return resultSb.toString();
    }
}
