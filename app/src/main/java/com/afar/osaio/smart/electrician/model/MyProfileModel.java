package com.afar.osaio.smart.electrician.model;

import com.nooie.sdk.api.network.account.AccountService;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.entity.UserInfoResult;

import rx.Observable;

/**
 * MyProfileModel
 *
 * @author Administrator
 * @date 2019/2/27
 */
public class MyProfileModel implements IMyProfileModel {

    @Override
    public Observable<BaseResponse<UserInfoResult>> getUserInfo() {
        return AccountService.getService().getUserInfo();
    }

    @Override
    public Observable<BaseResponse> updateNickname(String nickname) {
        return AccountService.getService().updateNickname(nickname);
    }

    @Override
    public Observable<BaseResponse> updateUserPhoto(String photo) {
        return AccountService.getService().updateUserPhoto(photo);
    }

    @Override
    public Observable<BaseResponse> logout() {
        return AccountService.getService().logout();
    }
}
