package com.afar.osaio.smart.electrician.presenter;
import com.afar.osaio.smart.electrician.model.IMemberModel;
import com.afar.osaio.smart.electrician.model.MemberModel;
import com.afar.osaio.smart.electrician.view.IMemberView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.AccountOfTuyaResult;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.ShareSentUserDetailBean;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;
import com.tuya.smart.sdk.api.IResultCallback;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * MemberPresenter
 *
 * @author Administrator
 * @date 2019/3/15
 */
public class MemberPresenter implements IMemberPresenter {

    IMemberView mMemberView;
    IMemberModel mMemberModel;

    public MemberPresenter(IMemberView view) {
        mMemberView = view;
        mMemberModel = new MemberModel();
    }


    @Override
    public void loadUserShareInfo(long memberId) {
        TuyaHomeSdk.getDeviceShareInstance().getUserShareInfo(memberId, new ITuyaResultCallback<ShareSentUserDetailBean>() {
            @Override
            public void onSuccess(ShareSentUserDetailBean detailBean) {
                if (mMemberView != null) {
                    mMemberView.notifyLoadUserShareInfoSuccess(detailBean);
                }
            }

            @Override
            public void onError(String code, String msg) {
                if (mMemberView != null) {
                    mMemberView.notifyLoadUserShareInfoFailed(code);
                }
            }
        });
    }


    @Override
    public void removeMemberForSingleDevice(long memberId, String devId) {
        TuyaHomeSdk.getDeviceShareInstance().disableDevShare(devId, memberId, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                if (mMemberView != null) {
                    mMemberView.notifyRemoveMemberState(code);
                }
            }

            @Override
            public void onSuccess() {
                if (mMemberView != null) {
                    mMemberView.notifyRemoveMemberState(ConstantValue.SUCCESS);
                }
            }
        });
    }


    @Override
    public void getAccount(String uid) {
        mMemberModel.getAccountByUid(uid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<AccountOfTuyaResult>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(BaseResponse<AccountOfTuyaResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mMemberView != null && response.getData() != null) {
                            mMemberView.notifyGetAccountSuccess(response.getData().getUid());
                        } else if (mMemberView != null) {
                            mMemberView.notifyGetAccountFailed(String.valueOf(response.getCode()));
                        }
                    }

                });
    }

    @Override
    public void removeUserShare(long memberId) {
        TuyaHomeSdk.getDeviceShareInstance().removeUserShare(memberId, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                if (mMemberView != null) {
                    mMemberView.notifyRemoveUserShareFail(code);
                }
            }

            @Override
            public void onSuccess() {
                if (mMemberView != null) {
                    mMemberView.notifyRemoveUserShareSuccess(ConstantValue.SUCCESS);
                }
            }
        });
    }

}
