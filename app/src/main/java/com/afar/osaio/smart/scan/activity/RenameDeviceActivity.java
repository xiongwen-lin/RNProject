package com.afar.osaio.smart.scan.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.scan.presenter.INooieNameDevicePresenter;
import com.afar.osaio.smart.scan.presenter.NooieNameDevicePresenter;
import com.afar.osaio.smart.scan.view.INooieNameDeviceView;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.FButton;
import com.afar.osaio.widget.InputFrameView;
import com.afar.osaio.widget.helper.ResHelper;
import com.nooie.common.utils.notify.NotificationUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by victor on 2018/6/30
 * Email is victor.qiao.0604@gmail.com
 */
public class RenameDeviceActivity extends BaseActivity implements INooieNameDeviceView {
    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.ivDeviceIcon)
    ImageView ivDeviceIcon;
    @BindView(R.id.ipvDeviceName)
    InputFrameView ipvDeviceName;
    @BindView(R.id.btnDone)
    FButton btnDone;

    private String mName;
    private String mDeviceModel;
    private INooieNameDevicePresenter mNameDevicePresenter;

    public static void toRenameDeviceActivity(Activity from, Bundle param, int requestCode) {
        Intent intent = new Intent(from, RenameDeviceActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_START_PARAM, param);
        from.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name_device);
        ButterKnife.bind(this);
        initData();
        initView();
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

    private void initData() {
        if (getCurrentIntent() != null) {
            mName = getDeviceName();
            mDeviceModel = getDeviceModel();
        }
        mNameDevicePresenter = new NooieNameDevicePresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.add_camera_name_your_camera);
        ivDeviceIcon.setImageResource(ResHelper.getInstance().getDeviceIconByType(mDeviceModel));
        setupInputFrameView();
    }

    private void setupInputFrameView() {
        ipvDeviceName.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_TEXT_CENTER)
                .setInputTitleVisible(View.GONE)
                .setInputBtnIsShow(false)
                .setTextAlign(Gravity.CENTER)
                .setEtInputMaxLength(ConstantValue.DEVICE_NAME_MAX_LEN)
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
                        mName = ipvDeviceName.getInputTextNoTrim();
                        checkBtnEnable();
                    }
                });

        checkBtnEnable();
    }

    public void checkBtnEnable() {
        if (!TextUtils.isEmpty(ipvDeviceName.getInputTextNoTrim())) {
            btnDone.setEnabled(true);
            btnDone.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
        } else {
            btnDone.setEnabled(false);
            btnDone.setTextColor(getResources().getColor(R.color.unable_clickable_color));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mNameDevicePresenter != null) {
            mNameDevicePresenter.destroy();
            mNameDevicePresenter = null;
        }
        releaseRes();
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        ivLeft = null;
        tvTitle = null;
        ivRight = null;
        ivDeviceIcon = null;
        if (ipvDeviceName != null) {
            ipvDeviceName.release();
            ipvDeviceName = null;
        }
        btnDone = null;
    }

    @OnClick({R.id.ivLeft, R.id.btnDone, R.id.tvFrontDoor, R.id.tvOffice, R.id.tvLivingRoom, R.id.tvGarage, R.id.tvBabyRoom, R.id.tvKitchen})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.tvFrontDoor:
                updateDeviceName(getString(R.string.add_camera_front_door));
                break;
            case R.id.tvOffice:
                updateDeviceName(getString(R.string.add_camera_office));
                break;
            case R.id.tvLivingRoom:
                updateDeviceName(getString(R.string.add_camera_living_room));
                break;
            case R.id.tvGarage:
                updateDeviceName(getString(R.string.add_camera_garage));
                break;
            case R.id.tvBabyRoom:
                updateDeviceName(getString(R.string.add_camera_baby_room));
                break;
            case R.id.tvKitchen:
                updateDeviceName(getString(R.string.add_camera_kitchen));
                break;
            case R.id.btnDone:
//                String deviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
//                if (!TextUtils.isEmpty(mName) && !TextUtils.isEmpty(deviceId)) {
//                    mNameDevicePresenter.renameDevice(deviceId, mName);
//                }
                dealOnClickConfirm();
                break;
        }
    }

    @Override
    public void notifyUpdateDeviceNameState(String result) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            Intent i = new Intent();
            i.putExtra(ConstantValue.INTENT_KEY_DEVICE_NAME, mName);
            setResult(RESULT_OK, i);
            Intent intent = new Intent(ConstantValue.BROADCAST_KEY_UPDATE_CAMERA);
            NotificationUtil.sendBroadcast(NooieApplication.mCtx, intent);
            finish();
        } else {
            if (!TextUtils.isEmpty(result)) {
                ToastUtil.showToast(this, result);
            }
        }
    }

    private void updateDeviceName(String name) {
        mName = name;
        if (ipvDeviceName != null) {
            ipvDeviceName.setEtInputText(name);
        }
    }

    private void dealOnClickConfirm() {
        if (!TextUtils.isEmpty(mName) && !TextUtils.isEmpty(getDeviceId())) {
            mNameDevicePresenter.renameDevice(getConnectionMode(), mUserAccount, getDeviceId(), mName);
        }
    }

    private String getDeviceId() {
        if (getStartParam() == null) {
            return null;
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_DEVICE_ID);
    }

    private String getDeviceModel() {
        if (getStartParam() == null) {
            return null;
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_IPC_MODEL);
    }

    private String getDeviceName() {
        if (getStartParam() == null) {
            return null;
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_DEVICE_NAME);
    }

    private int getConnectionMode() {
        if (getStartParam() == null) {
            return ConstantValue.CONNECTION_MODE_QC;
        }
        return getStartParam().getInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC);
    }
}
