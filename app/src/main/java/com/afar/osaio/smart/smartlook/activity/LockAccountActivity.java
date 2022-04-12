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
import com.afar.osaio.smart.event.DeviceChangeEvent;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.smart.smartlook.adapter.LockAccountAdapter;
import com.afar.osaio.smart.smartlook.bean.BleConnectState;
import com.afar.osaio.smart.smartlook.bean.BleDevice;
import com.afar.osaio.smart.smartlook.cache.BleDeviceScanCache;
import com.afar.osaio.smart.smartlook.cache.LockDeviceCache;
import com.afar.osaio.smart.smartlook.contract.LockAccountContract;
import com.nooie.sdk.db.entity.BleDeviceEntity;
import com.afar.osaio.smart.smartlook.helper.SmartLookDeviceHelper;
import com.afar.osaio.smart.smartlook.presenter.LockAccountPresenter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.nooie.common.utils.encrypt.SmartLookEncrypt;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LockAccountActivity extends BaseActivity implements LockAccountContract.View {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.rcvLockAccount)
    RecyclerView rcvLockAccount;

    private AlertDialog mDeleteAccountDialog = null;

    LockAccountContract.Presenter mPresenter;
    boolean mIsBleDeviceVerify = false;
    LockAccountAdapter mLockAccountAdapter;

    public static void toLockAccountActivity(Context from, String deviceId, String phone, String password, byte[] sec, boolean isAdmin) {
        Intent intent = new Intent(from, LockAccountActivity.class);
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
        setContentView(R.layout.activity_lock_account);
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
        new LockAccountPresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.lock_account_title);
        setupLockAccountView();
    }

    private void setupLockAccountView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rcvLockAccount.setLayoutManager(layoutManager);
        mLockAccountAdapter = new LockAccountAdapter();
        mLockAccountAdapter.setListener(new LockAccountAdapter.LockAccountListener() {
            @Override
            public void onItemClick(BleDeviceEntity bleDeviceEntity) {
            }

            @Override
            public void onItemDeleteClick(BleDeviceEntity bleDeviceEntity) {
                showDeleteAccountDialog(bleDeviceEntity);
            }
        });
        rcvLockAccount.setAdapter(mLockAccountAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        String deviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
        if (!TextUtils.isEmpty(deviceId) && mPresenter != null) {
            mPresenter.loadData(mUserAccount, deviceId);
        }
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
    public void setPresenter(@NonNull LockAccountContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @OnClick({R.id.ivLeft, R.id.ivRight})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                finish();
                break;
        }
    }

    public void tryConnectDevice() {
        String deviceId = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ID);
        if (mPresenter == null && TextUtils.isEmpty(deviceId)) {
            return;
        }

        BleDevice bleDevice = BleDeviceScanCache.getInstance().getCacheById(deviceId);
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
        if (isDestroyed()) {
            return;
        }

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
                }
                break;
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
    public void notifyLoadDataResult(String result, BleDeviceEntity bleDeviceEntity) {
        if (isDestroyed()) {
            return;
        }

        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            if (bleDeviceEntity != null && mLockAccountAdapter != null) {
                List<BleDeviceEntity> bleDeviceEntities = new ArrayList<>();
                bleDeviceEntities.add(bleDeviceEntity);
                mLockAccountAdapter.setData(bleDeviceEntities);
            }
        }
    }

    private void showDeleteAccountDialog(BleDeviceEntity bleDeviceEntity) {
        hideDeleteAuthorizationDialog();
        mDeleteAccountDialog = DialogUtils.showConfirmWithSubMsgDialog(this, R.string.dialog_tip_title, R.string.lock_account_delete_tip, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickRight() {
                if (mPresenter != null) {
                    mPresenter.deleteAccount(bleDeviceEntity);
                }
            }

            @Override
            public void onClickLeft() {
            }
        });
    }

    private void hideDeleteAuthorizationDialog() {
        if (mDeleteAccountDialog != null) {
            mDeleteAccountDialog.dismiss();
        }
    }

    @Override
    public void notifyDeleteAccountResult(String result, String deviceId) {
        if (isDestroyed()) {
            return;
        }

        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            LockDeviceCache.getInstance().removeCacheById(deviceId);
            EventBus.getDefault().post(new DeviceChangeEvent(DeviceChangeEvent.DEVICE_CHANGE_ACTION_UPDATE));
            HomeActivity.toHomeActivity(this);
            finish();
        }
    }
}
