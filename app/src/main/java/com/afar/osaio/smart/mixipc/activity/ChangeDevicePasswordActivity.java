package com.afar.osaio.smart.mixipc.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.bean.CurrentDeviceParam;
import com.afar.osaio.smart.mixipc.contract.ChangeDevicePasswordContract;
import com.afar.osaio.smart.mixipc.presenter.ChangeDevicePasswordPresenter;
import com.afar.osaio.util.CommonUtil;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.FButton;
import com.afar.osaio.widget.InputFrameView;
import com.nooie.sdk.bean.SDKConstant;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChangeDevicePasswordActivity extends BaseActivity implements ChangeDevicePasswordContract.View {

    private static final int PASSWORD_LIMIT_DOWN = 8;
    private static final int PASSWORD_LIMIT_UP = 16;

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ipvChangeDevicePasswordOldPw)
    InputFrameView ipvChangeDevicePasswordOldPw;
    @BindView(R.id.ipvChangeDevicePasswordNewPw)
    InputFrameView ipvChangeDevicePasswordNewPw;
    @BindView(R.id.ipvChangeDevicePasswordConfirmPw)
    InputFrameView ipvChangeDevicePasswordConfirmPw;
    @BindView(R.id.btnChangeDevicePasswordConfirm)
    FButton btnChangeDevicePasswordConfirm;

    private ChangeDevicePasswordContract.Presenter mPresenter;
    private Dialog mShowModifyPwSuccessDialog = null;

    public static void toModifyCameraPasswordActivity(Context from, Bundle param) {
        Intent intent = new Intent(from, ChangeDevicePasswordActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_START_PARAM, param);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_device_password);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        new ChangeDevicePasswordPresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.modify_camera_password_title);
        setInputView();
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
        registerInputListener();
        checkIsNeedToRequestLayout();
    }

    private void resumeData() {
    }

    @Override
    public void onPause() {
        super.onPause();
        unRegisterInputListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.destroy();
        }
        hideLoading();
        hideModifyPwSuccessDialog();
        releaseRes();
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        ivLeft = null;
        tvTitle = null;
    }

    @OnClick({R.id.ivLeft, R.id.btnChangeDevicePasswordConfirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnChangeDevicePasswordConfirm:
                confirmModifyPw();
                break;
        }
    }

    @Override
    public void setPresenter(@NonNull ChangeDevicePasswordContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onSetDeviceHotSpot(int state, int resultCode) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
        if (state == SDKConstant.SUCCESS) {
            showModifyPwSuccessDialog();
        } else if (resultCode == ConstantValue.CHANGE_BLE_AP_DEVICE_PASSWORD_RESULT_OLD_PW_ERROR){
            ToastUtil.showToast(this, getString(R.string.change_device_password_old_password_error));
        } else {
            ToastUtil.showToast(this, getString(R.string.bluetooth_scan_operation_tip_setting_fail));
        }
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

    private void setInputView() {
        ipvChangeDevicePasswordOldPw.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT).setInputTitle(getString(R.string.account_old_password))
                .setEtInputToggle(true)
                .setInputBtn(R.drawable.eye_open_icon_state_list)
                .setEtPwInputType(InputType.TYPE_CLASS_TEXT)
                .setEtInputType(InputFrameView.getPwInputType(InputType.TYPE_CLASS_TEXT, false))
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                    }

                    @Override
                    public void onEditorAction() {
                        hideInputMethod();
                    }

                    @Override
                    public void onEtInputClick() {
                    }
                })
                .setInputTextChangeListener(new TextWatcher() {
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

        ipvChangeDevicePasswordNewPw.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT).setInputTitle(getString(R.string.account_new_password))
                .setEtInputToggle(true)
                .setInputBtn(R.drawable.eye_open_icon_state_list)
                .setEtPwInputType(InputType.TYPE_CLASS_TEXT)
                .setEtInputType(InputFrameView.getPwInputType(InputType.TYPE_CLASS_TEXT, false))
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                    }

                    @Override
                    public void onEditorAction() {
                        hideInputMethod();
                    }

                    @Override
                    public void onEtInputClick() {
                    }
                })
                .setInputTextChangeListener(new TextWatcher() {
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

        ipvChangeDevicePasswordConfirmPw.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT).setInputTitle(getString(R.string.account_confirm_password))
                .setEtInputToggle(true)
                .setInputBtn(R.drawable.eye_open_icon_state_list)
                .setEtPwInputType(InputType.TYPE_CLASS_TEXT)
                .setEtInputType(InputFrameView.getPwInputType(InputType.TYPE_CLASS_TEXT, false))
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                    }

                    @Override
                    public void onEditorAction() {
                        hideInputMethod();
                    }

                    @Override
                    public void onEtInputClick() {
                    }
                })
                .setInputTextChangeListener(new TextWatcher() {
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

        checkBtnEnable();
    }

    private void checkBtnEnable() {
        if (!TextUtils.isEmpty(ipvChangeDevicePasswordOldPw.getInputText()) && !TextUtils.isEmpty(ipvChangeDevicePasswordNewPw.getInputText()) && !TextUtils.isEmpty(ipvChangeDevicePasswordConfirmPw.getInputText())) {
            btnChangeDevicePasswordConfirm.setEnabled(true);
        } else {
            btnChangeDevicePasswordConfirm.setEnabled(false);
        }
    }

    private void confirmModifyPw() {
        if (isDestroyed() || checkNull(ipvChangeDevicePasswordOldPw, ipvChangeDevicePasswordNewPw, ipvChangeDevicePasswordConfirmPw)) {
            return;
        }
        String oldPw = ipvChangeDevicePasswordOldPw.getInputText();
        String newPw = ipvChangeDevicePasswordNewPw.getInputText();
        String confirmPw = ipvChangeDevicePasswordConfirmPw.getInputText();
        if (!checkPwValid(oldPw) || !checkPwValid(newPw) || !checkPwValid(confirmPw)) {
            ToastUtil.showToast(this, R.string.modify_camera_password_invalid);
        } else if (oldPw.equals(newPw)) {
            ToastUtil.showToast(this, R.string.account_change_psd_old_new_same);
        } else if (!newPw.equals(confirmPw)) {
            ToastUtil.showToast(this, R.string.modify_camera_password_different);
        } else if (mPresenter != null) {
            showLoading();
            mPresenter.checkDeviceHotSpotPw(getDeviceId(), getSsid(), confirmPw, oldPw);
        }
    }

    private boolean checkPwValid(String password) {
        return !TextUtils.isEmpty(password) && password.length() >= PASSWORD_LIMIT_DOWN && password.length() <= PASSWORD_LIMIT_UP && !CommonUtil.checkPasswordIllegalChar(password);
    }

    private void showModifyPwSuccessDialog() {
        hideModifyPwSuccessDialog();
        mShowModifyPwSuccessDialog = DialogUtils.showInformationDialog(this, getString(R.string.change_device_password_reconnect_device_title), getString(R.string.change_device_password_reconnect_device_content), getString(R.string.change_device_password_reconnect_device_confirm_btn), false, false, new DialogUtils.OnClickInformationDialogLisenter() {
            @Override
            public void onConfirmClick() {
                gotoReconnectDevice();
            }
        });
    }

    private void hideModifyPwSuccessDialog() {
        if (mShowModifyPwSuccessDialog != null) {
            mShowModifyPwSuccessDialog.dismiss();
            mShowModifyPwSuccessDialog = null;
        }
    }

    private void gotoReconnectDevice() {
        Bundle param = new Bundle();
        param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, getDeviceModel());
        param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, getConnectionMode());
        param.putInt(ConstantValue.INTENT_KEY_DATA_PARAM_1, ConstantValue.BLUETOOTH_SCAN_TYPE_EXIST);
        param.putString(ConstantValue.INTENT_KEY_BLE_DEVICE, getBleDeviceId());
        BluetoothScanActivity.toBluetoothScanActivity(this, param);
        finish();
    }

    private String getDeviceId() {
        if (getStartParam() == null) {
            return null;
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_DEVICE_ID);
    }

    private String getDeviceModel() {
        if (getStartParam() == null) {
            return null;
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_IPC_MODEL);
    }

    private int getConnectionMode() {
        if (getStartParam() == null) {
            return ConstantValue.CONNECTION_MODE_QC;
        }
        return getStartParam().getInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC);
    }

    private String getSsid() {
        if (getStartParam() == null) {
            return null;
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_SSID);
    }

    private String getBleDeviceId() {
        if (getStartParam() == null) {
            return null;
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_BLE_DEVICE);
    }
}
