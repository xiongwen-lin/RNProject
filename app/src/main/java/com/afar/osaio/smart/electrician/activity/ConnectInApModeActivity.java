package com.afar.osaio.smart.electrician.activity;

import android.Manifest;
import android.app.AlertDialog;
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
import android.widget.ScrollView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.electrician.presenter.ConnectApModePresenter;
import com.afar.osaio.smart.electrician.util.DialogUtil;
import com.afar.osaio.smart.electrician.util.NetworkUtil;
import com.afar.osaio.smart.electrician.view.IConnectApModeView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

public class ConnectInApModeActivity extends BaseActivity implements IConnectApModeView {
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.svApModeFirst)
    ScrollView svApModeFirst;
    @BindView(R.id.svApModeNext)
    ScrollView svApModeNext;
    @BindView(R.id.ivStateStep1)
    ImageView ivStateStep1;
    @BindView(R.id.ivStateStep2)
    ImageView ivStateStep2;
    @BindView(R.id.ivStateStep3)
    ImageView ivStateStep3;
    @BindView(R.id.ivStateStep4)
    ImageView ivStateStep4;
    @BindView(R.id.tvApModeStep1)
    TextView tvApModeStep1;
    @BindView(R.id.tvApModeStep2)
    TextView tvApModeStep2;
    @BindView(R.id.tvApModeStep3)
    TextView tvApModeStep3;
    @BindView(R.id.tvApModeStep4)
    TextView tvApModeStep4;
    @BindView(R.id.ivStateStep6)
    ImageView ivStateStep6;

    private ConnectApModePresenter mConnectApModePresenter;
    private String mSSID;
    private String mPsd;
    private String mToken;
    private String mAddType;
    private String mApSSid;
    private boolean haveDone;

    public static void toConnectInApModeActivity(Context from, String ssid, String psd, String addType) {
        Intent intent = new Intent(from, ConnectInApModeActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_PSD, psd);
        intent.putExtra(ConstantValue.INTENT_KEY_SSID, ssid);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ADD_TYPE, addType);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_in_ap_mode);
        ButterKnife.bind(this);
        setupView();
        initData();
    }

    @Override
    public void onResume() {
        super.onResume();

        NooieLog.e("----->>> onResume ");

        if (svApModeNext.getVisibility() == View.VISIBLE) {
            NooieLog.e("----->>> onResume doWifiSSid()");
            doWifiSSid();
        }
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
        } else {
            mSSID = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SSID);
            mPsd = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_PSD);
            mAddType = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ADD_TYPE);
            if (mAddType.equals(ConstantValue.ADD_DEVICE)) {
                ivStateStep1.setImageResource(R.drawable.device_reset_step1);
                ivStateStep2.setImageResource(R.drawable.power_strip_reset_step2);
                ivStateStep3.setImageResource(R.drawable.device_reset_step3);
                ivStateStep4.setImageResource(R.drawable.device_reset_step4);
                ivStateStep4.setVisibility(View.VISIBLE);
            } else if (mAddType.equals(ConstantValue.ADD_POWERSTRIP)) {
                ivStateStep1.setImageResource(R.drawable.power_strip_reset_step1);
                ivStateStep2.setImageResource(R.drawable.power_strip_reset_step2);
                ivStateStep3.setImageResource(R.drawable.power_strip_reset_step3);
                ivStateStep4.setImageResource(R.drawable.power_strip_reset_step4);
                ivStateStep4.setVisibility(View.VISIBLE);
            } else if (mAddType.equals(ConstantValue.ADD_SWITCH)) {
                ivStateStep1.setImageResource(R.drawable.power_strip_reset_step1);
                ivStateStep2.setImageResource(R.drawable.power_strip_reset_step2);
                ivStateStep3.setImageResource(R.drawable.power_strip_reset_step3);
                ivStateStep4.setImageResource(R.drawable.power_strip_reset_step4);
                ivStateStep4.setVisibility(View.VISIBLE);
                tvApModeStep2.setText(R.string.scan_rest_switch_guide_info_2);
                tvApModeStep4.setText(R.string.scan_rest_switch_guide_info_4);
            } else if (mAddType.equals(ConstantValue.ADD_LAMP)) {
                ivStateStep1.setImageResource(R.drawable.light_reset_step1);
                ivStateStep2.setImageResource(R.drawable.light_reset_step2);
                ivStateStep3.setImageResource(R.drawable.light_reset_step3);
                ivStateStep4.setImageResource(R.drawable.light_reset_step4);
                ivStateStep4.setVisibility(View.VISIBLE);
                tvApModeStep1.setText(R.string.light_scan_reset_device_guide_info_1);
                tvApModeStep2.setText(R.string.light_scan_reset_device_guide_info_2);
                tvApModeStep3.setText(R.string.light_scan_reset_device_guide_info_3);
                tvApModeStep4.setText(R.string.light_scan_reset_device_guide_info_4);
            } else if (mAddType.equals(ConstantValue.ADD_LIGHT_STRIP)) {
                ivStateStep1.setImageResource(R.drawable.light_strip_reset_step1);
                ivStateStep2.setImageResource(R.drawable.light_strip_reset_step2);
                ivStateStep3.setImageResource(R.drawable.light_strip_reset_step3);
                ivStateStep4.setImageResource(R.drawable.light_strip_reset_step4);
                ivStateStep4.setVisibility(View.VISIBLE);
                tvApModeStep1.setText(R.string.light_strip_reset_device_guide_info_1);
                tvApModeStep2.setText(R.string.light_scan_reset_device_guide_info_2);
                tvApModeStep3.setText(R.string.light_scan_reset_device_guide_info_3);
                tvApModeStep4.setText(R.string.light_strip_scan_reset_device_guide_info_4);
            } else if (mAddType.equals(ConstantValue.ADD_LIGHT_MODULATOR)) {
                ivStateStep1.setImageResource(R.drawable.light_modulator_reset_step1);
                ivStateStep2.setImageResource(R.drawable.power_strip_reset_step2);
                ivStateStep3.setImageResource(R.drawable.power_strip_reset_step3);
                ivStateStep4.setImageResource(R.drawable.power_strip_reset_step4);
                ivStateStep4.setVisibility(View.VISIBLE);
            }
        }
        //获取token
        mConnectApModePresenter = new ConnectApModePresenter(this);
        mConnectApModePresenter.getToken();
    }

    private void setupView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.connect_in_AP_mode);
        ivRight.setVisibility(View.INVISIBLE);
        svApModeFirst.setVisibility(View.VISIBLE);
        svApModeNext.setVisibility(View.GONE);
    }

    @OnClick({R.id.ivLeft, R.id.btnNext, R.id.btnDone})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                onBackPressed();
                break;
            case R.id.btnNext:
                changeDone();
                doWifiSSid();
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
    public void onBackPressed() {
        if (svApModeFirst.getVisibility() == View.VISIBLE) {
            finish();
        } else {
            mIsNormalDenied = false;
            haveRequestPer = false;
            haveDone = false;
            svApModeFirst.setVisibility(View.VISIBLE);
            svApModeNext.setVisibility(View.GONE);
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

    private void changeDone() {
        svApModeFirst.setVisibility(View.GONE);
        svApModeNext.setVisibility(View.VISIBLE);
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
                        EasyPermissions.somePermissionDenied(ConnectInApModeActivity.this,
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
        /*if (!isFirstLaunch()) {
            if ((!mDeviceType.getType().equals("IPC-UNKNOWN")) && !TextUtils.isEmpty(ssid) && !ssid.contains("Teckin")) {
                ToastUtil.showToast(this, getString(R.string.connection_to_wifi_find_no_ap));
            }
        }*/
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
