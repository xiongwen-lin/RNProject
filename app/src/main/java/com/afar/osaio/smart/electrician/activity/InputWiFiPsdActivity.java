package com.afar.osaio.smart.electrician.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.bluetooth.activity.BaseBluetoothActivity;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.electrician.listener.KeyboardChangeListener;
import com.afar.osaio.smart.electrician.util.DialogUtil;
import com.afar.osaio.smart.scan.activity.RouterSettingActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.nooie.common.hardware.bluetooth.BluetoothHelper;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.network.NetworkUtil;
import com.nooie.common.utils.tool.PermissionUtil;
import com.tuya.smart.android.ble.api.ScanDeviceBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * InputWiFiPsdActivity
 *
 * @author Administrator
 * @date 2019/3/5
 * 权限开关检查：(1)wifi是否打开，showConnectWifiDialog()
 *           （2）是否位置授权  showPermDeniedDialog()
 *           （3）是否打开位置  showTopBarView()
 * 模式处理：
 * （4）AP配网：不支持输入wifi名称
 * （5）蓝牙配网：初始化initBle();监听到中断bluetoothStateOffChange，弹窗 showBluetoothDisconnectDialog()
 */
public class InputWiFiPsdActivity extends BaseBluetoothActivity {

    private static final int SSID_CHECK_TYPE_NORMAl = 0;
    private static final int SSID_CHECK_TYPE_FORMAT = 1;
    private static final int SSID_CHECK_TYPE_INVALID = 2;

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.tvInputWifiPsdTip)
    TextView tvInputWifiPsdTip;
    @BindView(R.id.etSsid)
    AutoCompleteTextView etSsid;
    @BindView(R.id.ssid_divider)
    View vSSidLine;
    @BindView(R.id.tvInputTitle)
    TextView tvPwdTitle;
    @BindView(R.id.etInput)
    AutoCompleteTextView etPwd;
    @BindView(R.id.btnInput)
    ImageView ivShowPwd;
    @BindView(R.id.btnDone)
    Button btnDone;
    @BindView(R.id.ivWifi)
    ImageView ivWifi;
    @BindView(R.id.tvWifi)
    TextView tvWifi;
    @BindView(R.id.ivWifiList)
    ImageView ivWifiList;
    @BindView(R.id.clInputWifiPsdContainer)
    ConstraintLayout clInputWifiPsdContainer;

    @BindView(R.id.topBarView)
    View topBarView;


    private int mConMode;
    private String mAddType;

    private AlertDialog mSsidWarningDialog = null;
    private AlertDialog mConnectWifiDialog = null;
    private boolean mIsNormalDenied = false;
    private boolean mSkipQRCodeConfigNetwork;
    private boolean mIsCheckLocationEnable = false;
    private AlertDialog mCheckLocationEnableDialog = null;
    private int deviceType;
    private String uuid;
    private String address;
    private String mac;
    private Dialog mShowBluetoothDisconnectDialog = null;

    private static final String[] PERMS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    public static void toInputWiFiPsdActivity(Context from, int mode, String addType) {
        Intent intent = new Intent(from, InputWiFiPsdActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ADD_TYPE, addType);
        intent.putExtra(ConstantValue.INTENT_KEY_CONFIG_MODE, mode);
        from.startActivity(intent);
    }

    public static void toInputWiFiPsdActivity(Context from, int mode, String addType, int deviceType,String uuid,String address,String mac) {
        Intent intent = new Intent(from, InputWiFiPsdActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ADD_TYPE, addType);
        intent.putExtra(ConstantValue.INTENT_KEY_CONFIG_MODE, mode);
        intent.putExtra(ConstantValue.INTENT_KEY_BLUE_DEVICE_TYPE, deviceType);
        intent.putExtra(ConstantValue.INTENT_KEY_BLUE_DEVICE_UUID, uuid);
        intent.putExtra(ConstantValue.INTENT_KEY_BLUE_DEVICE_ADDRESS, address);
        intent.putExtra(ConstantValue.INTENT_KEY_BLUE_DEVICE_MAC, mac);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teckin_input_wifi_psd);
        ButterKnife.bind(this);
        int screen_h = DisplayUtil.SCREEN_HIGHT_PX - DisplayUtil.dpToPx(NooieApplication.mCtx,
                DisplayUtil.HEADER_BAR_HEIGHT_DP) - DisplayUtil.getStatusBarHeight(NooieApplication.mCtx);
        screen_h = Math.max(DisplayUtil.SCREEN_CONTENT_MIN_HEIGHT_PX, screen_h);
        clInputWifiPsdContainer.setMinHeight(screen_h);
        initView();
        initData();
        setupMoreTv();
    }


    @Override
    public void onResume() {
        super.onResume();
        NooieLog.e("------->>> onResume ");
        if (NetworkUtil.isWifiConnected(NooieApplication.mCtx)) {//已连接Wifi
            if (!TextUtils.isEmpty(com.nooie.common.utils.network.NetworkUtil.getSSIDAuto(NooieApplication.mCtx))) {
                String ssid = com.nooie.common.utils.network.NetworkUtil.getSSIDAuto(NooieApplication.mCtx);
                etSsid.setText(ssid);
                etSsid.setSelection(ssid.length());
            }
            checkSsid();
        } else {
            showConnectWifiDialog();
        }
    }

    private void checkSsid(){
        String ssid = etSsid.getText().toString();
        if (!TextUtils.isEmpty(ssid)) {
            GlobalPrefs prefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
            prefs.loadWifiInfo();
            String psd = prefs.getWifiPsd(ssid);
            if (!TextUtils.isEmpty(psd)) {
                etPwd.setText(psd);
                etPwd.setSelection(psd.length());
            }
            topBarView.setVisibility(View.GONE);
            return;
        }
        if (!EasyPermissions.hasPermissions(NooieApplication.mCtx, PERMS)) {
            showPermDeniedDialog();
            return ;
        }
        if (!PermissionUtil.isLocationEnabled(NooieApplication.mCtx)) {
            showTopBarView();
        }else{
            topBarView.setVisibility(View.GONE);
        }

        if (EasyPermissions.hasPermissions(NooieApplication.mCtx, PERMS) &&TextUtils.isEmpty(ssid)&& mConMode != ConstantValue.AP_MODE) {
            showGetSsidFailDialog();
        }
    }
    private void setupMoreTv() {
        String wifiText = getString(R.string.add_camera_input_wifi_pas_24g_tip);
        String wifi = getString(R.string.add_camera_input_wifi_24g_tip);
        String moreTxt = getString(R.string.add_camera_input_wifi_pas_tip_more);
        String text = "";
        text = String.format(getString(R.string.only_support_24G), wifiText, moreTxt);


        SpannableStringBuilder style = new SpannableStringBuilder();

        text.length();

        //设置文字
        style.append(text);

        //设置部分文字点击事件
        ClickableSpan conditionClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                RouterSettingActivity.toRouterSettingActivity(InputWiFiPsdActivity.this);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
            }
        };
        style.setSpan(conditionClickableSpan, text.indexOf(moreTxt), text.indexOf(moreTxt) + moreTxt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvInputWifiPsdTip.setText(style);

        //设置部分文字颜色
        ForegroundColorSpan conditionForegroundColorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.theme_green));
        if (TextUtils.isEmpty(mAddType)) {
            style.setSpan(conditionForegroundColorSpan, text.indexOf(wifi), text.indexOf(wifi) + wifi.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            style.setSpan(conditionForegroundColorSpan, text.indexOf(wifiText), text.indexOf(wifiText) + wifiText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        ForegroundColorSpan commandColorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.theme_green));
        style.setSpan(commandColorSpan, text.indexOf(moreTxt), text.indexOf(moreTxt) + moreTxt.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //配置给TextView
        tvInputWifiPsdTip.setMovementMethod(LinkMovementMethod.getInstance());
        tvInputWifiPsdTip.setText(style);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mIsNormalDenied = false;
    }

    private boolean showSsidWarningDialog(String ssid) {

        if (TextUtils.isEmpty(ssid) && !EasyPermissions.hasPermissions(NooieApplication.mCtx, PERMS)) {
            showPermDeniedDialog();
            return true;
        }else {
            return  false;
        }

    }


    private void showTopBarView() {
        topBarView.setVisibility(View.VISIBLE);
        TextView  tvNetworkWeakTip = topBarView.findViewById(R.id.tvNetworkWeakTip);
        tvNetworkWeakTip.setText(R.string.bluetooth_scan_location_request_title );
        TextView  tvAction = findViewById(R.id.tvAction);
        tvAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
    }


    private void showGetSsidFailDialog() {
        if (mSsidWarningDialog != null) {
            mSsidWarningDialog.dismiss();
        }

        mSsidWarningDialog = DialogUtil.showGetSsidFailDialog(this, false,
                new DialogUtil.OnClickSingleButtonListener() {
                    @Override
                    public void onClick() {
                        if (mConMode != ConstantValue.AP_MODE) {
                            etSsid.setEnabled(true);
                        }
                        etSsid.setText("");
                        etSsid.setSelection(etSsid.getText().toString().length());
                    }
                });

    }

    private void showPermDeniedDialog() {
        if (mSsidWarningDialog != null) {
            mSsidWarningDialog.dismiss();
        }


        mSsidWarningDialog = DialogUtil.showPermissionDialog(this, mConMode,new DialogUtil.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {//授权
                mIsNormalDenied = !EasyPermissions.hasPermissions(NooieApplication.mCtx, PERMS) && EasyPermissions.somePermissionDenied(InputWiFiPsdActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
                requestPermissions(PERMS);
            }

            @Override
            public void onClickLeft() {
                if (mConMode != ConstantValue.AP_MODE){
                    etSsid.setEnabled(true);
                }else {
                    etSsid.setEnabled(false);
                }
            }
        });
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        DisplayUtil.setViewHeight(etPwd, etPwd.getMeasuredHeight());
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.add_camera_connect_to_wifi);
        ivRight.setVisibility(View.INVISIBLE);
        tvPwdTitle.setText(R.string.password);
        ivShowPwd.setVisibility(View.GONE);

        //ssid
        etSsid.addTextChangedListener(new TextWatcher() {
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

        etSsid.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    vSSidLine.setBackgroundColor(getResources().getColor(R.color.theme_text_color));
                } else {
                    vSSidLine.setBackgroundColor(getResources().getColor(R.color.theme_text_gray));
                }
            }
        });


        //默认密码为显示
        etPwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        ivShowPwd.setImageResource(R.drawable.eye_open_icon_state_list);
        etPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ivShowPwd.setVisibility(TextUtils.isEmpty(etPwd.getText().toString()) ? View.GONE : View.VISIBLE);
                //DisplayUtil.setInputBg(etPwd);
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkBtnEnable();
            }
        });

        etPwd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_GO) {
                    hideInputMethod();
                    return true;
                }
                return false;
            }
        });

        new KeyboardChangeListener(this).setKeyBoardListener(new KeyboardChangeListener.KeyBoardListener() {
            @Override
            public void onKeyboardChange(boolean isShow, int keyboardHeight) {
                if (isShow) {
                    ivWifi.setVisibility(View.GONE);
                    tvWifi.setVisibility(View.GONE);
                    ivWifiList.setVisibility(View.GONE);
                } else {
                    ivWifi.setVisibility(View.VISIBLE);
                    tvWifi.setVisibility(View.VISIBLE);
                    ivWifiList.setVisibility(View.VISIBLE);
                }
            }
        });

        etSsid.setFocusable(true);
        etSsid.setFocusableInTouchMode(true);
        etSsid.requestFocus();

        checkBtnEnable();
        //TODO 1.3.3需求：涂鸦插座ap配网wifi输入，如果拿不到ssid之前支持输入，现改为不支持输入
        if (mConMode != ConstantValue.AP_MODE){
            etSsid.setEnabled(TextUtils.isEmpty(com.nooie.common.utils.network.NetworkUtil.getSSIDAuto(NooieApplication.mCtx)));
        }else{
            etSsid.setEnabled(false);
        }
    }

    private void checkBtnEnable() {
        if (!TextUtils.isEmpty(etSsid.getText().toString()) && TextUtils.isEmpty(etPwd.getText().toString())
                || !TextUtils.isEmpty(etSsid.getText().toString()) && etPwd.getText().toString().length() >= 8) {
            btnDone.setEnabled(true);
            btnDone.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
        } else {
            btnDone.setEnabled(false);
            btnDone.setTextColor(getResources().getColor(R.color.unable_clickable_color));
        }
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
        } else {
            mSkipQRCodeConfigNetwork = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_SKIP_QR_CODE_CONFIG_NETWORK, false);
            mConMode = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_CONFIG_MODE, ConstantValue.EC_MODE);
            mAddType = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ADD_TYPE);
            if ( mConMode == ConstantValue.BLUE_MODE){
                deviceType = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_BLUE_DEVICE_TYPE,301); //301双模蓝牙
                uuid = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_BLUE_DEVICE_UUID);
                address = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_BLUE_DEVICE_ADDRESS);
                mac = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_BLUE_DEVICE_MAC);
                initBle();
            }
        }
    }

    @OnClick({R.id.ivLeft, R.id.btnInput, R.id.btnDone, R.id.tvSsidChange})
    public void onViewClicked(View v) {
        switch (v.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.tvSsidChange:
                if (mConMode == ConstantValue.AP_MODE &&!EasyPermissions.hasPermissions(NooieApplication.mCtx, PERMS)){
                    showPermDeniedDialog();
               }else {
                //直接进入手机中的wifi网络设置界面
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
              }

                break;
            case R.id.btnInput:
                if (etPwd.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    etPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    ivShowPwd.setImageResource(R.drawable.eye_close_icon_state_list);
                } else {
                    etPwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    ivShowPwd.setImageResource(R.drawable.eye_open_icon_state_list);
                }
                etPwd.setSelection(etPwd.getText().toString().length());
                break;
            case R.id.btnDone:
                doDone();
                break;

        }
    }

    private void doDone() {
        if (TextUtils.isEmpty(etPwd.getText().toString())) {
            DialogUtil.showConfirmWithSubMsgDialog(this, R.string.no_password, R.string.no_password_detail_tip, R.string.cancel, R.string.confirm_upper, mLogoutListener);
        } else {
            checkSSIDBeforeJump();
        }
    }

    private void checkSSIDBeforeJump() {
        if (checkSSIDEffective(etSsid.getText().toString().trim()) == SSID_CHECK_TYPE_FORMAT) {
            showCheckSSIDDialog(SSID_CHECK_TYPE_FORMAT);
            return;
        } else if (checkSSIDEffective(etSsid.getText().toString().trim()) == SSID_CHECK_TYPE_INVALID) {
            showCheckSSIDDialog(SSID_CHECK_TYPE_INVALID);
            return;
        }
        doNextStep();
    }

    private int checkSSIDEffective(String ssid) {
        if (NooieDeviceHelper.isNetworkOf5G(ssid)) {
            return SSID_CHECK_TYPE_FORMAT;
        } else {
            return SSID_CHECK_TYPE_NORMAl;
        }
    }

    private AlertDialog mCheckSSIDDialog;

    private void showCheckSSIDDialog(int checkType) {
        //String content = checkType == SSID_CHECK_TYPE_FORMAT ? getString(R.string.add_camera_input_wifi_format_tip) : getString(R.string.add_camera_input_wifi_invalid_tip);
        hideCheckSSIDDialog();
        mCheckSSIDDialog = DialogUtil.showConfirmWithSubMsgDialog(this, "", getString(R.string.add_camera_input_wifi_format_tip), R.string.ez_notSupport_5G_continue, R.string.add_camera_input_wifi_go_to_change, new DialogUtil.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }

            @Override
            public void onClickLeft() {
                doNextStep();
            }
        });
    }

    private void hideCheckSSIDDialog() {
        if (mCheckSSIDDialog != null) {
            mCheckSSIDDialog.dismiss();
            mCheckSSIDDialog = null;
        }
    }

    private void doNextStep() {
        GlobalPrefs prefs = GlobalPrefs.getPreferences(NooieApplication.mCtx);
        prefs.saveWifiInfo(etSsid.getText().toString(), etPwd.getText().toString());
        if (mConMode == ConstantValue.EC_MODE) {
            ScanDeviceActivity.toScanDeviceActivity(this, etSsid.getText().toString(), etPwd.getText().toString(), mConMode, null, mAddType);
        } else if (mConMode == ConstantValue.AP_MODE) {
            HotSpotConnectActivity.toHotSpotConnectActivity(this, etSsid.getText().toString(), etPwd.getText().toString(), mAddType);
        }else if (mConMode == ConstantValue.BLUE_MODE) {
            ScanDeviceActivity.toScanDeviceActivity(this, etSsid.getText().toString(), etPwd.getText().toString(), mConMode, null, mAddType,deviceType,uuid,address,mac);
        }

    }

    private DialogUtil.OnClickConfirmButtonListener mLogoutListener = new DialogUtil.OnClickConfirmButtonListener() {
        @Override
        public void onClickRight() {
            checkSSIDBeforeJump();
        }

        @Override
        public void onClickLeft() {
        }
    };

    @Override
    public void permissionsGranted() {
        super.permissionsGranted();
        NooieLog.e("-------->> permissionsGranted ");
        checkSsid();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
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
        }
    }

    @Override
    public void cancelPermission() {
        super.cancelPermission();
        showSsidWarningDialog(etSsid.getText().toString());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSsidWarningDialog != null) {
            mSsidWarningDialog.dismiss();
            mSsidWarningDialog = null;
        }
        hideCheckSSIDDialog();
    }

    @Override
    public void bluetoothStateOffChange() {

        //蓝牙关闭
        NooieLog.d("-->> debug SearchBlueActivity bluetoothStateOffChange() ");
        showBluetoothDisconnectDialog();
    }

    /**
     * 蓝牙断开，请求重连
     */
    private void showBluetoothDisconnectDialog() {
        if (mShowBluetoothDisconnectDialog != null) {
            mShowBluetoothDisconnectDialog.dismiss();
            mShowBluetoothDisconnectDialog = null;
        }
        mShowBluetoothDisconnectDialog = DialogUtils.showInformationDialog(this, getString(R.string.bluetooth_scan_operation_tip_disconnect_title), getString(R.string.bluetooth_scan_operation_tip_disconnect_content), getString(R.string.bluetooth_scan_operation_tip_disconnect_ok), false, false, new DialogUtils.OnClickInformationDialogLisenter() {
            @Override
            public void onConfirmClick() {
                finish();
             }
        });
    }
}
