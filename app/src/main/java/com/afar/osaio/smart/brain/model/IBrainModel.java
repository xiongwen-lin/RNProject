package com.afar.osaio.smart.brain.model;

import com.afar.osaio.base.mvp.IBaseModel;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.entity.brain.BrainTimeResult;
import com.nooie.sdk.api.network.base.bean.entity.brain.BrainUrlResult;

import rx.Observable;

/**
 * BrainModel
 *
 * @author Administrator
 * @date 2019/5/30
 */
public interface IBrainModel extends IBaseModel {

    Observable<BaseResponse<BrainTimeResult>> getBrainTime();

    Observable<BaseResponse<BrainUrlResult>> getBrainUrlByCountry(String country);

    Observable<BaseResponse<BrainUrlResult>> getBrainUrlByAccount(String account, String country);

    Observable<BaseResponse<BrainUrlResult>> getBrainUrlByAccountOrCountry(String account, String country);
}
