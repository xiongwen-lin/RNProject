package com.afar.osaio.smart.scan.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.mixipc.activity.BluetoothScanActivity;
import com.afar.osaio.widget.NEventFButton;
import com.nooie.common.utils.configure.FontUtil;
import com.nooie.sdk.bean.IpcType;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.helper.ResHelper;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.widget.NEventTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

public class AddACameraActivity extends BaseActivity {

    private static final int REQUEST_CODE_FOR_CAMERA = 1;

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.ivState)
    ImageView ivState;
    @BindView(R.id.tvLightNoRight)
    NEventTextView tvLightNoRight;
    @BindView(R.id.container)
    LinearLayout container;
//    @BindView(R.id.svAddCameraContainer)
//    ScrollView svAddCameraContainer;
    @BindView(R.id.textView4)
    TextView tvAddCameraTip1;
    @BindView(R.id.btnDone)
    NEventFButton btnDone;

    private static final int RED_LIGHT_ON = 1;
    private static final int RED_LIGHT_OFF = 2;
    private IpcType mDeviceType;

    public static void toAddACameraActivity(Context from, String model, int connectionMode) {
        Intent intent = new Intent(from, AddACameraActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_IPC_MODEL, model);
        intent.putExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, connectionMode);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_a_camera);
        ButterKnife.bind(this);
        mDeviceType = IpcType.getIpcType(getIntent().getStringExtra(ConstantValue.INTENT_KEY_IPC_MODEL));
        setupView();
    }

    private void setupView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.add_lp_camera_title);
        ivRight.setVisibility(View.INVISIBLE);

        int screen_h = DisplayUtil.SCREEN_HIGHT_PX - DisplayUtil.dpToPx(NooieApplication.mCtx,
                DisplayUtil.HEADER_BAR_HEIGHT_DP) - DisplayUtil.getStatusBarHeight(NooieApplication.mCtx);
        screen_h = Math.max(DisplayUtil.SCREEN_CONTENT_MIN_HEIGHT_PX, screen_h);
        container.setMinimumHeight(screen_h);

        setupClickableTv();

        tvAddCameraTip1.setText(getConnectionMode() == ConstantValue.CONNECTION_MODE_LAN ? R.string.add_camera_guide_info_lan : R.string.add_camera_guide_info);
        if (NooieDeviceHelper.mergeIpcType(mDeviceType) == IpcType.HC320) {
            tvAddCameraTip1.setText(R.string.add_camera_guide_info_hc320);
        }
        ivState.setImageResource(ResHelper.getInstance().getFlashLightOnIconByType(mDeviceType != null ? mDeviceType.getType() : ""));
        btnDone.setExternal(NooieDeviceHelper.createDistributionNetworkExternal(false));
        tvLightNoRight.setExternal(NooieDeviceHelper.createDistributionNetworkExternal(false));
    }

    private void setupClickableTv() {
        final SpannableStringBuilder style = new SpannableStringBuilder();
        String text = getString(R.string.add_camera_light_error);

        //设置文字
        style.append(text);

        //设置部分文字点击事件
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                NoRedLightActivity.toNoRedLightActivity(AddACameraActivity.this, mDeviceType.getType(), getConnectionMode());
            }
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
                ds.setTypeface(FontUtil.loadTypeface(getApplicationContext(), "fonts/manrope-semibold.otf"));
            }
        };
        style.setSpan(clickableSpan, 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvLightNoRight.setText(style);

        //设置部分文字颜色
        ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.theme_gray));
        style.setSpan(foregroundColorSpan, 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //配置给TextView
        tvLightNoRight.setMovementMethod(LinkMovementMethod.getInstance());
        tvLightNoRight.setText(style);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mHandler.post(mTimer);
        //ivState.setTag(RED_LIGHT_ON);
        //ivState.setImageResource(R.drawable.red_light);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //mHandler.removeCallbacks(mTimer);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseRes();
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        ivLeft = null;
        tvTitle = null;
        ivRight = null;
        ivState = null;
        tvLightNoRight = null;
        container = null;
        tvAddCameraTip1 = null;
        btnDone = null;
    }

    @OnClick({R.id.ivLeft, R.id.btnDone})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnDone:
                gotoSetupWifi();
                break;
        }
    }

    @Override
    protected void permissionsGranted(int requestCode) {
        super.permissionsGranted();

        if (requestCode == REQUEST_CODE_FOR_CAMERA) {
            ScanCodeActivity.toScanCodeActivity(this, (mDeviceType != null ? mDeviceType.getType() : ""));
        }

        /*
        if (NetworkUtil.isWifiConnected(NooieApplication.mCtx) &&
                !TextUtils.isEmpty(NetworkUtil.getSSIDAuto(NooieApplication.mCtx))) {
            // connected wifi
            InputWiFiPsdActivity.toInputWiFiPsdActivity(this, false, mDeviceType.getType(), getConnectionMode());
        } else {
            // don't connected wifi
            NoConnectWiFi.toNoConnectWiFi(this);
        }
        */
    }

    private void gotoSetupWifi() {
        NooieLog.d("-->> AddACameraActivity gotoSetupWifi start");
        /*
        if (NetworkUtil.isConnected(NooieApplication.mCtx)) {
            NooieLog.d("-->> AddACameraActivity gotoSetupWifi finish");
            if (getConnectionMode() == ConstantValue.CONNECTION_MODE_LAN) {
                checkPerm();
                return;
            }
            InputWiFiPsdActivity.toInputWiFiPsdActivity(this, false, mDeviceType.getType(), getConnectionMode());
        } else {
            NooieLog.d("-->> AddACameraActivity gotoSetupWifi finish2");
            //requestPermission(PERMS);
            NoConnectWiFi.toNoConnectWiFi(this);
        }
        */
        if (NooieDeviceHelper.isSupportBleDistributeNetwork(getDeviceModel())) {
            gotoConnectBleApDevice();
        } else if (getConnectionMode() == ConstantValue.CONNECTION_MODE_LAN) {
            checkPerm();
        } else {
            gotoInputWifiPage();
        }

    }

    private void checkPerm() {
        if (EasyPermissions.hasPermissions(this, ConstantValue.PERM_GROUP_CAMERA)) {
            ScanCodeActivity.toScanCodeActivity(this, (mDeviceType != null ? mDeviceType.getType() : ""));
        } else {
            requestPermission(ConstantValue.PERM_GROUP_CAMERA, REQUEST_CODE_FOR_CAMERA);
        }
    }

    private void gotoConnectBleApDevice() {
        Bundle param = new Bundle();
        param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, getDeviceModel());
        param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, getConnectionMode());
        param.putInt(ConstantValue.INTENT_KEY_DATA_PARAM_1, ConstantValue.BLUETOOTH_SCAN_TYPE_NEW);
        BluetoothScanActivity.toBluetoothScanActivity(this, param);
    }

    private void gotoInputWifiPage() {
        Bundle param = new Bundle();
        param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, getDeviceModel());
        param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, getConnectionMode());
        InputWiFiPsdActivity.toInputWiFiPsdActivity(this, param);
    }

    private int getConnectionMode() {
        int connectionMode = getCurrentIntent() != null ? getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC) : ConstantValue.CONNECTION_MODE_QC;
        return connectionMode;
    }

    private String getDeviceModel() {
        if (getCurrentIntent() == null) {
            return IpcType.PC420.getType();
        }
        return getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_IPC_MODEL);
    }

    @Override
    public String getExternal() {
        return NooieDeviceHelper.createDistributionNetworkExternal(false);
    }

    /*
    private Handler mHandler = new Handler(Looper.myLooper());

    private Runnable mTimer = new Runnable() {
        @Override
        public void run() {
            if ((Integer) ivState.getTag() == RED_LIGHT_ON) {
                ivState.setTag(RED_LIGHT_OFF);
                ivState.setImageResource(R.drawable.red_light_no);
            } else {
                ivState.setTag(RED_LIGHT_ON);
                ivState.setImageResource(R.drawable.red_light);
            }
            mHandler.postDelayed(mTimer, 500);
        }
    };
    */
}
