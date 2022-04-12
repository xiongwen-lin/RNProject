package com.afar.osaio.smart.smartlook.presenter;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.smartlook.bean.BleDevice;
import com.afar.osaio.smart.smartlook.contract.AddBluetoothDeviceContract;
import com.afar.osaio.smart.smartlook.db.dao.BleDeviceService;
import com.nooie.sdk.db.entity.BleDeviceEntity;
import com.afar.osaio.smart.smartlook.profile.callback.SmartLookManagerCallbacks;
import com.afar.osaio.smart.smartlook.profile.manager.SmartLookManager;
import com.afar.osaio.util.ConstantValue;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class AddBluetoothDevicePresenter extends BaseBlePresenter implements AddBluetoothDeviceContract.Presenter, SmartLookManagerCallbacks {

    private AddBluetoothDeviceContract.View mTaskView;
    private SmartLookManager mSmartLookManager;

    public AddBluetoothDevicePresenter(AddBluetoothDeviceContract.View view) {
        super();
        mSmartLookManager = new SmartLookManager(NooieApplication.mCtx, this);
        setBleManager(mSmartLookManager);
        mTaskView = view;
        if (mTaskView != null) {
            mTaskView.setPresenter(this);
        }
    }

    @Override
    public void destroy() {
        mTaskView = null;
    }

    @Override
    public void scanDeviceFinish(String result) {
        if (mTaskView != null) {
            mTaskView.onScanDeviceFinish(result);
        }
    }

    @Override
    public void checkAddBleDevice(BleDevice bleDevice) {
        Observable.just(bleDevice)
                .flatMap(new Func1<BleDevice, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(BleDevice bleDevice) {
                        boolean result = false;
                        if (bleDevice != null && bleDevice.getDevice() != null) {
                            BleDeviceEntity bleDeviceEntity = BleDeviceService.getInstance().getDevice(bleDevice.getAccount(), bleDevice.getDevice().getAddress());
                            result = bleDeviceEntity != null;
                        }
                        return Observable.just(result);
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
                            mTaskView.notifyCheckBleDevice(ConstantValue.ERROR, false, null);
                        }
                    }

                    @Override
                    public void onNext(Boolean result) {
                        if (mTaskView != null) {
                            mTaskView.notifyCheckBleDevice(ConstantValue.SUCCESS, result, bleDevice);
                        }
                    }
                });
    }

}
