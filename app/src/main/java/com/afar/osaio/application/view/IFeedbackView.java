package com.afar.osaio.application.view;

import com.afar.osaio.base.mvp.IBaseView;
import com.afar.osaio.bean.NooieFeedbackOption;

/**
 * Created by victor on 2018/7/19
 * Email is victor.qiao.0604@gmail.com
 */
public interface IFeedbackView extends IBaseView {
    void showLoadingDialog();

    void hideLoadingDialog();

    void notifyFeedbackResult(String result);

    void onLoadFeedbackInfoSuccess(NooieFeedbackOption option);

    void onLoadFeedbackInfoFailed(String msg);

    void notifyUploadPictureResult(String result, String localPath, String uploadPath);

    void onCopyFileToPrivateStorage(String result, String path);
}
