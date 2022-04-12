package com.afar.osaio.smart.lpipc.presenter;

import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.bean.SelectProduct;
import com.afar.osaio.smart.device.helper.NooieDeviceHelper;
import com.afar.osaio.smart.lpipc.contract.AddCameraSelectContract;
import com.afar.osaio.util.ConstantValue;
import com.google.gson.reflect.TypeToken;
import com.nooie.common.utils.json.GsonHelper;
import com.nooie.common.utils.json.JsonUtil;
import com.nooie.common.utils.log.NooieLog;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class AddCameraSelectPresenter implements AddCameraSelectContract.Presenter {

    private AddCameraSelectContract.View mTaskView;

    public AddCameraSelectPresenter(AddCameraSelectContract.View view) {
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
    public void loadProductInfo() {
        NooieLog.d("-->> AddCameraSelectPresenter loadProductInfo");
        Observable.just("")
                .flatMap(new Func1<String, Observable<List<SelectProduct>>>() {
                    @Override
                    public Observable<List<SelectProduct>> call(String value) {
                        NooieLog.d("-->> AddCameraSelectPresenter loadProductInfo call 1");
                        String productModelContent = JsonUtil.convertJsonFileFromAssert(NooieApplication.mCtx, "conf/product_model.json");
                        NooieLog.d("-->> AddCameraSelectPresenter loadProductInfo call 2");
                        List<SelectProduct> selectProducts = NooieDeviceHelper.filterEnableSelectProduct(GsonHelper.convertJsonForCollection(productModelContent, new TypeToken<List<SelectProduct>>(){}));
                        NooieLog.d("-->> AddCameraSelectPresenter loadProductInfo call 3");
                        return Observable.just(selectProducts);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<SelectProduct>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mTaskView != null) {
                            mTaskView.onLoadProductInfoResult(ConstantValue.ERROR, null);
                        }
                    }

                    @Override
                    public void onNext(List<SelectProduct> result) {
                        if (mTaskView != null) {
                            mTaskView.onLoadProductInfoResult(ConstantValue.SUCCESS, result);
                        }
                    }
                });
    }
}
