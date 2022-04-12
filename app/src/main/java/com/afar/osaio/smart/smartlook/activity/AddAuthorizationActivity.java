package com.afar.osaio.smart.smartlook.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.event.DeviceChangeEvent;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.smart.smartlook.bean.BleDevice;
import com.afar.osaio.smart.smartlook.contract.AddAuthorizationContract;
import com.afar.osaio.smart.smartlook.presenter.AddAuthorizationPresenter;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.encrypt.SmartLookEncrypt;
import com.nooie.common.widget.SecurityCodeView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddAuthorizationActivity extends BaseActivity implements AddAuthorizationContract.View {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.cvAuthorizationOne)
    SecurityCodeView cvAuthorizationOne;
    @BindView(R.id.cvAuthorizationTwo)
    SecurityCodeView cvAuthorizationTwo;
    @BindView(R.id.tvGotoAddAdmin)
    TextView tvGotoAddAdmin;

    private AddAuthorizationContract.Presenter mPresenter;

    public static void toAddAuthorizationActivity(Context from, BleDevice bleDevice) {
        Intent intent = new Intent(from, AddAuthorizationActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_BLE_DEVICE, bleDevice);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_authorization);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    private void initData() {
        new AddAuthorizationPresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.add_authorization_title);
        tvGotoAddAdmin.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        tvGotoAddAdmin.getPaint().setAntiAlias(true);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @OnClick({R.id.ivLeft, R.id.btnDone, R.id.tvGotoAddAdmin})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.btnDone:
                StringBuilder codeSb = new StringBuilder();
                codeSb.append(cvAuthorizationOne.getEditContent());
                codeSb.append(cvAuthorizationTwo.getEditContent());
                BleDevice bleDeviceForAuthorization = (BleDevice)getCurrentIntent().getParcelableExtra(ConstantValue.INTENT_KEY_BLE_DEVICE);
                if (!TextUtils.isEmpty(codeSb.toString()) && bleDeviceForAuthorization != null && mPresenter != null) {
                    mPresenter.addAuthorizationCode(mUserAccount, mUid, String.valueOf(SmartLookEncrypt.passwordPhone), codeSb.toString(), bleDeviceForAuthorization);
                }
                break;
            case R.id.tvGotoAddAdmin:
                BleDevice bleDevice = (BleDevice)getCurrentIntent().getParcelableExtra(ConstantValue.INTENT_KEY_BLE_DEVICE);
                if (bleDevice != null) {
                    AddAdminActivity.toAddAdminActivity(AddAuthorizationActivity.this, false, bleDevice);
                }
                break;
        }
    }

    @Override
    public void setPresenter(@NonNull AddAuthorizationContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void notifyAddAuthorizationCodeResult(String result) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            EventBus.getDefault().post(new DeviceChangeEvent(DeviceChangeEvent.DEVICE_CHANGE_ACTION_UPDATE));
            HomeActivity.toHomeActivity(this);
            finish();
        }
    }
}
