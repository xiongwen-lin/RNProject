package com.afar.osaio.smart.smartlook.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.event.DeviceChangeEvent;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.smart.smartlook.bean.BleConnectState;
import com.afar.osaio.smart.smartlook.bean.BleDevice;
import com.afar.osaio.smart.smartlook.contract.AddAdminContract;
import com.afar.osaio.smart.smartlook.presenter.AddAdminPresenter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.widget.FButton;
import com.afar.osaio.widget.InputFrameView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddAdminActivity extends BaseActivity implements AddAdminContract.View {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ipvAccount)
    InputFrameView ipvAccount;
    @BindView(R.id.ipvPsd)
    InputFrameView ipvPsd;
    @BindView(R.id.btnDone)
    FButton btnDone;

    AddAdminContract.Presenter mPresenter;

    public static void toAddAdminActivity(Context from, boolean isAdmin, BleDevice bleDevice) {
        Intent intent = new Intent(from, AddAdminActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_IS_ADMIN, isAdmin);
        intent.putExtra(ConstantValue.INTENT_KEY_BLE_DEVICE, bleDevice);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_admin);
        ButterKnife.bind(this);
        initData();
        initView();
        if (mPresenter != null && getCurrentIntent() != null) {
            BleDevice bleDevice = (BleDevice)getCurrentIntent().getParcelableExtra(ConstantValue.INTENT_KEY_BLE_DEVICE);
            mPresenter.connect(NooieApplication.mCtx, bleDevice);
        }
    }

    private void initData() {
        new AddAdminPresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.add_admin_title);
        setupInputFrameView();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mPresenter != null) {
            mPresenter.disconnect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setupInputFrameView() {
        ipvAccount.setTheme(InputFrameView.INPUT_FRAME_THEME_TYPE_DEFAULT)
                .setInputTitle(getString(R.string.phone_number_email))
                .setInputBtnIsShow(false)
                .setEtInputType(InputType.TYPE_CLASS_NUMBER)
                .setEtInputMaxLength(11)
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
                .setInputTitle(getString(R.string.password))
                .setEtInputToggle(true)
                .setInputBtn(R.drawable.eye_open_icon_state_list)
                .setEtPwInputType(InputType.TYPE_CLASS_NUMBER)
                .setEtInputType(InputFrameView.getPwInputType(InputType.TYPE_CLASS_NUMBER, true))
                .setEtInputMaxLength(8)
                .setOnClickListener(new InputFrameView.OnInputFrameClickListener() {
                    @Override
                    public void onInputBtnClick() {
                    }

                    @Override
                    public void onEditorAction() {
                        hideInputMethod();
                        onViewClick(btnDone);
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
    }

    public void checkBtnEnable() {
        if (!TextUtils.isEmpty(ipvAccount.getInputText()) && !TextUtils.isEmpty(ipvPsd.getInputText())) {
            btnDone.setEnabled(true);
            btnDone.setTextColor(getResources().getColor(R.color.theme_green_subtext_color));
        } else {
            btnDone.setEnabled(false);
            btnDone.setTextColor(getResources().getColor(R.color.unable_clickable_color));
        }
    }

    @OnClick({R.id.ivLeft, R.id.btnDone})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnDone:
                boolean isAdmin = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_IS_ADMIN, false);
                BleDevice bleDevice = (BleDevice)getCurrentIntent().getParcelableExtra(ConstantValue.INTENT_KEY_BLE_DEVICE);
                showLoading();
                mPresenter.addAdminUser(mUserAccount, mUid, ipvAccount.getInputText(), ipvPsd.getInputText(), isAdmin, bleDevice);
                break;
        }
    }

    @Override
    public void setPresenter(@NonNull AddAdminContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void notifyAddAdminResult(String result, boolean isAdmin) {
        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            EventBus.getDefault().post(new DeviceChangeEvent(DeviceChangeEvent.DEVICE_CHANGE_ACTION_UPDATE));
            HomeActivity.toHomeActivity(this);
            finish();
        } else {
        }
    }

    @Override
    public void notifyBleDeviceState(int connectState) {
        switch (connectState) {
            case BleConnectState.CONNECTING:
                showLoading();
                break;
            case BleConnectState.CONNECTED:
                hideLoading();
                break;
        }
    }
}
