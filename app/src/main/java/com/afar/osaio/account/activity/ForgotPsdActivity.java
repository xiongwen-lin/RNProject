package com.afar.osaio.account.activity;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.util.CommonUtil;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.afar.osaio.widget.WebViewDialogView;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.afar.osaio.R;
import com.afar.osaio.account.presenter.IVerifyAccountPresenter;
import com.afar.osaio.account.presenter.VerifyAccountPresenterImpl;
import com.afar.osaio.account.view.IAccountView;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.InputFrameView;
import com.nooie.common.utils.configure.CountryUtil;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.nooie.sdk.bean.SDKConstant;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by victor on 2018/6/28
 * Email is victor.qiao.0604@gmail.com
 */
public class ForgotPsdActivity extends BaseActivity implements IAccountView {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ipvAccount)
    InputFrameView ipvAccount;
    @BindView(R.id.btnDone)
    Button btnDone;
    @BindView(R.id.forgetPsdContainer)
    ConstraintLayout forgetPsdContainer;
    @BindView(R.id.wdvForgetPsd)
    WebViewDialogView wdvForgetPsd;
    @BindView(R.id.ivDoneLoading)
    ImageView ivDoneLoading;

    private IVerifyAccountPresenter mVerifyAccountPresenter;
    private int mVerifyType;
    private String mCountryCode;
    private String mUsername;
    private boolean mIsCodeSending = false;
    private long mLastDoneBtnClickTime = 0;
    private Dialog mAccountMigrationDialog = null;
    private ObjectAnimator doneAnimator = null;

    public static void toForgotPsdActivity(Context from, String account, int type) {
        Intent intent = new Intent(from, ForgotPsdActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_ACCOUNT, account);
        intent.putExtra(ConstantValue.INTENT_KEY_VERIFY_TYPE, type);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_psd);
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
        hideLoading();
        hideBtnDoneLoading();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideLoading();
        hideBtnDoneLoading();
        releaseRes();
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        hideAccountMigrationDialog();
        hidePrivacyDialogForAccountMoving();
        if (wdvForgetPsd != null) {
            wdvForgetPsd.release();
            wdvForgetPsd = null;
        }
    }

    private void initData() {
        // not check force logout
        setCheckForceLogout(false);
        mVerifyAccountPresenter = new VerifyAccountPresenterImpl(this);
        mCountryCode = CountryUtil.getCurrentCountry(NooieApplication.mCtx);
        mVerifyAccountPresenter.getCurrentCountryCode(mUserAccount);

        if (null != getCurrentIntent()) {
            mUsername = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_ACCOUNT);
            mVerifyType = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_VERIFY_TYPE, ConstantValue.USER_REGISTER_VERIFY);
        }
    }

    private void initView() {
        int screen_h = DisplayUtil.SCREEN_HIGHT_PX - DisplayUtil.dpToPx(NooieApplication.mCtx,
                DisplayUtil.HEADER_BAR_HEIGHT_DP) - DisplayUtil.getStatusBarHeight(NooieApplication.mCtx);
        screen_h = Math.max(DisplayUtil.SCREEN_CONTENT_MIN_HEIGHT_PX, screen_h);
        forgetPsdContainer.setMinHeight(screen_h);

        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setTextColor(ContextCompat.getColor(NooieApplication.mCtx, R.color.theme_text_color));
        tvTitle.setText(R.string.sign_in_forget_pwd);
        setupInputFrameView();
        setupPrivacyDialogForAccountMoving();
        setBtnDownAnimator();
    }

    private void setupInputFrameView() {
        ipvAccount.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputTitle(getString(R.string.phone_number_email))
                .setInputBtn(R.drawable.close_icon_state_list)
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                        ipvAccount.setEtInputText("");
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

        if (!TextUtils.isEmpty(mUsername)) {
            ipvAccount.setEtInputText(mUsername);
            ipvAccount.setEtSelection(mUsername.length());
        }

        checkBtnEnable();
    }

    public void checkBtnEnable() {
        if (!TextUtils.isEmpty(ipvAccount.getInputText())) {
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
            case R.id.btnDone:
                if (!checkPrivacyIsReadByAccount(getInputAccount())) {
                    if (mVerifyAccountPresenter != null) {
                        showLoading();
                        showBtnDoneLoading();
                        mVerifyAccountPresenter.checkAccountSourceForRegister(getInputAccount());
                    }
                    break;
                }
                tryToSendCode();
                break;
            case R.id.ivLeft:
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
        }
    }

    @Override
    public void sendVerifyCodeResult(String result, int code) {
        if (isDestroyed() || checkNull(ipvAccount)) {
            return;
        }

        hideLoading();
        hideBtnDoneLoading();
        if (result.equals(ConstantValue.SUCCESS)) {
            InputVerifyCodeActivity.toInputVerityCodeActivity(ForgotPsdActivity.this, ipvAccount.getInputText(), mCountryCode, mVerifyType);
        } else {
            if (code == StateCode.ACCOUNT_NOT_EXIST.code) {
                ToastUtil.showToast(this, R.string.sign_in_account_not_exist);
            } else if (code == StateCode.ACCOUNT_FORMAT_ERROR.code) {
                ToastUtil.showToast(this, R.string.camera_share_account_invalid);
            }
        }
        mIsCodeSending = false;
    }

    @Override
    public void notifyCurrentCountryCode(String code) {
        mCountryCode = code;
    }

    @Override
    public void onCheckAccountSourceForRegister(String account) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
        hideBtnDoneLoading();
        tryToSendCode();
    }

    @Override
    public void onCheckAccountSource(int state, boolean isOtherBrand, String brand) {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
        hideBtnDoneLoading();
        if (state == SDKConstant.SUCCESS) {
            checkAccountSourceOnSuccess(isOtherBrand, brand);
        } else {
            ToastUtil.showToast(this, getString(R.string.network_error0));
        }
    }

    private void setupPrivacyDialogForAccountMoving() {
        if (isDestroyed() || checkNull(wdvForgetPsd)) {
            return;
        }
        wdvForgetPsd.setListener(new WebViewDialogView.WebViewDialogListener() {
            @Override
            public void onCancel() {
                hidePrivacyDialogForAccountMoving();
            }

            @Override
            public void onConfirm() {
                dealOnConfirmPrivacyOfAccountMoving(getInputAccount());
            }

            @Override
            public void onOutSideClick() {
                hidePrivacyDialogForAccountMoving();
            }

            @Override
            public void onPageStarted() {
                if (isDestroyed()) {
                    return;
                }
                showLoading();
            }

            @Override
            public void onPageFinished() {
                if (isDestroyed()) {
                    return;
                }
                hideLoading();
            }
        });
    }

    private void showPrivacyDialogForAccountMoving(String countryCode) {
        if (isDestroyed() || checkNull(wdvForgetPsd)) {
            return;
        }
        wdvForgetPsd.setVisibility(View.VISIBLE);
        wdvForgetPsd.loadContent(CommonUtil.getPrivacyPolicyByCountry(NooieApplication.mCtx, countryCode));
    }

    private void hidePrivacyDialogForAccountMoving() {
        if (isDestroyed() || checkNull(wdvForgetPsd)) {
            return;
        }
        wdvForgetPsd.setVisibility(View.GONE);
    }

    private String getSelectCountryCode() {
        return mCountryCode;
    }

    private boolean checkIsShowPrivacyDialogForAccountMoving(String account) {
        if (TextUtils.isEmpty(account) || GlobalPrefs.getPrivacyIsReadByAccount(account)) {
            return false;
        }
        showPrivacyDialogForAccountMoving(getSelectCountryCode());
        return true;
    }

    private boolean checkPrivacyIsReadByAccount(String account) {
        return TextUtils.isEmpty(account) || GlobalPrefs.getPrivacyIsReadByAccount(account);
    }

    private void dealOnConfirmPrivacyOfAccountMoving(String account) {
        if (isDestroyed() || checkNull(ipvAccount) || TextUtils.isEmpty(account)) {
            return;
        }
        GlobalPrefs.setPrivacyIsReadByAccount(account, true);
        hidePrivacyDialogForAccountMoving();
        tryToSendCode();
    }

    private void tryToSendCode() {
        if (isDestroyed() || checkNull(ipvAccount, mVerifyAccountPresenter)) {
            return;
        }
        if (TextUtils.isEmpty(ipvAccount.getInputText())) {
            ToastUtil.showToast(this, R.string.sign_in_account_empty);
        } else if ((System.currentTimeMillis() - mLastDoneBtnClickTime) < ConstantValue.BTN_CLICK_GAP_TIME) {
            ToastUtil.showToast(ForgotPsdActivity.this, R.string.repeat_click_btn_soon);
        } else if (!mIsCodeSending) {
            mLastDoneBtnClickTime = System.currentTimeMillis();
            mIsCodeSending = true;
            showLoading();
            mVerifyAccountPresenter.sendVerifyCode(ipvAccount.getInputText(), mCountryCode, mVerifyType);
        }
    }

    private String getInputAccount() {
        if (isDestroyed() || checkNull(ipvAccount)) {
            return "";
        }
        String account = ipvAccount.getInputText();
        return account;
    }

    private void checkAccountSourceOnSuccess(boolean isOtherBrand, String brand) {
        if (isDestroyed()) {
            return;
        }
        if (isOtherBrand) {
            showAccountMigrationDialog(brand);
        }
    }

    private void showAccountMigrationDialog(String brand) {
        hideAccountMigrationDialog();
        String content = String.format(getString(R.string.sign_account_migration_tip_content), brand);
        mAccountMigrationDialog = DialogUtils.showConfirmWithSubMsgDialog(this, getString(R.string.sign_account_migration_tip_title), content, getString(R.string.cancel_normal), getString(R.string.privacy_policy), new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickLeft() {
            }

            @Override
            public void onClickRight() {
                showPrivacyDialogForAccountMoving(getSelectCountryCode());
            }
        });
    }

    private void hideAccountMigrationDialog() {
        if (mAccountMigrationDialog != null) {
            mAccountMigrationDialog.dismiss();
            mAccountMigrationDialog = null;
        }
    }
}
