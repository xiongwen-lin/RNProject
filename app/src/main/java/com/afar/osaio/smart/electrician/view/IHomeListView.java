package com.afar.osaio.smart.electrician.view;

import com.afar.osaio.base.mvp.IBaseView;
import com.tuya.smart.home.sdk.bean.HomeBean;

import java.util.List;

/**
 * IHomeListView
 *
 * @author Administrator
 * @date 2020/2/1
 */
public interface IHomeListView extends IBaseView {

    void notifyLoadHomesSuccess(List<HomeBean> homes);

    void notifyLoadHomesFailed(String code,String msg);

    void notifyChangeHomeState(String success);
}

