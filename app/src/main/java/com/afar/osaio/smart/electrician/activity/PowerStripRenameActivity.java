package com.afar.osaio.smart.electrician.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.electrician.eventbus.PlugRenameEvent;
import com.afar.osaio.smart.electrician.presenter.IPowerStripRenamePresenter;
import com.afar.osaio.smart.electrician.presenter.PowerStripRenamePresenter;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.view.IPowerStripRenameView;
import com.afar.osaio.smart.electrician.widget.InputFrameView;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.FButton;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * PowerStripRenameActivity
 *
 * @author Administrator
 * @date 2019/6/27
 */
public class PowerStripRenameActivity extends BaseActivity implements IPowerStripRenameView {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivIcon)
    ImageView ivIcon;
    @BindView(R.id.tvName)
    TextView tvName;
    @BindView(R.id.ipvDeviceName)
    InputFrameView ipvDeviceName;
    @BindView(R.id.btnRename)
    FButton btnRename;

    private IPowerStripRenamePresenter mPowerStripPresenter;
    private String mDeviceId;
    private String mDpId;
    // 0: 排插命名 1：排插插孔命名
    private int mRenameType;
    private String mNickName;

    public static void toPowerStripRenameActivity(Activity from, String deviceId, String nickname, String dpId, int renameType) {
        Intent intent = new Intent(from, PowerStripRenameActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_NAME, nickname);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_DP_ID, dpId);
        intent.putExtra(ConstantValue.INTENT_KEY_POWER_SZTRIP_NAME_TYPE, renameType);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_power_strip_rename);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        if (mRenameType == ConstantValue.POWER_STRIP_RENAME) {
            tvTitle.setText(R.string.name_your_power_strip);
            ivIcon.setImageResource(R.drawable.power_strip_name);
        } else if (mRenameType == ConstantValue.POWER_STRIP_PLUG_RENAME) {
            tvTitle.setText(R.string.name_your_plugs);
            ivIcon.setImageResource(R.drawable.device_setting_plug_icon);
        }

        tvName.setText(mNickName);
        ipvDeviceName.setInputTitle(getResources().getString(R.string.name))
                .setInputBtn(R.drawable.close_icon_state_list)
                .setEtInputType(InputType.TYPE_CLASS_TEXT)
                .setInputBtnIsShow(true)
                .setInputTitleVisible(View.GONE)
                .setEtInputGravity(Gravity.CENTER)
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
                        ipvDeviceName.setEtInputText("");
                    }

                    @Override
                    public void onEditorAction() {
                    }
                });

        checkButton();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
            return;
        } else {
            mDeviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
            mDpId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_DP_ID);
            mNickName = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_NAME);
            mRenameType = getCurrentIntent().getIntExtra(ConstantValue.INTENT_KEY_POWER_SZTRIP_NAME_TYPE, 0);
            initView();
            mPowerStripPresenter = new PowerStripRenamePresenter(this);
            ipvDeviceName.setEtInputText(mNickName);

        }
    }

    private void checkButton() {
        if (!TextUtils.isEmpty(ipvDeviceName.getInputText().trim())) {
            btnRename.setEnabled(true);
        } else {
            btnRename.setEnabled(false);
        }
    }

    @OnClick({R.id.ivLeft, R.id.btnRename})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft: {
                finish();
                break;
            }
            case R.id.btnRename: {
                if (mRenameType == ConstantValue.POWER_STRIP_RENAME) {
                    if (ipvDeviceName.getInputText().toString().length() > 50) {
                        ToastUtil.showToast(PowerStripRenameActivity.this, getString(R.string.name_too_long));
                    } else {
                        mPowerStripPresenter.renamePowerStrip(mDeviceId, ipvDeviceName.getInputText());
                    }
                } else if (mRenameType == ConstantValue.POWER_STRIP_PLUG_RENAME) {
                    if (ipvDeviceName.getInputText().toString().length() > 30) {
                        ToastUtil.showToast(PowerStripRenameActivity.this, getString(R.string.name_too_long));
                    } else {
                        mPowerStripPresenter.renameDevice(mDeviceId, mDpId, ipvDeviceName.getInputText());
                    }
                }
                break;
            }
        }
    }

    @Override
    public void notifyRenameDeviceState(String msg) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(msg)) {
            ToastUtil.showToast(this, getResources().getString(R.string.success));
            EventBus.getDefault().post(new PlugRenameEvent(true, mDpId, ipvDeviceName.getInputText()));
            finish();
        } else {
            ErrorHandleUtil.toastTuyaError(this, msg);
        }
    }
}
