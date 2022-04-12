package com.afar.osaio.smart.mixipc.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.nordicbluetooth.SmartBleManager;
import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.bluetooth.activity.BaseBluetoothActivity;
import com.afar.osaio.smart.bluetooth.listener.OnBleConnectListener;
import com.afar.osaio.smart.bluetooth.listener.OnBleStartScanListener;
import com.afar.osaio.smart.home.activity.HomeActivity;
import com.afar.osaio.smart.mixipc.adapter.ScanBluetoothDeviceAdapter;
import com.afar.osaio.smart.mixipc.adapter.listener.ScanBluetoothDeviceListener;
import com.afar.osaio.smart.mixipc.contract.BluetoothScanContract;
import com.afar.osaio.smart.mixipc.presenter.BluetoothScanPresenter;
import com.afar.osaio.smart.mixipc.profile.bean.BleConnectState;
import com.afar.osaio.smart.mixipc.profile.bean.BleDevice;
import com.afar.osaio.smart.mixipc.profile.bean.IpcBleCmd;
import com.afar.osaio.smart.mixipc.profile.cache.BleDeviceScanCache;
import com.afar.osaio.smart.scan.activity.InputWiFiPsdActivity;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.afar.osaio.util.ToastUtil;
import com.afar.osaio.widget.FButton;
import com.nooie.common.hardware.bluetooth.BluetoothHelper;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.graphics.DisplayUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.bean.SDKConstant;
import com.nooie.sdk.db.entity.BleApDeviceEntity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import pub.devrel.easypermissions.EasyPermissions;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class BluetoothScanActivity extends BaseBluetoothActivity implements BluetoothScanContract.View {

    private static final int BLUETOOTH_SCAN_VIEW_STATE_WAITING = 1;
    private static final int BLUETOOTH_SCAN_VIEW_STATE_PERM_DENY = 2;
    private static final int BLUETOOTH_SCAN_VIEW_STATE_NO_FOUND = 3;
    private static final int BLUETOOTH_SCAN_VIEW_STATE_SCANNING = 4;
    private static final int BLUETOOTH_SCAN_VIEW_STATE_SCAN_FINISH = 5;

    private static final int REFRESH_TYPE_RESET = 0;
    private static final int REFRESH_TYPE_SCANNING = 1;
    private static final int REFRESH_TYPE_FINISH = 2;

    private static final int RETRY_CONNECT_BLUETOOTH_MAX_COUTN = 3;

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.ivBluetoothScanState)
    ImageView ivBluetoothScanState;
    @BindView(R.id.tvBluetoothScanState)
    TextView tvBluetoothScanState;
    @BindView(R.id.rvBluetoothScanList)
    RecyclerView rvBluetoothScanList;
    @BindView(R.id.vBluetoothScanNone)
    View vBluetoothScanNone;
    @BindView(R.id.ivBluetoothScanWaiting)
    ImageView ivBluetoothScanWaiting;
    @BindView(R.id.tvBluetoothScanWaitingTip)
    TextView tvBluetoothScanWaitingTip;
    @BindView(R.id.tvBluetoothScanNoneTip)
    TextView tvBluetoothScanNoneTip;
    @BindView(R.id.ivBluetoothScanNone)
    ImageView ivBluetoothScanNone;
    @BindView(R.id.tvBluetoothScanNoneTip_1)
    TextView tvBluetoothScanNoneTip_1;
    @BindView(R.id.btnBluetoothScanNoneRetry)
    FButton btnBluetoothScanNoneRetry;
    @BindView(R.id.tvBluetoothScanNoneDeleteDevice)
    TextView tvBluetoothScanNoneDeleteDevice;

    private BluetoothScanContract.Presenter mPresenter;
    private Dialog mRequestLocationPermDialog;
    private boolean mIsNormalDenied = false;
    private boolean mIsIgnoreCheckBeforeScanningBluetooth = false;
    private ScanBluetoothDeviceAdapter mAdapter;
    private BluetoothDevice mConnectBluetoothDevice = null;
    private Animator mStartScanningAnimator;
    private Dialog mShowDeviceModeErrorDialog = null;
    private Dialog mShowDeleteBleApDeviceDialog = null;
    private boolean mIsTryConnectBluetooth = false;
    private long mLastShowBleCmdInvalidTip = 0;
    private long mLastStartConnectBluetooth = 0;
    private long mRetryConnectBluetoothCount = 0;
    private boolean mIsRetryConnectBluetooth = false;

    public static void toBluetoothScanActivity(Context from, Bundle param) {
        Intent intent = new Intent(from, BluetoothScanActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_START_PARAM, param);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_scan);
        ButterKnife.bind(this);

        initData();
        initView();
        initBle();
    }

    private void initData() {
        new BluetoothScanPresenter(this);
        BleDeviceScanCache.getInstance().clearCache();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.bluetooth_scan_title);
        displayBlueScanView(BLUETOOTH_SCAN_VIEW_STATE_WAITING);
        setupBluetoothDeviceView();
    }

    @Override
    public void onResume() {
        super.onResume();
        resumeData();
    }

    private void resumeData() {
        setIsGotoOtherPage(false);
        checkIsShowBluetoothDeclare();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopScanBluetooth();
        if (mPresenter != null) {
            mPresenter.destroy();
        }
        hideCheckLocationPermDialog();
        releaseRes();
    }

    @Override
    public void releaseRes() {
        super.releaseRes();
        ivLeft = null;
        tvTitle = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        tryDisconnectBluetooth();
    }

    @OnClick({R.id.ivLeft, R.id.tvBluetoothScanState, R.id.btnBluetoothScanNoneRetry, R.id.tvBluetoothScanNoneDeleteDevice, R.id.ivBluetoothScanState})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ivLeft:
                tryDisconnectBluetooth();
                finish();
                break;
            case R.id.btnBluetoothScanNoneRetry:
                if (getBlueScanViewState() == BLUETOOTH_SCAN_VIEW_STATE_PERM_DENY) {
                    displayBlueScanView(BLUETOOTH_SCAN_VIEW_STATE_WAITING);
                    checkBeforeScanningBluetooth();
                } else if (getBlueScanViewState() == BLUETOOTH_SCAN_VIEW_STATE_NO_FOUND) {
                    startScanBluetooth();
                }
                break;
            case R.id.tvBluetoothScanNoneDeleteDevice:
                if (getScanType() != ConstantValue.BLUETOOTH_SCAN_TYPE_EXIST || TextUtils.isEmpty(getBleDeviceId())) {
                    break;
                }
                checkDeleteBleApDevice(getBleDeviceId());
                break;
            case R.id.ivBluetoothScanState:
                if (getBlueScanViewState() == BLUETOOTH_SCAN_VIEW_STATE_SCAN_FINISH && mAdapter != null && !mAdapter.checkIsConnectingBluetoothState()) {
                    startScanBluetooth();
                }
                break;
        }
    }

    @Override
    public void setPresenter(@NonNull BluetoothScanContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onCheckDeleteBleApDevice(int state, BleApDeviceEntity result) {
        if (isDestroyed()) {
            return;
        }
        if (state == SDKConstant.SUCCESS && result != null) {
            showDeleteBleApDeviceDialog(result.getDeviceId(), result.getName());
        }
    }

    @Override
    public void onDeleteBleApDevice(int state) {
        if (isDestroyed()) {
            return;
        }
        if (state == SDKConstant.SUCCESS) {
            gotoHomePage();
        }
    }

    @Override
    public void bluetoothStateOffChange() {
        //蓝牙关闭
        NooieLog.d("-->> debug BaseBluetoothActivity bluetoothStateOffChange: ");
        checkAfterBluetoothDisable();
    }

    @Override
    public void bluetoothDisConnected() {
        //蓝牙已断开,蓝牙库会停止扫和断开连接
        NooieLog.d("-->> debug BluetoothScanActivity bluetoothDisConnected: ");
        if (isDestroyed() || checkIsGotoOtherPage() || mIsTryConnectBluetooth) {
            return;
        }
        hideLoading();
        refreshBleSanView(REFRESH_TYPE_RESET);
    }

    @Override
    public void onBatchScanResultsByFilter(@NotNull List<ScanResult> results) {
        super.onBatchScanResultsByFilter(results);
        if (isDestroyed() || checkIsGotoOtherPage()) {
            return;
        }
        BleDeviceScanCache.getInstance().updateBleDevice(filterDeviceBluetooth(results, getBleDeviceId()));
        refreshBleSanView(REFRESH_TYPE_SCANNING);
    }

    @Override
    public void onDeviceReady(@NotNull BluetoothDevice device) {
        //super.onDeviceReady(device);
        //设备Ready可以通信了
        if (isDestroyed() || checkIsGotoOtherPage()) {
            return;
        }
        NooieLog.d("-->> debug BluetoothScanActivity onDeviceReady: ");
        if (!checkMatchBluetooth(mConnectBluetoothDevice, device)) {
            return;
        }
        dealBleDeviceList(BleConnectState.DEVICE_READY, device);
    }

    @Override
    public void onDeviceDisconnecting(@NotNull BluetoothDevice device) {
        //super.onDeviceDisconnecting(device);
        //设备正在断开连接
        if (isDestroyed() || checkIsGotoOtherPage() || mIsTryConnectBluetooth || mIsRetryConnectBluetooth) {
            return;
        }
        NooieLog.d("-->> debug BluetoothScanActivity onDeviceDisconnecting: ");
        if (!checkMatchBluetooth(mConnectBluetoothDevice, device)) {
            return;
        }
        showBluetoothOperationTip(BLUETOOTH_OPERATION_TIP_TYPE_FAIL);
    }

    @Override
    public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
        super.onDataReceived(device, data);
        if (isDestroyed() || checkIsGotoOtherPage()) {
            return;
        }
        if (!checkMatchBluetooth(mConnectBluetoothDevice, device)) {
            return;
        }
        hideLoading();
        String cmdResponse = data != null ? data.getStringValue(0) : null;
        boolean isCmdResponseInValid = cmdResponse == null || !cmdResponse.startsWith(IpcBleCmd.BLE_CMD_FEATURE_RSP);
        NooieLog.d("-->> debug BluetoothScanActivity onDataReceived cmdResponse=" + cmdResponse + " isCmdResponseInValid=" + isCmdResponseInValid + " data=" + (data != null ? data.toString() : ""));
        if (isCmdResponseInValid) {
            showBleCmdInvalidTip();
            return;
        }
        cancelRetryConnectBluetooth();
        if (IpcBleCmd.checkCmdRspIsUnbind(cmdResponse)) {
            showBluetoothOperationTip(BLUETOOTH_OPERATION_TIP_TYPE_UNBIND);
            return;
        }
        if (getConnectionMode() == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            if (cmdResponse.contains(IpcBleCmd.BLE_CMD_LOGIN_RSP)) {
                queryBluetoothPw();
            } else if (cmdResponse.contains(IpcBleCmd.BLE_CMD_QUERY_PW_RSP)) {
                boolean isDefaultPw = cmdResponse.contains(IpcBleCmd.BLE_CMD_RSP_SUCCESS) && cmdResponse.contains(IpcBleCmd.BLE_CMD_QUERY_STATE_FACTORY);
                gotoSetupBleApDevice(isDefaultPw, device.getName(), device.getAddress());
            } else if (cmdResponse.contains(IpcBleCmd.BLE_CMD_OPEN_HOT_SPOT_RSP)) {
                boolean isOpenHotSpot = cmdResponse.contains(IpcBleCmd.BLE_CMD_RSP_SUCCESS);
                gotoConnectApDevice(isOpenHotSpot, device.getName(), device.getAddress());
            }
        } else {
            if (cmdResponse.contains(IpcBleCmd.BLE_CMD_LOGIN_RSP)) {
                gotoConnectQcDevice(device.getName(), device.getAddress());
            }
        }
    }

    @Override
    public void onDeviceFailedToConnect(@NotNull BluetoothDevice device) {
        NooieLog.d("-->> debug BluetoothScanActivity onDeviceFailedToConnect: 1001");
        if (isDestroyed() || checkIsGotoOtherPage() || mIsTryConnectBluetooth) {
            NooieLog.d("-->> debug BluetoothScanActivity onDeviceFailedToConnect: 1002");
            return;
        }
        NooieLog.d("-->> debug BluetoothScanActivity onDeviceFailedToConnect: 1003");
        if (!checkMatchBluetooth(mConnectBluetoothDevice, device)) {
            NooieLog.d("-->> debug BluetoothScanActivity onDeviceFailedToConnect: 1004");
            return;
        }
        NooieLog.d("-->> debug BluetoothScanActivity onDeviceFailedToConnect: 1005");
        hideLoading();
        refreshBleSanView(REFRESH_TYPE_RESET);
    }

    @Override
    public void onDeviceDisconnected(@NotNull BluetoothDevice device) {
        NooieLog.d("-->> debug BluetoothScanActivity onDeviceDisconnected: 1001");
        if (isDestroyed() || checkIsGotoOtherPage() || mIsTryConnectBluetooth) {
            NooieLog.d("-->> debug BluetoothScanActivity onDeviceDisconnected: 1002");
            return;
        }
        NooieLog.d("-->> debug BluetoothScanActivity onDeviceDisconnected: 1003");
        if (!checkMatchBluetooth(mConnectBluetoothDevice, device)) {
            NooieLog.d("-->> debug BluetoothScanActivity onDeviceDisconnected: 1004");
            return;
        }
        if (checkIsNeedToReconnectBluetooth(mConnectBluetoothDevice, device, mLastStartConnectBluetooth)) {
            return;
        }
        NooieLog.d("-->> debug BluetoothScanActivity onDeviceDisconnected: 1005");
        hideLoading();
        refreshBleSanView(REFRESH_TYPE_RESET);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ConstantValue.REQUEST_CODE_FOR_OPENING_LOCATION_PERM_SETTING) {
            mIsIgnoreCheckBeforeScanningBluetooth = false;
        } else if (requestCode == ConstantValue.REQUEST_CODE_FOR_ENABLE_BLUE) {
            if (resultCode == RESULT_OK) {
                mIsIgnoreCheckBeforeScanningBluetooth = true;
                checkBeforeScanningBluetooth();
            } else {
                showPermDenyView();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void permissionsGranted(int requestCode) {
        if (isDestroyed()) {
            return;
        }
        if (requestCode == ConstantValue.REQUEST_CODE_FOR_LOCATION_PERM) {
            checkBeforeScanningBluetooth();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms) && !mIsNormalDenied) {
            mIsNormalDenied = false;
            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.fromParts("package", getPackageName(), null));
                startActivityForResult(intent, ConstantValue.REQUEST_CODE_FOR_OPENING_LOCATION_PERM_SETTING);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showPermDenyView();
        }
    }

    @Override
    public void showCheckLocationPermDialog() {
        hideCheckLocationPermDialog();
        mIsNormalDenied = !EasyPermissions.hasPermissions(NooieApplication.mCtx, ConstantValue.PERM_GROUP_LOCATION) && EasyPermissions.somePermissionDenied(BluetoothScanActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
        if (EasyPermissions.somePermissionDenied(BluetoothScanActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)) {
            mIsIgnoreCheckBeforeScanningBluetooth = true;
            requestPermission(ConstantValue.PERM_GROUP_LOCATION, ConstantValue.REQUEST_CODE_FOR_LOCATION_PERM);
        } else {
            mRequestLocationPermDialog = DialogUtils.showConfirmWithSubMsgDialog(this, R.string.bluetooth_scan_request_location_perm_title, R.string.bluetooth_scan_request_location_perm_content, R.string.cancel_normal, R.string.confirm, new DialogUtils.OnClickConfirmButtonListener() {
                @Override
                public void onClickLeft() {
                    showPermDenyView();
                }

                @Override
                public void onClickRight() {
                    //mIsNormalDenied = !EasyPermissions.hasPermissions(NooieApplication.mCtx, ConstantValue.PERM_GROUP_LOCATION) && EasyPermissions.somePermissionDenied(BluetoothScanActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
                    mIsIgnoreCheckBeforeScanningBluetooth = true;
                    requestPermission(ConstantValue.PERM_GROUP_LOCATION, ConstantValue.REQUEST_CODE_FOR_LOCATION_PERM);
                }
            });
        }
    }

    public void hideCheckLocationPermDialog() {
        if (mRequestLocationPermDialog != null) {
            mRequestLocationPermDialog.dismiss();
            mRequestLocationPermDialog = null;
        }
    }

    private void checkIsShowBluetoothDeclare() {
        if (mIsIgnoreCheckBeforeScanningBluetooth) {
            return;
        }
        if (isBluetoothReady()) {
            startScanBluetooth();
            return;
        }
        checkBeforeScanningBluetooth();
    }

    private void checkBeforeScanningBluetooth() {
        if (!checkUseLocationEnable()) {
            requestLocationPerm(getString(R.string.bluetooth_scan_location_request_title), getString(R.string.bluetooth_scan_location_request_content), getString(R.string.cancel_normal), getString(R.string.bluetooth_scan_location_request_allow), new DialogUtils.OnClickConfirmButtonListener() {
                @Override
                public void onClickLeft() {
                    showPermDenyView();
                }

                @Override
                public void onClickRight() {
                }
            }, true);
        } else if (!BluetoothHelper.isBluetoothOn()) {
            mIsIgnoreCheckBeforeScanningBluetooth = true;
            BluetoothHelper.startBluetooth(this, ConstantValue.REQUEST_CODE_FOR_ENABLE_BLUE);
        } else {
            startScanBluetooth();
        }
    }

    private void showPermDenyView() {
        displayBlueScanView(BLUETOOTH_SCAN_VIEW_STATE_PERM_DENY);
    }

    private void showBluetoothScanView(int state) {
        displayBlueScanView(state);
    }

    private void displayBlueScanView(int state) {
        vBluetoothScanNone.setTag(state);
        hideBluetoothScanView();
        switch (state) {
            case BLUETOOTH_SCAN_VIEW_STATE_WAITING: {
                vBluetoothScanNone.setVisibility(View.VISIBLE);
                ivBluetoothScanWaiting.setVisibility(View.VISIBLE);
                tvBluetoothScanWaitingTip.setVisibility(View.VISIBLE);
                break;
            }
            case BLUETOOTH_SCAN_VIEW_STATE_PERM_DENY: {
                vBluetoothScanNone.setVisibility(View.VISIBLE);
                tvBluetoothScanNoneTip.setVisibility(View.VISIBLE);
                ivBluetoothScanNone.setVisibility(View.VISIBLE);
                tvBluetoothScanNoneTip_1.setVisibility(View.VISIBLE);
                btnBluetoothScanNoneRetry.setVisibility(View.VISIBLE);

                ivBluetoothScanNone.setImageResource(R.drawable.bluetooth_perm_deny);
                tvBluetoothScanNoneTip.setText(R.string.bluetooth_scan_perm_reject_declare_title);
                tvBluetoothScanNoneTip_1.setText(R.string.bluetooth_scan_perm_reject_declare_content);
                btnBluetoothScanNoneRetry.setText(R.string.bluetooth_scan_perm_reject_declare_reconnect);
                break;
            }
            case BLUETOOTH_SCAN_VIEW_STATE_NO_FOUND: {
                vBluetoothScanNone.setVisibility(View.VISIBLE);
                tvBluetoothScanNoneTip.setVisibility(View.VISIBLE);
                ivBluetoothScanNone.setVisibility(View.VISIBLE);
                tvBluetoothScanNoneTip_1.setVisibility(View.VISIBLE);
                btnBluetoothScanNoneRetry.setVisibility(View.VISIBLE);
                tvBluetoothScanNoneDeleteDevice.setVisibility(getScanType() == ConstantValue.BLUETOOTH_SCAN_TYPE_EXIST ? View.VISIBLE : View.GONE);

                ivBluetoothScanNone.setImageResource(R.drawable.bluetooth_no_found);
                tvBluetoothScanNoneTip.setText(R.string.bluetooth_scan_no_found_title);
                tvBluetoothScanNoneTip_1.setText(R.string.bluetooth_scan_no_found_content);
                btnBluetoothScanNoneRetry.setText(R.string.bluetooth_scan_no_found_research);
                break;
            }
            case BLUETOOTH_SCAN_VIEW_STATE_SCANNING: {
                ivBluetoothScanState.setVisibility(View.VISIBLE);
                tvBluetoothScanState.setVisibility(View.VISIBLE);
                rvBluetoothScanList.setVisibility(View.VISIBLE);
                tvBluetoothScanState.setText(R.string.bluetooth_scan_state_scanning);
                ivBluetoothScanState.setImageResource(R.drawable.small_loading);
                startScanningLoading();
                break;
            }
            case BLUETOOTH_SCAN_VIEW_STATE_SCAN_FINISH: {
                ivBluetoothScanState.setVisibility(View.VISIBLE);
                tvBluetoothScanState.setVisibility(View.VISIBLE);
                rvBluetoothScanList.setVisibility(View.VISIBLE);
                tvBluetoothScanState.setText(R.string.bluetooth_scan_state_result);
                ivBluetoothScanState.setImageResource(R.drawable.small_retry_icon);
                break;
            }
        }
    }

    private void hideBluetoothScanView() {
        stopScanningLoading();
        ivBluetoothScanState.setVisibility(View.GONE);
        tvBluetoothScanState.setVisibility(View.GONE);
        rvBluetoothScanList.setVisibility(View.GONE);
        vBluetoothScanNone.setVisibility(View.GONE);
        ivBluetoothScanWaiting.setVisibility(View.GONE);
        tvBluetoothScanWaitingTip.setVisibility(View.GONE);
        tvBluetoothScanNoneTip.setVisibility(View.GONE);
        ivBluetoothScanNone.setVisibility(View.GONE);
        tvBluetoothScanNoneTip_1.setVisibility(View.GONE);
        btnBluetoothScanNoneRetry.setVisibility(View.GONE);
        tvBluetoothScanNoneDeleteDevice.setVisibility(View.GONE);
    }

    private int getBlueScanViewState() {
        return vBluetoothScanNone != null && vBluetoothScanNone.getTag() != null ? (Integer)vBluetoothScanNone.getTag() : BLUETOOTH_SCAN_VIEW_STATE_WAITING;
    }

    private void setupBluetoothDeviceView() {
        tvBluetoothScanNoneDeleteDevice.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvBluetoothScanList.setLayoutManager(layoutManager);
        rvBluetoothScanList.addItemDecoration(getItemDecoration());

        mAdapter = new ScanBluetoothDeviceAdapter();
        mAdapter.setListener(new ScanBluetoothDeviceListener() {
            @Override
            public void onItemClick(BleDevice bleDevice) {
                if (isDestroyed()) {
                    return;
                }
                checkBeforeConnectBluetooth(bleDevice);
                //tryConnectBluetooth(bleDevice);
            }
        });
        mAdapter.setSelectedBleDevice(null);
        rvBluetoothScanList.setAdapter(mAdapter);
    }

    private RecyclerView.ItemDecoration getItemDecoration() {
        return new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.bottom = DisplayUtil.dpToPx(NooieApplication.mCtx, 5);//这里增加了20的上边距
            }
        };
    }

    private void dealBleDeviceList(int connectState, BluetoothDevice device) {
        if (isDestroyed()) {
            return;
        }
        if (!checkMatchBluetooth(mConnectBluetoothDevice, device)) {
            return;
        }
        if (connectState == BleConnectState.DEVICE_READY) {
            wakeDeviceByBle();
            //openHotSpotByBle();
            if (mAdapter != null) {
                mAdapter.setSelectedBleDevice(null);
            }
        } else if (mAdapter != null) {
            mAdapter.setSelectedBleDevice(null);
        }
    }

    private void startScanBluetooth() {
        if (isDestroyed()) {
            return;
        }
        resetScanListView();
        showBluetoothScanView(BLUETOOTH_SCAN_VIEW_STATE_SCANNING);
        startScanBle(DEFAULT_BLE_SCAN_TIME, new OnBleStartScanListener() {
            @Override
            public void onTimeFinish() {
                refreshBleSanView(REFRESH_TYPE_FINISH);
            }
        });
    }

    private void stopScanBluetooth() {
        stopScanBle();
    }

    private void refreshBleSanView(int refreshType) {
        if (isDestroyed()) {
            return;
        }
        if (refreshType == REFRESH_TYPE_SCANNING) {
            if (mAdapter != null) {
                mAdapter.updateBluetoothDevice(BleDeviceScanCache.getInstance().getAllCache());
            }
            return;
        }
        if (BleDeviceScanCache.getInstance().isEmpty()) {
            showBluetoothScanView(BLUETOOTH_SCAN_VIEW_STATE_NO_FOUND);
        } else {
            showBluetoothScanView(BLUETOOTH_SCAN_VIEW_STATE_SCAN_FINISH);
            if (mAdapter != null) {
                if (refreshType == REFRESH_TYPE_FINISH) {
                    mAdapter.updateBluetoothDevice(BleDeviceScanCache.getInstance().getAllCache());
                } else {
                    mAdapter.resetBluetoothDevice(BleDeviceScanCache.getInstance().getAllCache());
                }
            }
        }
    }

    private void resetScanListView() {
        if (mAdapter != null) {
            mAdapter.clearData();
        }
    }

    private void checkBeforeConnectBluetooth(BleDevice bleDevice) {
        NooieLog.d("-->> debug BluetoothScanActivity checkBeforeConnectBluetooth 1001");
        if (bleDevice == null || bleDevice.getDevice() == null) {
            return;
        }
        NooieLog.d("-->> debug BluetoothScanActivity checkBeforeConnectBluetooth 1002 name=" + bleDevice.getDevice().getName() + " address=" + bleDevice.getDevice().getAddress());
        if (mConnectBluetoothDevice != null) {
            NooieLog.d("-->> debug BluetoothScanActivity checkBeforeConnectBluetooth 1003 name=" + mConnectBluetoothDevice.getName() + " address=" + mConnectBluetoothDevice.getAddress());
        }
        NooieLog.d("-->> debug BluetoothScanActivity checkBeforeConnectBluetooth 1004 isScanning=" + checkBluetoothIsScanning() + " isBleConnect=" + SmartBleManager.core.isBleConnect());
        if (checkBluetoothIsScanning()) {
            stopScanBluetooth();
            refreshBleSanView(REFRESH_TYPE_FINISH);
        }
        mIsTryConnectBluetooth = true;
        cancelRetryConnectBluetooth();
        showLoading();
        boolean isCurrentBleDevice = checkMatchBluetooth(mConnectBluetoothDevice, bleDevice.getDevice());
        boolean isDisconnectCurrentConnection = !(isCurrentBleDevice && SmartBleManager.core.isConnectedBluetoothDevice(bleDevice.getDevice())) && SmartBleManager.core.isBleConnect();
        NooieLog.d("-->> debug BluetoothScanActivity checkBeforeConnectBluetooth 1005 isCurrentBleDevice=" + isCurrentBleDevice + " isDisconnectCurrentConnection=" + isDisconnectCurrentConnection);
        Observable.just(isDisconnectCurrentConnection)
                .flatMap(new Func1<Boolean, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(Boolean isDisconnect) {
                        NooieLog.d("-->> debug BluetoothScanActivity checkBeforeConnectBluetooth 1006");
                        if (isDisconnect) {
                            NooieLog.d("-->> debug BluetoothScanActivity checkBeforeConnectBluetooth 1007 disconnectBle");
                            SmartBleManager.core.disconnectBle();
                            return Observable.just(true).delay(5000, TimeUnit.MILLISECONDS);
                        }
                        NooieLog.d("-->> debug BluetoothScanActivity checkBeforeConnectBluetooth 1008");
                        return Observable.just(false);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        NooieLog.d("-->> debug BluetoothScanActivity checkBeforeConnectBluetooth 1009");
                        mIsTryConnectBluetooth = false;
                        hideLoading();
                        tryConnectBluetooth(bleDevice);
                    }

                    @Override
                    public void onNext(Boolean isDisconnect) {
                        NooieLog.d("-->> debug BluetoothScanActivity checkBeforeConnectBluetooth 1010");
                        mIsTryConnectBluetooth = false;
                        hideLoading();
                        tryConnectBluetooth(bleDevice);
                    }
                });
    }

    private void tryConnectBluetooth(BleDevice bleDevice) {
        NooieLog.d("-->> debug BluetoothScanActivity tryConnectBluetooth 1001");
        if (bleDevice == null || bleDevice.getDevice() == null) {
            return;
        }
        NooieLog.d("-->> debug BluetoothScanActivity tryConnectBluetooth 1002 checkBluetoothIsScanning=" + checkBluetoothIsScanning());
//        if (checkBluetoothIsScanning()) {
//            stopScanBluetooth();
//            refreshBleSanView(REFRESH_TYPE_FINISH);
//        }
        startConnectBluetooth(bleDevice.getDevice());
    }

    private void startConnectBluetooth(BluetoothDevice bluetoothDevice) {
        NooieLog.d("-->> debug BluetoothScanActivity startConnectBluetooth 1001");
        if (bluetoothDevice == null) {
            return;
        }
        NooieLog.d("-->> debug BluetoothScanActivity startConnectBluetooth 1002");
        mConnectBluetoothDevice = bluetoothDevice;
        mLastStartConnectBluetooth = System.currentTimeMillis();
        mRetryConnectBluetoothCount++;
        connectDevice(bluetoothDevice, new OnBleConnectListener() {
            @Override
            public void onResult(int state, BluetoothDevice bluetoothDevice) {
                NooieLog.d("-->> debug BluetoothScanActivity startConnectBluetooth 1003");
                dealBleDeviceList(state, bluetoothDevice);
            }
        });
    }

    private boolean checkIsNeedToReconnectBluetooth(BluetoothDevice currentBluetoothDevice, BluetoothDevice disconnectBluetoothDevice, long lastConnectBluetooth) {
        boolean isNeedToReconnectBluetooth = checkMatchBluetooth(currentBluetoothDevice, disconnectBluetoothDevice)
                && (System.currentTimeMillis() - lastConnectBluetooth < 17 * 1000) && mRetryConnectBluetoothCount < RETRY_CONNECT_BLUETOOTH_MAX_COUTN
                && !SmartBleManager.core.isBleConnect();
        mIsRetryConnectBluetooth = isNeedToReconnectBluetooth;
        if (isNeedToReconnectBluetooth) {
            startConnectBluetooth(currentBluetoothDevice);
        }
        return isNeedToReconnectBluetooth;
    }

    private void cancelRetryConnectBluetooth() {
        mLastStartConnectBluetooth = 0;
        mRetryConnectBluetoothCount = 0;
        mIsRetryConnectBluetooth = false;
    }

    private void wakeDeviceByBle() {
        showLoading();
        //sendCmd(IpcBleCmd.BLE_CMD_LOGIN);
        tryRetrySendBleCmd(IpcBleCmd.BLE_CMD_LOGIN, IpcBleCmd.BLE_CMD_LOGIN_RSP, mConnectBluetoothDevice);
    }

    private void queryBluetoothPw() {
        showLoading();
        sendCmd(IpcBleCmd.BLE_CMD_QUERY_PW);
    }

    private void openHotSpotByBle() {
        showLoading();
        sendCmd(IpcBleCmd.BLE_CMD_OPEN_HOT_SPOT);
    }

    private void closeHotSpotByBle() {
        showLoading();
        sendCmd(IpcBleCmd.BLE_CMD_CLOSE_HOT_SPOT);
    }

    private void setHotSpotPw(String password, String newPw) {
        StringBuilder pwCmd = new StringBuilder();
        pwCmd.append(IpcBleCmd.BLE_CMD_SET_PW).append(newPw).append("\r");
        sendCmd(pwCmd.toString());
    }

    private void sendDistributeNetworkCmd(String info) {
        //info = "L:8;15;73;WIFI:U:58e9e444731cbf8b;Z:8.00;R:CN;T:WPA;P:\"nooie666\";S:TP-LINK_HyFi_49;";
        if (TextUtils.isEmpty(info)) {
            return;
        }
        List<String> splitCmdList = IpcBleCmd.convertBleLongCmd(info);
        if (CollectionUtil.isNotEmpty(splitCmdList)) {
            try {
                List<String> sendCmdList = new ArrayList<>();
                int splitCmdListSize = CollectionUtil.size(splitCmdList);
                int cmdValueLength = IpcBleCmd.getTextByteSize(info);
                for (int i = 0; i < splitCmdListSize; i++) {
                    String splitCmd = splitCmdList.get(i);
                    sendCmdList.add(String.format(IpcBleCmd.BLE_CMD_DISTRIBUTE_NETWORK_SEND, i, splitCmd));
                }
                sendCmdList.add(String.format(IpcBleCmd.BLE_CMD_DISTRIBUTE_NETWORK_SEND_END, cmdValueLength));
                if (mPresenter != null) {
                    int sendCmdListSize = CollectionUtil.size(sendCmdList);
                    mPresenter.startSendCmdList(sendCmdListSize, new BluetoothScanContract.SendCmdListListener() {
                        @Override
                        public void onSendCmd(int state, int cmdListSize, int cmdIndex) {
                            if (state == SDKConstant.SUCCESS && cmdListSize == sendCmdListSize && CollectionUtil.isIndexSafe(cmdIndex, cmdListSize)) {
                                sendCmd(sendCmdList.get(cmdIndex));
                            }
                        }
                    });
                }
            } catch (Exception e) {
                NooieLog.printStackTrace(e);
            }
        } else {
        }
    }

    private void gotoSetupBleApDevice(boolean isDefaultPw, String ssid, String address) {
        if (isDefaultPw) {
            setIsGotoOtherPage(true);
            Bundle param = new Bundle();
            param.putBoolean(ConstantValue.INTENT_KEY_DATA_PARAM, isDefaultPw);
            param.putString(ConstantValue.INTENT_KEY_SSID, ssid);
            param.putString(ConstantValue.INTENT_KEY_BLE_DEVICE, address);
            param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, getDeviceModel());
            param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, getConnectionMode());
            param.putBoolean(ConstantValue.INTENT_KEY_DATA_PARAM_4, true);
            ModifyCameraPasswordActivity.toModifyCameraPasswordActivity(this, param);
            finish();
        } else {
            openHotSpotByBle();
        }
    }

    private void gotoConnectApDevice(boolean isOpenHotSpot, String ssid, String address) {
        if (isOpenHotSpot) {
            setIsGotoOtherPage(true);
            Bundle param = new Bundle();
            param.putString(ConstantValue.INTENT_KEY_SSID, ssid);
            param.putString(ConstantValue.INTENT_KEY_BLE_DEVICE, address);
            param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, getDeviceModel());
            param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, getConnectionMode());
            param.putBoolean(ConstantValue.INTENT_KEY_DATA_PARAM, false);
            param.putBoolean(ConstantValue.INTENT_KEY_DATA_PARAM_4, true);
            ConnectApDeviceActivity.toConnectApDeviceActivity(this, param);
            finish();
        } else {
            showBluetoothOperationTip(BLUETOOTH_OPERATION_TIP_TYPE_SETTING_FAIL);
        }
    }

    private void gotoConnectQcDevice(String ssid, String address) {
        setIsGotoOtherPage(true);
        Bundle param = new Bundle();
        param.putString(ConstantValue.INTENT_KEY_SSID, ssid);
        param.putString(ConstantValue.INTENT_KEY_BLE_DEVICE, address);
        param.putString(ConstantValue.INTENT_KEY_IPC_MODEL, getDeviceModel());
        param.putInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, getConnectionMode());
        param.putBoolean(ConstantValue.INTENT_KEY_DATA_PARAM_4, true);
        InputWiFiPsdActivity.toInputWiFiPsdActivity(this, param);
        finish();
    }

    private void startScanningLoading() {
        stopScanningLoading();
        mStartScanningAnimator = AnimatorInflater.loadAnimator(this, R.animator.small_loading);
        mStartScanningAnimator.setTarget(ivBluetoothScanState);
        mStartScanningAnimator.start();
    }

    private void stopScanningLoading()  {
        if (mStartScanningAnimator != null) {
            mStartScanningAnimator.end();
            mStartScanningAnimator = null;
        }
    }

    private void showBluetoothOperationTip(int type) {
        boolean isShowBluetoothWarning =  mIsTryConnectBluetooth || mIsRetryConnectBluetooth;
        if (isShowBluetoothWarning) {
            return;
        }
        if (type == BLUETOOTH_OPERATION_TIP_TYPE_UNBIND) {
            showDeviceModeErrorDialog();
        } else if (type == BLUETOOTH_OPERATION_TIP_TYPE_SUCCESS) {
            ToastUtil.showToast(this, R.string.bluetooth_scan_operation_tip_success);
        } else if (type == BLUETOOTH_OPERATION_TIP_TYPE_SETTING_FAIL) {
            ToastUtil.showToast(this, R.string.bluetooth_scan_operation_tip_setting_fail);
        } else {
            ToastUtil.showToast(this, R.string.bluetooth_scan_operation_tip_fail);
        }
    }

    private void showDeviceModeErrorDialog() {
        hideDeviceModeErrorDialog();
        mShowDeviceModeErrorDialog = DialogUtils.showInformationDialog(this, "", getString(R.string.bluetooth_scan_operation_tip_unbind), getString(R.string.bluetooth_scan_operation_tip_unbind_confirm), false, false, new DialogUtils.OnClickInformationDialogLisenter() {
            @Override
            public void onConfirmClick() {
                gotoHomePage();
            }
        });
    }

    private void hideDeviceModeErrorDialog() {
        if (mShowDeviceModeErrorDialog != null) {
            mShowDeviceModeErrorDialog.dismiss();
            mShowDeviceModeErrorDialog = null;
        }
    }

    private void gotoHomePage() {
        setIsGotoOtherPage(true);
        HomeActivity.toHomeActivity(this);
        finish();
    }

    private void checkDeleteBleApDevice(String bleApDeviceId) {
        if (mPresenter != null) {
            mPresenter.checkDeleteBleApDevice(bleApDeviceId);
        }
    }

    private void showDeleteBleApDeviceDialog(String deviceId, String name) {
        hideDeleteBleApDeviceDialog();
        mShowDeleteBleApDeviceDialog = DialogUtils.showConfirmWithSubMsgDialog(this, getString(R.string.camera_settings_remove_camera), String.format(getString(R.string.camera_settings_remove_info), name), R.string.cancel, R.string.confirm_upper, new DialogUtils.OnClickConfirmButtonListener() {
            @Override
            public void onClickLeft() {
            }

            @Override
            public void onClickRight() {
                if (TextUtils.isEmpty(deviceId)) {
                    return;
                }
                if (mPresenter != null) {
                    mPresenter.deleteBleApDevice(deviceId);
                }
            }
        });
    }

    private void hideDeleteBleApDeviceDialog() {
        if (mShowDeleteBleApDeviceDialog != null) {
            mShowDeleteBleApDeviceDialog.dismiss();
            mShowDeleteBleApDeviceDialog = null;
        }
    }

    private void checkAfterBluetoothDisable() {
        if (isDestroyed()) {
            return;
        }
        hideLoading();
        stopScanBluetooth();
        if (mAdapter != null) {
            mAdapter.resetBluetoothDevice(null);
        }
        BleDeviceScanCache.getInstance().clearCache();
        displayBlueScanView(BLUETOOTH_SCAN_VIEW_STATE_WAITING);
        checkBeforeScanningBluetooth();
    }

    private void showBleCmdInvalidTip() {
        boolean isShowBleCmdInvalidTip = System.currentTimeMillis() - mLastShowBleCmdInvalidTip > 3 * 1000;
        if (isShowBleCmdInvalidTip) {
            mLastShowBleCmdInvalidTip = System.currentTimeMillis();
            showBluetoothOperationTip(BLUETOOTH_OPERATION_TIP_TYPE_SETTING_FAIL);
        }
    }

    private String getDeviceModel() {
        if (getStartParam() == null) {
            return null;
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_IPC_MODEL);
    }

    private int getConnectionMode() {
        if (getStartParam() == null) {
            return ConstantValue.CONNECTION_MODE_QC;
        }
        return getStartParam().getInt(ConstantValue.INTENT_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC);
    }

    private int getScanType() {
        if (getStartParam() == null) {
            return ConstantValue.BLUETOOTH_SCAN_TYPE_NEW;
        }
        return getStartParam().getInt(ConstantValue.INTENT_KEY_DATA_PARAM_1, ConstantValue.BLUETOOTH_SCAN_TYPE_NEW);
    }

    private String getBleDeviceId() {
        if (getStartParam() == null) {
            return null;
        }
        return getStartParam().getString(ConstantValue.INTENT_KEY_BLE_DEVICE);
    }
}
