package com.afar.osaio.account.activity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.smart.device.helper.DeviceConnectionHelper;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.event.AccountStateEvent;
import com.afar.osaio.util.CommonUtil;
import com.afar.osaio.util.CompatUtil;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.preference.GlobalPrefs;
import com.afar.osaio.widget.WebViewDialogView;
import com.nooie.common.base.GlobalData;
import com.nooie.common.utils.configure.FontUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.tool.CpuWakeLock;
import com.nooie.data.EventDictionary;
import com.nooie.eventtracking.EventTrackingApi;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.afar.osaio.R;
import com.afar.osaio.account.helper.MyAccountHelper;
import com.afar.osaio.account.presenter.ISignInPresenter;
import com.afar.osaio.account.presenter.SignInPresenterImpl;
import com.afar.osaio.account.view.ISignInView;
import com.afar.osaio.application.activity.WebViewActivity;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.notification.NotificationManager;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.db.entity.UserInfoEntity;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.smart.setting.activity.CountryListActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.FButton;
import com.afar.osaio.widget.InputFrameView;
import com.afar.osaio.widget.adapter.AutoCompleteAdapter;
import com.nooie.common.bean.CountryViewBean;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.configure.CountryUtil;
import com.tuya.smart.wrapper.api.TuyaWrapper;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.afar.osaio.util.ConstantValue.INTENT_KEY_ACCOUNT;
import static com.afar.osaio.util.ConstantValue.INTENT_KEY_PSD;
import static com.afar.osaio.util.ConstantValue.SUCCESS;

public class SignInActivity extends BaseActivity implements ISignInView {

    private static final int PRIVACY_OF_ACCOUNT_MOVING_TYPE_SIGN_IN = 1;
    private static final int PRIVACY_OF_ACCOUNT_MOVING_TYPE_SIGN_UP = 2;

    @BindView(R.id.btnSignIn)
    Button btnSignIn;
    @BindView(R.id.tvForgetPsd)
    TextView tvForgetPsd;
    @BindView(R.id.ipvAccount)
    InputFrameView ipvAccount;
    @BindView(R.id.ipvPsd)
    InputFrameView ipvPsd;
    @BindView(R.id.btnSignInTag)
    TextView btnSignInTag;
    @BindView(R.id.vSignInSelectedTag)
    View vSignInSelectedTag;
    @BindView(R.id.btnSignUpTag)
    TextView btnSignUpTag;
    @BindView(R.id.vSignUpSelectedTag)
    View vSignUpSelectedTag;
    @BindView(R.id.ipvSelectCountry)
    InputFrameView ipvSelectCountry;
    @BindView(R.id.ipvRegisterAccount)
    InputFrameView ipvRegisterAccount;
    @BindView(R.id.ipvVerifyCode)
    InputFrameView ipvVerifyCode;
    @BindView(R.id.tvCountdown)
    TextView tvCountdown;
    @BindView(R.id.cbPrivacy)
    CheckBox cbPrivacy;
    @BindView(R.id.tvPrivacy)
    TextView tvPrivacy;
    @BindView(R.id.btnSignUp)
    FButton btnSignUp;
    @BindView(R.id.ivSignUpLoading)
    ImageView ivSignUpLoading;
    @BindView(R.id.ivSignInLoading)
    ImageView ivSignInLoading;

    @BindView(R.id.wdvSignIn)
    WebViewDialogView wdvSignIn;

    private static final int REGISTER_STATE_NORMAL = 1;
    private static final int REGISTER_STATE_COUNT_DOWN = 2;
    private static final int REGISTER_STATE_RESENT = 3;
    private int mRegisterState = REGISTER_STATE_NORMAL;
    private static final int SIGN_IN_TYPE = 1;
    private static final int SIGN_UP_TYPE = 2;
    private static final int SEND_CODE_NORMAL_TYPE = 1;
    private static final int SEND_CODE_AUTO_TYPE = 2;
    private static final int CPU_WAKE_TIME_OUT = 60 * 60 * 1000;

    private AutoCompleteAdapter accountAdapter;
    private int mDropListItemHeight = 0;
    private int prevCount = 0;
    private static final int MAX_ONCE_MATCHED_ITEM = 2;
    private boolean isSignining;
    private CountryViewBean mCountry;
    private int mCurrentType;
    private boolean mIsAutoSendCode = false;

    private ISignInPresenter mSignInPresenter;
    private Dialog mSignUpTipDialog = null;
    private int mPrivacyOfAccountMovingType = 0;
    private Dialog mAccountMigrationDialog = null;
    private ObjectAnimator btnSignInAnimator = null;
    private ObjectAnimator btnSignUpAnimator = null;

    public static void toSignInActivity(Context from) {
        Intent intent = new Intent(from, SignInActivity.class);
        from.startActivity(intent);
    }

    public static void toSignInActivity(Activity from, String account, String password, boolean isClearTask) {
        Intent intent = new Intent(from, SignInActivity.class);
        if (isClearTask) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        intent.putExtra(INTENT_KEY_ACCOUNT, account);
        intent.putExtra(INTENT_KEY_PSD, password);
        intent.putExtra(ConstantValue.INTENT_KEY_DATA_PARAM, isClearTask);
        from.startActivityForResult(intent, ConstantValue.REQUEST_CODE_SIGN_IN);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSlideable(false);

        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);

        initData();
        initView();

        // not check force logout
        setCheckForceLogout(false);
    }

    @Override
    protected void onResume() {
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
    public void onStop() {
        super.onStop();
        if (CpuWakeLock.getInstance().isLock()) {
            CpuWakeLock.getInstance().unlock();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mSignInPresenter != null) {
            mSignInPresenter.stopVerifyCodeCounter();
            mSignInPresenter.destroy();
            mSignInPresenter = null;
        }
        releaseRes();
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        btnSignIn = null;
        tvForgetPsd = null;
        if (ipvAccount != null) {
            ipvAccount.release();
            ipvAccount = null;
        }
        if (ipvPsd != null) {
            ipvPsd.release();
            ipvPsd = null;
        }
        btnSignInTag = null;
        vSignInSelectedTag = null;
        btnSignUpTag = null;
        vSignUpSelectedTag = null;
        if (ipvSelectCountry != null) {
            ipvSelectCountry.release();
            ipvSelectCountry = null;
        }
        if (ipvRegisterAccount != null) {
            ipvRegisterAccount.release();
            ipvRegisterAccount = null;
        }
        if (ipvVerifyCode != null) {
            ipvVerifyCode.release();
            ipvVerifyCode = null;
        }
        tvCountdown = null;
        cbPrivacy = null;
        tvPrivacy = null;
        btnSignUp = null;
        if (accountAdapter != null) {
            accountAdapter.setOnSimpleItemDeletedListener(null);
            accountAdapter.clear();
            accountAdapter = null;
        }
        hideSignUpTipDialog();
        hideAccountMigrationDialog();
        hidePrivacyDialogForAccountMoving();
        if (wdvSignIn != null) {
            wdvSignIn.release();
            wdvSignIn = null;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    private void initView() {
        ArrayList<String> mOriginalValues = new ArrayList<>();
        accountAdapter = new AutoCompleteAdapter(this, mOriginalValues);
        accountAdapter.setDefaultMode(AutoCompleteAdapter.MODE_STARTSWITH | AutoCompleteAdapter.MODE_SPLIT);// 设置匹配模式
        accountAdapter.setSupportPreview(false);

        accountAdapter.setOnSimpleItemDeletedListener(new AutoCompleteAdapter.OnSimpleItemDeletedListener() {
            @Override
            public void onSimpleItemDeletedListener(String value) {
                if (mSignInPresenter != null) {
                    mSignInPresenter.removeAccountFromHistory(value);
                }
            }
        });
        // 设置下拉时显示的提示行数 (此处不设置也可以，因为在AutoCompleteAdapter中有专门的事件监听来实时设置提示框的高度)
        // mCustomTv.setDropDownHeight(simpleItemHeight * MAX_ONCE_MATCHED_ITEM);
        setupSignInOrUpView();
        setupInputFrameView();
        tvForgetPsd.setText(getString(R.string.sign_in_forget_pwd) + "?");
        setupPrivacyClickableTv();
        setupPrivacyDialogForAccountMoving();
        setBtnSignInAnimator();
        setBtnSignUpAnimator();
    }

    private void initData() {
        isSignining = false;
        mIsAutoSendCode = true;
        mSignInPresenter = new SignInPresenterImpl(this);
        if (mSignInPresenter != null) {
            mSignInPresenter.loadAccountHistory();
        }

        String account = getCurrentIntent().getStringExtra(INTENT_KEY_ACCOUNT);
        String password = getCurrentIntent().getStringExtra(INTENT_KEY_PSD);
        if (!TextUtils.isEmpty(account)) {
            ipvAccount.setEtInputText(account);
            ipvAccount.setEtSelection(account.length());
        }

        if (!TextUtils.isEmpty(password)) {
            ipvPsd.setEtInputText(password);
            ipvPsd.setEtSelection(password.length());
        }

        initCurrentCountryCode();
    }

    private void initCurrentCountryCode() {
        String countryKey = CountryUtil.getCurrentCountryKey(NooieApplication.mCtx);
        String countryCode = CountryUtil.getCurrentCountry(NooieApplication.mCtx);
        String countryName = CountryUtil.getCurrentCountryTitle(NooieApplication.mCtx);
        mCountry = new CountryViewBean(countryKey, countryName, countryCode, "", NooieApplication.mCtx);
    }

    private void setupInputFrameView() {
        ipvAccount.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputTitleVisible(View.GONE)
                .setEtInputHint(getString(R.string.phone_number_email))
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

        ipvPsd.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputTitleVisible(View.GONE)
                .setEtInputHint(getString(R.string.password))
                .setEtInputToggle(true)
                .setEtPwInputType(InputType.TYPE_CLASS_TEXT)
                .setEtInputType(InputFrameView.getPwInputType(InputType.TYPE_CLASS_TEXT, false))
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                    }

                    @Override
                    public void onEditorAction() {
                        hideInputMethod();
                        onViewClicked(btnSignIn);
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

        ipvSelectCountry.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputTitleVisible(View.GONE)
                .setEtInputHint(getString(R.string.sign_in_country_title))
                .setEtInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
                .setInputBtnIsShow(true)
                .setInputBtn(R.drawable.right_arrow_gray)
                .setCursorVisilbe(false)
                .setIpvFocusable(false)
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                        CountryListActivity.toCountryListActivity(SignInActivity.this, ConstantValue.REQUEST_CODE_SELECT_COUNTRY);
                    }

                    @Override
                    public void onEditorAction() {
                    }

                    @Override
                    public void onEtInputClick() {
                        CountryListActivity.toCountryListActivity(SignInActivity.this, ConstantValue.REQUEST_CODE_SELECT_COUNTRY);
                    }
                });

        if (mCountry != null) {
            ipvSelectCountry.setEtInputText(mCountry.getCountryName());
        }

        ipvRegisterAccount.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputTitleVisible(View.GONE)
                .setEtInputHint(getString(R.string.phone_number_email))
                .setInputBtn(R.drawable.close_icon_state_list)
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                        ipvRegisterAccount.setEtInputText("");
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

        ipvVerifyCode.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputTitleVisible(View.GONE)
                .setEtInputHint(getString(R.string.input_verify_code_enter_code))
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
                })
                .setEtInputFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean hasFocus) {
                        NooieLog.d("-->> debug SignInActivity onViewClicked: ipvVerifyCode onFocusChange hasFocus=" + hasFocus + " mIsAutoSendCode=" + mIsAutoSendCode);
                        if (hasFocus) {
                            if (ipvRegisterAccount != null) {
                                checkIsResetSentCode(ipvRegisterAccount.getInputText());
                            }
                            if (mIsAutoSendCode) {
                                tryToSendCode(SEND_CODE_AUTO_TYPE);
                            }
                        }
                    }
                });

        checkBtnEnable();
    }

    public void setupSignInOrUpView() {
        mCurrentType = SIGN_IN_TYPE;
        showSignInOrUpView();
        mRegisterState = REGISTER_STATE_NORMAL;
        showCountDownTv("");
    }

    public void showSignInOrUpView() {
        if (checkNull(btnSignInTag, vSignInSelectedTag, btnSignIn, btnSignUp, btnSignUpTag, vSignUpSelectedTag, ipvSelectCountry, ipvAccount, ipvPsd, ipvVerifyCode, cbPrivacy, tvPrivacy, tvCountdown, tvForgetPsd)) {
            return;
        }
        if (mCurrentType == SIGN_IN_TYPE) {
            btnSignInTag.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
            vSignInSelectedTag.setVisibility(View.VISIBLE);
            btnSignUpTag.setTextColor(getResources().getColor(R.color.theme_text_color));
            vSignUpSelectedTag.setVisibility(View.GONE);
            ipvSelectCountry.setVisibility(View.GONE);
            ipvRegisterAccount.setVisibility(View.GONE);
            ipvVerifyCode.setVisibility(View.GONE);
            tvCountdown.setVisibility(View.GONE);
            cbPrivacy.setVisibility(View.GONE);
            tvPrivacy.setVisibility(View.GONE);
            btnSignUp.setVisibility(View.GONE);

            ipvAccount.setVisibility(View.VISIBLE);
            ipvPsd.setVisibility(View.VISIBLE);
            tvForgetPsd.setVisibility(View.VISIBLE);
            btnSignIn.setVisibility(View.VISIBLE);
        } else if (mCurrentType == SIGN_UP_TYPE) {
            btnSignInTag.setTextColor(getResources().getColor(R.color.theme_text_color));
            vSignInSelectedTag.setVisibility(View.GONE);
            btnSignUpTag.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
            vSignUpSelectedTag.setVisibility(View.VISIBLE);
            ipvAccount.setVisibility(View.GONE);
            ipvPsd.setVisibility(View.GONE);
            tvForgetPsd.setVisibility(View.GONE);
            btnSignIn.setVisibility(View.GONE);

            ipvSelectCountry.setVisibility(View.VISIBLE);
            ipvRegisterAccount.setVisibility(View.VISIBLE);
            ipvVerifyCode.setVisibility(View.VISIBLE);
            tvCountdown.setVisibility(View.VISIBLE);
            cbPrivacy.setVisibility(View.VISIBLE);
            tvPrivacy.setVisibility(View.VISIBLE);
            btnSignUp.setVisibility(View.VISIBLE);
        }
    }

    public void checkBtnEnable() {
        if (!TextUtils.isEmpty(ipvAccount.getInputText()) && !TextUtils.isEmpty(ipvPsd.getInputText())) {
            btnSignIn.setEnabled(true);
            btnSignIn.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
        } else {
            btnSignIn.setEnabled(false);
            btnSignIn.setTextColor(getResources().getColor(R.color.unable_clickable_color));
        }

        if (!TextUtils.isEmpty(ipvRegisterAccount.getInputText()) && !TextUtils.isEmpty(ipvVerifyCode.getInputText())) {
            btnSignUp.setEnabled(true);
            btnSignUp.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
        } else {
            btnSignUp.setEnabled(false);
            btnSignUp.setTextColor(getResources().getColor(R.color.unable_clickable_color));
        }
    }

    private void setupPrivacyClickableTv() {
        SpannableStringBuilder style = new SpannableStringBuilder();
        String conditionUse = getString(R.string.terms_of_service);
        String privacy = getString(R.string.privacy_policy);
        String text = String.format(getString(R.string.sign_up_create_account_tip), conditionUse, privacy);

        //设置文字
        style.append(text);

        //设置部分文字点击事件
        ClickableSpan conditionClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                WebViewActivity.toWebViewActivity(SignInActivity.this, WebViewActivity.getUrl(CommonUtil.getTerms(NooieApplication.mCtx)), getString(R.string.terms_of_service));
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
                ds.setTypeface(FontUtil.loadTypeface(getApplicationContext(), "fonts/manrope-semibold.otf"));
            }
        };
        style.setSpan(conditionClickableSpan, text.indexOf(conditionUse), text.indexOf(conditionUse) + conditionUse.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvPrivacy.setText(style);

        //设置部分文字点击事件
        ClickableSpan privacyClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                String countryCode = mCountry != null && !TextUtils.isEmpty(mCountry.getNumber()) ? mCountry.getNumber() : CountryUtil.getCurrentCountry(NooieApplication.mCtx);
                WebViewActivity.toWebViewActivity(SignInActivity.this, WebViewActivity.getUrl(CommonUtil.getPrivacyPolicyByCountry(NooieApplication.mCtx, countryCode)), getString(R.string.privacy_policy));
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
                ds.setTypeface(FontUtil.loadTypeface(getApplicationContext(), "fonts/manrope-semibold.otf"));
            }
        };
        style.setSpan(privacyClickableSpan, text.indexOf(privacy), text.indexOf(privacy) + privacy.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvPrivacy.setText(style);

        //设置部分文字颜色
        ForegroundColorSpan conditionForegroundColorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.theme_green));
        style.setSpan(conditionForegroundColorSpan, text.indexOf(conditionUse), text.indexOf(conditionUse) + conditionUse.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ForegroundColorSpan privacyForegroundColorSpan = new ForegroundColorSpan(ContextCompat.getColor(this, R.color.theme_green));
        style.setSpan(privacyForegroundColorSpan, text.indexOf(privacy), text.indexOf(privacy) + privacy.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //配置给TextView
        tvPrivacy.setMovementMethod(LinkMovementMethod.getInstance());
        tvPrivacy.setText(style);

        cbPrivacy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            }
        });
    }

    @Override
    public int getStatusBarMode() {
        return ConstantValue.STATUS_BAR_LIGHT_BLUE_MODE;
    }

    @OnClick({R.id.btnSignInTag, R.id.btnSignUpTag, R.id.btnSignIn, R.id.btnSignUp, R.id.tvForgetPsd, R.id.tvCountdown, R.id.ivExistIcon})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btnSignIn:
                hideInputMethod();
                if (isSignining || mCurrentType != SIGN_IN_TYPE) {
                    break;
                }

                if (TextUtils.isEmpty(ipvAccount.getInputText()) || TextUtils.isEmpty(ipvAccount.getInputText())) {
                    String msg = TextUtils.isEmpty(ipvAccount.getInputText()) ? getResources().getString(R.string.sign_in_account_empty) : getResources().getString(R.string.sign_in_password_empty);
                    notifySignInResult(msg, StateCode.UNKNOWN.code);
                    break;
                }

                if (!checkPrivacyIsReadByAccount(ipvAccount.getInputText())) {
                    if (mSignInPresenter != null) {
                        //showLoading();
                        showBtnSignInLoading();
                        mSignInPresenter.checkAccountSourceForSignIn(ipvAccount.getInputText(), ipvPsd.getInputText());
                        break;
                    }
                    break;
                }

                tryToSignIn();
                break;
            case R.id.btnSignUp:
                hideInputMethod();
                if (mCurrentType != SIGN_UP_TYPE) {
                    break;
                }

                if (TextUtils.isEmpty(ipvRegisterAccount.getInputText())) {
                    ToastUtil.showToast(this, R.string.sign_in_account_empty);
                } else if (TextUtils.isEmpty(ipvVerifyCode.getInputText())) {
                    ToastUtil.showToast(this, R.string.input_verify_please_enter_code);
                } else if (!cbPrivacy.isChecked()) {
                    ToastUtil.showToast(this, R.string.sign_up_tip_check_user_privacy);
                } else {
                    showBtnSignUpLoading();
                    showLoadingDialog();
                    if (mSignInPresenter != null) {
                        mSignInPresenter.checkRegisterVerifyCode(ipvRegisterAccount.getInputText(), ipvVerifyCode.getInputText(), mCountry.getNumber());
                    }
                }
                break;
            case R.id.tvForgetPsd:
                ForgotPsdActivity.toForgotPsdActivity(this, ipvAccount.getInputText(), ConstantValue.USER_FIND_PWD_VERIFY);
                break;
            case R.id.btnSignInTag:
                if (mCurrentType == SIGN_IN_TYPE) {
                    break;
                }
                mCurrentType = SIGN_IN_TYPE;
                showSignInOrUpView();
                break;
            case R.id.btnSignUpTag:
                if (mCurrentType == SIGN_UP_TYPE) {
                    break;
                }
                if (!CpuWakeLock.getInstance().isLock()) {
                    CpuWakeLock.getInstance().lock(this, CPU_WAKE_TIME_OUT);
                }
                mCurrentType = SIGN_UP_TYPE;
                showSignInOrUpView();
                break;
            case R.id.tvCountdown:
                NooieLog.d("-->> debug SignInActivity onViewClicked: tvCountdown");
                checkIsResetSentCode(ipvRegisterAccount.getInputText());
                tryToSendCode(SEND_CODE_NORMAL_TYPE);
                /*
                if (mCurrentType != SIGN_UP_TYPE) {
                    return;
                }
                NooieLog.d("-->> debug SignInActivity onViewClicked: tvCountdown mRegisterState" + mRegisterState);
                switch (mRegisterState) {
                    case REGISTER_STATE_NORMAL:
                    case REGISTER_STATE_RESENT:
                        if (TextUtils.isEmpty(ipvRegisterAccount.getInputText())) {
                            ToastUtil.showToast(this, R.string.sign_in_account_empty);
                        } else if (!Util.checkEmail(ipvRegisterAccount.getInputText())) {
                            ToastUtil.showToast(this, R.string.sign_up_tip_email);
                        } else {
                            showLoadingDialog();
                            if (mSignInPresenter != null) {
                                mIsAutoSendCode = false;
                                mSignInPresenter.sendRegisterVerifyCode(ipvRegisterAccount.getInputText(), mCountry.getNumber());
                            }
                            if (mRegisterState == REGISTER_STATE_RESENT) {
                                EventTrackingApi.getInstance().trackNormalEvent(EventDictionary.EVENT_ID_CLICK_RESEND_VERIFY_CODE);
                            }
                        }
                        break;
                }
                 */
                break;
            case R.id.ivExistIcon:
                goBackToHome(true);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ConstantValue.REQUEST_CODE_SELECT_COUNTRY: {
                    if (isDestroyed() || checkNull(ipvSelectCountry)) {
                        break;
                    }
                    String countryCode = data != null ? data.getStringExtra(ConstantValue.INTENT_KEY_PHONE_CODE) : CountryUtil.getCurrentCountry(NooieApplication.mCtx);
                    String countryName = data != null ? data.getStringExtra(ConstantValue.INTENT_KEY_COUNTRY_NAME) : CountryUtil.getCurrentCountryTitle(NooieApplication.mCtx);
                    String countryKey = data != null ? data.getStringExtra(ConstantValue.INTENT_KEY_COUNTRY_KEY) : CountryUtil.getCurrentCountryKey(NooieApplication.mCtx);
                    ipvSelectCountry.setEtInputText(countryName);
                    if (mCountry == null) {
                        initCurrentCountryCode();
                    }
                    mCountry.setNumber(countryCode);
                    mCountry.setKey(countryKey);
                    mCountry.setCountryName(countryName);
                    break;
                }
                case ConstantValue.REQUEST_CODE_FOR_TWO_AUTH_LOGIN: {
                    if (data != null && data.getBooleanExtra(ConstantValue.INTENT_KEY_DATA_PARAM, false)) {
                        finish();
                    }
                    break;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        /*
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            goBackToHome(true);
        }
         */
        return super.onKeyUp(keyCode, event);
    }

    private void goBackToHome(boolean isBack) {
        if (getCurrentIntent() != null && getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_DATA_PARAM, false)) {
            HomeActivity.toHomeActivity(this);
            //finish();
        } else if (!isBack && getCurrentIntent() != null && getCurrentIntent().hasExtra(ConstantValue.INTENT_KEY_DATA_PARAM) && !getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_DATA_PARAM, false)) {
            EventBus.getDefault().post(new AccountStateEvent(AccountStateEvent.ACCOUNT_STATE_REFRESH_AFTER_LOGIN));
        }
        finish();
    }

    private void showBtnSignInLoading() {
        btnSignIn.setText("");
        btnSignInAnimator.start();
        ivSignInLoading.setVisibility(View.VISIBLE);
        btnSignUpTag.setEnabled(false);
        btnSignInTag.setEnabled(false);
        btnSignIn.setClickable(false);
        ipvAccount.setEnabled(false);
        ipvPsd.setEnabled(false);
    }

    private void hideBtnSignInLoading() {
        btnSignIn.setText(R.string.submit);
        ivSignInLoading.setVisibility(View.GONE);
        btnSignInAnimator.pause();
        btnSignUpTag.setEnabled(true);
        btnSignInTag.setEnabled(true);
        btnSignIn.setClickable(true);
        ipvAccount.setEnabled(true);
        ipvPsd.setEnabled(true);
    }

    private void showBtnSignUpLoading() {
        btnSignUp.setText("");
        ivSignUpLoading.setVisibility(View.VISIBLE);
        btnSignUpAnimator.start();
    }

    private void hideBtnSignUpLoading() {
        btnSignUp.setText(R.string.submit);
        ivSignUpLoading.setVisibility(View.GONE);
        btnSignUpAnimator.pause();
    }

    private void setBtnSignInAnimator(){
        btnSignInAnimator = ObjectAnimator.ofFloat(ivSignInLoading, "Rotation", 0, 360);
        btnSignInAnimator.setDuration(2000);
        btnSignInAnimator.setRepeatCount(-1);
    }

    private void setBtnSignUpAnimator(){
        btnSignUpAnimator = ObjectAnimator.ofFloat(ivSignUpLoading, "Rotation", 0, 360);
        btnSignUpAnimator.setDuration(2000);
        btnSignUpAnimator.setRepeatCount(-1);
    }

    @Override
    public void notifySignInResult(String msg, int code) {
        if (isDestroyed()) {
            return;
        }
        hideBtnSignInLoading();
        if (SUCCESS.equals(msg)) {
            initGlobalData();
            TuyaWrapper.onLogin();
            NooieDeviceHelper.initNativeConnect();
            NooieDeviceHelper.sendBroadcast(NooieApplication.mCtx, SDKConstant.ACTION_UPDATE_GLOBAL_DATA_FOR_LOGIN, null);
            DeviceConnectionHelper.getInstance().startCheckDeviceConnection(GlobalData.getInstance().getUid());
            hideLoadingDialog();
            goBackToHome(false);
        } else if (ConstantValue.ERROR.equalsIgnoreCase(msg)) {
            if (ipvAccount == null || TextUtils.isEmpty(ipvAccount.getInputText())) {
                return;
            }
            String account = ipvAccount.getInputText();
            String password = ipvPsd.getInputText();
            boolean isClearTask = getCurrentIntent() != null && getCurrentIntent().hasExtra(ConstantValue.INTENT_KEY_DATA_PARAM) ? getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_DATA_PARAM, false) : false;
            TwoAuthLoginActivity.toTwoAuthLoginActivity(this, ConstantValue.REQUEST_CODE_FOR_TWO_AUTH_LOGIN, account, password, CountryUtil.getCurrentCountry(NooieApplication.mCtx), true, isClearTask);
            hideLoadingDialog();
        } else {
            ToastUtil.showToast(SignInActivity.this, msg);
            MyAccountHelper.getInstance().logout();
            hideLoadingDialog();
        }

        isSignining = false;
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
    public void onLoadAccountHistorySuccess(List<UserInfoEntity> result) {
        if (isDestroyed() || checkNull(accountAdapter, ipvAccount, ipvPsd)) {
            return;
        }
        if (CollectionUtil.isNotEmpty(result) && accountAdapter != null && ipvAccount != null) {
            String account = result.get(0) != null ? result.get(0).getAccount() : "";
            String psd = result.get(0) != null ? result.get(0).getPsd() : "";
            if (ipvAccount != null && TextUtils.isEmpty(ipvAccount.getInputText()) && !TextUtils.isEmpty(account)) {
                ipvAccount.setEtInputText(account);
                ipvAccount.setEtSelection(account.length());
                if (!TextUtils.isEmpty(psd) && ipvPsd != null) {
                    ipvPsd.setEtInputText(psd);
                    ipvPsd.setEtSelection(psd.length());
                }
            }

            List<String> accounts = new ArrayList<>();
            for (UserInfoEntity userInfoEntity : CollectionUtil.safeFor(result)) {
                accounts.add(userInfoEntity.getAccount());
            }
            accountAdapter.setData(accounts);
            mDropListItemHeight = accountAdapter.getSimpleItemHeight();
            accountAdapter.setOnFilterResultsListener(new AutoCompleteAdapter.OnFilterResultsListener() {
                @Override
                public void onFilterResultsListener(int count) {
                    int curCount = count;
                    if (count > MAX_ONCE_MATCHED_ITEM) {
                        curCount = MAX_ONCE_MATCHED_ITEM;
                    }
                    if (curCount != prevCount) {
                        prevCount = curCount;
                        if (ipvAccount.getEtInput() != null) {
                            ipvAccount.getEtInput().setDropDownHeight(mDropListItemHeight * curCount);
                        }
                    }
                }
            });
            if (ipvAccount.getEtInput() != null) {
                ipvAccount.getEtInput().setAdapter(accountAdapter);
            }
        }
    }

    private String mSentRegisterAccount = null;

    @Override
    public void notifySendRegisterVerifyCode(int sendCodeType, String result, int code, String account) {
        if (isDestroyed()) {
            return;
        }
        hideLoadingDialog();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            ToastUtil.showToast(this, R.string.input_verify_code_sended);
            mSentRegisterAccount = account;
            ipvVerifyCode.setIpvFocusable(true);
        } else {
            if (code == StateCode.ACCOUNT_EXIST.code) {
                setPrivacyOfAccountMovingType(PRIVACY_OF_ACCOUNT_MOVING_TYPE_SIGN_UP);
                if (!checkPrivacyIsReadByAccount(account)) {
                    hideInputMethod();
                    if (mSignInPresenter != null) {
                        showLoading();
                        mSignInPresenter.checkAccountSourceForRegister(account);
                    }
                    return;
                }
                ToastUtil.showToast(this, R.string.sign_up_account_exist);
                //showSignUpTipDialog();
            } else if (code == StateCode.ACCOUNT_FORMAT_ERROR.code) {
                ToastUtil.showToast(this, R.string.camera_share_account_invalid);
            }
            mRegisterState = REGISTER_STATE_NORMAL;
            showCountDownTv("");
            if (sendCodeType == SEND_CODE_AUTO_TYPE) {
                mIsAutoSendCode = true;
            }
        }
    }

    @Override
    public void notifyRegisterVerifyCodeLimitTime(String result) {
        if (isDestroyed()) {
            return;
        }
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            mRegisterState = REGISTER_STATE_RESENT;
        } else {
            mRegisterState = REGISTER_STATE_COUNT_DOWN;
        }
        showCountDownTv(result + "s");
    }

    private void showCountDownTv(String result) {
        if (checkNull(tvCountdown)) {
            return;
        }
        switch (mRegisterState) {
            case REGISTER_STATE_NORMAL:
                /*tvCountdown.setBackgroundResource(R.drawable.button_black_state_list_radius_13);
                tvCountdown.setTextColor(CompatUtil.getColor(NooieApplication.mCtx, R.color.theme_white));*/
                tvCountdown.setText(R.string.sign_in_send);
                tvCountdown.setTextColor(CompatUtil.getColor(NooieApplication.mCtx, R.color.theme_green_subtext_color));
                break;
            case REGISTER_STATE_COUNT_DOWN:
                 /*tvCountdown.setBackground(null);
                tvCountdown.setTextColor(CompatUtil.getColor(NooieApplication.mCtx, R.color.theme_text_color));*/
                tvCountdown.setText(result);
                tvCountdown.setTextColor(CompatUtil.getColor(NooieApplication.mCtx, R.color.theme_green_subtext_color));
                break;
            case REGISTER_STATE_RESENT:
                /*tvCountdown.setBackgroundResource(R.drawable.button_black_state_list_radius_13);
                tvCountdown.setTextColor(CompatUtil.getColor(NooieApplication.mCtx, R.color.theme_white));*/
                tvCountdown.setText(R.string.sign_in_resend);
                tvCountdown.setTextColor(CompatUtil.getColor(NooieApplication.mCtx, R.color.theme_green_subtext_color));
                break;
        }
    }

    @Override
    public void notifyCheckVerifyCodeResult(String result, int code) {
        if (isDestroyed()) {
            return;
        }
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            SetPasswordActivity.toSetPasswordActivity(SignInActivity.this, ipvRegisterAccount.getInputText(), ipvVerifyCode.getInputText(), mCountry.getNumber(), ConstantValue.USER_REGISTER_VERIFY);
        } else {
            String errorMsg = getString(R.string.network_error0);
            if (code == StateCode.ACCOUNT_EXIST.code) {
                errorMsg = getString(R.string.sign_up_account_exist);
            } else if (code == StateCode.ACCOUNT_FORMAT_ERROR.code) {
                errorMsg = getString(R.string.camera_share_account_invalid);
            } else if (code == StateCode.SEND_VERIFY_FAILED.code) {
                errorMsg = getString(R.string.sign_in_verify_code_incorrect);
            }
            ToastUtil.showToast(this, errorMsg);
        }
        hideLoadingDialog();
        hideBtnSignUpLoading();
    }

    @Override
    public void onCheckAccountSourceForSignIn(String account, String password) {
        if (isDestroyed()) {
            return;
        }
        hideBtnSignInLoading();
        hideLoadingDialog();
        tryToSignIn();
    }

    @Override
    public void onCheckAccountSourceForRegister(String account) {
        if (isDestroyed()) {
            return;
        }
        hideLoadingDialog();
        ToastUtil.showToast(this, R.string.sign_up_account_exist);
    }

    @Override
    public void onCheckAccountSource(int state, boolean isOtherBrand, String brand, boolean isSignIn) {
        if (isDestroyed()) {
            return;
        }
        hideLoadingDialog();
        hideBtnSignInLoading();
        if (state == SDKConstant.SUCCESS) {
            checkAccountSourceOnSuccess(isOtherBrand, brand, isSignIn);
        } else {
            ToastUtil.showToast(this, getString(R.string.network_error0));
        }
    }

    private void tryToSendCode(int sendCodeType) {
        NooieLog.d("-->> debug SignInActivity tryToSendCode: 1000 mCurrentType=" + mCurrentType + " mRegisterState" + mRegisterState + " mRegisterState" + mIsAutoSendCode);
        if (mCurrentType != SIGN_UP_TYPE) {
            return;
        }
        NooieLog.d("-->> debug SignInActivity tryToSendCode: 1001");
        switch (mRegisterState) {
            case REGISTER_STATE_NORMAL:
            case REGISTER_STATE_RESENT:
                if (TextUtils.isEmpty(ipvRegisterAccount.getInputText())) {
                    NooieLog.d("-->> debug SignInActivity tryToSendCode: 1002");
                    ToastUtil.showToast(this, R.string.sign_in_account_empty);
                } else {
                    NooieLog.d("-->> debug SignInActivity tryToSendCode: 1004");
                    showLoadingDialog();
                    if (mSignInPresenter != null) {
                        NooieLog.d("-->> debug SignInActivity tryToSendCode: 1005");
                        mIsAutoSendCode = false;
                        mSignInPresenter.sendRegisterVerifyCode(sendCodeType, ipvRegisterAccount.getInputText(), mCountry.getNumber());
                    }
                    if (mRegisterState == REGISTER_STATE_RESENT) {
                        EventTrackingApi.getInstance().trackNormalEvent(EventDictionary.EVENT_ID_CLICK_RESEND_VERIFY_CODE);
                    }
                }
                break;
        }
    }

    private void checkIsResetSentCode(String account) {
        if (mCurrentType != SIGN_UP_TYPE || !checkSentAccountChange(account) || mSignInPresenter == null) {
            return;
        }
        if (mRegisterState == REGISTER_STATE_COUNT_DOWN) {
            mSignInPresenter.stopVerifyCodeCounter();
        }
        mRegisterState = REGISTER_STATE_NORMAL;
        showCountDownTv("");
        mIsAutoSendCode = true;
    }

    private boolean checkSentAccountChange(String account) {
        return !TextUtils.isEmpty(mSentRegisterAccount) && !mSentRegisterAccount.equals(account);
    }

    private void showSignUpTipDialog() {
        hideSignUpTipDialog();
        mSignUpTipDialog = DialogUtils.showInformationDialog(this, getString(R.string.dialog_tip_title), getString(R.string.sign_up_dialog_tip_account_exist_content), getString(R.string.bluetooth_scan_operation_tip_unbind_confirm), true, false, new DialogUtils.OnClickInformationDialogLisenter() {
            @Override
            public void onConfirmClick() {
            }
        });
    }

    private void hideSignUpTipDialog() {
        if (mSignUpTipDialog != null) {
            mSignUpTipDialog.dismiss();
            mSignUpTipDialog = null;
        }
    }


    private void setupPrivacyDialogForAccountMoving() {
        if (isDestroyed() || checkNull(wdvSignIn)) {
            return;
        }
        wdvSignIn.setListener(new WebViewDialogView.WebViewDialogListener() {
            @Override
            public void onCancel() {
                hidePrivacyDialogForAccountMoving();
            }

            @Override
            public void onConfirm() {
                dealOnConfirmPrivacyOfAccountMoving(mPrivacyOfAccountMovingType, getAccountByPrivacyOfAccountMovingType(mPrivacyOfAccountMovingType));
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
        if (isDestroyed() || checkNull(wdvSignIn)) {
            return;
        }
        wdvSignIn.setVisibility(View.VISIBLE);
        wdvSignIn.loadContent(CommonUtil.getPrivacyPolicyByCountry(NooieApplication.mCtx, countryCode));
    }

    private void hidePrivacyDialogForAccountMoving() {
        if (isDestroyed() || checkNull(wdvSignIn)) {
            return;
        }
        wdvSignIn.setVisibility(View.GONE);
    }

    private String getSelectCountryCode() {
        String countryCode = mCountry != null && !TextUtils.isEmpty(mCountry.getNumber()) ? mCountry.getNumber() : CountryUtil.getCurrentCountry(NooieApplication.mCtx);
        return countryCode;
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

    private void setPrivacyOfAccountMovingType(int type) {
        mPrivacyOfAccountMovingType = type;
    }

    private void dealOnConfirmPrivacyOfAccountMoving(int type, String account) {
        if (isDestroyed() || checkNull(ipvAccount, ipvRegisterAccount, btnSignInTag) || TextUtils.isEmpty(account)) {
            return;
        }
        GlobalPrefs.setPrivacyIsReadByAccount(account, true);
        hidePrivacyDialogForAccountMoving();
        switch (type) {
            case PRIVACY_OF_ACCOUNT_MOVING_TYPE_SIGN_IN: {
                tryToSignIn();
                break;
            }
            case PRIVACY_OF_ACCOUNT_MOVING_TYPE_SIGN_UP: {
                onViewClicked(btnSignInTag);
                break;
            }
        }
    }

    private String getAccountByPrivacyOfAccountMovingType(int type) {
        if (isDestroyed() || checkNull(ipvAccount, ipvRegisterAccount)) {
            return "";
        }
        String account = "";
        switch (type) {
            case PRIVACY_OF_ACCOUNT_MOVING_TYPE_SIGN_IN: {
                account = ipvAccount.getInputText();
                break;
            }
            case PRIVACY_OF_ACCOUNT_MOVING_TYPE_SIGN_UP: {
                account = ipvRegisterAccount.getInputText();
                break;
            }
        }
        return account;
    }

    private void tryToSignIn() {
        if (isSignining || mCurrentType != SIGN_IN_TYPE) {
            return;
        }

        if (TextUtils.isEmpty(ipvAccount.getInputText()) || TextUtils.isEmpty(ipvAccount.getInputText())) {
            String msg = TextUtils.isEmpty(ipvAccount.getInputText()) ? getResources().getString(R.string.sign_in_account_empty) : getResources().getString(R.string.sign_in_password_empty);
            notifySignInResult(msg, StateCode.UNKNOWN.code);
            return;
        }

        isSignining = true;
        hideInputMethod();
        NotificationManager.getInstance().cancelAllNotifications();
        if (mSignInPresenter != null) {
            showBtnSignInLoading();
            showLoadingDialog();
            mSignInPresenter.signIn(ipvAccount.getInputText(), ipvPsd.getInputText());
        }
    }

    private void checkAccountSourceOnSuccess(boolean isOtherBrand, String brand, boolean isSignIn) {
        if (isDestroyed()) {
            return;
        }
        if (isOtherBrand) {

            showAccountMigrationDialog(brand, isSignIn);
        }
    }

    private void showAccountMigrationDialog(String brand, boolean isSignIn) {
        hideAccountMigrationDialog();
        String content = String.format(getString(R.string.sign_account_migration_tip_content), brand);
        mAccountMigrationDialog = DialogUtils.showConfirmWithSubMsgDialog(this, getString(R.string.sign_account_migration_tip_title), content, getString(R.string.cancel_normal), getString(R.string.privacy_policy), new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickLeft() {
            }

            @Override
            public void onClickRight() {
                setPrivacyOfAccountMovingType(isSignIn ? PRIVACY_OF_ACCOUNT_MOVING_TYPE_SIGN_IN : PRIVACY_OF_ACCOUNT_MOVING_TYPE_SIGN_UP);
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

    @Override
    public String getEventId(int trackType) {
        return EventDictionary.EVENT_ID_ACCESS_LOGIN_PAGE;
    }
}
