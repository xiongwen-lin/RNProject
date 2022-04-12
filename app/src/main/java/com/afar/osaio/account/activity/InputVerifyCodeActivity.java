package com.afar.osaio.account.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nooie.sdk.api.network.base.bean.StateCode;
import com.afar.osaio.R;
import com.afar.osaio.account.presenter.InputVerifyCodePresenterImpl;
import com.afar.osaio.account.view.IInputVerifyCodeView;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.InputFrameView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by victor on 2018/6/28
 * Email is victor.qiao.0604@gmail.com
 */
public class InputVerifyCodeActivity extends BaseActivity implements IInputVerifyCodeView {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.ipvVerifyCode)
    InputFrameView ipvVerifyCode;
    @BindView(R.id.btnDone)
    Button btnDone;
    @BindView(R.id.tvDestination)
    TextView tvDestination;
    @BindView(R.id.tvCountdown)
    TextView tvCountdown;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvMsg)
    TextView tvMsg;

    private static final int VERIFY_STATE_NORMAL = 1;
    private static final int VERIFY_STATE_COUNT_DOWN = 2;
    private static final int VERIFY_STATE_RESENT = 3;
    private int mVerifyState = VERIFY_STATE_NORMAL;
    private String mAccount;
    private int mVerifyType;
    private String mCountryCode;
    private InputVerifyCodePresenterImpl mInputVerifyCodePresenter;

    public static void toInputVerityCodeActivity(Context from, String account, String country, int type) {
        Intent intent = new Intent(from, InputVerifyCodeActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_ACCOUNT, account);
        intent.putExtra(ConstantValue.INTENT_KEY_COUNTRY_CODE, country);
        intent.putExtra(ConstantValue.INTENT_KEY_VERIFY_TYPE, type);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_verify_code);
        ButterKnife.bind(this);
        initData();
        initView();
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

    private void initData() {
        // not check force logout
        setCheckForceLogout(false);
        if (null == getCurrentIntent()) {
            finish();
        } else {
            mAccount = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_ACCOUNT);
            mCountryCode = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_COUNTRY_CODE);
            mVerifyType = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_VERIFY_TYPE, ConstantValue.USER_FIND_PWD_VERIFY);
            mInputVerifyCodePresenter = new InputVerifyCodePresenterImpl(this);
        }
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.theme_text_color));
        if (mVerifyType == ConstantValue.USER_FIND_PWD_VERIFY) {
            // reset psd
            ToastUtil.showToast(this, R.string.input_reset_code_sended);
            tvTitle.setText(R.string.sign_in_forget_pwd);
            tvMsg.setText(R.string.input_verify_reset_code_send);
        } else if (mVerifyType == ConstantValue.USER_REGISTER_VERIFY) {
            // sign up
            ToastUtil.showToast(this, R.string.input_verify_code_sended);
            tvTitle.setText(R.string.input_verify_code_title);
            tvMsg.setText(R.string.input_verify_code_send);
        }

        tvCountdown.setVisibility(View.VISIBLE);
        tvCountdown.setBackground(null);
        tvCountdown.setText("");
        tvDestination.setText(mAccount);

        setupInputFrameView();
        mInputVerifyCodePresenter.startVerifyCodeCounter();
    }

    private void setupInputFrameView() {
        ipvVerifyCode.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputTitle(getString(R.string.input_verify_code_enter_code))
                .setInputBtnIsShow(false)
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
        if (!TextUtils.isEmpty(ipvVerifyCode.getInputText())) {
            btnDone.setEnabled(true);
            btnDone.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
        } else {
            btnDone.setEnabled(false);
            btnDone.setTextColor(getResources().getColor(R.color.unable_clickable_color));
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mInputVerifyCodePresenter != null) {
            mInputVerifyCodePresenter.stopVerifyCodeCounter();
        }
    }

    @Override
    public int getStatusBarMode() {
        return ConstantValue.STATUS_BAR_LIGHT_BLUE_MODE;
    }

    @OnClick({R.id.ivLeft, R.id.tvCountdown, R.id.btnDone})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                finish();
                break;
            }
            case R.id.tvCountdown:
                switch (mVerifyState) {
                    case VERIFY_STATE_NORMAL:
                    case VERIFY_STATE_RESENT:
                        showLoading();
                        mInputVerifyCodePresenter.sendVerifyCode(mAccount, mCountryCode, mVerifyType);
                        break;
                }
                break;
            case R.id.btnDone:
                if (TextUtils.isEmpty(ipvVerifyCode.getInputText())) {
                    ToastUtil.showToast(this, R.string.input_verify_please_enter_code);
                    return;
                }
                showLoading();
                mInputVerifyCodePresenter.checkVerifyCode(mAccount, ipvVerifyCode.getInputText(), mCountryCode, mVerifyType);
                break;
        }
    }

    @Override
    public void notifyVerifyCodeLimitTime(String result) {
        if (isDestroyed()) {
            return;
        }
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            mVerifyState = VERIFY_STATE_RESENT;
        } else {
            mVerifyState = VERIFY_STATE_COUNT_DOWN;
        }
        showCountDownTv(result + "s");
    }

    private void showCountDownTv(String result) {
        if (checkNull(tvCountdown)) {
            return;
        }
        switch (mVerifyState) {
            case VERIFY_STATE_NORMAL:
                tvCountdown.setBackground(null);
                tvCountdown.setText("");
                break;
            case VERIFY_STATE_COUNT_DOWN:
                tvCountdown.setBackground(null);
                tvCountdown.setText(result);
                break;
            case VERIFY_STATE_RESENT:
                tvCountdown.setBackgroundResource(R.drawable.button_black_state_list_radius_13);
                tvCountdown.setText(R.string.sign_in_resend);
                break;
        }
    }

    @Override
    public void notifyCheckVerifyCodeResult(String result, int code) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            SetPasswordActivity.toSetPasswordActivity(InputVerifyCodeActivity.this, mAccount, ipvVerifyCode.getInputText(), mCountryCode, mVerifyType);
        }  else {
            String errorMsg = getString(R.string.sign_in_verify_code_incorrect);
            if (code == StateCode.ACCOUNT_EXIST.code) {
                errorMsg = getString(R.string.sign_up_account_exist);
            }
            ToastUtil.showToast(this, errorMsg);
        }
    }

    @Override
    public void sendVerifyCodeResult(String result) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            ToastUtil.showToast(this, R.string.input_verify_code_sended);
        } else {
            mVerifyState = VERIFY_STATE_RESENT;
            showCountDownTv("");
        }
    }
}
