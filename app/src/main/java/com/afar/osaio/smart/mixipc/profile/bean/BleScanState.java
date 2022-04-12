package com.afar.osaio.smart.mixipc.profile.bean;

public class BleScanState {

    private boolean mScanningStarted;
    private boolean mHasRecords;
    private boolean mBluetoothEnabled;
    private boolean mLocationEnabled;

    public BleScanState(final boolean bluetoothEnabled, final boolean locationEnabled) {
        mScanningStarted = false;
        mBluetoothEnabled = bluetoothEnabled;
        mLocationEnabled = locationEnabled;
    }

    public void scanningStarted() {
        mScanningStarted = true;
    }

    public void scanningStopped() {
        mScanningStarted = false;
    }

    public void bluetoothEnabled() {
        mBluetoothEnabled = true;
    }

    public void bluetoothDisabled() {
        mBluetoothEnabled = false;
        mHasRecords = false;
    }

    public void setLocationEnabled(final boolean enabled) {
        mLocationEnabled = enabled;
    }

    public void recordFound() {
        mHasRecords = true;
    }

    /**
     * Returns whether scanning is in progress.
     */
    public boolean isScanning() {
        return mScanningStarted;
    }

    /**
     * Returns whether any records matching filter criteria has been found.
     */
    public boolean hasRecords() {
        return mHasRecords;
    }

    /**
     * Returns whether Bluetooth adapter is enabled.
     */
    public boolean isBluetoothEnabled() {
        return mBluetoothEnabled;
    }

    /**
     * Returns whether Location is enabled.
     */
    public boolean isLocationEnabled() {
        return mLocationEnabled;
    }

    /**
     * Notifies the observer that scanner has no records to show.
     */
    public void clearRecords() {
        mHasRecords = false;
    }

}
