package com.afar.osaio.smart.electrician.presenter;


import com.afar.osaio.base.mvp.IBasePresenter;

/**
 * IMemberPresenter
 *
 * @author Administrator
 * @date 2019/3/15
 */
public interface IFamilyMemberPresenter extends IBasePresenter {

    void loadUserShareInfo(long memberId);

    void removeMember(long memberId);

    void getAccount(String uid);
}
