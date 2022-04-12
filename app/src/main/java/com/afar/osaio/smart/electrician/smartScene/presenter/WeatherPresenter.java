package com.afar.osaio.smart.electrician.smartScene.presenter;

import com.afar.osaio.smart.electrician.smartScene.view.IWeatherView;
import com.nooie.common.utils.log.NooieLog;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.bean.scene.PlaceFacadeBean;
import com.tuya.smart.home.sdk.bean.scene.condition.ConditionListBean;
import com.tuya.smart.home.sdk.callback.ITuyaResultCallback;

import java.util.List;

public class WeatherPresenter implements IWeatherPresenter {

    private IWeatherView mView;

    public WeatherPresenter(IWeatherView view) {
        this.mView = view;
    }

    @Override
    public void getWeatherCondition() {
        if (mView != null) {
            mView.showLoadingDialog();
        }
        TuyaHomeSdk.getSceneManagerInstance().getConditionList(false, new ITuyaResultCallback<List<ConditionListBean>>() {
            @Override
            public void onSuccess(List<ConditionListBean> result) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyGetConditionListSuccess(result);
                }
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyGetConditionListFail(errorMessage);
                    NooieLog.e("getConditionList error " + errorCode + "  errorMsg  " + errorMessage);
                }
            }
        });
    }

    @Override
    public void getCityByLatLng(String longitude, String latitude) {
        if (mView != null) {
            mView.showLoadingDialog();
        }
        TuyaHomeSdk.getSceneManagerInstance().getCityByLatLng(longitude, latitude, new ITuyaResultCallback<PlaceFacadeBean>() {
            @Override
            public void onSuccess(PlaceFacadeBean result) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyGetCityByLatLngSuccess(result);
                }
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                if (mView != null) {
                    mView.hideLoadingDialog();
                    mView.notifyGetCityByLatLngFail(errorMessage);
                    NooieLog.e("getCityByLatLng error " + errorCode + "  errorMsg  " + errorMessage);
                }
            }
        });
    }
}
