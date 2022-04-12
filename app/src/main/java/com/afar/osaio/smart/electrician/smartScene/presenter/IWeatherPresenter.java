package com.afar.osaio.smart.electrician.smartScene.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

public interface IWeatherPresenter extends IBasePresenter {

    void getWeatherCondition();

    void getCityByLatLng(String longitude,String latitude);
}
