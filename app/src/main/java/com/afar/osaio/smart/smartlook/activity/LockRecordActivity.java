package com.afar.osaio.smart.smartlook.activity;

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
import com.afar.osaio.smart.smartlook.adapter.LockRecordAdapter;
import com.afar.osaio.smart.smartlook.bean.BleConnectState;
import com.afar.osaio.smart.smartlook.bean.BleDevice;
import com.afar.osaio.smart.smartlook.cache.BleDeviceScanCache;
import com.afar.osaio.smart.smartlook.contract.LockRecordContract;
import com.nooie.sdk.db.entity.LockRecordEntity;
import com.afar.osaio.smart.smartlook.helper.SmartLookDeviceHelper;
import com.afar.osaio.smart.smartlook.presenter.LockRecordPresenter;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.encrypt.SmartLookEncrypt;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LockRecordActivity extends BaseActivity implements LockRecordContract.View {

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.rcvLockRecord)
    RecyclerView rcvLockRecord;

    LockRecordContract.Presenter mPresenter;
    boolean mIsBleDeviceVerify = false;
    LockRecordAdapter mLockRecordAdapter;

    public static void toLookDeviceActivity(Context from, String deviceId, String phone, String password, byte[] sec, boolean isAdmin) {
        Intent intent = new Intent(from, LockRecordActivity.class);
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
        setContentView(R.layout.activity_lock_record);
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
        new LockRecordPresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.lock_record_title);
        setupLockRecordView();
    }

    private void setupLockRecordView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rcvLockRecord.setLayoutManager(layoutManager);
        mLockRecordAdapter = new LockRecordAdapter();
        mLockRecordAdapter.setListener(new LockRecordAdapter.LockRecordListener() {
            @Override
            public void onItemClick(LockRecordEntity lockRecordEntity) {
            }
        });
        rcvLockRecord.setAdapter(mLockRecordAdapter);
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
    public void setPresenter(@NonNull LockRecordContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @OnClick({R.id.ivLeft})
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

        mPresenter.getLockRecords(mUserAccount, deviceId);

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
                    mPresenter.getLockRecordsFromDevice(mUserAccount, deviceId);
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
    public void notifyGetLockRecord(String result, List<LockRecordEntity> lockRecords) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            if (mLockRecordAdapter != null) {
                mLockRecordAdapter.setData(lockRecords);
            }
        }
    }

}
