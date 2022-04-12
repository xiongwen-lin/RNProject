package com.afar.osaio.smart.smartlook.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.widget.ImageView;
import android.widget.TextView;

import com.afar.osaio.R;
import com.afar.osaio.base.BaseActivity;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.smartlook.bean.BleDevice;
import com.afar.osaio.smart.smartlook.cache.BleDeviceCache;
import com.afar.osaio.smart.smartlook.contract.AddBluetoothDeviceContract;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.Util;
import com.nooie.common.hardware.bluetooth.BluetoothHelper;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.data.ByteUtil;
import com.nooie.common.utils.encrypt.SmartLookEncrypt;
import com.nooie.common.utils.log.NooieLog;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.EasyPermissions;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class DemoActivity extends BaseActivity {

    private static final String[] PERMS_INCLUDE_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private int REQUEST_CODE_FOR_BLUETOOTH = 3;

    @BindView(R.id.ivLeft)
    ImageView ivLeft;
    @BindView(R.id.tvTitle)
    TextView tvTitle;

    private AddBluetoothDeviceContract.Presenter mPresenter;

    public static void toAddBluetoothDeviceActivity(Context from) {
        Intent intent = new Intent(from, DemoActivity.class);
        from.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bluetooth_device);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    private void initData() {
        //new AddBluetoothDevicePresenter(this);
        initSmartLook();
    }

    private void initView() {
        ivLeft.setImageResource(R.drawable.left_arrow_icon_black_state_list);
        tvTitle.setText(R.string.add_bluetooth_device_title);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        closeGatt(mBluetoothGatt);
    }

    //@Override
    public void setPresenter(@NonNull AddBluetoothDeviceContract.Presenter presenter) {
        mPresenter = presenter;
    }

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    private ScanCallback mScanCallback;
    private BluetoothGatt mBluetoothGatt;

    private static final String nottiUUID = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";

    private static final String serviceUUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";

    private static final String writeUUID = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    // 服务标识
    private final UUID SERVICE_UUID = UUID.fromString(serviceUUID);
    // 特征标识（读取数据）
    private final UUID CHARACTERISTIC_READ_UUID = UUID.fromString(nottiUUID);
    // 特征标识（发送数据）
    private final UUID CHARACTERISTIC_WRITE_UUID = UUID.fromString(writeUUID);

    public static int SMART_LOOK_MANUFACTURER_ID = 0x5847;
    public static int SMART_LOOK_MANUFACTURER_BEFORE_ID = 0x58;
    public static int SMART_LOOK_MANUFACTURER_AFTER_ID = 0x47;

    private void initSmartLook() {
        if (!EasyPermissions.hasPermissions(NooieApplication.mCtx, PERMS_INCLUDE_LOCATION)) {
            requestPermission(PERMS_INCLUDE_LOCATION, REQUEST_CODE_FOR_BLUETOOTH);
            return;
        }
        BluetoothHelper.startBluetooth(this, ConstantValue.REQUEST_CODE_FOR_ENABLE_BLUE);
        mBluetoothAdapter = BluetoothHelper.getBluetoothAdapter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mScanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    if (result == null) {
                        return;
                    }

                    if (result.getDevice() == null) {
                        return;
                    }

                    if (result.getDevice().getName() == null || (result.getScanRecord().getManufacturerSpecificData() == null || result.getScanRecord().getManufacturerSpecificData().size() == 0)) {
                        return;
                    }

                    byte[] manufacturerDataBytes = result.getScanRecord().getManufacturerSpecificData().get(SMART_LOOK_MANUFACTURER_ID);

                    if (manufacturerDataBytes == null || manufacturerDataBytes.length < 11) {
                        return;
                    }

                    if (manufacturerDataBytes[0] == SMART_LOOK_MANUFACTURER_BEFORE_ID && manufacturerDataBytes[1] == SMART_LOOK_MANUFACTURER_AFTER_ID) {
                        NooieLog.d("-->> HomeActivity onScanResult result=" + result.toString() + " scanRecord 5=" + result.getScanRecord().getBytes()[5] + " 6=" + result.getScanRecord().getBytes()[6]);
                        short s1 = (short)(manufacturerDataBytes[9] & 0xFF);
                        short s2 = (short)(manufacturerDataBytes[10] & 0xFF);
                        int rssi = result.getRssi();
                        byte initState = manufacturerDataBytes[8];
                        byte deviceType = manufacturerDataBytes[3];
                        BleDevice bleDevice = new BleDevice(result.getDevice(), rssi, new byte[] { }, initState, deviceType);
                        bleDevice.setUpdateTime(System.currentTimeMillis());
                        BleDeviceCache.getInstance().updateCache(bleDevice);
                    }
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);
                    NooieLog.d("-->> HomeActivity onBatchScanResults");
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                    NooieLog.d("-->> HomeActivity onScanFailed");
                }
            };
            mBluetoothLeScanner = mBluetoothAdapter != null ? mBluetoothAdapter.getBluetoothLeScanner() : null;
        } else {
            mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    NooieLog.d("-->> HomeActivity onLeScan");
                }
            };
        }
        startScanDeviceByTask();
    }

    private Subscription mStartScanTask;
    private static final int MAX_SCAN_BLUE_TOOTH_TIME = 15;
    public void startScanDeviceByTask() {
        stopScanDeviceByTask();
        mStartScanTask = Observable.just("")
                .flatMap(new Func1<String, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(String s) {
                        startBLDeviceScan();
                        return Observable.just(true);
                    }
                })
                .delay(MAX_SCAN_BLUE_TOOTH_TIME, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        stopScanBluetooth();
                    }

                    @Override
                    public void onNext(Boolean result) {
                        stopScanBluetooth();
                        if (CollectionUtil.isNotEmpty(BleDeviceCache.getInstance().getAllCache())) {
                            String key = "D8:C8:02:1C:A2:C3";
                            mBluetoothGatt = connectGatt(key, mGattCallback);
                        }
                    }
                });
    }

    public void stopScanDeviceByTask() {
        if (mStartScanTask != null && !mStartScanTask.isUnsubscribed()) {
            mStartScanTask.unsubscribe();
            mStartScanTask = null;
        }
    }

    public void startBLDeviceScan() {
        if (!BluetoothHelper.isBluetoothOn()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();
            if (bluetoothAdapter != null) {
                if (mBluetoothLeScanner == null) {
                    mBluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                }
                if (mBluetoothLeScanner != null) {
                    ArrayList arrayList = new ArrayList();
                    ScanSettings scanSettings = (new ScanSettings.Builder()).setScanMode(2).build();
                    mBluetoothLeScanner.startScan(arrayList, scanSettings, this.mScanCallback);
                }
            }
        } else {
            BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();
            if (bluetoothAdapter != null) {
                bluetoothAdapter.startLeScan(this.mLeScanCallback);
            }
        }
    }

    private void stopScanBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();
            if (bluetoothAdapter != null) {
                if (mBluetoothLeScanner == null) {
                    mBluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                }
                if (mBluetoothLeScanner != null) {
                    mBluetoothLeScanner.stopScan(this.mScanCallback);
                }
            }
        } else {
            BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();
            if (bluetoothAdapter != null) {
                bluetoothAdapter.stopLeScan(this.mLeScanCallback);
            }
        }
    }

    public BluetoothGatt connectGatt(String key, BluetoothGattCallback gattCallback) {
        BleDevice bleDevice = BleDeviceCache.getInstance().getCacheById(key);
        if (bleDevice != null && bleDevice.getDevice() != null) {
            NooieLog.d("-->> AddBluetoothDeviceActivity connectGatt device=" + bleDevice.getDevice().getName() + " address=" + bleDevice.getDevice().getAddress());
            return bleDevice.getDevice().connectGatt(NooieApplication.mCtx, false, gattCallback);
        }
        return null;
    }

    public void disconnectGatt(BluetoothGatt bluetoothGatt) {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        }
    }

    public void closeGatt(BluetoothGatt bluetoothGatt) {
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
        }
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        /**
         * 连接状态发生改变
         * @param gatt
         * @param status
         * @param newState
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            NooieLog.d("-->> AddBluetoothDeviceActivity onConnectionStateChange status=" + status + " newState=" + newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (gatt != null) {
                    gatt.discoverServices();
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            }
        }

        /**
         * 当服务被发现的时候回调的结果
         * @param gatt
         * @param status
         */
        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            NooieLog.d("-->> AddBluetoothDeviceActivity onServicesDiscovered status=" + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                displayGattServiceList(gatt);
                setBleNotification(gatt);
                Util.delayTask(5000, new Util.OnDelayTaskFinishListener() {
                    @Override
                    public void onFinish() {
                        byte[] data = new byte[]{};
                        sendData(gatt, data);
                    }
                });
            } else {
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            NooieLog.d("-->> AddBluetoothDeviceActivity onCharacteristicChanged characteristic value=" + ByteUtil.byteArrayToHexString(characteristic.getValue()));
        }

        /**
         * 回调响应特征读操作的结果
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            NooieLog.d("-->> AddBluetoothDeviceActivity onCharacteristicRead status=" + status + " characteristic value=" + ByteUtil.byteArrayToHexString(characteristic.getValue()));
        }

        /**
         * 回调响应特征写操作的结果
         * @param gatt
         * @param characteristic
         * @param status
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            NooieLog.d("-->> AddBluetoothDeviceActivity onCharacteristicWrite status=" + status + " characteristic value=" + ByteUtil.byteArrayToHexString(characteristic.getValue()));
        }
    };

    public void setBleNotification(BluetoothGatt bluetoothGatt) {
        if (bluetoothGatt == null) {
            return;
        }

        // 获取蓝牙设备的服务
        BluetoothGattService gattService = bluetoothGatt.getService(SERVICE_UUID);
        if (gattService == null) {
//            sendBleBroadcast(ACTION_CONNECTING_FAIL);
            NooieLog.d("-->> AddBluetoothDeviceActivity setBleNotification gattService null");
            return;
        }

        // 获取蓝牙设备的特征
        BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(CHARACTERISTIC_READ_UUID);
        if (gattCharacteristic == null) {
//            sendBleBroadcast(ACTION_CONNECTING_FAIL);
            NooieLog.d("-->> AddBluetoothDeviceActivity setBleNotification gattCharacteristic null");
            return;
        }

        // 获取蓝牙设备特征的描述符
        List<BluetoothGattDescriptor> gattDescriptors = gattCharacteristic.getDescriptors();
        if (CollectionUtil.isEmpty(gattDescriptors)) {
            NooieLog.d("-->> AddBluetoothDeviceActivity setBleNotification descriptor null");
        }
        for (BluetoothGattDescriptor gattDescriptor : CollectionUtil.safeFor(gattDescriptors)) {
            if (gattDescriptor != null) {
                gattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                if (bluetoothGatt.writeDescriptor(gattDescriptor)) {
                    // 蓝牙设备在数据改变时，通知App，App在收到数据后回调onCharacteristicChanged方法
                    bluetoothGatt.setCharacteristicNotification(gattCharacteristic, true);
                }
            }
        }
    }

    /**
     * 发送数据
     *
     * @param data 数据
     * @return true：发送成功 false：发送失败
     */
    public boolean sendData(BluetoothGatt bluetoothGatt, byte[] data) {
        // 获取蓝牙设备的服务
        BluetoothGattService gattService = null;
        if (bluetoothGatt != null) {
            gattService = bluetoothGatt.getService(SERVICE_UUID);
        }
        if (gattService == null) {
            NooieLog.d("-->> AddBluetoothDeviceActivity sendData gattService null");
            return false;
        }

        // 获取蓝牙设备的特征
        BluetoothGattCharacteristic rgattCharacteristic = gattService.getCharacteristic(CHARACTERISTIC_READ_UUID);
        if (rgattCharacteristic == null) {
            NooieLog.d("-->> AddBluetoothDeviceActivity sendData rgattCharacteristic null");
            return false;
        }
        NooieLog.d("-->> AddBluetoothDeviceActivity sendData read");
        bluetoothGatt.readCharacteristic(rgattCharacteristic);


        // 获取蓝牙设备的特征
        BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(CHARACTERISTIC_WRITE_UUID);
        if (gattCharacteristic == null) {
            NooieLog.d("-->> AddBluetoothDeviceActivity sendData gattCharacteristic null");
            return false;
        }

        NooieLog.d("-->> AddBluetoothDeviceActivity sendData write");
        // 发送数据
        byte[] arrayOfByte = SmartLookEncrypt.getEncryptByte(new short[] { 0xB7 });
        gattCharacteristic.setValue(arrayOfByte);
        bluetoothGatt.writeCharacteristic(gattCharacteristic);
        return true;
    }

    private void displayGattServiceList(BluetoothGatt gatt) {

        List<BluetoothGattService> bluetoothGattServices = gatt.getServices();
        //发现服务是可以在这里查找支持的所有服务
        //BluetoothGattService bluetoothGattService = gatt.getService(UUID.randomUUID());
        for (BluetoothGattService bluetoothGattService : bluetoothGattServices) {
            UUID uuid = bluetoothGattService.getUuid();
            NooieLog.d("-->> AddBluetoothDeviceActivity displayGattServiceList ");
            NooieLog.d("-->> AddBluetoothDeviceActivity displayGattServiceList onServicesDiscovered--uuid=" + uuid);
            List<BluetoothGattCharacteristic> bluetoothGattCharacteristics = bluetoothGattService.getCharacteristics();
            NooieLog.d("-->> AddBluetoothDeviceActivity displayGattServiceList onServicesDiscovered--遍历特征值");
            /*获取指定服务uuid的特征值*/
            //BluetoothGattCharacteristic mBluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(uuid);
            //gatt.readCharacteristic(mBluetoothGattCharacteristic);
            for (BluetoothGattCharacteristic bluetoothGattCharacteristic : CollectionUtil.safeFor(bluetoothGattCharacteristics)) {
                if (bluetoothGattCharacteristic != null) {
                    NooieLog.d("-->> AddBluetoothDeviceActivity displayGattServiceList onServicesDiscovered--特征值 uuid=" + bluetoothGattCharacteristic.getUuid());
                    //gatt.readCharacteristic(bluetoothGattCharacteristic);
                    //bluetoothGattCharacteristic.getValue();

//                    final int charaProp = mBluetoothGattCharacteristic.getProperties();
//                    //bluetoothGattCharacteristic.getWriteType()==BluetoothGattCharacteristic.PROPERTY_READ
//                    /*如果该字符串可读*/
//                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//                        NooieLog.d("-->> AddBluetoothDeviceActivity displayGattServiceList onServicesDiscovered--字符串可读--");
//                        byte[] value = new byte[20];
//                        bluetoothGattCharacteristic.setValue(value[0], BluetoothGattCharacteristic.FORMAT_UINT8, 0);
//                        String writeBytes = "HYL";
//                        bluetoothGattCharacteristic.setValue(writeBytes.getBytes());
//                    }
//                    if (gatt.setCharacteristicNotification(bluetoothGattCharacteristic, true)) {
//                        NooieLog.d("-->> AddBluetoothDeviceActivity displayGattServiceList onServicesDiscovered--设置通知成功=--" + uuid);
//                    }
//                    /*
//                    再从指定的Characteristic中，我们可以通过getDescriptor()方法来获取该特征所包含的descriptor
//				    以上的BluetoothGattService、BluetoothGattCharacteristic、BluetoothGattDescriptor。
//				    我们都可以通过其getUuid()方法，来获取其对应的Uuid，从而判断是否是自己需要的。
//                     */
//                    List<BluetoothGattDescriptor> bluetoothGattDescriptors = bluetoothGattCharacteristic.getDescriptors();
//                    NooieLog.d("-->> AddBluetoothDeviceActivity displayGattServiceList onServicesDiscovered--遍历Descriptor=");
//                    for (BluetoothGattDescriptor bluetoothGattDescriptor : bluetoothGattDescriptors) {
//                        NooieLog.d("-->> AddBluetoothDeviceActivity displayGattServiceList onServicesDiscovered--Descriptor uuid=" + bluetoothGattDescriptor.getUuid());
//                        //bluetoothGattDescriptor.getValue();
//                    }
                }
            }
        }
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter != null ? mBluetoothAdapter : BluetoothHelper.getBluetoothAdapter();
    }
}
