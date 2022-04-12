package com.afar.osaio.message.presenter;

import com.afar.osaio.message.model.IAppSettingModel;
import com.afar.osaio.message.model.AppSettingModelImpl;
import com.afar.osaio.message.view.IAppSettingView;
import com.afar.osaio.util.ConstantValue;
import com.afar.osaio.util.GlideUtil;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by victor on 2018/7/10
 * Email is victor.qiao.0604@gmail.com
 */
public class AppSettingPresenterImpl implements IAppSettingPresenter {

    private IAppSettingView messagePushView;
    private IAppSettingModel messageSettingModel;

    public AppSettingPresenterImpl(IAppSettingView messagePushView) {
        this.messagePushView = messagePushView;
        messageSettingModel = new AppSettingModelImpl();
    }

    @Override
    public void getCacheSize() {
        messageSettingModel.getCacheSize()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String size) {
                        messagePushView.notifyGetCacheSuccess(size);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (messagePushView != null) {
                            messagePushView.notifyGetCacheFailed("");
                        }
                    }
                });
    }

    @Override
    public void clearCache() {
        messageSettingModel.clearCache()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean result) {
                        GlideUtil.clearMemoryCache();
                        messagePushView.notifyClearCacheResult(ConstantValue.SUCCESS);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (messagePushView != null) {
                            messagePushView.notifyClearCacheResult("");
                        }
                    }
                });
    }
}
