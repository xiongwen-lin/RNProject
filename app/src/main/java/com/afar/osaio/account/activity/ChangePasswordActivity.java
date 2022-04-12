package com.afar.osaio.account.activity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.account.presenter.ChangedPasswordPresenterImpl;
import com.afar.osaio.account.presenter.IChangedPasswordPresenter;
import com.afar.osaio.account.view.IChangePasswordView;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.InputFrameView;
import com.apemans.yrcxsdk.data.YRCXSDKDataManager;
import com.nooie.common.base.GlobalData;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by victor on 2018/7/10
 * Email is victor.qiao.0604@gmail.com
 */
public class ChangePasswordActivity extends BaseActivity implements IChangePasswordView {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.ipvOldPsd)
    InputFrameView ipvOldPsd;
    @BindView(R.id.ipvNewPsd)
    InputFrameView ipvNewPsd;
    @BindView(R.id.ipvConfirmPsd)
    InputFrameView ipvConfirmPsd;
    @BindView(R.id.btnConfirm)
    Button btnConfirm;
    @BindView(R.id.ivConfirmLoading)
    ImageView ivConfirmLoading;

    private IChangedPasswordPresenter mChangePsdPresenter;
    private ObjectAnimator doneAnimator = null;

    public static void toChangePasswordActivity(Context from) {
        Intent intent = new Intent(from, ChangePasswordActivity.class);
        from.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        ButterKnife.bind(this);

        initView();
        initData();
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

    private void setBtnDownAnimator() {
        doneAnimator = ObjectAnimator.ofFloat(ivConfirmLoading, "Rotation", 0, 360);
        doneAnimator.setDuration(2000);
        doneAnimator.setRepeatCount(-1);
    }

    private void showBtnDoneLoading() {
        ivLeft.setEnabled(false);
        ipvConfirmPsd.setEnabled(false);
        ipvOldPsd.setEnabled(false);
        ipvNewPsd.setEnabled(false);
        btnConfirm.setClickable(false);
        btnConfirm.setText("");
        ivConfirmLoading.setVisibility(View.VISIBLE);
        doneAnimator.start();
    }

    private void hideBtnDoneLoading() {
        btnConfirm.setText(R.string.submit);
        ivConfirmLoading.setVisibility(View.GONE);
        doneAnimator.pause();
        ivLeft.setEnabled(true);
        ipvConfirmPsd.setEnabled(true);
        ipvOldPsd.setEnabled(true);
        ipvNewPsd.setEnabled(true);
        btnConfirm.setClickable(true);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    private void initData() {
        mChangePsdPresenter = new ChangedPasswordPresenterImpl(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.account_change_password);
        ivRight.setVisibility(View.GONE);
        setBtnDownAnimator();
        setupInputFrameView();
    }

    private void setupInputFrameView() {
        ipvOldPsd.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT).setInputTitle(getString(R.string.account_old_password))
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

        ipvNewPsd.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT).setInputTitle(getString(R.string.account_new_password))
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

        ipvConfirmPsd.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT).setInputTitle(getString(R.string.account_confirm_password))
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
                        onViewClicked(btnConfirm);
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

    public void checkBtnEnable() {
        ipvOldPsd.setWrongTextVisible(View.GONE).setEtInputBackground(R.drawable.osaio_input_bg);
        ipvConfirmPsd.setWrongTextVisible(View.GONE).setEtInputBackground(R.drawable.osaio_input_bg);
        if (!TextUtils.isEmpty(ipvOldPsd.getInputText()) && !TextUtils.isEmpty(ipvNewPsd.getInputText()) && !TextUtils.isEmpty(ipvConfirmPsd.getInputText())) {
            btnConfirm.setEnabled(true);
            btnConfirm.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
        } else {
            btnConfirm.setEnabled(false);
            btnConfirm.setTextColor(getResources().getColor(R.color.unable_clickable_color));
        }
    }

    @Override
    public void notifyChangePsdState(String result) {
        if (isDestroyed()) {
            return;
        }
        hideBtnDoneLoading();
        if (result.equals(ConstantValue.SUCCESS)) {
            ToastUtil.showLongToast(this, R.string.account_change_psd_success);
            // 更新YRCXSDK 和 YRBusiness 模块中的缓存信息
            YRCXSDKDataManager.INSTANCE.setUserPassword(ipvConfirmPsd.getInputText());
            //AccountHelper.getInstance().logout();
            //SignInActivity.toSignInActivity(this);
            finish();
        } else {
            ToastUtil.showLongToast(this, R.string.get_fail);
        }
    }

    @OnClick({R.id.ivLeft, R.id.btnConfirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnConfirm:
                if (!GlobalData.getInstance().getPassword().equals(ipvOldPsd.getInputText())) {
                    ipvOldPsd.setWrongTextVisible(View.VISIBLE).setWrongText(getResources().getString(R.string.account_old_password_incorrect)).setEtInputBackground(R.drawable.input_background_wrong);
                    //ToastUtil.showLongToast(this, R.string.account_old_password_incorrect);
                } else if (ipvNewPsd.getInputText().length() < 6 || ipvConfirmPsd.getInputText().length() < 6) {
                    ipvConfirmPsd.setWrongTextVisible(View.VISIBLE).setWrongText(getResources().getString(R.string.account_psd_not_less_six)).setEtInputBackground(R.drawable.input_background_wrong);
                    //ToastUtil.showLongToast(this, R.string.account_psd_not_less_six);
                } else if (ipvNewPsd.getInputText().length() > 12 || ipvConfirmPsd.getInputText().length() > 12) {
                    ipvConfirmPsd.setWrongTextVisible(View.VISIBLE).setWrongText(getResources().getString(R.string.account_password_limit_num)).setEtInputBackground(R.drawable.input_background_wrong);
                    //ToastUtil.showLongToast(this, R.string.account_password_limit_num);
                } else if (!ipvNewPsd.getInputText().equals(ipvConfirmPsd.getInputText())) {
                    ipvConfirmPsd.setWrongTextVisible(View.VISIBLE).setWrongText(getResources().getString(R.string.account_change_psd_new_confirm_not_same)).setEtInputBackground(R.drawable.input_background_wrong);
                    //ToastUtil.showLongToast(this, R.string.account_change_psd_new_confirm_not_same);
                } else if (ipvOldPsd.getInputText().equals(ipvNewPsd.getInputText())) {
                    ipvConfirmPsd.setWrongTextVisible(View.VISIBLE).setWrongText(getResources().getString(R.string.account_change_psd_old_new_same)).setEtInputBackground(R.drawable.input_background_wrong);
                    //ToastUtil.showLongToast(this, R.string.account_change_psd_old_new_same);
                } else {
                    showBtnDoneLoading();
                    mChangePsdPresenter.changePassword(mUserAccount, ipvOldPsd.getInputText(), ipvNewPsd.getInputText());
                }
                break;
        }
    }
}
