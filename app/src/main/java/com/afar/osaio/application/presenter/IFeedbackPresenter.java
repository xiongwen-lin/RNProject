package com.afar.osaio.application.presenter;

import android.net.Uri;

import com.afar.osaio.base.mvp.IBasePresenter;

/**
 * Created by victor on 2018/7/19
 * Email is victor.qiao.0604@gmail.com
 */
public interface IFeedbackPresenter extends IBasePresenter {

    void loadFeedbackInfo();

    void postFeedback(int typeId, int productId, String email, String content, String images);

    void upLoadPicture(String userid, String username, String picPath);

    void copyFileToPrivateStorage(Uri srcUri, String targetPath);
}
