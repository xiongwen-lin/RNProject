package com.afar.osaio.account.model;

import com.afar.osaio.base.mvp.IBaseModel;

import java.util.List;

import rx.Observable;

/**
 * IPlatformUpgradeModel
 *
 * @author Administrator
 * @date 2019/4/12
 */
public interface IPlatformUpgradeModel extends IBaseModel {

    Observable<Boolean> reportNooieLog(String uid, String account);

    List<String> getCrashLogForClearing(boolean isDebug);
}
