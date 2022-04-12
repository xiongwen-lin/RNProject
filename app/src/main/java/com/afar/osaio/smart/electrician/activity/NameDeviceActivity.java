package com.afar.osaio.smart.electrician.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.application.activity.HelpActivity;
import com.afar.osaio.application.activity.ThirdSkillActivity;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.electrician.presenter.INameDevicePresenter;
import com.afar.osaio.smart.electrician.presenter.NameDevicePresenter;
import com.afar.osaio.smart.electrician.util.DialogUtil;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.view.INameDeviceView;
import com.afar.osaio.smart.electrician.widget.InputFrameView;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.FButton;
import com.nooie.common.utils.log.NooieLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * NameDeviceActivity
 *
 * @author Administrator
 * @date 2019/3/6
 */
public class NameDeviceActivity extends BaseActivity implements INameDeviceView {
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.editNameContainer)
    View editNameContainer;
    @BindView(R.id.ivDeviceIcon)
    ImageView ivDeviceIcon;
    @BindView(R.id.ipvDeviceName)
    InputFrameView ipvDeviceName;
    @BindView(R.id.btnRename)
    FButton btnRename;

    private String mName;
    private String mDeviceId;
    private String productId;
    private INameDevicePresenter mAddDevicePresenter;
    private String mAddType;

    public static void toNameDeviceActivity(Context from, String deviceId, String addType,String productId) {
        Intent intent = new Intent(from, NameDeviceActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ADD_TYPE, addType);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_PRODUCTID, productId);
        from.startActivity(intent);

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teckin_name_device);
        ButterKnife.bind(this);

        initData();
        initView();
    }

    private void initData() {
        String deviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
        mAddType = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ADD_TYPE);
        productId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_PRODUCTID);
        if (deviceId != null) {
            mDeviceId = deviceId;
        } else {
            finish();
        }
        NooieLog.d("NameDeviceActivity----deviceId="+deviceId+",productId="+productId);
        mAddDevicePresenter = new NameDevicePresenter(this, deviceId);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.rename_plug);
        ipvDeviceName.setEtInputHint(getResources().getString(R.string.name_your_device));
        if (mAddType.equals(ConstantValue.ADD_LAMP)) {
            ivDeviceIcon.setImageResource(R.drawable.light_bulb_icon_name);
            //tvTitle.setText(R.string.name_your_light);
            //ipvDeviceName.setEtInputHint(getResources().getString(R.string.name_your_light));
        } else if (mAddType.equals(ConstantValue.ADD_POWERSTRIP)) {
            ivDeviceIcon.setImageResource(R.drawable.power_strip_name);
            //tvTitle.setText(R.string.name_your_power_strip);
            //ipvDeviceName.setEtInputHint(getResources().getString(R.string.name_your_power_strip));
        } else if (mAddType.equals(ConstantValue.ADD_SWITCH)) {
            ivDeviceIcon.setImageResource(R.drawable.device_setting_switch_icon);
            //ipvDeviceName.setEtInputHint(getResources().getString(R.string.name_your_switch));
        } else if (mAddType.equals(ConstantValue.ADD_LIGHT_STRIP)) {
            ivDeviceIcon.setImageResource(R.drawable.device_setting_light_strip_icon);
            //ipvDeviceName.setEtInputHint(getResources().getString(R.string.name_your_light_strip));
        } else if (mAddType.equals(ConstantValue.ADD_LIGHT_MODULATOR)) {
            ivDeviceIcon.setImageResource(R.drawable.device_setting_modulator_icon);
            //ipvDeviceName.setEtInputHint(getResources().getString(R.string.name_your_modulator));
        } else if (mAddType.equals(ConstantValue.ADD_PET_FEEDER)) {
            ivDeviceIcon.setImageResource(R.drawable.ic_device_light_feeder);
            //ipvDeviceName.setEtInputHint(getResources().getString(R.string.name_your_pet_feeder));
        } else if (mAddType.equals(ConstantValue.ADD_AIR_PURIFIER)) {
            ivDeviceIcon.setImageResource(R.drawable.ic_device_purifier);
        } else {
            ivDeviceIcon.setImageResource(R.drawable.device_setting_plug_icon);
            //tvTitle.setText(R.string.name_your_plug);
            //ipvDeviceName.setEtInputHint(getResources().getString(R.string.name_your_plug));
        }
        ivRight.setVisibility(View.INVISIBLE);
        setupEditNameView();
    }

    private void setupEditNameView() {
        ipvDeviceName.setInputTitleVisible(View.GONE)
                .setInputBtn(R.drawable.close_icon_state_list)
                .setEtInputType(InputType.TYPE_CLASS_TEXT)
                .setInputBtnIsShow(true)
                .setEtInputGravity(Gravity.CENTER)
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                        ipvDeviceName.setEtInputText("");
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
        checkBtnEnable();
    }

    public void checkBtnEnable() {
        if (!TextUtils.isEmpty(ipvDeviceName.getInputText())) {
            btnRename.setEnabled(true);
            btnRename.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
        } else {
            btnRename.setEnabled(false);
            btnRename.setTextColor(getResources().getColor(R.color.unable_clickable_color));
        }
    }

    private DialogUtil.OnClickInputDialogListener inputDialogListener = new DialogUtil.OnClickInputDialogListener() {
        @Override
        public void onClickCancel() {
        }

        @Override
        public void onClickSave(String text) {
            mName = text;
            addNewDevice(mName);
        }
    };

    @OnClick({R.id.ivLeft, R.id.btnRename})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                finish();
                break;
            }
            case R.id.btnRename: {
                if (!TextUtils.isEmpty(ipvDeviceName.getInputText())) {
                    mName = ipvDeviceName.getInputText();
                    if (mName.length() > ConstantValue.MAX_DEVICE_NAME_LENGTH) {
                        ToastUtil.showToast(NameDeviceActivity.this, getString(R.string.name_too_long));
                    } else {
                        addNewDevice(ipvDeviceName.getInputText());
                    }
                } else {
                    ToastUtil.showToast(this, R.string.device_name_can_not_blank);
                }
                break;
            }
        }
    }


    private void setDrawableTop(TextView textView, int res) {
        if (res == -1 || textView == null) {
            return;
        }
        Drawable drawable = ContextCompat.getDrawable(this, res);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        textView.setCompoundDrawables(null, drawable, null, null);
    }


    private void addNewDevice(String name) {
        showLoadingDialog();
        mAddDevicePresenter.addNewDevice(name);
    }

    @Override
    public void onAddDevSuccess() {
        hideLoadingDialog();
        if (isPause()) {
            return;
        }
        ThirdSkillActivity.toThirdSkillActivity(this,productId);
     //   HomeActivity.toHomeActivity(NameDeviceActivity.this, HomeActivity.TYPE_ADD_DEVICE);
    }

    @Override
    public void onAddDevFailed(String error) {
        hideLoadingDialog();
        ErrorHandleUtil.toastTuyaError(this, error);
    }
}
