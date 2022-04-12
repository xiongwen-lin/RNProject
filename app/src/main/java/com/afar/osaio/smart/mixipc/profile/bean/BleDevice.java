package com.afar.osaio.smart.mixipc.profile.bean;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

public class BleDevice implements Parcelable {

    private String uid;
    private String account;

    private int deviceType;
    private int initState;
    private BluetoothDevice mDevice;
    private int mRssi;
    private byte[] sec;
    private long updateTime;

    public BleDevice() {
    }

    public BleDevice(BluetoothDevice bluetoothDevice, int rssi, byte[] sec, int initState, int deviceType) {
        this.mDevice = bluetoothDevice;
        this.mRssi = rssi;
        this.sec = sec;
        this.initState = initState;
        this.deviceType = deviceType;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public int getDeviceType() {
        return this.deviceType;
    }

    public int getInitState() {
        return this.initState;
    }

    public byte[] getSec() {
        return this.sec;
    }

    public long getUpdateTime() {
        return this.updateTime;
    }

    public BluetoothDevice getDevice() {
        return this.mDevice;
    }

    public int getRssi() {
        return this.mRssi;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public void setInitState(int initState) {
        this.initState = initState;
    }

    public void setDevice(BluetoothDevice device) {
        this.mDevice = device;
    }

    public void setSec(byte[] sec) {
        this.sec = sec;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public void setRssi(int rssi) {
        this.mRssi = rssi;
    }

    // Parcelable implementation

    private BleDevice(final Parcel in) {
        uid = in.readString();
        account = in.readString();
        mDevice = in.readParcelable(BluetoothDevice.class.getClassLoader());
        mRssi = in.readInt();
        sec = in.createByteArray();
        initState = in.readInt();
        deviceType = in.readInt();
        updateTime = in.readLong();
    }

    @Override
    public void writeToParcel(final Parcel parcel, final int flags) {
        parcel.writeString(uid);
        parcel.writeString(account);
        parcel.writeParcelable(mDevice, flags);
        parcel.writeInt(mRssi);
        parcel.writeByteArray(sec);
        parcel.writeInt(initState);
        parcel.writeInt(deviceType);
        parcel.writeLong(updateTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BleDevice> CREATOR = new Creator<BleDevice>() {
        @Override
        public BleDevice createFromParcel(final Parcel source) {
            return new BleDevice(source);
        }

        @Override
        public BleDevice[] newArray(final int size) {
            return new BleDevice[size];
        }
    };
}
