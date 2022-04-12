package com.afar.osaio.smart.electrician.presenter;


import com.afar.osaio.base.mvp.IBasePresenter;

import java.util.List;

/**
 * INameGroupPresenter
 *
 * @author Administrator
 * @date 2019/3/20
 */
public interface INameGroupPresenter extends IBasePresenter {

    void createGroup(String productId, String name, List<String> deviceIds);

    void renameGroup(long groupId, String name);
}
