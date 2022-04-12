package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.smart.electrician.view.IPowerStripRenameView;
import com.afar.osaio.util.ConstantValue;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IRequestCallback;
import com.tuya.smart.sdk.api.IResultCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * PowerStripRenamePresenter
 *
 * @author Administrator
 * @date 2019/6/27
 */
public class PowerStripRenamePresenter implements IPowerStripRenamePresenter {

    private IPowerStripRenameView mPowerStirpRenameView;

    public PowerStripRenamePresenter(IPowerStripRenameView view) {
        mPowerStirpRenameView = view;
    }

    @Override
    public void renameDevice(String devId, String dpId, String name) {
        Map<String, Object> postData = new HashMap<String, Object>();
        postData.put("gwId",devId);
        postData.put("devId",devId);
        postData.put("dpId",dpId);
        postData.put("name",name);
        TuyaHomeSdk.getRequestInstance().requestWithApiName("s.m.dev.dp.name.update", "1.0", postData, new IRequestCallback() {
            @Override
            public void onSuccess(Object o) {
                boolean success = (boolean) o;
                if (success && mPowerStirpRenameView != null){
                    mPowerStirpRenameView.notifyRenameDeviceState(ConstantValue.SUCCESS);
                }else if (mPowerStirpRenameView != null){
                    mPowerStirpRenameView.notifyRenameDeviceState("");
                }
            }

            @Override
            public void onFailure(String errorCode, String errorMsg) {
                if (mPowerStirpRenameView != null){
                    mPowerStirpRenameView.notifyRenameDeviceState(errorMsg);
                }
            }
        });
    }

    @Override
    public void renamePowerStrip(String deviceId, String name) {
        TuyaHomeSdk.newDeviceInstance(deviceId).renameDevice(name, new IResultCallback() {
            @Override
            public void onError(String code, String msg) {
                if (mPowerStirpRenameView != null) {
                    mPowerStirpRenameView.notifyRenameDeviceState(code);
                }
            }

            @Override
            public void onSuccess() {
                if (mPowerStirpRenameView != null) {
                    mPowerStirpRenameView.notifyRenameDeviceState(ConstantValue.SUCCESS);
                }
            }
        });
    }

}
