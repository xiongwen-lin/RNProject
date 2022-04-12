package com.afar.osaio.smart.player.component;

import android.os.Bundle;

import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.player.presenter.PlayComponent;
import com.afar.osaio.util.ConstantValue;
import com.nooie.sdk.api.network.base.bean.entity.BindDevice;
import com.nooie.sdk.bean.IpcType;
import com.nooie.sdk.listener.OnActionResultListener;
import com.nooie.sdk.processor.cmd.DeviceCmdApi;

public class LivePlayerComponent extends PlayComponent {

    private Bundle mParam;

    public void setParam(Bundle param) {
        this.mParam = param;
    }

    public void startVideo() {
        if (getConnectionMode() == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            if (IpcType.getIpcType(getModel()) == IpcType.HC320) {
                startApP2PLive();
            } else {
                startApLive();
            }
        } else {
        }
    }

    public BindDevice getDevice() {
        if (getConnectionMode() == ConstantValue.CONNECTION_MODE_AP_DIRECT) {
            return null;
        } else {
            return NooieDeviceHelper.getDeviceById(getDeviceId());
        }
    }

    private void startApP2PLive() {
        if (player == null) {
            return;
        }
        int modelType = NooieDeviceHelper.convertNooieModel(IpcType.getIpcType(getModel()), getModel());
        player.startAPP2PLive(DeviceCmdApi.getInstance().getApDeviceId(getDeviceId(), getModel()), 0, modelType, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
            }
        });
    }

    private void startApLive() {
        if (player == null) {
            return;
        }
        int modelType = NooieDeviceHelper.convertNooieModel(IpcType.getIpcType(getModel()), getModel());
        player.startAPLive(getDeviceId(), 0, modelType, new OnActionResultListener() {
            @Override
            public void onResult(int code) {
            }
        });
    }

    private int getConnectionMode() {
        if (mParam == null) {
            return ConstantValue.CONNECTION_MODE_QC;
        }
        return mParam.getInt(ConstantValue.PARAM_KEY_CONNECTION_MODE, ConstantValue.CONNECTION_MODE_QC);
    }

    private String getDeviceId() {
        if (mParam == null) {
            return new String();
        }
        return mParam.getString(ConstantValue.PARAM_KEY_DEVICE_ID);
    }

    private String getPDeviceId() {
        if (mParam == null) {
            return new String();
        }
        return mParam.getString(ConstantValue.PARAM_KEY_PDEVICE_ID);
    }

    private String getModel() {
        if (mParam == null) {
            return new String();
        }
        return mParam.getString(ConstantValue.PARAM_KEY_MODEL);
    }
}
