package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

import java.io.File;

/**
 * IMyProfilePresenter
 *
 * @author Administrator
 * @date 2019/2/27
 */
public interface IMyProfilePresenter extends IBasePresenter {
    /**
     * 加载用户信息
     */
    void loadUserInfo();

    /**
     * 退出登录
     */
    void logout();

    /**
     * 设置头像（tuya）
     */
    void setPortrait(File photo);

    void uploadPictures(String userid, String username, String photoPath);

    void getUserInfo(String userId, final String userName, String portraitPath);

    void downloadPortrait(String userId, final String username, String portraitPath);

    void setDownloadPortraitState(boolean isDownloadPortrait);
}
