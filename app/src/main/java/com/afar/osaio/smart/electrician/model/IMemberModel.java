package com.afar.osaio.smart.electrician.model;

import com.afar.osaio.base.mvp.IBaseModel;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.entity.AccountOfTuyaResult;
import com.nooie.sdk.api.network.base.bean.entity.UidResult;

import rx.Observable;

/**
 * IMemberModel
 *
 * @author Administrator
 * @date 2019/3/15
 */
public interface IMemberModel extends IBaseModel {

    Observable<BaseResponse<UidResult>> getUidByAccount(String account);

    Observable<BaseResponse<AccountOfTuyaResult>> getAccountByUid(String uid);
}
