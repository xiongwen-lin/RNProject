package com.afar.osaio.smart.lpipc.activity;

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
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.lpipc.contract.GatewayFirmwareContract;
import com.afar.osaio.smart.lpipc.presenter.GatewayFirmwarePresenter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.nooie.common.utils.log.NooieLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GatewayFirmwareActivity extends BaseActivity implements GatewayFirmwareContract.View {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvCurrentVersion)
    TextView tvCurrentVersion;
    @BindView(R.id.tvNewVersion)
    TextView tvNewVersion;

    private AlertDialog mUpgradeDialog;

    private GatewayFirmwareContract.Presenter mPresenter;
    private String mDeviceId;

    public static void toGatewayFirmwareActivity(Context from, String deviceId, String currentVersion, String newVersion, String log, String ptk, String md5, String model) {
        Intent intent = new Intent(from, GatewayFirmwareActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM, currentVersion);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM_1, newVersion);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM_2, log);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM_3, ptk);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM_4, md5);
        intent.putExtra(ConstantValue.INTENT_KEY_IPC_MODEL, model);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gateway_firmware);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        new GatewayFirmwarePresenter(this);
        if (getCurrentIntent() != null) {
            mDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
        }
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.gateway_firmware_title);
        if (getCurrentIntent() != null) {
            refreshVersion(getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DATA_PARAM), getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DATA_PARAM_1));
        }
    }

    private void refreshVersion(String currentVersion, String newVersion) {
        if (checkNull(tvCurrentVersion, tvNewVersion)) {
            return;
        }
        StringBuilder currentVersionSb = new StringBuilder();
        currentVersionSb.append(getString(R.string.gateway_firmware_current_version_label));
        currentVersionSb.append(currentVersion);
        tvCurrentVersion.setText(currentVersionSb.toString());
        StringBuilder newVersionSb = new StringBuilder();
        newVersionSb.append(getString(R.string.gateway_firmware_new_version_label));
        newVersionSb.append(newVersion);
        tvNewVersion.setText(newVersionSb.toString());
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
    }

    private void resumeData() {
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
        hideUpgradeDialog();
        releaseRes();
        release();
    }

    private void release() {
        tvCurrentVersion = null;
        tvNewVersion = null;
    }

    @OnClick({R.id.ivLeft, R.id.btnDone})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnDone:
                String log = getCurrentIntent() != null ? getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DATA_PARAM_2) : "";
                showUpgradeDialog(log);
                break;
        }
    }

    @Override
    public void setPresenter(@NonNull GatewayFirmwareContract.Presenter presenter) {
        mPresenter = presenter;
    }

    private void showUpgradeDialog(String log) {
        hideUpgradeDialog();
        StringBuilder upgradeContentSb = new StringBuilder();
        if (!TextUtils.isEmpty(log)) {
            upgradeContentSb.append(log);
            //upgradeContentSb.append("\n");
        }
        //upgradeContentSb.append(getString(R.string.gateway_firmware_confirm_upgrade_content));
        mUpgradeDialog = DialogUtils.showConfirmWithSubMsgDialog(this, getString(R.string.gateway_firmware_confirm_upgrade_title), upgradeContentSb.toString(), R.string.cancel, R.string.confirm_upper, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                if (mPresenter != null) {
                    showLoading();
                    mPresenter.checkDeviceUpdateStatus(mDeviceId, mUserAccount);
                }

            }

            @Override
            public void onClickLeft() {
            }
        });
    }

    private void hideUpgradeDialog() {
        if (mUpgradeDialog != null) {
            mUpgradeDialog.dismiss();
            mUpgradeDialog = null;
        }
    }

    @Override
    public void onCheckDeviceUpdateStatusResult(String result, int type) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            String model = getCurrentIntent() != null ? getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_IPC_MODEL) : "";
            String version = getCurrentIntent() != null ? getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DATA_PARAM_1) : "";
            String ptk = getCurrentIntent() != null ? getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DATA_PARAM_3) : "";
            String md5 = getCurrentIntent() != null ? getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DATA_PARAM_4) : "";
            if (TextUtils.isEmpty(model) || TextUtils.isEmpty(version) || TextUtils.isEmpty(ptk) || TextUtils.isEmpty(md5)) {
                hideLoading();
                return;
            }
            if (NooieDeviceHelper.isDeviceUpdating(type)) {
                hideLoading();
                GatewayUpgradeActivity.toGatewayUpgradeActivity(GatewayFirmwareActivity.this, mDeviceId, model, version, ptk, md5, true);
                finish();
            } else if (mPresenter != null) {
                mPresenter.startUpdateDevice(mUserAccount, mDeviceId, model, version, ptk, md5);
            }
        }
    }

    @Override
    public void onStartUpdateDeviceResult(String result) {
        hideLoading();
        String model = getCurrentIntent() != null ? getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_IPC_MODEL) : "";
        String version = getCurrentIntent() != null ? getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DATA_PARAM_1) : "";
        String ptk = getCurrentIntent() != null ? getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DATA_PARAM_3) : "";
        String md5 = getCurrentIntent() != null ? getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DATA_PARAM_4) : "";
        NooieLog.d("-->> GatewayFirmwareActivity onStartUpdateDeviceResult {r model=" + model + " version=" + version + " ptk=" + ptk + " md5=" + md5);
        if (TextUtils.isEmpty(model) || TextUtils.isEmpty(version) || TextUtils.isEmpty(ptk) || TextUtils.isEmpty(md5)) {
            hideLoading();
            return;
        }
        GatewayUpgradeActivity.toGatewayUpgradeActivity(GatewayFirmwareActivity.this, mDeviceId, model, version, ptk, md5, false);
        finish();
    }
}
