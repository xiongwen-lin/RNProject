package com.afar.osaio.smart.smartlook.profile.manager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import androidx.annotation.NonNull;

import com.afar.osaio.smart.smartlook.bean.BaseBleCmd;
import com.afar.osaio.smart.smartlook.profile.callback.SmartLookManagerCallbacks;
import com.afar.osaio.smart.smartlook.profile.listener.SmartLockManagerListener;
import com.nooie.common.utils.data.ByteUtil;
import com.nooie.common.utils.encrypt.SmartLookEncrypt;
import com.nooie.common.utils.log.NooieLog;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import no.nordicsemi.android.ble.callback.DataSentCallback;
import no.nordicsemi.android.ble.callback.profile.ProfileDataCallback;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.log.LogContract;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class SmartLookManager extends BaseBleManager<SmartLookManagerCallbacks> {
    /**
     *  Service UUID.
     */
    public final static UUID LBS_UUID_SERVICE = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private final static UUID LBS_UUID_NOTIFY_CHAR = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    private final static UUID LBS_UUID_WRITE_CHAR = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");

    private BluetoothGattCharacteristic mNotifyCharacteristic, mWriteCharacteristic;
    private SmartLockManagerListener mListener;

    public SmartLookManager(final Context context) {
        super(context);
    }

    public SmartLookManager(final Context context, SmartLookManagerCallbacks callbacks) {
        super(context);
        setGattCallbacks(callbacks);
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return mGattCallback;
    }

    /**
     * BluetoothGatt callbacks object.
     */
    private final BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {
        @Override
        protected void initialize() {
            setNotificationCallback(mNotifyCharacteristic).with(mNotifyDataCallBack);
            enableNotifications(mNotifyCharacteristic).enqueue();
        }

        @Override
        public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(LBS_UUID_SERVICE);
            if (service != null) {
                mNotifyCharacteristic = service.getCharacteristic(LBS_UUID_NOTIFY_CHAR);
                mWriteCharacteristic = service.getCharacteristic(LBS_UUID_WRITE_CHAR);
            }

            boolean writeRequest = false;
            if (mWriteCharacteristic != null) {
                final int rxProperties = mWriteCharacteristic.getProperties();
                writeRequest = (rxProperties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0;
            }

            return mNotifyCharacteristic != null && mWriteCharacteristic != null && writeRequest;

        }

        @Override
        protected void onDeviceDisconnected() {
            mNotifyCharacteristic = null;
            mWriteCharacteristic = null;
        }
    };

    DataSentCallback mWriteDataCallBack = new DataSentCallback() {
        @Override
        public void onDataSent(@NonNull BluetoothDevice device, @NonNull Data data) {
            log(LogContract.Log.Level.INFO, "-->> write onDataReceived data" + data);
        }
    };

    ProfileDataCallback mNotifyDataCallBack = new ProfileDataCallback() {
        @Override
        public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
            log(LogContract.Log.Level.INFO, "-->> notify onDataReceived data" + data);
            parseB7Result(data.getValue());
            if (mListener != null) {
                mListener.onWriteDataReceive(data.getValue());
            }
        }
    };

    public void setListener(SmartLockManagerListener listener) {
        mListener = listener;
    }

    public boolean isBleDeviceConnected() {
        boolean isConnected = mWriteCharacteristic != null && mNotifyCharacteristic != null;
        return isConnected;
    }

    /**
     * Sends a request to the device to turn the LED on or off.
     *
     * @param on true to turn the LED on, false to turn it off.
     */
    public void send(final boolean on) {
        // Are we connected?
        if (mWriteCharacteristic == null) {
            return;
        }

        Observable.just("")
            .flatMap(new Func1<String, Observable<String>>() {
                @Override
                public Observable<String> call(String s) {
                    initAuthor(true);
                    return Observable.just(s);
                }
            })
            .delay(500, TimeUnit.MILLISECONDS)
            .flatMap(new Func1<String, Observable<String>>() {
                @Override
                public Observable<String> call(String s) {
                    BaseBleCmd checkLogBleCmd = new BaseBleCmd(new short[] { 0xB6 });
                    //writeCharacteristic(mWriteCharacteristic, new Data(SmartLookEncrypt.getEncryptByte(checkLogBleCmd.getCmdBytes()))).with(mWriteDataCallBack).enqueue();
                    //setOpenDirection(false);
                    //getTemporaryPassword();
                    //getAuthorCode();
                    openAndCloseLock(true);
                    return Observable.just(s);
                }
            })
            .delay(500, TimeUnit.MILLISECONDS)
            .flatMap(new Func1<String, Observable<String>>() {
                @Override
                public Observable<String> call(String s) {
                    return Observable.just(s);
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<String>() {
                @Override
                public void onCompleted() {
                }

                @Override
                public void onError(Throwable e) {
                }

                @Override
                public void onNext(String s) {
                }
            });

    }

    public void initAuthor(boolean isManager) {
        if (isManager) {
            String phone = "13060582775";
            SmartLookEncrypt.passwordPhone = Long.parseLong(phone);
            String password = "12341234";
            SmartLookEncrypt.passwordNumber = Long.parseLong(password, 16);
        } else {
            addAuthorCode("13955556666", "31676822");
        }
    }

    public void addManager(String phone, String password) {
        //String phone = "13060582775";
        SmartLookEncrypt.passwordPhone = Long.parseLong(phone);
        //String password = "12341234";
        log(LogContract.Log.Level.INFO,"-->> BlinkyManager addManager phone = " + SmartLookEncrypt.passwordPhone + " password=" + SmartLookEncrypt.passwordNumber);
        short[] passwordBytes = SmartLookEncrypt.getPasswordByte(password);
        BaseBleCmd setPhoneBleCmd = new BaseBleCmd(new short[] { 0xB1 , passwordBytes[0], passwordBytes[1], passwordBytes[2], passwordBytes[3]});
        writeCharacteristic(mWriteCharacteristic, new Data(SmartLookEncrypt.getEncryptByte(setPhoneBleCmd.getCmdBytes()))).with(mWriteDataCallBack).enqueue();
    }

    public void addAuthorCode(String phone, String password) {
        SmartLookEncrypt.passwordPhone = Long.parseLong(phone);
        SmartLookEncrypt.passwordNumber = Long.parseLong(password, 16);
        log(LogContract.Log.Level.INFO,"-->> BlinkyManager addManager phone = " + SmartLookEncrypt.passwordPhone + " password=" + SmartLookEncrypt.passwordNumber);
        short cmdCode = 0xB2;
        short[] cmdBytes = new short[] { cmdCode };
        BaseBleCmd bleCmd = new BaseBleCmd(cmdBytes);
        //writeCharacteristic(mWriteCharacteristic, new Data(SmartLookEncrypt.getEncryptByte(bleCmd.getCmdBytes()))).with(mWriteDataCallBack).enqueue();
    }

    public void openAndCloseLock(boolean open) {
        String time = Long.toHexString(System.currentTimeMillis() / 1000L);
        short openValue = 0x01;
        short closeValue = 0x00;
        short value = open ? openValue : closeValue;
        short cmdCode = 0xB4;
        short[] cmdBytes = new short[] { cmdCode, value, Short.parseShort(time.substring(0, 2), 16), Short.parseShort(time.substring(2, 4), 16), Short.parseShort(time.substring(4, 6), 16), Short.parseShort(time.substring(6, 8), 16) };
        BaseBleCmd bleCmd = new BaseBleCmd(cmdBytes);
        writeCharacteristic(mWriteCharacteristic, new Data(SmartLookEncrypt.getEncryptByte(bleCmd.getCmdBytes()))).with(mWriteDataCallBack).enqueue();
    }

    public void getSmartLookRecord() {
        short cmdCode = 0xB6;
        short[] cmdBytes = new short[] { cmdCode };
        BaseBleCmd bleCmd = new BaseBleCmd(cmdBytes);
        writeCharacteristic(mWriteCharacteristic, new Data(SmartLookEncrypt.getEncryptByte(bleCmd.getCmdBytes()))).with(mWriteDataCallBack).enqueue();
    }

    public void getTemporaryPassword() {
        short cmdCode = 0xBC;
        short[] cmdBytes = new short[] { cmdCode };
        BaseBleCmd bleCmd = new BaseBleCmd(cmdBytes);
        writeCharacteristic(mWriteCharacteristic, new Data(SmartLookEncrypt.getEncryptByte(bleCmd.getCmdBytes()))).with(mWriteDataCallBack).enqueue();
    }

    public void setOpenDirection(boolean isLeft) {
        short cmdCode = 0xC3;
        short leftValue = 0x00;
        short rightValue = 0x01;
        short value = isLeft ? leftValue : rightValue;
        short[] cmdBytes = new short[] { cmdCode, value };
        BaseBleCmd bleCmd = new BaseBleCmd(cmdBytes);
        writeCharacteristic(mWriteCharacteristic, new Data(SmartLookEncrypt.getEncryptByte(bleCmd.getCmdBytes()))).with(mWriteDataCallBack).enqueue();
    }

    public void createAuthorCode() {
        short cmdCode = 0xB7;
        short[] cmdBytes = new short[] { cmdCode };
        BaseBleCmd bleCmd = new BaseBleCmd(cmdBytes);
        writeCharacteristic(mWriteCharacteristic, new Data(SmartLookEncrypt.getEncryptByte(bleCmd.getCmdBytes()))).with(mWriteDataCallBack).enqueue();
    }

    public void getAuthorCode() {
        short cmdCode = 0xB8;
        short[] cmdBytes = new short[] { cmdCode };
        BaseBleCmd bleCmd = new BaseBleCmd(cmdBytes);
        writeCharacteristic(mWriteCharacteristic, new Data(SmartLookEncrypt.getEncryptByte(bleCmd.getCmdBytes()))).with(mWriteDataCallBack).enqueue();
    }

    public void deleteAuthorCode(int id) {
        short cmdCode = 0xB9;
        short codeId = (short)id;
        short[] cmdBytes = new short[] { cmdCode, codeId };
        BaseBleCmd bleCmd = new BaseBleCmd(cmdBytes);
        writeCharacteristic(mWriteCharacteristic, new Data(SmartLookEncrypt.getEncryptByte(bleCmd.getCmdBytes()))).with(mWriteDataCallBack).enqueue();
    }

    public void resetSmartLook() {
        short cmdCode = 0xBF;
        short[] cmdBytes = new short[] { cmdCode };
        BaseBleCmd bleCmd = new BaseBleCmd(cmdBytes);
        writeCharacteristic(mWriteCharacteristic, new Data(SmartLookEncrypt.getEncryptByte(bleCmd.getCmdBytes()))).with(mWriteDataCallBack).enqueue();
    }

    public void renameLock(String name) {
        short cmdCode = 0xC1;
        short[] dataBytes = getDeviceNameByte(name);
        short[] cmdBytes = new short[] {cmdCode, dataBytes[0], dataBytes[1], dataBytes[2], dataBytes[3], dataBytes[4], dataBytes[5]};
        BaseBleCmd bleCmd = new BaseBleCmd(cmdBytes);
        writeCharacteristic(mWriteCharacteristic, new Data(SmartLookEncrypt.getEncryptByte(bleCmd.getCmdBytes()))).with(mWriteDataCallBack).enqueue();
    }

    public static short[] getDeviceNameByte(String paramString) {
        //byte[] paramBytes = paramString.getBytes(Charset.forName("UTF-8"));
        byte[] paramBytes = ByteUtil.hexStr2Byte(paramString);
        short[] arrayOfShort = new short[6];
        NooieLog.d("-->> SmartLookManager getDeviceNameByte param byte len=" + paramBytes.length);
        for (int i = 0; i < paramBytes.length; i++) {
            NooieLog.d("-->> SmartLookManager getDeviceNameByte byte[i]=" + paramBytes[i] );
            arrayOfShort[i] = (short)paramBytes[i];
        }
        return arrayOfShort;
    }

    public void getBattery() {
        short cmdCode = 0xB5;
        short[] cmdBytes = new short[] { cmdCode };
        BaseBleCmd bleCmd = new BaseBleCmd(cmdBytes);
        writeCharacteristic(mWriteCharacteristic, new Data(SmartLookEncrypt.getEncryptByte(bleCmd.getCmdBytes()))).with(mWriteDataCallBack).enqueue();
    }

    public void test() {
        short cmdCode = 0xB5;
        short[] cmdBytes = new short[] { cmdCode };
        BaseBleCmd bleCmd = new BaseBleCmd(cmdBytes);
        writeCharacteristic(mWriteCharacteristic, new Data(SmartLookEncrypt.getEncryptByte(bleCmd.getCmdBytes()))).with(mWriteDataCallBack).enqueue();
    }

    public void parseB7Result(byte[] resultByte) {
        if (resultByte.length > 6 && (resultByte[0] & 0xFF) == 183) {
            if (resultByte[1] == 0) {
                String str1;
                StringBuilder stringBuilder = new StringBuilder();
                if (resultByte[3] == 0) {
                    str1 = "00";
                } else {
                    str1 = Integer.toHexString(resultByte[3] & 0xFF);
                }
                stringBuilder.append(str1);
                if (resultByte[4] == 0) {
                    str1 = "00";
                } else {
                    str1 = Integer.toHexString(resultByte[4] & 0xFF);
                }
                stringBuilder.append(str1);
                if (resultByte[5] == 0) {
                    str1 = "00";
                } else {
                    str1 = Integer.toHexString(resultByte[5] & 0xFF);
                }
                stringBuilder.append(str1);
                if (resultByte[6] == 0) {
                    str1 = "00";
                } else {
                    str1 = Integer.toHexString(resultByte[6] & 0xFF);
                }
                stringBuilder.append(str1);
                log(LogContract.Log.Level.INFO, "-->> parseB7Result sb=" + stringBuilder.toString() + " resultByte[2]=" + resultByte[2]);
                return;
            }
            return;
        }
    }
}
