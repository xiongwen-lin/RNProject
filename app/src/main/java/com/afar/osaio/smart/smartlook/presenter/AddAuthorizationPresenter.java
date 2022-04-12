package com.afar.osaio.smart.smartlook.presenter;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.smartlook.bean.BleDevice;
import com.afar.osaio.smart.smartlook.contract.AddAuthorizationContract;
import com.afar.osaio.smart.smartlook.db.dao.BleDeviceService;
import com.afar.osaio.smart.smartlook.profile.callback.SmartLookManagerCallbacks;
import com.afar.osaio.smart.smartlook.profile.manager.SmartLookManager;
import com.afar.osaio.util.ConstantValue;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class AddAuthorizationPresenter extends BaseBlePresenter implements AddAuthorizationContract.Presenter, SmartLookManagerCallbacks {

    AddAuthorizationContract.View mTaskView;
    private SmartLookManager mSmartLookManager;

    public AddAuthorizationPresenter(AddAuthorizationContract.View view) {
        super();
        mSmartLookManager = new SmartLookManager(NooieApplication.mCtx, this);
        setBleManager(mSmartLookManager);
        mTaskView = view;
        mTaskView.setPresenter(this);
    }

    @Override
    public void addAuthorizationCode(String user, String uid, String phone, String code, BleDevice bleDevice) {
        if (mSmartLookManager != null && mTaskView != null) {
            Observable.just("")
                    .flatMap(new Func1<String, Observable<Boolean>>() {
                        @Override
                        public Observable<Boolean> call(String s) {
                            BleDeviceService.getInstance().addDevice(user, uid, phone, code, ConstantValue.BLE_USER_TYPE_NORMAL, bleDevice);
                            mSmartLookManager.addAuthorCode(phone, code);
                            mSmartLookManager.openAndCloseLock(true);
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
                                mTaskView.notifyAddAuthorizationCodeResult(ConstantValue.ERROR);
                            }
                        }

                        @Override
                        public void onNext(Boolean result) {
                            if (mTaskView != null) {
                                mTaskView.notifyAddAuthorizationCodeResult(ConstantValue.SUCCESS);
                            }
                        }
                    });
        }
    }

    @Override
    public void destroy() {
    }
}
