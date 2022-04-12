package com.afar.osaio.smart.electrician.smartScene.view;

import com.afar.osaio.base.mvp.IBaseView;
import com.tuya.smart.home.sdk.bean.scene.PlaceFacadeBean;
import com.tuya.smart.home.sdk.bean.scene.condition.ConditionListBean;

import java.util.List;

public interface IWeatherView extends IBaseView {
    void notifyGetConditionListSuccess(List<ConditionListBean> conditionList);

    void notifyGetConditionListFail(String errorMsg);

    void notifyGetCityByLatLngSuccess(PlaceFacadeBean placeFacadeBean);

    void notifyGetCityByLatLngFail(String errorMsg);
}
