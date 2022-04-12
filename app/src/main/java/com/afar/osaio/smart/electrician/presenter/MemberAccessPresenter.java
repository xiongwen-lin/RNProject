package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.smart.electrician.view.IMemberAccessView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.ShareSentUserDetailBean;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;
import com.tuya.smart.sdk.api.IResultCallback;

/**
 * MemberPresenter
 *
 * @author jiangzt
 * @date 2019/4/26
 */
public class MemberAccessPresenter implements IMemberAccessPresenter {

    IMemberAccessView mMemberAccessView;

    public MemberAccessPresenter(IMemberAccessView view) {
        mMemberAccessView = view;

    }

    @Override
    public void loadUserShareInfo(long memberId) {
        TuyaHomeSdk.getDeviceShareInstance().getUserShareInfo(memberId, new ITuyaResultCallback<ShareSentUserDetailBean>() {
            @Override
            public void onSuccess(ShareSentUserDetailBean detailBean) {
                if (mMemberAccessView != null) {
                    mMemberAccessView.notifyLoadUserShareInfoSuccess(detailBean);
                }
            }

            @Override
            public void onError(String code, String msg) {
                if (mMemberAccessView != null) {
                    NooieLog.e("------>error getUserShareInfo code " + code + " msg " + msg);
                    mMemberAccessView.notifyLoadUserShareInfoFailed(code);
                }
            }
        });
    }

    @Override
    public void removeDevice(long memberId, String deviceId) {
        TuyaHomeSdk.getDeviceShareInstance().disableDevShare(deviceId, memberId, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                if (mMemberAccessView != null) {
                    mMemberAccessView.notifyRemoveDeviceState(code);
                }
            }

            @Override
            public void onSuccess() {
                if (mMemberAccessView != null) {
                    mMemberAccessView.notifyRemoveDeviceState(ConstantValue.SUCCESS);
                }
            }
        });
    }

}
