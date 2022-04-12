package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.smart.electrician.model.IMemberModel;
import com.afar.osaio.smart.electrician.model.MemberModel;
import com.afar.osaio.smart.electrician.view.IAddGuestView;
import com.afar.osaio.util.ConstantValue;
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
 * AddGuestPresenter
 *
 * @author Administrator
 * @date 2019/3/13
 */
public class AddGuestPresenter implements IAddGuestPresenter {

    private IAddGuestView mView;
    private IMemberModel mMemberModel;

    public AddGuestPresenter(IAddGuestView view) {
        mView = view;
        mMemberModel = new MemberModel();
    }

    @Override
    public void getUidByAccount(String account) {
        mMemberModel.getUidByAccount(account)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<UidResult>>() {
                    @Override
                    public void onNext(BaseResponse<UidResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mView != null && response.getData() != null) {
                            mView.notifyGetUidSuccess(response.getData().getUid());
                        } else if (response != null && response.getCode() == 1055 && mView != null){
                            mView.hideLoadingDialog();
                            mView.notifyUserNotRegister();
                        }else if (mView != null) {
                            String errorCode = ConstantValue.ERROR;
                            NooieLog.e("----------->>> getUidByAccount onNext errorCode "+errorCode);
                            mView.notifyAddMemberFailed(errorCode,false);
                        }
                    }

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mView != null) {
                            mView.notifyAddMemberFailed(ConstantValue.ERROR,false);
                        }
                    }

                });
    }

    @Override
    public void loadHomeGuest(long homeId) {
        TuyaHomeSdk.getDeviceShareInstance().queryUserShareList(homeId, new ITuyaResultCallback<List<SharedUserInfoBean>>() {
            @Override
            public void onSuccess(List<SharedUserInfoBean> sharedUserInfoBeanList) {
                if (mView != null){
                    mView.notifyHomeGuestSuccess(sharedUserInfoBeanList);
                }
            }

            @Override
            public void onError(String errorMsg, String errorCode) {
                if (mView != null){
                    mView.notifyHomeGuestFailed(errorMsg);
                }
            }
        });

    }
}

