package com.afar.osaio.application.presenter;

import android.net.Uri;
import androidx.annotation.NonNull;

import com.afar.osaio.R;
import com.afar.osaio.application.view.IFeedbackView;
import com.afar.osaio.base.NooieApplication;
import com.afar.osaio.smart.device.model.CloudModel;
import com.afar.osaio.smart.device.model.ICloudModel;
import com.afar.osaio.bean.NooieFeedbackOption;
import com.nooie.common.utils.file.FileUtil;
import com.nooie.common.utils.graphics.BitmapUtil;
import com.afar.osaio.util.ConstantValue;
import com.nooie.common.utils.collection.CollectionUtil;
import com.nooie.common.utils.log.NooieLog;
import com.nooie.common.utils.time.DateTimeUtil;
import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.FeedbackProduct;
import com.nooie.sdk.api.network.base.bean.entity.FeedbackType;
import com.nooie.sdk.api.network.base.core.NetConfigure;
import com.nooie.sdk.api.network.setting.SettingService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Created by victor on 2018/7/19
 * Email is victor.qiao.0604@gmail.com
 */
public class FeedbackPresenterImpl implements IFeedbackPresenter {

    private IFeedbackView mFeedbackView;
    private ICloudModel mCloudModel;

    public FeedbackPresenterImpl(@NonNull IFeedbackView mFeedbackView) {
        this.mFeedbackView = mFeedbackView;

        mCloudModel = new CloudModel();
    }

    @Override
    public void loadFeedbackInfo() {

        Observable<List<FeedbackType>> feedBackTypesObservable = SettingService.getService().getFeedbackType()
                .flatMap(new Func1<BaseResponse<List<FeedbackType>>, Observable<List<FeedbackType>>>() {
                    @Override
                    public Observable<List<FeedbackType>> call(BaseResponse<List<FeedbackType>> response) {
                        List<FeedbackType> feedbackTypes = new ArrayList<>();
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && CollectionUtil.isNotEmpty(response.getData())) {
                            feedbackTypes.addAll(response.getData());
                        }
                        return Observable.just(feedbackTypes);
                    }
                })
                .onErrorReturn(new Func1<Throwable, List<FeedbackType>>() {
                    @Override
                    public List<FeedbackType> call(Throwable throwable) {
                        return null;
                    }
                });

        Observable<List<FeedbackProduct>> productTypesObservable = SettingService.getService().getFeedbackProduct()
                .flatMap(new Func1<BaseResponse<List<FeedbackProduct>>, Observable<List<FeedbackProduct>>>() {
                    @Override
                    public Observable<List<FeedbackProduct>> call(BaseResponse<List<FeedbackProduct>> response) {
                        List<FeedbackProduct> feedbackProducts = new ArrayList<>();
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && CollectionUtil.isNotEmpty(response.getData())) {
                            feedbackProducts.addAll(response.getData());
                        }
                        return Observable.just(feedbackProducts);
                    }
                })
                .onErrorReturn(new Func1<Throwable, List<FeedbackProduct>>() {
                    @Override
                    public List<FeedbackProduct> call(Throwable throwable) {
                        return null;
                    }
                });

        Observable.zip(feedBackTypesObservable, productTypesObservable, new Func2<List<FeedbackType>, List<FeedbackProduct>, NooieFeedbackOption>() {
            @Override
            public NooieFeedbackOption call(List<FeedbackType> feedbackTypes, List<FeedbackProduct> feedbackProducts) {
                NooieFeedbackOption option = new NooieFeedbackOption();
                option.setFeedbackTypes(feedbackTypes);
                option.setFeedbackProducts(feedbackProducts);

                return option;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<NooieFeedbackOption>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mFeedbackView != null) {
                            mFeedbackView.onLoadFeedbackInfoFailed(e.getMessage());
                        }
                    }

                    @Override
                    public void onNext(NooieFeedbackOption option) {
                        if (mFeedbackView != null) {
                            mFeedbackView.onLoadFeedbackInfoSuccess(option);
                        }
                    }
                });
    }

    @Override
    public void postFeedback(final int typeId, final int productId, final String email, final String content, final String images) {
        SettingService.getService().checkFeedback()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(new Func1<BaseResponse, Observable<BaseResponse>>() {
                    @Override
                    public Observable<BaseResponse> call(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code) {
                            return SettingService.getService().postFeedback(typeId, productId, email, content, images);
                        }
                        return Observable.just(response);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BaseResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        updateFeedbackResult(false);
                    }

                    @Override
                    public void onNext(BaseResponse response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && mFeedbackView != null) {
                            updateFeedbackResult(true);
                        } else if (response != null && response.getCode() == StateCode.FEEDBACK_MSG_TOO_MUCH.code && mFeedbackView != null) {
                            mFeedbackView.notifyFeedbackResult(NooieApplication.get().getString(R.string.feedback_send_to_much_msg));
                        } else if (mFeedbackView != null) {
                            updateFeedbackResult(false);
                        }
                    }
                });
    }

    private void updateFeedbackResult(boolean success) {
        if (success) {
            mFeedbackView.notifyFeedbackResult(ConstantValue.SUCCESS);
        } else {
            mFeedbackView.notifyFeedbackResult(NooieApplication.get().getString(R.string.get_fail));
        }
    }

    @Override
    public void upLoadPicture(final String userid, final String username, final String picPath) {

        final StringBuilder fileNameSb = new StringBuilder();
        final String fileName = DateTimeUtil.getOnlyTimeId() + ".jpg";
        //fileNameSb.append(ApiConstant.APP_ID);
        fileNameSb.append(NetConfigure.getInstance().getAppId());
        fileNameSb.append("/");
        fileNameSb.append(userid);
        fileNameSb.append("/");
        fileNameSb.append(fileName);
        final StringBuilder compressPicPathSb = new StringBuilder();

        Observable.just(picPath)
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String filePath) {
                        String compressPicPath = BitmapUtil.compressImage(filePath, FileUtil.getStorageTempFolder(NooieApplication.mCtx), "", 480, 800);
                        compressPicPathSb.append(compressPicPath);
                        NooieLog.d("-->> FeedbackPresenterImpl call compressPicPath=" + compressPicPath);
                        File file =new File(compressPicPath);
                        return mCloudModel.getFeedbackPresignUrl(userid, username, fileName, (int)file.length());
                    }
                })
                .flatMap(new Func1<String, Observable<retrofit2.Response>>() {
                    @Override
                    public Observable<retrofit2.Response> call(String url) {
                        try {
                            String userNameParam = "username=" + username;
                            File uploadFile = new File(compressPicPathSb.toString());
                            if (!uploadFile.exists()) {
                                return Observable.just(null);
                            }
                            retrofit2.Response  response = mCloudModel.upLoadFileToCloud(url, "image/jpeg", userNameParam, uploadFile);
                            FileUtil.deleteFile(uploadFile);
                            return Observable.just(response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return Observable.just(null);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<retrofit2.Response>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mFeedbackView != null) {
                            mFeedbackView.notifyUploadPictureResult(ConstantValue.ERROR, "", "");
                        }
                    }

                    @Override
                    public void onNext(retrofit2.Response response) {
                        if (response != null && response.isSuccessful() && mFeedbackView != null) {
                            mFeedbackView.notifyUploadPictureResult(ConstantValue.SUCCESS, picPath, fileNameSb.toString());
                        } else if (mFeedbackView != null) {
                            mFeedbackView.notifyUploadPictureResult(ConstantValue.ERROR, "", "");
                        }
                    }
                });
    }

    @Override
    public void copyFileToPrivateStorage(Uri srcUri, String targetPath) {
        Observable.just(1)
                .flatMap(new Func1<Integer, Observable<String>>() {
                    @Override
                    public Observable<String> call(Integer value) {
                        FileUtil.copyFile(NooieApplication.mCtx, srcUri, targetPath);
                        return Observable.just(targetPath);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (mFeedbackView != null) {
                            mFeedbackView.onCopyFileToPrivateStorage(ConstantValue.ERROR, null);
                        }
                    }

                    @Override
                    public void onNext(String path) {
                        if (mFeedbackView != null) {
                            mFeedbackView.onCopyFileToPrivateStorage(ConstantValue.SUCCESS, path);
                        }
                    }
                });
    }
}
