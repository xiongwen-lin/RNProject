package com.afar.osaio.smart.electrician.presenter;


import com.afar.osaio.base.mvp.IBasePresenter;

/**
 * IMemberPresenter
 *
 * @author Administrator
 * @date 2019/3/15
 */
public interface IMemberPresenter extends IBasePresenter {

    void loadUserShareInfo(long memberId);

    void removeMemberForSingleDevice(long memberId,String devId);

    void getAccount(String uid);

    void removeUserShare(long memberId);

}
