package com.afar.osaio.smart.electrician.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.electrician.manager.FamilyManager;
import com.afar.osaio.smart.electrician.presenter.ISingleDeviceSharePresenter;
import com.afar.osaio.smart.electrician.presenter.SingleDeviceSharePresenter;
import com.afar.osaio.smart.electrician.util.CommonUtil;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.view.ISingleDeviceShareView;
import com.afar.osaio.smart.electrician.widget.InputFrameView;
import com.afar.osaio.smart.setting.activity.CountryListActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.FButton;
import com.nooie.common.utils.configure.CountryUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * SingleDeviceShareActivity
 *
 * @author Administrator
 * @date 2019/3/13
 */
public class SingleDeviceShareActivity extends BaseActivity implements ISingleDeviceShareView {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvAddMemberTip)
    TextView tvAddMemberTip;
    @BindView(R.id.ipvName)
    InputFrameView ipvName;
    @BindView(R.id.ipvEmail)
    InputFrameView ipvEmail;
    @BindView(R.id.ipvConfirmEmail)
    InputFrameView ipvConfirmEmail;
    @BindView(R.id.btnDone)
    FButton btnDone;
    @BindView(R.id.countrySelectName)
    TextView countrySelectName;
    @BindView(R.id.signUpSelectCountry)
    View signUpSelectCountry;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;

    private String mCountryCode;

    private ISingleDeviceSharePresenter mSingleDeviceSharePresenter;

    private long lastClickTime = 0;

    public static void toSingleDeviceShareActivity(Activity from, String deviceId, long homeId, String deviceName, int requestCode) {
        Intent intent = new Intent(from, SingleDeviceShareActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_HOME_ID, homeId);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_NAME, deviceName);
        from.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);
        ButterKnife.bind(this);

        initView();
        initData();

        initDoneListener();

    }

    private void initDoneListener() {
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = System.currentTimeMillis();
                if (now - lastClickTime > 3000) {
                    lastClickTime = now;
                    doDone();
                }
            }
        });
    }

    private void doDone() {
        if (!CommonUtil.checkEmail(ipvEmail.getInputText())) {
            ToastUtil.showToast(SingleDeviceShareActivity.this, R.string.email_address_is_not_valid);
        } else if (!ipvEmail.getInputText().trim().equals(ipvConfirmEmail.getInputText().trim())) {
            ToastUtil.showToast(SingleDeviceShareActivity.this, R.string.email_not_match);
        } else {
            mSingleDeviceSharePresenter.getUidByAccount(ipvEmail.getInputText().trim());
        }
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        ipvName.setVisibility(View.GONE);
        signUpSelectCountry.setVisibility(View.VISIBLE);
        tvTitle.setText(R.string.dev_sharing);
        btnDone.setText(R.string.send_invitation);

        ipvEmail.setInputTitle(getResources().getString(R.string.feedback_email))
                .setEtInputToggle(false)
                .setInputBtn(R.drawable.close_icon_state_list)
                .setInputTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        checkButton();
                    }
                })
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                        ipvEmail.setEtInputText("");
                    }

                    @Override
                    public void onEditorAction() {
                        hideInputMethod();
                    }
                });

        ipvConfirmEmail.setInputTitle(getResources().getString(R.string.confirm_email))
                .setEtInputToggle(false)
                .setInputBtn(R.drawable.close_icon_state_list)
                .setInputTextChangeListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        checkButton();
                    }
                })
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                        ipvConfirmEmail.setEtInputText("");
                    }

                    @Override
                    public void onEditorAction() {
                        hideInputMethod();
                        if (!TextUtils.isEmpty(ipvEmail.getInputText().trim()) && !TextUtils.isEmpty(ipvConfirmEmail.getInputText().trim()) && ipvEmail.getInputText().trim().equals(ipvConfirmEmail.getInputText().trim())) {
                            onViewClick(btnDone);
                        }
                    }
                });

        checkButton();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
        } else {
            tvAddMemberTip.setText(String.format(getResources().getString(R.string.share_dev_detail_info), getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_NAME)));
            mSingleDeviceSharePresenter = new SingleDeviceSharePresenter(this);
            setupCountrySelect();
        }
    }

    public void setupCountrySelect() {
        countrySelectName.setText(CountryUtil.getCurrentCountryTitle(NooieApplication.mCtx));
        mCountryCode = CountryUtil.getCurrentCountry(NooieApplication.mCtx);
    }

    public void checkButton() {
        if (!TextUtils.isEmpty(ipvEmail.getInputText().trim()) && !TextUtils.isEmpty(ipvConfirmEmail.getInputText().trim())) {
            btnDone.setEnabled(true);
            btnDone.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
        } else {
            btnDone.setEnabled(false);
            btnDone.setTextColor(getResources().getColor(R.color.unable_clickable_color));
        }
    }

    @OnClick({R.id.ivLeft, R.id.countrySelectName})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                finish();
                break;
            }
            case R.id.countrySelectName:
                CountryListActivity.toCountryListActivity(SingleDeviceShareActivity.this, ConstantValue.REQUEST_CODE_SELECT_COUNTRY);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ConstantValue.REQUEST_CODE_SELECT_COUNTRY: {
                    mCountryCode = data != null ? data.getStringExtra(ConstantValue.INTENT_KEY_PHONE_CODE) : CountryUtil.getCurrentCountry(NooieApplication.mCtx);
                    String countryName = data != null ? data.getStringExtra(ConstantValue.INTENT_KEY_COUNTRY_NAME) : CountryUtil.getCurrentCountryTitle(NooieApplication.mCtx);
                    countrySelectName.setText(countryName);
                    break;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void notifyGetUidSuccess(String uid) {
        String deviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
        long homeId = getCurrentIntent().getLongExtra(ConstantValue.INTENT_KEY_HOME_ID, FamilyManager.getInstance().getCurrentHomeId());
        if (!TextUtils.isEmpty(deviceId) && !TextUtils.isEmpty(uid)) {
            List<String> deviceIds = new ArrayList<>();
            deviceIds.add(deviceId);
            mSingleDeviceSharePresenter.shareDevices(homeId, mCountryCode, uid, deviceIds);
        } else {
            ToastUtil.showToast(this, R.string.get_fail);
        }
    }

    @Override
    public void notifyGetUidFailed(String msg) {
        ErrorHandleUtil.toastTuyaError(this, msg);
    }

    @Override
    public void notifyUserNotRegister() {
        ToastUtil.showToast(this, getResources().getString(R.string.shared_send_user_not_exist1));
    }

    @Override
    public void notifySharedDeviceState(String msg) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(msg)) {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        } else {
            ErrorHandleUtil.toastTuyaError(this, msg);
        }
    }
}
