package com.afar.osaio.smart.lpipc.presenter;

import com.afar.osaio.smart.lpipc.contract.AddLpSuitContract;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.GatewayDevice;
import com.nooie.sdk.api.network.device.DeviceService;

import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AddLpSuitPresenter implements AddLpSuitContract.Presenter {

    private AddLpSuitContract.View mTaskView;

    public AddLpSuitPresenter(AddLpSuitContract.View view) {
        mTaskView = view;
        mTaskView.setPresenter(this);
    }

    @Override
    public void destroy() {
        if (mTaskView != null) {
            mTaskView.setPresenter(null);
            mTaskView = null;
        }
    }

    @Override
    public void getGatewayNum() {
        DeviceService.getService().getGatewayDevices()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse<List<GatewayDevice>>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onGetGatewayNumResult(ConstantValue.ERROR, 0);
                        }
                    }

                    @Override
                    public void onNext(BaseResponse<List<GatewayDevice>> response) {
                        if (response != null && mTaskView != null) {
                            mTaskView.onGetGatewayNumResult((response.getCode() == StateCode.SUCCESS.code ? ConstantValue.SUCCESS : ConstantValue.ERROR), CollectionUtil.size(response.getData()));
                        }
                    }
                });
    }
}
