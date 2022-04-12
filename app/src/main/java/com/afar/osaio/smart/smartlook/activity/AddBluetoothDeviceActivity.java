package com.afar.osaio.smart.smartlook.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.smart.smartlook.adapter.ScanBleDeviceAdapter;
import com.afar.osaio.smart.smartlook.bean.BleDevice;
import com.afar.osaio.smart.smartlook.cache.BleDeviceScanCache;
import com.afar.osaio.smart.smartlook.contract.AddBluetoothDeviceContract;
import com.afar.osaio.smart.smartlook.presenter.AddBluetoothDevicePresenter;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.ScanBleDeviceView;
import com.afar.osaio.widget.base.BaseScanCameraView;
import com.nooie.common.hardware.bluetooth.BluetoothHelper;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.nooie.common.utils.log.NooieLog;

import java.util.ArrayList;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddBluetoothDeviceActivity extends BaseActivity implements AddBluetoothDeviceContract.View {

    private static final String[] PERMS_INCLUDE_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.scanBleContainer)
    LinearLayout scanBleContainer;
    @BindView(R.id.tvTip)
    TextView tvTip;
    BaseScanCameraView mBaseScanBleView;

    private AddBluetoothDeviceContract.Presenter mPresenter;
    private AlertDialog mScanBelDeviceDialog = null;

    public static void toAddBluetoothDeviceActivity(Context from) {
        Intent intent = new Intent(from, AddBluetoothDeviceActivity.class);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bluetooth_device);
        ButterKnife.bind(this);
        initData();
        initView();
        initSmartLook();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initData() {
        BleDeviceScanCache.getInstance().clearCache();
        new AddBluetoothDevicePresenter(this);
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.add_bluetooth_device_title);
        setupScan();
    }

    private void setupScan() {
        if (mBaseScanBleView == null) {
            mBaseScanBleView = new ScanBleDeviceView(getApplicationContext());
            ((ScanBleDeviceView) mBaseScanBleView).setupScanAnimView(ScanBleDeviceView.TYPE_SCAN_BLE_DEVICE_ANIM_DEFAULT);
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, (int)(DisplayUtil.SCREEN_HIGHT_PX*0.1));
            mBaseScanBleView.setLayoutParams(params);
        }

        scanBleContainer.removeAllViews();
        scanBleContainer.addView(mBaseScanBleView);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mBaseScanBleView != null) {
            mBaseScanBleView.closeScan();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void setPresenter(@NonNull AddBluetoothDeviceContract.Presenter presenter) {
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

    private void initSmartLook() {
        if (!isBluetoothReady()) {
            ToastUtil.showToast(this, R.string.add_bluetooth_device_bluetooth_invalid_tip);
            return;
        }

        startScanBleDevice();
    }

    private void startScanBleDevice() {
        if (checkNull(mBaseScanBleView, mPresenter)) {
            return;
        }
        mPresenter.startScanDeviceByTask(mUserAccount, new ArrayList<>());
        mBaseScanBleView.startScanLoop();
    }

    @Override
    public void onScanDeviceFinish(String result) {
        Set<BluetoothDevice> bondedDevices =  BluetoothHelper.getBluetoothAdapter().getBondedDevices();
        if (isDestroyed() || checkNull(mBaseScanBleView)) {
            return;
        }
        mBaseScanBleView.closeScan();
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            showScanDevices();
        }
    }

    private void showScanDevices() {
        if (CollectionUtil.isEmpty(BleDeviceScanCache.getInstance().getAllCache())) {
            tvTip.setText(R.string.add_bluetooth_device_scan_empty);
            return;
        }

        hideScanDevices();
        ScanBleDeviceAdapter scanBleDeviceAdapter = new ScanBleDeviceAdapter();
        mScanBelDeviceDialog = DialogUtils.showListViewDialog(this, "Device List", scanBleDeviceAdapter, true);
        scanBleDeviceAdapter.setListener(new ScanBleDeviceAdapter.ScanBleDeviceListener() {
            @Override
            public void onItemClick(BleDevice bleDevice) {
                hideScanDevices();
                if (bleDevice != null && mPresenter != null) {
                    mPresenter.checkAddBleDevice(bleDevice);
                }
            }
        });
        scanBleDeviceAdapter.setData(BleDeviceScanCache.getInstance().getAllCache());
    }

    private void hideScanDevices() {
        if (mScanBelDeviceDialog != null) {
            mScanBelDeviceDialog.dismiss();
        }
    }

    private void gotoAddBleDevice(BleDevice bleDevice) {
        if (bleDevice == null) {
            return;
        }
        NooieLog.d("-->> AddBluetoothDeviceActivity gotoAddBleDevice bleDevice name=" + bleDevice.getDevice().getName() + " address=" + bleDevice.getDevice().getAddress() + " deviceType=" + bleDevice.getDeviceType()
                + " rssi=" + bleDevice.getRssi() + " initState=" + bleDevice.getInitState());
        if (bleDevice.getInitState() == 2) {
            AddAuthorizationActivity.toAddAuthorizationActivity(AddBluetoothDeviceActivity.this, bleDevice);
            finish();
        } else {
            AddAdminActivity.toAddAdminActivity(AddBluetoothDeviceActivity.this, true, bleDevice);
            finish();
        }
    }

    @Override
    public void notifyCheckBleDevice(String result, boolean isExist, BleDevice bleDevice) {
        if (ConstantValue.SUCCESS.equalsIgnoreCase(result)) {
            if (isExist) {
                ToastUtil.showToast(this, R.string.add_bluetooth_device_exist);
                return;
            }
            gotoAddBleDevice(bleDevice);
        } else {
            ToastUtil.showToast(this, R.string.add_bluetooth_device_add_fail);
        }
    }
}
