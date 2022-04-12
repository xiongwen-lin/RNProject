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
import com.afar.osaio.smart.electrician.presenter.IRenameDevicePresenter;
import com.afar.osaio.smart.electrician.presenter.RenameDevicePresenter;
import com.afar.osaio.smart.electrician.util.ErrorHandleUtil;
import com.afar.osaio.smart.electrician.view.IRenameDeviceView;
import com.afar.osaio.smart.electrician.widget.InputFrameView;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.FButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * RenameDeviceActivity
 * 设备重命名
 * public final static String RENAME_PLUG = "RENAME_PLUG";//重命名单插
 * public final static String RENAME_LAMP = "RENAME_LAMP";//重命名智能灯
 *
 * @author Administrator
 * @date 2019/3/18
 */
public class RenameDeviceActivity extends BaseActivity implements IRenameDeviceView {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivDeviceIcon)
    ImageView ivDeviceIcon;
    @BindView(R.id.ipvDeviceName)
    InputFrameView ipvDeviceName;
    @BindView(R.id.btnRename)
    FButton btnRename;

    private IRenameDevicePresenter mRenameDevicePresenter;
    private String mDeviceId;
    private String mRenameType;
    private String mNickName;

    public static void toRenameDeviceActivity(Activity from, String deviceId, String renameType, String nickname) {
        Intent intent = new Intent(from, RenameDeviceActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_RENAME_TYPE, renameType);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_NAME, nickname);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_rename_device);

        ButterKnife.bind(this);
        initData();
        initView();
        setValue();
    }

    private void setValue() {
        tvTitle.setText(R.string.rename_plug);
        if (mRenameType.equals(ConstantValue.RENAME_PLUG)) {//重命名单插
            ivDeviceIcon.setImageResource(R.drawable.home_plug_icon);
        } else if (mRenameType.equals(ConstantValue.RENAME_LAMP)) {//重命名智能灯
            ivDeviceIcon.setImageResource(R.drawable.light_bulb_icon_name);
        } else if (mRenameType.equals(ConstantValue.RENAME_LIGHT_STRIP)) {//重命名灯带
            ivDeviceIcon.setImageResource(R.drawable.device_setting_light_strip_icon);
        } else if (mRenameType.equals(ConstantValue.RENAME_MODULATOR)) {//重命名调光器
            ivDeviceIcon.setImageResource(R.drawable.device_setting_modulator_icon);
        } else if (mRenameType.equals(ConstantValue.RENAME_SWITCH)) {//重命名开关
            ivDeviceIcon.setImageResource(R.drawable.device_setting_switch_icon);
        } else if (mRenameType.equals(ConstantValue.RENAME_PETFEEDER)) {
            ivDeviceIcon.setImageResource(R.drawable.ic_device_light_feeder);
        } else if (mRenameType.equals(ConstantValue.RENAME_AIRPURIFIER)) {
            ivDeviceIcon.setImageResource(R.drawable.ic_device_purifier);
        }
        ipvDeviceName.setEtInputText(mNickName);
        ipvDeviceName.setEtSelection(TextUtils.isEmpty(mNickName) ? 0 : mNickName.length());
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
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
            mRenameType = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_RENAME_TYPE);
            mNickName = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_NAME);
            mRenameDevicePresenter = new RenameDevicePresenter(this);
        }
    }

    private void checkButton() {
        if (!TextUtils.isEmpty(ipvDeviceName.getInputText().trim())) {
            btnRename.setEnabled(true);
            btnRename.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
        } else {
            btnRename.setEnabled(false);
            btnRename.setTextColor(getResources().getColor(R.color.unable_clickable_color));
        }
    }

    @OnClick({R.id.btnRename, R.id.ivLeft})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.btnRename: {
                if (ipvDeviceName.getInputText().length() > ConstantValue.MAX_DEVICE_NAME_LENGTH) {
                    ToastUtil.showToast(RenameDeviceActivity.this, getString(R.string.name_too_long));
                } else {
                    mRenameDevicePresenter.renameDevice(mDeviceId, ipvDeviceName.getInputText());
                }
                break;
            }
            case R.id.ivLeft: {
                finish();
                break;
            }
        }
    }

    @Override
    public void notifyRenameDeviceState(String msg) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(msg)) {
            ToastUtil.showToast(this, getResources().getString(R.string.success));
            finish();
        } else {
            ErrorHandleUtil.toastTuyaError(this, msg);
        }
    }
}
