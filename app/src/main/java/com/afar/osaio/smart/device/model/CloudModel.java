package com.afar.osaio.smart.device.model;

import com.nooie.sdk.api.network.base.bean.BaseResponse;
import com.nooie.sdk.api.network.base.bean.StateCode;
import com.nooie.sdk.api.network.base.bean.entity.aws.AwsFilePreSign;
import com.nooie.sdk.api.network.cloud.CloudService;

import java.io.File;

import retrofit2.Callback;
import retrofit2.Response;
import rx.Observable;
import rx.functions.Func1;

/**
 * CloudModel
 *
 * @author Administrator
 * @date 2019/4/25
 */
public class CloudModel implements ICloudModel {

    @Override
    public Observable<String> getFeedbackPresignUrl(String userId, String username, String filename, int fileSize) {
        return CloudService.getService().getFeedbackUploadPresignUrl(userId, username, filename, fileSize)
                .flatMap(new Func1<BaseResponse<AwsFilePreSign>, Observable<String>>() {
                    @Override
                    public Observable<String> call(BaseResponse<AwsFilePreSign> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            return Observable.just(response.getData().getUrl());
                        }
                        return Observable.just("");
                    }
                });
    }

    @Override
    public Observable<String> getPhotoUploadPreSignUrl(String userId, String username, String filename, int fileSize) {
        return CloudService.getService().getPhotoUploadPresignUrl(userId, username, filename, fileSize)
                .flatMap(new Func1<BaseResponse<AwsFilePreSign>, Observable<String>>() {
                    @Override
                    public Observable<String> call(BaseResponse<AwsFilePreSign> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            return Observable.just(response.getData().getUrl());
                        }
                        return Observable.just("");
                    }
                });
    }

    @Override
    public Observable<String> getPhotoDownloadPreSignUrl(String userId, String filename) {
        return CloudService.getService().getPhotoDownloadPresignUrl(userId,filename)
                .flatMap(new Func1<BaseResponse<AwsFilePreSign>, Observable<String>>() {
                    @Override
                    public Observable<String> call(BaseResponse<AwsFilePreSign> response) {
                        if (response != null && response.getCode() == StateCode.SUCCESS.code && response.getData() != null) {
                            return Observable.just(response.getData().getUrl());
                        }
                        return Observable.just("");
                    }
                });
    }

    @Override
    public void upLoadFileToCloud(String url, String contentType, String usenameParam, File uploadFile, Callback callback) {
        CloudService.getService().upLoadFileToCloud(url, contentType, usenameParam, uploadFile)
                .enqueue(callback);
    }

    @Override
    public Response upLoadFileToCloud(String url, String contentType, String usenameParam, File uploadFile) throws Exception {
        return CloudService.getService().upLoadFileToCloud(url, contentType, usenameParam, uploadFile).execute();
    }
}
