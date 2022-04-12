package com.afar.osaio.account.activity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.account.helper.MyAccountHelper;
import com.afar.osaio.account.presenter.ISetPasswordPresenter;
import com.afar.osaio.account.presenter.SetPasswordPresenterImpl;
import com.afar.osaio.account.view.ISetPasswordView;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.notification.NotificationManager;
import com.afar.osaio.smart.device.helper.DeviceConnectionHelper;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.afar.osaio.widget.InputFrameView;
import com.nooie.common.base.GlobalData;
import com.nooie.data.EventDictionary;
import com.nooie.sdk.bean.SDKConstant;
import com.tuya.smart.wrapper.api.TuyaWrapper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by victor on 2018/6/28
 * Email is victor.qiao.0604@gmail.com
 */
public class SetPasswordActivity extends BaseActivity implements ISetPasswordView {
    public static int USER_FIND_PWD_VERIFY = 2;

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.ipvCreatePsd)
    InputFrameView ipvCreatePsd;
    @BindView(R.id.ipvConfirmPsd)
    InputFrameView ipvConfirmPsd;
    @BindView(R.id.btnDone)
    Button btnDone;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivDoneLoading)
    ImageView ivDoneLoading;
    private int mVerifyType;
    private String mVerifyCode;
    private String mAccount;
    private String mCountryCode;
    private ISetPasswordPresenter mSetPsdPresenter;
    private boolean mIsSignUpOrReset = false;
    private ObjectAnimator doneAnimator = null;

    public static void toSetPasswordActivity(Context from, String account, String verifyCode, String country, int verifyType) {
        Intent intent = new Intent(from, SetPasswordActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_ACCOUNT, account);
        intent.putExtra(ConstantValue.INTENT_KEY_VERIFY_CODE, verifyCode);
        intent.putExtra(ConstantValue.INTENT_KEY_COUNTRY_CODE, country);
        intent.putExtra(ConstantValue.INTENT_KEY_VERIFY_TYPE, verifyType);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);
        ButterKnife.bind(this);

        initData();
        initView();

        // not check force logout
        setCheckForceLogout(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerInputListener();
        checkIsNeedToRequestLayout();
    }

    @Override
    public void onPause() {
        super.onPause();
        unRegisterInputListener();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    private void initData() {
        mCountryCode = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_COUNTRY_CODE);
        mVerifyType = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_VERIFY_TYPE, -1);
        mVerifyCode = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_VERIFY_CODE);
        mAccount = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_ACCOUNT);
        mAccount = mAccount != null ? mAccount.trim() : mAccount;
        mSetPsdPresenter = new SetPasswordPresenterImpl(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.theme_text_color));
        if (mVerifyType == ConstantValue.USER_FIND_PWD_VERIFY) {
            tvTitle.setText(R.string.set_psd_reset_title);
            btnDone.setText(R.string.set_psd_save_and_sign_in);
        } else if (mVerifyType == ConstantValue.USER_REGISTER_VERIFY) {
            tvTitle.setText(R.string.set_psd_title);
            btnDone.setText(R.string.sign_up);
        }
        setupInputFrameView();
        setBtnDownAnimator();
    }

    private void setupInputFrameView() {
        String createPsdTitle = mVerifyType == ConstantValue.USER_REGISTER_VERIFY ? getString(R.string.account_new_password) : getString(R.string.account_new_password);
        ipvCreatePsd.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputTitle(createPsdTitle)
                .setEtInputToggle(true)
                .setInputBtn(R.drawable.close_icon_state_list)
                .setEtPwInputType(InputType.TYPE_CLASS_TEXT)
                .setEtInputType(InputFrameView.getPwInputType(InputType.TYPE_CLASS_TEXT, false))
                .setEtInputMaxLength(20)
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

        ipvConfirmPsd.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputTitle(getString(R.string.account_confirm_password))
                .setEtInputToggle(true)
                .setInputBtn(R.drawable.close_icon_state_list)
                .setEtPwInputType(InputType.TYPE_CLASS_TEXT)
                .setEtInputType(InputFrameView.getPwInputType(InputType.TYPE_CLASS_TEXT, false))
                .setEtInputMaxLength(20)
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                    }

                    @Override
                    public void onEditorAction() {
                        hideInputMethod();
                        onViewClicked(btnDone);
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

    private void setBtnDownAnimator() {
        doneAnimator = ObjectAnimator.ofFloat(ivDoneLoading, "Rotation", 0, 360);
        doneAnimator.setDuration(2000);
        doneAnimator.setRepeatCount(-1);
    }

    private void showBtnDoneLoading() {
        btnDone.setText("");
        ivDoneLoading.setVisibility(View.VISIBLE);
        doneAnimator.start();
    }

    private void hideBtnDoneLoading() {
        btnDone.setText(R.string.submit);
        ivDoneLoading.setVisibility(View.GONE);
        doneAnimator.pause();
    }

    public void checkBtnEnable() {
        ipvConfirmPsd.setWrongTextVisible(View.GONE).setEtInputBackground(R.drawable.osaio_input_bg);
        if (!TextUtils.isEmpty(ipvCreatePsd.getInputText()) && !TextUtils.isEmpty(ipvConfirmPsd.getInputText())) {
            btnDone.setEnabled(true);
            btnDone.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
        } else {
            btnDone.setEnabled(false);
            btnDone.setTextColor(getResources().getColor(R.color.unable_clickable_color));
        }
    }

    @Override
    public int getStatusBarMode() {
        return ConstantValue.STATUS_BAR_LIGHT_BLUE_MODE;
    }

    @OnClick({R.id.ivLeft, R.id.btnDone})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                finish();
                break;
            }
            case R.id.btnDone:
                if (mIsSignUpOrReset) {
                    return;
                }
                if (ipvCreatePsd.getInputText().length() < 6 || ipvConfirmPsd.getInputText().length() < 6) {
                    ipvConfirmPsd.setWrongTextVisible(View.VISIBLE).setWrongText(getResources().getString(R.string.account_psd_not_less_six)).setEtInputBackground(R.drawable.input_background_wrong);
                    //ToastUtil.showLongToast(this, R.string.account_psd_not_less_six);
                } else if (ipvCreatePsd.getInputText().length() > 12 || ipvConfirmPsd.getInputText().length() > 12) {
                    ipvConfirmPsd.setWrongTextVisible(View.VISIBLE).setWrongText(getResources().getString(R.string.account_password_limit_num)).setEtInputBackground(R.drawable.input_background_wrong);
                    //ToastUtil.showLongToast(this, R.string.account_password_limit_num);
                } else if (!ipvCreatePsd.getInputText().equals(ipvConfirmPsd.getInputText())) {
                    ipvConfirmPsd.setWrongTextVisible(View.VISIBLE).setWrongText(getResources().getString(R.string.account_change_psd_new_confirm_not_same)).setEtInputBackground(R.drawable.input_background_wrong);
                    //ToastUtil.showToast(this, R.string.account_change_psd_new_confirm_not_same);
                } else {
                    showLoadingDialog();
                    showBtnDoneLoading();
                    mIsSignUpOrReset = true;
                    mSetPsdPresenter.manageAccount(mAccount, ipvCreatePsd.getInputText(), mVerifyCode, mCountryCode, mVerifyType);
                }
                break;
        }
    }

    @Override
    public void notifySignUpResult(String result) {
        if (isDestroyed()) {
            return;
        }
        if (ConstantValue.SUCCESS.equals(result)) {
            GlobalPrefs.isStartFromRegister = true;
            TuyaWrapper.onLogin();
            NooieDeviceHelper.initNativeConnect();
            NooieDeviceHelper.sendBroadcast(NooieApplication.mCtx, SDKConstant.ACTION_UPDATE_GLOBAL_DATA_FOR_LOGIN, null);
            DeviceConnectionHelper.getInstance().startCheckDeviceConnection(GlobalData.getInstance().getUid());
            HomeActivity.toHomeActivity(SetPasswordActivity.this);
            finish();
        } else {
            MyAccountHelper.getInstance().logout();
            mIsSignUpOrReset = false;
        }
        hideLoadingDialog();
        hideBtnDoneLoading();
    }

    @Override
    public void notifyResetPsdResult(String result) {
        if (isDestroyed()) {
            return;
        }
        if (ConstantValue.SUCCESS.equals(result)) {
            MyAccountHelper.getInstance().logout();
            NotificationManager.getInstance().cancelAllNotifications();
            SignInActivity.toSignInActivity(SetPasswordActivity.this, mAccount, ipvConfirmPsd.getInputText(), true);
            mIsSignUpOrReset = false;
            finish();
        } else {
            MyAccountHelper.getInstance().logout();
            NotificationManager.getInstance().cancelAllNotifications();
            mIsSignUpOrReset = false;
        }
        hideLoadingDialog();
        hideBtnDoneLoading();
    }

    @Override
    public void showLoadingDialog() {
        showLoading(false);
    }

    @Override
    public void hideLoadingDialog() {
        hideLoading();
    }

    @Override
    public String getEventId(int trackType) {
        return EventDictionary.EVENT_ID_ACCESS_SET_PASSWORD;
    }
}
