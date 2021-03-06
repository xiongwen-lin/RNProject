package com.afar.osaio.smart.setting.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.bean.NooieDevice;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.setting.contract.DeviceTestToolContract;
import com.afar.osaio.smart.setting.presenter.DeviceTestToolPresenter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.FButton;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.AppVersionResult;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.device.DeviceCmdService;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DeviceTestToolActivity extends BaseActivity implements DeviceTestToolContract.View {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.btnTestAutoUpgrade)
    FButton btnTestAutoUpgrade;
    @BindView(R.id.tvTestAutoUpgradeState)
    TextView tvTestAutoUpgradeState;
    @BindView(R.id.tvTestAutoUpgradeStateTip)
    TextView tvTestAutoUpgradeStateTip;
    @BindView(R.id.tvTestDeviceInfo)
    TextView tvTestDeviceInfo;
    @BindView(R.id.etUpgradeModel)
    EditText etUpgradeModel;
    @BindView(R.id.etUpgradeVersion)
    EditText etUpgradeVersion;
    @BindView(R.id.etUpgradePtk)
    EditText etUpgradePtk;
    @BindView(R.id.etUpgradeMd5)
    EditText etUpgradeMd5;

    private DeviceTestToolContract.Presenter mPresenter;
    private String mDeviceId;
    private String mPDeviceId;

    public static void toDeviceTestToolActivity(Context from, String deviceId, String pDeviceId) {
        Intent intent = new Intent(from, DeviceTestToolActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM, pDeviceId);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_test_tool);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            return;
        }
        mDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
        mPDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DATA_PARAM);
        new DeviceTestToolPresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText("??????????????????");
        setupAutoUpgradeTestView();
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
        keepScreenLongLight();
    }

    private void resumeData() {
    }

    @Override
    public void onPause() {
        super.onPause();
        clearKeepScreenLongLight();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.stopAutoUpgradeTest();
            mPresenter.stopQueryDeviceUpgradeStatus();
            mPresenter.destroy();
        }
        releaseRes();
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        ivLeft = null;
        tvTitle = null;
    }

    private void setupAutoUpgradeTestView() {
        if (mPresenter != null) {
            mPresenter.loadDeviceInfo(mDeviceId);
        }
        tvTestAutoUpgradeState.setTag(ApiConstant.DEVICE_UPDATE_TYPE_UPDATABLE);
        btnTestAutoUpgrade.setText("????????????????????????????????????????????????");
        btnTestAutoUpgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvTestAutoUpgradeState != null && tvTestAutoUpgradeState.getTag() != null && NooieDeviceHelper.isDeviceUpdating((Integer)tvTestAutoUpgradeState.getTag())) {
                    ToastUtil.showToast(DeviceTestToolActivity.this, "?????????????????????");
                    return;
                }
                if (mPresenter != null) {
                    showLoading();
                    mPresenter.startAutoUpgradeTest(mDeviceId);
                    mPresenter.startQueryDeviceUpgradeStatus(mDeviceId);
                }
            }
        });

        btnTestAutoUpgrade.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ToastUtil.showToast(DeviceTestToolActivity.this, "????????????");
                if (mPresenter != null) {
                    mPresenter.stopQueryDeviceUpgradeStatus();
                    mPresenter.stopAutoUpgradeTest();
                }
                tvTestAutoUpgradeState.setTag(ApiConstant.DEVICE_UPDATE_TYPE_UPDATABLE);
                btnTestAutoUpgrade.setText("????????????????????????????????????????????????");
                return false;
            }
        });
    }

    @OnClick({R.id.ivLeft, R.id.btnTestCamDownUpgrade, R.id.btnTestGatewayDownUpgrade})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnTestCamDownUpgrade: {
                if (TextUtils.isEmpty(mDeviceId)) {
                    ToastUtil.showToast(this, "Cam??????Id??????");
                    return;
                }

                String model = etUpgradeModel.getText().toString();
                String version = etUpgradeVersion.getText().toString();
                String pkt = etUpgradePtk.getText().toString();
                String md5 = etUpgradeMd5.getText().toString();

                if (TextUtils.isEmpty(model) || TextUtils.isEmpty(version) || TextUtils.isEmpty(pkt)) {
                    ToastUtil.showToast(this, "??????????????????model???version???pkt??????");
                    return;
                }

                showLoading();
                DeviceCmdApi.getInstance().upgrade(mDeviceId, model, version, pkt, md5, new OnActionResultListener() {
                    @Override
                    public void onResult(int code) {
                        if (isDestroyed()) {
                            return;
                        }
                        hideLoading();
                        if (code == 0) {
                            ToastUtil.showLongToast(DeviceTestToolActivity.this, "?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????");
                        } else {
                            ToastUtil.showToast(DeviceTestToolActivity.this, "???????????????????????????????????????????????????????????????????????????");
                        }
                    }
                });
                if (NooieDeviceHelper.isSubDevice(mPDeviceId, model)) {
                    /*
                    DeviceCmdService.getInstance(NooieApplication.mCtx).camUpgrade(mDeviceId, model, version, pkt, md5, new OnActionResultListener() {
                        @Override
                        public void onResult(int code) {
                            if (isDestroyed()) {
                                return;
                            }
                            hideLoading();
                            if (code == 0) {
                                ToastUtil.showLongToast(DeviceTestToolActivity.this, "EC810-Cam?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????");
                            } else {
                                ToastUtil.showToast(DeviceTestToolActivity.this, "???????????????????????????????????????????????????????????????????????????");
                            }
                        }
                    });

                     */
                } else {
                    /*
                    DeviceCmdService.getInstance(NooieApplication.mCtx).upgrade(mDeviceId, model, version, pkt, new OnActionResultListener() {
                        @Override
                        public void onResult(int code) {
                            if (isDestroyed()) {
                                return;
                            }
                            hideLoading();
                            if (code == 0) {
                                ToastUtil.showLongToast(DeviceTestToolActivity.this, "Cam?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????");
                            } else {
                                ToastUtil.showToast(DeviceTestToolActivity.this, "???????????????????????????????????????????????????????????????????????????");
                            }
                        }
                    });

                     */
                }
                break;
            }
            case R.id.btnTestGatewayDownUpgrade: {
                if (TextUtils.isEmpty(mPDeviceId)) {
                    ToastUtil.showToast(this, "Hub??????Id??????");
                    return;
                }
                String model = "EC810-HUB";
//                String version = "3.1.52";
//                String pkt = "a825f17220d345bdb348548ebf38addd.bin";
//                String md5 = "cff194564c86b65bf4e7713fbf6ee91c";

//                String version = "3.1.66";
//                String pkt = "1d8dd3d4ac1e49fbc9217ec894f3a3e9.bin";
//                String md5 = "b648f804562ce117d11f877979b54c9e";

                String version = "3.1.66";
                String pkt = "1d8dd3d4ac1e49fbc9217ec894f3a3e9.bin";
                String md5 = "b648f804562ce117d11f877979b54c9e";
                showLoading();
                DeviceCmdService.getInstance(NooieApplication.mCtx).hubUpgrade(mPDeviceId, model, version, pkt, md5, new OnActionResultListener() {
                    @Override
                    public void onResult(int code) {
                        if (isDestroyed()) {
                            return;
                        }
                        hideLoading();
                        if (code == 0) {
                            ToastUtil.showLongToast(DeviceTestToolActivity.this, "EC810-Hub?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????");
                        } else {
                            ToastUtil.showToast(DeviceTestToolActivity.this, "???????????????????????????????????????????????????????????????????????????");
                        }
                    }
                });
                break;
            }
        }
    }

    @Override
    public void setPresenter(@NonNull DeviceTestToolContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onLoadDeviceInfo(String result, NooieDevice deviceInfo) {
        if (isDestroyed() || checkNull(tvTestDeviceInfo)) {
            return;
        }
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            BindDevice device = deviceInfo.getDevice();
            if (device == null) {
                return;
            }
            AppVersionResult appVersionResult = deviceInfo.getAppVersionResult();
            StringBuilder deviceInfoSb = new StringBuilder();
            deviceInfoSb.append("??????Id???");
            deviceInfoSb.append(device.getUuid());
            deviceInfoSb.append("\n");
            deviceInfoSb.append("?????????");
            deviceInfoSb.append(device.getName());
            deviceInfoSb.append("\n");
            deviceInfoSb.append("?????????");
            deviceInfoSb.append(device.getType());
            deviceInfoSb.append("\n");
            deviceInfoSb.append("???????????????");
            deviceInfoSb.append(device.getVersion());
            deviceInfoSb.append("\n");
            if (appVersionResult != null) {
                deviceInfoSb.append("???????????????");
                deviceInfoSb.append(appVersionResult.getVersion_code());
                deviceInfoSb.append("\n");
            }
            deviceInfoSb.append("?????????");
            deviceInfoSb.append(device.getOnline() == ApiConstant.ONLINE_STATUS_ON ? "??????" : "??????");
            deviceInfoSb.append("\n");
            tvTestDeviceInfo.setText(deviceInfoSb.toString());
        }
    }

    @Override
    public void onQueryDeviceUpgradeStatus(String result, int type) {
        if (isDestroyed() || checkNull(tvTestAutoUpgradeState)) {
            return;
        }
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            tvTestAutoUpgradeState.setTag(type);
            StringBuilder upgradeStateSb = new StringBuilder();
            upgradeStateSb.append(String.format("???????????????????????????%1$s", String.valueOf(type)));
            upgradeStateSb.append("\n");
            if (mPresenter != null && mPresenter.getDeviceTestResult() != null) {
                upgradeStateSb.append(mPresenter.getDeviceTestResult().log());
            }
            tvTestAutoUpgradeState.setText(upgradeStateSb.toString());
        }

    }

    @Override
    public void onStartAutoUpgradeTest(String result, int state) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            switch (state) {
                case 1: {
                    ToastUtil.showToast(this, "????????????????????????");
                    break;
                }
                case 2: {
                    ToastUtil.showToast(this, "???????????????????????????????????????");
                    break;
                }
                case 3: {
                    ToastUtil.showToast(this, "???????????????????????????????????????");
                    break;
                }
                case 4: {
                    ToastUtil.showToast(this, "???????????????????????????????????????????????????????????????");
                    break;
                }
                case 5: {
                    ToastUtil.showToast(this, "?????????????????????????????????");
                    break;
                }
                case 6: {
                    ToastUtil.showToast(this, "?????????????????????");
                    break;
                }
            }
        }
    }
}
