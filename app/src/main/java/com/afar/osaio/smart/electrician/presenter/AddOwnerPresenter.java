package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.electrician.model.IMemberModel;
import com.afar.osaio.smart.electrician.model.MemberModel;
import com.afar.osaio.smart.electrician.view.IAddOwnerView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.UidResult;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.MemberBean;
import com.tuya.smart.home.sdk.callback.ITuyaMemberResultCallback;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * AddOwnerPresenter
 *
 * @author Administrator
 * @date 2019/3/13
 */
public class AddOwnerPresenter implements IAddOwnerPresenter {

    private IAddOwnerView mView;
    private IMemberModel mMemberModel;

    public AddOwnerPresenter(IAddOwnerView view) {
        mView = view;
        mMemberModel = new MemberModel();
    }

    @Override
    public void addMember(long homeId, String countryCode, String userAccount, String name, boolean isAdmin) {
        TuyaHomeSdk.getMemberInstance().addMember(homeId, countryCode, userAccount, name, isAdmin, new ITuyaMemberResultCallback() {
            @Override
            public void onSuccess(MemberBean memberBean) {
                if (mView != null) {
                    mView.notifyAddMemberSuccess(memberBean);
                }
            }

            @Override
            public void onError(String error, String msg) {
                if (mView != null) {
                    if (error.equals("USER_DIFFIENT_COUNTRY_URL") || error.equals("USER_DIFFIENT_COUNTRY")) {
                        msg = NooieApplication.mCtx.getResources().getString(R.string.different_region_can_not_sharing);
                    }
                    mView.notifyAddMemberFailed(msg,true);
                }
            }
        });
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
                        } else if (response != null && response.getCode() == 1055 && mView != null) {
                            mView.hideLoadingDialog();
                            mView.notifyUserNotRegister();
                        } else if (mView != null) {
                            String errorCode = response != null ? String.valueOf(response.getCode()) : ConstantValue.ERROR;
                            NooieLog.e("----------->>> getUidByAccount onNext errorCode " + errorCode);
                            mView.notifyAddMemberFailed(errorCode, false);
                        }
                    }

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mView != null) {
                            mView.notifyAddMemberFailed(ConstantValue.ERROR, false);
                        }
                    }
                });

    }
}

