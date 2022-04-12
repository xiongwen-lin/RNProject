package com.afar.osaio.smart.scan.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.bluetooth.activity.BaseBluetoothActivity;
import com.afar.osaio.smart.bluetooth.listener.OnBleConnectListener;
import com.afar.osaio.smart.bluetooth.listener.OnBleStartScanListener;
import com.afar.osaio.smart.mixipc.contract.BluetoothScanContract;
import com.afar.osaio.smart.mixipc.profile.bean.BleConnectState;
import com.afar.osaio.smart.mixipc.profile.bean.BleDevice;
import com.afar.osaio.smart.mixipc.profile.bean.IpcBleCmd;
import com.afar.osaio.smart.mixipc.profile.cache.BleDeviceScanCache;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.afar.osaio.widget.NEventFButton;
import com.nooie.common.base.GlobalData;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.configure.FontUtil;
import com.nooie.common.utils.data.StringHelper;
import com.nooie.data.EventDictionary;
import com.nooie.sdk.bean.IpcType;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.scan.contract.InputWiFiPsdContract;
import com.afar.osaio.smart.scan.presenter.InputWiFiPsdPresenter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.afar.osaio.widget.InputFrameView;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.network.NetworkUtil;
import com.nooie.sdk.bean.SDKConstant;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by victor on 2018/6/29
 * Email is victor.qiao.0604@gmail.com
 */
public class InputWiFiPsdActivity extends BaseBluetoothActivity implements InputWiFiPsdContract.View {

    private static final int USE_TYPE_REFRESH_SSID = 1;
    private static final int USE_TYPE_CHECK_PERM_FOR_SSID = 2;
    private static final int SSID_CHECK_TYPE_NORMAl = 0;
    private static final int SSID_CHECK_TYPE_FORMAT = 1;
    private static final int SSID_CHECK_TYPE_INVALID = 2;

    private static final int SEND_BLUETOOTH_CMD_TYPE_UNkNOW = 0;
    private static final int SEND_BLUETOOTH_CMD_TYPE_SEND_DISTRIBUTE_NETWORK = 1;

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.btnDone)
    NEventFButton btnDone;
    @BindView(R.id.clInputWifiPsdContainer)
    ConstraintLayout clInputWifiPsdContainer;
    @BindView(R.id.ipvSsid)
    InputFrameView ipvSsid;
    @BindView(R.id.ipvPsd)
    InputFrameView ipvPsd;
    @BindView(R.id.tvTip)
    TextView tvTip;

    private InputWiFiPsdContract.Presenter mPresenter;
    private AlertDialog mSsidWarningDialog = null;
    private boolean mSkipQRCodeConfigNetwork = false;
    private IpcType mDeviceType;
    private boolean mIsNormalDenied = false;
    private BluetoothDevice mConnectBluetoothDevice = null;
    private int mSendBluetoothCmdType = SEND_BLUETOOTH_CMD_TYPE_UNkNOW;
    private Dialog mShowBluetoothDisconnectDialog = null;
    private Dialog mShowDeviceModeErrorDialog = null;
    private long mLastShowBleCmdInvalidTip = 0;

    public static void toInputWiFiPsdActivity(Context from, Bundle param) {
        Intent intent = new Intent(from, InputWiFiPsdActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_START_PARAM, param);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_wifi_psd);
        ButterKnife.bind(this);

        initData();
        initView();
        initBle();
    }

    @Override
    public void onResume() {
        super.onResume();
        NooieLog.d("-->> InputWiFiPsdActivity onResume");
        setIsGotoOtherPage(false);
        if (mPresenter != null) {
            mPresenter.getSSID(USE_TYPE_REFRESH_SSID);
        }
        if (NooieDeviceHelper.isSupport5GConnected(mDeviceType)) {
            tvTip.setVisibility(View.INVISIBLE);
        }
    }

    private void resumeData(String autoSSID) {
        NooieLog.d("-->> InputWiFiPsdActivity resumeData 1");
        if (!NetworkUtil.isWifiEnabled(NooieApplication.mCtx)) {
            NooieLog.d("-->> InputWiFiPsdActivity resumeData 2");
            ToastUtil.showToast(this, R.string.add_camera_input_check_wifi_tip);
        }
        String ssid = ipvSsid.getInputTextNoTrim();
        if (!TextUtils.isEmpty(autoSSID)) {
            ssid = autoSSID;
            ipvSsid.setEtInputText(ssid);
            ipvSsid.setEtSelection(ssid.length());
            ipvSsid.setIpvEnable(false);
        }
        ipvSsid.setIpvEnable(getConnectionMode() == ConstantValue.CONNECTION_MODE_AP || TextUtils.isEmpty(autoSSID));

        if (TextUtils.isEmpty(ssid)) {
            checkLocalPerm(getString(R.string.add_camera_input_wifi_psd_location_enable), true);
        }

        if (!TextUtils.isEmpty(ssid)) {
            GlobalPrefs prefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
            prefs.loadWifiInfo();
            String psd = prefs.getWifiPsd(ssid);
            if (!TextUtils.isEmpty(psd)) {
                ipvPsd.setEtInputText(psd);
                ipvPsd.setEtSelection(psd.length());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.destroy();
        }
        hidePermDeniedDialog();
        hidePsdEmptyDialog();
        hideCheckSSIDDialog();
        releaseRes();
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        ivLeft = null;
        tvTitle = null;
        ivRight = null;
        btnDone = null;
        clInputWifiPsdContainer = null;
        if (ipvSsid != null) {
            ipvSsid.release();
            ipvSsid = null;
        }
        if (ipvPsd != null) {
            ipvPsd.release();
            ipvPsd = null;
        }
        tvTip = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        tryDisconnectBluetooth();
    }

    @Override
    public void setPresenter(InputWiFiPsdContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
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
        if (isDestroyed() || checkIsGotoOtherPage()) {
            return;
        }
        NooieLog.d("-->> debug InputWiFiPsdActivity onDeviceReady: ");
        if (!checkMatchBluetooth(mConnectBluetoothDevice, device)) {
            return;
        }
        dealBleDeviceList(BleConnectState.DEVICE_READY, device);
    }

    @Override
    public void onDeviceDisconnecting(@NotNull BluetoothDevice device) {
        //super.onDeviceDisconnecting(device);
        //设备正在断开连接
        if (isDestroyed() || checkIsGotoOtherPage()) {
            return;
        }
        NooieLog.d("-->> debug InputWiFiPsdActivity onDeviceDisconnecting: ");
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
        NooieLog.d("-->> debug InputWiFiPsdActivity onDataReceived cmdResponse=" + cmdResponse + " isCmdResponseInValid=" + isCmdResponseInValid + " data=" + (data != null ? data.toString() : ""));
        if (isCmdResponseInValid) {
            showBleCmdInvalidTip(BLUETOOTH_OPERATION_TIP_TYPE_SETTING_FAIL);
            return;
        }
        if (IpcBleCmd.checkCmdRspIsUnbind(cmdResponse)) {
            showBleCmdInvalidTip(BLUETOOTH_OPERATION_TIP_TYPE_UNBIND);
            return;
        }
        if (IpcBleCmd.checkCmdRspIsBoundBySelf(cmdResponse)) {
            showBleCmdInvalidTip(BLUETOOTH_OPERATION_TIP_TYPE_BOUND);
            return;
        }
        if (IpcBleCmd.checkCmdRspIsBoundByOther(cmdResponse)) {
            showBleCmdInvalidTip(BLUETOOTH_OPERATION_TIP_TYPE_BOUND_BY_OTHER);
            return;
        }
        if (cmdResponse.contains(IpcBleCmd.BLE_CMD_DISTRIBUTE_NETWORK_SEND_RSP)) {
            boolean isDistributeNetworkSuccess = cmdResponse.contains(IpcBleCmd.BLE_CMD_RSP_SUCCESS);
            dealAfterDistributeNetworkByBluetooth(isDistributeNetworkSuccess);
        }
    }

    private boolean initData() {
        if (getCurrentIntent() != null) {
            mSkipQRCodeConfigNetwork = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_SKIP_QR_CODE_CONFIG_NETWORK, false);
            mDeviceType = IpcType.getIpcType(getDeviceModel());
            new InputWiFiPsdPresenter(this);
            mConnectBluetoothDevice = getBluetoothDeviceFromCache(getBleAddress());
            return true;
        }
        return false;
    }

    private void initView() {
        int screen_h = DisplayUtil.SCREEN_HIGHT_PX - DisplayUtil.dpToPx(NooieApplication.mCtx,
                DisplayUtil.HEADER_BAR_HEIGHT_DP) - DisplayUtil.getStatusBarHeight(NooieApplication.mCtx);
        screen_h = Math.max(DisplayUtil.SCREEN_CONTENT_MIN_HEIGHT_PX, screen_h);
        clInputWifiPsdContainer.setMinHeight(screen_h);

        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.add_camera_connect_to_wifi);
        ivRight.setVisibility(View.INVISIBLE);
        setupInputFrameView();
        setupMoreTv();
        btnDone.setExternal(NooieDeviceHelper.createDistributionNetworkExternal(false));
        showBluetoothConnectSuccessToast();
    }

    private void setupInputFrameView() {
        ipvSsid.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputTitle(getString(R.string.add_camera_input_current_network))
                .setInputBtnType(InputFrameView.INPUT_BTN_TYPE_TEXT)
                .setTextInputBtn(getString(R.string.change_upper))
                .setInputBtnIsShow(true)
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                        setIsGotoOtherPage(true);
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
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

        ipvPsd.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputTitle(getString(R.string.password))
                .setEtInputToggle(true)
                .setInputBtn(R.drawable.eye_open_icon_state_list)
                .setEtInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                    }

                    @Override
                    public void onEditorAction() {
                        hideInputMethod();
                        onViewClicked(btnDone);
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

    public void checkBtnEnable() {
        String psdStr = ipvPsd.getInputTextNoTrim();
        int psdLen = psdStr != null ? psdStr.length() : 0;
        if (!TextUtils.isEmpty(ipvSsid.getInputText()) && (psdLen == 0 || psdLen > ConstantValue.DEVICE_WIFI_MIN_LEN)) {
            btnDone.setEnabled(true);
            btnDone.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
        } else {
            btnDone.setEnabled(false);
            btnDone.setTextColor(getResources().getColor(R.color.unable_clickable_color));
        }
    }

    private void setupMoreTv() {
        String moreTxt = getString(R.string.add_camera_input_wifi_pas_tip_more);
        String text = String.format(getString(R.string.add_camera_input_wifi_pas_tip), moreTxt);
        SpannableStringBuilder style = new SpannableStringBuilder();

        text.length();

        //设置文字
        style.append(text);

        //设置部分文字点击事件
        ClickableSpan conditionClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                setIsGotoOtherPage(true);
                NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_CLICK_ROUTER_SETTING);
                RouterSettingActivity.toRouterSettingActivity(InputWiFiPsdActivity.this);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setTypeface(FontUtil.loadTypeface(getApplicationContext(), "fonts/manrope-semibold.otf"));
                ds.setUnderlineText(false);
            }
        };
        style.setSpan(conditionClickableSpan, text.indexOf(moreTxt), text.indexOf(moreTxt) + moreTxt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvTip.setText(style);

        //设置部分文字颜色
        ForegroundColorSpan conditionForegroundColorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.theme_green_subtext_color));
        style.setSpan(conditionForegroundColorSpan, text.indexOf(moreTxt), text.indexOf(moreTxt) + moreTxt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //配置给TextView
        tvTip.setMovementMethod(LinkMovementMethod.getInstance());
        tvTip.setText(style);
    }

    @OnClick({R.id.ivLeft, R.id.btnDone})
    public void onViewClicked(View v) {
        switch (v.getId()) {
            case R.id.ivLeft:
                tryDisconnectBluetooth();
                finish();
                break;
            case R.id.btnDone:
                hideInputMethod();
                String psdStr = ipvPsd.getInputTextNoTrim();
                if (TextUtils.isEmpty(psdStr)) {
                    showPsdEmptyDialog();
                } else {
                    //gotoWiFiQRCodeOrScan();
                    checkSSIDBeforeJump();
                }
                break;
        }
    }

    private int checkSSIDEffective(String ssid) {
        if (NooieDeviceHelper.isNetworkOf5G(ssid) || checkCurrentWifiIs5G(ssid)) {
            return SSID_CHECK_TYPE_FORMAT;
        } else if (NooieDeviceHelper.checkApFutureCode(ssid)) {
            return SSID_CHECK_TYPE_INVALID;
        } else {
            return SSID_CHECK_TYPE_NORMAl;
        }
    }

    private boolean checkCurrentWifiIs5G(String ssid) {
        if (!NooieApplication.TEST_MODE) {
            return false;
        }
        String currentSSId = NetworkUtil.getSSIDAuto(NooieApplication.mCtx);
        if (TextUtils.isEmpty(ssid) || !ssid.equals(currentSSId)) {
            return false;
        }
        return NetworkUtil.is5GHz(NetworkUtil.getFrequency(NooieApplication.mCtx));
    }

    @Override
    public void onGetSSID(String result, int useType, String ssid) {
        if (isDestroyed()) {
            return;
        }
        switch (useType) {
            case USE_TYPE_REFRESH_SSID: {
                resumeData(ssid);
                break;
            }
            case USE_TYPE_CHECK_PERM_FOR_SSID: {
                showSsidWarningDialog(ssid);
                break;
            }
        }
    }

    private void gotoWiFiQRCodeOrScan() {
        /*
        if (checkSSIDEffective(ipvSsid.getInputTextNoTrim()) == SSID_CHECK_TYPE_FORMAT) {
            showCheckSSIDDialog(SSID_CHECK_TYPE_FORMAT);
            return;
        } else if (checkSSIDEffective(ipvSsid.getInputTextNoTrim()) == SSID_CHECK_TYPE_INVALID) {
            showCheckSSIDDialog(SSID_CHECK_TYPE_INVALID);
            return;
        }
        */
        /*
        String ssid = ipvSsid.getInputTextNoTrim();
        String psdStr = ipvPsd.getInputTextNoTrim();
        int psdLen = psdStr != null ? psdStr.length() : 0;
        StringBuilder illegalStr = getIllegalChars(psdStr);
        if (psdLen == 0 || (psdLen < ConstantValue.DEVICE_WIFI_MAX_LEN && illegalStr.length() == 0)) {
            GlobalPrefs prefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
            prefs.saveWifiInfo(ssid, psdStr);
            if (mSkipQRCodeConfigNetwork) {
                NooieScanActivity.toNooieScanActivity(this, ssid, psdStr, mDeviceType.getType(), getConnectionMode(), null);
            } else if (getConnectionMode() == ConstantValue.CONNECTION_MODE_AP) {
                ConnectToWiFiActivity.toConnectToWiFiActivity(this, ssid, psdStr, mDeviceType.getType(), getConnectionMode());
            } else {
                WiFiQRCodeActivity.toWiFiQRCodeActivity(this, ssid, psdStr, mDeviceType.getType(), getConnectionMode());
            }
        } else {
            ToastUtil.showToast(this, R.string.add_camera_psd_error);
        }

         */
        String ssid = ipvSsid.getInputTextNoTrim();
        String psdStr = ipvPsd.getInputTextNoTrim();
        int psdLen = psdStr != null ? psdStr.length() : 0;
        StringBuilder illegalStr = getIllegalChars(psdStr);
        if (psdLen == 0 || (psdLen < ConstantValue.DEVICE_WIFI_MAX_LEN && illegalStr.length() == 0)) {
            GlobalPrefs prefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
            prefs.saveWifiInfo(ssid, psdStr);
            if (NooieDeviceHelper.isSupportBleDistributeNetwork(getDeviceModel())) {
                tryToSendBleCmd(SEND_BLUETOOTH_CMD_TYPE_SEND_DISTRIBUTE_NETWORK);
            } else if (mSkipQRCodeConfigNetwork) {
                NooieScanActivity.toNooieScanActivity(this, ssid, psdStr, mDeviceType.getType(), getConnectionMode(), null);
            } else if (getConnectionMode() == ConstantValue.CONNECTION_MODE_AP) {
                ConnectToWiFiActivity.toConnectToWiFiActivity(this, ssid, psdStr, mDeviceType.getType(), getConnectionMode());
            } else {
                WiFiQRCodeActivity.toWiFiQRCodeActivity(this, ssid, psdStr, mDeviceType.getType(), getConnectionMode());
            }
        } else {
            ToastUtil.showToast(this, R.string.add_camera_psd_error);
        }
    }

    private void checkSSIDBeforeJump() {
        if (checkSSIDEffective(ipvSsid.getInputTextNoTrim()) == SSID_CHECK_TYPE_FORMAT && !NooieDeviceHelper.isSupport5GConnected(mDeviceType)) {
            showCheckSSIDDialog(SSID_CHECK_TYPE_FORMAT);
            return;
        } else if (checkSSIDEffective(ipvSsid.getInputTextNoTrim()) == SSID_CHECK_TYPE_INVALID) {
            showCheckSSIDDialog(SSID_CHECK_TYPE_INVALID);
            return;
        }
        gotoWiFiQRCodeOrScan();
    }

    private StringBuilder getIllegalChars(String password) {
        StringBuilder illegalStr = new StringBuilder();
        for (int i = 0; i < password.length(); i++) {
            if (password.charAt(i) >= 0x20 && password.charAt(i) <= 0x7E) {
            } else {
                illegalStr.append(password.charAt(i));
            }
        }
        return illegalStr;
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        NooieLog.d("-->> InputWiFiPsdActivity onPermissionsDenied somePermissionPermanentlyDenied=" + EasyPermissions.somePermissionPermanentlyDenied(this, perms) + " mIsNormalDenied=" + mIsNormalDenied);
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms) && !mIsNormalDenied) {
            mIsNormalDenied = false;
            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.fromParts("package", getPackageName(), null));
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void showCheckLocationPermDialog() {
        //showSsidWarningDialog(NetworkUtil.getSSIDAuto(NooieApplication.mCtx));
        if (mPresenter != null) {
            mPresenter.getSSID(USE_TYPE_CHECK_PERM_FOR_SSID);
        }
    }

    private boolean showSsidWarningDialog(String ssid) {
        if (!TextUtils.isEmpty(ssid)) {
            return false;
        }
        List<String> permList = new ArrayList<>();
        for (int i = 0; i < ConstantValue.PERM_GROUP_LOCATION.length; i++) {
            permList.add(ConstantValue.PERM_GROUP_LOCATION[i]);
        }
        NooieLog.d("-->> InputWiFiPsdActivity showSsidWarningDialog perm isHas=" + EasyPermissions.hasPermissions(NooieApplication.mCtx, ConstantValue.PERM_GROUP_LOCATION) + " isDenied=" + EasyPermissions.somePermissionDenied(this, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION) + " isPemDenied=" + EasyPermissions.somePermissionPermanentlyDenied(this, permList));
        if (TextUtils.isEmpty(ssid) && !EasyPermissions.hasPermissions(NooieApplication.mCtx, ConstantValue.PERM_GROUP_LOCATION)) {
            showPermDeniedDialog();
        } else if (TextUtils.isEmpty(ssid)) {
//            if (mSsidWarningDialog != null) {
//                mSsidWarningDialog.hide();
//            }
            hidePermDeniedDialog();
            mSsidWarningDialog = DialogUtils.showInformationDialog(this, getString(R.string.dialog_tip_title), getString(R.string.add_camera_input_wifi_psd_wifi_no_ssid), getString(R.string.add_camera_input_wifi_psd_wifi_enter_name), true, false, new DialogUtils.OnClickInformationDialogLisenter() {
                @Override
                public void onConfirmClick() {
                    ipvSsid.setIpvFocusable(true);
                }
            });
        }
        return true;
    }

    private void showPermDeniedDialog() {
//        if (mSsidWarningDialog != null) {
//            mSsidWarningDialog.dismiss();
//        }
        hidePermDeniedDialog();
        mSsidWarningDialog = DialogUtils.showConfirmWithImageDialog(this, "", getString(R.string.add_camera_input_wifi_psd_wifi_no_perm_for_ssid), 0, getString(R.string.add_camera_input_wifi_psd_wifi_enter_name), getString(R.string.settings), false, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                mIsNormalDenied = !EasyPermissions.hasPermissions(NooieApplication.mCtx, ConstantValue.PERM_GROUP_LOCATION) && EasyPermissions.somePermissionDenied(InputWiFiPsdActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
                requestPermission(ConstantValue.PERM_GROUP_LOCATION);
            }

            @Override
            public void onClickLeft() {
                ipvSsid.setIpvFocusable(true);
            }
        });
    }

    private void hidePermDeniedDialog() {
        if (mSsidWarningDialog != null) {
            mSsidWarningDialog.dismiss();
            mSsidWarningDialog = null;
        }
    }

    private AlertDialog mPsdEmptyDialog;

    private void showPsdEmptyDialog() {
        hidePsdEmptyDialog();
        /*mPsdEmptyDialog = DialogUtils.showInformationDialog(this, getString(R.string.add_camera_input_empty_psd_title), getString(R.string.add_camera_input_empty_psd_content), getString(R.string.next), true, false, new DialogUtils.OnClickInformationDialogLisenter() {
            @Override
            public void onConfirmClick() {
                //gotoWiFiQRCodeOrScan();
                checkSSIDBeforeJump();
            }
        });*/
        mPsdEmptyDialog = DialogUtils.showConfirmWithSubMsgDialog(this, getString(R.string.add_camera_input_empty_psd_title), getString(R.string.add_camera_input_empty_psd_content), getString(R.string.cancel), getString(R.string.ok), new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickLeft() {

            }

            @Override
            public void onClickRight() {
                //gotoWiFiQRCodeOrScan();
                checkSSIDBeforeJump();
            }
        });
    }

    private void hidePsdEmptyDialog() {
        if (mPsdEmptyDialog != null) {
            mPsdEmptyDialog.dismiss();
            mPsdEmptyDialog = null;
        }
    }

    private AlertDialog mCheckSSIDDialog;

    private void showCheckSSIDDialog(int checkType) {
        String content = checkType == SSID_CHECK_TYPE_FORMAT ? getString(R.string.add_camera_input_wifi_format_tip) : getString(R.string.add_camera_input_wifi_invalid_tip);
        hideCheckSSIDDialog();
        mCheckSSIDDialog = DialogUtils.showConfirmWithSubMsgDialog(this, getString(R.string.add_camera_input_wifi_tip_title), content, R.string.next, R.string.add_camera_input_wifi_go_to_change, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }

            @Override
            public void onClickLeft() {
                gotoWiFiQRCodeOrScan();
            }
        });
    }

    private void hideCheckSSIDDialog() {
        if (mCheckSSIDDialog != null) {
            mCheckSSIDDialog.dismiss();
            mCheckSSIDDialog = null;
        }
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

    private void sendDistributeNetworkCmd(String info) {
        //info = "L:8;15;73;WIFI:U:58e9e444731cbf8b;Z:8.00;R:CN;T:WPA;P:\"nooie666\";S:TP-LINK_HyFi_49;";
        if (TextUtils.isEmpty(info)) {
            return;
        }
        //NooieLog.d("-->> debug InputWiFiPsdActivity sendDistributeNetworkCmd info=" + info + " info bytes" + info.getBytes());
        List<String> splitCmdList = IpcBleCmd.convertBleLongCmd(info);
        if (CollectionUtil.isNotEmpty(splitCmdList)) {
            try {
                List<String> sendCmdList = new ArrayList<>();
                int splitCmdListSize = CollectionUtil.size(splitCmdList);
                int cmdValueLength = IpcBleCmd.getTextByteSize(info);
                for (int i = 0; i < splitCmdListSize; i++) {
                    String splitCmd = splitCmdList.get(i);
                    sendCmdList.add(String.format(IpcBleCmd.BLE_CMD_DISTRIBUTE_NETWORK_SEND, i, splitCmd));
                }
                sendCmdList.add(String.format(IpcBleCmd.BLE_CMD_DISTRIBUTE_NETWORK_SEND_END, cmdValueLength));
                if (mPresenter != null) {
                    int sendCmdListSize = CollectionUtil.size(sendCmdList);
                    showLoading();
                    mPresenter.startSendCmdList(sendCmdListSize, new BluetoothScanContract.SendCmdListListener() {
                        @Override
                        public void onSendCmd(int state, int cmdListSize, int cmdIndex) {
                            if (state == SDKConstant.SUCCESS && cmdListSize == sendCmdListSize && CollectionUtil.isIndexSafe(cmdIndex, cmdListSize)) {
                                sendCmd(sendCmdList.get(cmdIndex));
                                if (cmdIndex == cmdListSize - 1) {
                                    hideLoading();
                                }
                            } else {
                                hideLoading();
                            }
                        }
                    });
                }
            } catch (Exception e) {
                NooieLog.printStackTrace(e);
            }
        } else {
        }
    }

    private String convertTextToByteString(String text) {
        StringBuilder byteStrSb = new StringBuilder();
        if (TextUtils.isEmpty(text)) {
            return byteStrSb.toString();
        }
        byte[] textBytes = text.getBytes();
        int len = textBytes.length;
        for (int i = 0; i < len; i++) {
            byteStrSb.append(textBytes[i]);
        }
        return byteStrSb.toString();
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
            if (mSendBluetoothCmdType == SEND_BLUETOOTH_CMD_TYPE_SEND_DISTRIBUTE_NETWORK) {
                String ssid = ipvSsid != null ? ipvSsid.getInputTextNoTrim() : null;
                String psdStr = ipvPsd != null ? ipvPsd.getInputTextNoTrim() : null;
                sendDistributeNetworkCmd(createDistributeNetworkInfo(ssid, psdStr));
            }
        } else {
        }
    }

    private void dealAfterDistributeNetworkByBluetooth(boolean isDistributeNetworkSuccess) {
        if (isDistributeNetworkSuccess) {
            setIsGotoOtherPage(true);
            String ssid = ipvSsid != null ? ipvSsid.getInputTextNoTrim() : null;
            String psdStr = ipvPsd != null ? ipvPsd.getInputTextNoTrim() : null;
            NooieScanActivity.toNooieScanActivity(this, ssid, psdStr, getDeviceModel(), getConnectionMode(), null);
            tryDisconnectBluetooth();
            finish();
        } else {
            //showBluetoothOperationTip(BLUETOOTH_OPERATION_TIP_TYPE_SETTING_FAIL);
        }
    }

    private String createDistributeNetworkInfo(String ssid, String psd) {
        try {
            String zone = CountryUtil.getCurrentTimezone() + ".00";
            String area = TextUtils.isEmpty(GlobalData.getInstance().getRegion()) ? ApHelper.getInstance().getCurrentRegion().toUpperCase() : GlobalData.getInstance().getRegion().toUpperCase();
            String wifiInfo = String.format("WIFI:U:%s;Z:%s;R:%s;T:WPA;P:\"%s\";S:%s;", mUid, zone, area, psd, ssid);
            String lenInfo = String.format("L:%d;%d;%d;", StringHelper.getStringByteSize(psd, StringHelper.CharSet_UTF_8), StringHelper.getStringByteSize(ssid, StringHelper.CharSet_UTF_8), StringHelper.getStringByteSize(wifiInfo, StringHelper.CharSet_UTF_8));
            String info = new StringBuilder()
                    .append(lenInfo)
                    .append(wifiInfo)
                    .toString();
            NooieLog.d("-->> InputWiFiPsdActivity createDistributeNetworkInfo info=" + info);
            return info;
        } catch (Exception e) {
            NooieLog.printStackTrace(e);
        }
        return null;
    }

    private void showBluetoothOperationTip(int type) {
        if (!NooieDeviceHelper.isSupportBleDistributeNetwork(getDeviceModel())) {
            return;
        }
        if (type == BLUETOOTH_OPERATION_TIP_TYPE_UNBIND) {
            showDeviceModeErrorDialog(type);
        } else if (type == BLUETOOTH_OPERATION_TIP_TYPE_BOUND) {
            showDeviceModeErrorDialog(type);
        } else if (type == BLUETOOTH_OPERATION_TIP_TYPE_BOUND_BY_OTHER) {
            showDeviceModeErrorDialog(type);
        } else if (type == BLUETOOTH_OPERATION_TIP_TYPE_SUCCESS) {
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

    private void showDeviceModeErrorDialog(int type) {
        if (mShowDeviceModeErrorDialog != null && mShowDeviceModeErrorDialog.isShowing()) {
            return;
        }
        String content = getString(R.string.bluetooth_scan_operation_tip_unbind);
        if (type == BLUETOOTH_OPERATION_TIP_TYPE_BOUND) {
            content = getString(R.string.bluetooth_scan_operation_tip_bound_by_self);
        } else if (type == BLUETOOTH_OPERATION_TIP_TYPE_BOUND_BY_OTHER) {
            content = getString(R.string.bluetooth_scan_operation_tip_bound);
        }
        hideDeviceModeErrorDialog();
        mShowDeviceModeErrorDialog = DialogUtils.showInformationDialog(this, "", content, getString(R.string.bluetooth_scan_operation_tip_unbind_confirm), false, false, new DialogUtils.OnClickInformationDialogLisenter() {
            @Override
            public void onConfirmClick() {
                redirectGotoHomePage();
            }
        });
    }

    private void hideDeviceModeErrorDialog() {
        if (mShowDeviceModeErrorDialog != null) {
            mShowDeviceModeErrorDialog.dismiss();
            mShowDeviceModeErrorDialog = null;
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

    private int getConnectionMode() {
        if (getStartParam() == null) {
            return ConstantValue.CONNECTION_MODE_QC;
        }
        return getStartParam().getInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC);
    }

    private String getDeviceModel() {
        if (getStartParam() == null) {
            return IpcType.PC420.getType();
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_IPC_MODEL);
    }

    private String getBleAddress() {
        if (getStartParam() == null) {
            return null;
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_BLE_DEVICE);
    }

    private boolean getBluetoothConnectSuccess() {
        if (getStartParam() == null) {
            return false;
        }
        return getStartParam().getBoolean(ConstantValue.INTENT_KEY_DATA_PARAM_4, false);
    }

    @Override
    public String getExternal() {
        return NooieDeviceHelper.createDistributionNetworkExternal(false);
    }
}
