package com.afar.osaio.smart.smartlook.presenter;

import android.bluetooth.BluetoothDevice;
import androidx.annotation.NonNull;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.smartlook.bean.BleConnectState;
import com.afar.osaio.smart.smartlook.bean.BleDevice;
import com.afar.osaio.smart.smartlook.contract.AddAdminContract;
import com.afar.osaio.smart.smartlook.db.dao.BleDeviceService;
import com.afar.osaio.smart.smartlook.profile.callback.SmartLookManagerCallbacks;
import com.afar.osaio.smart.smartlook.profile.manager.SmartLookManager;
import com.afar.osaio.util.ConstantValue;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class AddAdminPresenter extends BaseBlePresenter implements AddAdminContract.Presenter, SmartLookManagerCallbacks {

    AddAdminContract.View mTaskView;
    private SmartLookManager mSmartLookManager;

    public AddAdminPresenter(AddAdminContract.View view) {
        super();
        mSmartLookManager = new SmartLookManager(NooieApplication.mCtx, this);
        setBleManager(mSmartLookManager);
        mTaskView = view;
        mTaskView.setPresenter(this);
    }

    @Override
    public void destroy() {
    }

    @Override
    public void addAdminUser(String user, String uid, String phone, String password, boolean isAdmin, BleDevice bleDevice) {
        if (mSmartLookManager != null) {
            if (isAdmin) {
                mSmartLookManager.addManager(phone, password);
            } else {
                Observable.just("")
                        .flatMap(new Func1<String, Observable<Boolean>>() {
                            @Override
                            public Observable<Boolean> call(String s) {
                                int userType = isAdmin ? ConstantValue.BLE_USER_TYPE_ADMIN : ConstantValue.BLE_USER_TYPE_NORMAL;
                                BleDeviceService.getInstance().addDevice(user, uid, phone, password, userType, bleDevice);
                                mSmartLookManager.addAuthorCode(phone, password);
                                return Observable.just(true);
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
                                if (mTaskView != null) {
                                    mTaskView.notifyAddAdminResult(ConstantValue.ERROR, isAdmin);
                                }
                            }

                            @Override
                            public void onNext(Boolean result) {
                                if (mTaskView != null) {
                                    mTaskView.notifyAddAdminResult(ConstantValue.SUCCESS, isAdmin);
                                }
                            }
                        });
            }
        }
    }

    @Override
    public void onDeviceConnecting(@NonNull final BluetoothDevice device) {
        super.onDeviceConnecting(device);
        if (mTaskView != null) {
            mTaskView.notifyBleDeviceState(BleConnectState.CONNECTING);
        }
    }

    @Override
    public void onDeviceConnected(@NonNull final BluetoothDevice device) {
        super.onDeviceConnected(device);
        if (mTaskView != null) {
            mTaskView.notifyBleDeviceState(BleConnectState.CONNECTED);
        }
    }
}
