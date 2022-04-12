package com.afar.osaio.smart.brain.model;

import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.entity.brain.BrainTimeResult;
import com.nooie.sdk.api.network.base.bean.entity.brain.BrainUrlResult;
import com.nooie.sdk.api.network.brain.BrainService;

import rx.Observable;

/**
 * BrainModel
 *
 * @author Administrator
 * @date 2019/5/30
 */
public class BrainModel implements IBrainModel {

    @Override
    public Observable<BaseResponse<BrainTimeResult>> getBrainTime() {
        return BrainService.getService().getBrainTime();
    }

    @Override
    public Observable<BaseResponse<BrainUrlResult>> getBrainUrlByCountry(String country) {
        return BrainService.getService().getBrainUrlByCountry(country);
    }

    @Override
    public Observable<BaseResponse<BrainUrlResult>> getBrainUrlByAccount(String account, String country) {
        return BrainService.getService().getBrainUrlByAccount(account);
    }

    @Override
    public Observable<BaseResponse<BrainUrlResult>> getBrainUrlByAccountOrCountry(String account, String country) {
        return BrainService.getService().getBrainUrlByAccountOrCountry(account, country);
    }
}
