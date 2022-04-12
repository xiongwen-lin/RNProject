package com.afar.osaio.smart.device.presenter;


import com.nooie.common.utils.collection.CollectionUtil;
import com.afar.osaio.R;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.view.INooieShareDeviceView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.data.DataHelper;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.DeviceRelationResult;
import com.nooie.sdk.api.network.base.bean.entity.ShareDeviceResult;
import com.nooie.sdk.api.network.device.DeviceService;

import java.lang.ref.WeakReference;
import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * NooieShareDevicePresenter
 *
 * @author Administrator
 * @date 2019/4/19
 */
public class NooieShareDevicePresenter implements INooieShareDevicePresenter {
    private WeakReference<INooieShareDeviceView> mSharedUsersView;

    public NooieShareDevicePresenter(INooieShareDeviceView sharedUsersView) {
        this.mSharedUsersView = new WeakReference<>(sharedUsersView);
    }

    @Override
    public void destroy() {
        mSharedUsersView = null;
    }

    @Override
    public void getDeviceSharedUserList(String deviceId, int page, int perPage) {
        DeviceService.getService().getDeviceRelationList(deviceId, page, perPage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<DeviceRelationResult>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mSharedUsersView != null && mSharedUsersView.get() != null) {
                            mSharedUsersView.get().onGetShareToUsersError("");
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<DeviceRelationResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mSharedUsersView != null && mSharedUsersView.get() != null) {
                            mSharedUsersView.get().onShowShareToUsers(response.getData());
                        } else if (mSharedUsersView != null && mSharedUsersView.get() != null) {
                            mSharedUsersView.get().onGetShareToUsersError("");
                        }
                    }
                });
    }

    /**
     * 检查用户是否可以被分享设备
     *
     * @param newSharer
     * @param oldSharers
     */
    @Override
    public boolean checkUser(String owner, final String newSharer, List<String> oldSharers) {
        if (owner != null && owner.equals(newSharer) && mSharedUsersView != null && mSharedUsersView.get() != null) {
            mSharedUsersView.get().onShowUserIsYourself(newSharer);
        } else if (CollectionUtil.isNotEmpty(oldSharers) && oldSharers.contains(newSharer) && mSharedUsersView != null && mSharedUsersView.get() != null) {
            mSharedUsersView.get().onUserIsYourSharer("");
        } else  {
            return true;
        }
        return false;
    }

    /**
     * 分享设备
     *
     * @param deviceId
     * @param userAccount
     */
    @Override
    public void shareDevice(String deviceId, final String userAccount) {
        DeviceService.getService().shareDevice(userAccount, deviceId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<ShareDeviceResult>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mSharedUsersView != null && mSharedUsersView.get() != null) {
                            mSharedUsersView.get().onShareDevFailed(StateCode.UNKNOWN.code, userAccount, ConstantValue.SHARE_DEVICE_MAX_COUNT);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<ShareDeviceResult> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mSharedUsersView != null && mSharedUsersView.get() != null) {
                            mSharedUsersView.get().onShareDevSuccess(NooieApplication.get().getString(R.string.shared_send_already_share));
                        } else if (response != null && mSharedUsersView != null && mSharedUsersView.get() != null) {
                            int shareDeviceMaxCount = response.getData() != null ? DataHelper.toInt(response.getData().getNum()) : ConstantValue.SHARE_DEVICE_MAX_COUNT;
                            mSharedUsersView.get().onShareDevFailed(response.getCode(), userAccount, shareDeviceMaxCount);
                        } else if (mSharedUsersView != null && mSharedUsersView.get() != null) {
                            mSharedUsersView.get().onShareDevFailed(StateCode.UNKNOWN.code, userAccount, ConstantValue.SHARE_DEVICE_MAX_COUNT);
                        }
                    }
                });
    }

    @Override
    public void deleteDeviceShared(int sharedId) {
        DeviceService.getService().deleteDeviceRelation(sharedId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mSharedUsersView != null && mSharedUsersView.get() != null) {
                            mSharedUsersView.get().notifyDeleteSharedResult("");
                        }
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mSharedUsersView != null && mSharedUsersView.get() != null) {
                            mSharedUsersView.get().notifyDeleteSharedResult(ConstantValue.SUCCESS);
                        } else if (mSharedUsersView != null && mSharedUsersView.get() != null) {
                            mSharedUsersView.get().notifyDeleteSharedResult("");
                        }
                    }
                });
    }
}
