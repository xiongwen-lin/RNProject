package com.afar.osaio.smart.mixipc.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.cache.BleApDeviceInfoCache;
import com.afar.osaio.smart.device.helper.DeviceConnectionHelper;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.mixipc.contract.ConnectApDeviceContract;
import com.afar.osaio.smart.mixipc.presenter.ConnectApDevicePresenter;
import com.afar.osaio.smart.player.activity.NooiePlayActivity;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.data.EventDictionary;
import com.nooie.sdk.bean.IpcType;
import com.nooie.sdk.bean.SDKConstant;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by victor on 2018/11/12
 * Email is victor.qiao.0604@gmail.com
 */
public class ConnectApDeviceActivity extends BaseActivity implements ConnectApDeviceContract.View {
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.tvConnectApDeviceSSID)
    TextView tvConnectApDeviceSSID;
    @BindView(R.id.tvConnectApDevicePw)
    TextView tvConnectApDevicePw;
    @BindView(R.id.tvConnectApDeviceForgotPw)
    TextView tvConnectApDeviceForgotPw;

    private ConnectApDeviceContract.Presenter mPresenter;
    private boolean mIsNormalDenied = false;
    private boolean mIsGotoNamePage = false;
    private Dialog mForgotDevicePasswordDialog = null;

    public static void toConnectApDeviceActivity(Context from, Bundle param) {
        Intent intent = new Intent(from, ConnectApDeviceActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_START_PARAM, param);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_ap_device);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    private void initData() {
        if (isCurrentIntentNull()) {
            finish();
            return;
        }
        new ConnectApDevicePresenter(this);
        switchCheckDeviceConnection(true);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.add_camera_connect_to_wifi);
        ivRight.setVisibility(View.GONE);
        setupView();
        showBluetoothConnectSuccessToast();
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
    }

    private void resumeData() {
        checkApSsid();
    }

    private void checkApSsid() {
        if (mIsGotoNamePage) {
            mIsGotoNamePage = false;
            return;
        }
        if (mPresenter != null) {
            NooieLog.d("-->> ConnectToWiFiActivity testApDirectConnect 0");
            showLoading();
            mPresenter.checkConnectAp();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.destroy();
        }
        switchCheckDeviceConnection(false);
        hideCheckLocationEnableDialog();
        hideCheckLocalPermForBluetoothDialog();
        hideForgotDevicePasswordDialog();
        hideLoading();
        releaseRes();
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        ivLeft = null;
        tvTitle = null;
        ivRight = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        tryDisconnectBluetooth();
    }

    @OnClick({R.id.ivLeft, R.id.btnConnectApDevice, R.id.tvConnectApDeviceForgotPw})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                tryDisconnectBluetooth();
                finish();
                break;
            case R.id.btnConnectApDevice:
                NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_CLICK_GO_TO_CONNECT_WIFI);
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_JUMP_WIFI_SETTING);
                break;
            case R.id.tvConnectApDeviceForgotPw:
                showForgotDevicePasswordDialog();
                break;
        }
    }

    @Override
    public void setPresenter(@NonNull ConnectApDeviceContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onCheckConnectAp(String result, String ssid) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
        NooieLog.d("-->> ConnectToWiFiActivity testApDirectConnect 1 result=" + result + " ssid=" + ssid);
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            if (NooieDeviceHelper.mergeIpcType(getDeviceModel()) == IpcType.MC120) {
                //gotoScan(ssid);
                checkNormalApSsid(ssid);
                return;
            }
            if (!NooieDeviceHelper.checkBluetoothApFutureCode(ssid, getDeviceSsid())) {
                NooieLog.d("-->> ConnectToWiFiActivity testApDirectConnect 2 isFirstLaunch=" + isFirstLaunch());
                if (!isFirstLaunch()) {
                    ToastUtil.showToast(this, getString(R.string.connection_to_wifi_find_no_ap));
                }
            } else {
                NooieLog.d("-->> ConnectToWiFiActivity testApDirectConnect 3");
                startApDeviceConnect(ssid);
            }
        }
    }

    @Override
    public void permissionsGranted(int requestcode) {
        if (requestcode == ConstantValue.REQUEST_CODE_FOR_LOCATION_PERM) {
            checkApSsid();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
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
        showCheckLocalPermDialog(getString(R.string.connection_to_wifi_no_perm_for_ssid), new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                mIsNormalDenied = !EasyPermissions.hasPermissions(NooieApplication.mCtx, ConstantValue.PERM_GROUP_LOCATION) && EasyPermissions.somePermissionDenied(ConnectApDeviceActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
                requestPermission(ConstantValue.PERM_GROUP_LOCATION, ConstantValue.REQUEST_CODE_FOR_LOCATION_PERM);
            }

            @Override
            public void onClickLeft() {
            }
        });
    }

    @Override
    public void onStartAPDirectConnect(String result, Bundle param, String deviceId) {
        if (isDestroyed()) {
            return;
        }
        NooieLog.d("-->> ConnectToWiFiActivity testApDirectConnect 4");
        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            /*
            String deviceSsid = param != null ? param.getString(ApHelper.KEY_PARAM_SSID) : null;
            ApHelper.getInstance().setApDirectConnectionCheckState(ApHelper.AP_DIRECT_CONNECTION_CHECK_FINISH);
            NooiePlayActivity.startPlayActivity(this, ConstantValue.VICTURE_AP_DIRECT_DEVICE_ID, IpcType.MC120.getType(), ConstantValue.NOOIE_PLAYBACK_TYPE_LIVE, ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_NORMAL, 0, ConstantValue.ROUTE_SOURCE_ADD_DEVICE, ConstantValue.CONNECTION_MODE_AP_DIRECT, deviceSsid);
            finish();
             */
            String deviceSsid = param != null ? param.getString(ApHelper.KEY_PARAM_SSID) : null;
            gotoNextPage(deviceId, getDeviceModel(), deviceSsid);
        }
    }

    @Override
    public void onStartBluetoothAPConnect(int result, Bundle param, String deviceId) {
        if (isDestroyed()) {
            return;
        }
        if (result == SDKConstant.SUCCESS) {
            /*
            mIsGotoNamePage = true;
            String deviceSsid = param != null ? param.getString(ApHelper.KEY_PARAM_SSID) : null;
            ApHelper.getInstance().setApDirectConnectionCheckState(ApHelper.AP_DIRECT_CONNECTION_CHECK_FINISH);
            Bundle param1 = new Bundle();
            param1.putString(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
            param1.putString(ConstantValue.INTENT_KEY_IPC_MODEL, getDeviceModel());
            param1.putString(ConstantValue.INTENT_KEY_SSID, deviceSsid);
            NameDeviceActivity.toNameDeviceActivity(this, param1);
            finish();
             */
            String deviceSsid = param != null ? param.getString(ApHelper.KEY_PARAM_SSID) : null;
            gotoNextPage(deviceId, getDeviceModel(), deviceSsid);
        }
    }

    private void setupView() {
        if (NooieDeviceHelper.mergeIpcType(getDeviceModel()) != IpcType.HC320) {
            tvConnectApDeviceSSID.setVisibility(View.GONE);
            tvConnectApDevicePw.setVisibility(View.GONE);
            tvConnectApDeviceForgotPw.setVisibility(View.GONE);
            return;
        }
        tvConnectApDeviceSSID.setText(String.format(getString(R.string.modify_camera_password_ssid), getDeviceSsid()));
        tvConnectApDevicePw.setText(String.format(getString(R.string.modify_camera_password_defaut_pw), getDeviceDefaultPw()));
        tvConnectApDeviceSSID.setVisibility(View.VISIBLE);
        tvConnectApDevicePw.setVisibility(getIsDefaultPw() ? View.VISIBLE : View.GONE);
        tvConnectApDeviceForgotPw.setVisibility(getIsDefaultPw() ? View.GONE : View.VISIBLE);
        //tvConnectApDeviceForgotPw.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
    }

    private void checkNormalApSsid(String ssid) {
        if (TextUtils.isEmpty(ssid) && !requestLocationPerm(getString(R.string.add_camera_input_wifi_psd_location_enable), false)) {
            NooieLog.d("-->> ConnectToWiFiActivity testApDirectConnect 2");
        } else if (!NooieDeviceHelper.checkApFutureCode(ssid)) {
            NooieLog.d("-->> ConnectToWiFiActivity testApDirectConnect 3 isFirstLaunch=" + isFirstLaunch());
            if (!isFirstLaunch()) {
                ToastUtil.showToast(this, getString(R.string.connection_to_wifi_find_no_ap));
            }
        } else {
            NooieLog.d("-->> ConnectToWiFiActivity testApDirectConnect 4");
            startApDeviceConnect(ssid);
        }
    }

    private void startApDeviceConnect(String deviceSsid) {
        if (getConnectionMode() == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            NooieLog.d("-->> ConnectToWiFiActivity testApDirectConnect 3");
            DeviceConnectionHelper.getInstance().removeConnectionsForAp();
            Bundle param = new Bundle();
            if (NooieDeviceHelper.mergeIpcType(getDeviceModel()) == IpcType.HC320 && mPresenter != null) {
                showLoading();
                param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, getDeviceModel());
                param.putInt(ApHelper.KEY_PARAM_DEVICE_TYPE, ConstantValue.AP_DEVICE_TYPE_BLE_LP);
                param.putInt(ApHelper.KEY_PARAM_CONNECTION_MODE, getConnectionMode());
                param.putString(ApHelper.KEY_PARAM_SSID, deviceSsid);
                param.putString(ApHelper.KEY_PARAM_DEFAULT_PW, ConstantValue.DEFAULT_PASSWORD_AP_P2P);
                param.putString(ApHelper.KEY_PARAM_DEVICE_ID, ConstantValue.DEFAULT_UUID_AP_P2P);
                param.putString(ApHelper.KEY_PARAM_SERVER, ConstantValue.DEFAULT_SERVER_AP_P2P);
                param.putInt(ApHelper.KEY_PARAM_PORT, ConstantValue.DEFAULT_PORT_AP_P2P);
                param.putString(ApHelper.KEY_PARAM_DEVICE_MODEL, getDeviceModel());
                param.putString(ApHelper.INTENT_KEY_BLE_DEVICE, getBleAddress());
            } else if (mPresenter != null) {
                showLoading();
                param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, getDeviceModel());
                param.putInt(ApHelper.KEY_PARAM_DEVICE_TYPE, ConstantValue.AP_DEVICE_TYPE_IPC);
                param.putInt(ApHelper.KEY_PARAM_CONNECTION_MODE, getConnectionMode());
                param.putString(ApHelper.KEY_PARAM_SSID, deviceSsid);
            }
            if (mPresenter != null) {
                mPresenter.startBluetoothAPConnect(param);
            }
        }
    }

    private void gotoNextPage(String deviceId, String model, String deviceSsid) {
        boolean isApDeviceExist = BleApDeviceInfoCache.getInstance().isExisted(deviceId);
        if (isApDeviceExist) {
            ApHelper.getInstance().setApDirectConnectionCheckState(ApHelper.AP_DIRECT_CONNECTION_CHECK_FINISH);
            NooiePlayActivity.startPlayActivity(this, deviceId, model, ConstantValue.NOOIE_PLAYBACK_TYPE_LIVE, ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_NORMAL, 0, ConstantValue.ROUTE_SOURCE_ADD_DEVICE, ConstantValue.CONNECTION_MODE_AP_DIRECT, deviceSsid);
        } else {
            mIsGotoNamePage = true;
            ApHelper.getInstance().setApDirectConnectionCheckState(ApHelper.AP_DIRECT_CONNECTION_CHECK_FINISH);
            Bundle param = new Bundle();
            param.putString(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
            param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, model);
            param.putString(ConstantValue.INTENT_KEY_SSID, deviceSsid);
            NameDeviceActivity.toNameDeviceActivity(this, param);
        }
        finish();
    }

    private void showForgotDevicePasswordDialog() {
        hideForgotDevicePasswordDialog();
        mForgotDevicePasswordDialog = DialogUtils.showInformationDialog(this, getString(R.string.connect_ap_device_forgot_pw_title), getString(R.string.connect_ap_device_forgot_pw_message), getString(R.string.ok), true, false, new DialogUtils.OnClickInformationDialogLisenter() {
            @Override
            public void onConfirmClick() {
            }
        });
    }

    private void hideForgotDevicePasswordDialog() {
        if (mForgotDevicePasswordDialog != null) {
            mForgotDevicePasswordDialog.dismiss();
            mForgotDevicePasswordDialog = null;
        }
    }

    private void showBluetoothOperationTip(int type) {
        ToastUtil.showToast(this, R.string.bluetooth_scan_operation_tip_success);
    }

    private void showBluetoothConnectSuccessToast() {
        if (!getBluetoothConnectSuccess()) {
            return;
        }
        showBluetoothOperationTip(0);
    }

    private String getDeviceSsid() {
        if (getStartParam() == null) {
            return null;
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_SSID);
    }

    private String getDeviceDefaultPw() {
        if (getStartParam() == null) {
            return null;
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_PSD);
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

    private boolean getIsDefaultPw() {
        if (getStartParam() == null) {
            return false;
        }
        return getStartParam().getBoolean(ConstantValue.INTENT_KEY_DATA_PARAM, false);
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
