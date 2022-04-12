package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.electrician.model.IMemberModel;
import com.afar.osaio.smart.electrician.model.MemberModel;
import com.afar.osaio.smart.electrician.view.ISingleDeviceShareView;
import com.afar.osaio.util.ConstantValue;
import com.google.gson.Gson;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.UidResult;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.SharedUserInfoBean;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;

import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * SelectShareDevicePresenter
 *
 * @author Administrator
 * @date 2019/3/23
 */
public class SingleDeviceSharePresenter implements ISingleDeviceSharePresenter {

    private ISingleDeviceShareView mSingleDeviceShareView;
    private IMemberModel mMemberModel;

    public SingleDeviceSharePresenter(ISingleDeviceShareView view){
        mSingleDeviceShareView = view;
        mMemberModel = new MemberModel();
    }

    @Override
    public void shareDevices(long homeId, String countryCode, String uid, List<String> deviceIds) {
        NooieLog.e("homeId "+homeId+" countryCode "+countryCode+" uid "+uid+" deviceIds "+new Gson().toJson(deviceIds));
        TuyaHomeSdk.getDeviceShareInstance().addShareWithHomeId(homeId, countryCode, uid, deviceIds, new ITuyaResultCallback<SharedUserInfoBean>() {
            @Override
            public void onSuccess(SharedUserInfoBean sharedUserInfoBean) {
                if (mSingleDeviceShareView != null) {
                    mSingleDeviceShareView.hideLoadingDialog();
                    mSingleDeviceShareView.notifySharedDeviceState(ConstantValue.SUCCESS);
                }
            }

            @Override
            public void onError(String code, String msg) {
                NooieLog.e("------>>>  SingleDeviceSharePresenter code "+code+"  msg "+msg);
                if (mSingleDeviceShareView != null) {
                    if (code.contains("NOT_REG") || msg.contains("未注册")||code.contains("AFTER_USER_REG_INVITE")||msg.contains("The account does not exist.Please try again after it is registered.")){
                        mSingleDeviceShareView.hideLoadingDialog();
                        mSingleDeviceShareView.notifySharedDeviceState(NooieApplication.mCtx.getResources().getString(R.string.different_region_can_not_sharing));
                    }else {
                        mSingleDeviceShareView.hideLoadingDialog();
                        mSingleDeviceShareView.notifySharedDeviceState(msg);
                    }
                }
            }
        });
    }

    @Override
    public void getUidByAccount(String account) {
        if (mSingleDeviceShareView!=null){
            mSingleDeviceShareView.showLoadingDialog();
        }
        mMemberModel.getUidByAccount(account)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<UidResult>>() {
                    @Override
                    public void onNext(BaseResponse<UidResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mSingleDeviceShareView != null && response.getData() != null) {
                            mSingleDeviceShareView.notifyGetUidSuccess(response.getData().getUid());
                        }else if (response != null && response.getCode() == 1055 && mSingleDeviceShareView != null){
                            mSingleDeviceShareView.hideLoadingDialog();
                            mSingleDeviceShareView.notifyUserNotRegister();
                        } else if (mSingleDeviceShareView != null) {
                            mSingleDeviceShareView.hideLoadingDialog();
                            mSingleDeviceShareView.notifyGetUidFailed(String.valueOf(response.getCode()));
                        }
                    }

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mSingleDeviceShareView != null) {
                            mSingleDeviceShareView.hideLoadingDialog();
                            mSingleDeviceShareView.notifyGetUidFailed(ConstantValue.ERROR);
                        }
                    }
                });
    }

}
