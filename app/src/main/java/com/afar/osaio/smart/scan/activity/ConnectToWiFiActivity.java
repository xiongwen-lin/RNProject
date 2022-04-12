package com.afar.osaio.smart.scan.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.nooie.data.EventDictionary;
import com.nooie.sdk.bean.IpcType;
import com.afar.osaio.smart.device.helper.DeviceConnectionHelper;
import com.afar.osaio.smart.player.activity.NooiePlayActivity;
import com.afar.osaio.smart.scan.contract.ConnectToWiFiContract;
import com.afar.osaio.smart.scan.presenter.ConnectToWiFiPresenter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;
import com.nooie.common.utils.log.NooieLog;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by victor on 2018/11/12
 * Email is victor.qiao.0604@gmail.com
 */
public class ConnectToWiFiActivity extends BaseActivity implements ConnectToWiFiContract.View {
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;

    private ConnectToWiFiContract.Presenter mPresenter;
    private boolean mIsNormalDenied = false;
    private int mConnectionMode = ConstantValue.CONNECTION_MODE_QC;

    public static void toConnectToWiFiActivity(Context from, String ssid, String psd, String model, int connectionMode) {
        Intent intent = new Intent(from, ConnectToWiFiActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_SSID, ssid);
        intent.putExtra(ConstantValue.INTENT_KEY_PSD, psd);
        intent.putExtra(ConstantValue.INTENT_KEY_IPC_MODEL, model);
        intent.putExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, connectionMode);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_to_wifi);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    private void initData() {
        if (isCurrentIntentNull()) {
            finish();
            return;
        }
        mConnectionMode = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC);
        new ConnectToWiFiPresenter(this);
        switchCheckDeviceConnection(true);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.add_camera_connect_to_wifi);
        ivRight.setVisibility(View.GONE);
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

    @OnClick({R.id.ivLeft, R.id.btnDone})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnDone:
                NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_CLICK_GO_TO_CONNECT_WIFI);
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                NooieDeviceHelper.trackDNEvent(EventDictionary.EVENT_ID_JUMP_WIFI_SETTING);
                break;
        }
    }

    @Override
    public void setPresenter(@NonNull ConnectToWiFiContract.Presenter presenter) {
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
            if (TextUtils.isEmpty(ssid) && !requestLocationPerm(getString(R.string.add_camera_input_wifi_psd_location_enable), false)) {
                NooieLog.d("-->> ConnectToWiFiActivity testApDirectConnect 2");
            } else if (!NooieDeviceHelper.checkApFutureCode(ssid)) {
                NooieLog.d("-->> ConnectToWiFiActivity testApDirectConnect 3 isFirstLaunch=" + isFirstLaunch());
                if (!isFirstLaunch()) {
                    ToastUtil.showToast(this, getString(R.string.connection_to_wifi_find_no_ap));
                }
            } else {
                NooieLog.d("-->> ConnectToWiFiActivity testApDirectConnect 4");
                gotoScan(ssid);
            }
        }
    }

    private void gotoScan(String deviceSsid) {
        if (mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            NooieLog.d("-->> ConnectToWiFiActivity testApDirectConnect 3");
            DeviceConnectionHelper.getInstance().removeConnectionsForAp();
            if (mPresenter != null) {
                showLoading();
                mPresenter.startAPDirectConnect(deviceSsid);
            }
        } else {
            String ssid = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SSID);
            String psd = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_PSD);
            String mode = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_IPC_MODEL);
            NooieScanActivity.toNooieScanActivity(this, ssid, psd, mode, ConstantValue.CONNECTION_MODE_AP, deviceSsid);
            finish();
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
                mIsNormalDenied = !EasyPermissions.hasPermissions(NooieApplication.mCtx, ConstantValue.PERM_GROUP_LOCATION) && EasyPermissions.somePermissionDenied(ConnectToWiFiActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
                requestPermission(ConstantValue.PERM_GROUP_LOCATION, ConstantValue.REQUEST_CODE_FOR_LOCATION_PERM);
            }

            @Override
            public void onClickLeft() {
            }
        });
    }

    @Override
    public void onStartAPDirectConnect(String result, String deviceSsid) {
        if (isDestroyed()) {
            return;
        }
        NooieLog.d("-->> ConnectToWiFiActivity testApDirectConnect 4");
        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            //NooiePlayActivity.startPlayActivity(this, ConstantValue.VICTURE_AP_DIRECT_DEVICE_ID, IpcType.MC120, ConstantValue.NOOIE_PLAYBACK_TYPE_LIVE, ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_NORMAL, 0, ConstantValue.ROUTE_SOURCE_ADD_DEVICE, ConstantValue.CONNECTION_MODE_AP_DIRECT);
            ApHelper.getInstance().setApDirectConnectionCheckState(ApHelper.AP_DIRECT_CONNECTION_CHECK_FINISH);
            NooiePlayActivity.startPlayActivity(this, ConstantValue.VICTURE_AP_DIRECT_DEVICE_ID, IpcType.MC120.getType(), ConstantValue.NOOIE_PLAYBACK_TYPE_LIVE, ConstantValue.NOOIE_PLAYBACK_SOURCE_TYPE_NORMAL, 0, ConstantValue.ROUTE_SOURCE_ADD_DEVICE, ConstantValue.CONNECTION_MODE_AP_DIRECT, deviceSsid);
            finish();
        }
    }

    @Override
    public String getExternal() {
        return NooieDeviceHelper.createDistributionNetworkExternal(false);
    }
}
