package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.smart.electrician.view.IRenameDeviceView;
import com.afar.osaio.util.ConstantValue;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IResultCallback;

/**
 * RenameDevicePresenter
 *
 * @author Administrator
 * @date 2019/3/18
 */
public class RenameDevicePresenter implements IRenameDevicePresenter {

    private IRenameDeviceView mRenameDeviceView;

    public RenameDevicePresenter(IRenameDeviceView view) {
        mRenameDeviceView = view;
    }

    @Override
    public void renameDevice(String deviceId, String name) {
        TuyaHomeSdk.newDeviceInstance(deviceId).renameDevice(name, new IResultCallback() {
            @Override
            public void onError(String code, String msg) {
                if (mRenameDeviceView != null) {
                    mRenameDeviceView.notifyRenameDeviceState(code);
                }
            }

            @Override
            public void onSuccess() {
                if (mRenameDeviceView != null) {
                    mRenameDeviceView.notifyRenameDeviceState(ConstantValue.SUCCESS);
                }
            }
        });
    }

}
