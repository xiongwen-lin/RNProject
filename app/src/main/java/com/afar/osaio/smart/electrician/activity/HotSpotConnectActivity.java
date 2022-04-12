package com.afar.osaio.smart.electrician.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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
import com.afar.osaio.smart.electrician.presenter.ConnectApModePresenter;
import com.afar.osaio.smart.electrician.util.DialogUtil;
import com.afar.osaio.smart.electrician.util.NetworkUtil;
import com.afar.osaio.smart.electrician.view.IConnectApModeView;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.nooie.common.utils.log.NooieLog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

public class HotSpotConnectActivity extends BaseActivity implements IConnectApModeView {
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.ivStateStep6)
    ImageView ivStateStep6;

    private ConnectApModePresenter mConnectApModePresenter;
    private String mSSID;
    private String mPsd;
    private String mToken;
    private String mAddType;
    private String mApSSid;
    private boolean haveDone;

    public static void toHotSpotConnectActivity(Context from, String ssid, String psd, String addType) {
        Intent intent = new Intent(from, HotSpotConnectActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_PSD, psd);
        intent.putExtra(ConstantValue.INTENT_KEY_SSID, ssid);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ADD_TYPE, addType);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_in_hot_spot);
        ButterKnife.bind(this);
        setupView();
        initData();
    }

    @Override
    public void onResume() {
        super.onResume();
        NooieLog.e("----->>> onResume doWifiSSid()");
        doWifiSSid();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
        } else {
            mSSID = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SSID);
            mPsd = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_PSD);
            mAddType = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ADD_TYPE);
        }
        //获取token
        mConnectApModePresenter = new ConnectApModePresenter(this);
        mConnectApModePresenter.getToken();
    }

    private void setupView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.connect_in_AP_mode);
        ivRight.setVisibility(View.INVISIBLE);
    }

    @OnClick({R.id.ivLeft, R.id.btnDone})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnDone:
                if (haveDone) {
                    gotoSetting();
                } else {
                    doWifiSSid();
                }
                break;
        }
    }


    @Override
    public void onGetTokenSuccess(String token) {
        mToken = token;
    }

    @Override
    public void onGetTokenFailed() {

    }

    private boolean haveRequestPer = false;
    private AlertDialog mSsidWarningDialog = null;
    private AlertDialog mConnectWifiDialog = null;
    private boolean mIsNormalDenied = false;

    private static final String[] PERMS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private void gotoSetting() {
        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    }

    private void doWifiSSid() {
        NooieLog.e("------->>> onResume ");
        if (NetworkUtil.isWifiConnected(NooieApplication.mCtx)) {//已连接Wifi

            mApSSid = NetworkUtil.getSSIDAuto(NooieApplication.mCtx);

            boolean isCheckPerm = !haveRequestPer && TextUtils.isEmpty(mApSSid);
            if (isCheckPerm) {
                showSsidWarningDialog(mApSSid);
                return;
            }

            if (!TextUtils.isEmpty(mApSSid)) {
                if (haveDone) {
                    goNext(mApSSid);
                } else {
                    haveDone = true;
                }
                hideDeniedDialog();
            } else {
                if (haveDone) {
                    goNext(mApSSid);
                } else if (EasyPermissions.hasPermissions(NooieApplication.mCtx, PERMS)) {
                    haveDone = true;
                    hideDeniedDialog();
                } else {
                    showPermDeniedDialog();
                }
            }

        } else {
            showConnectWifiDialog();
        }

    }

    private boolean showSsidWarningDialog(String ssid) {

        haveRequestPer = true;

        if (!TextUtils.isEmpty(ssid)) {
            return false;
        }
        List<String> permList = new ArrayList<>();
        for (int i = 0; i < PERMS.length; i++) {
            permList.add(PERMS[i]);
        }
        NooieLog.e("-->> InputWiFiPsdActivity showSsidWarningDialog perm isHas=" +
                EasyPermissions.hasPermissions(NooieApplication.mCtx, PERMS) + " isDenied=" +
                EasyPermissions.somePermissionDenied(this, Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION) + " isPemDenied=" +
                EasyPermissions.somePermissionPermanentlyDenied(this, permList));
        if (TextUtils.isEmpty(ssid) && !EasyPermissions.hasPermissions(NooieApplication.mCtx, PERMS)) {
            showPermDeniedDialog();
        } else {

            if (haveDone) {
                goNext(ssid);
            } else if (EasyPermissions.hasPermissions(NooieApplication.mCtx, PERMS)) {
                haveDone = true;
                hideDeniedDialog();
            }
        }
        return true;
    }


    private void showPermDeniedDialog() {
        if (mSsidWarningDialog != null) {
            mSsidWarningDialog.dismiss();
        }

        mSsidWarningDialog = DialogUtil.showAPPermissionDialog(this, new DialogUtil.OnClickSingleButtonListener() {
            @Override
            public void onClick() {
                mIsNormalDenied = !EasyPermissions.hasPermissions(NooieApplication.mCtx, PERMS) &&
                        EasyPermissions.somePermissionDenied(HotSpotConnectActivity.this,
                                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
                requestPermissions(PERMS);
            }
        });

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mIsNormalDenied = false;
        haveRequestPer = false;
        haveDone = false;

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
//        super.onPermissionsDenied(requestCode, perms);
        NooieLog.e("-------->> onPermissionsDenied requestCode " + requestCode);
        NooieLog.e("-->> InputWiFiPsdActivity onPermissionsDenied somePermissionPermanentlyDenied=" + EasyPermissions.somePermissionPermanentlyDenied(this, perms) + " mIsNormalDenied=" + mIsNormalDenied);
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms) && !mIsNormalDenied) {
            mIsNormalDenied = false;
            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.fromParts("package", getPackageName(), null));
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (mIsNormalDenied) {
            showPermDeniedDialog();
        }
    }

    private void hideDeniedDialog() {
        if (mSsidWarningDialog != null) {
            mSsidWarningDialog.dismiss();
        }
    }

    private void showConnectWifiDialog() {

        if (mConnectWifiDialog != null) {
            mConnectWifiDialog.dismiss();
        }

        mConnectWifiDialog = DialogUtil.showSingleBtnDialog(this, getString(R.string.not_connect), getString(R.string.not_connect_middle_tip), R.string.connect_btn_title, false, new DialogUtil.OnClickSingleButtonListener() {
            @Override
            public void onClick() {
                startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), ConstantValue.REQUEST_CODE_WIFI_SETTING);
            }
        });
    }


    private void goNext(String ssid) {
        if (!isFirstLaunch()) {
            if (TextUtils.isEmpty(mAddType) && !TextUtils.isEmpty(ssid) && !ssid.contains("Teckin")) {
                ToastUtil.showToast(this, getString(R.string.connection_to_wifi_find_no_ap));
            }
        }
        if (TextUtils.isEmpty(ssid) || ssid.contains("SmartLife")) {
            NooieLog.e("----->>>>SmartLife mSSID " + mSSID + "  mPsd " + mPsd + " mToken " + mToken + "  mAddType " + mAddType);
            ScanDeviceActivity.toScanDeviceActivity(this, mSSID, mPsd, ConstantValue.AP_MODE, mToken, mAddType);
            finish();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSsidWarningDialog != null) {
            mSsidWarningDialog.dismiss();
            mSsidWarningDialog = null;
        }
    }
}
