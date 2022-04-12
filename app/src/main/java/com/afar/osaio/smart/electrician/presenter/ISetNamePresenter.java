package com.afar.osaio.smart.electrician.presenter;

import com.afar.osaio.base.mvp.IBasePresenter;

/**
 * ISetPasswordPresenter
 *
 * @author Administrator
 * @date 2019/2/20
 */
public interface ISetNamePresenter extends IBasePresenter {
    /**
     * 修改用户昵称
     *
     * @param nickName
     */
    void modifyUserNickname(String nickName);


    /**
     * 修改成员昵称
     *
     * @param memberId
     * @param remarkName
     */
    void modifyMemberNickname(long memberId, String remarkName);
}
