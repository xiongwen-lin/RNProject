package com.afar.osaio.smart.setting.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.CurrentDeviceParam;
import com.afar.osaio.bean.ShortLinkDeviceParam;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.setting.contract.FileSettingContract;
import com.afar.osaio.smart.setting.presenter.FileSettingPresenter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.LabelTextItemView;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.device.bean.NooieMediaMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FileSettingActivity extends BaseActivity implements FileSettingContract.View {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.livFileSettingMode)
    LabelTextItemView livFileSettingMode;
    @BindView(R.id.livFileSettingSnapNumber)
    LabelTextItemView livFileSettingSnapNumber;
    @BindView(R.id.livFileSettingRecordTime)
    LabelTextItemView livFileSettingRecordTime;

    private FileSettingContract.Presenter mPresenter;
    private NooieMediaMode mMediaMode = null;

    public static void toFileSettingActivity(Context from, Bundle param) {
        Intent intent = new Intent(from, FileSettingActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM, param);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_setting);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        new FileSettingPresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.file_setting_title);
        livFileSettingMode.displayLabelRight_1(View.VISIBLE).setLabelTitle(getString(R.string.file_setting_mode));
        livFileSettingSnapNumber.displayLabelRight_1(View.VISIBLE).setLabelTitle(getString(R.string.file_setting_snap_number));
        livFileSettingRecordTime.displayLabelRight_1(View.VISIBLE).setLabelTitle(getString(R.string.file_setting_record_time));
        displayFileSettingView(-1);
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
        registerShortLinkKeepListener();
    }

    private void resumeData() {
        if (mPresenter != null) {
            showLoading();
            mPresenter.getFileSettingMode(getDeviceId());
        }
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

    @OnClick({R.id.ivLeft, R.id.livFileSettingMode, R.id.livFileSettingSnapNumber, R.id.livFileSettingRecordTime})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.livFileSettingMode: {
                FileSettingConfigureActivity.toFileSettingConfigureActivity(this, createConfigureParam(ConstantValue.TYPE_FILE_SETTING_CONFIGURE_MODE, mMediaMode));
                setIsGotoOtherPage(true);
                break;
            }
            case R.id.livFileSettingSnapNumber: {
                FileSettingConfigureActivity.toFileSettingConfigureActivity(this, createConfigureParam(ConstantValue.TYPE_FILE_SETTING_CONFIGURE_SNAP_NUMBER, mMediaMode));
                setIsGotoOtherPage(true);
                break;
            }
            case R.id.livFileSettingRecordTime: {
                FileSettingConfigureActivity.toFileSettingConfigureActivity(this, createConfigureParam(ConstantValue.TYPE_FILE_SETTING_CONFIGURE_RECORDING_TIME, mMediaMode));
                setIsGotoOtherPage(true);
                break;
            }
        }
    }

    @Override
    public void setPresenter(@NonNull FileSettingContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onGetFileSettingMode(int state, NooieMediaMode mode) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
        if (state == SDKConstant.SUCCESS) {
            mMediaMode = mode;
            refreshFileSettingView(mode);
        }
    }

    @Override
    public ShortLinkDeviceParam getShortLinkDeviceParam() {
        BindDevice device = NooieDeviceHelper.getDeviceById(getDeviceId());
        if (device == null) {
            return null;
        }
        String model = device.getType();
        boolean isSubDevice = NooieDeviceHelper.isSubDevice(device.getPuuid(), device.getType());
        ShortLinkDeviceParam shortLinkDeviceParam = new ShortLinkDeviceParam(mUserAccount, getDeviceId(), model, isSubDevice, false, getConnectionMode());
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

    private void refreshFileSettingView(NooieMediaMode mode) {
        if (isDestroyed() || checkNull(livFileSettingMode, livFileSettingSnapNumber, livFileSettingRecordTime, mode)) {
            return;
        }
        livFileSettingMode.setLabelRight_1(NooieDeviceHelper.getFileSettingModeText(NooieApplication.mCtx, mode.mode));
        livFileSettingSnapNumber.setLabelRight_1(String.valueOf(mode.picNum));
        livFileSettingRecordTime.setLabelRight_1(new StringBuilder().append(mode.vidDur).append("s").toString());
        displayFileSettingView(mode.mode);
    }

    private void displayFileSettingView(int mode) {
        boolean isShowSnapNumber = mode == ConstantValue.DEVICE_MEDIA_MODE_VIDEO_IMAGE || mode == ConstantValue.DEVICE_MEDIA_MODE_IMAGE;
        boolean isShowRecordTime = mode == ConstantValue.DEVICE_MEDIA_MODE_VIDEO_IMAGE || mode == ConstantValue.DEVICE_MEDIA_MODE_VIDEO;
        livFileSettingSnapNumber.setVisibility(isShowSnapNumber ? View.VISIBLE : View.GONE);
        livFileSettingRecordTime.setVisibility(isShowRecordTime ? View.VISIBLE : View.GONE);
    }

    private Bundle createConfigureParam(int configureMode, NooieMediaMode mode) {
        Bundle param = new Bundle();
        param.putString(ConstantValue.INTENT_KEY_DEVICE_ID, getDeviceId());
        param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, getDeviceModel());
        param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, getConnectionMode());
        param.putInt(ConstantValue.INTENT_KEY_DATA_PARAM_1, configureMode);
        if (configureMode == ConstantValue.TYPE_FILE_SETTING_CONFIGURE_MODE) {
            param.putInt(ConstantValue.INTENT_KEY_DATA_PARAM_2, (mode != null ? mode.mode : 1));
        } else if (configureMode == ConstantValue.TYPE_FILE_SETTING_CONFIGURE_SNAP_NUMBER) {
            param.putInt(ConstantValue.INTENT_KEY_DATA_PARAM_3, (mode != null ? mode.picNum : 5));
        } else if (configureMode == ConstantValue.TYPE_FILE_SETTING_CONFIGURE_RECORDING_TIME) {
            param.putInt(ConstantValue.INTENT_KEY_DATA_PARAM_4, (mode != null ? mode.vidDur : 10));
        }
        return param;
    }

    private Bundle getParam() {
        if (getCurrentIntent() == null) {
            return null;
        }
        return getCurrentIntent().getBundleExtra(ConstantValue.INTENT_KEY_DATA_PARAM);
    }

    private String getDeviceModel() {
        if (getParam() == null) {
            return new String();
        }
        return getParam().getString(ConstantValue.INTENT_KEY_IPC_MODEL);
    }

    private String getDeviceId() {
        if (getParam() == null) {
            return new String();
        }
        return getParam().getString(ConstantValue.INTENT_KEY_DEVICE_ID);
    }

    private int getConnectionMode() {
        if (getCurrentIntent() == null) {
            return ConstantValue.CONNECTION_MODE_QC;
        }
        return getParam().getInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC);
    }
}
