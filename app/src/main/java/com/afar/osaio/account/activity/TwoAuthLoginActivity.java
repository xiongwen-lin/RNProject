package com.afar.osaio.account.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.account.contract.TwoAuthLoginContract;
import com.afar.osaio.account.helper.MyAccountHelper;
import com.afar.osaio.account.presenter.TwoAuthLoginPresenter;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.helper.DeviceConnectionHelper;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.event.AccountStateEvent;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.InputFrameView;
import com.nooie.common.base.GlobalData;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.LoginResult;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.widget.NEventButton;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TwoAuthLoginActivity extends BaseActivity implements TwoAuthLoginContract.View {

    private TwoAuthLoginContract.Presenter mPresenter;

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvTwoAuthLoginAccount)
    TextView tvTwoAuthLoginAccount;
    @BindView(R.id.etTwoAuthLoginCode)
    InputFrameView etTwoAuthLoginCode;
    @BindView(R.id.btnTwoAuthLoginCheck)
    NEventButton btnTwoAuthLoginCheck;
    private boolean mCheckingLogin = false;

    public static void toTwoAuthLoginActivity(Activity from, int requestCode, String account, String password, String countryCode, boolean isSendCode, boolean isClearTask) {
        Intent intent = new Intent(from, TwoAuthLoginActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_ACCOUNT, account);
        intent.putExtra(ConstantValue.INTENT_KEY_PSD, password);
        intent.putExtra(ConstantValue.INTENT_KEY_COUNTRY_CODE, countryCode);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM, isSendCode);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM_1, isClearTask);
        from.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_auth_login);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
            return;
        }
        new TwoAuthLoginPresenter(this);
        mCheckingLogin = false;
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.two_auth_login_title);
        tvTitle.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.theme_text_color));
        String account = getCurrentIntent() != null ? getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_ACCOUNT) : new String();
        tvTwoAuthLoginAccount.setText(String.format(getString(R.string.two_auth_login_tip), account));
        setInputView();
        setupCodeCounterView();
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
            mPresenter.stopSendTwoAuthCodeTask();
            mPresenter.stopVerifyCodeCounter();
            mPresenter.destroy();
        }
        hideLoading();
        releaseRes();
    }

    public void releaseRes() {
        tvTitle = null;
        tvTwoAuthLoginAccount = null;
        if (etTwoAuthLoginCode != null) {
            etTwoAuthLoginCode.release();
        }
        btnTwoAuthLoginCheck = null;
    }

    @Override
    public int getStatusBarMode() {
        return ConstantValue.STATUS_BAR_LIGHT_BLUE_MODE;
    }

    @OnClick({R.id.ivLeft, R.id.btnTwoAuthLoginCheck})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                if (mCheckingLogin) {
                    break;
                }
                finish();
                break;
            }
            case R.id.btnTwoAuthLoginCheck: {
                if (mCheckingLogin) {
                    break;
                }
                String account = getCurrentIntent() != null ? getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_ACCOUNT) : new String();
                String password = getCurrentIntent() != null ? getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_PSD) : new String();
                String country = getCurrentIntent() != null ? getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_COUNTRY_CODE) : new String();
                String code = etTwoAuthLoginCode != null ? etTwoAuthLoginCode.getInputText() : new String();
                if (mPresenter != null && !TextUtils.isEmpty(account) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(country) && !TextUtils.isEmpty(code)) {
                    mCheckingLogin = true;
                    showLoading(false);
                    mPresenter.checkAndLogin(account, password, country, code);
                }
                break;
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mCheckingLogin && keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void setPresenter(@NonNull TwoAuthLoginContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onSendTwoAuthCodeResult(int result) {
        if (isDestroyed() || mPresenter == null) {
            return;
        }
        hideLoading();
        if (result == SDKConstant.SUCCESS) {
            mPresenter.startVerifyCodeCounter();
        } else {
            updateCodeCounter(TwoAuthLoginPresenter.CODE_NONE, null);
        }
    }

    @Override
    public void onCheckAndLoginResult(int result, BaseResponse<LoginResult> response) {
        if (isDestroyed()) {
            return;
        }
        mCheckingLogin = false;
        if (result == SDKConstant.SUCCESS && response != null && response.getCode() == StateCode.SUCCESS.code) {
            initGlobalData();
            NooieDeviceHelper.initNativeConnect();
            NooieDeviceHelper.sendBroadcast(NooieApplication.mCtx, SDKConstant.ACTION_UPDATE_GLOBAL_DATA_FOR_LOGIN, null);
            DeviceConnectionHelper.getInstance().startCheckDeviceConnection(GlobalData.getInstance().getUid());
            hideLoading();
            setLoginSuccessResult();
            goBackToHome();
        } else {
            String errorMsg = getString(R.string.get_fail);
            if (response != null && response.getCode() == StateCode.SEND_VERIFY_FAILED.code) {
                errorMsg = getString(R.string.sign_in_verify_code_incorrect);
            }
            MyAccountHelper.getInstance().logout();
            ToastUtil.showToast(this, errorMsg);
            hideLoading();
        }
    }

    @Override
    public void onCodeCounterChange(int state, String result) {
        if (isDestroyed()) {
            return;
        }
        updateCodeCounter(state, result);
    }

    private void setInputView() {
        etTwoAuthLoginCode
                .setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputTitle(getString(R.string.input_verify_code_enter_code))
                .setInputBtnType(InputFrameView.INPUT_BTN_TYPE_TEXT)
                .setTextInputBtnBg(null)
                .setTextInputBtn(getString(R.string.sign_in_send))
                .setTextInputBtnColor(R.color.theme_subtext_color)
                .setInputBtnIsShow(true)
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                        dealInputBtnClick();
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
        if (!TextUtils.isEmpty(etTwoAuthLoginCode.getInputText())) {
            btnTwoAuthLoginCheck.setEnabled(true);
            btnTwoAuthLoginCheck.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
        } else {
            btnTwoAuthLoginCheck.setEnabled(false);
            btnTwoAuthLoginCheck.setTextColor(getResources().getColor(R.color.unable_clickable_color));
        }
    }

    private void dealInputBtnClick() {
        if (isDestroyed()) {
            return;
        }
        if (etTwoAuthLoginCode != null && etTwoAuthLoginCode.getTag() != null && (int)etTwoAuthLoginCode.getTag() == TwoAuthLoginPresenter.CODE_COUNTING) {
            return;
        }
        String account = getCurrentIntent() != null ? getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_ACCOUNT) : new String();
        String country = getCurrentIntent() != null ? getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_COUNTRY_CODE) : CountryUtil.getCurrentCountry(NooieApplication.mCtx);
        if (mPresenter != null && !TextUtils.isEmpty(account) && !TextUtils.isEmpty(country)) {
            showLoading();
            updateCodeCounter(TwoAuthLoginPresenter.CODE_COUNTING, String.valueOf(TwoAuthLoginPresenter.MAX_VERIFY_CODE_LIMIT_TIME));
            mPresenter.sendTwoAuthCode(account, country);
        }
    }

    private void setupCodeCounterView() {
        String country = getCurrentIntent() != null ? getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_COUNTRY_CODE) : CountryUtil.getCurrentCountry(NooieApplication.mCtx);
        boolean isSendCode = getCurrentIntent() != null && getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_DATA_PARAM, false)
                && !TextUtils.isEmpty(getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_ACCOUNT)) && !TextUtils.isEmpty(country);
        if (isSendCode) {
            updateCodeCounter(TwoAuthLoginPresenter.CODE_COUNTING, String.valueOf(TwoAuthLoginPresenter.MAX_VERIFY_CODE_LIMIT_TIME));
            if (mPresenter != null) {
                mPresenter.startVerifyCodeCounter();
            }
        } else {
            updateCodeCounter(TwoAuthLoginPresenter.CODE_NONE, null);
        }
    }

    private void updateCodeCounter(int state, String result) {
        if (etTwoAuthLoginCode == null) {
            return;
        }
        if (state == TwoAuthLoginPresenter.CODE_COUNTING && !TextUtils.isEmpty(result)) {
            etTwoAuthLoginCode.setTextInputBtnBg(null);
            etTwoAuthLoginCode.setTextInputBtn(new StringBuilder().append(result).append("s").toString());
            etTwoAuthLoginCode.setTag(TwoAuthLoginPresenter.CODE_COUNTING);
        } else if (state == TwoAuthLoginPresenter.CODE_COUNT_FINISH) {
            etTwoAuthLoginCode.setTextInputBtnBg(R.drawable.button_black_state_list_radius_13);
            etTwoAuthLoginCode.setTextInputBtn(getString(R.string.sign_in_resend));
            etTwoAuthLoginCode.setTag(TwoAuthLoginPresenter.CODE_COUNT_FINISH);
        } else {
            etTwoAuthLoginCode.setTextInputBtnBg(R.drawable.button_black_state_list_radius_13);
            etTwoAuthLoginCode.setTextInputBtn(getString(R.string.sign_in_send));
            etTwoAuthLoginCode.setTag(TwoAuthLoginPresenter.CODE_NONE);
        }
    }

    private void goBackToHome() {
        if (getCurrentIntent() != null && getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_DATA_PARAM_1, false)) {
            HomeActivity.toHomeActivity(this);
            //finish();
        } else if (getCurrentIntent() != null && getCurrentIntent().hasExtra(ConstantValue.INTENT_KEY_DATA_PARAM_1) && !getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_DATA_PARAM_1, false)) {
            EventBus.getDefault().post(new AccountStateEvent(AccountStateEvent.ACCOUNT_STATE_REFRESH_AFTER_LOGIN));
        }
        finish();
    }

    private void setLoginSuccessResult() {
        Intent data = new Intent();
        data.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM, true);
        setResult(RESULT_OK, data);
    }
}
