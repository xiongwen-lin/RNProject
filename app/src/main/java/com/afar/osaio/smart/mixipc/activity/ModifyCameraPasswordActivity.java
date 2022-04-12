package com.afar.osaio.smart.mixipc.activity;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.afar.osaio.R;
import com.afar.osaio.smart.bluetooth.activity.BaseBluetoothActivity;
import com.afar.osaio.smart.bluetooth.listener.OnBleConnectListener;
import com.afar.osaio.smart.bluetooth.listener.OnBleStartScanListener;
import com.afar.osaio.smart.mixipc.contract.ModifyCameraPasswordContract;
import com.afar.osaio.smart.mixipc.presenter.ModifyCameraPasswordPresenter;
import com.afar.osaio.smart.mixipc.profile.bean.BleConnectState;
import com.afar.osaio.smart.mixipc.profile.bean.BleDevice;
import com.afar.osaio.smart.mixipc.profile.bean.IpcBleCmd;
import com.afar.osaio.smart.mixipc.profile.cache.BleDeviceScanCache;
import com.afar.osaio.util.CommonUtil;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.FButton;
import com.afar.osaio.widget.InputFrameView;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class ModifyCameraPasswordActivity extends BaseBluetoothActivity implements ModifyCameraPasswordContract.View {

    private static final int PASSWORD_LIMIT_DOWN = 8;
    private static final int PASSWORD_LIMIT_UP = 16;
    private static final int SEND_BLUETOOTH_CMD_TYPE_UNkNOW = 0;
    private static final int SEND_BLUETOOTH_CMD_TYPE_SET_PW = 1;
    private static final int SEND_BLUETOOTH_CMD_TYPE_OPEN_AP = 2;

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvRight)
    TextView tvRight;
    @BindView(R.id.tvModifyCameraPasswordSSID)
    TextView tvModifyCameraPasswordSSID;
    @BindView(R.id.tvModifyCameraPasswordDefaultPw)
    TextView tvModifyCameraPasswordDefaultPw;
    @BindView(R.id.ipvModifyCameraPasswordNewPw)
    InputFrameView ipvModifyCameraPasswordNewPw;
    @BindView(R.id.ipvModifyCameraPasswordConfirmPw)
    InputFrameView ipvModifyCameraPasswordConfirmPw;
    @BindView(R.id.btnModifyCameraPasswordConfirm)
    FButton btnModifyCameraPasswordConfirm;

    private ModifyCameraPasswordContract.Presenter mPresenter;
    private BluetoothDevice mConnectBluetoothDevice = null;
    private int mSendBluetoothCmdType = SEND_BLUETOOTH_CMD_TYPE_UNkNOW;
    private boolean mIsSetPwSuccess = false;
    private Dialog mShowBluetoothDisconnectDialog = null;
    private long mLastShowBleCmdInvalidTip = 0;

    public static void toModifyCameraPasswordActivity(Context from, Bundle param) {
        Intent intent = new Intent(from, ModifyCameraPasswordActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_START_PARAM, param);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_camera_password);
        ButterKnife.bind(this);

        initData();
        initView();
        initBle();
    }

    private void initData() {
        new ModifyCameraPasswordPresenter(this);
        mConnectBluetoothDevice = getBluetoothDeviceFromCache(getBleAddress());
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.modify_camera_password_title);
        tvRight.setText(R.string.modify_camera_skip);
        tvRight.setVisibility(View.VISIBLE);
        tvRight.setTextColor(ContextCompat.getColor(this, R.color.theme_green));
        tvModifyCameraPasswordSSID.setText(String.format(getString(R.string.modify_camera_password_ssid), getDeviceSsid()));
        tvModifyCameraPasswordDefaultPw.setText(String.format(getString(R.string.modify_camera_password_defaut_pw), "12345678"));
        setInputView();
        showBluetoothConnectSuccessToast();
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
        registerInputListener();
        checkIsNeedToRequestLayout();
    }

    private void resumeData() {
        setIsGotoOtherPage(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        unRegisterInputListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.destroy();
        }
        releaseRes();
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        ivLeft = null;
        tvTitle = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        tryDisconnectBluetooth();
    }

    @OnClick({R.id.ivLeft, R.id.tvRight, R.id.btnModifyCameraPasswordConfirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                tryDisconnectBluetooth();
                finish();
                break;
            case R.id.tvRight:
                tryToSendBleCmd(SEND_BLUETOOTH_CMD_TYPE_OPEN_AP);
                break;
            case R.id.btnModifyCameraPasswordConfirm:
                confirmModifyPw();
                break;
        }
    }

    @Override
    public void setPresenter(@NonNull ModifyCameraPasswordContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void bluetoothStateOffChange() {
        //蓝牙关闭
        NooieLog.d("-->> debug BaseBluetoothActivity bluetoothStateOffChange: ");
        showBluetoothOperationTip(BLUETOOTH_OPERATION_TIP_TYPE_DISCONNECT);
    }

    @Override
    public void onBatchScanResultsByFilter(@NotNull List<ScanResult> results) {
        //super.onBatchScanResultsByFilter(results);
        if (isDestroyed() || checkIsGotoOtherPage()) {
            return;
        }
        dealBleScanResult(filterDeviceBluetooth(results, getBleAddress()));
    }

    @Override
    public void onDeviceReady(@NotNull BluetoothDevice device) {
        //super.onDeviceReady(device);
        //设备Ready可以通信了
        NooieLog.d("-->> debug ModifyCameraPasswordActivity onDeviceReady: ");
        if (isDestroyed() || checkIsGotoOtherPage()) {
            return;
        }
        if (!checkMatchBluetooth(mConnectBluetoothDevice, device)) {
            return;
        }
        dealBleDeviceList(BleConnectState.DEVICE_READY, device);
    }

    @Override
    public void onDeviceDisconnecting(@NotNull BluetoothDevice device) {
        //super.onDeviceDisconnecting(device);
        //设备正在断开连接
        NooieLog.d("-->> debug ModifyCameraPasswordActivity onDeviceDisconnecting: ");
        if (isDestroyed() || checkIsGotoOtherPage()) {
            return;
        }
        if (!checkMatchBluetooth(mConnectBluetoothDevice, device)) {
            return;
        }
        dealBleDeviceList(BleConnectState.DISCONNECTED, device);
        showBluetoothOperationTip(BLUETOOTH_OPERATION_TIP_TYPE_DISCONNECT);
    }

    @Override
    public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
        super.onDataReceived(device, data);
        if (isDestroyed() || checkIsGotoOtherPage()) {
            return;
        }
        if (!checkMatchBluetooth(mConnectBluetoothDevice, device)) {
            return;
        }
        hideLoading();
        String cmdResponse = data != null ? data.getStringValue(0) : null;
        boolean isCmdResponseInValid = cmdResponse == null || !cmdResponse.startsWith(IpcBleCmd.BLE_CMD_FEATURE_RSP);
        NooieLog.d("-->> debug ModifyCameraPasswordActivity onDataReceived cmdResponse=" + cmdResponse + " isCmdResponseInValid=" + isCmdResponseInValid + " data=" + (data != null ? data.toString() : ""));
        if (isCmdResponseInValid) {
            showBleCmdInvalidTip(BLUETOOTH_OPERATION_TIP_TYPE_SETTING_FAIL);
            return;
        }
        if (cmdResponse.contains(IpcBleCmd.BLE_CMD_SET_PW_RSP)) {
            mIsSetPwSuccess = cmdResponse.contains(IpcBleCmd.BLE_CMD_RSP_SUCCESS);
            if (mIsSetPwSuccess) {
                tryToSendBleCmd(SEND_BLUETOOTH_CMD_TYPE_OPEN_AP);
            } else {
                showBluetoothOperationTip(BLUETOOTH_OPERATION_TIP_TYPE_SETTING_FAIL);
            }
        } else if (cmdResponse.contains(IpcBleCmd.BLE_CMD_OPEN_HOT_SPOT_RSP)) {
            boolean isOpenHotSpot = cmdResponse.contains(IpcBleCmd.BLE_CMD_RSP_SUCCESS);
            gotoConnectApDevice(isOpenHotSpot, !mIsSetPwSuccess, device.getAddress());
        }
    }

    private void setInputView() {
        ipvModifyCameraPasswordNewPw.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT).setInputTitle(getString(R.string.account_new_password))
                .setEtInputToggle(true)
                .setInputBtn(R.drawable.eye_open_icon_state_list)
                .setEtPwInputType(InputType.TYPE_CLASS_TEXT)
                .setEtInputType(InputFrameView.getPwInputType(InputType.TYPE_CLASS_TEXT, false))
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                    }

                    @Override
                    public void onEditorAction() {
                        hideInputMethod();
                    }

                    @Override
                    public void onEtInputClick() {
                    }
                })
                .setInputTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        checkBtnEnable();
                    }
                });

        ipvModifyCameraPasswordConfirmPw.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT).setInputTitle(getString(R.string.account_confirm_password))
                .setEtInputToggle(true)
                .setInputBtn(R.drawable.eye_open_icon_state_list)
                .setEtPwInputType(InputType.TYPE_CLASS_TEXT)
                .setEtInputType(InputFrameView.getPwInputType(InputType.TYPE_CLASS_TEXT, false))
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                    }

                    @Override
                    public void onEditorAction() {
                        hideInputMethod();
                        onViewClicked(btnModifyCameraPasswordConfirm);
                    }

                    @Override
                    public void onEtInputClick() {
                    }
                })
                .setInputTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        checkBtnEnable();
                    }
                });

        checkBtnEnable();
    }

    private void checkBtnEnable() {
        if (!TextUtils.isEmpty(ipvModifyCameraPasswordNewPw.getInputText()) && !TextUtils.isEmpty(ipvModifyCameraPasswordConfirmPw.getInputText())) {
            btnModifyCameraPasswordConfirm.setEnabled(true);
            btnModifyCameraPasswordConfirm.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
        } else {
            btnModifyCameraPasswordConfirm.setEnabled(false);
            btnModifyCameraPasswordConfirm.setTextColor(getResources().getColor(R.color.unable_clickable_color));
        }
    }

    private void confirmModifyPw() {
        if (isDestroyed() || checkNull(ipvModifyCameraPasswordNewPw, ipvModifyCameraPasswordConfirmPw)) {
            return;
        }
        String newPw = ipvModifyCameraPasswordNewPw.getInputText();
        String confirmPw = ipvModifyCameraPasswordConfirmPw.getInputText();
        if (!checkPwValid(newPw) || !checkPwValid(confirmPw)) {
            ToastUtil.showToast(this, R.string.modify_camera_password_invalid);
        } else if (!newPw.equals(confirmPw)) {
            ToastUtil.showToast(this, R.string.modify_camera_password_different);
        } else {
            tryToSendBleCmd(SEND_BLUETOOTH_CMD_TYPE_SET_PW);
        }
    }

    private void gotoConnectApDevice(boolean isOpenHotSpot, boolean isDefault, String address) {
        if (isOpenHotSpot) {
            setIsGotoOtherPage(true);
            Bundle param = new Bundle();
            param.putString(ConstantValue.INTENT_KEY_SSID, getDeviceSsid());
            param.putString(ConstantValue.INTENT_KEY_BLE_DEVICE, address);
            param.putString(ConstantValue.INTENT_KEY_PSD, "12345678");
            param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, getDeviceModel());
            param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, getConnectionMode());
            param.putBoolean(ConstantValue.INTENT_KEY_DATA_PARAM, isDefault);
            ConnectApDeviceActivity.toConnectApDeviceActivity(this, param);
        } else {
            showBluetoothOperationTip(BLUETOOTH_OPERATION_TIP_TYPE_SETTING_FAIL);
        }
    }

    private boolean checkPwValid(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= PASSWORD_LIMIT_DOWN && password.length() <= PASSWORD_LIMIT_UP && !CommonUtil.checkPasswordIllegalChar(password);
    }

    private BluetoothDevice getBluetoothDeviceFromCache(String address) {
        BleDevice bleDevice = BleDeviceScanCache.getInstance().getCacheById(address);
        return bleDevice != null && bleDevice.getDevice() != null ? bleDevice.getDevice() : null;
    }

    private void startScanBluetooth() {
        if (isDestroyed()) {
            return;
        }
        showLoading();
        startScanBle(DEFAULT_BLE_SCAN_TIME, new OnBleStartScanListener() {
            @Override
            public void onTimeFinish() {
                if (isDestroyed()) {
                    return;
                }
                hideLoading();
            }
        });
    }

    private void stopScanBluetooth() {
        stopScanBle();
    }

    private void tryToSendBleCmd(int sendBluetoothCmdType) {
        mSendBluetoothCmdType = sendBluetoothCmdType;
        showLoading();
        if (mConnectBluetoothDevice == null) {
            startScanBluetooth();
            return;
        }
        connectDevice(mConnectBluetoothDevice, new OnBleConnectListener() {
            @Override
            public void onResult(int state, BluetoothDevice bluetoothDevice) {
                dealBleDeviceList(state, bluetoothDevice);
            }
        });
    }

    private void wakeDeviceByBle() {
        showLoading();
        sendCmd(IpcBleCmd.BLE_CMD_LOGIN);
    }

    private void openHotSpotByBle() {
        showLoading();
        sendCmd(IpcBleCmd.BLE_CMD_OPEN_HOT_SPOT);
    }

    private void closeHotSpotByBle() {
        showLoading();
        sendCmd(IpcBleCmd.BLE_CMD_CLOSE_HOT_SPOT);
    }

    private void setHotSpotPw(String newPw) {
        StringBuilder pwCmd = new StringBuilder();
        pwCmd.append(IpcBleCmd.BLE_CMD_SET_PW).append(newPw).append("\r");
        sendCmd(pwCmd.toString());
    }

    private void dealBleScanResult(List<ScanResult> results) {
        if (CollectionUtil.isEmpty(results)) {
            return;
        }
        BleDeviceScanCache.getInstance().updateBleDevice(results);
        BleDevice bleDevice = BleDeviceScanCache.getInstance().getCacheById(getBleAddress());
        if (bleDevice != null && bleDevice.getDevice() != null) {
            mConnectBluetoothDevice = bleDevice.getDevice();
            stopScanBluetooth();
            hideLoading();
        }
    }

    private void dealBleDeviceList(int connectState, BluetoothDevice device) {
        if (isDestroyed()) {
            return;
        }
        if (!checkMatchBluetooth(mConnectBluetoothDevice, device)) {
            return;
        }
        hideLoading();
        if (connectState == BleConnectState.DEVICE_READY) {
            if (mSendBluetoothCmdType == SEND_BLUETOOTH_CMD_TYPE_SET_PW) {
                resetDevicePassword();
            } else if (mSendBluetoothCmdType == SEND_BLUETOOTH_CMD_TYPE_OPEN_AP) {
                openHotSpotByBle();
            }
        } else {
        }
    }

    private void resetDevicePassword() {
        String newPw = ipvModifyCameraPasswordNewPw != null ? ipvModifyCameraPasswordNewPw.getInputTextNoTrim() : null;
        if (TextUtils.isEmpty(newPw)) {
            return;
        }
        showLoading();
        setHotSpotPw(newPw);
    }

    private void showBluetoothOperationTip(int type) {
        if (type == BLUETOOTH_OPERATION_TIP_TYPE_SUCCESS) {
            ToastUtil.showToast(this, R.string.bluetooth_scan_operation_tip_success);
        } else if (type == BLUETOOTH_OPERATION_TIP_TYPE_DISCONNECT) {
            showBluetoothDisconnectDialog();
        } else {
            ToastUtil.showToast(this, R.string.bluetooth_scan_operation_tip_setting_fail);
        }
    }

    private void showBluetoothDisconnectDialog() {
        hideBluetoothDisconnectDialog();
        mShowBluetoothDisconnectDialog = DialogUtils.showInformationDialog(this, getString(R.string.bluetooth_scan_operation_tip_disconnect_title), getString(R.string.bluetooth_scan_operation_tip_disconnect_content), getString(R.string.bluetooth_scan_operation_tip_disconnect_confirm), false, false, new DialogUtils.OnClickInformationDialogLisenter() {
            @Override
            public void onConfirmClick() {
                finish();
            }
        });
    }

    private void hideBluetoothDisconnectDialog() {
        if (mShowBluetoothDisconnectDialog != null) {
            mShowBluetoothDisconnectDialog.dismiss();
            mShowBluetoothDisconnectDialog = null;
        }
    }

    private void showBluetoothConnectSuccessToast() {
        if (!getBluetoothConnectSuccess()) {
            return;
        }
        showBluetoothOperationTip(BLUETOOTH_OPERATION_TIP_TYPE_SUCCESS);
    }

    private void showBleCmdInvalidTip(int type) {
        boolean isShowBleCmdInvalidTip = System.currentTimeMillis() - mLastShowBleCmdInvalidTip > 3 * 1000;
        if (isShowBleCmdInvalidTip) {
            mLastShowBleCmdInvalidTip = System.currentTimeMillis();
            showBluetoothOperationTip(type);
        }
    }

    private String getDeviceSsid() {
        if (getStartParam() == null) {
            return null;
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_SSID);
    }

    private String getBleAddress() {
        if (getStartParam() == null) {
            return null;
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_BLE_DEVICE);
    }

    private boolean getDeviceDefaultPw() {
        if (getStartParam() == null) {
            return false;
        }
        return getStartParam().getBoolean(ConstantValue.INTENT_KEY_DATA_PARAM, false);
    }

    private String getDeviceModel() {
        if (getStartParam() == null) {
            return null;
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_IPC_MODEL);
    }

    private int getConnectionMode() {
        if (getStartParam() == null) {
            return ConstantValue.CONNECTION_MODE_QC;
        }
        return getStartParam().getInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC);
    }

    private boolean getBluetoothConnectSuccess() {
        if (getStartParam() == null) {
            return false;
        }
        return getStartParam().getBoolean(ConstantValue.INTENT_KEY_DATA_PARAM_4, false);
    }
}
