package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.electrician.view.ISelectShareDeviceView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.SharedUserInfoBean;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;
import com.tuya.smart.sdk.api.IResultCallback;

import java.util.List;

/**
 * SelectShareDevicePresenter
 *
 * @author Administrator
 * @date 2019/3/23
 */
public class SelectShareDevicePresenter implements ISelectShareDevicePresenter {

    private ISelectShareDeviceView mSelectShareDeviceView;

    public SelectShareDevicePresenter(ISelectShareDeviceView view){
        mSelectShareDeviceView = view;
    }

    @Override
    public void addShareWithHomeId(long homeId, String countryCode, String uid, List<String> deviceIds) {
        TuyaHomeSdk.getDeviceShareInstance().addShareWithHomeId(homeId, countryCode, uid, deviceIds, new ITuyaResultCallback<SharedUserInfoBean>() {
            @Override
            public void onSuccess(SharedUserInfoBean sharedUserInfoBean) {
                if (mSelectShareDeviceView != null) {
                    mSelectShareDeviceView.notifySharedDeviceState(ConstantValue.SUCCESS);
                }
            }

            @Override
            public void onError(String code, String message) {
                NooieLog.e("------>error addShareWithHomeId code " + code + " message " + message);
                if (code.contains("NOT_REG") || message.contains("未注册")||code.contains("AFTER_USER_REG_INVITE")||message.contains("The account does not exist.Please try again after it is registered.")){
                    mSelectShareDeviceView.notifySharedDeviceState(NooieApplication.mCtx.getResources().getString(R.string.different_region_can_not_sharing));
                }else {
                    mSelectShareDeviceView.notifySharedDeviceState(message);
                }
            }
        });
    }

    @Override
    public void addShareWithMemberId(long memberId, List<String> deviceIds) {
        TuyaHomeSdk.getDeviceShareInstance().addShareWithMemberId (memberId, deviceIds, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                NooieLog.e("------>error addShareWithHomeId code " + code + " message " + error);
                if (code.contains("NOT_REG") || error.contains("未注册")||code.contains("AFTER_USER_REG_INVITE")||error.contains("The account does not exist.Please try again after it is registered.")){
                    mSelectShareDeviceView.notifySharedDeviceState(NooieApplication.mCtx.getResources().getString(R.string.different_region_can_not_sharing));
                }else {
                    mSelectShareDeviceView.notifySharedDeviceState(error);
                }
            }

            @Override
            public void onSuccess() {
                if (mSelectShareDeviceView != null) {
                    mSelectShareDeviceView.notifySharedDeviceState(ConstantValue.SUCCESS);
                }
            }
        });

    }


}
