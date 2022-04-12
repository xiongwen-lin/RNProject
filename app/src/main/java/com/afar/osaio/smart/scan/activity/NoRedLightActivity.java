package com.afar.osaio.smart.scan.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.nooie.sdk.bean.IpcType;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.helper.ResHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

public class NoRedLightActivity extends BaseActivity {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.ivBlueLight)
    ImageView ivBlueLight;
    @BindView(R.id.ivReset)
    ImageView ivReset;
    @BindView(R.id.textView4)
    TextView textView4;

    private IpcType mDeviceType;

    public static void toNoRedLightActivity(Context from, String model, int connectionMode) {
        Intent intent = new Intent(from, NoRedLightActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_IPC_MODEL, model);
        intent.putExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, connectionMode);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_red_light);
        ButterKnife.bind(this);
        mDeviceType = IpcType.getIpcType(getDeviceModel());
        setupView();
    }

    private void setupView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.add_camera_no_red_light);
        ivRight.setVisibility(View.INVISIBLE);
        textView4.setText(R.string.add_camera_360_no_red_light_step_1);
        /*
        ivBlueLight.setImageResource(R.drawable.red_light);
        ivReset.setImageResource(R.drawable.reset_cam720);
        if (NooieDeviceHelper.mergeIpcType(mDeviceType) == IpcType.PC530) {
            ivBlueLight.setImageResource(R.drawable.light_cam360);
            ivReset.setImageResource(R.drawable.reset_cam360);
        } else if (NooieDeviceHelper.mergeIpcType(mDeviceType) == IpcType.PC730) {
            ivBlueLight.setImageResource(R.drawable.light_cam_outdoor);
            ivReset.setImageResource(R.drawable.reset_cam_outdoor);
        } else {
            ivBlueLight.setImageResource(R.drawable.red_light);
            ivReset.setImageResource(R.drawable.reset_cam720);
        }
        */
        ivBlueLight.setImageResource(ResHelper.getInstance().getDeviceLightIconByType(mDeviceType != null ? mDeviceType.getType() : ""));
        ivReset.setImageResource(ResHelper.getInstance().getDeviceResetIconByType(mDeviceType != null ? mDeviceType.getType() : ""));
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
        ivBlueLight = null;
        ivReset = null;
        textView4 = null;
    }

    @OnClick({R.id.ivLeft, R.id.btnResetSuccess, R.id.btnNext})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnNext:
                gotoSetupWifi();
                break;
            case R.id.btnResetSuccess:
                finish();
                break;
        }
    }

    @Override
    protected void permissionsGranted(int requestCode) {
        super.permissionsGranted();
        if (requestCode == ConstantValue.REQUEST_CODE_FOR_CAMERA) {
            ScanCodeActivity.toScanCodeActivity(this, (mDeviceType != null ? mDeviceType.getType() : ""));
        }
        /*
        if (NetworkUtil.isWifiEnabled(NooieApplication.mCtx)) {
            // connected wifi
            if (getConnectionMode() == ConstantValue.CONNECTION_MODE_LAN) {
                finish();
                return;
            }
            InputWiFiPsdActivity.toInputWiFiPsdActivity(this, true, mDeviceType.getType(), getConnectionMode());
        } else {
            // don't connected wifi
            NoConnectWiFi.toNoConnectWiFi(this);
        }
         */
    }

    private void gotoSetupWifi() {
        /*
        if (NetworkUtil.isConnected(NooieApplication.mCtx)) {
            if (getConnectionMode() == ConstantValue.CONNECTION_MODE_LAN) {
                finish();
                return;
            }
            InputWiFiPsdActivity.toInputWiFiPsdActivity(this, true, mDeviceType.getType(), getConnectionMode());
        } else {
            requestPermission(ConstantValue.PERM_GROUP_LOCATION);
            //NoConnectWiFi.toNoConnectWiFi(this);
        }
        */
        if (getConnectionMode() == ConstantValue.CONNECTION_MODE_LAN) {
            checkPerm();
            return;
        }
        Bundle param = new Bundle();
        param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, getDeviceModel());
        param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, getConnectionMode());
        InputWiFiPsdActivity.toInputWiFiPsdActivity(this, param);
    }

    private void checkPerm() {
        if (EasyPermissions.hasPermissions(this, ConstantValue.PERM_GROUP_CAMERA)) {
            ScanCodeActivity.toScanCodeActivity(this, (mDeviceType != null ? mDeviceType.getType() : ""));
        } else {
            requestPermission(ConstantValue.PERM_GROUP_CAMERA, ConstantValue.REQUEST_CODE_FOR_CAMERA);
        }
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
}
