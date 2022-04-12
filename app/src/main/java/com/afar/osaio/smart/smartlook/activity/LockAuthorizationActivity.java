package com.afar.osaio.smart.smartlook.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.smartlook.adapter.LockAuthorizationAdapter;
import com.afar.osaio.smart.smartlook.bean.BleConnectState;
import com.afar.osaio.smart.smartlook.bean.BleDevice;
import com.afar.osaio.smart.smartlook.cache.BleDeviceScanCache;
import com.afar.osaio.smart.smartlook.contract.LockAuthorizationContract;
import com.nooie.sdk.db.entity.LockAuthorizationEntity;
import com.afar.osaio.smart.smartlook.helper.SmartLookDeviceHelper;
import com.afar.osaio.smart.smartlook.presenter.LockAuthorizationPresenter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;
import com.nooie.common.utils.encrypt.SmartLookEncrypt;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LockAuthorizationActivity extends BaseActivity implements LockAuthorizationContract.View {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivRight)
    ImageView ivRight;
    @BindView(R.id.rcvLockAuthorization)
    RecyclerView rcvLockAuthorization;

    LockAuthorizationContract.Presenter mPresenter;
    boolean mIsBleDeviceVerify = false;
    LockAuthorizationAdapter mLockAuthorizationAdapter;

    public static void toLockAuthorizationActivity(Context from, String deviceId, String phone, String password, byte[] sec, boolean isAdmin) {
        Intent intent = new Intent(from, LockAuthorizationActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ID, deviceId);
        intent.putExtra(ConstantValue.INTENT_KEY_PHONE_CODE, phone);
        intent.putExtra(ConstantValue.INTENT_KEY_PSD, password);
        intent.putExtra(ConstantValue.INTENT_KEY_BLE_SEC, sec);
        intent.putExtra(ConstantValue.INTENT_KEY_IS_ADMIN, isAdmin);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_authorization);
        ButterKnife.bind(this);
        initData();
        initView();
        tryConnectDevice();
    }

    private void initData() {
        if (getCurrentIntent() == null) {
            finish();
            return;
        }
        String phone = getIntent().getStringExtra(ConstantValue.INTENT_KEY_PHONE_CODE);
        String password = getIntent().getStringExtra(ConstantValue.INTENT_KEY_PSD);
        boolean isAdmin = getCurrentIntent().getBooleanExtra(ConstantValue.INTENT_KEY_IS_ADMIN, false);
        byte[] sec = getCurrentIntent().getByteArrayExtra(ConstantValue.INTENT_KEY_BLE_SEC);
        if (sec != null && sec.length == 2) {
            SmartLookEncrypt.sec[0] = SmartLookDeviceHelper.convertByteToShort(sec[0]);
            SmartLookEncrypt.sec[1] = SmartLookDeviceHelper.convertByteToShort(sec[1]);
        }
        SmartLookEncrypt.passwordPhone = Long.parseLong(phone);
        SmartLookEncrypt.passwordNumber = Long.parseLong(password, 16);
        mIsBleDeviceVerify = SmartLookDeviceHelper.isBleDeviceVerify(phone, password, isAdmin);
        new LockAuthorizationPresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.lock_authorization_title);
        ivRight.setImageResource(R.drawable.add_photo);
        setupLockAuthorizationView();
    }

    private void setupLockAuthorizationView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rcvLockAuthorization.setLayoutManager(layoutManager);
        mLockAuthorizationAdapter = new LockAuthorizationAdapter();
        mLockAuthorizationAdapter.setListener(new LockAuthorizationAdapter.LockAuthorizationListener() {
            @Override
            public void onItemClick(LockAuthorizationEntity lockRecordEntity) {
                if (lockRecordEntity != null) {
                    showNameAuthorizationDialog(lockRecordEntity);
                }
            }

            @Override
            public void onItemDeleteClick(LockAuthorizationEntity lockRecordEntity) {
                showDeleteAuthorizationDialog(lockRecordEntity);
            }
        });
        rcvLockAuthorization.setAdapter(mLockAuthorizationAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
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

    @Override
    public void setPresenter(@NonNull LockAuthorizationContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @OnClick({R.id.ivLeft, R.id.ivRight})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
            case R.id.ivRight:
                if (mPresenter != null) {
                    mPresenter.getSmartLookManager().createAuthorCode();
                }
                break;
        }
    }

    public void tryConnectDevice() {
        String deviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
        if (mPresenter == null && TextUtils.isEmpty(deviceId)) {
            return;
        }

        mPresenter.getLockAuthorizations(mUserAccount, deviceId);

        BleDevice bleDevice = BleDeviceScanCache.getInstance().getCacheById(deviceId);
        mPresenter.setBaseInfo(mUserAccount, deviceId);
        if (bleDevice != null) {
            mPresenter.connect(NooieApplication.mCtx, bleDevice);
        } else {
            showLoading();
            List<String> filterDeviceIds = new ArrayList<>();
            filterDeviceIds.add(deviceId);
            mPresenter.startScanDeviceByTask(mUserAccount, filterDeviceIds);
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
            case BleConnectState.DEVICE_READY:
                String deviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
                if (!TextUtils.isEmpty(deviceId) && mPresenter != null) {
                    mPresenter.getLockAuthorizationsFromDevice(mUserAccount, deviceId);
                }
                break;
        }
    }

    private AlertDialog mNameAuthorizationDialog = null;
    private AlertDialog mDeleteAuthorizationDialog = null;

    private void showNameAuthorizationDialog(LockAuthorizationEntity lockAuthorizationEntity) {
        if (lockAuthorizationEntity == null) {
            return;
        }
        hideNameAuthorizationDialog();
        mNameAuthorizationDialog = DialogUtils.showBleAuthorizationCodeDialog(this, getString(R.string.dialog_ble_authorization_title), lockAuthorizationEntity.getCode(), getString(R.string.dialog_ble_authorization_code_hint),
                getString(R.string.cancel), getString(R.string.confirm_upper), true, DialogUtils.INPUT_DIALOG_TYPE_TEXT_EDIT, new DialogUtils.OnClickBleAuthorizationCodeDialogListener() {
                    @Override
                    public void onClickCancel() {
                    }

                    @Override
                    public void onClickConfirm(int inputType, String text) {
                        lockAuthorizationEntity.setName(text);
                        if (mPresenter != null) {
                            mPresenter.updateAuthorization(lockAuthorizationEntity);
                        }
                    }
                });
    }

    private void hideNameAuthorizationDialog() {
        if (mNameAuthorizationDialog != null) {
            mNameAuthorizationDialog.dismiss();
        }
    }

    private void showDeleteAuthorizationDialog(LockAuthorizationEntity lockAuthorizationEntity) {
        hideDeleteAuthorizationDialog();
        mDeleteAuthorizationDialog = DialogUtils.showConfirmWithSubMsgDialog(this, R.string.dialog_tip_title, R.string.lock_authorization_delete_tip, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                if (mPresenter != null) {
                    mPresenter.deleteAuthorization(lockAuthorizationEntity);
                }
            }

            @Override
            public void onClickLeft() {
            }
        });
    }

    private void hideDeleteAuthorizationDialog() {
        if (mDeleteAuthorizationDialog != null) {
            mDeleteAuthorizationDialog.dismiss();
        }
    }

    @Override
    public void onScanDeviceFinish(String result) {
        if (isDestroyed() || mPresenter == null) {
            return;
        }

        hideLoading();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            String deviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
            BleDevice bleDevice = BleDeviceScanCache.getInstance().getCacheById(deviceId);
            if (bleDevice != null) {
                mPresenter.connect(NooieApplication.mCtx, bleDevice);
            }
        }
    }

    @Override
    public void notifyGetLockAuthorization(String result, List<LockAuthorizationEntity> lockAuthorizations) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            if (mLockAuthorizationAdapter != null) {
                mLockAuthorizationAdapter.setData(lockAuthorizations);
            }
        }
    }

    @Override
    public void notifyCreateAuthorization(String result, LockAuthorizationEntity lockAuthorizationEntity) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            showNameAuthorizationDialog(lockAuthorizationEntity);
        } else {
            ToastUtil.showToast(this, R.string.get_fail);
        }
    }

    @Override
    public void notifyDeleteAuthorization(String result, LockAuthorizationEntity lockAuthorizationEntity) {
        if (isDestroyed()) {
            return;
        }

        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            if (mLockAuthorizationAdapter != null) {
                mLockAuthorizationAdapter.removeData(lockAuthorizationEntity);
            }
        }
    }

}
