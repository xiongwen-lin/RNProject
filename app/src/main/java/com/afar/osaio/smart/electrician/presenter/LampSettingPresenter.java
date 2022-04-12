package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.smart.electrician.model.DeviceModel;
import com.afar.osaio.smart.electrician.model.IDeviceModel;
import com.afar.osaio.smart.electrician.view.ILampSettingView;
import com.afar.osaio.util.ConstantValue;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.BindTyDeviceResult;
import com.nooie.sdk.api.network.device.DeviceService;
import com.tuya.smart.android.device.bean.UpgradeInfoBean;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IGetOtaInfoCallback;
import com.tuya.smart.sdk.api.IOtaListener;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.bean.DeviceBean;

import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * LampSettingPresenter
 *
 * @author Administrator
 * @date 2019/8/260
 */
public class LampSettingPresenter implements ILampSettingPresenter {

    private ILampSettingView mView;
    private IDeviceModel mDeviceModel;

    public LampSettingPresenter(ILampSettingView view, String deviceId) {
        mView = view;
        mDeviceModel = new DeviceModel(deviceId);
    }

    @Override
    public void loadDeviceInfo(String deviceId) {
        DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(deviceId);
        if (mView != null) {
            mView.notifyLoadDeviceInfo(deviceBean);
        }
    }

    @Override
    public void removeDevice() {
        mDeviceModel.removeDevice(new IResultCallback() {
            @Override
            public void onError(String code, String msg) {
                if (mView != null) {
                    mView.notifyRemoveDeviceState(msg);
                }
            }

            @Override
            public void onSuccess() {
                if (mView != null) {
                    mView.notifyRemoveDeviceState(ConstantValue.SUCCESS);
                }
            }
        });
    }


    @Override
    public void removeShareDevice(String devId) {
        TuyaHomeSdk.getDeviceShareInstance().removeReceivedDevShare(devId, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                if (mView != null) {
                    mView.notifyRemoveDeviceState(code);
                }
            }

            @Override
            public void onSuccess() {
                if (mView != null) {
                    mView.notifyRemoveDeviceState(ConstantValue.SUCCESS);
                }
            }
        });

    }

    @Override
    public void resetFactory() {
        mDeviceModel.resetFactory(new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                if (mView != null) {
                    mView.notifyResetFactory(error);
                }
            }

            @Override
            public void onSuccess() {
                if (mView != null) {
                    mView.notifyResetFactory(ConstantValue.SUCCESS);
                }
            }
        });

    }

    @Override
    public void onDevInfoUpdate(String deviceId) {
        DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(deviceId);
        if (mView != null) {
            mView.notifyOnDevInfoUpdateState(deviceBean);
        }
    }

    @Override
    public void isSupportThirdParty(String productId) {
        DeviceService.getService().getTuyaModel(productId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<BindTyDeviceResult>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(BaseResponse<BindTyDeviceResult> baseResponse) {
                        if (mView != null && baseResponse != null && baseResponse.getData() != null && baseResponse.getCode() == StateCode.SUCCESS.code) {
                            mView.notifyShowThirdParty(baseResponse.getData().getList().get(0));
                        }
                    }
                });
    }

    @Override
    public void loadOtaInfo() {
        mDeviceModel.getOtaInfo(new IGetOtaInfoCallback() {
            @Override
            public void onSuccess(List<UpgradeInfoBean> infoList) {
                if (infoList != null && infoList.size() > 0) {
                    UpgradeInfoBean upgradeInfoBean = null;
                    for (UpgradeInfoBean upgradeInfo : infoList) {
                        if (upgradeInfo != null && upgradeInfo.getType() == ConstantValue.UPGRADE_WIFI_TYPE) {
                            upgradeInfoBean = upgradeInfo;
                        }
                    }
                    if (mView != null) {
                        mView.notifyLoadOtaInfoSuccess(upgradeInfoBean);
                    }
                }
            }

            @Override
            public void onFailure(String code, String msg) {
                if (mView != null) {
                    mView.notifyLoadOtaInfoFailed(msg);
                }
            }
        });
    }

    @Override
    public void registerOtaListener(IOtaListener listener) {
        mDeviceModel.registerOtaListener(listener);
    }

    @Override
    public void startOta() {
        mDeviceModel.startOta();
    }

    @Override
    public void release() {
        mDeviceModel.release();
    }
}
