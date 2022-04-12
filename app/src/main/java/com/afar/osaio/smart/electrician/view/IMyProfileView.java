package com.afar.osaio.smart.electrician.view;


import com.afar.osaio.base.mvp.IBaseView;
import com.nooie.sdk.api.network.base.bean.entity.UserInfoResult;

/**
 * IMyProfileView
 *
 * @author Administrator
 * @date 2019/2/27
 */
public interface IMyProfileView extends IBaseView {

    /**
     * 加载用户信息回调
     * @param result
     */
    void notifyLoadUserInfoState(String result);

    void notifyLoadUserInfoSucess(UserInfoResult userInfo);

    /**
     * 修改用户昵称回调
     * @param result
     */
    void notifySetNickNameState(String result);

    /**
     * 更换头像回调
     * @param result
     */
    void notifySetPortraitState(String result);

    /**
     * 退出登录回调
     * @param state
     */
    void notifyLogoutState(String state);

    void notifyGetUserInfoResult(String result);

    void notifyRefreshUserPortrait(String result, boolean isUploadPortrait);

}
