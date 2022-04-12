package com.afar.osaio.smart.setting.activity;

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
import com.afar.osaio.base.TplContract;
import com.afar.osaio.base.TplPresenter;
import com.afar.osaio.bean.CurrentDeviceParam;
import com.afar.osaio.bean.ShortLinkDeviceParam;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.smart.lpipc.activity.MatchLpCameraFailActivity;
import com.afar.osaio.smart.scan.helper.ApHelper;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.FButton;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.bean.IpcType;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SwitchConnectionModeActivity extends BaseActivity implements TplContract.View {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvSwitchConnectionModeTip_1)
    TextView tvSwitchConnectionModeTip_1;
    @BindView(R.id.tvSwitchConnectionModeTip_2)
    TextView tvSwitchConnectionModeTip_2;
    @BindView(R.id.ivSwitchConnectionModeIcon)
    ImageView ivSwitchConnectionModeIcon;
    @BindView(R.id.btnSwitchConnectionNext)
    FButton btnSwitchConnectionNext;

    private TplContract.Presenter mPresenter;

    public static void toSwitchConnectionModeActivity(Context from, Bundle param) {
        Intent intent = new Intent(from, SwitchConnectionModeActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_START_PARAM, param);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch_connection_mode);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        new TplPresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        ivRight.setImageResource(R.drawable.question_black_icon);
        tvTitle.setText(R.string.switch_connection_mode_title);
        setupSwitchConnectionView();
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
        registerShortLinkKeepListener();
    }

    private void resumeData() {
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
        releaseRes();
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        ivLeft = null;
        tvTitle = null;
    }

    @OnClick({R.id.ivLeft, R.id.ivRight, R.id.btnSwitchConnectionNext})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                setIsGotoOtherPage(true);
                finish();
                break;
            case R.id.ivRight:
                setIsGotoOtherPage(true);
                MatchLpCameraFailActivity.toMatchLpCameraFailActivity(this, MatchLpCameraFailActivity.FAIL_HELP_FOR_AP_CAMERA);
                break;
            case R.id.btnSwitchConnectionNext:
                trySwitchConnectionMode();
                break;
        }
    }

    @Override
    public void setPresenter(@NonNull TplContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public ShortLinkDeviceParam getShortLinkDeviceParam() {
        BindDevice device = NooieDeviceHelper.getDeviceById(getDeviceId());
        if (device == null) {
            return null;
        }
        String model = device.getType();
        boolean isSubDevice = NooieDeviceHelper.isSubDevice(device.getPuuid(), device.getType());
        ShortLinkDeviceParam shortLinkDeviceParam = new ShortLinkDeviceParam(mUid, getDeviceId(), model, isSubDevice, false, getConnectionMode());
        return shortLinkDeviceParam;
    }

    @Override
    public CurrentDeviceParam getCurrentDeviceParam() {
        if (TextUtils.isEmpty(getDeviceId())) {
            return null;
        }
        CurrentDeviceParam currentDeviceParam = null;
        if (getConnectionMode() == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            currentDeviceParam = new CurrentDeviceParam();
            currentDeviceParam.setDeviceId(getDeviceId());
            currentDeviceParam.setConnectionMode(getConnectionMode());
            currentDeviceParam.setModel(getDeviceModel());
        } else {
        }
        return currentDeviceParam;
    }

    private void setupSwitchConnectionView() {
        if (NooieDeviceHelper.mergeIpcType(getDeviceModel()) == IpcType.HC320) {
            ivRight.setVisibility(View.VISIBLE);
            String currentMode = getConnectionMode() == ConstantValue.CONNECTION_MODE_AP_DIRECT ? getString(R.string.connection_mode_dc_title_hc_320) : getString(R.string.connection_mode_qc_title_hc_320);
            tvSwitchConnectionModeTip_1.setText(String.format(getString(R.string.switch_connection_mode_tip_1_hc_320), currentMode));
            tvSwitchConnectionModeTip_2.setVisibility(View.GONE);
            ivSwitchConnectionModeIcon.setImageResource(R.drawable.device_reset_icon_lp_hc_320);
            btnSwitchConnectionNext.setVisibility(View.VISIBLE);
            btnSwitchConnectionNext.setText(R.string.switch_connection_mode_confirm_btn);
        } else {
            ivRight.setVisibility(View.GONE);
            tvSwitchConnectionModeTip_1.setText(R.string.switch_connection_mode_tip_1);
            tvSwitchConnectionModeTip_2.setVisibility(View.VISIBLE);
            ivSwitchConnectionModeIcon.setImageResource(R.drawable.switch_connection_mode_icon);
            btnSwitchConnectionNext.setVisibility(View.GONE);
        }
    }

    private void goNextPage() {
        /*
        if (getConnectionMode() != ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            AddCameraSelectActivity.toAddCameraSelectActivity(this);
        } else {
            if (IpcType.getIpcType(getDeviceModel()) == IpcType.HC320) {
                Bundle param = new Bundle();
                param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, getDeviceModel());
                param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_AP_DIRECT);
                ConnectBluetoothActivity.toConnectBluetoothActivity(this, param);
            } else {
                AddCameraSelectActivity.toAddCameraSelectActivity(this);
            }
        }
         */

        Bundle param = new Bundle();
        param.putInt(ConstantValue.INTENT_KEY_HOME_PAGE_ACTION, ConstantValue.HOME_PAGE_ACTION_SWITCH_CONNECTION_MODE);
        param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, getDeviceModel());
        HomeActivity.toHomeActivity(this, param);
        finish();
    }

    private void trySwitchConnectionMode() {
        if (getConnectionMode() != ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            goNextPage();
            return;
        }
        showLoading();
        ApHelper.getInstance().removeBleApDeviceConnection(mUserAccount, mUid, getDeviceId(), getDeviceModel(), false, new ApHelper.APDirectListener() {
            @Override
            public void onSwitchConnectionMode(boolean result, int connectionMode, String deviceId) {
                hideLoading();
                goNextPage();
            }
        });
    }

    private String getDeviceId() {
        if (getStartParam() == null) {
            return new String();
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_DEVICE_ID);
    }

    private String getDeviceModel() {
        if (getStartParam() == null) {
            return new String();
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_IPC_MODEL);
    }

    private int getConnectionMode() {
        if (getStartParam() == null) {
            return ConstantValue.CONNECTION_MODE_QC;
        }
        return getStartParam().getInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC);
    }

}
