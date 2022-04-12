package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.smart.electrician.model.INameDeviceModel;
import com.afar.osaio.smart.electrician.model.NameDeviceModel;
import com.afar.osaio.smart.electrician.view.INameDeviceView;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.sdk.api.IResultCallback;

/**
 * NameDevicePresenter
 *
 * @author Administrator
 * @date 2019/3/6
 */
public class NameDevicePresenter implements INameDevicePresenter {

    private INameDeviceView nameDeviceView;
    private INameDeviceModel nameDeviceModel;

    public NameDevicePresenter(INameDeviceView view, String deviceId) {
        nameDeviceView = view;
        nameDeviceModel = new NameDeviceModel(deviceId);
    }

    @Override
    public void addNewDevice(String name) {
        nameDeviceModel.renameDevice(name, new IResultCallback() {
            @Override
            public void onError(String code, String msg) {
                NooieLog.e("--------------->>>renameDevice  code "+code+" msg "+msg);
                if (nameDeviceView != null) {
                    nameDeviceView.onAddDevFailed(msg);
                }
            }

            @Override
            public void onSuccess() {
                NooieLog.e("--------------->>>renameDevice  onSuccess ");
                if (nameDeviceView != null) {
                    nameDeviceView.onAddDevSuccess();
                }
            }
        });
    }
}
