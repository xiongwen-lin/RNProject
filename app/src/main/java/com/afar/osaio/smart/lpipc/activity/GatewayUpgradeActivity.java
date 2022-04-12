package com.afar.osaio.smart.lpipc.activity;

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
import com.afar.osaio.smart.lpipc.contract.GatewayUpgradeContract;
import com.afar.osaio.smart.lpipc.presenter.GatewayUpgradePresenter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.FButton;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.common.widget.ProgressWheel;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GatewayUpgradeActivity extends BaseActivity implements GatewayUpgradeContract.View {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.pbGatewayUpgrade)
    ProgressWheel pbGatewayUpgrade;
    @BindView(R.id.tvGatewayUpgradeTip)
    TextView tvGatewayUpgradeTip;
    @BindView(R.id.tvGatewayUpgradeRestartTip)
    TextView tvGatewayUpgradeRestartTip;
    @BindView(R.id.btnDone)
    FButton btnDone;

    private GatewayUpgradeContract.Presenter mPresenter;
    private String mDeviceId;

    public static void toGatewayUpgradeActivity(Context from, String deviceId, String model, String newVersion, String ptk, String md5, boolean isUpdating) {
        Intent intent = new Intent(from, GatewayUpgradeActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_IPC_MODEL, model);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM_1, newVersion);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM_2, ptk);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM_3, md5);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM, isUpdating);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gateway_upgrade);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        if (getCurrentIntent() !=  null) {
            mDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
        } else {
            finish();
            return;
        }
        new GatewayUpgradePresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.gateway_firmware_title);
        displayUpgradeView(ApiConstant.DEVICE_UPDATE_TYPE_NORMAL, 0);

        NooieLog.d("-->> GatewayUpgradeActivity {r current utc time=" + DateTimeUtil.getUtcCalendar().getTimeInMillis());
        if (mPresenter != null) {
            mPresenter.queryDeviceUpgradeTime(mDeviceId, mUserAccount, !getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_DATA_PARAM, true));
        }
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
            mPresenter.stopQueryDeviceUpdateState();
            mPresenter.stopUpdateProcessTask();
            mPresenter.destroy();
        }
        releaseRes();
        release();
    }

    private void release() {
        btnDone = null;
        tvGatewayUpgradeTip = null;
        tvGatewayUpgradeRestartTip = null;
    }

    private void displayUpgradeView(int upgradeType, int process) {
        if (upgradeType == ApiConstant.DEVICE_UPDATE_TYPE_UPATE_FINISH) {
            tvGatewayUpgradeTip.setVisibility(View.GONE);
            tvGatewayUpgradeRestartTip.setVisibility(View.INVISIBLE);
            btnDone.setVisibility(View.VISIBLE);
            btnDone.setTag(upgradeType);
            upgradeProgressBar(100);
        } else if (upgradeType == ApiConstant.DEVICE_UPDATE_TYPE_UPDATE_FAILED) {
            upgradeProgressBar(0);
            tvGatewayUpgradeTip.setVisibility(View.GONE);
            tvGatewayUpgradeRestartTip.setVisibility(View.VISIBLE);
            btnDone.setVisibility(View.VISIBLE);
            btnDone.setTag(upgradeType);
        } else {
            upgradeProgressBar(process);
            tvGatewayUpgradeTip.setVisibility(View.VISIBLE);
            tvGatewayUpgradeRestartTip.setVisibility(View.INVISIBLE);
            btnDone.setVisibility(View.GONE);
        }
    }

    @OnClick({R.id.ivLeft, R.id.btnDone})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnDone:
                if (btnDone != null && btnDone.getTag() != null) {
                    int upgradeType = (Integer) btnDone.getTag();
                    if (upgradeType == ApiConstant.DEVICE_UPDATE_TYPE_UPDATE_FAILED && mPresenter != null) {
                        String model = getCurrentIntent() != null ? getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_IPC_MODEL) : "";
                        String version = getCurrentIntent() != null ? getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DATA_PARAM_1) : "";
                        String ptk = getCurrentIntent() != null ? getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DATA_PARAM_2) : "";
                        String md5 = getCurrentIntent() != null ? getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DATA_PARAM_3) : "";
                        if (TextUtils.isEmpty(model) || TextUtils.isEmpty(version) || TextUtils.isEmpty(ptk) || TextUtils.isEmpty(md5)) {
                            hideLoading();
                            return;
                        }
                        showLoading();
                        mPresenter.startUpdateDevice(mUserAccount, mDeviceId, model, version, ptk, md5);
                    } else if (upgradeType == ApiConstant.DEVICE_UPDATE_TYPE_UPATE_FINISH) {
                        GatewayInfoActivity.toGatewayInfoActivity(this, mDeviceId);
                        finish();
                    }
                }
                break;
        }
    }

    @Override
    public void setPresenter(@NonNull GatewayUpgradeContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onQueryDeviceUpdateStatus(int type, int process) {
        if (isDestroyed()) {
            return;
        }

        if (type == ApiConstant.DEVICE_UPDATE_TYPE_UPATE_FINISH) {
            displayUpgradeView(type, 100);
        } else if (type == ApiConstant.DEVICE_UPDATE_TYPE_UPDATE_FAILED) {
            displayUpgradeView(type, 0);
        } else {
            displayUpgradeView(type, process);
        }
    }

    private void upgradeProgressBar(int progress) {
        if (checkNull(pbGatewayUpgrade)) {
            return;
        }
        double progressValue = 360.0 * (progress / 100.0);
        pbGatewayUpgrade.setProgress((int)progressValue);
        StringBuilder progressSb = new StringBuilder();
        progressSb.append(progress);
        progressSb.append("%");
        pbGatewayUpgrade.setText(progressSb.toString());
    }

    @Override
    public void onStartUpdateDeviceResult(String result) {
        hideLoading();
    }
}
