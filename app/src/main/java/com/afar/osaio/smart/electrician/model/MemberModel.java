package com.afar.osaio.smart.electrician.model;

import com.nooie.sdk.api.network.account.AccountService;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.entity.AccountOfTuyaResult;
import com.nooie.sdk.api.network.base.bean.entity.UidResult;

import rx.Observable;

/**
 * MemberModel
 *
 * @author Administrator
 * @date 2019/3/15
 */
public class MemberModel implements IMemberModel {

    @Override
    public rx.Observable<BaseResponse<UidResult>> getUidByAccount(String account) {
        return AccountService.getService().getUidByAccount(account);
    }

    @Override
    public Observable<BaseResponse<AccountOfTuyaResult>> getAccountByUid(String uid) {
        return AccountService.getService().getAccountByUid(uid);
    }

}
