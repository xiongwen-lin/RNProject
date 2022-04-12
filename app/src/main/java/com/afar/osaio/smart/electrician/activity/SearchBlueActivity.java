package com.afar.osaio.smart.electrician.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.smart.bluetooth.activity.BaseBluetoothActivity;
import com.afar.osaio.smart.home.tuyable.AddTuYaBlePopupWindows;
import com.afar.osaio.smart.home.tuyable.BlueScanView;
import com.afar.osaio.smart.home.tuyable.DeviceBleInfoBean;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.DialogUtils;
import com.nooie.common.hardware.bluetooth.BluetoothHelper;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.android.ble.api.ScanDeviceBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchBlueActivity extends BaseBluetoothActivity {
    private AddTuYaBlePopupWindows addTuYaBlePopupWindows;
    private String mAddType;

    @BindView(R.id.lay_add_device_search)
    LinearLayout lay_add_device_search;
    @BindView(R.id.ble_view)
    BlueScanView blueScanView;
    @BindView(R.id.tv_ble_search_empty)
    TextView tv_ble_search_empty;
    @BindView(R.id.ivLeft)
    ImageView ivLeft;

    private static int TUYA_BLE_CONFIG_TIME_OUT_EMPTY = 3000;
    private List<ScanDeviceBean> scanDeviceBeanList = new ArrayList<>(); //多个设备
    private Dialog mShowBluetoothDisconnectDialog = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_ble);
        ButterKnife.bind(this);
        mAddType = getCurrentIntent().getStringExtra(ConstantValue.INTENT_KEY_DEVICE_ADD_TYPE);
        initView();
        initBle();
        startScanBleView();
    }

    private  void  initView(){
        ivLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void startScanBleView(){
        showBlueScanView();
        showTuYaBlePopupWindows();
        checkScanDeviceBeanList();
    }

    public static void toSearchBlueActivity( Context from,String addType) {
        Intent intent = new Intent(from, SearchBlueActivity.class);
        intent.putExtra(ConstantValue.INTENT_KEY_DEVICE_ADD_TYPE, addType);
        from.startActivity(intent);
    }

    private  void  showBlueScanView()  {
        blueScanView.setupScanView();
    }

    private void checkScanDeviceBeanList(){
        lay_add_device_search.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (scanDeviceBeanList!=null ||  scanDeviceBeanList.isEmpty()){
                    tv_ble_search_empty.setVisibility(View.VISIBLE);
                }
            }
        }, TUYA_BLE_CONFIG_TIME_OUT_EMPTY);
        tv_ble_search_empty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResetDeviceActivity.toResetDeviceActivity(SearchBlueActivity.this, mAddType);
            }
        });
    }

    /**
     * 打开涂鸦配网
     */
    private void showTuYaBlePopupWindows() {
        NooieLog.d("checkBeforeScanningBluetooth()---showTuYaBlePopupWindows() ");
        if (addTuYaBlePopupWindows != null) {
            addTuYaBlePopupWindows.dismiss();
        }
        addTuYaBlePopupWindows = new AddTuYaBlePopupWindows(this, false,true);
        addTuYaBlePopupWindows.setListener(new AddTuYaBlePopupWindows.AddTuYaBleListener() {

            @Override
            public void onSelectSmartClick(DeviceBleInfoBean deviceBleInfoBean) {
                if (deviceBleInfoBean != null && deviceBleInfoBean.getScanDeviceBean() != null) {
                    ScanDeviceBean scanDeviceBean = deviceBleInfoBean.getScanDeviceBean();
                    InputWiFiPsdActivity.toInputWiFiPsdActivity(SearchBlueActivity.this, ConstantValue.BLUE_MODE, mAddType,
                            scanDeviceBean.getDeviceType(), scanDeviceBean.getUuid(), scanDeviceBean.getAddress(), scanDeviceBean.getMac());
                    finish();
                }
            }

            @Override
            public void closePopView() {
                startScanBleView();
            }


            @Override
            public void startLeScanSuccess(ScanDeviceBean bean) {
                lay_add_device_search.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        addTuYaBlePopupWindows.showAtLocation(lay_add_device_search, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                    }
                }, 100);
                startBLeScanSuccess(bean);
            }

            @Override
            public void startLeScanEmpty() {
                ResetDeviceActivity.toResetDeviceActivity(SearchBlueActivity.this, mAddType);
                finish();
            }
        });

    }

    private void startBLeScanSuccess(ScanDeviceBean bean){
        scanDeviceBeanList.add(bean);
        blueScanView.stopScan(true);
    }


    @Override
    public void bluetoothStateOffChange() {
        if (addTuYaBlePopupWindows != null) {
            addTuYaBlePopupWindows.dismiss();
        }
        //蓝牙关闭
        NooieLog.d("-->> debug SearchBlueActivity bluetoothStateOffChange() ");
        showBluetoothDisconnectDialog();
        blueScanView.stopScan(true);
    }

    /**
     * 蓝牙断开，请求重连
     */
    private void showBluetoothDisconnectDialog() {
        if (mShowBluetoothDisconnectDialog != null) {
            mShowBluetoothDisconnectDialog.dismiss();
            mShowBluetoothDisconnectDialog = null;
        }
        mShowBluetoothDisconnectDialog = DialogUtils.showInformationDialog(this, getString(R.string.bluetooth_scan_operation_tip_disconnect_title), getString(R.string.bluetooth_scan_operation_tip_disconnect_content), getString(R.string.bluetooth_scan_operation_tip_disconnect_ok), false, false, new DialogUtils.OnClickInformationDialogLisenter() {
            @Override
            public void onConfirmClick() {
                BluetoothHelper.startBluetooth(SearchBlueActivity.this, ConstantValue.REQUEST_CODE_FOR_ENABLE_BLUE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (  requestCode == ConstantValue.REQUEST_CODE_FOR_ENABLE_BLUE) {
            if (resultCode == RESULT_OK) {
                startScanBleView();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
