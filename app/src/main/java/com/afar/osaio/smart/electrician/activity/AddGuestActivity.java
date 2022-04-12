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
import com.afar.osaio.smart.electrician.presenter.AddGuestPresenter;
import com.afar.osaio.smart.electrician.presenter.IAddGuestPresenter;
import com.afar.osaio.smart.electrician.util.CommonUtil;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.view.IAddGuestView;
import com.afar.osaio.smart.electrician.widget.InputFrameView;
import com.afar.osaio.smart.setting.activity.CountryListActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.FButton;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.configure.CountryUtil;
import com.tuya.smart.home.sdk.bean.SharedUserInfoBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * AddGuestActivity
 *
 * @author Administrator
 * @date 2019/3/13
 */
public class AddGuestActivity extends BaseActivity implements IAddGuestView {

    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.tvAddMemberTip)
    TextView tvAddMemberTip;
    @BindView(R.id.ipvEmail)
    InputFrameView ipvEmail;
    @BindView(R.id.ipvConfirmEmail)
    InputFrameView ipvConfirmEmail;
    @BindView(R.id.btnDone)
    FButton btnDone;
    @BindView(R.id.countrySelectName)
    TextView countrySelectName;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;

    private IAddGuestPresenter mPresenter;

    private String mUid;
    private String mCountryCode;
    private String mHomeName;
    private long mHomeId;

    public static void toAddGuestActivity(Activity from, long homeId, String homeName, int requestCode) {
        Intent intent = new Intent(from, AddGuestActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_HOME_ID, homeId);
        intent.putExtra(ConstantValue.INTENT_KEY_HOME_NAME, homeName);
        intent.putExtra(ConstantValue.INTENT_KEY_ADD_MEMBER_CODE, requestCode);
        from.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_guest);
        ButterKnife.bind(this);

        initView();
        initData();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.add_guest);
        countrySelectName.setText(CountryUtil.getCurrentCountryTitle(NooieApplication.mCtx));

        setupCountrySelect();

        ipvEmail.setInputTitle(getResources().getString(R.string.feedback_email))
                .setInputBtn(R.drawable.close_icon_state_list)
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                        ipvEmail.setEtInputText("");
                    }

                    @Override
                    public void onEditorAction() {
                        hideInputMethod();
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

        ipvConfirmEmail.setInputTitle(getResources().getString(R.string.confirm_email))
                .setInputBtn(R.drawable.close_icon_state_list)
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                        ipvConfirmEmail.setEtInputText("");
                    }

                    @Override
                    public void onEditorAction() {
                        hideInputMethod();
                        onViewClicked(btnDone);
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

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
        } else {
            mHomeName = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_HOME_NAME);
            mHomeId = getCurrentIntent().getLongExtra(ConstantValue.INTENT_KEY_HOME_ID, 0);
        }

        mPresenter = new AddGuestPresenter(this);

        tvAddMemberTip.setText(String.format(getResources().getString(R.string.share_dev_detail_info), mHomeName));

    }

    public void setupCountrySelect() {
        countrySelectName.setText(CountryUtil.getCurrentCountryTitle(NooieApplication.mCtx));
        mCountryCode = CountryUtil.getCurrentCountry(NooieApplication.mCtx);
    }

    @OnClick({R.id.ivLeft, R.id.btnDone, R.id.countrySelectName})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                finish();
                break;
            }
            case R.id.btnDone: {
                if (!CommonUtil.checkEmail(ipvEmail.getInputText()) || !CommonUtil.checkEmail(ipvConfirmEmail.getInputText())) {
                    ToastUtil.showToast(this, R.string.email_address_is_not_valid);
                } else if (!ipvEmail.getInputText().equals(ipvConfirmEmail.getInputText())) {
                    ToastUtil.showToast(this, R.string.email_not_match);
                } else if (!TextUtils.isEmpty(ipvEmail.getInputText())) {
                    showLoadingDialog();
                    mPresenter.getUidByAccount(ipvEmail.getInputText());
                }
                break;
            }
            case R.id.countrySelectName: {
                CountryListActivity.toCountryListActivity(AddGuestActivity.this, ConstantValue.REQUEST_CODE_SELECT_COUNTRY);
                break;
            }
        }
    }

    @Override
    public void notifyAddMemberFailed(String msg, boolean isTuyaError) {
        hideLoadingDialog();
        if (isTuyaError) {
            ErrorHandleUtil.toastTuyaError(this, msg);
        } else {
            ToastUtil.showToast(this, R.string.get_fail);
        }
    }

    @Override
    public void notifyGetUidSuccess(String uid) {
        if (!TextUtils.isEmpty(uid)) {
            mUid = uid;
            mPresenter.loadHomeGuest(mHomeId);
        } else {
            ToastUtil.showToast(this, R.string.shared_send_user_not_exist);
        }
    }

    @Override
    public void notifyHomeGuestSuccess(List<SharedUserInfoBean> sharedUserInfoList) {
        hideLoadingDialog();
        boolean guestAdded = false;
        if (CollectionUtil.isEmpty(sharedUserInfoList)) {
            gotoSelectShareDeviceActivity();
        } else {
            for (SharedUserInfoBean userInfoBean : sharedUserInfoList) {
                if (userInfoBean.getUserName().equals(mUid)) {
                    guestAdded = true;
                    break;
                }
            }
            if (guestAdded) {
                ToastUtil.showToast(this, getResources().getString(R.string.have_added_guest));
            } else {
                gotoSelectShareDeviceActivity();
            }
        }
    }

    private void gotoSelectShareDeviceActivity() {
        SelectShareDeviceActivity.toSelectShareDeviceActivity(AddGuestActivity.this, ConstantValue.REQUEST_CODE_DEVICE_ADD, mHomeId, mCountryCode, null, mUid, true);
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void notifyHomeGuestFailed(String msg) {
        hideLoadingDialog();
        ToastUtil.showToast(this, R.string.get_fail);
    }

    @Override
    public void notifyUserNotRegister() {
        ToastUtil.showToast(this, getResources().getString(R.string.shared_send_user_not_exist));
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

    public void checkBtnEnable() {
        if (!TextUtils.isEmpty(ipvEmail.getInputText()) && !TextUtils.isEmpty(ipvConfirmEmail.getInputText())) {
            btnDone.setEnabled(true);
            btnDone.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
        } else {
            btnDone.setEnabled(false);
            btnDone.setTextColor(getResources().getColor(R.color.unable_clickable_color));
        }
    }
}

