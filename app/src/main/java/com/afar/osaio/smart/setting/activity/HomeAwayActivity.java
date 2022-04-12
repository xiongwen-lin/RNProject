package com.afar.osaio.smart.setting.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.bean.ShortLinkDeviceParam;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.nooie.sdk.api.network.base.bean.constant.ApiConstant;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.api.network.base.bean.entity.DeviceStatusResult;
import com.suke.widget.SwitchButton;
import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.setting.presenter.HomeAwayPresenter;
import com.afar.osaio.smart.setting.view.HomeAwayContract;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.util.Util;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeAwayActivity extends BaseActivity implements HomeAwayContract.View {

    public static void toHomeAwayActivity(Context from, String deviceId, int connectionMode, String deviceSsid) {
        Intent intent = new Intent(from, HomeAwayActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, connectionMode);
        intent.putExtra(ConstantValue.INTENT_KEY_SSID, deviceSsid);
        from.startActivity(intent);
    }

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.tvHomeAwayTip)
    TextView tvHomeAwayTip;
    @BindView(R.id.sbHomeAwaySwitch)
    SwitchButton sbHomeAwaySwitch;

    private HomeAwayContract.Presenter mPresenter;
    private String mDeviceId;
    private int mConnectionMode = ConstantValue.CONNECTION_MODE_QC;
    private String mDeviceSsid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_away);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            return;
        } else {
            mDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
            mConnectionMode = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC);
            mDeviceSsid = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_SSID);
            new HomeAwayPresenter(this);
        }
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.camera_settings_home_away);
        ivRight.setVisibility(View.GONE);
        sbHomeAwaySwitch.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (mPresenter != null) {
                    showLoading();
                    if (mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
                        mPresenter.updateApDeviceOpenStatus(mDeviceId, mDeviceSsid, !isChecked);
                    } else {
                        mPresenter.updateDeviceOpenStatus(mDeviceId, isChecked ? ApiConstant.OPEN_STATUS_ON : ApiConstant.OPEN_STATUS_OFF);
                    }
                }
            }
        });

        if (mPresenter != null) {
            if (mConnectionMode == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
                mPresenter.getAPDeviceOpenStatus(mDeviceId);
            } else {
                mPresenter.getDeviceOpenStatus(mDeviceId);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerShortLinkKeepListener();
    }

    public void resumeData() {
    }

    @Override
    public void onPause() {
        super.onPause();
        unRegisterShortLinkKeepListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.destroy();
        }
        release();
    }

    private void release() {
        ivLeft = null;
        tvTitle = null;
        ivRight = null;
        tvHomeAwayTip = null;
        if (sbHomeAwaySwitch != null) {
            sbHomeAwaySwitch.setOnCheckedChangeListener(null);
            sbHomeAwaySwitch = null;
        }
    }

    @OnClick({R.id.ivLeft, R.id.ivRight})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                setIsGotoOtherPage(true);
                finish();
                break;
        }
    }

    @Override
    public void setPresenter(HomeAwayContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void notifyUpdateDeviceOpenStatusResult(String result, String deviceId, int status) {
        if (isDestroyed() || TextUtils.isEmpty(deviceId)) {
            return;
        }

        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            NooieDeviceHelper.updateDeviceOpenStatus(deviceId, status);
            Util.delayTask(3000, new Util.OnDelayTaskFinishListener() {
                @Override
                public void onFinish() {
                    sendUpdateCameraBroadcast();
                }
            });
        } else if (mPresenter != null) {
            mPresenter.getDeviceOpenStatus(deviceId);
            ToastUtil.showToast(this, R.string.camera_setting_warn_msg_set_sleep_fail);
        }
        hideLoading();
    }

    @Override
    public void onUpdateDeviceOpenStatus(String result, String deviceId, String deviceSsid, boolean sleep) {
        if (isDestroyed()) {
            return;
        }

        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            //ApHelper.getInstance().updateOpenStatusInApDeviceCache(deviceSsid, (sleep ? ApiConstant.OPEN_STATUS_OFF : ApiConstant.OPEN_STATUS_ON));
            ApHelper.getInstance().updateCurrentApDeviceInfoOfOpenStatus(deviceId, (sleep ? ApiConstant.OPEN_STATUS_OFF : ApiConstant.OPEN_STATUS_ON));
            Util.delayTask(3000, new Util.OnDelayTaskFinishListener() {
                @Override
                public void onFinish() {
                    sendUpdateCameraBroadcast();
                }
            });
        } else {
            ToastUtil.showToast(this, R.string.camera_setting_warn_msg_set_sleep_fail);
        }
        hideLoading();
    }

    @Override
    public void notifyGetDeviceOpenStatusSuccess(String deviceId, DeviceStatusResult result) {
        if (isDestroyed() || TextUtils.isEmpty(deviceId) || checkNull(sbHomeAwaySwitch)) {
            return;
        }

        boolean isOpen = result != null && result.getOpen_status() == ApiConstant.OPEN_STATUS_ON;
        NooieDeviceHelper.updateDeviceOpenStatus(deviceId, isOpen ? ApiConstant.OPEN_STATUS_ON : ApiConstant.OPEN_STATUS_OFF);
        if (sbHomeAwaySwitch != null && sbHomeAwaySwitch.isChecked() != isOpen) {
            sbHomeAwaySwitch.toggleNoCallback();
        }
    }

    @Override
    public void notifyGetDeviceOpenStatusFailed(String deviceId, String msg) {
        if (isPause() && !TextUtils.isEmpty(deviceId)) {
            return;
        }
    }

    @Override
    public void onGetApDeviceOpenStatus(String result, int openStatus) {
        if (isDestroyed() || checkNull(sbHomeAwaySwitch)) {
            return;
        }
        boolean isOpen = openStatus == ApiConstant.OPEN_STATUS_ON;
        if (sbHomeAwaySwitch != null && sbHomeAwaySwitch.isChecked() != isOpen) {
            sbHomeAwaySwitch.toggleNoCallback();
        }
    }

    @Override
    public boolean checkIsAddDeviceApHelperListener() {
        return true;
    }

    @Override
    public ShortLinkDeviceParam getShortLinkDeviceParam() {
        BindDevice device = NooieDeviceHelper.getDeviceById(mDeviceId);
        if (device == null) {
            return null;
        }
        String model = device.getType();
        boolean isSubDevice = NooieDeviceHelper.isSubDevice(device.getPuuid(), device.getType());
        ShortLinkDeviceParam shortLinkDeviceParam = new ShortLinkDeviceParam(mUid, mDeviceId, model, isSubDevice, false, mConnectionMode);
        return shortLinkDeviceParam;
    }
}
