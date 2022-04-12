package com.afar.osaio.smart.electrician.model;


import com.afar.osaio.base.mvp.IBaseModel;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.entity.UserInfoResult;

import rx.Observable;

/**
 * IMyProfileModel
 *
 * @author Administrator
 * @date 2019/2/27
 */
public interface IMyProfileModel extends IBaseModel {
    /**
     * 用户详情
     * @return
     */
    Observable<BaseResponse<UserInfoResult>> getUserInfo();

    /**
     * 用户昵称修改
     * @param nickname
     * @return
     */
    Observable<BaseResponse> updateNickname(String nickname);

    /**
     * 用户头像修改
     * @param photo
     * @return
     */
    Observable<BaseResponse> updateUserPhoto(String photo);

    /**
     * 用户退出登录
     * @return
     */
    Observable<BaseResponse> logout();
}
